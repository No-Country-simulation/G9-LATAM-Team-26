package com.equipo26.financeai.repository;
import com.equipo26.financeai.model.FinancialAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinancialRepository extends JpaRepository<FinancialAnalysis, Long> {
}
