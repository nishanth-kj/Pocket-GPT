package com.pocketgpt.app.ui.documents;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.pocketgpt.app.constants.ResponseStatus;
import com.pocketgpt.app.model.response.ApiResponse;
import java.util.ArrayList;
import java.util.List;
import android.os.Handler;
import android.os.Looper;

public class DocumentsViewModel extends ViewModel {
    private final MutableLiveData<ApiResponse<List<String>>> searchResult = new MutableLiveData<>();

    public LiveData<ApiResponse<List<String>>> getSearchResult() {
        return searchResult;
    }

    public void search(String query) {
        // Mocking a network search
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            List<String> mockData = new ArrayList<>();
            mockData.add("Result 1 for " + query);
            mockData.add("Result 2 for " + query);
            
            ApiResponse<List<String>> response = new ApiResponse<>();
            response.status = ResponseStatus.SUCCESS;
            response.data = mockData;
            
            searchResult.setValue(response);
        }, 1000);
    }
}