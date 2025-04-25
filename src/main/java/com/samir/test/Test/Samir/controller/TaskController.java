package com.samir.test.Test.Samir.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.samir.test.Test.Samir.model.Task;
import com.samir.test.Test.Samir.service.TaskService;
import com.samir.test.Test.Samir.dto.TaskResponse;
import com.samir.test.Test.Samir.dto.TaskDTO;
import com.samir.test.Test.Samir.dto.NumbersRequest;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDTO createTask(@RequestBody Task task, @RequestHeader("Authorization") String token) {
        return taskService.createTask(task, token);
    }

    @GetMapping
    public List<TaskResponse> getTasks(@RequestHeader("Authorization") String token) {
        return taskService.getTasks(token);
    }

    @PutMapping("/{id}/complete")
    public TaskDTO markTaskCompleted(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        return taskService.markTaskCompleted(id, token);
    }

    @GetMapping("/incomplete")
    public List<TaskDTO> getIncompleteTasks(@RequestHeader("Authorization") String token) {
        return taskService.getIncompleteTasks(token);
    }

    @PostMapping("/calculate/sum-even")
    public int calculateSumOfEvenNumbers(@RequestBody NumbersRequest request) {
        return request.getNumbers().stream()
                .filter(num -> num % 2 == 0)
                .mapToInt(Integer::intValue)
                .sum();
    }
}
