package com.samir.test.Test.Samir.repository;

import com.samir.test.Test.Samir.model.Task;
import com.samir.test.Test.Samir.model.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUser(User user);
    List<Task> findByUserAndCompletedFalse(User user);
}