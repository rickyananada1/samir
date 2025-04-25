package com.samir.test.Test.Samir.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import lombok.extern.slf4j.Slf4j;

import com.samir.test.Test.Samir.model.Task;
import com.samir.test.Test.Samir.model.User;
import com.samir.test.Test.Samir.repository.TaskRepository;
import com.samir.test.Test.Samir.repository.UserRepository;
import com.samir.test.Test.Samir.security.JwtUtil;
import com.samir.test.Test.Samir.exception.UnauthorizedException;
import com.samir.test.Test.Samir.exception.ResourceNotFoundException;
import com.samir.test.Test.Samir.exception.ExternalServiceException;
import com.samir.test.Test.Samir.dto.TaskResponse;
import com.samir.test.Test.Samir.dto.TaskDTO;

@Service
@Slf4j
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RestTemplate restTemplate;

    public TaskDTO createTask(Task task, String token) {
        String username = validateTokenAndGetUsername(token);
        User user = findUserByUsername(username);
        task.setUser(user);
        Task savedTask = taskRepository.save(task);
        return convertToDTO(savedTask);
    }

    public List<TaskResponse> getTasks(String token) {
        String username = validateTokenAndGetUsername(token);
        User user = findUserByUsername(username);
        List<Task> tasks = taskRepository.findByUser(user);
        return tasks.stream()
            .map(task -> new TaskResponse(task.getId(), task.getTitle(), task.getDescription(), task.isCompleted(), user.getUsername()))
            .collect(Collectors.toList());
    }

    public TaskDTO markTaskCompleted(Long id, String token) {
        String username = validateTokenAndGetUsername(token);
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task tidak ditemukan dengan id: " + id));
            
        if (!task.getUser().getUsername().equals(username)) {
            throw new UnauthorizedException("Anda tidak berhak mengubah tugas ini");
        }
        task.setCompleted(true);
        Task updatedTask = taskRepository.save(task);
        return convertToDTO(updatedTask);
    }

    public List<TaskDTO> getIncompleteTasks(String token) {
        String username = validateTokenAndGetUsername(token);
        User user = findUserByUsername(username);
        return taskRepository.findByUserAndCompletedFalse(user).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    private String validateTokenAndGetUsername(String token) {
        String tokenValue = token.substring(7);
        String username = jwtUtil.extractUsername(tokenValue);
        if (username == null || !jwtUtil.validateToken(tokenValue, username)) {
            throw new UnauthorizedException("Invalid atau expired token");
        }
        return username;
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));
    }

    private TaskDTO convertToDTO(Task task) {
        return new TaskDTO(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.isCompleted()
        );
    }

    public boolean sendTaskNotification(Long taskId, String token) {
        String username = validateTokenAndGetUsername(token);
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task tidak ditemukan dengan id: " + taskId));

        if (!task.getUser().getUsername().equals(username)) {
            throw new UnauthorizedException("Anda tidak berhak mengakses tugas ini");
        }

        String notificationServiceUrl = "https://notification-service.example.com/notify";

        try {
            NotificationRequest request = new NotificationRequest(
                username,
                "Task Reminder",
                "Don't forget about your task: " + task.getTitle()
            );

            restTemplate.postForEntity(notificationServiceUrl, request, String.class);
            log.info("Successfully sent notification for task ID: {}", taskId);
            return true;

        } catch (ResourceAccessException e) {
            log.error("Network error while sending notification for task ID {}: {}", taskId, e.getMessage());
            throw new ExternalServiceException("Gagal terhubung ke layanan notifikasi. Silakan coba lagi nanti.", e);

        } catch (HttpClientErrorException e) {
            log.error("Client error while sending notification for task ID {}: {}", taskId, e.getMessage());
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new ExternalServiceException("Tidak dapat mengautentikasi dengan layanan notifikasi", e);
            }
            throw new ExternalServiceException("Kesalahan dalam permintaan ke layanan notifikasi", e);

        } catch (HttpServerErrorException e) {
            log.error("Server error while sending notification for task ID {}: {}", taskId, e.getMessage());
            throw new ExternalServiceException("Layanan notifikasi sedang mengalami gangguan. Silakan coba lagi nanti.", e);

        } catch (Exception e) {
            log.error("Unexpected error while sending notification for task ID {}: {}", taskId, e.getMessage());
            throw new ExternalServiceException("Terjadi kesalahan yang tidak terduga", e);
        }
    }
}

class NotificationRequest {
    private final String username;
    private final String title;
    private final String message;

    public NotificationRequest(String username, String title, String message) {
        this.username = username;
        this.title = title;
        this.message = message;
    }

    public String getUsername() { return username; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
}