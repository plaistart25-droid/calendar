package dev.kuklin.kworkcalendar.library.model;

import lombok.Data;

@Data
public class SpeechRequest {
    String input;
    String model;
    String voice;
}
