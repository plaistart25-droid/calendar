package dev.kuklin.kworkcalendar.telegram.handlers.notificationsettings;

import dev.kuklin.kworkcalendar.entities.TelegramUser;
import dev.kuklin.kworkcalendar.entities.UserNotificationSettings;
import dev.kuklin.kworkcalendar.library.tgmodels.TelegramBot;
import dev.kuklin.kworkcalendar.library.tgmodels.UpdateHandler;
import dev.kuklin.kworkcalendar.library.tgutils.Command;
import dev.kuklin.kworkcalendar.library.tgutils.TelegramKeyboard;
import dev.kuklin.kworkcalendar.models.TokenRefreshException;
import dev.kuklin.kworkcalendar.services.UserMessagesLogService;
import dev.kuklin.kworkcalendar.services.UserNotificationSettingsService;
import dev.kuklin.kworkcalendar.services.google.CalendarService;
import dev.kuklin.kworkcalendar.telegram.AssistantTelegramBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssistantTimeZoneUpdateHandler implements UpdateHandler {

    private final AssistantTelegramBot assistantTelegramBot;
    private final UserMessagesLogService userMessagesLogService; //TODO
    private final CalendarService calendarService;
    private final UserNotificationSettingsService userNotificationSettingsService;

    private static final String CMD = Command.ASSISTANT_TZ.getCommandText();
    private static final String MSG = """
                "Выбери свой часовой пояс (формат UTC+N):"
            """;
    private static final String TZ_MSG = """
                Часовой пояс установлен:\nUTC+
            """;
    private static final String TZ_ERROR_MSG = """
                Не получилось определить часовой пояс! Попробуйте повторить действие позже!
            """;
    private static final String GOOGLE_ERROR_MSG = """
                Не получилось обновить время в гугл календаре! Попробуйте повторить действие позже!
            """;
    private static final String CALENDAR_ERROR_MSG = """
                У вас не установлен календарь!
            """;

    @Override
    public void handle(Update update, TelegramUser telegramUser) {
        //Если пришло сообщение - то отправляем новое сообщение
        if (update.hasMessage()) processMessage(update, telegramUser);
        //Если пришел колбэк, то значит надо отредактировать сообщение
        //Также отвечает за установки таймзоны в БД
        else if (update.hasCallbackQuery()) processCallback(update, telegramUser);
    }

    private void processMessage(Update update, TelegramUser telegramUser) {
        Long chatId = update.getMessage().getChatId();
        assistantTelegramBot.sendChatActionTyping(chatId);

        assistantTelegramBot.sendReturnedMessage(chatId, MSG, buildTimezoneKeyboard(), null);
    }

    private void processCallback(Update update, TelegramUser telegramUser) {
        //Извлечение данных
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String callbackData = callbackQuery.getData();

        String[] parts = callbackData.split(TelegramBot.DEFAULT_DELIMETER);
        //Если в колбэк-данных нет ничего, кроме комманды - то
        // возвращаем измененное сообщение
        if (parts.length == 1) {
            assistantTelegramBot.sendEditMessage(
                    chatId, MSG, messageId, buildTimezoneKeyboard());
            return;
        }
        if (parts.length < 2 || !CMD.equals(parts[0])) return;

        //Устанавливаем таймзону
        try {
            int offset = Integer.parseInt(parts[1]);

            // Сохраняем смещение в настройках пользователя
            UserNotificationSettings settings = userNotificationSettingsService.updateUtcOffset(
                    telegramUser.getTelegramId(),
                    offset
            );

            var newTime = calendarService
                    .setNewTimeZoneOrNull(
                            telegramUser.getTelegramId(),
                            offset);

            if (newTime == null) {
                assistantTelegramBot.sendReturnedMessage(
                        chatId, CALENDAR_ERROR_MSG);
                return;
            }

            String text = TZ_MSG + offset;

            // Обновляем исходное сообщение и убираем клавиатуру
            assistantTelegramBot.sendEditMessage(chatId, text, messageId, null);
        } catch (NumberFormatException e) {
            log.error("Ошибка парсинга смещения часового пояса из callbackData: {}", callbackData, e);
            assistantTelegramBot.sendReturnedMessage(
                    chatId, TZ_ERROR_MSG
            );
        } catch (TokenRefreshException e) {
            log.error("Invalid google token: telegram_id:{}", telegramUser.getTelegramId(), e);
            assistantTelegramBot.sendReturnedMessage(
                    chatId, TZ_ERROR_MSG
            );
        } catch (IOException e) {
            log.error("Google connection error!: telegram_id{}", telegramUser.getTelegramId(), e);
            assistantTelegramBot.sendReturnedMessage(
                    chatId, GOOGLE_ERROR_MSG
            );
        }
    }

    /**
     * Строим клавиатуру с UTC+0 ... UTC+14, по 3 кнопки в строке.
     * callbackData: "/timezone <offset>"
     */
    private InlineKeyboardMarkup buildTimezoneKeyboard() {
        TelegramKeyboard.TelegramKeyboardBuilder builder = TelegramKeyboard.builder();
        List<InlineKeyboardButton> row = new ArrayList<>();

        for (int offset = 0; offset <= 14; offset++) {
            String text = "UTC+" + offset;
            String callbackData = CMD + TelegramBot.DEFAULT_DELIMETER + offset;
            row.add(TelegramKeyboard.button(text, callbackData));

            // По 3 кнопки в строке
            if (row.size() == 3 || offset == 14) {
                builder.row(row);
                row = new ArrayList<>();
            }
        }

        return builder.build();
    }

    @Override
    public String getHandlerListName() {
        return Command.ASSISTANT_TZ.getCommandText();
    }
}
