package com.nishikatakagi.genzdictionary.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nishikatakagi.genzdictionary.R;
import com.nishikatakagi.genzdictionary.models.SlangWord;

import java.util.HashMap;
import java.util.Map;

public class UpdateSlangWordFragment extends Fragment {

    private TextInputEditText etWord, etMeaning, etOrigin, etExample;
    private Spinner spinnerCategory;
    private MaterialButton btnSave;
    private FirebaseFirestore firestore;
    private SlangWord slangWord;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firestore = FirebaseFirestore.getInstance();
        if (getArguments() != null) {
            slangWord = (SlangWord) getArguments().getSerializable("slang_word");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_update_slang_word, container, false);

        // Bind views
        etWord = view.findViewById(R.id.et_word);
        spinnerCategory = view.findViewById(R.id.spinner_category);
        etMeaning = view.findViewById(R.id.et_meaning);
        etOrigin = view.findViewById(R.id.et_origin);
        etExample = view.findViewById(R.id.et_example);
        btnSave = view.findViewById(R.id.btn_save);

        // Populate fields with existing data
        if (slangWord != null) {
            etWord.setText(slangWord.getWord() != null ? slangWord.getWord() : "");
            // Set Spinner selection based on current category
            if (slangWord.getCategory() != null) {
                String[] categories = getResources().getStringArray(R.array.category_options);
                for (int i = 0; i < categories.length; i++) {
                    if (categories[i].equals(slangWord.getCategory())) {
                        spinnerCategory.setSelection(i);
                        break;
                    }
                }
            }
            etMeaning.setText(slangWord.getMeaning() != null ? slangWord.getMeaning() : "");
            etOrigin.setText(slangWord.getOrigin() != null ? slangWord.getOrigin() : "");
            etExample.setText(slangWord.getExample() != null ? slangWord.getExample() : "");
        }

        // Set up save button listener
        btnSave.setOnClickListener(v -> saveUpdatedWord());

        return view;
    }

    private void saveUpdatedWord() {
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

        if (slangWord != null && slangWord.getSlangWordId() != null) {
            // Prepare updated data
            Map<String, Object> updatedData = new HashMap<>();
            updatedData.put("word", word);
            updatedData.put("category", category);
            updatedData.put("meaning", meaning);
            updatedData.put("origin", origin);
            updatedData.put("example", example);

            // Update Firestore
            firestore.collection("slang_words")
                    .document(slangWord.getSlangWordId())
                    .update(updatedData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Cập nhật từ lóng thành công", Toast.LENGTH_SHORT).show();
                        NavController navController = Navigation.findNavController(requireView());
                        navController.popBackStack();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Lỗi khi cập nhật từ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(getContext(), "Không thể cập nhật: Dữ liệu từ lóng không hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }
}