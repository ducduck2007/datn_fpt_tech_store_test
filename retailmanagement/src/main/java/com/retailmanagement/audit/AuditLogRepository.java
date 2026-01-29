package com.retailmanagement.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByCreatedAtBetween(Instant from, Instant to);
    List<AuditLog> findByActionOrderByCreatedAtDesc(String action);
    List<AuditLog> findByUserId(Integer userId);

    //Báo cáo theo module
    @Query(value = """
        SELECT module, COUNT (*) AS total
        from audit_logs    
        group by module
        order by total DESC 
    """, nativeQuery = true)
    List<Object[]> reportByModule();



}
