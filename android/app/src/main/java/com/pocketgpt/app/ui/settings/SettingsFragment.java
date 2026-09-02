package com.pocketgpt.app.ui.settings;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.pocketgpt.app.R;
import com.pocketgpt.app.model.AiModel;
import com.pocketgpt.app.utils.ModelManager;
import com.pocketgpt.app.utils.PocketGptDatabase;

import java.io.File;
import java.util.List;

public class SettingsFragment extends Fragment {

    private TextView textCurrentModelDir;
    private TextView textModelStorageUsage;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        textCurrentModelDir = view.findViewById(R.id.textCurrentModelDir);
        textModelStorageUsage = view.findViewById(R.id.textModelStorageUsage);
        MaterialButton btnChangeModelDir = view.findViewById(R.id.btnChangeModelDir);
        MaterialButton btnDownloadAllModels = view.findViewById(R.id.btnDownloadAllModels);
        MaterialButton btnClearModelCache = view.findViewById(R.id.btnClearModelCache);
        MaterialButton btnClearAllData = view.findViewById(R.id.btnClearAllData);
        com.google.android.material.button.MaterialButtonToggleGroup toggleThemeGroup = view.findViewById(R.id.toggleThemeGroup);

        // Initialize Theme Mode
        String currentTheme = com.pocketgpt.app.utils.ThemeHelper.getThemeMode(requireContext());
        if (com.pocketgpt.app.utils.ThemeHelper.MODE_LIGHT.equals(currentTheme)) {
            toggleThemeGroup.check(R.id.btnThemeLight);
        } else if (com.pocketgpt.app.utils.ThemeHelper.MODE_DARK.equals(currentTheme)) {
            toggleThemeGroup.check(R.id.btnThemeDark);
        } else {
            toggleThemeGroup.check(R.id.btnThemeSystem);
        }

        toggleThemeGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnThemeLight) {
                    com.pocketgpt.app.utils.ThemeHelper.setThemeMode(requireContext(), com.pocketgpt.app.utils.ThemeHelper.MODE_LIGHT);
                } else if (checkedId == R.id.btnThemeDark) {
                    com.pocketgpt.app.utils.ThemeHelper.setThemeMode(requireContext(), com.pocketgpt.app.utils.ThemeHelper.MODE_DARK);
                } else if (checkedId == R.id.btnThemeSystem) {
                    com.pocketgpt.app.utils.ThemeHelper.setThemeMode(requireContext(), com.pocketgpt.app.utils.ThemeHelper.MODE_SYSTEM);
                }
            }
        });

        refreshModelDirUI();

        btnChangeModelDir.setOnClickListener(v -> showChangeModelDirDialog());
        btnDownloadAllModels.setOnClickListener(v -> startDownloadAllModels());
        btnClearModelCache.setOnClickListener(v -> showClearModelCacheDialog());

        btnClearAllData.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Clear All Data")
                    .setMessage("This will wipe all documents, vector embeddings, chunk records, and chat history from the device. Are you sure?")
                    .setPositiveButton("Clear Everything", (dialog, which) -> {
                        Context context = getContext();
                        if (context == null) return;
                        final Context appContext = context.getApplicationContext();
                        new Thread(() -> {
                            PocketGptDatabase db = PocketGptDatabase.getDatabase(appContext);
                            db.searchDao().clearAllChunks();
                            db.searchDao().clearAllDocuments();
                            db.chatDao().clearAllMessages();
                            db.chatDao().clearAllSessions();
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() ->
                                        Toast.makeText(getContext(), "Database and chat history cleared.", Toast.LENGTH_SHORT).show()
                                );
                            }
                        }).start();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        MaterialButton btnSettingsDevWebsite = view.findViewById(R.id.btnSettingsDevWebsite);
        if (btnSettingsDevWebsite != null) {
            btnSettingsDevWebsite.setOnClickListener(v -> {
                try {
                    android.content.Intent browserIntent = new android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse("https://nishanth-kj.xyz"));
                    startActivity(browserIntent);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Opening nishanth-kj.xyz", Toast.LENGTH_SHORT).show();
                }
            });
        }

        return view;
    }

    private void refreshModelDirUI() {
        if (getContext() == null) return;
        ModelManager manager = ModelManager.getInstance(getContext());
        File dir = manager.getModelsDirectory();
        textCurrentModelDir.setText(dir != null ? dir.getAbsolutePath() : "Default");

        List<AiModel> models = manager.getAllModels();
        int downloadedCount = 0;
        for (AiModel m : models) {
            if (m.isDownloaded()) downloadedCount++;
        }
        textModelStorageUsage.setText("Disk usage: " + manager.getTotalStorageUsedFormatted() + " (" + downloadedCount + " models downloaded)");
    }

    private void showChangeModelDirDialog() {
        if (getContext() == null) return;
        ModelManager manager = ModelManager.getInstance(getContext());

        File extDir = requireContext().getExternalFilesDir("models");
        File intDir = new File(requireContext().getFilesDir(), "models");

        String[] options = {
                "External App Storage (" + (extDir != null ? extDir.getAbsolutePath() : "External") + ")",
                "Internal Protected Storage (" + intDir.getAbsolutePath() + ")",
                "Custom Directory Path"
        };

        new AlertDialog.Builder(requireContext())
                .setTitle("Set Model Storage Directory")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        if (extDir != null) {
                            manager.setCustomModelsDirectory(extDir.getAbsolutePath());
                            Toast.makeText(getContext(), "Set to External Storage", Toast.LENGTH_SHORT).show();
                            refreshModelDirUI();
                        }
                    } else if (which == 1) {
                        manager.setCustomModelsDirectory(intDir.getAbsolutePath());
                        Toast.makeText(getContext(), "Set to Internal Storage", Toast.LENGTH_SHORT).show();
                        refreshModelDirUI();
                    } else {
                        showCustomPathPrompt();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showCustomPathPrompt() {
        if (getContext() == null) return;
        ModelManager manager = ModelManager.getInstance(getContext());

        EditText input = new EditText(getContext());
        input.setHint("/storage/emulated/0/Download/PocketGPT_Models");
        if (manager.getModelsDirectory() != null) {
            input.setText(manager.getModelsDirectory().getAbsolutePath());
        }

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);
        layout.addView(input);

        new AlertDialog.Builder(requireContext())
                .setTitle("Custom Directory Path")
                .setView(layout)
                .setPositiveButton("Save & Switch", (dialog, which) -> {
                    String customPath = input.getText().toString().trim();
                    if (!customPath.isEmpty()) {
                        manager.setCustomModelsDirectory(customPath);
                        Toast.makeText(getContext(), "Model directory updated!", Toast.LENGTH_SHORT).show();
                        refreshModelDirUI();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void startDownloadAllModels() {
        if (getContext() == null) return;
        ModelManager manager = ModelManager.getInstance(getContext());
        List<AiModel> models = manager.getAllModels();

        Toast.makeText(getContext(), "Starting download queue for offline SLMs...", Toast.LENGTH_SHORT).show();

        for (AiModel model : models) {
            if (!model.isDownloaded() && !model.isDownloading()) {
                manager.downloadModel(model.getId(), new ModelManager.ModelDownloadListener() {
                    @Override
                    public void onProgress(String modelId, int progressPercent, String statusMessage) {
                        mainHandler.post(() -> refreshModelDirUI());
                    }

                    @Override
                    public void onComplete(String modelId, File modelFile) {
                        mainHandler.post(() -> {
                            Toast.makeText(getContext(), "Completed: " + model.getName(), Toast.LENGTH_SHORT).show();
                            refreshModelDirUI();
                        });
                    }

                    @Override
                    public void onError(String modelId, String errorMessage) {
                        mainHandler.post(() -> {
                            Toast.makeText(getContext(), "Failed " + model.getName() + ": " + errorMessage, Toast.LENGTH_SHORT).show();
                            refreshModelDirUI();
                        });
                    }
                });
            }
        }
    }

    private void showClearModelCacheDialog() {
        if (getContext() == null) return;
        ModelManager manager = ModelManager.getInstance(getContext());

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Downloaded Models")
                .setMessage("Are you sure you want to remove all downloaded model files from " + manager.getModelsDirectory().getAbsolutePath() + "?")
                .setPositiveButton("Delete All", (dialog, which) -> {
                    manager.deleteAllDownloadedModels();
                    Toast.makeText(getContext(), "All model files removed", Toast.LENGTH_SHORT).show();
                    refreshModelDirUI();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
