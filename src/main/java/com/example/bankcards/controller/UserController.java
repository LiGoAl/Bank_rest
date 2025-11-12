package com.example.bankcards.controller;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.exception.RequestValidationException;
import com.example.bankcards.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@Validated
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<UserDto> readUsers(@RequestParam(defaultValue = "0") Integer page,
                                  @RequestParam(defaultValue = "5") Integer size) {
        return userService.readUsers(page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Valid @RequestBody UserDto userDto) {
        return userService.createUser(userDto);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable("userId") Long id) {
        userService.deleteUser(validateUserId(id));
    }

    @PutMapping("/{userId}")
    public void updateUser(@PathVariable("userId") Long id,
                           @RequestParam(value = "username", required = false) String username,
                           @RequestParam(value = "email", required = false) String email,
                           @RequestParam(value = "password", required = false) String password,
                           @RequestParam(value = "roles", required = false) String roles) {
        userService.updateUser(validateUserId(id),
                validateUsername(username),
                validateUserEmail(email),
                validateUserPassword(password),
                validateUserRoles(roles));
    }

    private String validateUsername(String username) {
        if (username != null && username.isEmpty()) {
            throw new RequestValidationException("Username can't be empty");
        } else return username;
    }

    private String validateUserEmail(String email) {
        if (email != null && !email.matches("\\w+@\\w+\\.\\w+")) {
            throw new RequestValidationException("Email doesn't match the form");
        } else return email;
    }

    private String validateUserPassword(String password) {
        if (password != null && password.isEmpty()) {
            throw new RequestValidationException("Password can't be empty");
        } else return password;
    }

    private String validateUserRoles(String roles) {
        if (roles != null && roles.isEmpty()) {
            throw new RequestValidationException("Roles can't be empty");
        } else return roles;
    }

    private Long validateUserId(Long id) {
        if (id == null || id <= 0) {
            throw new RequestValidationException("Id must be greater than 0 and can't be empty");
        } else return id;
    }
}
