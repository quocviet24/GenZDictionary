package com.nishikatakagi.genzdictionary.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nishikatakagi.genzdictionary.models.SlangWord;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {
    private final MutableLiveData<List<SlangWord>> slangWords = new MutableLiveData<>();
    private final MutableLiveData<Integer> scrollPosition = new MutableLiveData<>();
    private final MutableLiveData<Boolean> initialDataLoaded = new MutableLiveData<>(false);
    private final MutableLiveData<String> selectedCategory = new MutableLiveData<>("Tất cả"); // Default to "Tất cả"

    public void setSlangWords(List<SlangWord> words) {
        slangWords.setValue(words);
    }

    public LiveData<List<SlangWord>> getSlangWords() {
        return slangWords;
    }

    public void setScrollPosition(int position) {
        scrollPosition.setValue(position);
    }

    public LiveData<Integer> getScrollPosition() {
        return scrollPosition;
    }

    public void setInitialDataLoaded(boolean loaded) {
        initialDataLoaded.setValue(loaded);
    }

    public LiveData<Boolean> getInitialDataLoaded() {
        return initialDataLoaded;
    }

    public boolean hasData() {
        return Boolean.TRUE.equals(initialDataLoaded.getValue()) && slangWords.getValue() != null;
    }

    public void setSelectedCategory(String category) {
        selectedCategory.setValue(category);
    }

    public LiveData<String> getSelectedCategory() {
        return selectedCategory;
    }
}