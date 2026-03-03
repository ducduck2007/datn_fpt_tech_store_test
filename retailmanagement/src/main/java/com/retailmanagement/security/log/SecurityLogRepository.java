package com.retailmanagement.security.log;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityLogRepository extends JpaRepository<SecurityLog, Long> {
}
