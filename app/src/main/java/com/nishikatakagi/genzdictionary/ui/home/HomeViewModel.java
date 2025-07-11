package com.nishikatakagi.genzdictionary.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nishikatakagi.genzdictionary.models.SlangWord;

import java.util.List;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<List<SlangWord>> _slangWords = new MutableLiveData<>();
    public LiveData<List<SlangWord>> getSlangWords() {
        return _slangWords;
    }

    private final MutableLiveData<Integer> _scrollPosition = new MutableLiveData<>();
    public LiveData<Integer> getScrollPosition() {
        return _scrollPosition;
    }

    // Biến này để kiểm tra xem dữ liệu đã được tải lần đầu chưa
    private boolean isInitialDataLoaded = false;

    public void setSlangWords(List<SlangWord> slangWords) {
        _slangWords.setValue(slangWords);
    }

    public void setScrollPosition(int position) {
        _scrollPosition.setValue(position);
    }

    public boolean isInitialDataLoaded() {
        return isInitialDataLoaded;
    }

    public void setInitialDataLoaded(boolean loaded) {
        this.isInitialDataLoaded = loaded;
    }

    // Phương thức để kiểm tra xem ViewModel đã có dữ liệu chưa
    public boolean hasData() {
        return _slangWords.getValue() != null && !_slangWords.getValue().isEmpty();
    }
}