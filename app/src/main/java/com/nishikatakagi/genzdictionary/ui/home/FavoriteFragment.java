package com.nishikatakagi.genzdictionary.ui.home;

import android.content.SharedPreferences;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.nishikatakagi.genzdictionary.R;
import com.nishikatakagi.genzdictionary.models.SlangWord;
import com.nishikatakagi.genzdictionary.SlangWordAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;

public class FavoriteFragment extends Fragment {

    private static final String TAG = "FavoriteFragment";
    private RecyclerView rvFavoriteWords;
    private EditText etSearch;
    private Spinner spinnerSort;
    private TextView tvEmptyState;
    private ProgressBar progressBar;
    private SlangWordAdapter adapter;
    private List<SlangWord> favoriteWordList;
    private List<SlangWord> filteredFavoriteWordList;
    private FirebaseFirestore firestore;
    private SharedPreferences sharedPreferences;
    private String currentQuery = "";
    private boolean sortNewest = true;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);

        // Khởi tạo Firestore và SharedPreferences
        firestore = FirebaseFirestore.getInstance();
        sharedPreferences = requireContext().getSharedPreferences("loginPrefs", MODE_PRIVATE);

        // Khởi tạo views
        rvFavoriteWords = view.findViewById(R.id.rv_favorite_words);
        etSearch = view.findViewById(R.id.et_search);
        spinnerSort = view.findViewById(R.id.spinner_sort);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);
        progressBar = view.findViewById(R.id.progress_bar);

        // Khởi tạo RecyclerView
        favoriteWordList = new ArrayList<>();
        filteredFavoriteWordList = new ArrayList<>();
        adapter = new SlangWordAdapter(requireContext(), filteredFavoriteWordList);
        rvFavoriteWords.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFavoriteWords.setAdapter(adapter);

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
                fetchFavoriteWords();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Tìm nạp danh sách yêu thích
        fetchFavoriteWords();

        return view;
    }

    private void fetchFavoriteWords() {
        String userEmail = sharedPreferences.getString("email", null);
        Log.d(TAG, "User email: " + userEmail);
        if (userEmail == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để xem danh sách yêu thích", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText("Vui lòng đăng nhập để xem danh sách yêu thích");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);

        // Lấy danh sách slangWordId từ favorites
        firestore.collection("favorites")
                .whereEqualTo("userEmail", userEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot == null || querySnapshot.isEmpty()) {
                            Log.d(TAG, "Không tìm thấy mục yêu thích nào cho người dùng: " + userEmail);
                            progressBar.setVisibility(View.GONE);
                            tvEmptyState.setVisibility(View.VISIBLE);
                            tvEmptyState.setText("Chưa có từ lóng nào trong danh sách yêu thích");
                            filteredFavoriteWordList.clear();
                            adapter.updateData(filteredFavoriteWordList);
                            return;
                        }

                        Log.d(TAG, "Tìm thấy " + querySnapshot.size() + " mục yêu thích");
                        // Sử dụng Set để loại bỏ slangWordId trùng lặp
                        Set<String> slangWordIds = new HashSet<>();
                        for (var doc : querySnapshot) {
                            String slangWordId = doc.getString("slangWordId");
                            if (slangWordId != null) {
                                slangWordIds.add(slangWordId);
                            } else {
                                Log.e(TAG, "slangWordId là null cho tài liệu yêu thích: " + doc.getId());
                            }
                        }

                        if (slangWordIds.isEmpty()) {
                            Log.d(TAG, "Không có slangWordId hợp lệ trong danh sách yêu thích");
                            progressBar.setVisibility(View.GONE);
                            tvEmptyState.setVisibility(View.VISIBLE);
                            tvEmptyState.setText("Chưa có từ lóng nào trong danh sách yêu thích");
                            filteredFavoriteWordList.clear();
                            adapter.updateData(filteredFavoriteWordList);
                            return;
                        }

                        // Tìm nạp slang_words dựa trên slangWordIds
                        favoriteWordList.clear();
                        int totalDocs = slangWordIds.size();
                        int[] fetchedDocs = {0};

                        for (String slangWordId : slangWordIds) {
                            Log.d(TAG, "Đang tìm nạp từ lóng với ID: " + slangWordId);
                            firestore.collection("slang_words")
                                    .document(slangWordId)
                                    .get()
                                    .addOnSuccessListener(slangDoc -> {
                                        fetchedDocs[0]++;
                                        if (slangDoc.exists()) {
                                            SlangWord slangWord = slangDoc.toObject(SlangWord.class);
                                            if (slangWord != null) {
                                                if (slangWord.getSlangWordId() == null) {
                                                    slangWord.setSlangWordId(slangDoc.getId());
                                                }
                                                // Kiểm tra xem slangWord đã tồn tại trong favoriteWordList chưa
                                                if (!isSlangWordInList(slangWord, favoriteWordList)) {
                                                    favoriteWordList.add(slangWord);
                                                    Log.d(TAG, "Đã thêm từ lóng: " + slangWord.getWord() + " (ID: " + slangWordId + ")");
                                                } else {
                                                    Log.d(TAG, "Bỏ qua từ lóng trùng lặp: " + slangWord.getWord() + " (ID: " + slangWordId + ")");
                                                }
                                            } else {
                                                Log.e(TAG, "Không thể ánh xạ từ lóng cho ID: " + slangWordId);
                                            }
                                        } else {
                                            Log.e(TAG, "Không tìm thấy từ lóng cho ID: " + slangWordId);
                                        }

                                        if (fetchedDocs[0] == totalDocs) {
                                            // Sắp xếp theo createdAt
                                            favoriteWordList.sort((a, b) -> {
                                                if (a.getCreatedAt() == null || b.getCreatedAt() == null) {
                                                    return 0;
                                                }
                                                return sortNewest ?
                                                        b.getCreatedAt().compareTo(a.getCreatedAt()) :
                                                        a.getCreatedAt().compareTo(b.getCreatedAt());
                                            });
                                            filterAndUpdateList();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Lỗi khi tìm nạp từ lóng: " + slangWordId, e);
                                        fetchedDocs[0]++;
                                        if (fetchedDocs[0] == totalDocs) {
                                            favoriteWordList.sort((a, b) -> {
                                                if (a.getCreatedAt() == null || b.getCreatedAt() == null) {
                                                    return 0;
                                                }
                                                return sortNewest ?
                                                        b.getCreatedAt().compareTo(a.getCreatedAt()) :
                                                        a.getCreatedAt().compareTo(b.getCreatedAt());
                                            });
                                            filterAndUpdateList();
                                        }
                                        Toast.makeText(getContext(), "Lỗi khi tải từ lóng", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Log.e(TAG, "Lỗi khi tìm nạp danh sách yêu thích", task.getException());
                        progressBar.setVisibility(View.GONE);
                        tvEmptyState.setVisibility(View.VISIBLE);
                        tvEmptyState.setText("Lỗi khi tải danh sách yêu thích");
                        filteredFavoriteWordList.clear();
                        adapter.updateData(filteredFavoriteWordList);
                        Toast.makeText(getContext(), "Lỗi khi tải danh sách yêu thích", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isSlangWordInList(SlangWord slangWord, List<SlangWord> list) {
        if (slangWord.getSlangWordId() == null) {
            return false;
        }
        for (SlangWord existing : list) {
            if (slangWord.getSlangWordId().equals(existing.getSlangWordId())) {
                return true;
            }
        }
        return false;
    }

    private void filterAndUpdateList() {
        filteredFavoriteWordList.clear();
        if (currentQuery.isEmpty()) {
            filteredFavoriteWordList.addAll(favoriteWordList);
        } else {
            for (SlangWord slangWord : favoriteWordList) {
                if (slangWord.getWord() != null && slangWord.getWord().toLowerCase().contains(currentQuery) ||
                        slangWord.getMeaning() != null && slangWord.getMeaning().toLowerCase().contains(currentQuery)) {
                    if (!isSlangWordInList(slangWord, filteredFavoriteWordList)) {
                        filteredFavoriteWordList.add(slangWord);
                    }
                }
            }
        }
        progressBar.setVisibility(View.GONE);
        if (filteredFavoriteWordList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText(currentQuery.isEmpty() ? "Chưa có từ lóng nào trong danh sách yêu thích" : "Không tìm thấy kết quả cho \"" + currentQuery + "\"");
        } else {
            tvEmptyState.setVisibility(View.GONE);
        }
        adapter.updateData(filteredFavoriteWordList);
    }
}