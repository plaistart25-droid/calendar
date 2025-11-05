package dev.kuklin.kworkcalendar.configurations.feignclients;

import dev.kuklin.kworkcalendar.configurations.FeignClientConfig;
import dev.kuklin.kworkcalendar.library.model.TranscriptionResponse;
import dev.kuklin.kworkcalendar.library.model.openaimodels.OpenAiChatCompletionRequest;
import dev.kuklin.kworkcalendar.library.model.openaimodels.OpenAiChatCompletionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;


@FeignClient(
        value = "open-ai-feign-client",
        url = "${integrations.openai-api.url}",
        configuration = FeignClientConfig.class
)
public interface OpenAiFeignClient {
    @PostMapping("chat/completions")
    OpenAiChatCompletionResponse generate(@RequestHeader("Authorization") String key,
                                          @RequestBody OpenAiChatCompletionRequest request);

    @PostMapping(value = "audio/transcriptions",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    TranscriptionResponse transcribeAudio(
            @RequestHeader("Authorization") String key,
            @RequestPart("file") MultipartFile file,
            @RequestPart("model") String model
    );
}
