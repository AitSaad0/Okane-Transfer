package com.okane.compliance.repository;

import com.okane.compliance.bean.SarReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SarReportRepository extends JpaRepository<SarReport, Long> {

    long countByStatus(String status);
    @Query("""
        SELECT s
        FROM SarReport s
        JOIN FETCH s.transfert
    """)
    List<SarReport> findAllWithTransfert();
}