package pl.edu.anstar.recruitment.service;

import pl.edu.anstar.recruitment.exception.InvalidCreditCardException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreditCardService {

  Logger LOGGER = LoggerFactory.getLogger(CreditCardService.class);

  public void chargeAmount(String cardNumber, String cvc, String expiryDate, Double amount)
      throws InvalidCreditCardException {
    if (expiryDate.length() == 7) {
      LOGGER.info("Credit card number: " + cardNumber + ", CVC: " + cvc + ", Expiry date: " + expiryDate + ", Total amount: " + amount);
    } else {
      LOGGER.error("The credit card expiry date is invalid: " + expiryDate);

      throw new InvalidCreditCardException("Invalid credit card expiry date!");
    }
  }
}