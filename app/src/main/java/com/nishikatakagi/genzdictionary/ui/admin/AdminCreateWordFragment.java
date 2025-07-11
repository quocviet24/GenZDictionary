package com.nishikatakagi.genzdictionary.ui.admin;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;

import com.nishikatakagi.genzdictionary.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AdminCreateWordFragment extends Fragment {

    private TextInputEditText etWord, etMeaning, etOrigin, etExample;
    private Spinner spinnerCategory;
    private MaterialButton btnSubmit;
    private FirebaseFirestore firestore;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firestore = FirebaseFirestore.getInstance();
        sharedPreferences = requireActivity().getSharedPreferences("loginPrefs", requireActivity().MODE_PRIVATE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_create_word, container, false);

        // Bind views
        etWord = view.findViewById(R.id.et_word);
        spinnerCategory = view.findViewById(R.id.spinner_category);
        etMeaning = view.findViewById(R.id.et_meaning);
        etOrigin = view.findViewById(R.id.et_origin);
        etExample = view.findViewById(R.id.et_example);
        btnSubmit = view.findViewById(R.id.btn_submit);

        // Set up submit button click listener
        btnSubmit.setOnClickListener(v -> createNewWord());

        return view;
    }

    private void createNewWord() {
        String word = etWord.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem() != null ? spinnerCategory.getSelectedItem().toString() : "";
        String meaning = etMeaning.getText().toString().trim();
        String origin = etOrigin.getText().toString().trim();
        String example = etExample.getText().toString().trim();

        // Validate input
        if (word.isEmpty() || category.isEmpty() || meaning.isEmpty() || origin.isEmpty() || example.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current timestamp and format date
        Timestamp createdAt = Timestamp.now();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = dateFormat.format(new Date());

        // Get username from SharedPreferences
        String createdBy = sharedPreferences.getString("username", "admin");

        // Generate random UUID for slangWord
        String slangWordId = UUID.randomUUID().toString();

        // Prepare data for Firestore
        Map<String, Object> wordData = new HashMap<>();
        wordData.put("word", word);
        wordData.put("category", category);
        wordData.put("meaning", meaning);
        wordData.put("origin", origin);
        wordData.put("example", example);
        wordData.put("createdAt", createdAt);
        wordData.put("createdBy", createdBy);
        wordData.put("slangWordId", slangWordId);
        wordData.put("date", date);
        wordData.put("status", "active");

        // Save to Firestore
        firestore.collection("slang_words")
                .document(slangWordId)
                .set(wordData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Tạo từ mới thành công", Toast.LENGTH_SHORT).show();
                    // Clear input fields
                    etWord.setText("");
                    spinnerCategory.setSelection(0);
                    etMeaning.setText("");
                    etOrigin.setText("");
                    etExample.setText("");
                    // Navigate back to dashboard
                    NavController navController = Navigation.findNavController(requireView());
                    navController.popBackStack(R.id.dashboard, false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi khi tạo từ mới: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}