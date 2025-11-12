package com.example.bankcards.repository;

import com.example.bankcards.entity.BlockCardRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockCardRequestRepository extends JpaRepository<BlockCardRequest, Long> {

    Page<BlockCardRequest> findByProcessedFalse(Pageable pageable);
}
