package com.nishikatakagi.genzdictionary.ui.home;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nishikatakagi.genzdictionary.R;
import com.nishikatakagi.genzdictionary.models.SlangWord;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class SlangWordDetailFragment extends Fragment {

    private TextView tvDetailWord, tvDetailMeaning, tvDetailExample, tvDetailOrigin;
    private TextView tvDetailSlangWordId, tvDetailDate, tvDetailCreatedAt, tvDetailCreatedBy;
    private MaterialButton btnUpdate, btnDeactivate, btnReactivate, btnDelete;
    private LinearLayout adminButtonsContainer;
    private FirebaseFirestore firestore;
    private SlangWord slangWord;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firestore = FirebaseFirestore.getInstance();
        sharedPreferences = requireActivity().getSharedPreferences("loginPrefs", requireActivity().MODE_PRIVATE);
        if (getArguments() != null) {
            slangWord = (SlangWord) getArguments().getSerializable("slang_word");
        }
    }

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
        adminButtonsContainer = view.findViewById(R.id.admin_buttons_container);
        btnUpdate = view.findViewById(R.id.btn_update);
        btnDeactivate = view.findViewById(R.id.btn_deactivate);
        btnReactivate = view.findViewById(R.id.btn_reactivate);
        btnDelete = view.findViewById(R.id.btn_delete);

        // Check if user is admin
        boolean isAdmin = sharedPreferences.getBoolean("isAdmin", false);
        if (isAdmin && slangWord != null) {
            adminButtonsContainer.setVisibility(View.VISIBLE);
            if ("deactive".equals(slangWord.getStatus())) {
                btnUpdate.setVisibility(View.GONE);
                btnDeactivate.setVisibility(View.GONE);
                btnReactivate.setVisibility(View.VISIBLE);
                btnDelete.setVisibility(View.VISIBLE);
            } else {
                btnUpdate.setVisibility(View.VISIBLE);
                btnDeactivate.setVisibility(View.VISIBLE);
                btnReactivate.setVisibility(View.GONE);
                btnDelete.setVisibility(View.GONE);
            }
        } else {
            adminButtonsContainer.setVisibility(View.GONE);
        }

        // Populate data
        if (slangWord != null) {
            tvDetailWord.setText(slangWord.getWord() != null ? slangWord.getWord() : "Không có dữ liệu");
            tvDetailMeaning.setText("Nghĩa: " + (slangWord.getMeaning() != null ? slangWord.getMeaning() : "Không có dữ liệu"));
            tvDetailExample.setText("Ví dụ: " + (slangWord.getExample() != null ? slangWord.getExample() : "Không có dữ liệu"));
            tvDetailOrigin.setText("Nguồn gốc: " + (slangWord.getOrigin() != null ? slangWord.getOrigin() : "Không có dữ liệu"));
            tvDetailSlangWordId.setText("ID: " + (slangWord.getSlangWordId() != null ? slangWord.getSlangWordId() : "Không có dữ liệu"));
            tvDetailDate.setText("Ngày: " + (slangWord.getDate() != null ? slangWord.getDate() : "Không có dữ liệu"));
            tvDetailCreatedBy.setText("Tạo bởi: " + (slangWord.getCreatedBy() != null ? slangWord.getCreatedBy() : "Không có dữ liệu"));

            if (slangWord.getCreatedAt() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                tvDetailCreatedAt.setText("Tạo lúc: " + sdf.format(slangWord.getCreatedAt().toDate()));
            } else {
                tvDetailCreatedAt.setText("Tạo lúc: Không có dữ liệu");
            }
        } else {
            tvDetailWord.setText("Không có dữ liệu");
            tvDetailMeaning.setText("Nghĩa: Không có dữ liệu");
            tvDetailExample.setText("Ví dụ: Không có dữ liệu");
            tvDetailOrigin.setText("Nguồn gốc: Không có dữ liệu");
            tvDetailSlangWordId.setText("ID: Không có dữ liệu");
            tvDetailDate.setText("Ngày: Không có dữ liệu");
            tvDetailCreatedAt.setText("Tạo lúc: Không có dữ liệu");
            tvDetailCreatedBy.setText("Tạo bởi: Không có dữ liệu");
        }

        // Set up button listeners for admins
        if (isAdmin) {
            btnUpdate.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putSerializable("slang_word", slangWord);
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_slang_word_detail_to_update_slang_word, bundle);
            });

            btnDeactivate.setOnClickListener(v -> showDeactivateConfirmationDialog());

            btnReactivate.setOnClickListener(v -> showReactivateConfirmationDialog());

            btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog());
        }

        return view;
    }

    private void showDeactivateConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận Deactivate")
                .setMessage("Bạn có chắc muốn deactive từ này không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    if (slangWord != null && slangWord.getSlangWordId() != null) {
                        firestore.collection("slang_words")
                                .document(slangWord.getSlangWordId())
                                .update("status", "deactive")
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Đã deactive từ thành công", Toast.LENGTH_SHORT).show();
                                    NavController navController = Navigation.findNavController(requireView());
                                    navController.popBackStack();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Lỗi khi deactive từ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void showReactivateConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận Kích hoạt lại")
                .setMessage("Bạn có chắc muốn kích hoạt lại từ này không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    if (slangWord != null && slangWord.getSlangWordId() != null) {
                        firestore.collection("slang_words")
                                .document(slangWord.getSlangWordId())
                                .update("status", "active")
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Đã kích hoạt lại từ thành công", Toast.LENGTH_SHORT).show();
                                    NavController navController = Navigation.findNavController(requireView());
                                    navController.popBackStack();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Lỗi khi kích hoạt lại từ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận Xóa")
                .setMessage("Bạn có chắc muốn xóa hoàn toàn từ này không? Hành động này không thể hoàn tác.")
                .setPositiveButton("Có", (dialog, which) -> {
                    if (slangWord != null && slangWord.getSlangWordId() != null) {
                        firestore.collection("slang_words")
                                .document(slangWord.getSlangWordId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Đã xóa từ thành công", Toast.LENGTH_SHORT).show();
                                    NavController navController = Navigation.findNavController(requireView());
                                    navController.popBackStack();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Lỗi khi xóa từ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }
}