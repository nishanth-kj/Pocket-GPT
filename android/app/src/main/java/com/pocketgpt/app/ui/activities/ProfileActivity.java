package com.pocketgpt.app.ui.activities;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.pocketgpt.app.R;
import com.pocketgpt.app.model.AiModel;
import com.pocketgpt.app.model.AppDocument;
import com.pocketgpt.app.model.ChatSession;
import com.pocketgpt.app.repository.ChatDao;
import com.pocketgpt.app.repository.SearchDao;
import com.pocketgpt.app.utils.ModelManager;
import com.pocketgpt.app.utils.PocketGptDatabase;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileActivity extends AppCompatActivity {

    private static final String PREFS_PROFILE = "pocketgpt_user_profile";
    private static final String KEY_USER_NAME = "profile_user_name";

    private TextView textProfileUserName;
    private TextView textProfileDocCount;
    private TextView textProfileChunkCount;
    private TextView textProfileSessionCount;
    private TextView textProfileMessageCount;
    private TextView textProfileActiveEngine;
    private TextView textProfileModelStorage;
    private TextView textDeviceModel;
    private TextView textDeviceAndroidVersion;
    private TextView textDeviceCpuAbi;
    private TextView textDeviceRamUsage;
    private TextView textDeviceStorageFree;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private SharedPreferences profilePrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profilePrefs = getSharedPreferences(PREFS_PROFILE, Context.MODE_PRIVATE);

        MaterialToolbar toolbar = findViewById(R.id.profileToolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        textProfileUserName = findViewById(R.id.textProfileUserName);
        textProfileDocCount = findViewById(R.id.textProfileDocCount);
        textProfileChunkCount = findViewById(R.id.textProfileChunkCount);
        textProfileSessionCount = findViewById(R.id.textProfileSessionCount);
        textProfileMessageCount = findViewById(R.id.textProfileMessageCount);
        textProfileActiveEngine = findViewById(R.id.textProfileActiveEngine);
        textProfileModelStorage = findViewById(R.id.textProfileModelStorage);
        textDeviceModel = findViewById(R.id.textDeviceModel);
        textDeviceAndroidVersion = findViewById(R.id.textDeviceAndroidVersion);
        textDeviceCpuAbi = findViewById(R.id.textDeviceCpuAbi);
        textDeviceRamUsage = findViewById(R.id.textDeviceRamUsage);
        textDeviceStorageFree = findViewById(R.id.textDeviceStorageFree);

        MaterialButton btnEditProfileName = findViewById(R.id.btnEditProfileName);
        btnEditProfileName.setOnClickListener(v -> showEditNameDialog());

        MaterialButton btnOpenDevWebsite = findViewById(R.id.btnOpenDevWebsite);
        if (btnOpenDevWebsite != null) {
            btnOpenDevWebsite.setOnClickListener(v -> {
                try {
                    android.content.Intent browserIntent = new android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse("https://nishanth-kj.xyz"));
                    startActivity(browserIntent);
                } catch (Exception e) {
                    Toast.makeText(this, "Opening nishanth-kj.xyz", Toast.LENGTH_SHORT).show();
                }
            });
        }

        loadUserProfile();
        loadHardwareDiagnostics();
        loadDatabaseTelemetry();
    }

    private void loadUserProfile() {
        String name = profilePrefs.getString(KEY_USER_NAME, "Pocket GPT Operator");
        textProfileUserName.setText(name);
    }

    private void showEditNameDialog() {
        EditText input = new EditText(this);
        input.setText(textProfileUserName.getText());
        input.setHint("Enter Operator Name");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 30);
        layout.addView(input);

        new AlertDialog.Builder(this)
                .setTitle("Edit Operator Name")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        profilePrefs.edit().putString(KEY_USER_NAME, newName).apply();
                        textProfileUserName.setText(newName);
                        Toast.makeText(this, "Profile name updated!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadHardwareDiagnostics() {
        // Device Model & Manufacturer
        String manufacturer = Build.MANUFACTURER.substring(0, 1).toUpperCase() + Build.MANUFACTURER.substring(1);
        textDeviceModel.setText("Device: " + manufacturer + " " + Build.MODEL);

        // Android Version & API
        textDeviceAndroidVersion.setText("Android OS: Android " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")");

        // CPU ABIs
        String cpuAbi = Build.SUPPORTED_ABIS.length > 0 ? Build.SUPPORTED_ABIS[0] : "arm64-v8a";
        textDeviceCpuAbi.setText("CPU Architecture: " + cpuAbi + " (" + Runtime.getRuntime().availableProcessors() + " Cores)");

        // RAM Usage
        try {
            ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            if (activityManager != null) {
                activityManager.getMemoryInfo(memoryInfo);
                double availGb = memoryInfo.availMem / (1024.0 * 1024.0 * 1024.0);
                double totalGb = memoryInfo.totalMem / (1024.0 * 1024.0 * 1024.0);
                textDeviceRamUsage.setText(String.format(Locale.US, "RAM: %.1f GB Available / %.1f GB Total", availGb, totalGb));
            }
        } catch (Exception e) {
            textDeviceRamUsage.setText("RAM: Telemetry available");
        }

        // Storage Free
        try {
            File dataDir = Environment.getDataDirectory();
            StatFs stat = new StatFs(dataDir.getPath());
            long bytesAvailable = stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
            double freeGb = bytesAvailable / (1024.0 * 1024.0 * 1024.0);
            textDeviceStorageFree.setText(String.format(Locale.US, "Internal Storage: %.1f GB Free", freeGb));
        } catch (Exception e) {
            textDeviceStorageFree.setText("Internal Storage: Available");
        }
    }

    private void loadDatabaseTelemetry() {
        executorService.execute(() -> {
            PocketGptDatabase db = PocketGptDatabase.getDatabase(this);
            SearchDao searchDao = db.searchDao();
            ChatDao chatDao = db.chatDao();

            List<AppDocument> docs = searchDao.getAllDocuments();
            int docCount = docs.size();
            int chunkCount = searchDao.getChunkCount();

            List<ChatSession> sessions = chatDao.getAllSessions();
            int sessionCount = sessions.size();
            int count = 0;
            for (ChatSession s : sessions) {
                count += s.messageCount;
            }
            final int totalMessages = count;

            ModelManager modelManager = ModelManager.getInstance(this);
            AiModel activeModel = modelManager.getActiveModel();
            String modelInfo = activeModel != null ? activeModel.getName() + " (" + activeModel.getPublisher() + ")" : "SmolLM 135M";
            String storageUsed = modelManager.getTotalStorageUsedFormatted();

            mainHandler.post(() -> {
                textProfileDocCount.setText(docCount + " Docs");
                textProfileChunkCount.setText(chunkCount + " Chunks");
                textProfileSessionCount.setText(sessionCount + " Sessions");
                textProfileMessageCount.setText(totalMessages + " Msgs");
                textProfileActiveEngine.setText("Active Model: " + modelInfo);
                textProfileModelStorage.setText("Model Storage: " + storageUsed);
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDatabaseTelemetry();
    }
}