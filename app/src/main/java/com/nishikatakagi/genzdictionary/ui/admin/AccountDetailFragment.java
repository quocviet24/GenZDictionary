package com.nishikatakagi.genzdictionary.ui.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.nishikatakagi.genzdictionary.R;
import com.nishikatakagi.genzdictionary.models.Account;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class AccountDetailFragment extends Fragment {

    private TextView tvEmail, tvUsername, tvPassword, tvStatus, tvCreatedAt;
    private Button btnToggleStatus;
    private FirebaseFirestore firestore;
    private Account account;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account_detail, container, false);

        firestore = FirebaseFirestore.getInstance();
        tvEmail = view.findViewById(R.id.tv_account_detail_email);
        tvUsername = view.findViewById(R.id.tv_account_detail_username);
        tvPassword = view.findViewById(R.id.tv_account_detail_password);
        tvStatus = view.findViewById(R.id.tv_account_detail_status);
        tvCreatedAt = view.findViewById(R.id.tv_account_detail_created_at);
        btnToggleStatus = view.findViewById(R.id.btn_toggle_status);

        account = (Account) getArguments().getSerializable("account");

        if (account != null) {
            tvEmail.setText("Email: " + account.getEmail());
            tvUsername.setText("Username: " + account.getUsername());
            tvPassword.setText("Password: " + account.getPassword());
            tvStatus.setText("Status: " + account.getStatus());
            if (account.getCreatedAt() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                tvCreatedAt.setText("Created At: " + sdf.format(account.getCreatedAt().toDate()));
            } else {
                tvCreatedAt.setText("Created At: Không có dữ liệu");
            }
            btnToggleStatus.setText(account.getStatus().equals("active") ? "Tạm ngừng hoạt động" : "Kích hoạt");
        }

        btnToggleStatus.setOnClickListener(v -> toggleAccountStatus());

        return view;
    }

    private void toggleAccountStatus() {
        if (account == null || account.getId() == null) {
            Toast.makeText(getContext(), "Không tìm thấy tài khoản hoặc ID", Toast.LENGTH_SHORT).show();
            return;
        }

        String newStatus = account.getStatus().equals("active") ? "deactive" : "active";
        DocumentReference docRef = firestore.collection("accounts").document(account.getId());
        Log.d("AccountDetail", "Updating document ID: " + account.getId());

        // Check if document exists before updating
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    // Document exists, update it
                    docRef.update("status", newStatus)
                            .addOnSuccessListener(aVoid -> {
                                account.setStatus(newStatus);
                                tvStatus.setText("Status: " + newStatus);
                                btnToggleStatus.setText(newStatus.equals("active") ? "Tạm ngừng hoạt động" : "Kích hoạt");
                                Toast.makeText(getContext(), "Cập nhật trạng thái thành công", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Lỗi khi cập nhật trạng thái: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    // Document does not exist, create it with set() and merge
                    docRef.set(account, SetOptions.merge())
                            .addOnSuccessListener(aVoid -> {
                                account.setStatus(newStatus);
                                tvStatus.setText("Status: " + newStatus);
                                btnToggleStatus.setText(newStatus.equals("active") ? "Tạm ngừng hoạt động" : "Kích hoạt");
                                Toast.makeText(getContext(), "Tạo và cập nhật tài khoản thành công", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Lỗi khi tạo tài khoản: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            } else {
                Toast.makeText(getContext(), "Lỗi khi kiểm tra tài khoản: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}