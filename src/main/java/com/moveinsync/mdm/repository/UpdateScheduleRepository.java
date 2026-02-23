package com.moveinsync.mdm.repository;

import com.moveinsync.mdm.entity.UpdateSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

public interface UpdateScheduleRepository extends JpaRepository<UpdateSchedule, Long>, JpaSpecificationExecutor<UpdateSchedule> {
    List<UpdateSchedule> findByStatus(String status);
    List<UpdateSchedule> findByStatusAndScheduledTimeLessThanEqual(String status, LocalDateTime dateTime);
}
