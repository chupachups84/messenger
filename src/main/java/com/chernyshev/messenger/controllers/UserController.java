package com.chernyshev.messenger.controllers;

import com.chernyshev.messenger.dtos.*;
import com.chernyshev.messenger.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "User")
public class UserController {
    private final UserService userService;
    public static final  String USER ="/api/v1/users/{username}";
    public static final  String USER_CHANGE_PASSWORD ="/api/v1/users/{username}/password-change";
    public static final String USER_FRIENDS="/api/v1/users/{username}/friends";

    @Operation(summary = "Return User Account Information", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    content=@Content(mediaType = "application/json",schema = @Schema(implementation = UserDto.class))
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
    @GetMapping(USER)
    public ResponseEntity<UserDto> getUserInfo(@PathVariable String username){
        return userService.getUserInfo(username);
    }

    @Operation(summary = "Update User Account Information", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    content=@Content(mediaType = "application/json",schema = @Schema(implementation = TokenDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    content = @Content(mediaType = "application/json",schema = @Schema(implementation = ErrorDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    content = @Content(mediaType = "application/json",schema = @Schema(implementation = ErrorDto.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    content = @Content(mediaType = "application/json",schema = @Schema(implementation = ErrorDto.class))
            )

    })
    @PatchMapping(USER)
    public ResponseEntity<TokenDto> changeUserInfo(
            Principal principal,@PathVariable String username,@RequestBody UserDto userDto
    ){
        return userService.changeUserInfo(principal,username,userDto);
    }

    @Operation(summary = "Update User Account Password", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    content=@Content(mediaType = "application/json",schema = @Schema(implementation = ResponseMessageDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    content = @Content(mediaType = "application/json",schema = @Schema(implementation = ErrorDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    content = @Content(mediaType = "application/json",schema = @Schema(implementation = ErrorDto.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    content = @Content(mediaType = "application/json",schema = @Schema(implementation = ErrorDto.class))
            )

    })
    @PatchMapping(USER_CHANGE_PASSWORD)
    public ResponseEntity<ResponseMessageDto> changePassword(
            Principal principal,@PathVariable String username,@RequestBody PasswordDto request
    ) {
        return userService.changeUserPassword(principal,username,request);
    }
    @Operation(summary = "Delete User Account", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    content=@Content(mediaType = "application/json",schema = @Schema(implementation = RecoverTokenDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    content = @Content(mediaType = "application/json",schema = @Schema(implementation = ErrorDto.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    content = @Content(mediaType = "application/json",schema = @Schema(implementation = ErrorDto.class))
            )

    })
    @DeleteMapping(USER)
    public ResponseEntity<RecoverTokenDto> deleteUser(Principal principal,@PathVariable String username) {
        return userService.deleteUser(principal,username);
    }
    @Operation(summary = "Recover User Account", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    content=@Content(mediaType = "application/json",schema = @Schema(implementation = TokenDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    content = @Content(mediaType = "application/json",schema = @Schema(implementation = ErrorDto.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    content = @Content(mediaType = "application/json",schema = @Schema(implementation = ErrorDto.class))
            )

    })
    @PutMapping(USER)
    public ResponseEntity<TokenDto> recoverUser(@RequestBody RecoverTokenDto token,@PathVariable String username) {
        return userService.recoverUser(token,username);
    }

    @Operation(summary = "Return User Friend List", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    content=@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserDto.class)))
            ),
            @ApiResponse(
                    responseCode = "401",
                    content = @Content(mediaType = "application/json",schema = @Schema(implementation = ErrorDto.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    content = @Content(mediaType = "application/json",schema = @Schema(implementation = ErrorDto.class))
            )
            ,
            @ApiResponse(
                    responseCode = "404",
                    content = @Content(mediaType = "application/json",schema = @Schema(implementation = ErrorDto.class))
            )

    })
    @GetMapping(USER_FRIENDS)
    public ResponseEntity<List<UserDto>> getFriendList(Principal principal, @PathVariable String username){
        return userService.getFriendList(principal,username);
    }

    @Operation(summary = "Send Friend Request", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    content=@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ResponseMessageDto.class)))
            ),
            @ApiResponse(
                    responseCode = "400",
                    content = @Content(mediaType = "application/json",schema = @Schema(implementation = ErrorDto.class))
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
    @PostMapping(USER_FRIENDS)
    public ResponseEntity<ResponseMessageDto> addToFriends(Principal principal, @PathVariable String username){
        return userService.addFriend(principal,username);
    }

}
