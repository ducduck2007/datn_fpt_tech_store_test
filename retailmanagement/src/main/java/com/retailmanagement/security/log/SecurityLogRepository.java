package com.retailmanagement.security.log;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityLogRepository
        extends JpaRepository<SecurityLog, Long>,
                JpaSpecificationExecutor<SecurityLog> {

    @Query(value = """
    SELECT COUNT(*) 
    FROM security_logs
    WHERE action_type = :action
      AND severity = 'HIGH'
      AND created_at >= DATEADD(MINUTE, -5, GETUTCDATE())
    """, nativeQuery = true)
    long countRecentHigh(@Param("action") String action);
}
