package com.samir.test.Test.Samir.service;

import com.samir.test.Test.Samir.model.User;
import com.samir.test.Test.Samir.repository.UserRepository;
import com.samir.test.Test.Samir.security.JwtUtil;
import com.samir.test.Test.Samir.dto.LoginResponse;
import com.samir.test.Test.Samir.dto.RegisterResponse;
import com.samir.test.Test.Samir.exception.UserAlreadyExistsException;
import com.samir.test.Test.Samir.exception.ResourceNotFoundException;
import com.samir.test.Test.Samir.exception.InvalidCredentialsException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public RegisterResponse registerUser(User user) {
        Optional<User> existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser.isPresent()) {
            throw new UserAlreadyExistsException("Username sudah ada");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        return new RegisterResponse("User berhasil didaftarkan");
    }

    public LoginResponse loginUser(String username, String password) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("Password salah");
        }
        
        String token = jwtUtil.generateToken(username);
        return new LoginResponse(token);
    }
}
