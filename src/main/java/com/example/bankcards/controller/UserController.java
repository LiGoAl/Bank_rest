package com.example.bankcards.controller;

import com.example.bankcards.dto.IdDto;
import com.example.bankcards.dto.UpdatedUserDto;
import com.example.bankcards.dto.UserDto;
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
    public List<UserDto> readUsers(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "5") int size) {
        return userService.readUsers(page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Valid @RequestBody UserDto userDto) {
        return userService.createUser(userDto);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@Valid @RequestBody IdDto idDto) {
        userService.deleteUser(idDto.getId());
    }

    @PutMapping
    public void updateUser(@Valid @RequestBody UpdatedUserDto updatedUserDto) {
        userService.updateUser(updatedUserDto);
    }
}
