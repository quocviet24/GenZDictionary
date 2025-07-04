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

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.Calendar;
import java.util.Date;
import com.google.firebase.Timestamp;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnGuestLogin;
    private TextView tvGotoRegister, tvMessage;
    private FirebaseFirestore firestore;
    private SharedPreferences sharedPreferences;

    private void createData() {
        firestore = FirebaseFirestore.getInstance();

        addWord("Chằm Zn", "Trầm cảm nhẹ, buồn", "Biến âm từ 'trầm cảm'", "Tao thi rớt, chằm Zn luôn á");
        addWord("Toang", "Hỏng, thất bại", "Game online, mạng xã hội", "Bài kiểm tra này là toang thật rồi");
        addWord("Xỉu up xỉu down", "Choáng váng, sốc", "Biến tấu từ 'xỉu'", "Đẹp trai quá xỉu up xỉu down");
        addWord("Ủa alo?", "Không hiểu chuyện gì đang xảy ra", "Biến tấu điện thoại", "Ủa alo? Mới nói cái gì vậy?");
        addWord("Cà khịa", "Châm chọc, gây sự", "Tiếng Nam Bộ", "M suốt ngày đi cà khịa người khác");
        addWord("GATO", "Ghen ăn tức ở", "Viết tắt tiếng Việt", "Đừng GATO với tình yêu của người ta");
        addWord("Bủh", "Cạn lời, bất lực", "Tiếng biểu cảm troll", "Bủh bủh bủh");
        addWord("Xạo ke", "Nói xạo", "Tiếng miền Nam", "T nó đẹp như nào? Xạo ke quá");
        addWord("Crush", "Người mình thích", "Tiếng Anh", "Crush t mới đăng story cute lắm");
        addWord("Flex", "Khoe mẽ", "Tiếng Anh", "Hắn cứ lên hình là flex hàng hiệu");
        addWord("Mlem", "Dễ thương, ngon", "Tiếng Anh", "Bánh bông lan nhìn mlem mlem ghê");
        addWord("Đỉnh kout", "Tuyệt đỉnh", "'Đỉnh' + cách đọc 'cool'", "Nhạc này đỉnh kout luôn");
        addWord("Tán tỉnh", "Thả thính", "Ngôn ngữ GenZ", "T đang tán tỉnh thính crush");
        addWord("Thả thính", "Cố tình tạo sự chú ý", "Ngôn ngữ GenZ", "Up ảnh đẹp thả thính crush");
        addWord("Chill", "Thư giãn", "Tiếng Anh", "Cuối tuần chill tí");
        addWord("Drama", "Lùm xùm, chuyện rắc rối", "Tiếng Anh", "Drama lớp A chưa dứt");
        addWord("Boom hàng", "Đặt hàng rồi không lấy", "Shipper, thương mại điện tử", "Nó boom đơn 3 lần rồi");
        addWord("Trầm cảm level max", "Rất buồn", "Biến thể từ chằm Zn", "Thi rớt cả kỳ. Trầm cảm level max");
        addWord("Fake", "Giả trân", "Tiếng Anh", "Nó xài đồ fake nha");
        addWord("Giả trân", "Giả tạo, không thật", "Meme TikTok", "Nó cười mà nhìn giả trân ghê");
        addWord("Tấu hài", "Làm trò gây cười", "Biến thể từ sân khấu", "Đang họp mà tấu hài quá thể");
        addWord("Ngơ ngác, ngỡ ngàng, bật ngửa", "Bất ngờ, không hiểu gì", "Viral TikTok", "Hắn nói mà tôi ngơ ngác bật ngửa");
        addWord("Ố dề", "Lố bịch, phô trương", "Meme TikTok", "Đi học mà makeup ố dề quá");
        addWord("Chán như con gián", "Rất chán", "Văn nói", "Ở nhà một mình chán như con gián");
        addWord("Tự nhiên cái", "Không có lý do", "Câu nói vui GenZ", "Tự nhiên cái nhớ người yêu cũ");
        addWord("Trmúa hông?", "Thiệt không?", "Cách nói ngọng tạo trend", "Nó thi đậu? Trmúa hông?");
        addWord("Không hiểu luôn á", "Không hiểu gì cả", "Ngôn ngữ vui vẻ", "C nói gì hong hiểu luôn á");
        addWord("No hope", "Không hy vọng", "Tiếng Anh", "T rớt môn rồi, no hope");
        addWord("Đi trend", "Bắt chước theo trend", "Mạng xã hội", "Tụi nhỏ giờ suốt ngày đi trend TikTok");
        addWord("Cục súc", "Cộc cằn, thô lỗ", "Tiếng Việt", "Ông nội m nói chuyện cực súc ghê");
        addWord("Cắn team", "Gánh cả nhóm", "Game online", "Nó vô là cân team luôn");
        addWord("Khum", "Không", "Viết tắt", "Khum thích m nữa đâu");
        addWord("Hông", "Không", "Tiếng miền Nam", "Hông chịu đâu");
        addWord("Chời ơi", "Trời ơi", "Biến âm miền Nam", "Chời ơi, vui dữ thần");
        addWord("Bị gì dzợ?", "Bị gì vậy?", "Cách nói troll", "T thấy mặt mày lạ lạ, bị gì dzợ?");
    }

    private void addWord(String wordStr, String meaning, String origin, String example) {
        String slangWordId = UUID.randomUUID().toString();

        // Tạo ngày ngẫu nhiên trong năm 2025
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2025);
        calendar.set(Calendar.DAY_OF_YEAR, new Random().nextInt(365) + 1);
        Date randomDate = calendar.getTime();

        // Định dạng ngày
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Map<String, Object> word = new HashMap<>();
        word.put("slangWordId", slangWordId);
        word.put("word", wordStr);
        word.put("meaning", meaning);
        word.put("origin", origin);
        word.put("example", example);
        word.put("createdAt", new Timestamp(randomDate));  // Firestore timestamp
        word.put("date", sdf.format(randomDate));          // String format
        word.put("createdBy", "admin");                    // Mặc định admin tạo
        word.put("status", "active");
        word.put("category", "Từ điển gen Z");

        firestore.collection("slang_words")
                .document(slangWordId)
                .set(word)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(LoginActivity.this, "Đã thêm: " + wordStr, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(LoginActivity.this, "Lỗi khi thêm: " + wordStr, Toast.LENGTH_SHORT).show());
    }

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        firestore = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);

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