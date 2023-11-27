package com.chernyshev.messenger.friends.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FriendResponse {
    private String lastName;
    private String firstName;
    private String bio;
    private String status;
    private String avatarUrl;
}
