package dev.kuklin.kworkcalendar.telegram.handlers.notificationsettings;

import dev.kuklin.kworkcalendar.entities.TelegramUser;
import dev.kuklin.kworkcalendar.library.tgmodels.TelegramBot;
import dev.kuklin.kworkcalendar.library.tgmodels.UpdateHandler;
import dev.kuklin.kworkcalendar.library.tgutils.Command;
import dev.kuklin.kworkcalendar.library.tgutils.TelegramKeyboard;
import dev.kuklin.kworkcalendar.services.UserNotificationSettingsService;
import dev.kuklin.kworkcalendar.telegram.AssistantTelegramBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssistantDailyTimeUpdateHandler implements UpdateHandler {

    private final AssistantTelegramBot assistantTelegramBot;
    private final UserNotificationSettingsService userNotificationSettingsService;

    private static final String CMD = Command.ASSISTANT_DAILY_TIME.getCommandText();

    private static final String MSG = "Выбери время, во сколько присылать утреннее уведомление:";
    private static final String TIME_SET_MSG = "Время ежедневного уведомления установлено: ";
    private static final String ERROR_MSG = "Не получилось определить время, попробуй ещё раз.";

    @Override
    public void handle(Update update, TelegramUser telegramUser) {
        if (update.hasMessage()) {
            // Пришло сообщение (команда) – отправляем новое сообщение с клавиатурой
            processMessage(update, telegramUser);
        } else if (update.hasCallbackQuery()) {
            // Пришёл callback от кнопки – разбираем и сохраняем время
            processCallback(update, telegramUser);
        }
    }

    private void processMessage(Update update, TelegramUser telegramUser) {
        Long chatId = update.getMessage().getChatId();
        assistantTelegramBot.sendChatActionTyping(chatId);

        assistantTelegramBot.sendReturnedMessage(
                chatId,
                MSG,
                buildTimeKeyboard(),
                null
        );
    }

    private void processCallback(Update update, TelegramUser telegramUser) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String callbackData = callbackQuery.getData();

        String[] parts = callbackData.split(TelegramBot.DEFAULT_DELIMETER);

        // Если пришёл только командный префикс — просто перерисуем клавиатуру
        if (parts.length == 1) {
            assistantTelegramBot.sendEditMessage(
                    chatId,
                    MSG,
                    messageId,
                    buildTimeKeyboard()
            );
            return;
        }

        if (parts.length < 2 || !CMD.equals(parts[0])) {
            // Не наш callback – игнорируем
            return;
        }

        String timePart = parts[1];

        try {
            // Парсим "HH:mm" в LocalTime
            LocalTime time = LocalTime.parse(timePart);

            // Сохраняем время утреннего уведомления
            userNotificationSettingsService.updateDailyTime(
                    telegramUser.getTelegramId(),
                    time
            );

            String text = TIME_SET_MSG + timePart;

            // Обновляем исходное сообщение и убираем клавиатуру
            assistantTelegramBot.sendEditMessage(
                    chatId,
                    text,
                    messageId,
                    null
            );
        } catch (Exception e) {
            log.error("Ошибка парсинга времени из callbackData: {}", callbackData, e);
            assistantTelegramBot.sendReturnedMessage(
                    chatId,
                    ERROR_MSG
            );
        }
    }

    /**
     * Строим клавиатуру с временем от 00:00 до 23:30, шаг 30 минут.
     * callbackData: "<CMD><DELIM>HH:mm"
     */
    private InlineKeyboardMarkup buildTimeKeyboard() {
        TelegramKeyboard.TelegramKeyboardBuilder builder = TelegramKeyboard.builder();
        List<InlineKeyboardButton> row = new ArrayList<>();

        for (int hour = 0; hour < 24; hour++) {
            for (int minute = 0; minute < 60; minute += 30) {
                String time = String.format("%02d:%02d", hour, minute);
                String text = time;
                String callbackData = CMD + TelegramBot.DEFAULT_DELIMETER + time;

                row.add(TelegramKeyboard.button(text, callbackData));

                // по 4 кнопки в ряд, чтобы не разъезжалось
                if (row.size() == 4) {
                    builder.row(row);
                    row = new ArrayList<>();
                }
            }
        }

        // добиваем остаток, если не кратно 4
        if (!row.isEmpty()) {
            builder.row(row);
        }

        return builder.build();
    }

    @Override
    public String getHandlerListName() {
        return CMD;
    }
}
