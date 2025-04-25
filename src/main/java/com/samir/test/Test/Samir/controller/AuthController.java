package com.samir.test.Test.Samir.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.samir.test.Test.Samir.model.User;
import com.samir.test.Test.Samir.dto.LoginResponse;
import com.samir.test.Test.Samir.dto.RegisterResponse;
import com.samir.test.Test.Samir.service.AuthService;
import com.samir.test.Test.Samir.exception.InvalidCredentialsException;
import com.samir.test.Test.Samir.exception.UserAlreadyExistsException;
import com.samir.test.Test.Samir.exception.ResourceNotFoundException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public RegisterResponse registerUser(@RequestBody User user) {
        try {
            return authService.registerUser(user);
        } catch (UserAlreadyExistsException ex) {
            throw ex;
        } catch (Exception e) {
            throw new RuntimeException("Gagal mendaftarkan user: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public LoginResponse loginUser(@RequestBody User user) {
        try {
            return authService.loginUser(user.getUsername(), user.getPassword());
        } catch (ResourceNotFoundException | InvalidCredentialsException ex) {
            throw ex;
        } catch (Exception e) {
            throw new RuntimeException("Gagal login: " + e.getMessage());
        }
    }
}