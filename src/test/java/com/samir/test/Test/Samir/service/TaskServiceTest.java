package com.samir.test.Test.Samir.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.samir.test.Test.Samir.model.Task;
import com.samir.test.Test.Samir.model.User;
import com.samir.test.Test.Samir.repository.TaskRepository;
import com.samir.test.Test.Samir.repository.UserRepository;
import com.samir.test.Test.Samir.security.JwtUtil;
import com.samir.test.Test.Samir.exception.ExternalServiceException;
import com.samir.test.Test.Samir.exception.ResourceNotFoundException;
import com.samir.test.Test.Samir.exception.UnauthorizedException;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private TaskService taskService;

    private String validToken = "Bearer valid-token";
    private String username = "testuser";
    private User user;
    private Task task;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername(username);

        task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setUser(user);

        // Using lenient() to avoid UnnecessaryStubbing exceptions
        lenient().when(jwtUtil.extractUsername("valid-token")).thenReturn(username);
        lenient().when(jwtUtil.validateToken("valid-token", username)).thenReturn(true);
        lenient().when(userRepository.findByUsername(username)).thenReturn(java.util.Optional.of(user));
        lenient().when(taskRepository.findById(1L)).thenReturn(java.util.Optional.of(task));
    }

    @Test
    void whenNetworkError_thenThrowExternalServiceException() {
        when(restTemplate.postForEntity(anyString(), any(), any()))
            .thenThrow(new ResourceAccessException("Network Error"));

        ExternalServiceException exception = assertThrows(
            ExternalServiceException.class,
            () -> taskService.sendTaskNotification(1L, validToken)
        );

        assertEquals("Gagal terhubung ke layanan notifikasi. Silakan coba lagi nanti.", exception.getMessage());
    }

    @Test
    void whenUnauthorized_thenThrowExternalServiceException() {
        when(restTemplate.postForEntity(anyString(), any(), any()))
            .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        ExternalServiceException exception = assertThrows(
            ExternalServiceException.class,
            () -> taskService.sendTaskNotification(1L, validToken)
        );

        assertEquals("Tidak dapat mengautentikasi dengan layanan notifikasi", exception.getMessage());
    }

    @Test
    void whenServerError_thenThrowExternalServiceException() {
        when(restTemplate.postForEntity(anyString(), any(), any()))
            .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        ExternalServiceException exception = assertThrows(
            ExternalServiceException.class,
            () -> taskService.sendTaskNotification(1L, validToken)
        );

        assertEquals("Layanan notifikasi sedang mengalami gangguan. Silakan coba lagi nanti.", exception.getMessage());
    }

    @Test
    void whenTaskNotFound_thenThrowResourceNotFoundException() {
        when(taskRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> taskService.sendTaskNotification(999L, validToken)
        );
    }

    @Test
    void whenInvalidToken_thenThrowUnauthorizedException() {
        String invalidToken = "Bearer invalid-token";
        when(jwtUtil.extractUsername("invalid-token")).thenReturn(null);

        assertThrows(
            UnauthorizedException.class,
            () -> taskService.sendTaskNotification(1L, invalidToken)
        );
    }

    @Test
    void whenSuccessful_thenReturnTrue() {
        when(restTemplate.postForEntity(
            anyString(), 
            any(), 
            eq(String.class)
        )).thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        boolean result = taskService.sendTaskNotification(1L, validToken);
        assertTrue(result);
    }
}
