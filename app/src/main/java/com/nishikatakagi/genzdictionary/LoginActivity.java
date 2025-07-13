package com.nishikatakagi.genzdictionary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnGuestLogin;
    private TextView tvGotoRegister, tvMessage, tvForgotPassword;
    private FirebaseFirestore firestore;
    private SharedPreferences sharedPreferences;

    private void onBindingView() {
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnGuestLogin = findViewById(R.id.btn_guest_login);
        tvGotoRegister = findViewById(R.id.tv_goto_register);
        tvMessage = findViewById(R.id.tv_message);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
    }

    private void onBindingAction() {
        btnLogin.setOnClickListener(this::onBtnLoginClick);
        btnGuestLogin.setOnClickListener(this::onBtnGuestLoginClick);
        tvGotoRegister.setOnClickListener(this::onTvGotoRegisterClick);
        tvForgotPassword.setOnClickListener(this::onTvForgotPasswordClick);
    }

    private void onBtnGuestLoginClick(View view) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.remove("email");
        editor.remove("username");
        editor.remove("isAdmin");
        editor.apply();
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void onBtnLoginClick(View view) {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            tvMessage.setVisibility(View.VISIBLE);
            tvMessage.setText("Vui lòng nhập đầy đủ email và mật khẩu");
            tvMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            return;
        }

        firestore.collection("accounts")
                .whereEqualTo("email", email)
                .whereEqualTo("password", password)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            String status = querySnapshot.getDocuments().get(0).getString("status");
                            if ("active".equals(status)) {
                                String username = querySnapshot.getDocuments().get(0).getString("username");
                                Boolean isAdmin = querySnapshot.getDocuments().get(0).contains("role") &&
                                        "admin".equals(querySnapshot.getDocuments().get(0).getString("role"));
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("email", email);
                                editor.putString("username", username);
                                editor.putBoolean("isLoggedIn", true);
                                editor.putBoolean("isAdmin", isAdmin);
                                editor.apply();

                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                tvMessage.setVisibility(View.VISIBLE);
                                tvMessage.setText("Tài khoản của bạn hiện tại đang bị khóa");
                                tvMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                            }
                        } else {
                            tvMessage.setVisibility(View.VISIBLE);
                            tvMessage.setText("Thông tin tài khoản hoặc mật khẩu không đúng, vui lòng thử lại");
                            tvMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        }
                    } else {
                        tvMessage.setVisibility(View.VISIBLE);
                        tvMessage.setText("Đã xảy ra lỗi, vui lòng thử lại sau");
                        tvMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    }
                });
    }

    private void onTvGotoRegisterClick(View view) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    private void onTvForgotPasswordClick(View view) {
        Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        firestore = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);

        // Kiểm tra trạng thái đăng nhập
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        if (isLoggedIn) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        onBindingView();
        onBindingAction();

        if (getIntent().getBooleanExtra("registration_success", false)) {
            tvMessage.setVisibility(View.VISIBLE);
            tvMessage.setText("Bạn đã đăng ký tài khoản thành công, bây giờ bạn có thể đăng nhập");
            tvMessage.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }
    }
}