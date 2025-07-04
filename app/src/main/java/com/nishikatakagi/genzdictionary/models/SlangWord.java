package com.nishikatakagi.genzdictionary.models;

import com.google.firebase.Timestamp;
import java.io.Serializable;

public class SlangWord implements Serializable {
    private String word;
    private String category;
    private String meaning;
    private String origin;
    private String example;
    private String createdBy;
    private Timestamp createdAt;
    private String slangWordId;
    private String date;
    private String status;

    public SlangWord() {}

    // Getters and setters
    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getMeaning() { return meaning; }
    public void setMeaning(String meaning) { this.meaning = meaning; }
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    public String getExample() { return example; }
    public void setExample(String example) { this.example = example; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public String getSlangWordId() { return slangWordId; }
    public void setSlangWordId(String slangWordId) { this.slangWordId = slangWordId; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}