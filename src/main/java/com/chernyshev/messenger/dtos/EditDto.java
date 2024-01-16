package com.chernyshev.messenger.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditDto {
    private String lastname;
    private String firstname;
    private String username;
    private String email;
    private String bio;
    private String status;
    @JsonProperty(value = "avatar_url")
    private String avatarUrl;
    @JsonProperty(value = "is_receive_messages_friend_only")
    private boolean isReceiveMessagesFriendOnly;
    @JsonProperty(value = "is_friends_list_hidden")
    private boolean isFriendsListHidden;
}
