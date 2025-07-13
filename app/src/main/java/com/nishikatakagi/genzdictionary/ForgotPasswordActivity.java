package com.nishikatakagi.genzdictionary;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nishikatakagi.genzdictionary.utils.EmailSender;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etOtp;
    private TextInputLayout tilEmail, tilOtp;
    private MaterialButton btnSendOtp, btnVerifyOtp, btnResendOtp;
    private TextView tvError, tvBackToLogin;
    private FirebaseFirestore firestore;

    private void onBindingView() {
        etEmail = findViewById(R.id.et_email);
        etOtp = findViewById(R.id.et_otp);
        tilEmail = findViewById(R.id.til_email);
        tilOtp = findViewById(R.id.til_otp);
        btnSendOtp = findViewById(R.id.btn_send_otp);
        btnVerifyOtp = findViewById(R.id.btn_verify_otp);
        btnResendOtp = findViewById(R.id.btn_resend_otp);
        tvError = findViewById(R.id.tv_error);
        tvBackToLogin = findViewById(R.id.tv_back_to_login);
        firestore = FirebaseFirestore.getInstance();
    }

    private void onBindingAction() {
        btnSendOtp.setOnClickListener(this::onSendOtpClick);
        btnVerifyOtp.setOnClickListener(this::onVerifyOtpClick);
        btnResendOtp.setOnClickListener(this::onResendOtpClick);
        tvBackToLogin.setOnClickListener(this::onBackToLoginClick);
    }

    private void onSendOtpClick(View view) {
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            tvError.setVisibility(View.VISIBLE);
            tvError.setText("Vui lòng nhập email");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tvError.setVisibility(View.VISIBLE);
            tvError.setText("Email không đúng định dạng");
            return;
        }

        // Check if email exists in accounts
        firestore.collection("accounts")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        tvError.setVisibility(View.VISIBLE);
                        tvError.setText("Email không tồn tại");
                    } else {
                        // Generate OTP
                        String otp = generateOtp();
                        Timestamp createdAt = Timestamp.now();

                        // Store OTP in Firestore
                        Map<String, Object> otpData = new HashMap<>();
                        otpData.put("email", email);
                        otpData.put("otp", otp);
                        otpData.put("createdAt", createdAt);

                        firestore.collection("forgot_password")
                                .document(email) // Use email as document ID to overwrite previous OTP
                                .set(otpData)
                                .addOnSuccessListener(aVoid -> {
                                    // Send OTP via email
                                    String subject = "Mã OTP để đặt lại mật khẩu";
                                    String body = "Mã OTP của bạn là: " + otp + "\nMã này có hiệu lực trong 2 phút.";
                                    EmailSender.sendEmail(email, subject, body, new EmailSender.EmailCallback() {
                                        @Override
                                        public void onEmailSent() {
                                            tvError.setVisibility(View.GONE);
                                            tilOtp.setVisibility(View.VISIBLE);
                                            btnVerifyOtp.setVisibility(View.VISIBLE);
                                            btnResendOtp.setVisibility(View.VISIBLE);
                                            Toast.makeText(ForgotPasswordActivity.this, "Đã gửi OTP đến " + email, Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onEmailFailed(String error) {
                                            tvError.setVisibility(View.VISIBLE);
                                            tvError.setText("Lỗi gửi OTP: " + error);
                                        }
                                    });
                                })
                                .addOnFailureListener(e -> {
                                    tvError.setVisibility(View.VISIBLE);
                                    tvError.setText("Lỗi khi lưu OTP: " + e.getMessage());
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    tvError.setVisibility(View.VISIBLE);
                    tvError.setText("Lỗi khi kiểm tra email: " + e.getMessage());
                });
    }

    private void onVerifyOtpClick(View view) {
        String email = etEmail.getText().toString().trim();
        String otp = etOtp.getText().toString().trim();

        if (otp.isEmpty()) {
            tvError.setVisibility(View.VISIBLE);
            tvError.setText("Vui lòng nhập OTP");
            return;
        }

        firestore.collection("forgot_password")
                .document(email)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        tvError.setVisibility(View.VISIBLE);
                        tvError.setText("Không tìm thấy OTP cho email này");
                        return;
                    }

                    String storedOtp = documentSnapshot.getString("otp");
                    Timestamp createdAt = documentSnapshot.getTimestamp("createdAt");
                    long currentTime = System.currentTimeMillis();
                    long otpTime = createdAt.toDate().getTime();
                    long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(currentTime - otpTime);

                    if (diffInMinutes > 2) {
                        tvError.setVisibility(View.VISIBLE);
                        tvError.setText("OTP đã hết hạn. Vui lòng yêu cầu OTP mới.");
                        return;
                    }

                    if (!otp.equals(storedOtp)) {
                        tvError.setVisibility(View.VISIBLE);
                        tvError.setText("OTP không đúng");
                        return;
                    }

                    // OTP valid, fetch password
                    firestore.collection("accounts")
                            .whereEqualTo("email", email)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    DocumentSnapshot accountDoc = queryDocumentSnapshots.getDocuments().get(0);
                                    String password = accountDoc.getString("password");

                                    // Send password via email
                                    String subject = "Mật khẩu của bạn";
                                    String body = "Mật khẩu hiện tại của bạn là: " + password + "\nVui lòng đổi mật khẩu sau khi đăng nhập.";
                                    EmailSender.sendEmail(email, subject, body, new EmailSender.EmailCallback() {
                                        @Override
                                        public void onEmailSent() {
                                            Toast.makeText(ForgotPasswordActivity.this, "Mật khẩu đã được gửi đến " + email, Toast.LENGTH_SHORT).show();
                                            // Navigate back to LoginActivity
                                            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }

                                        @Override
                                        public void onEmailFailed(String error) {
                                            tvError.setVisibility(View.VISIBLE);
                                            tvError.setText("Lỗi gửi mật khẩu: " + error);
                                        }
                                    });
                                }
                            })
                            .addOnFailureListener(e -> {
                                tvError.setVisibility(View.VISIBLE);
                                tvError.setText("Lỗi khi lấy mật khẩu: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    tvError.setVisibility(View.VISIBLE);
                    tvError.setText("Lỗi khi xác minh OTP: " + e.getMessage());
                });
    }

    private void onResendOtpClick(View view) {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            tvError.setVisibility(View.VISIBLE);
            tvError.setText("Vui lòng nhập email");
            return;
        }

        firestore.collection("forgot_password")
                .document(email)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Timestamp createdAt = documentSnapshot.getTimestamp("createdAt");
                        long currentTime = System.currentTimeMillis();
                        long otpTime = createdAt.toDate().getTime();
                        long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(currentTime - otpTime);

                        if (diffInMinutes <= 2) {
                            tvError.setVisibility(View.VISIBLE);
                            tvError.setText("Vui lòng đợi " + (2 - diffInMinutes) + " phút để gửi OTP mới");
                            return;
                        }
                    }
                    onSendOtpClick(view); // Reuse send OTP logic
                })
                .addOnFailureListener(e -> {
                    tvError.setVisibility(View.VISIBLE);
                    tvError.setText("Lỗi khi kiểm tra OTP: " + e.getMessage());
                });
    }

    private void onBackToLoginClick(View view) {
        Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private String generateOtp() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            otp.append(characters.charAt(random.nextInt(characters.length())));
        }
        return otp.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.forgot_password_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        onBindingView();
        onBindingAction();
    }
}