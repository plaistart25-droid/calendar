package dev.kuklin.kworkcalendar.models;

public class TimeZoneUpdateException extends Exception{
    public TimeZoneUpdateException() {
        super("Google calendar time zone update error!");
    }
    public TimeZoneUpdateException(String message) {
        super(message);
    }
}
