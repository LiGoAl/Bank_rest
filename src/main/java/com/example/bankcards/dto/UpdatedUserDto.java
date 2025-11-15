package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdatedUserDto {
    @NotNull(message = "Id can't be empty")
    private Long id;
    @NotBlank(message = "Name can't be empty")
    private String username;
    @Pattern(regexp = "\\w+@\\w+\\.\\w+",message = "Email doesn't match the form")
    private String email;
    @NotBlank(message = "Password can't be empty")
    private String password;
    @NotBlank(message = "Roles can't be empty")
    private String roles;
}
