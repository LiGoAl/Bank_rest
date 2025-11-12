package com.example.bankcards.controller;

import com.example.bankcards.dto.JwtAuthenticationDto;
import com.example.bankcards.dto.RefreshTokenDto;
import com.example.bankcards.dto.UserCredentialsDto;
import com.example.bankcards.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;

    @PostMapping("/sign-in")
    public JwtAuthenticationDto signIn(@RequestBody UserCredentialsDto userCredentialsDTO) {
        return userService.signIn(userCredentialsDTO);
    }

    @PostMapping("/refresh")
    public JwtAuthenticationDto refresh(@RequestBody RefreshTokenDto refreshTokenDTO) {
        return userService.refreshToken(refreshTokenDTO);
    }
}
