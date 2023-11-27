package com.chernyshev.messenger.friends.controllers;

import com.chernyshev.messenger.exception.myExceptions.FriendshipException;
import com.chernyshev.messenger.friends.dtos.FriendResponse;
import com.chernyshev.messenger.friends.services.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/friends")
@RequiredArgsConstructor
public class FriendController {
    private final FriendService friendService;

    @GetMapping()
    public ResponseEntity<List<FriendResponse>> getMyFriends(Principal principal) {
        List<FriendResponse> responseList = friendService.getFriends(principal.getName());
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<FriendResponse>> getUserFriends(@PathVariable Long id,Principal principal) throws FriendshipException,IllegalStateException {
        List<FriendResponse> responseList = friendService.getUserFriends(id, principal.getName());
        return ResponseEntity.ok(responseList);
    }
    @PostMapping("/{id}")
    public ResponseEntity<String> addFriend(@PathVariable Long id, Principal principal) throws IllegalStateException {
        friendService.sendFriendRequest(principal.getName(),id);
        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/{id}")
    public void deleteFriend(@PathVariable Long id, Principal principal){
        friendService.deleteFriend(principal.getName(),id);
    }
}
