package com.opsCo.calc_devops.dto;

public record CalculatorResponse(
    String operation,
    double a,
    double b,
    double result
    ) {}
