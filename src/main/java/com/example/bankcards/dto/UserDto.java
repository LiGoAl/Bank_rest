package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDto {
    @Null(message = "Id must be empty")
    private Long id;
    @NotNull(message = "Name can't be empty")
    @NotBlank(message = "Name can't be empty")
    private String username;
    @NotNull(message = "Email can't be empty")
    @Pattern(regexp = "\\w+@\\w+\\.\\w+",message = "Email doesn't match the form")
    private String email;
    @NotNull(message = "Password can't be empty")
    @NotBlank(message = "Password can't be empty")
    private String password;
    @NotNull(message = "Roles can't be empty")
    @NotBlank(message = "Roles can't be empty")
    private String roles;
}
