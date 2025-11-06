package dev.kuklin.kworkcalendar.services;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.function.Consumer;

import dev.kuklin.kworkcalendar.telegram.AssistantTelegramBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncService {

    @Async
    public void executeAsyncCustom(AssistantTelegramBot assistantTelegramBot, Update update, Consumer<Update> runnable) {
        try {
            runnable.accept(update);
        } finally {
            assistantTelegramBot.notifyAsyncDone(update);
        }
    }
}
