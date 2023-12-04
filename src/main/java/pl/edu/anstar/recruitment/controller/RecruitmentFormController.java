package pl.edu.anstar.recruitment.controller;

import java.util.Map;
import io.camunda.zeebe.client.ZeebeClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.anstar.recruitment.ProcessConstants;
import pl.edu.anstar.recruitment.service.TaskListService;


@RestController
@RequestMapping("/")
public class RecruitmentFormController {

  private static final Logger LOG = LoggerFactory.getLogger(RecruitmentFormController.class);

  @Autowired
  private TaskListService taskListService;
  @Qualifier("zeebeClientLifecycle")
  @Autowired
  private ZeebeClient client;

  @PostMapping("/start")
  public void startProcessInstance(@RequestBody Map<String, Object> variables) {

    LOG.info("Starting process " + ProcessConstants.BPMN_PROCESS_ID + " with variables: " + variables);

    //variables.put("orderTotal", 200);

    client
            .newCreateInstanceCommand()
            .bpmnProcessId(ProcessConstants.BPMN_PROCESS_ID)
            .latestVersion()
            .variables(variables)
            .send();
  }
}