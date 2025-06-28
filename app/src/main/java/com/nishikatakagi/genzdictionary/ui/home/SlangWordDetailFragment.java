package com.nishikatakagi.genzdictionary.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.nishikatakagi.genzdictionary.R;
import com.nishikatakagi.genzdictionary.SlangWord;

public class SlangWordDetailFragment extends Fragment {

    private TextView tvDetailWord, tvDetailMeaning, tvDetailExample, tvDetailOrigin;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_slang_word_detail, container, false);

        // Bind views
        tvDetailWord = view.findViewById(R.id.tv_detail_word);
        tvDetailMeaning = view.findViewById(R.id.tv_detail_meaning);
        tvDetailExample = view.findViewById(R.id.tv_detail_example);
        tvDetailOrigin = view.findViewById(R.id.tv_detail_origin);

        // Get SlangWord from arguments
        SlangWord slangWord = null;
        if (getArguments() != null) {
            slangWord = (SlangWord) getArguments().getSerializable("slang_word");
        }

        if (slangWord != null) {
            tvDetailWord.setText(slangWord.getWord());
            tvDetailMeaning.setText("Nghĩa: " + slangWord.getMeaning());
            tvDetailExample.setText("Ví dụ: " + slangWord.getExample());
            tvDetailOrigin.setText("Nguồn gốc: " + slangWord.getOrigin());
        }

        return view;
    }
}