package com.chernyshev.messenger.users.controllers;

import com.chernyshev.messenger.exception.myExceptions.InvalidTokenException;
import com.chernyshev.messenger.exception.myExceptions.UserDeactivatedException;
import com.chernyshev.messenger.users.dtos.AuthenticationRequest;
import com.chernyshev.messenger.users.dtos.RegisterRequest;
import com.chernyshev.messenger.users.dtos.TokensResponse;
import com.chernyshev.messenger.users.services.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication",description = "AuthenticationAPI")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Operation(summary = "Регистрирует нового пользователя")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RegisterRequest.class),
                    examples ={
                            @ExampleObject(
                                    name = "registerRequest",
                                    value  = "{" +
                                            "\"firstname\":\"Pavel\"," +
                                            "\"lastname\":\"Chernyshev\"," +
                                            "\"email\":\"pasha7w@gmail.com\"," +
                                            "\"username\":\"pp848484\"," +
                                            "\"password\":\"password123\"" +
                                            "}"
                            )
                    }
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь успешно зарегистрирован",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TokensResponse.class)
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Проблема с входными данными",
                    content = {@Content(mediaType = "text/plain")}
            )
    })

    @PostMapping("/register")
    public ResponseEntity<TokensResponse> register(@RequestBody @Valid RegisterRequest request) {
        TokensResponse response = authenticationService.register(request);
        return ResponseEntity.ok(response);
    }
    @Operation(summary = "Авторизует пользователя")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AuthenticationRequest.class),
                    examples ={
                            @ExampleObject(
                                    name = "authenticationRequest",
                                    value  = "{" + "\"username\":\"pp848484\"," + "\"password\":\"password123\"" + "}"
                            )
                    }
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь успешно авторизован",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TokensResponse.class)
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Проблема с входными данными",
                    content = {@Content(mediaType = "text/plain")}
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Пользователь не найден",
                    content = {@Content(mediaType = "text/plain")}
            )
    })
    @PostMapping("/login")
    public ResponseEntity<TokensResponse> authenticate(@RequestBody @Valid AuthenticationRequest request) {
            TokensResponse response = authenticationService.login(request);
            return  ResponseEntity.ok(response);
    }
    @Operation(summary = "Подтверждает почту")
    @Parameter(
            name = "emailConfirmationToken",
            required = true,
            content = @Content(mediaType = "text/plain")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Почта успешно подтверждена",
                    content = {@Content(mediaType = "text/plain")}
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Неверный токен подтверждения",
                    content = {@Content(mediaType = "text/plain")}
            )
    })
    @PostMapping("/confirm/{emailConfirmationToken}")
    public ResponseEntity<String> confirm(@PathVariable String emailConfirmationToken) throws InvalidTokenException {
        return authenticationService.confirmation(emailConfirmationToken);
    }
    @SecurityRequirement(name= "bearerAuth")
    @Operation(summary = "Обновляет токен доступа")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Токен успешно обновлен",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TokensResponse.class)
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Неверный токен подтверждения",
                    content = {@Content(mediaType = "text/plain")}
            )
    })
    @PostMapping("/refresh-token")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        authenticationService.refreshToken(request,response);
    }
    @SecurityRequirement(name= "bearerAuth")
    @Operation(summary = "Выход из системы")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь вышел из аккаунта",
                    content = {
                            @Content(mediaType = "text/plain")
                    }
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Пользователь не найден",
                    content = {@Content(mediaType = "text/plain")}
            )
    })
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        return authenticationService.logout(request);
    }
}
