package com.un.authservice.repository;

import com.un.authservice.entity.User;
import com.un.authservice.entity.UserRole;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.JpqlQueryBuilder;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    /**
     * Найти пользователя по email.
     * ВАЖНО: @SQLRestriction автоматически отфильтрует deleted_at IS NULL.
     * То есть вернёт только активного пользователя.
     */
    Optional<User> findByEmail(String email);

    /**
     * Проверить существование по email.
     */
    boolean existsByEmail(String email);

    /**
     * Заблокировать аккаунт до момента времени.
     * SQL: UPDATE users SET locked_until = ?, updated_at = now() WHERE id = ?
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE User u SET u.lockedUntil = :until, u.updatedAt = :now WHERE u.id = :id")
    int lockAccount(@Param("id") UUID id, @Param("until") Instant until);

    /**
     * Снять блокировку.
     * SQL: UPDATE users SET locked_until = NULL WHERE id = ?
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE User u SET u.lockedUntil = NULL, u.updatedAt = :now WHERE u.id = :id")
    int unlockAccount(@Param("id") UUID id);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE User u set u.deletedAt = now() where u.id = :id AND u.deletedAt IS NULL")
    int softDelete(@Param("id") UUID id);

    /**
     * Восстановить удалённого.
     * из-за @SQLRestriction. Этот метод — исключение.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE User u SET u.deletedAt = NULL WHERE u.id = :id")
    int restore(@Param("id") UUID id);


    /**
     * Native query для админки — получить ВСЕХ, включая удалённых.
     */
    @Query(value = "SELECT * FROM users WHERE email = :email", nativeQuery = true)
    Optional<User> findByEmailIncludingDeleted(@Param("email") String email);

    /**
     * Найти всех заблокированных — для админ-панели или cron.
     * WHERE locked_until IS NOT NULL AND locked_until > now()
     */

    @Query(value = "select * from users where locked_until is not null and locked_until >now()  ", nativeQuery = true)
    List<User> findLockedUsers();

    /**
     * Очистка: удалить soft-deleted старше N дней (hard-delete).
     * Запускается cron.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM User u WHERE u.deletedAt IS NOT NULL AND u.deletedAt < :cutoff", nativeQuery = true)
    int hardDeleteOlderThan();
}

