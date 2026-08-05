package com.equipo26.financeai.repository;

import com.equipo26.financeai.entity.AnalisisFinanciero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalisisFinancieroRepository extends JpaRepository<AnalisisFinanciero, Long> {
}