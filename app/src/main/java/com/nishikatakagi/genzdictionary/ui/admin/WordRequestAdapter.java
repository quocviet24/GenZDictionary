package com.nishikatakagi.genzdictionary.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.nishikatakagi.genzdictionary.R;
import com.nishikatakagi.genzdictionary.models.WordRequest;
import java.util.List;

public class WordRequestAdapter extends RecyclerView.Adapter<WordRequestAdapter.WordRequestViewHolder> {

    private List<WordRequest> wordRequestList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(WordRequest wordRequest);
    }

    public WordRequestAdapter(List<WordRequest> wordRequestList, OnItemClickListener listener) {
        this.wordRequestList = wordRequestList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WordRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_slang_word, parent, false);
        return new WordRequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordRequestViewHolder holder, int position) {
        WordRequest wordRequest = wordRequestList.get(position);
        holder.tvWord.setText(wordRequest.getWord());
        holder.tvMeaning.setText(wordRequest.getMeaning());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(wordRequest));
        holder.ivFavorite.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return wordRequestList.size();
    }

    static class WordRequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvWord, tvMeaning;
        View ivFavorite;

        public WordRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWord = itemView.findViewById(R.id.tv_word);
            tvMeaning = itemView.findViewById(R.id.tv_meaning);
            ivFavorite = itemView.findViewById(R.id.iv_favorite);
        }
    }
}