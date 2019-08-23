package com.example.mcr_rework;

public class Messages {

    private String from, message,type, time, date;

    public Messages(){

    }


    public Messages(String from, String message, String type,String time, String date){
        this.from = from;
        this.message = message;
        this.type = type;
        this.time = time;
        this.date = date;

    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
