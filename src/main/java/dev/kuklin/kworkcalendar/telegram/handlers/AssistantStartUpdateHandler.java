package dev.kuklin.kworkcalendar.telegram.handlers;

import dev.kuklin.kworkcalendar.entities.TelegramUser;
import dev.kuklin.kworkcalendar.library.tgmodels.UpdateHandler;
import dev.kuklin.kworkcalendar.library.tgutils.Command;
import dev.kuklin.kworkcalendar.library.tgutils.TelegramKeyboard;
import dev.kuklin.kworkcalendar.services.UserMessagesLogService;
import dev.kuklin.kworkcalendar.telegram.AssistantTelegramBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssistantStartUpdateHandler implements UpdateHandler {
    private final AssistantTelegramBot assistantTelegramBot;
    private final UserMessagesLogService userMessagesLogService;
    private static final String START_MESSAGE =
            """
                    Добро пожаловать!\s
                                                                      
                    🗓️ Я твой личный ассистент, который умеет ставить задачи в google календарь.\s
                    
                    Просто опиши задачу текстом или голосовым сообщением:
                    Пример: Завтра у меня встреча с Иваном в 15:00, длительность 2 часа.
                    Пример: Напомни мне 17 ноября поздравить маму с днем рождения
                                                                      
                    📧 Чтобы авторизоваться в google календаре, отправьте свою почту админу @plai_admin. Дождитесь ответа администратора.\s
                    
                    После этого вернитесь в бота и введите команду /auth.
                                                                      
                    """;

    @Override
    public void handle(Update update, TelegramUser telegramUser) {
        assistantTelegramBot.sendReturnedMessage(
                update.getMessage().getChatId(),
                START_MESSAGE,
                getAuthButton(),
                null
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

    @Override
    public String getHandlerListName() {
        return Command.ASSISTANT_START.getCommandText();
    }
}
