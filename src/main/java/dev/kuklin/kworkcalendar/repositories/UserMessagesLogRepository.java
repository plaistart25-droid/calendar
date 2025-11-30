package dev.kuklin.kworkcalendar.repositories;

import dev.kuklin.kworkcalendar.entities.UserMessagesLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMessagesLogRepository extends JpaRepository<UserMessagesLog, Long> {
    List<UserMessagesLog> findAllByOrderByCreatedAtDesc();
}
