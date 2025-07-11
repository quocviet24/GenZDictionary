package com.nishikatakagi.genzdictionary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.nishikatakagi.genzdictionary.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private SharedPreferences sharedPreferences;
    private DrawerLayout drawer;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo SharedPreferences
        sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);

        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.addNewWord.setOnClickListener(view -> {
            // Check if user is logged in
            boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
            if (!isLoggedIn) {
                Toast.makeText(MainActivity.this, "Bạn cần đăng nhập để có thể yêu cầu tạo từ mới", Toast.LENGTH_SHORT).show();
                return;
            }
            // Navigate to ClientRequestFragment
            NavController navController = Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.client_request_fragment);
        });

        drawer = binding.drawerLayout;
        navigationView = binding.navView;

        // Cập nhật tiêu đề của navigation header
        TextView navHeaderTitle = navigationView.getHeaderView(0).findViewById(R.id.nav_header_title);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        String username = sharedPreferences.getString("username", null);
        boolean isAdmin = sharedPreferences.getBoolean("isAdmin", false);
        if (isLoggedIn && username != null) {
            navHeaderTitle.setText("Từ điển Gen Z xin chào " + username);
        } else {
            navHeaderTitle.setText("Từ điển Gen Z");
        }

        // Cấu hình navigation
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.list_favorite, R.id.dashboard, R.id.manage_account,
                R.id.manage_request_new_word, R.id.garbage, R.id.client_request_fragment)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Kiểm tra trạng thái đăng nhập để hiển thị menu
        Menu navMenu = navigationView.getMenu();
        navMenu.findItem(R.id.nav_login).setVisible(!isLoggedIn);
        navMenu.findItem(R.id.nav_logout).setVisible(isLoggedIn);
        navMenu.findItem(R.id.list_favorite).setVisible(isLoggedIn);
        navMenu.findItem(R.id.dashboard).setVisible(isLoggedIn && isAdmin);
        navMenu.findItem(R.id.manage_account).setVisible(isLoggedIn && isAdmin);
        navMenu.findItem(R.id.manage_request_new_word).setVisible(isLoggedIn && isAdmin);
        navMenu.findItem(R.id.garbage).setVisible(isLoggedIn && isAdmin);
        navMenu.findItem(R.id.admin_create_word_fragment).setVisible(isLoggedIn && isAdmin);

        // Xử lý sự kiện click menu
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_login) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                drawer.closeDrawers();
                return true;
            } else if (id == R.id.nav_logout) {
                // Xử lý đăng xuất
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isLoggedIn", false);
                editor.remove("email");
                editor.remove("username");
                editor.remove("isAdmin");
                editor.apply();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.nav_home && navController.getCurrentDestination().getId() == R.id.client_request_fragment) {
                // Điều hướng từ ClientRequestFragment đến Home
                navController.navigate(R.id.action_client_request_to_home);
                drawer.closeDrawers();
                return true;
            } else if (id == R.id.list_favorite && navController.getCurrentDestination().getId() == R.id.client_request_fragment) {
                // Điều hướng từ ClientRequestFragment đến Favorite
                navController.navigate(R.id.action_client_request_to_list_favorite);
                drawer.closeDrawers();
                return true;
            } else if (id == R.id.manage_account && navController.getCurrentDestination().getId() == R.id.client_request_fragment) {
                // Điều hướng từ ClientRequestFragment đến ManageAccount
                navController.navigate(R.id.manage_account);
                drawer.closeDrawers();
                return true;
            }
            // Xử lý các mục menu khác
            boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
            if (handled) {
                drawer.closeDrawers();
            }
            return handled;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}