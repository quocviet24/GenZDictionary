package com.nishikatakagi.genzdictionary.ui.home;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.nishikatakagi.genzdictionary.models.SlangWord;
import com.nishikatakagi.genzdictionary.SlangWordAdapter;

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

        Query query = firestore.collection("slang_words")
                .whereEqualTo("status", "active") // 🔥 Chỉ lấy những từ active
                .orderBy("createdAt", sortNewest ? Query.Direction.DESCENDING : Query.Direction.ASCENDING);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                slangWordList.clear();
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null) {
                    for (var doc : querySnapshot) {
                        SlangWord slangWord = doc.toObject(SlangWord.class);
                        if (slangWord.getSlangWordId() == null) {
                            slangWord.setSlangWordId(doc.getId());
                        }
                        slangWordList.add(slangWord);
                    }
                    filterAndUpdateList();
                } else {
                    progressBar.setVisibility(View.GONE);
                    tvEmptyState.setVisibility(View.VISIBLE);
                    tvEmptyState.setText("Không tìm thấy từ lóng nào");
                }
            } else {
                progressBar.setVisibility(View.GONE);
                tvEmptyState.setVisibility(View.VISIBLE);
                tvEmptyState.setText("Lỗi khi tải dữ liệu từ lóng");
                Snackbar.make(requireView(), "Lỗi khi tải dữ liệu từ lóng", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void filterAndUpdateList() {
        filteredWordList.clear();
        if (currentQuery.isEmpty()) {
            filteredWordList.addAll(slangWordList);
        } else {
            for (SlangWord slangWord : slangWordList) {
                if (slangWord.getWord().toLowerCase().contains(currentQuery) ||
                        slangWord.getMeaning().toLowerCase().contains(currentQuery)) {
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