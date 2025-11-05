package dev.kuklin.kworkcalendar.telegram.handlers;

import dev.kuklin.kworkcalendar.entities.TelegramUser;
import dev.kuklin.kworkcalendar.library.tgmodels.TelegramBot;
import dev.kuklin.kworkcalendar.library.tgmodels.UpdateHandler;
import dev.kuklin.kworkcalendar.library.tgutils.Command;
import dev.kuklin.kworkcalendar.services.UserGoogleCalendarService;
import dev.kuklin.kworkcalendar.services.google.CalendarService;
import dev.kuklin.kworkcalendar.telegram.AssistantTelegramBot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class SetCalendarIdUpdateHandler implements UpdateHandler {
    private final AssistantTelegramBot assistantTelegramBot;
    private final UserGoogleCalendarService userGoogleCalendarService;
    private final CalendarService calendarService;
    private static final String SUCCESS_MSG = "Календарь установлен";
    private static final String ERROR_MSG = "Неверный формат команды";
    public static final String CALENDAR_IS_NULL_MSG = "Вам необходимо установить свой календарь! Для инструкций введите команду /start";

    @Override
    public void handle(Update update, TelegramUser telegramUser) {
        //Ожидается сообщение формата /set calendarId
        Message message = update.getMessage();
        Long chatId = message.getChatId();
        assistantTelegramBot.sendChatActionTyping(chatId);

        String calendarId = extractCalendarId(message.getText());
        if (calendarId == null) {
            assistantTelegramBot.sendReturnedMessage(chatId, ERROR_MSG);
            return;
        }
        if (!checkCalendarConnection(telegramUser.getTelegramId(), calendarId)) {
            assistantTelegramBot.sendReturnedMessage(chatId, "Календарь или не существует, или к нему не установлен доступ!");
            return;
        }

        userGoogleCalendarService.setCalendarIdByTelegramId(telegramUser.getTelegramId(), calendarId);
        assistantTelegramBot.sendReturnedMessage(chatId, SUCCESS_MSG);
    }

    private boolean checkCalendarConnection(Long telegramId, String calendarId) {
        if (calendarId == null) return false;
        return calendarService.existConnectionCalendarWithNoAuth(calendarId);
    }

    private String extractCalendarId(String message) {
        String[] parts = message.split(TelegramBot.DEFAULT_DELIMETER);

        if (parts.length != 2) return null;
        return parts[1];
    }

    @Override
    public String getHandlerListName() {
        return Command.ASSISTANT_SET_CALENDARID.getCommandText();
    }
}
