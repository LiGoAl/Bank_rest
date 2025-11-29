package com.example.bankcards.repository;

import com.example.bankcards.entity.BlockCardRequest;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BlockCardRequestRepository extends JpaRepository<BlockCardRequest, Long> {

    Page<BlockCardRequest> findByProcessedFalse(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM BlockCardRequest r WHERE r.id = :requestId")
    Optional<BlockCardRequest> findByIdForUpdate(Long requestId);
}
