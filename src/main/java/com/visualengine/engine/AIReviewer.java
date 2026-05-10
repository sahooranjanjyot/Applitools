package com.visualengine.engine;

import okhttp3.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.visualengine.config.Config;
import java.io.IOException;
import java.util.Map;

public class AIReviewer {
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    public String classifyDiff(double score, Map<String, Integer> domDiff, Map<String, Object> metadata) {
        // Proceeding with local LLM evaluation

        String prompt = String.format("You are an AI QA Reviewer.\nMetadata: %s\nScore: %f\nDOM Diff: %s\nClassify into exactly one: REAL_DEFECT, EXPECTED_CHANGE, LOCATOR_ISSUE, LAYOUT_SHIFT",
                gson.toJson(metadata), score, gson.toJson(domDiff));

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", prompt);

        JsonArray messages = new JsonArray();
        messages.add(userMsg);

        JsonObject bodyObj = new JsonObject();
        bodyObj.addProperty("model", "llama3"); // Local model
        bodyObj.addProperty("prompt", prompt);
        bodyObj.addProperty("stream", false);

        RequestBody body = RequestBody.create(bodyObj.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url("http://localhost:11434/api/generate")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                JsonObject jsonResponse = gson.fromJson(response.body().string(), JsonObject.class);
                String result = jsonResponse.get("response").getAsString().trim();
                
                if (result.contains("EXPECTED_CHANGE")) return "EXPECTED_CHANGE";
                if (result.contains("LOCATOR_ISSUE")) return "LOCATOR_ISSUE";
                if (result.contains("LAYOUT_SHIFT")) return "LAYOUT_SHIFT";
                return "REAL_DEFECT";
            }
        } catch (IOException e) {
            System.err.println("Local LLM not responding. Falling back to REAL_DEFECT.");
        }
        return "REAL_DEFECT";
    }
}
