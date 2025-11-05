package dev.kuklin.kworkcalendar.repositories;

import dev.kuklin.kworkcalendar.entities.AiMessageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiMessageLogRepository extends JpaRepository<AiMessageLog, Long> {
}
