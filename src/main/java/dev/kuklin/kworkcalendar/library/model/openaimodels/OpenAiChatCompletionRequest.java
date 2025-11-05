package dev.kuklin.kworkcalendar.library.model.openaimodels;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class OpenAiChatCompletionRequest {
    private String model;
    private List<Message> messages;
    private float temperature;

    public static final float TEMPERATURE_DEFAULT = 0.1f;
    private static final String MODEL_DEFAULT = "gpt-4o";

    public static OpenAiChatCompletionRequest makeDefaultRequest(
            String content) {

        return new OpenAiChatCompletionRequest()
                .setTemperature(TEMPERATURE_DEFAULT)
                .setModel(MODEL_DEFAULT)
                .setMessages(Message.getFirstMessage(content));
    }

}
