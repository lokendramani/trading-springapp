package com.example.kitespringapp.repository;

import com.example.kitespringapp.entity.PnLRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PnLRepository extends JpaRepository<PnLRecord, Long> {
    
    @Query("SELECT p FROM PnLRecord p WHERE p.timestamp >= :startTime ORDER BY p.timestamp")
    List<PnLRecord> findRecentRecords(LocalDateTime startTime);
} 