package com.nishikatakagi.genzdictionary;

import com.google.firebase.Timestamp;

import java.io.Serializable;

public class SlangWord implements Serializable {
    private String slangWordId;
    private String word;
    private String meaning;
    private String example;
    private String origin;
    private String date;
    private Timestamp createdAt;
    private String createdBy;

    public SlangWord() {}

    public SlangWord(String slangWordId, String word, String meaning, String example, String origin, String date, Timestamp createdAt, String createdBy) {
        this.slangWordId = slangWordId;
        this.word = word;
        this.meaning = meaning;
        this.example = example;
        this.origin = origin;
        this.date = date;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public String getSlangWordId() {
        return slangWordId;
    }

    public void setSlangWordId(String slangWordId) {
        this.slangWordId = slangWordId;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}