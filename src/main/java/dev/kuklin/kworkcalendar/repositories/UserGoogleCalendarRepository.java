package dev.kuklin.kworkcalendar.repositories;

import dev.kuklin.kworkcalendar.entities.UserGoogleCalendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserGoogleCalendarRepository extends JpaRepository<UserGoogleCalendar, Long> {
}
