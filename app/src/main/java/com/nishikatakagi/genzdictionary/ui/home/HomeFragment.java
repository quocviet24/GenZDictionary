// src/main/java/com/nishikatakagi/genzdictionary/ui/home/HomeFragment.java
package com.nishikatakagi.genzdictionary.ui.home;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider; // Import ViewModelProvider
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.nishikatakagi.genzdictionary.R;
import com.nishikatakagi.genzdictionary.SlangWordAdapter;
import com.nishikatakagi.genzdictionary.models.SlangWord;
import com.nishikatakagi.genzdictionary.ui.home.HomeViewModel; // Import HomeViewModel

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvSlangWords;
    private EditText etSearch;
    private Spinner spinnerSort;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private SlangWordAdapter adapter;
    private List<SlangWord> slangWordList; // Danh sách gốc
    private List<SlangWord> filteredWordList; // Danh sách hiển thị sau khi lọc/tìm kiếm
    private FirebaseFirestore firestore;
    private String currentQuery = "";
    private boolean sortNewest = true;
    private LinearLayoutManager layoutManager;

    private HomeViewModel viewModel; // Khai báo ViewModel

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Khởi tạo ViewModel. Sử dụng requireActivity() để ViewModel có phạm vi lifecycle của Activity
        // Điều này đảm bảo ViewModel tồn tại qua các lần tạo lại Fragment.
        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Firestore and views
        firestore = FirebaseFirestore.getInstance();
        rvSlangWords = view.findViewById(R.id.rv_slang_words);
        etSearch = view.findViewById(R.id.et_search);
        spinnerSort = view.findViewById(R.id.spinner_sort);
        progressBar = view.findViewById(R.id.progress_bar);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);

        // Initialize RecyclerView
        slangWordList = new ArrayList<>();
        filteredWordList = new ArrayList<>();
        adapter = new SlangWordAdapter(requireContext(), filteredWordList);
        adapter.setOnItemClickListener((slangWord, itemView) -> {
            // Lưu vị trí cuộn và danh sách từ vào ViewModel trước khi điều hướng
            int scrollPosition = layoutManager.findFirstVisibleItemPosition();
            viewModel.setScrollPosition(scrollPosition);
            viewModel.setSlangWords(new ArrayList<>(slangWordList)); // Lưu bản sao của danh sách

            Bundle bundle = new Bundle();
            bundle.putSerializable("slang_word", slangWord); // SlangWord implements Serializable
            NavController navController = Navigation.findNavController(itemView);
            navController.navigate(R.id.action_nav_home_to_slang_word_detail_fragment, bundle);
        });
        layoutManager = new LinearLayoutManager(getContext());
        rvSlangWords.setLayoutManager(layoutManager);
        rvSlangWords.setAdapter(adapter);

        // Setup search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                currentQuery = s.toString().trim().toLowerCase();
                filterAndUpdateList();
            }
        });

        // Setup sort
        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortNewest = position == 0; // 0: Mới nhất, 1: Cũ nhất
                // Khi thay đổi kiểu sắp xếp, luôn tải lại dữ liệu và cuộn về đầu
                // (hoặc bạn có thể giữ vị trí nếu bạn muốn, nhưng tải lại dữ liệu mới thường đi kèm với cuộn về đầu)
                viewModel.setInitialDataLoaded(false); // Đánh dấu là cần tải lại dữ liệu
                fetchSlangWords();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Lấy dữ liệu từ ViewModel hoặc tải mới nếu chưa có
        if (viewModel.hasData()) {
            Log.d("HomeFragment", "Loading data from ViewModel");
            slangWordList = viewModel.getSlangWords().getValue();
            if (slangWordList == null) { // Trường hợp _slangWords là null nhưng hasData() vẫn true nếu list rỗng
                slangWordList = new ArrayList<>();
            }
            filteredWordList.clear();
            filteredWordList.addAll(slangWordList);
            adapter.updateData(filteredWordList); // Cập nhật adapter với danh sách đã lọc

            // Khôi phục vị trí cuộn
            Integer savedScrollPosition = viewModel.getScrollPosition().getValue();
            if (savedScrollPosition != null) {
                layoutManager.scrollToPositionWithOffset(savedScrollPosition, 0);
                Log.d("HomeFragment", "Restored scroll position: " + savedScrollPosition);
            }
        } else {
            Log.d("HomeFragment", "No data in ViewModel, fetching from Firestore");
            fetchSlangWords(); // Tải dữ liệu lần đầu
        }

        return view;
    }

    // Loại bỏ onViewStateRestored và onSaveInstanceState vì ViewModel đã đảm nhiệm việc lưu trạng thái
    @Override
    public void onResume() {
        super.onResume();
        // Không cần cuộn lại ở đây nếu đã xử lý trong onCreateView
        // filterAndUpdateList(); // Có thể gọi để đảm bảo dữ liệu được hiển thị đúng sau khi quay lại
    }

    private void fetchSlangWords() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);
        Log.d("HomeFragment", "Bắt đầu truy vấn Firestore cho slang_words với status = active");

        Query query = firestore.collection("slang_words")
                .whereEqualTo("status", "active")
                .orderBy("createdAt", sortNewest ? Query.Direction.DESCENDING : Query.Direction.ASCENDING);

        query.get().addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                slangWordList.clear();
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                    Log.d("HomeFragment", "Tải được " + querySnapshot.size() + " từ lóng với status = active");
                    for (var doc : querySnapshot) {
                        try {
                            SlangWord slangWord = doc.toObject(SlangWord.class);
                            if (slangWord.getSlangWordId() == null) {
                                slangWord.setSlangWordId(doc.getId());
                            }
                            Log.d("HomeFragment", "Word: " + slangWord.getWord() + ", Status: " + slangWord.getStatus() + ", ID: " + slangWord.getSlangWordId());
                            slangWordList.add(slangWord);
                        } catch (Exception e) {
                            Log.e("HomeFragment", "Lỗi khi ánh xạ document " + doc.getId() + ": " + e.getMessage(), e);
                        }
                    }
                    // Cập nhật ViewModel sau khi tải dữ liệu mới
                    viewModel.setSlangWords(new ArrayList<>(slangWordList));
                    filterAndUpdateList();
                    viewModel.setInitialDataLoaded(true); // Đánh dấu là đã tải dữ liệu lần đầu
                } else {
                    Log.d("HomeFragment", "Không tìm thấy tài liệu nào với status = active");
                    tvEmptyState.setVisibility(View.VISIBLE);
                    tvEmptyState.setText("Không tìm thấy từ lóng nào");
                    // Đặt danh sách trống vào ViewModel nếu không có dữ liệu
                    viewModel.setSlangWords(new ArrayList<>());
                    filterAndUpdateList(); // Cập nhật adapter để hiển thị trạng thái rỗng
                }
            } else {
                Log.e("HomeFragment", "Lỗi tải dữ liệu từ Firestore: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                tvEmptyState.setVisibility(View.VISIBLE);
                tvEmptyState.setText("Lỗi khi tải dữ liệu từ lóng");
                Snackbar.make(requireView(), "Lỗi khi tải dữ liệu từ lóng: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void filterAndUpdateList() {
        filteredWordList.clear();
        if (currentQuery.isEmpty()) {
            filteredWordList.addAll(slangWordList);
        } else {
            for (SlangWord slangWord : slangWordList) {
                if (slangWord.getWord() != null && slangWord.getWord().toLowerCase().contains(currentQuery) ||
                        slangWord.getMeaning() != null && slangWord.getMeaning().toLowerCase().contains(currentQuery)) {
                    filteredWordList.add(slangWord);
                }
            }
        }
        progressBar.setVisibility(View.GONE);
        if (filteredWordList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText(currentQuery.isEmpty() ? "Không tìm thấy từ lóng nào" : "Không tìm thấy kết quả cho \"" + currentQuery + "\"");
        } else {
            tvEmptyState.setVisibility(View.GONE);
        }
        adapter.updateData(filteredWordList);
    }
}