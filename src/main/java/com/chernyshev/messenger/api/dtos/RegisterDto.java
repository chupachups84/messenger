package com.chernyshev.messenger.api.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDto {
    @NotBlank(message = "Имя обязательно для регистрации")
    private String firstname;
    @NotBlank(message = "Фамилия обязательна для регистрации")
    private String lastname;
    @Email(message = "Некорректная почта")
    @NotBlank(message = "Почта обязательна для регистрации")
    private String email;
    @NotBlank(message = "Имя пользователя обязательно для регистрации")
    private String username;
    @NotBlank(message ="Пароль обязателен для регистрации" )
    @Size(min = 8,message = "Минимальная длина пароля 8 символов")
    private String password;
}
