package com.retailmanagement.audit;

import com.retailmanagement.dto.response.UserActionReportResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {
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

    //Báo cáo theo user
    @Query("""
        select new com.retailmanagement.dto.response.UserActionReportResponse(
                u.id,
                u.username,
                COUNT(l)
                )
        FROM User u
        LEFT JOIN AuditLog l ON l.user.id = u.id
        group by u.id, u.username
        order by COUNT(l) DESC 
        """)
    List<UserActionReportResponse> countTotalActionByUser();

    @Query("""
        SELECT l
        from AuditLog l
        where l.createdAt BETWEEN :start and :end
        order by l.createdAt DESC 
        """)
    List<AuditLog> findLogsThisWeek(@Param("start") Instant start,@Param("end") Instant end);

    @Query("""
        SELECT l
        from AuditLog l
        where l.module = :module
        """)
    public Page<AuditLog> findByModule(@Param("module") String module, Pageable pageable);


    List<AuditLog> findByModuleOrderByCreatedAtDesc(String module);
}
