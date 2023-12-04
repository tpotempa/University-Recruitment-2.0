package pl.edu.anstar.recruitment.service;

public class PayerService {

  public double getPayerCredit() {
    return Double.valueOf(0);
  }

  public double deductCredit(double payerCredit, double amount) {
    return payerCredit > amount ? 0.0 : amount - payerCredit;
  }
}