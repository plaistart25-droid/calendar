package dev.kuklin.kworkcalendar.library.tgmodels;

import dev.kuklin.kworkcalendar.entities.TelegramUser;
import dev.kuklin.kworkcalendar.telegram.AssistantTelegramFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface UpdateHandler {
    void handle(Update update, TelegramUser telegramUser);

    String getHandlerListName();

    @Autowired
    default void registerMyself(AssistantTelegramFacade messageFacade) {
        messageFacade.register(getHandlerListName(), this);
    }

}
