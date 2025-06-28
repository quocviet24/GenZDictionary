package com.nishikatakagi.genzdictionary;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SlangWordAdapter extends RecyclerView.Adapter<SlangWordAdapter.SlangWordViewHolder> {

    private List<SlangWord> slangWordList;
    private Context context;

    public SlangWordAdapter(Context context, List<SlangWord> slangWordList) {
        this.context = context;
        this.slangWordList = slangWordList;
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

        // Handle item click
        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("slang_word", slangWord);
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.nav_slang_word_detail, bundle);
        });
    }

    @Override
    public int getItemCount() {
        return slangWordList.size();
    }

    public void updateData(List<SlangWord> newSlangWordList) {
        this.slangWordList = newSlangWordList;
        notifyDataSetChanged();
    }

    static class SlangWordViewHolder extends RecyclerView.ViewHolder {
        TextView tvWord, tvMeaning;

        public SlangWordViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWord = itemView.findViewById(R.id.tv_word);
            tvMeaning = itemView.findViewById(R.id.tv_meaning);
        }
    }
}