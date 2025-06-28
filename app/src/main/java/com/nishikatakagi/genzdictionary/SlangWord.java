package com.nishikatakagi.genzdictionary;

import java.io.Serializable;

public class SlangWord implements Serializable {
    private String word;
    private String meaning;
    private String example;
    private String origin;

    // Empty constructor for Firestore
    public SlangWord() {}

    public SlangWord(String word, String meaning, String example, String origin) {
        this.word = word;
        this.meaning = meaning;
        this.example = example;
        this.origin = origin;
    }

    // Getters and Setters
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
}