package pl.edu.anstar.recruitment.worker;

import pl.edu.anstar.recruitment.service.PayerService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CreditDeductionWorker {

  Logger LOGGER = LoggerFactory.getLogger(CreditDeductionWorker.class);

  @JobWorker(type = "credit-deduction")
  public void handleCreditDeduction(final JobClient jobClient, final ActivatedJob job) {
    LOGGER.info("Task/Job definition type: " + job.getType());
    final Map<String, Object> jobVariables = job.getVariablesAsMap();
    for (Map.Entry<String, Object> entry : jobVariables.entrySet()) {
      LOGGER.info("Process variables & task/job variables (e.g. data submitted by user): " + entry.getKey() + " : " + entry.getValue());
    }

    Map<String, Object> variables = job.getVariablesAsMap();
    String paymentId = variables.get("paymentId").toString();
    Double recruitmentFee = Double.valueOf(variables.get("recruitmentFee").toString());

    PayerService creditService = new PayerService();
    double payerCredit = creditService.getPayerCredit();
    double openAmount = creditService.deductCredit(payerCredit, recruitmentFee);

    variables.put("paymentId", variables.get("paymentId").toString());
    variables.put("cardNumber", "4005 5500 0000 0019");
    variables.put("cvc", "111");
    variables.put("expiryDate", "4/2026");

    variables.put("payerCredit", payerCredit);
    variables.put("openAmount", openAmount);

    jobClient.newCompleteCommand(job).variables(variables).send().join();
  }
}
