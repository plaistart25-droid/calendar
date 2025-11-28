package dev.kuklin.kworkcalendar.telegram.handlers.notificationsettings;

import dev.kuklin.kworkcalendar.entities.TelegramUser;
import dev.kuklin.kworkcalendar.library.tgmodels.UpdateHandler;
import dev.kuklin.kworkcalendar.library.tgutils.Command;
import dev.kuklin.kworkcalendar.telegram.AssistantTelegramBot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class AssistantCloseUpdateHandler implements UpdateHandler {
    private final AssistantTelegramBot assistantTelegramBot;
    @Override
    public void handle(Update update, TelegramUser telegramUser) {
        if (!update.hasCallbackQuery()) return;

        CallbackQuery query = update.getCallbackQuery();
        Long chatId = query.getMessage().getChatId();

        assistantTelegramBot.sendDeleteMessage(chatId, query.getMessage().getMessageId());
    }

    @Override
    public String getHandlerListName() {
        return Command.ASSISTANT_CLOSE.getCommandText();
    }
}
