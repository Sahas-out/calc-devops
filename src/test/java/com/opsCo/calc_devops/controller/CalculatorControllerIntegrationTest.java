package com.opsCo.calc_devops.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class CalculatorControllerIntegrationTest {

  @Autowired private WebApplicationContext context;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
  }

  // ---- happy paths ----

  @Test
  void add() throws Exception {
    mockMvc.perform(get("/api/v1/add").param("a", "2").param("b", "3"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.operation").value("add"))
        .andExpect(jsonPath("$.a").value(2.0))
        .andExpect(jsonPath("$.b").value(3.0))
        .andExpect(jsonPath("$.result").value(5.0));
  }

  @Test
  void subtract() throws Exception {
    mockMvc.perform(get("/api/v1/subtract").param("a", "2").param("b", "3"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.operation").value("subtract"))
        .andExpect(jsonPath("$.result").value(-1.0));
  }

  @Test
  void multiply() throws Exception {
    mockMvc.perform(get("/api/v1/multiply").param("a", "2").param("b", "3"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.operation").value("multiply"))
        .andExpect(jsonPath("$.result").value(6.0));
  }

  @Test
  void divide() throws Exception {
    mockMvc.perform(get("/api/v1/divide").param("a", "5").param("b", "2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.operation").value("divide"))
        .andExpect(jsonPath("$.result").value(2.5));
  }

  // ---- division by zero ----

  @Test
  void divideByZeroReturnsBadRequest() throws Exception {
    mockMvc.perform(get("/api/v1/divide").param("a", "5").param("b", "0"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Division by zero is not allowed"));
  }

  // ---- missing parameters ----

  @Test
  void addMissingParameters() throws Exception {
    mockMvc.perform(get("/api/v1/add").param("a", "1")).andExpect(status().isBadRequest());
    mockMvc.perform(get("/api/v1/add").param("b", "1")).andExpect(status().isBadRequest());
    mockMvc.perform(get("/api/v1/add")).andExpect(status().isBadRequest());
  }

  @Test
  void subtractMissingParameters() throws Exception {
    mockMvc.perform(get("/api/v1/subtract").param("a", "1")).andExpect(status().isBadRequest());
    mockMvc.perform(get("/api/v1/subtract").param("b", "1")).andExpect(status().isBadRequest());
    mockMvc.perform(get("/api/v1/subtract")).andExpect(status().isBadRequest());
  }

  @Test
  void multiplyMissingParameters() throws Exception {
    mockMvc.perform(get("/api/v1/multiply").param("a", "1")).andExpect(status().isBadRequest());
    mockMvc.perform(get("/api/v1/multiply").param("b", "1")).andExpect(status().isBadRequest());
    mockMvc.perform(get("/api/v1/multiply")).andExpect(status().isBadRequest());
  }

  @Test
  void divideMissingParameters() throws Exception {
    mockMvc.perform(get("/api/v1/divide").param("a", "1")).andExpect(status().isBadRequest());
    mockMvc.perform(get("/api/v1/divide").param("b", "1")).andExpect(status().isBadRequest());
    mockMvc.perform(get("/api/v1/divide")).andExpect(status().isBadRequest());
  }
}
