package com.chernyshev.messenger.friends.repositories;

import com.chernyshev.messenger.friends.models.FriendEntity;
import com.chernyshev.messenger.users.models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface FriendRepository extends JpaRepository<FriendEntity,Long> {
     Optional<FriendEntity> findByUserAndFriend(UserEntity user, UserEntity friend);
     List<FriendEntity> findAllByUser(UserEntity user);
     boolean existsByUserAndFriend(UserEntity user,UserEntity friend);
}
