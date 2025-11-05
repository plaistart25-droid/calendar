package dev.kuklin.kworkcalendar.repositories;

import dev.kuklin.kworkcalendar.entities.TelegramUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TelegramUserRepository extends JpaRepository<TelegramUser, Long> {
    Optional<TelegramUser> findTelegramUserByTelegramId(Long telegramId);
}
