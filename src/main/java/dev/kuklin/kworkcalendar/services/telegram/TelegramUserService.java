package dev.kuklin.kworkcalendar.services.telegram;

import dev.kuklin.kworkcalendar.entities.TelegramUser;
import dev.kuklin.kworkcalendar.repositories.TelegramUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramUserService {
    private static final Long DEFAULT_RESPONSE_COUNT = 0L;
    private final TelegramUserRepository telegramUserRepository;

    public TelegramUser getTelegramUserByTelegramIdOrNull(Long telegramId) {
        return telegramUserRepository
                .findTelegramUserByTelegramId(telegramId)
                .orElse(null)
                ;
    }

    public TelegramUser createOrGetUserByTelegram(User telegramUser) {

        Optional<TelegramUser> optionalTelegramUser =
                telegramUserRepository.findTelegramUserByTelegramId(telegramUser.getId());

        if (optionalTelegramUser.isPresent()) {
            return optionalTelegramUser.get();
        }
        TelegramUser tgUser = TelegramUser.convertFromTelegram(telegramUser)
                .setResponseCount(DEFAULT_RESPONSE_COUNT);
        return telegramUserRepository.save(tgUser);
    }

    public TelegramUser save(TelegramUser telegramUser) {
        return telegramUserRepository.save(telegramUser);
    }
}
