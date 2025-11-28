package dev.kuklin.kworkcalendar.telegram.handlers;

import dev.kuklin.kworkcalendar.entities.TelegramUser;
import dev.kuklin.kworkcalendar.library.tgmodels.UpdateHandler;
import dev.kuklin.kworkcalendar.library.tgutils.Command;
import dev.kuklin.kworkcalendar.library.tgutils.TelegramKeyboard;
import dev.kuklin.kworkcalendar.services.UserAuthNotificationService;
import dev.kuklin.kworkcalendar.services.UserMessagesLogService;
import dev.kuklin.kworkcalendar.telegram.AssistantTelegramBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssistantStartUpdateHandler implements UpdateHandler {
    private final AssistantTelegramBot assistantTelegramBot;
    private final UserMessagesLogService userMessagesLogService;
    private final UserAuthNotificationService userAuthNotificationService;
    private static final String ADMIN_TELEGRAM_USERNAME = "plai_admin";
    private static final Integer NOTIFY_AFTER_HOURS = 2;
    private static final String MSG = """
            Если вы уже отправили почту админу, и админ вам ответил и написал что “добавил вас”, то авторизуйтесь, введите команду /auth
            """;
    private static final String START_MESSAGE =
            """
                    Добро пожаловать!
                                                                      
                    🗓️ Я твой личный ассистент, который умеет ставить задачи в google календарь.\s        
                                        
                    📧 Чтобы начать пользоваться, нужно авторизоваться в google календаре (в нем будут храниться все ваши события), отправьте свою google почту админу @plai_admin. Админ выдаст разрешение на пользование ботом.    
                    """;

    @Override
    public void handle(Update update, TelegramUser telegramUser) {
        assistantTelegramBot.sendReturnedMessage(
                update.getMessage().getChatId(),
                START_MESSAGE,
                getAdminRedirectButton(),
                null
        );
        userAuthNotificationService.create(
                telegramUser.getTelegramId(),
                LocalDateTime.now().plusHours(NOTIFY_AFTER_HOURS),
                MSG
                );
        userMessagesLogService.createLog(
                telegramUser.getTelegramId(),
                telegramUser.getUsername(),
                telegramUser.getFirstname(),
                telegramUser.getLastname(),
                update.getMessage().getText()
        );
    }

    public InlineKeyboardMarkup getAuthButton() {
        TelegramKeyboard.TelegramKeyboardBuilder builder = TelegramKeyboard.builder();
        builder.row(TelegramKeyboard.button("Авторизация", Command.ASSISTANT_AUTH.getCommandText()));

        return builder.build();
    }

    public InlineKeyboardMarkup getAdminRedirectButton() {
        InlineKeyboardButton btn = InlineKeyboardButton.builder()
                .text("Написать админу")
                .url("https://t.me/" + ADMIN_TELEGRAM_USERNAME)   // ссылка на пользователя
                .build();

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(List.of(btn)))
                .build();
    }

    @Override
    public String getHandlerListName() {
        return Command.ASSISTANT_START.getCommandText();
    }
}
