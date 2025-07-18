package com.nishikatakagi.genzdictionary;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.nishikatakagi.genzdictionary.models.SlangWord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlangWordAdapter extends RecyclerView.Adapter<SlangWordAdapter.SlangWordViewHolder> {

    private List<SlangWord> slangWordList;
    private Context context;
    private FirebaseFirestore firestore;
    private SharedPreferences sharedPreferences;
    private Map<String, Boolean> favoriteStatus;
    private static final String TAG = "SlangWordAdapter";

    public SlangWordAdapter(Context context, List<SlangWord> slangWordList) {
        this.context = context;
        this.slangWordList = slangWordList;
        this.firestore = FirebaseFirestore.getInstance();
        this.sharedPreferences = context.getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
        this.favoriteStatus = new HashMap<>();
        loadFavoriteStatus();
    }

    public interface OnItemClickListener {
        void onItemClick(SlangWord slangWord, View view);
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public SlangWordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_slang_word, parent, false);
        return new SlangWordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SlangWordViewHolder holder, int position) {
        SlangWord slangWord = slangWordList.get(position);
        holder.tvWord.setText(slangWord.getWord());
        holder.tvMeaning.setText("Nghĩa: " + slangWord.getMeaning());

        // Check favorite status
        String slangWordId = slangWord.getSlangWordId();
        boolean isFavorite = favoriteStatus.getOrDefault(slangWordId, false);
        holder.ivFavorite.setImageResource(isFavorite ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);

        // Handle favorite toggle
        holder.ivFavorite.setOnClickListener(v -> {
            String userEmail = sharedPreferences.getString("email", null);
            if (userEmail == null) {
                Toast.makeText(context, "Vui lòng đăng nhập để thêm vào danh sách yêu thích", Toast.LENGTH_SHORT).show();
                return;
            }
            if (slangWordId == null) {
                Toast.makeText(context, "Lỗi: Không tìm thấy ID từ lóng", Toast.LENGTH_SHORT).show();
                return;
            }

            // Toggle favorite status
            if (favoriteStatus.getOrDefault(slangWordId, false)) {
                // Remove from favorites
                firestore.collection("favorites")
                        .whereEqualTo("userEmail", userEmail)
                        .whereEqualTo("slangWordId", slangWordId)
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            if (!querySnapshot.isEmpty()) {
                                for (var doc : querySnapshot) {
                                    doc.getReference().delete();
                                }
                                favoriteStatus.put(slangWordId, false);
                                holder.ivFavorite.setImageResource(R.drawable.ic_star_outline);
                                Toast.makeText(context, "Đã xóa \"" + slangWord.getWord() + "\" khỏi danh sách yêu thích", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e(TAG, "Không tìm thấy tài liệu yêu thích cho slangWordId: " + slangWordId);
                                Toast.makeText(context, "Lỗi: Không tìm thấy từ trong danh sách yêu thích", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Lỗi khi xóa khỏi danh sách yêu thích: " + e.getMessage());
                            Toast.makeText(context, "Lỗi khi xóa khỏi danh sách yêu thích: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                // Add to favorites
                Map<String, Object> favorite = new HashMap<>();
                favorite.put("userEmail", userEmail);
                favorite.put("slangWordId", slangWordId);
                firestore.collection("favorites")
                        .add(favorite)
                        .addOnSuccessListener(docRef -> {
                            favoriteStatus.put(slangWordId, true);
                            holder.ivFavorite.setImageResource(R.drawable.ic_star_filled);
                            Toast.makeText(context, "Đã thêm \"" + slangWord.getWord() + "\" vào danh sách yêu thích", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Lỗi khi thêm vào danh sách yêu thích: " + e.getMessage());
                            Toast.makeText(context, "Lỗi khi thêm vào danh sách yêu thích: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });

        // Handle item click for details
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(slangWord, v);
            }
        });
    }

    @Override
    public int getItemCount() {
        return slangWordList.size();
    }

    public void updateData(List<SlangWord> newSlangWordList) {
        this.slangWordList = newSlangWordList;
        loadFavoriteStatus();
        notifyDataSetChanged();
    }

    private void loadFavoriteStatus() {
        String userEmail = sharedPreferences.getString("email", null);
        if (userEmail == null) {
            favoriteStatus.clear();
            notifyDataSetChanged();
            return;
        }

        firestore.collection("favorites")
                .whereEqualTo("userEmail", userEmail)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    favoriteStatus.clear();
                    for (var doc : querySnapshot) {
                        String slangWordId = doc.getString("slangWordId");
                        if (slangWordId != null) {
                            favoriteStatus.put(slangWordId, true);
                        }
                    }
                    Log.d(TAG, "Đã tải trạng thái yêu thích: " + favoriteStatus.size() + " mục");
                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi tải trạng thái yêu thích: " + e.getMessage());
                    Toast.makeText(context, "Lỗi khi tải trạng thái yêu thích", Toast.LENGTH_SHORT).show();
                });
    }

    static class SlangWordViewHolder extends RecyclerView.ViewHolder {
        TextView tvWord, tvMeaning;
        ImageView ivFavorite;

        public SlangWordViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWord = itemView.findViewById(R.id.tv_word);
            tvMeaning = itemView.findViewById(R.id.tv_meaning);
            ivFavorite = itemView.findViewById(R.id.iv_favorite);
        }
    }
}