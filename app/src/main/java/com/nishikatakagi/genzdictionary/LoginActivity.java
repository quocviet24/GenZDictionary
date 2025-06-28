package com.nishikatakagi.genzdictionary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
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
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnGuestLogin;
    private TextView tvGotoRegister, tvMessage;
    private FirebaseFirestore firestore;
    private SharedPreferences sharedPreferences;

    private void onBindingView() {
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvGotoRegister = findViewById(R.id.tv_goto_register);
        tvMessage = findViewById(R.id.tv_message);
        btnGuestLogin = findViewById(R.id.btn_guest_login);
    }

    private void onBindingAction() {
        btnLogin.setOnClickListener(this::onBtnLoginClick);
        tvGotoRegister.setOnClickListener(this::onTvGotoRegisterClick);
        btnGuestLogin.setOnClickListener(this::onBtnGuestLoginClick);
    }
    private void onBtnGuestLoginClick(View view) {
        // Đăng nhập với tư cách khách, không lưu thông tin đăng nhập
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.remove("email");
        editor.remove("username");
        editor.apply();
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void onBtnLoginClick(View view) {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Kiểm tra xem các trường có rỗng không
        if (email.isEmpty() || password.isEmpty()) {
            tvMessage.setVisibility(View.VISIBLE);
            tvMessage.setText("Vui lòng nhập đầy đủ email và mật khẩu");
            tvMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            return;
        }

        // Truy vấn Firestore để kiểm tra thông tin đăng nhập
        firestore.collection("accounts")
                .whereEqualTo("email", email)
                .whereEqualTo("password", password)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            // Đăng nhập thành công, lưu thông tin vào SharedPreferences
                            String username = querySnapshot.getDocuments().get(0).getString("username");
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("email", email);
                            editor.putString("username", username);
                            editor.putBoolean("isLoggedIn", true);
                            editor.apply();
                            // Đăng nhập thành công, chuyển sang MainActivity
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish(); // Đóng LoginActivity để không quay lại
                        } else {
                            // Đăng nhập thất bại, hiển thị thông báo lỗi
                            tvMessage.setVisibility(View.VISIBLE);
                            tvMessage.setText("Thông tin tài khoản hoặc mật khẩu không đúng, vui lòng thử lại");
                            tvMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        }
                    } else {
                        // Lỗi khi truy vấn Firestore
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        firestore = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);

        // Xử lý WindowInsets để hỗ trợ Edge-to-Edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        onBindingView();
        onBindingAction();

        // Kiểm tra xem có thông báo đăng ký thành công không
        if (getIntent().getBooleanExtra("registration_success", false)) {
            tvMessage.setVisibility(View.VISIBLE);
            tvMessage.setText("Bạn đã đăng ký tài khoản thành công, bây giờ bạn có thể đăng nhập");
            tvMessage.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }
    }
}