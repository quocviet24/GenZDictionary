package com.nishikatakagi.genzdictionary;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etEmail, etPassword, etConfirmPassword;
    private CheckBox cbTerms;
    private MaterialButton btnRegister;
    private TextView tvError, tvGotoLogin;
    private ImageView ivBack;
    private FirebaseFirestore firestore;

    private void onBindingView() {
        etFullName = findViewById(R.id.et_full_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        cbTerms = findViewById(R.id.cb_terms);
        btnRegister = findViewById(R.id.btn_register);
        tvError = findViewById(R.id.tv_error);
        tvGotoLogin = findViewById(R.id.tv_goto_login);
        ivBack = findViewById(R.id.iv_back);
    }

    private void onBindingAction() {
        btnRegister.setOnClickListener(this::onBtnRegisterClick);
        tvGotoLogin.setOnClickListener(this::onTvGotoLoginClick);
        ivBack.setOnClickListener(this::onIvBackClick);
    }

    private void onBtnRegisterClick(View view) {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Kiểm tra dữ liệu đầu vào
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            tvError.setVisibility(View.VISIBLE);
            tvError.setText("Vui lòng nhập đầy đủ các trường");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tvError.setVisibility(View.VISIBLE);
            tvError.setText("Email không đúng định dạng");
            return;
        }

        if (password.length() < 6) {
            tvError.setVisibility(View.VISIBLE);
            tvError.setText("Mật khẩu phải có ít nhất 6 ký tự");
            return;
        }

        if (!password.equals(confirmPassword)) {
            tvError.setVisibility(View.VISIBLE);
            tvError.setText("Mật khẩu xác nhận không khớp");
            return;
        }

        if (!cbTerms.isChecked()) {
            tvError.setVisibility(View.VISIBLE);
            tvError.setText("Vui lòng đồng ý với điều khoản sử dụng");
            return;
        }

        // Lưu tài khoản vào Firestore
        Map<String, Object> account = new HashMap<>();
        account.put("username", fullName);
        account.put("email", email);
        account.put("password", password);

        firestore.collection("accounts")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        tvError.setVisibility(View.VISIBLE);
                        tvError.setText("Email đã được sử dụng");
                    } else {
                        firestore.collection("accounts")
                                .add(account)
                                .addOnSuccessListener(documentReference -> {
                                    // Đăng ký thành công, chuyển sang LoginActivity với thông báo
                                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                    intent.putExtra("registration_success", true);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    tvError.setVisibility(View.VISIBLE);
                                    tvError.setText("Đã xảy ra lỗi, vui lòng thử lại sau");
                                });
                    }
                });
    }

    private void onTvGotoLoginClick(View view) {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void onIvBackClick(View view) {
        onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        // Khởi tạo Firestore
        firestore = FirebaseFirestore.getInstance();

        // Xử lý WindowInsets để hỗ trợ Edge-to-Edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        onBindingView();
        onBindingAction();
    }
}