package pl.edu.anstar.recruitment.worker;

import pl.edu.anstar.recruitment.exception.InvalidCreditCardException;
import pl.edu.anstar.recruitment.service.CreditCardService;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CreditCardChargingWorker {

  Logger LOGGER = LoggerFactory.getLogger(CreditCardChargingWorker.class);
  @Qualifier("zeebeClientLifecycle")
  @Autowired
  private ZeebeClient zeebeClient;

  @JobWorker(type = "credit-card-charging")
  public void handleCreditCardCharging(final JobClient jobClient, final ActivatedJob job) {
    LOGGER.info("Task/Job definition type: " + job.getType());
    final Map<String, Object> jobVariables = job.getVariablesAsMap();
    for (Map.Entry<String, Object> entry : jobVariables.entrySet()) {
      LOGGER.info("Process variables & task/job variables (e.g. data submitted by user): " + entry.getKey() + " : " + entry.getValue());
    }

    Map<String, Object> variables = job.getVariablesAsMap();
    String cardNumber = variables.get("cardNumber").toString();
    String cvc = variables.get("cvc").toString();
    String expiryDate = variables.get("expiryDate").toString();
    Double amount = Double.valueOf(variables.get("openAmount").toString());

    try {
      new CreditCardService().chargeAmount(cardNumber, cvc, expiryDate, amount);

      jobClient.newCompleteCommand(job).send().join();
    } catch (InvalidCreditCardException e) {
      zeebeClient.newThrowErrorCommand(job).errorCode("creditCardChargeError").send().join();
    } catch (Exception e) {
      jobClient.newFailCommand(job).retries(3).errorMessage(e.getMessage()).send().join();
    }
  }
}
