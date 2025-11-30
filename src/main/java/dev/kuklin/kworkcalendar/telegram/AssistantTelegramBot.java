package dev.kuklin.kworkcalendar.telegram;

import dev.kuklin.kworkcalendar.configurations.TelegramAiAssistantCalendarBotKeyComponents;
import dev.kuklin.kworkcalendar.library.tgmodels.TelegramBot;
import dev.kuklin.kworkcalendar.services.AsyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@Component
@Slf4j
public class AssistantTelegramBot extends TelegramBot {
    public static final String DELIMETER = "#";
    @Autowired
    private AssistantTelegramFacade assistantTelegramFacade;
    @Autowired
    private AsyncService asyncService;

    public AssistantTelegramBot(TelegramAiAssistantCalendarBotKeyComponents telegramAiAssistantCalendarBotKeyComponents) {
        super(telegramAiAssistantCalendarBotKeyComponents.getKey());
    }

    @Override
    public void onUpdateReceived(Update update) {
        boolean result = doAsync(update, u -> assistantTelegramFacade.handleUpdate(update));

        if (!result) {
            notifyAlreadyInProcess(update);
        }
    }

    /**
     * Удобный метод для отправки документа из байтов (CSV и т.п.)
     */
    public Message sendDocument(long chatId, byte[] content, String filename, String caption) throws TelegramApiException {
        InputFile inputFile = new InputFile(
                new ByteArrayInputStream(content),
                filename
        );

        SendDocument sendDocument = SendDocument.builder()
                .chatId(chatId)
                .document(inputFile)
                .caption(caption)
                .build();

        return execute(sendDocument);
    }

    private final Set<Long> inProcess = new HashSet<>();

    public static User getUserFromUpdate(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getFrom();
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getFrom();
        }

        return null;
    }

    public void notifyAsyncDone(Update update) {
        if (update.hasCallbackQuery()) {
            return;
        }

        User user = getUserFromUpdate(update);
        if (user == null) {
            log.error("Not a message or callback {} in async done", update);
            return;
        }
        Long tgUserId = user.getId();
        inProcess.remove(tgUserId);
    }

    public boolean doAsync(Update update, Consumer<Update> runnable) {
        if (update.hasCallbackQuery()) {
            asyncService.executeAsyncCustom(this, update, runnable);
            return true;
        }

        User user = getUserFromUpdate(update);
        if (user == null) {
            log.error("Not a message or callback {}", update);
            return false;
        }

        Long tgUserId = user.getId();
        if (inProcess.add(tgUserId)) {
            try {
                asyncService.executeAsyncCustom(this, update, runnable);
            } catch (Exception ex) {
                inProcess.remove(tgUserId);
                log.error( "Parallel execution failed", ex);
            }
            return true;
        }

        return false;
    }

    @Override
    public String getBotUsername() {
        return "ai assistant";
    }
}
