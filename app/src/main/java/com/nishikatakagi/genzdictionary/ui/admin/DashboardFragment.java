package com.nishikatakagi.genzdictionary.ui.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.nishikatakagi.genzdictionary.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private static final String TAG = "DashboardFragment";
    private FirebaseFirestore firestore;
    private TextView tvTotalUsers, tvUsersByMonth, tvTotalSlangWords;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Initialize views
        tvTotalUsers = view.findViewById(R.id.tv_total_users);
        tvUsersByMonth = view.findViewById(R.id.tv_users_by_month);
        tvTotalSlangWords = view.findViewById(R.id.tv_total_slang_words);

        // Fetch statistics
        fetchStatistics();

        return view;
    }

    private void fetchStatistics() {
        // Count total users
        firestore.collection("accounts")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int totalUsers = querySnapshot.size();
                    tvTotalUsers.setText("Tổng số người dùng: " + totalUsers);
                    Log.d(TAG, "Total users: " + totalUsers);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching total users", e);
                    Toast.makeText(getContext(), "Lỗi khi tải số lượng người dùng", Toast.LENGTH_SHORT).show();
                });

        // Count users by month (current month)
        Calendar calendar = Calendar.getInstance();
        String currentMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(calendar.getTime());
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        com.google.firebase.Timestamp startOfMonth = new com.google.firebase.Timestamp(calendar.getTime());
        calendar.add(Calendar.MONTH, 1);
        com.google.firebase.Timestamp endOfMonth = new com.google.firebase.Timestamp(calendar.getTime());

        firestore.collection("accounts")
                .whereGreaterThanOrEqualTo("createdAt", startOfMonth)
                .whereLessThan("createdAt", endOfMonth)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int usersByMonth = querySnapshot.size();
                    tvUsersByMonth.setText("Người dùng trong tháng " + currentMonth + ": " + usersByMonth);
                    Log.d(TAG, "Users in " + currentMonth + ": " + usersByMonth);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching users by month", e);
                    Toast.makeText(getContext(), "Lỗi khi tải số lượng người dùng theo tháng", Toast.LENGTH_SHORT).show();
                });

        // Count total slang words
        firestore.collection("slang_words")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int totalSlangWords = querySnapshot.size();
                    tvTotalSlangWords.setText("Tổng số từ lóng: " + totalSlangWords);
                    Log.d(TAG, "Total slang words: " + totalSlangWords);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching total slang words", e);
                    Toast.makeText(getContext(), "Lỗi khi tải số lượng từ lóng", Toast.LENGTH_SHORT).show();
                });
    }
}