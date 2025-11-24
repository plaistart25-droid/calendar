package dev.kuklin.kworkcalendar.telegram.handlers;

import dev.kuklin.kworkcalendar.entities.TelegramUser;
import dev.kuklin.kworkcalendar.library.tgmodels.UpdateHandler;
import dev.kuklin.kworkcalendar.library.tgutils.Command;
import dev.kuklin.kworkcalendar.services.UserMessagesLogService;
import dev.kuklin.kworkcalendar.telegram.AssistantTelegramBot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class AssistantHelpUpdateHandler implements UpdateHandler {
    private final AssistantTelegramBot telegramBot;
    private final UserMessagesLogService userMessagesLogService;
    private static final String HELP_MSG =
            """
                    <b>ИНСТРУКЦИЯ ПО РУЧНОЙ УСТАНОВКЕ КАЛЕНДАРЯ</b>
                               
                    1) Дай боту доступ на изменение событий. В настройках календаря, в разделе "Имеют доступ", добавь этот адрес:
                    tgbot-calendar-assistante@calendar-477314.iam.gserviceaccount.com
                                
                    2) Найди идентификатор календаря. Найти его можно в настройках Google Календаря → вкладка «Интеграция календаря».
                    
                    3) Укажи идентификатор календаря командой:
                    /set calendarId
                    
                    <b>КОММАНДЫ:</b>
                    /auth - авторизация
                    /auth_status - узнать статус авторизации
                    /choosecalendar - выбор календаря         
                    """;
    @Override
    public void handle(Update update, TelegramUser telegramUser) {
        telegramBot.sendReturnedMessage(update.getMessage().getChatId(), HELP_MSG);
        userMessagesLogService.createLog(
                telegramUser.getTelegramId(),
                telegramUser.getUsername(),
                telegramUser.getFirstname(),
                telegramUser.getLastname(),
                update.getMessage().getText()
        );
    }

    @Override
    public String getHandlerListName() {
        return Command.ASSISTANT_HELP.getCommandText();
    }
}
