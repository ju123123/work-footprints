package com.workfootprint.controller;

import com.workfootprint.dto.auth.LoginRequest;
import com.workfootprint.dto.auth.LoginResponse;
import com.workfootprint.entity.UserEntity;
import com.workfootprint.security.JwtService;
import com.workfootprint.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        UserEntity user = userService.verifyPassword(request.getUsername(), request.getPassword());
        if (user == null) {
            throw new UnauthorizedException();
        }
        String token = jwtService.createToken(user.getId(), user.getUsername());
        return new LoginResponse(token, user.getUsername());
    }
}

