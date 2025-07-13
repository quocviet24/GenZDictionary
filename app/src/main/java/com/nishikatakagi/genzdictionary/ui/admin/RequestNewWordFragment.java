package com.nishikatakagi.genzdictionary.ui.admin;

import android.os.Bundle;
import android.util.Log;
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
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nishikatakagi.genzdictionary.R;
import com.nishikatakagi.genzdictionary.models.WordRequest;
import java.util.ArrayList;
import java.util.List;

public class RequestNewWordFragment extends Fragment {

    private RecyclerView rvWordRequests;
    private TextView tvEmptyState;
    private WordRequestAdapter adapter;
    private List<WordRequest> wordRequestList;
    private FirebaseFirestore firestore;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firestore = FirebaseFirestore.getInstance();
        wordRequestList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request_new_word, container, false);

        // Bind views
        rvWordRequests = view.findViewById(R.id.rv_word_requests);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);
        adapter = new WordRequestAdapter(wordRequestList, wordRequest -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("wordRequest", wordRequest);
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.word_request_detail_fragment, bundle);
        });
        rvWordRequests.setAdapter(adapter);

        // Fetch pending word requests
        fetchPendingWordRequests();

        return view;
    }

    private void fetchPendingWordRequests() {
        firestore.collection("slang_words")
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    wordRequestList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        WordRequest wordRequest = document.toObject(WordRequest.class);
                        wordRequestList.add(wordRequest);
                    }
                    adapter.notifyDataSetChanged();
                    tvEmptyState.setVisibility(wordRequestList.isEmpty() ? View.VISIBLE : View.GONE);
                    rvWordRequests.setVisibility(wordRequestList.isEmpty() ? View.GONE : View.VISIBLE);
                    Toast.makeText(getContext(), "Tải được " + wordRequestList.size() + " yêu cầu", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi tải yêu cầu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("RequestNewWordFragment", "Error: " + e.getMessage(), e);
                });
    }
}