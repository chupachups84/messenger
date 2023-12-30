package com.chernyshev.messenger.dtos;

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
public class PasswordRequest {
    @NotBlank(message ="Старый пароль обязателен для смены пароля" )
    @Size(min = 8,message = "Минимальная длина пароля 8 символов")
    private String oldPassword;
    @NotBlank(message ="Новый пароль обязателен для смены пароля" )
    @Size(min = 8,message = "Минимальная длина пароля 8 символов")
    private String newPassword;
}
