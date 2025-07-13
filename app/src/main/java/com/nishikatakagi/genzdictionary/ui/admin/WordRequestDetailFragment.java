package com.nishikatakagi.genzdictionary.ui.admin;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nishikatakagi.genzdictionary.R;
import com.nishikatakagi.genzdictionary.models.WordRequest;
import com.nishikatakagi.genzdictionary.utils.EmailSender;
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class WordRequestDetailFragment extends Fragment {

    private TextView tvWord, tvCategory, tvMeaning, tvOrigin, tvExample, tvCreatedBy, tvCreatedAt;
    private MaterialButton btnAccept, btnReject;
    private FirebaseFirestore firestore;
    private WordRequest wordRequest;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firestore = FirebaseFirestore.getInstance();
        if (getArguments() != null) {
            wordRequest = (WordRequest) getArguments().getSerializable("wordRequest");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_word_request_detail, container, false);

        // Bind views
        tvWord = view.findViewById(R.id.tv_word);
        tvCategory = view.findViewById(R.id.tv_category);
        tvMeaning = view.findViewById(R.id.tv_meaning);
        tvOrigin = view.findViewById(R.id.tv_origin);
        tvExample = view.findViewById(R.id.tv_example);
        tvCreatedBy = view.findViewById(R.id.tv_created_by);
        tvCreatedAt = view.findViewById(R.id.tv_created_at);
        btnAccept = view.findViewById(R.id.btn_accept);
        btnReject = view.findViewById(R.id.btn_reject);

        // Populate data
        if (wordRequest != null) {
            tvWord.setText("Từ: " + wordRequest.getWord());
            tvCategory.setText("Danh mục: " + wordRequest.getCategory());
            tvMeaning.setText("Ý nghĩa: " + wordRequest.getMeaning());
            tvOrigin.setText("Nguồn gốc: " + wordRequest.getOrigin());
            tvExample.setText("Ví dụ: " + wordRequest.getExample());
            tvCreatedBy.setText("Tạo bởi: " + wordRequest.getCreatedBy());
            Timestamp createdAt = wordRequest.getCreatedAt();
            if (createdAt != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                tvCreatedAt.setText("Tạo lúc: " + sdf.format(createdAt.toDate()));
            }
        }

        // Set up button listeners
        btnAccept.setOnClickListener(v -> updateWordRequestStatus("active"));
        btnReject.setOnClickListener(v -> updateWordRequestStatus("deactive"));

        return view;
    }

    private void updateWordRequestStatus(String status) {
        if (wordRequest != null) {
            firestore.collection("slang_words")
                    .document(wordRequest.getSlangWordId())
                    .update("status", status)
                    .addOnSuccessListener(aVoid -> {
                        // Kiểm tra Context trước khi hiển thị Toast
                        Context context = getContext();
                        if (context != null) {
                            Toast.makeText(context, "Cập nhật trạng thái thành công: " + status, Toast.LENGTH_SHORT).show();
                        }
                        // Send email notification
                        String emailSubject = status.equals("active") ?
                                "Từ của bạn đã được chấp nhận" :
                                "Từ của bạn không được duyệt";
                        String emailBody = status.equals("active") ?
                                "Chúc mừng! Từ \"" + wordRequest.getWord() + "\" của bạn đã được chấp nhận vào GenZ Dictionary." :
                                "Rất tiếc, từ \"" + wordRequest.getWord() + "\" của bạn không được duyệt do không phù hợp với tiêu chí của GenZ Dictionary.";
                        EmailSender.sendEmail(wordRequest.getCreatedBy(), emailSubject, emailBody, new EmailSender.EmailCallback() {
                            @Override
                            public void onEmailSent() {
                                // Kiểm tra Context trước khi hiển thị Toast
                                Context context = getContext();
                                if (context != null) {
                                    Toast.makeText(context, "Đã gửi email thông báo đến " + wordRequest.getCreatedBy(), Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onEmailFailed(String error) {
                                // Kiểm tra Context trước khi hiển thị Toast
                                Context context = getContext();
                                if (context != null) {
                                    Toast.makeText(context, "Lỗi gửi email: " + error, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                        // Navigate back to the request list
                        NavController navController = Navigation.findNavController(requireView());
                        navController.popBackStack();
                    })
                    .addOnFailureListener(e -> {
                        // Kiểm tra Context trước khi hiển thị Toast
                        Context context = getContext();
                        if (context != null) {
                            Toast.makeText(context, "Lỗi khi cập nhật trạng thái: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}