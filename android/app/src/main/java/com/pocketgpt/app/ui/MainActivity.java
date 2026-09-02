package com.pocketgpt.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import com.pocketgpt.app.R;
import com.pocketgpt.app.databinding.ActivityMainBinding;
import com.pocketgpt.app.ui.activities.ChatActivity;
import com.pocketgpt.app.ui.activities.ProfileActivity;
import com.pocketgpt.app.ui.home.HomeFragment;
import com.pocketgpt.app.ui.documents.DocumentsFragment;
import com.pocketgpt.app.ui.models.AiModelsFragment;
import com.pocketgpt.app.ui.settings.SettingsFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> binding.drawerLayout.openDrawer(GravityCompat.START));

        binding.navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                switchFragment(new HomeFragment());
                binding.bottomNavigation.setSelectedItemId(R.id.bottom_home);
            } else if (id == R.id.nav_search) {
                switchFragment(new DocumentsFragment());
                binding.bottomNavigation.setSelectedItemId(R.id.bottom_search);
            } else if (id == R.id.nav_ai_chat || id == R.id.nav_bookmarks) {
                startActivity(new Intent(this, ChatActivity.class));
            } else if (id == R.id.nav_download_models) {
                switchFragment(new AiModelsFragment());
                binding.bottomNavigation.setSelectedItemId(R.id.bottom_model);
            } else if (id == R.id.nav_settings) {
                switchFragment(new SettingsFragment());
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.bottom_home) {
                switchFragment(new HomeFragment());
                return true;
            } else if (id == R.id.bottom_search) {
                switchFragment(new DocumentsFragment());
                return true;
            } else if (id == R.id.bottom_model) {
                switchFragment(new AiModelsFragment());
                return true;
            } else if (id == R.id.bottom_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });

        binding.fabAiChat.setOnClickListener(v -> startActivity(new Intent(this, ChatActivity.class)));

        if (savedInstanceState == null) {
            switchFragment(new HomeFragment());
            binding.navigationView.setCheckedItem(R.id.nav_home);
        }
    }

    private void switchFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}