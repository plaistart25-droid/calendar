package dev.kuklin.kworkcalendar.ai;

import dev.kuklin.kworkcalendar.configurations.feignclients.OpenAiFeignClient;
import dev.kuklin.kworkcalendar.library.model.ByteArrayMultipartFile;
import dev.kuklin.kworkcalendar.library.model.TranscriptionResponse;
import dev.kuklin.kworkcalendar.library.model.openaimodels.OpenAiChatCompletionRequest;
import dev.kuklin.kworkcalendar.library.model.openaimodels.OpenAiChatCompletionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class OpenAiIntegrationService {

    private final OpenAiFeignClient openAiFeignClient;

    public OpenAiIntegrationService(OpenAiFeignClient openAiFeignClient) {
        this.openAiFeignClient = openAiFeignClient;
    }

    public String fetchResponse(String aiKey, String content) {
        OpenAiChatCompletionRequest request =
                OpenAiChatCompletionRequest.makeDefaultRequest(content);

        String key = "Bearer " + aiKey;
        OpenAiChatCompletionResponse response =
                openAiFeignClient.generate(key, request);

        return response.getChoices().get(0).getMessage().getContent();
    }

    public String fetchAudioResponse(byte[] content, String aiKey) {
        MultipartFile multipartFile = new ByteArrayMultipartFile(
                "file",
                "audio.ogg",
                "audio/ogg",
                content
        );

        TranscriptionResponse response = openAiFeignClient.transcribeAudio(
                "Bearer " + aiKey,
                multipartFile,
                "whisper-1"
//                "gpt-4o-transcribe"
        );

        return response.getText();
    }

}
