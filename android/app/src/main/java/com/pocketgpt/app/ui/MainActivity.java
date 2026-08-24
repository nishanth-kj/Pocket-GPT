package com.pocketgpt.app.ui;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.pocketgpt.app.R;
import com.pocketgpt.app.databinding.ActivityMainBinding;
import com.pocketgpt.app.ui.fragments.AiChatFragment;
import com.pocketgpt.app.ui.fragments.DownloadModelsFragment;
import com.pocketgpt.app.ui.fragments.HomeFragment;
import com.pocketgpt.app.ui.fragments.SearchFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
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
                switchFragment(new SearchFragment());
                binding.bottomNavigation.setSelectedItemId(R.id.bottom_search);
            } else if (id == R.id.nav_bookmarks) {
                Toast.makeText(this, "Bookmarks Clicked", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_download_models) {
                switchFragment(new DownloadModelsFragment());
                binding.bottomNavigation.setSelectedItemId(R.id.bottom_model);
            } else if (id == R.id.nav_settings) {
                showThemeDialog();
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
                switchFragment(new SearchFragment());
                return true;
            } else if (id == R.id.bottom_model) {
                Toast.makeText(this, "Model Clicked", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.bottom_profile) {
                Toast.makeText(this, "Profile Clicked", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        binding.fabAiChat.setOnClickListener(v -> switchFragment(new AiChatFragment()));

        if (savedInstanceState == null) {
            switchFragment(new HomeFragment());
            binding.navigationView.setCheckedItem(R.id.nav_home);
        }
    }

    private void switchFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void showThemeDialog() {
        String[] themes = {"System Default", "Light", "Dark"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Choose Theme")
                .setSingleChoiceItems(themes, 0, (dialog, which) -> {
                    Toast.makeText(this, "Theme set to: " + themes[which], Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .show();
    }
}