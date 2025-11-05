package dev.kuklin.kworkcalendar.telegram;

import dev.kuklin.kworkcalendar.entities.TelegramUser;
import dev.kuklin.kworkcalendar.library.tgmodels.TelegramBot;
import dev.kuklin.kworkcalendar.library.tgmodels.TelegramFacade;
import dev.kuklin.kworkcalendar.library.tgmodels.UpdateHandler;
import dev.kuklin.kworkcalendar.library.tgutils.Command;
import dev.kuklin.kworkcalendar.services.telegram.TelegramUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

@Component
@Slf4j
public class AssistantTelegramFacade extends TelegramFacade {
    @Autowired
    private TelegramUserService telegramUserService;
    @Override
    public void handleUpdate(Update update) {
        try {
            if (!update.hasCallbackQuery() && !update.hasMessage()) return;
            User user = update.getMessage() != null ?
                    update.getMessage().getFrom() :
                    update.getCallbackQuery().getFrom();

            TelegramUser telegramUser = telegramUserService
                    .createOrGetUserByTelegram(user);

            log.info("New message {}", update);
            processInputUpdate(update).handle(update, telegramUser);
        } catch (Exception ex) {
            log.error("Error processing message", ex);
        }
    }

    public UpdateHandler processInputUpdate(Update update) {
        String request;
        if (update.hasCallbackQuery()) {
            request = update.getCallbackQuery().getData().split(TelegramBot.DEFAULT_DELIMETER)[0];
        } else if (update.getMessage().hasVoice()) {
            return getUpdateHandlerMap().get(Command.ASSISTANT_VOICE.getCommandText());
        } else {
            request = update.getMessage().getText().split(TelegramBot.DEFAULT_DELIMETER)[0];
        }

        if (request.startsWith(Command.ASSISTANT_CHOOSE_CALENDAR.getCommandText())) {
            return getUpdateHandlerMap().get(Command.ASSISTANT_CHOOSE_CALENDAR.getCommandText());
        }

        UpdateHandler updateHandler = getUpdateHandlerMap().get(request);
        if (updateHandler == null) {
            return getUpdateHandlerMap().get(Command.ASSISTANT_VOICE.getCommandText());
        }
        return updateHandler;

    }
}
