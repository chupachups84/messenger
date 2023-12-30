package com.chernyshev.messenger.controllers;

import com.chernyshev.messenger.dtos.*;
import com.chernyshev.messenger.exception.myExceptions.UserDeactivatedException;
import com.chernyshev.messenger.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
@SecurityRequirement(name= "bearerAuth")
@Tag(name = "User",description = "UserAPI")
public class UserController {
    private final UserService userService;
    @Operation(summary = "Выводит информацию о пользователе")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "ОК",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = InfoResponse.class)
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Пользователь не найден",
                    content = {@Content(mediaType = "text/plain")}
            )
    })
    @GetMapping()
    public ResponseEntity<InfoResponse> info(Principal principal) throws UserDeactivatedException {
        InfoResponse response = userService.info(principal.getName());
        return ResponseEntity.ok().body(response);
    }
    @Operation(summary = "Изменяет информацию о пользователе")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = InfoRequest.class),
                    examples ={
                            @ExampleObject(
                                    name = "infoRequest",
                                    value  = "{" +
                                            "\"firstname\":\"Pasha\"," +
                                            "\"lastname\":\"Chernyshev\"," +
                                            "\"status\":\"PMA\"," +
                                            "\"bio\":\"info about me etc.\"," +
                                            "\"avatarUrl\":\"avatarUrl\"" +
                                            "}"
                            )
                    }
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Информация о пользователе успешно изменена",
                    content = {@Content(mediaType = "text/plain")}
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
    @PutMapping("/edit")
    public ResponseEntity<String> edit(@RequestBody @Valid InfoRequest request, Principal principal)
            throws UserDeactivatedException {
        userService.edit(request,principal.getName());
        return ResponseEntity.ok().build();
    }
    @Operation(summary = "Изменяет имя пользователя")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UsernameRequest.class),
                    examples ={
                            @ExampleObject(
                                    name = "usernameRequest",
                                    value  = "{" + "\"username\":\"newusername\""+ "}"
                            )
                    }
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Имя пользователя успешно изменено",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TokenDto.class)
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
    @PostMapping("/change-username")
    public ResponseEntity<TokenDto> changeUsername(@Valid @RequestBody UsernameRequest request, Principal principal)
            throws IllegalStateException, UserDeactivatedException {
        TokenDto response = userService.changeUsername(request,principal.getName());
        return ResponseEntity.ok(response);
    }
    @Operation(summary = "Изменяет почту")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = EmailRequest.class),
                    examples ={
                            @ExampleObject(
                                    name = "emailRequest",
                                    value  = "{" + "\"email\":\"newemail@mail.ru\""+ "}"
                            )
                    }
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Почта успешно изменена",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TokenDto.class)
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
    @PostMapping("/change-email")
    public ResponseEntity<TokenDto> changeEmail(@Valid @RequestBody EmailRequest request, Principal principal) {
        TokenDto response = userService.changeEmail(request,principal.getName());
        return ResponseEntity.ok(response);
    }
    @Operation(summary = "Изменяет пароль")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PasswordRequest.class),
                    examples ={
                            @ExampleObject(
                                    name = "passwordRequest",
                                    value  = "{" + "\"oldPassword\":\"password123\","+ "\"newPassword\":\"password1234\""+ "}"
                            )
                    }
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Пароль успешно изменен",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TokenDto.class)
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
    @PostMapping("/change-password")
    public ResponseEntity<TokenDto> changePassword(@RequestBody @Valid PasswordRequest request, Principal principal){
        TokenDto response = userService.changePassword(request,principal.getName());
        return ResponseEntity.ok(response);
    }
    @Operation(summary = "Деактивирует аккаунт пользователя")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь успешно деактивирован",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TokenDto.class)
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Пользователь не найден",
                    content = {@Content(mediaType = "text/plain")}
            )
    })
    @DeleteMapping("/delete")
    public ResponseEntity<TokenDto> delete(Principal principal) {
        TokenDto response = userService.delete(principal.getName());
        return ResponseEntity.ok(response);
    }
    @Operation(summary = "Восстанавливает аккаунт пользователя")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь успешно восстановлен",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TokenDto.class)
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Пользователь не найден",
                    content = {@Content(mediaType = "text/plain")}
            )
    })
    @PostMapping("/recover")
    public ResponseEntity<TokenDto> recover(Principal principal) {
        TokenDto response = userService.recover(principal.getName());
        return ResponseEntity.ok(response);
    }
    @Operation(summary = "Изменяет приватность аккаунта пользователя")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Приватность успешно изменена",
                    content = {@Content(mediaType = "text/plain")}
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Пользователь не найден",
                    content = {@Content(mediaType = "text/plain")}
            )
    })
    @PutMapping("/change-profile-privacy")
    public ResponseEntity<String> setPrivateProfile(Principal principal){
        userService.changeProfilePrivacy(principal.getName());
        return ResponseEntity.ok().build();
    }

}
