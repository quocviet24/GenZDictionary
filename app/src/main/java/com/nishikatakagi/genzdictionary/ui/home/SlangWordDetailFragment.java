package com.nishikatakagi.genzdictionary.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.nishikatakagi.genzdictionary.R;
import com.nishikatakagi.genzdictionary.models.SlangWord;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class SlangWordDetailFragment extends Fragment {

    private TextView tvDetailWord, tvDetailMeaning, tvDetailExample, tvDetailOrigin;
    private TextView tvDetailSlangWordId, tvDetailDate, tvDetailCreatedAt, tvDetailCreatedBy;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_slang_word_detail, container, false);

        // Bind views
        tvDetailWord = view.findViewById(R.id.tv_detail_word);
        tvDetailMeaning = view.findViewById(R.id.tv_detail_meaning);
        tvDetailExample = view.findViewById(R.id.tv_detail_example);
        tvDetailOrigin = view.findViewById(R.id.tv_detail_origin);
        tvDetailSlangWordId = view.findViewById(R.id.tv_detail_slang_word_id);
        tvDetailDate = view.findViewById(R.id.tv_detail_date);
        tvDetailCreatedAt = view.findViewById(R.id.tv_detail_created_at);
        tvDetailCreatedBy = view.findViewById(R.id.tv_detail_created_by);

        // Get SlangWord from arguments
        SlangWord slangWord = null;
        if (getArguments() != null) {
            slangWord = (SlangWord) getArguments().getSerializable("slang_word");
        }

        if (slangWord != null) {
            // Display existing fields
            tvDetailWord.setText(slangWord.getWord() != null ? slangWord.getWord() : "Không có dữ liệu");
            tvDetailMeaning.setText("Nghĩa: " + (slangWord.getMeaning() != null ? slangWord.getMeaning() : "Không có dữ liệu"));
            tvDetailExample.setText("Ví dụ: " + (slangWord.getExample() != null ? slangWord.getExample() : "Không có dữ liệu"));
            tvDetailOrigin.setText("Nguồn gốc: " + (slangWord.getOrigin() != null ? slangWord.getOrigin() : "Không có dữ liệu"));

            // Display new fields
            tvDetailDate.setText("Ngày: " + (slangWord.getDate() != null ? slangWord.getDate() : "Không có dữ liệu"));

            // Format and display createdAt
            if (slangWord.getCreatedAt() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                String formattedDate = sdf.format(slangWord.getCreatedAt().toDate());
                tvDetailCreatedAt.setText("Tạo lúc: " + formattedDate);
            } else {
                tvDetailCreatedAt.setText("Tạo lúc: Không có dữ liệu");
            }

            tvDetailCreatedBy.setText("Tạo bởi: " + (slangWord.getCreatedBy() != null ? slangWord.getCreatedBy() : "Không có dữ liệu"));
        } else {
            // Handle case when slangWord is null
            tvDetailWord.setText("Không có dữ liệu");
            tvDetailMeaning.setText("Nghĩa: Không có dữ liệu");
            tvDetailExample.setText("Ví dụ: Không có dữ liệu");
            tvDetailOrigin.setText("Nguồn gốc: Không có dữ liệu");
            tvDetailSlangWordId.setText("ID: Không có dữ liệu");
            tvDetailDate.setText("Ngày: Không có dữ liệu");
            tvDetailCreatedAt.setText("Tạo lúc: Không có dữ liệu");
            tvDetailCreatedBy.setText("Tạo bởi: Không có dữ liệu");
        }

        return view;
    }
}