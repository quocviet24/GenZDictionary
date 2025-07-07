package com.nishikatakagi.genzdictionary.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nishikatakagi.genzdictionary.R;
import com.nishikatakagi.genzdictionary.models.Account;

import java.util.ArrayList;
import java.util.List;

public class ManageAccountFragment extends Fragment {

    private RecyclerView recyclerView;
    private AccountAdapter accountAdapter;
    private List<Account> accountList;
    private List<Account> filteredList;
    private EditText etSearch;
    private Spinner spinnerFilter;
    private FirebaseFirestore firestore;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_account, container, false);

        firestore = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.rv_account_list);
        etSearch = view.findViewById(R.id.et_search);
        spinnerFilter = view.findViewById(R.id.spinner_filter);

        accountList = new ArrayList<>();
        filteredList = new ArrayList<>();
        accountAdapter = new AccountAdapter(filteredList, account -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("account", account);
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_manage_account_to_account_detail, bundle);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(accountAdapter);

        // Setup filter spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.filter_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(adapter);

        // Load accounts
        loadAccounts();

        // Search functionality
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                filterAccounts();
            }
        });

        // Filter functionality
        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterAccounts();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        return view;
    }

    private void loadAccounts() {
        firestore.collection("accounts")
                .whereNotEqualTo("role", "admin")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    accountList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Account account = document.toObject(Account.class);
                        account.setId(document.getId()); // Set the random document ID
                        accountList.add(account);
                    }
                    filteredList.clear();
                    filteredList.addAll(accountList);
                    accountAdapter.notifyDataSetChanged();
                });
    }

    private void filterAccounts() {
        String query = etSearch.getText().toString().trim().toLowerCase();
        String filter = spinnerFilter.getSelectedItem().toString();
        filteredList.clear();

        for (Account account : accountList) {
            boolean matchesSearch = account.getEmail().toLowerCase().contains(query) ||
                    account.getUsername().toLowerCase().contains(query);
            boolean matchesFilter = filter.equals("Tất cả") ||
                    (filter.equals("Hoạt động") && account.getStatus().equals("active")) ||
                    (filter.equals("Ngừng hoạt động") && account.getStatus().equals("deactive"));

            if (matchesSearch && matchesFilter) {
                filteredList.add(account);
            }
        }
        accountAdapter.notifyDataSetChanged();
    }
}

class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {

    private List<Account> accounts;
    private OnAccountClickListener listener;

    interface OnAccountClickListener {
        void onAccountClick(Account account);
    }

    public AccountAdapter(List<Account> accounts, OnAccountClickListener listener) {
        this.accounts = accounts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account, parent, false);
        return new AccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        Account account = accounts.get(position);
        holder.tvEmail.setText(account.getEmail());
        holder.tvUsername.setText(account.getUsername());
        holder.cardView.setCardBackgroundColor(
                account.getStatus().equals("active") ?
                        holder.itemView.getContext().getResources().getColor(R.color.light_green) :
                        holder.itemView.getContext().getResources().getColor(R.color.light_red)
        );
        holder.itemView.setOnClickListener(v -> listener.onAccountClick(account));
    }

    @Override
    public int getItemCount() {
        return accounts.size();
    }

    static class AccountViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmail, tvUsername;
        androidx.cardview.widget.CardView cardView;

        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmail = itemView.findViewById(R.id.tv_account_email);
            tvUsername = itemView.findViewById(R.id.tv_account_username);
            cardView = itemView.findViewById(R.id.card_view);
        }
    }
}