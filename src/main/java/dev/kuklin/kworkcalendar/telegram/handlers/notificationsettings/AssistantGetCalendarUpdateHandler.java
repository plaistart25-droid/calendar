package dev.kuklin.kworkcalendar.telegram.handlers.notificationsettings;

import dev.kuklin.kworkcalendar.entities.TelegramUser;
import dev.kuklin.kworkcalendar.library.tgmodels.UpdateHandler;
import dev.kuklin.kworkcalendar.library.tgutils.Command;
import dev.kuklin.kworkcalendar.models.TokenRefreshException;
import dev.kuklin.kworkcalendar.services.google.CalendarService;
import dev.kuklin.kworkcalendar.telegram.AssistantTelegramBot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AssistantGetCalendarUpdateHandler implements UpdateHandler {
    private final AssistantTelegramBot assistantTelegramBot;
    private final CalendarService calendarService;
    private static final String TOKEN_ERROR_MSG = """
                Проверьте статус вашей авторизации!
            """;
    private static final String GOOGLE_ERROR_MSG = """
                Ошибка соединения с Google! Попробуйте повторить действие позже!
            """;
    @Override
    public void handle(Update update, TelegramUser telegramUser) {
        Long chatId = update.getMessage() != null
                ? update.getMessage().getChatId()
                : update.getCallbackQuery().getMessage().getChatId();
        try {
            if (update.hasMessage()) processMessage(update, telegramUser);
            else if (update.hasCallbackQuery()) processCallback(update, telegramUser);
        } catch (TokenRefreshException e) {
            assistantTelegramBot.sendReturnedMessage(chatId, TOKEN_ERROR_MSG);
        } catch (IOException e) {
            assistantTelegramBot.sendReturnedMessage(chatId, GOOGLE_ERROR_MSG);
        }

    }

    private void processMessage(Update update, TelegramUser telegramUser) throws TokenRefreshException, IOException {
        Long chatId = update.getMessage().getChatId();
        assistantTelegramBot.sendChatActionTyping(chatId);

        assistantTelegramBot.sendReturnedMessage(chatId, calendarService.getCalendarSettingsString(telegramUser.getTelegramId()));
    }

    private void processCallback(Update update, TelegramUser telegramUser) throws TokenRefreshException, IOException {
        //Извлечение данных
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();

        assistantTelegramBot.sendEditMessage(
                chatId, calendarService.getCalendarSettingsString(telegramUser.getTelegramId()),
                messageId, null);
    }

    @Override
    public String getHandlerListName() {
        return Command.ASSISTANT_GET_CALENDAR.getCommandText();
    }
}
