package com.chernyshev.messenger.store.repositories;

import com.chernyshev.messenger.store.models.FriendEntity;
import com.chernyshev.messenger.store.models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<FriendEntity,Long> {
    boolean existsByUser1AndUser2(UserEntity user1, UserEntity user2);
    @Query(
            "SELECT f.user2 from FriendEntity f where f.user1=:user"
    )
    Optional<List<UserEntity>> findByUser1(UserEntity user);
}
