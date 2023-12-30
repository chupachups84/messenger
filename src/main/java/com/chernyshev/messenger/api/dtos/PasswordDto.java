package com.chernyshev.messenger.api.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class PasswordDto {
    @NotBlank(message ="Старый пароль обязателен для смены пароля" )
    @Size(min = 8,message = "Минимальная длина пароля 8 символов")
    @JsonProperty(value = "old_password")
    private String oldPassword;
    @NotBlank(message ="Новый пароль обязателен для смены пароля" )
    @Size(min = 8,message = "Минимальная длина пароля 8 символов")
    @JsonProperty(value = "new_password")
    private String newPassword;
    @NotBlank(message ="Новый пароль обязателен для смены пароля" )
    @Size(min = 8,message = "Минимальная длина пароля 8 символов")
    @JsonProperty(value = "confirm_password")
    private String confirmPassword;
}
