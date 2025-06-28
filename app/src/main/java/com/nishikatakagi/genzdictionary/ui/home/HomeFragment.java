package com.nishikatakagi.genzdictionary.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.nishikatakagi.genzdictionary.R;
import com.nishikatakagi.genzdictionary.SlangWord;
import com.nishikatakagi.genzdictionary.SlangWordAdapter;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvSlangWords;
    private SlangWordAdapter adapter;
    private List<SlangWord> slangWordList;
    private FirebaseFirestore firestore;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Khởi tạo Firestore và RecyclerView
        firestore = FirebaseFirestore.getInstance();
        rvSlangWords = view.findViewById(R.id.rv_slang_words);
        slangWordList = new ArrayList<>();
        adapter = new SlangWordAdapter(requireContext(), slangWordList);
        rvSlangWords.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSlangWords.setAdapter(adapter);

        // Lấy dữ liệu từ Firestore
        fetchSlangWords();

        return view;
    }

    private void fetchSlangWords() {
        firestore.collection("slang_words")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        slangWordList.clear();
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            for (var doc : querySnapshot) {
                                SlangWord slangWord = doc.toObject(SlangWord.class);
                                slangWordList.add(slangWord);
                            }
                            adapter.updateData(slangWordList);
                        }
                    } else {
                        // Hiển thị thông báo lỗi
                        Snackbar.make(requireView(), "Lỗi khi tải dữ liệu từ lóng", Snackbar.LENGTH_LONG).show();
                    }
                });
    }
}