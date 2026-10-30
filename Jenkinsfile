pipeline {
    agent any

    tools {
        // Must match the JDK name configured in Manage Jenkins -> Tools
        jdk 'jdk21'
    }

    environment {
        DOCKER_IMAGE         = 'sahas000/calc-devops'
        IMAGE_TAG            = "${env.BUILD_NUMBER}"
        // Fixed project name so every build replaces the same containers
        COMPOSE_PROJECT_NAME = 'calc-devops'
    }

    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'java -version'
                sh 'mvn -B clean package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Test') {
            steps {
                sh 'mvn -B test'
            }
            post {
                always {
                    junit allowEmptyResults: false, testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t "$DOCKER_IMAGE:$IMAGE_TAG" -t "$DOCKER_IMAGE:latest" .'
            }
        }

        stage('Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials',
                                                  usernameVariable: 'DOCKER_USER',
                                                  passwordVariable: 'DOCKER_PASS')]) {
                    sh '''
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        docker push "$DOCKER_IMAGE:$IMAGE_TAG"
                        docker push "$DOCKER_IMAGE:latest"
                    '''
                }
            }
            post {
                always {
                    sh 'docker logout || true'
                }
            }
        }

        stage('Deploy') {
            steps {
                // IMAGE_TAG is read by docker-compose.yml, so this deploys exactly this build's image
                sh '''
                    docker compose pull app
                    docker compose up -d --no-build --remove-orphans --wait --wait-timeout 120
                '''
                sh '''
                    running=$(docker inspect --format '{{.Config.Image}}' calc-app)
                    echo "Running image: $running"
                    if [ "$running" != "$DOCKER_IMAGE:$IMAGE_TAG" ]; then
                        echo "Expected $DOCKER_IMAGE:$IMAGE_TAG"
                        exit 1
                    fi

                    health=$(docker inspect --format '{{.State.Health.Status}}' calc-app)
                    echo "calc-app health: $health"
                    [ "$health" = "healthy" ]

                    # Smoke test through nginx
                    curl -fsS --retry 5 --retry-delay 3 --retry-all-errors \
                        "http://localhost/api/v1/add?a=2&b=3"
                    echo
                '''
            }
            post {
                failure {
                    sh 'docker compose ps || true'
                    sh 'docker compose logs --tail=100 || true'
                }
            }
        }
    }

    post {
        success {
            echo "Deployed $DOCKER_IMAGE:$IMAGE_TAG"
        }
        always {
            // Drop dangling layers left by rebuilds
            sh 'docker image prune -f || true'
        }
    }
}
