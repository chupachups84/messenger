package com.chernyshev.messenger.store.repositories;

import com.chernyshev.messenger.store.models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<UserEntity,Long> {
    @Query("select u from UserEntity u WHERE u.username=:username and u.isActive=false")
    Optional<UserEntity> findDeactivatedByUsername(String username);

    @Query("select u from UserEntity u WHERE u.id=:id and u.isActive=false")
    Optional<UserEntity> findDeactivatedById(Long id);


    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByEmailConfirmationToken(String token);
}
