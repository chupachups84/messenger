package com.chernyshev.messenger.api.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationDto {
    @NotBlank(message = "Имя пользователя необходимо для входа")
    private String username;
    @NotBlank(message = "Пароль необходим для входа")
    private String password;
}
