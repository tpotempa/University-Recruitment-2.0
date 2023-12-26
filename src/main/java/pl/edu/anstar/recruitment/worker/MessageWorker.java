package pl.edu.anstar.recruitment.worker;

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
import pl.edu.anstar.recruitment.service.RandomService;

@Component
public class MessageWorker {

  Logger LOGGER = LoggerFactory.getLogger(MessageWorker.class);

  @Qualifier("zeebeClientLifecycle")
  @Autowired
  private ZeebeClient zeebeClient;

  @JobWorker(type = "payment-invocation")
  public void handlePaymentInvocation(final JobClient jobClient, final ActivatedJob job) {
    LOGGER.info("Task/Job definition type: " + job.getType());
    final Map<String, Object> variables = job.getVariablesAsMap();
    for (Map.Entry<String, Object> entry : variables.entrySet()) {
      LOGGER.info("Process variables & task/job variables (e.g. data submitted by user): " + entry.getKey() + " : " + entry.getValue());
    }

    RandomService rs = new RandomService();
    String paymentId = rs.generateRandom(40);
    variables.put("paymentId", paymentId);
    variables.put("recruitmentFee", variables.get("recruitmentFee").toString());

    String messageName = "paymentRequestMessage";
    zeebeClient.newPublishMessageCommand()
        .messageName(messageName)
        .correlationKey(paymentId)
        .variables(variables)
        .send().join();

    LOGGER.info("Message name (sent): " + messageName);
    LOGGER.info("Correlation key (created and sent): " + paymentId);
    LOGGER.info("Then process instance goes into subscription mode with use of catching message event.");

    jobClient.newCompleteCommand(job).variables(variables).send().join();
  }

  @JobWorker(type = "payment-completion")
  public void handlePaymentCompletion(final JobClient jobClient, final ActivatedJob job) {
    LOGGER.info("Task/Job definition type: " + job.getType());
    final Map<String, Object> variables = job.getVariablesAsMap();
    LOGGER.info("Correlation key (sent): " + variables.get("paymentId").toString());
    LOGGER.info("Broker seeks form process instance in subscription mode with the correlation key.");
    for (Map.Entry<String, Object> entry : variables.entrySet()) {
      LOGGER.info("Process variables & task/job variables (e.g. data submitted by user): " + entry.getKey() + " : " + entry.getValue());
    }

    zeebeClient.newPublishMessageCommand()
        .messageName("paymentCompletedMessage")
        .correlationKey(variables.get("paymentId").toString())
        .send().join();

    jobClient.newCompleteCommand(job).send().join();
  }

}