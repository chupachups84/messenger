package com.chernyshev.messenger.store.models;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "friend_request")
public class FriendEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "friend_id")
    private UserEntity friend;

    @Override
    public String toString() {
        return "FriendshipEntity{" +
                "id=" + id +
                ", user=" + user.getUsername() +
                ", friend=" + friend.getUsername() +
                '}';
    }
}
