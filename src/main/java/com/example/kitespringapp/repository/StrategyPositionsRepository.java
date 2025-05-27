package com.example.kitespringapp.repository;

import com.example.kitespringapp.entity.StrategyPosition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StrategyPositionsRepository extends JpaRepository<StrategyPosition, String> {
    List<StrategyPosition> findByStrategyName(String strategyName);
}
