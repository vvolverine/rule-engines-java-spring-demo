package dev.wolverine.flowable.demo.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.wolverine.flowable.demo.domain.Article;
import dev.wolverine.flowable.demo.domain.Approval;

@Service
public class ArticleWorkflowService {
    private final RuntimeService runtimeService;
    private final TaskService taskService;

    @Autowired
    public ArticleWorkflowService(RuntimeService runtimeService, TaskService taskService) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
    }

    @Transactional
    public void startProcess(Article article) {
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("author", article.getAuthor());
        variables.put("url", article.getUrl());
        runtimeService.startProcessInstanceByKey("articleReview", variables);
    }

    @Transactional
    public List<Article> getTasks(String assignee) {
        List<Task> tasks = taskService.createTaskQuery()
          .taskCandidateGroup(assignee)
          .list();
        return tasks.stream()
          .map(task -> {
              Map<String, Object> variables = taskService.getVariables(task.getId());
              return new Article(task.getId(), (String) variables.get("author"), (String) variables.get("url"));
          })
          .collect(Collectors.toList());
    }

    @Transactional
    public void submitReview(Approval approval) {
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("approved", approval.isStatus());
        taskService.complete(approval.getId(), variables);
    }
}
