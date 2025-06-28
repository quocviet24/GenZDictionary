package com.nishikatakagi.genzdictionary.ui.home;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.nishikatakagi.genzdictionary.R;
import com.nishikatakagi.genzdictionary.SlangWord;
import com.nishikatakagi.genzdictionary.SlangWordAdapter;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class FavoriteFragment extends Fragment {

    private RecyclerView rvFavoriteWords;
    private TextView tvEmptyState;
    private ProgressBar progressBar;
    private SlangWordAdapter adapter;
    private List<SlangWord> favoriteWordList;
    private FirebaseFirestore firestore;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);

        // Khởi tạo Firestore và SharedPreferences
        firestore = FirebaseFirestore.getInstance();
        sharedPreferences = requireContext().getSharedPreferences("loginPrefs", MODE_PRIVATE);

        // Khởi tạo views
        rvFavoriteWords = view.findViewById(R.id.rv_favorite_words);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);
        progressBar = view.findViewById(R.id.progress_bar);

        // Khởi tạo RecyclerView
        favoriteWordList = new ArrayList<>();
        adapter = new SlangWordAdapter(requireContext(), favoriteWordList);
        rvFavoriteWords.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFavoriteWords.setAdapter(adapter);

        // Tìm nạp danh sách yêu thích
        fetchFavoriteWords();

        return view;
    }

    private void fetchFavoriteWords() {
        String userEmail = sharedPreferences.getString("email", null);
        Log.d("FavoriteFragment", "User email: " + userEmail);
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
                            Log.d("FavoriteFragment", "Không tìm thấy mục yêu thích nào cho người dùng: " + userEmail);
                            progressBar.setVisibility(View.GONE);
                            tvEmptyState.setVisibility(View.VISIBLE);
                            tvEmptyState.setText("Chưa có từ lóng nào trong danh sách yêu thích");
                            return;
                        }

                        Log.d("FavoriteFragment", "Tìm thấy " + querySnapshot.size() + " mục yêu thích");
                        List<String> slangWordIds = new ArrayList<>();
                        for (var doc : querySnapshot) {
                            String slangWordId = doc.getString("slangWordId");
                            if (slangWordId != null) {
                                slangWordIds.add(slangWordId);
                            } else {
                                Log.e("FavoriteFragment", "slangWordId là null cho tài liệu yêu thích: " + doc.getId());
                            }
                        }

                        if (slangWordIds.isEmpty()) {
                            Log.d("FavoriteFragment", "Không có slangWordId hợp lệ trong danh sách yêu thích");
                            progressBar.setVisibility(View.GONE);
                            tvEmptyState.setVisibility(View.VISIBLE);
                            tvEmptyState.setText("Chưa có từ lóng nào trong danh sách yêu thích");
                            return;
                        }

                        // Tìm nạp slang_words dựa trên slangWordIds
                        favoriteWordList.clear();
                        int totalDocs = slangWordIds.size();
                        int[] fetchedDocs = {0};

                        for (String slangWordId : slangWordIds) {
                            Log.d("FavoriteFragment", "Đang tìm nạp từ lóng với ID: " + slangWordId);
                            firestore.collection("slang_words")
                                    .document(slangWordId)
                                    .get()
                                    .addOnSuccessListener(slangDoc -> {
                                        fetchedDocs[0]++;
                                        if (slangDoc.exists()) {
                                            SlangWord slangWord = slangDoc.toObject(SlangWord.class);
                                            if (slangWord != null) {
                                                // Đảm bảo slangWordId được thiết lập
                                                if (slangWord.getSlangWordId() == null) {
                                                    slangWord.setSlangWordId(slangDoc.getId());
                                                }
                                                favoriteWordList.add(slangWord);
                                                Log.d("FavoriteFragment", "Đã thêm từ lóng: " + slangWord.getWord() + " (ID: " + slangWordId + ")");
                                            } else {
                                                Log.e("FavoriteFragment", "Không thể ánh xạ từ lóng cho ID: " + slangWordId);
                                            }
                                        } else {
                                            Log.e("FavoriteFragment", "Không tìm thấy từ lóng cho ID: " + slangWordId);
                                        }

                                        // Cập nhật UI sau khi tất cả tài liệu được tìm nạp
                                        if (fetchedDocs[0] == totalDocs) {
                                            progressBar.setVisibility(View.GONE);
                                            if (favoriteWordList.isEmpty()) {
                                                tvEmptyState.setVisibility(View.VISIBLE);
                                                tvEmptyState.setText("Chưa có từ lóng nào trong danh sách yêu thích");
                                            } else {
                                                tvEmptyState.setVisibility(View.GONE);
                                                adapter.updateData(favoriteWordList);
                                                Log.d("FavoriteFragment", "Đã cập nhật RecyclerView với " + favoriteWordList.size() + " mục");
                                            }
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("FavoriteFragment", "Lỗi khi tìm nạp từ lóng: " + slangWordId, e);
                                        fetchedDocs[0]++;
                                        if (fetchedDocs[0] == totalDocs) {
                                            progressBar.setVisibility(View.GONE);
                                            if (favoriteWordList.isEmpty()) {
                                                tvEmptyState.setVisibility(View.VISIBLE);
                                                tvEmptyState.setText("Chưa có từ lóng nào trong danh sách yêu thích");
                                            } else {
                                                tvEmptyState.setVisibility(View.GONE);
                                                adapter.updateData(favoriteWordList);
                                                Log.d("FavoriteFragment", "Đã cập nhật RecyclerView với " + favoriteWordList.size() + " mục");
                                            }
                                        }
                                        Toast.makeText(getContext(), "Lỗi khi tải từ lóng", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Log.e("FavoriteFragment", "Lỗi khi tìm nạp danh sách yêu thích", task.getException());
                        progressBar.setVisibility(View.GONE);
                        tvEmptyState.setVisibility(View.VISIBLE);
                        tvEmptyState.setText("Lỗi khi tải danh sách yêu thích");
                        Toast.makeText(getContext(), "Lỗi khi tải danh sách yêu thích", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}