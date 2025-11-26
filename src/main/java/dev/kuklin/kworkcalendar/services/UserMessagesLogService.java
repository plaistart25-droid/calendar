package dev.kuklin.kworkcalendar.services;

import dev.kuklin.kworkcalendar.entities.AssistantGoogleOAuth;
import dev.kuklin.kworkcalendar.entities.TelegramUser;
import dev.kuklin.kworkcalendar.entities.UserMessagesLog;
import dev.kuklin.kworkcalendar.repositories.UserMessagesLogRepository;
import dev.kuklin.kworkcalendar.services.google.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserMessagesLogService {

    private final UserMessagesLogRepository userMessagesLogRepository;
    private final TokenService tokenService;

    public void createLog(
            Long telegramId,
            String username,
            String firstname,
            String lastname,
            String message
    ) {

        AssistantGoogleOAuth oAuth = tokenService.getByTelegramIdOrNull(telegramId);

        String googleEmail = null;
        if (oAuth != null) {
            googleEmail = oAuth.getEmail();
        }
        userMessagesLogRepository.save(
                new UserMessagesLog()
                        .setTelegramId(telegramId)
                        .setUsername(username)
                        .setFirstname(firstname)
                        .setLastname(lastname)
                        .setGoogleEmail(googleEmail)
                        .setMessage(message)
        );
    }

    public void createLog(
            TelegramUser telegramUser,
            String message
    ) {

        AssistantGoogleOAuth oAuth = tokenService.getByTelegramIdOrNull(telegramUser.getTelegramId());

        String googleEmail = null;
        if (oAuth != null) {
            googleEmail = oAuth.getEmail();
        }
        userMessagesLogRepository.save(
                new UserMessagesLog()
                        .setTelegramId(telegramUser.getTelegramId())
                        .setUsername(telegramUser.getUsername())
                        .setFirstname(telegramUser.getFirstname())
                        .setLastname(telegramUser.getLastname())
                        .setGoogleEmail(googleEmail)
                        .setMessage(message)
        );
    }
}
