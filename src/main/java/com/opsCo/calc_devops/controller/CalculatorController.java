package com.opsCo.calc_devops.controller;
import com.opsCo.calc_devops.dto.CalculatorResponse;
import com.opsCo.calc_devops.service.CalculatorService;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class CalculatorController {

  private final CalculatorService calculatorService;

  public CalculatorController(CalculatorService calculatorService) {
    this.calculatorService = calculatorService;
  }

  @GetMapping("/add")
  public CalculatorResponse add(
      @RequestParam double a,
      @RequestParam double b) {

      double result = calculatorService.add(a, b);

      return new CalculatorResponse("add", a, b, result);
      }

  @GetMapping("/subtract")
  public CalculatorResponse subtract(
      @RequestParam double a,
      @RequestParam double b) {

      double result = calculatorService.subtract(a, b);

      return new CalculatorResponse("subtract", a, b, result);
      }

  @GetMapping("/multiply")
  public CalculatorResponse multiply(
      @RequestParam double a,
      @RequestParam double b) {

      double result = calculatorService.multiply(a, b);

      return new CalculatorResponse("multiply", a, b, result);
      }

  @GetMapping("/divide")
  public CalculatorResponse divide(
      @RequestParam double a,
      @RequestParam double b) {

      double result = calculatorService.divide(a, b);

      return new CalculatorResponse("divide", a, b, result);
      }
}
