package dev.kuklin.kworkcalendar.telegram;

import dev.kuklin.kworkcalendar.configurations.TelegramAiAssistantCalendarBotKeyComponents;
import dev.kuklin.kworkcalendar.library.tgmodels.TelegramBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Slf4j
public class AssistantTelegramBot extends TelegramBot {
    public static final String DELIMETER = "#";
    @Autowired
    private AssistantTelegramFacade assistantTelegramFacade;

    public AssistantTelegramBot(TelegramAiAssistantCalendarBotKeyComponents telegramAiAssistantCalendarBotKeyComponents) {
        super(telegramAiAssistantCalendarBotKeyComponents.getKey());
    }

    @Override
    public void onUpdateReceived(Update update) {
        assistantTelegramFacade.handleUpdate(update);
    }

    @Override
    public String getBotUsername() {
        return "ai assistant";
    }
}
