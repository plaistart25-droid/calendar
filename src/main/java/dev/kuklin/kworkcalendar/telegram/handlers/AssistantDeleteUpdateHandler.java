package dev.kuklin.kworkcalendar.telegram.handlers;

import dev.kuklin.kworkcalendar.entities.TelegramUser;
import dev.kuklin.kworkcalendar.library.tgmodels.TelegramBot;
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

@RequiredArgsConstructor
@Component
public class AssistantDeleteUpdateHandler implements UpdateHandler {
    private final AssistantTelegramBot assistantTelegramBot;
    private final CalendarService calendarService;
    private static final String ERROR_MSG = "Не получилось удалить мероприятие";
    private static final String GOOGLE_OTHER_ERROR_MESSAGE =
            "Попробуйте обратиться позже!";
    private static final String GOOGLE_AUTH_ERROR_MESSAGE =
            "Вам нужно пройти авторизацию заново!";
    @Override
    public void handle(Update update, TelegramUser telegramUser) {
        CallbackQuery callback = update.getCallbackQuery();
        Long chatId = callback.getMessage().getChatId();
        Integer messageId = callback.getMessage().getMessageId();

        String[] data = callback.getData().split(TelegramBot.DEFAULT_DELIMETER);
        String eventId = data[1];

        try {
            calendarService.removeEventInCalendar(
                    eventId, telegramUser.getTelegramId()
            );
            assistantTelegramBot.sendDeleteMessage(chatId, messageId);
        } catch (IOException e) {
            assistantTelegramBot.sendReturnedMessage(chatId, ERROR_MSG);
        } catch (TokenRefreshException e) {
            if (e.getReason().equals(TokenRefreshException.Reason.INVALID_GRANT)) {
                assistantTelegramBot.sendReturnedMessage(chatId, GOOGLE_AUTH_ERROR_MESSAGE);
            } else {
                assistantTelegramBot.sendReturnedMessage(chatId, GOOGLE_OTHER_ERROR_MESSAGE);
            }
        }
    }


    @Override
    public String getHandlerListName() {
        return Command.ASSISTANT_DELETE.getCommandText();
    }
}
