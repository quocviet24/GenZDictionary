package com.nishikatakagi.genzdictionary.ui.home;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.nishikatakagi.genzdictionary.R;
import com.nishikatakagi.genzdictionary.SlangWordAdapter;
import com.nishikatakagi.genzdictionary.models.SlangWord;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvSlangWords;
    private EditText etSearch;
    private Spinner spinnerSort;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private SlangWordAdapter adapter;
    private List<SlangWord> slangWordList;
    private List<SlangWord> filteredWordList;
    private FirebaseFirestore firestore;
    private String currentQuery = "";
    private boolean sortNewest = true;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Firestore and views
        firestore = FirebaseFirestore.getInstance();
        rvSlangWords = view.findViewById(R.id.rv_slang_words);
        etSearch = view.findViewById(R.id.et_search);
        spinnerSort = view.findViewById(R.id.spinner_sort);
        progressBar = view.findViewById(R.id.progress_bar);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);

        // Initialize RecyclerView
        slangWordList = new ArrayList<>();
        filteredWordList = new ArrayList<>();
        adapter = new SlangWordAdapter(requireContext(), filteredWordList);
        rvSlangWords.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSlangWords.setAdapter(adapter);

        // Setup search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                currentQuery = s.toString().trim().toLowerCase();
                filterAndUpdateList();
            }
        });

        // Setup sort
        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortNewest = position == 0; // 0: Mới nhất, 1: Cũ nhất
                fetchSlangWords();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Fetch slang words
        fetchSlangWords();

        return view;
    }

    private void fetchSlangWords() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);
        Log.d("HomeFragment", "Bắt đầu truy vấn Firestore cho slang_words với status = active");

        Query query = firestore.collection("slang_words")
                .whereEqualTo("status", "active")
                .orderBy("createdAt", sortNewest ? Query.Direction.DESCENDING : Query.Direction.ASCENDING);

        query.get().addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                slangWordList.clear();
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                    Log.d("HomeFragment", "Tải được " + querySnapshot.size() + " từ lóng với status = active");
                    for (var doc : querySnapshot) {
                        try {
                            SlangWord slangWord = doc.toObject(SlangWord.class);
                            if (slangWord.getSlangWordId() == null) {
                                slangWord.setSlangWordId(doc.getId());
                            }
                            Log.d("HomeFragment", "Word: " + slangWord.getWord() + ", Status: " + slangWord.getStatus() + ", ID: " + slangWord.getSlangWordId());
                            slangWordList.add(slangWord);
                        } catch (Exception e) {
                            Log.e("HomeFragment", "Lỗi khi ánh xạ document " + doc.getId() + ": " + e.getMessage(), e);
                        }
                    }
                    filterAndUpdateList();
                } else {
                    Log.d("HomeFragment", "Không tìm thấy tài liệu nào với status = active");
                    tvEmptyState.setVisibility(View.VISIBLE);
                    tvEmptyState.setText("Không tìm thấy từ lóng nào");
                }
            } else {
                Log.e("HomeFragment", "Lỗi tải dữ liệu từ Firestore: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                tvEmptyState.setVisibility(View.VISIBLE);
                tvEmptyState.setText("Lỗi khi tải dữ liệu từ lóng");
                Snackbar.make(requireView(), "Lỗi khi tải dữ liệu từ lóng: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void filterAndUpdateList() {
        filteredWordList.clear();
        if (currentQuery.isEmpty()) {
            filteredWordList.addAll(slangWordList);
        } else {
            for (SlangWord slangWord : slangWordList) {
                if (slangWord.getWord() != null && slangWord.getWord().toLowerCase().contains(currentQuery) ||
                        slangWord.getMeaning() != null && slangWord.getMeaning().toLowerCase().contains(currentQuery)) {
                    filteredWordList.add(slangWord);
                }
            }
        }
        progressBar.setVisibility(View.GONE);
        if (filteredWordList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText(currentQuery.isEmpty() ? "Không tìm thấy từ lóng nào" : "Không tìm thấy kết quả cho \"" + currentQuery + "\"");
        } else {
            tvEmptyState.setVisibility(View.GONE);
        }
        adapter.updateData(filteredWordList);
    }
}