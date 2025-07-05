package com.nishikatakagi.genzdictionary.ui.admin;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
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

public class GarbageFragment extends Fragment {

    private RecyclerView rvGarbageWords;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private SlangWordAdapter adapter;
    private List<SlangWord> slangWordList;
    private FirebaseFirestore firestore;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firestore = FirebaseFirestore.getInstance();
        sharedPreferences = requireActivity().getSharedPreferences("loginPrefs", requireActivity().MODE_PRIVATE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_garbage, container, false);

        // Bind views
        rvGarbageWords = view.findViewById(R.id.rv_garbage_words);
        progressBar = view.findViewById(R.id.progress_bar);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);

        // Check if user is admin
        boolean isAdmin = sharedPreferences.getBoolean("isAdmin", false);
        if (!isAdmin) {
            Snackbar.make(view, "Chỉ admin mới có quyền truy cập thùng rác", Snackbar.LENGTH_LONG).show();
            NavController navController = Navigation.findNavController(view);
            navController.popBackStack();
            return view;
        }

        // Initialize RecyclerView
        slangWordList = new ArrayList<>();
        adapter = new SlangWordAdapter(requireContext(), slangWordList);
        rvGarbageWords.setLayoutManager(new LinearLayoutManager(getContext()));
        rvGarbageWords.setAdapter(adapter);

        // Fetch deactivated slang words
        fetchDeactivatedWords();

        return view;
    }

    private void fetchDeactivatedWords() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);
        Log.d("GarbageFragment", "Bắt đầu truy vấn Firestore cho slang_words với status = deactive");

        Query query = firestore.collection("slang_words")
                .whereEqualTo("status", "deactive")
                .orderBy("createdAt", Query.Direction.DESCENDING);

        query.get().addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                slangWordList.clear();
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                    Log.d("GarbageFragment", "Tải được " + querySnapshot.size() + " từ lóng với status = deactive");
                    for (var doc : querySnapshot) {
                        try {
                            SlangWord slangWord = doc.toObject(SlangWord.class);
                            if (slangWord.getSlangWordId() == null) {
                                slangWord.setSlangWordId(doc.getId());
                            }
                            Log.d("GarbageFragment", "Word: " + slangWord.getWord() + ", Status: " + slangWord.getStatus());
                            slangWordList.add(slangWord);
                        } catch (Exception e) {
                            Log.e("GarbageFragment", "Lỗi khi ánh xạ document " + doc.getId() + ": " + e.getMessage(), e);
                        }
                    }
                    adapter.updateData(slangWordList);
                } else {
                    Log.d("GarbageFragment", "Không tìm thấy từ lóng nào trong thùng rác");
                    tvEmptyState.setVisibility(View.VISIBLE);
                    tvEmptyState.setText("Không có từ lóng nào trong thùng rác");
                }
            } else {
                Log.e("GarbageFragment", "Lỗi tải dữ liệu từ Firestore: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                tvEmptyState.setVisibility(View.VISIBLE);
                tvEmptyState.setText("Lỗi khi tải dữ liệu từ thùng rác");
                Snackbar.make(requireView(), "Lỗi khi tải dữ liệu từ thùng rác: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"), Snackbar.LENGTH_LONG).show();
            }
        });
    }
}