package com.tefah.neverforget;

import org.parceler.Parcel;

/**
 * class to hold the task attributes
 */
@Parcel
public class Task {
    int id;
    String text, audioFilePath, imageFilePath;
    long timeStamp;
    boolean alarm;

    // empty constructor needed by the Parceler library
    public Task(){}

    public Task( long timeStamp, boolean alarm, String text){
        this.timeStamp = timeStamp;
        this.alarm = alarm;
        this.text = text;
    }

    public Task(int id, long timeStamp, boolean alarm, String text){
        this.id = id;
        this.timeStamp = timeStamp;
        this.alarm = alarm;
        this.text = text;
    }

    public Task(int id, long timeStamp, boolean alarm, String text, String audioFilePath) {
        this(id, timeStamp, alarm, text);
        this.audioFilePath = audioFilePath;
    }

    public Task(int id, long timeStamp, boolean alarm, String text, String audioFilePath, String imageFilePath) {
        this(id, timeStamp, alarm, text, audioFilePath);
        this.imageFilePath = imageFilePath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAudioFilePath() {
        return audioFilePath;
    }

    public void setAudioFilePath(String audioFilePath) {
        this.audioFilePath = audioFilePath;
    }

    public String getImageFilePath() {
        return imageFilePath;
    }

    public void setImageFilePath(String imageFilePath) {
        this.imageFilePath = imageFilePath;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public boolean isAlarm() {
        return alarm;
    }

    public void setAlarm(boolean alarm) {
        this.alarm = alarm;
    }
}
