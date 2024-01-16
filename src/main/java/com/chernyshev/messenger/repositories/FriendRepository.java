package com.chernyshev.messenger.repositories;

import com.chernyshev.messenger.models.FriendEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<FriendEntity,Long> {
    @Query("""
           from FriendEntity f
           join fetch f.user1
           join fetch f.user2
           where f.statusType = 'APPROVED' and (f.user1.username=:username or f.user2.username=:username)
    """)
    Optional<List<FriendEntity>> findAllByUsername(String username);

    @Query("""
            from FriendEntity f
            join fetch f.user1
            join fetch f.user2
            where f.user1.username=:username1 and f.user2.username=:username2 or
            f.user2.username=:username1 and f.user1.username=:username2
    """)
    Optional<FriendEntity> findByUsername1AndUsername2(String username1,String username2);
}
