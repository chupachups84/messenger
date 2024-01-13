package com.chernyshev.messenger.store.repositories;

import com.chernyshev.messenger.store.models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<UserEntity,Long> {
    @Query("select u from UserEntity u WHERE u.username=:username and u.isActive=:isActive")
    Optional<UserEntity> findByUsernameAndActive(String username,boolean isActive);

    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByEmailToken(String token);

    @Query("select u.friends from UserEntity u join u.friends v where u.username=:username order by v.lastname asc ")
    Optional<List<UserEntity>> getFriendList(String username);
    @Query(
    """
    SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM UserEntity u
    JOIN u.friends f
    WHERE (u.username = :username1 AND f.username = :username2)
    """
    )
    boolean areFriends(String username1, String username2);


}
