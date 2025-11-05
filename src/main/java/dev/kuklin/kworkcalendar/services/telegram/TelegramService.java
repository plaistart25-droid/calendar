package dev.kuklin.kworkcalendar.services.telegram;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kuklin.kworkcalendar.configurations.feignclients.TelegramFeignClient;
import dev.kuklin.kworkcalendar.library.tgmodels.TelegramBotClient;
import dev.kuklin.kworkcalendar.library.tgutils.TelegramBotFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramService {

    private final TelegramFeignClient telegramFeignClient;

    public TelegramBotFile getFileOrNull(TelegramBotClient telegramBot, String fileId) {
        try {
            return new ObjectMapper().readValue(
                    telegramFeignClient.getFileRaw(
                            telegramBot.getToken(), fileId),
                    TelegramBotFile.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public byte[] downloadFile(TelegramBotClient telegramBot, TelegramBotFile telegramBotFile) {
        return telegramFeignClient.downloadFile(
                telegramBot.getToken(),
                telegramBotFile.getResult().getFilePath());
    }

    public InputStream downloadPhotoOrNull(TelegramBotClient telegramBot, String fileId) {
        return new ByteArrayInputStream(downloadFileOrNull(telegramBot, fileId));
    }

    public byte[] downloadFileOrNull(TelegramBotClient telegramBot,String fileId) {
        TelegramBotFile telegramBotFile = getFileOrNull(telegramBot, fileId);
        if (telegramBotFile == null) return null;

        return downloadFile(telegramBot, telegramBotFile);
    }

}
