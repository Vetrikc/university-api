package com.un.authservice.repository;

import com.un.authservice.entity.RefreshToken;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    @EntityGraph
    Optional<RefreshToken> findByTokenHash(String hash);

    @EntityGraph(attributePaths = {"user"})
    Optional<RefreshToken> findWithUserByTokenHash(String hash);

    @Query("select rt from RefreshToken rt WHERE rt.user.id = :userId AND rt.revoked = false AND rt.expiresAt > :now ORDER BY rt.createdAt DESC")
    @EntityGraph(attributePaths = {"user"})
    List<RefreshToken> findActiveByUserId(@Param("userId") UUID userId,
                                          @Param("now") Instant now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update RefreshToken rt set rt.revoked = true, rt.revokedAt = :now WHERE rt.id = :id AND rt.revoked = false")
    int revokeById(@Param("id") UUID id, @Param("now") Instant now);

    @EntityGraph(attributePaths = {"user"})
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update RefreshToken rt set rt.revoked = true, rt.revokedAt = :now WHERE rt.user.id = :userId AND rt.revoked = false")
    int revokeAllByUserId(@Param("userId") UUID userId, @Param("now") Instant now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete FROM RefreshToken rt where rt.expiresAt < :cutoff")
    int deleteExpiredBefore(@Param("cutoff") Instant cutoff);

    @EntityGraph(attributePaths = {"replacedBy"})
    Optional<RefreshToken> findWithReplacedByTokenHash(String tokenHash);


    boolean existsByTokenHash(String tokenHash);

}