package com.chernyshev.messenger.store.repositories;

import com.chernyshev.messenger.store.models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<UserEntity,Long> {
    @Query("select u from UserEntity u WHERE u.username=:username and u.isActive=:isActive")
    Optional<UserEntity> findByUsernameAndActive(String username,boolean isActive);

    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByEmailToken(String token);




}
