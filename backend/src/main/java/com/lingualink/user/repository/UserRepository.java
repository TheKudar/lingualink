package com.lingualink.user.repository;

import com.lingualink.user.entity.User;
import com.lingualink.user.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByUsernameIgnoreCase(String username);
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByUsernameIgnoreCase(String username);

    @Query("""
            SELECT u
            FROM User u
            WHERE u.status = :status
              AND (:excludeUserId IS NULL OR u.id <> :excludeUserId)
              AND (
                    :query IS NULL
                    OR LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))
              )
            ORDER BY u.username ASC
            """)
    List<User> searchActiveUsers(
            @Param("query") String query,
            @Param("status") UserStatus status,
            @Param("excludeUserId") Long excludeUserId
    );
}
