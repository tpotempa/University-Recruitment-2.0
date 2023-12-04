package pl.edu.anstar.recruitment.service;

import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.auth.SaasAuthentication;
import io.camunda.tasklist.dto.Form;
import io.camunda.tasklist.dto.TaskList;
import io.camunda.tasklist.dto.TaskState;
import io.camunda.tasklist.dto.Variable;
import io.camunda.tasklist.exception.TaskListException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.anstar.recruitment.model.Task;

@Service
public class TaskListService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskListService.class);

    private String baseUrl;

    @Value("${zeebe.client.cloud.clientId:N/A}")
    private String clientId;

    @Value("${zeebe.client.cloud.clientSecret:N/A}")
    private String clientSecret;

    @Value("${zeebe.client.cloud.clusterId:N/A}")
    private String clusterId;

    @Value("${zeebe.client.cloud.region:N/A}")
    private String region;

    @Value("${tasklistUrl:N/A}")
    private String tasklistUrl;

    private CamundaTaskListClient client;

    private CamundaTaskListClient getCamundaTaskListClient() throws TaskListException {

        LOG.info("Creating CamundaTaskListClient.");

        if (client == null) {
            if (!"N/A".equals(clientId)) {
                SaasAuthentication sa = new SaasAuthentication(clientId, clientSecret);
                client =
                        new CamundaTaskListClient.Builder()
                                .shouldReturnVariables()
                                .taskListUrl("https://" + region + ".tasklist.camunda.io/" + clusterId)
                                .authentication(sa)
                                .build();
            }
        }
        return client;
    }

    public Task claim(String taskId, String assignee) throws TaskListException {
        return convert(getCamundaTaskListClient().claim(taskId, assignee));
    }

    public Task unclaim(String taskId) throws TaskListException {
        return convert(getCamundaTaskListClient().unclaim(taskId));
    }

    public Task getTask(String taskId) throws TaskListException {
        return convert(getCamundaTaskListClient().getTask(taskId));
    }

    public List<Task> getGroupTasks(String group, TaskState state, Integer pageSize)
            throws TaskListException {
        return convertTasks(getCamundaTaskListClient().getGroupTasks(group, state, pageSize));
    }

    public List<Task> getAssigneeTasks(String assignee, TaskState state, Integer pageSize)
            throws TaskListException {
        return convertTasks(getCamundaTaskListClient().getAssigneeTasks(assignee, state, pageSize));
    }

    public List<Task> getTasks(TaskState state, Integer pageSize) throws TaskListException {
        return convertTasks(getCamundaTaskListClient().getTasks(null, state, pageSize));
    }

    public TaskList getTaskList(TaskState state, Integer pageSize) throws TaskListException {
        return getCamundaTaskListClient().getTasks(null, state, pageSize);
    }

    public void completeTask(String taskId, Map<String, Object> variables) throws TaskListException {
        getCamundaTaskListClient().completeTask(taskId, variables);
    }

    public String getForm(String processDefinitionId, String formId) throws TaskListException {
        Form form = getCamundaTaskListClient().getForm(formId, processDefinitionId);
        return form.getSchema();
    }

    private Task convert(io.camunda.tasklist.dto.Task task) {
        Task result = new Task();
        BeanUtils.copyProperties(task, result);
        if (task.getVariables() != null) {
            result.setVariables(new HashMap<>());
            for (Variable var : task.getVariables()) {
                result.getVariables().put(var.getName(), var.getValue());
            }
        }
        return result;
    }

    private List<Task> convertTasks(io.camunda.tasklist.dto.TaskList tasks) {
        List<Task> result = new ArrayList<>();
        for (io.camunda.tasklist.dto.Task task : tasks) {
            result.add(convert(task));
        }
        return result;
    }
}