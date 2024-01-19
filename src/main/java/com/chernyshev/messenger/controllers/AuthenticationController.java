package com.chernyshev.messenger.controllers;

import com.chernyshev.messenger.dtos.*;
import com.chernyshev.messenger.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthenticationController {
    private final UserService userService;
    public static final String REGISTER = "/api/v1/auth/register";
    public static final String LOGIN = "/api/v1/auth/login";
    public static final String EMAIL_CONFIRMATION = "/api/v1/auth/confirmation";
    public static final String REFRESH_TOKEN = "/api/v1/auth/token-refresh";
    public static final String LOGOUT = "/api/v1/auth/logout";

    @Operation(summary = "Register new User")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    content=@Content(mediaType = "application/json",schema = @Schema(implementation = TokenDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    content = @Content(mediaType = "application/json",schema = @Schema(implementation = ErrorDto.class))
            )
    })
    @PostMapping(REGISTER)
    public ResponseEntity<TokenDto> signUp(@RequestBody RegisterDto request) {
        return userService.signUp(request);
    }

    @Operation(summary = "Authenticate User")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    content=@Content(mediaType = "application/json",schema = @Schema(implementation = TokenDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    content = @Content(mediaType = "application/json",schema = @Schema(implementation = ErrorDto.class))
            )
    })
    @PostMapping(LOGIN)
    public ResponseEntity<TokenDto> signIn(@RequestBody LoginDto request) {
        return userService.signIn(request);
    }

    @Operation(summary = "Logout User", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    content=@Content(mediaType = "application/json",schema = @Schema(implementation = ResponseMessageDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    content = @Content(mediaType = "application/json",schema = @Schema(implementation = ErrorDto.class))
            )
    })
    @PostMapping(LOGOUT)
    public ResponseEntity<ResponseMessageDto> signOut(HttpServletRequest request) {
        return userService.signOut(request);
    }

    @Operation(summary = "Confirm User Email")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    content=@Content(mediaType = "application/json",schema = @Schema(implementation = ResponseMessageDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    content = @Content(mediaType = "application/json",schema = @Schema(implementation = ErrorDto.class))
            )
    })
    @GetMapping(EMAIL_CONFIRMATION)
    public ResponseEntity<ResponseMessageDto> confirm(@RequestParam String confirmationToken) {
        return userService.emailConfirm(confirmationToken);
    }

    @Operation(summary = "Refresh access token")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    content=@Content(mediaType = "application/json",schema = @Schema(implementation = ResponseMessageDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    content = @Content(mediaType = "application/json",schema = @Schema(implementation = ErrorDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    content = @Content(mediaType = "application/json",schema = @Schema(implementation = ErrorDto.class))
            )
    })
    @PutMapping(REFRESH_TOKEN)
    public ResponseEntity<TokenDto> refreshToken(HttpServletRequest request) {
        return userService.tokenRefresh(request);
    }
}
