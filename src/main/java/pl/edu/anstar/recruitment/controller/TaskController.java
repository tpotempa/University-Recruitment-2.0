package pl.edu.anstar.recruitment.controller;

import io.camunda.zeebe.client.ZeebeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.text.SimpleDateFormat;
import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.dto.TaskList;
import io.camunda.tasklist.dto.TaskState;
import io.camunda.tasklist.exception.TaskListException;
import pl.edu.anstar.recruitment.service.TaskListService;

@RestController
@RequestMapping("/")
public class TaskController {

    private static final Logger LOG = LoggerFactory.getLogger(TaskController.class);
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    @Autowired
    private TaskListService taskListService;
    @Qualifier("zeebeClientLifecycle")
    @Autowired
    private ZeebeClient client;

    @GetMapping("/assesscandidate")
    // @TODO Check tasks variable faculty.
    public ResponseEntity<TaskList> getAllExamApplications(@RequestParam(required = false) String faculty) {

        TaskList tasks = new TaskList();

        LOG.info("Assessing candidate.");

        try {
            tasks = taskListService.getTaskList(TaskState.CREATED, null);
            LOG.info("TaskList fetched.");

            for(Task task : tasks) {
                //client.completeTask(task.getId(), Map.of("decision", "yes"));
                LOG.info("Task {}.", task.getId());
            }

        } catch (Exception e) {
            LOG.error("Task exception.", e);
        }

        return new ResponseEntity(tasks, HttpStatus.OK);
    }

    @PostMapping("/complete/{taskId}")
    public void completeTask(@PathVariable String taskId, @RequestBody Map<String, Object> variables)
            throws TaskListException {

        LOG.info("Completing task " + taskId + "` with variables: " + variables);

        if (variables.containsKey("lastComment")) {
            String comment = (String) variables.get("lastComment");
            Object commentsVar = variables.get("comments");
            List<Map<String, String>> comments = null;
            Map<String, String> commentToAdd =
                    Map.of(
                            "author",
                            "Tomasz Potempa",
                            "comment",
                            comment,
                            "date",
                            sdf.format(new Date()));
            if (commentsVar == null
                    || (commentsVar instanceof String && "".equals((String) commentsVar))) {
                comments = List.of(commentToAdd);
            } else {
                comments = (List<Map<String, String>>) commentsVar;
                comments.add(commentToAdd);
            }

            variables.put("comments", comments);
            variables.put("decision", "yes");
        }

        taskListService.completeTask(taskId, variables);
    }
}