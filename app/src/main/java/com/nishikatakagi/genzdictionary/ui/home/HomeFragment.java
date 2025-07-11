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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
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
    private Spinner spinnerCategoryFilter;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private SlangWordAdapter adapter;
    private List<SlangWord> slangWordList;
    private List<SlangWord> filteredWordList;
    private FirebaseFirestore firestore;
    private String currentQuery = "";
    private boolean sortNewest = true;
    private String selectedCategory = "Tất cả";
    private LinearLayoutManager layoutManager;
    private HomeViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Firestore and views
        firestore = FirebaseFirestore.getInstance();
        rvSlangWords = view.findViewById(R.id.rv_slang_words);
        etSearch = view.findViewById(R.id.et_search);
        spinnerSort = view.findViewById(R.id.spinner_sort);
        spinnerCategoryFilter = view.findViewById(R.id.spinner_category_filter);
        progressBar = view.findViewById(R.id.progress_bar);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);

        // Initialize RecyclerView
        slangWordList = new ArrayList<>();
        filteredWordList = new ArrayList<>();
        adapter = new SlangWordAdapter(requireContext(), filteredWordList);
        adapter.setOnItemClickListener((slangWord, itemView) -> {
            int scrollPosition = layoutManager.findFirstVisibleItemPosition();
            viewModel.setScrollPosition(scrollPosition);
            viewModel.setSlangWords(new ArrayList<>(slangWordList));

            Bundle bundle = new Bundle();
            bundle.putSerializable("slang_word", slangWord);
            NavController navController = Navigation.findNavController(itemView);
            navController.navigate(R.id.action_nav_home_to_slang_word_detail_fragment, bundle);
        });
        layoutManager = new LinearLayoutManager(getContext());
        rvSlangWords.setLayoutManager(layoutManager);
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
                sortNewest = position == 0;
                viewModel.setInitialDataLoaded(false);
                fetchSlangWords();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Setup category filter
        spinnerCategoryFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] categories = getResources().getStringArray(R.array.category_filter_options);
                selectedCategory = categories[position];
                viewModel.setSelectedCategory(selectedCategory);
                viewModel.setInitialDataLoaded(false);
                fetchSlangWords();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Restore state from ViewModel
        if (viewModel.hasData()) {
            Log.d("HomeFragment", "Loading data from ViewModel");
            slangWordList = viewModel.getSlangWords().getValue();
            if (slangWordList == null) {
                slangWordList = new ArrayList<>();
            }
            filteredWordList.clear();
            filteredWordList.addAll(slangWordList);
            adapter.updateData(filteredWordList);

            Integer savedScrollPosition = viewModel.getScrollPosition().getValue();
            if (savedScrollPosition != null) {
                layoutManager.scrollToPositionWithOffset(savedScrollPosition, 0);
                Log.d("HomeFragment", "Restored scroll position: " + savedScrollPosition);
            }

            String savedCategory = viewModel.getSelectedCategory().getValue();
            if (savedCategory != null) {
                selectedCategory = savedCategory;
                String[] categories = getResources().getStringArray(R.array.category_filter_options);
                for (int i = 0; i < categories.length; i++) {
                    if (categories[i].equals(savedCategory)) {
                        spinnerCategoryFilter.setSelection(i);
                        break;
                    }
                }
            }
        } else {
            Log.d("HomeFragment", "No data in ViewModel, fetching from Firestore");
            fetchSlangWords();
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        filterAndUpdateList();
    }

    private void fetchSlangWords() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);
        Log.d("HomeFragment", "Bắt đầu truy vấn Firestore cho slang_words với status = active và category = " + selectedCategory);

        Query query = firestore.collection("slang_words")
                .whereEqualTo("status", "active");

        if (!selectedCategory.equals("Tất cả")) {
            query = query.whereEqualTo("category", selectedCategory);
        }

        query = query.orderBy("createdAt", sortNewest ? Query.Direction.DESCENDING : Query.Direction.ASCENDING);

        query.get().addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                slangWordList.clear();
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                    for (var doc : querySnapshot) {
                        try {
                            SlangWord slangWord = doc.toObject(SlangWord.class);
                            if (slangWord.getSlangWordId() == null) {
                                slangWord.setSlangWordId(doc.getId());
                            }
                            Log.d("HomeFragment", "Word: " + slangWord.getWord() + ", Status: " + slangWord.getStatus() + ", Category: " + slangWord.getCategory() + ", ID: " + slangWord.getSlangWordId());
                            slangWordList.add(slangWord);
                        } catch (Exception e) {
                            Log.e("HomeFragment", "Lỗi khi ánh xạ document " + doc.getId() + ": " + e.getMessage(), e);
                        }
                    }
                    viewModel.setSlangWords(new ArrayList<>(slangWordList));
                    filterAndUpdateList();
                    viewModel.setInitialDataLoaded(true);
                } else {
                    tvEmptyState.setVisibility(View.VISIBLE);
                    tvEmptyState.setText("Không tìm thấy từ lóng nào");
                    viewModel.setSlangWords(new ArrayList<>());
                    filterAndUpdateList();
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