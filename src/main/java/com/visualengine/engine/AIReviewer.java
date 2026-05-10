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
        if (Config.OPENAI_API_KEY.isEmpty()) {
            return "Classification Unavailable (No API Key)";
        }

        String prompt = String.format("You are an AI QA Reviewer.\nMetadata: %s\nScore: %f\nDOM Diff: %s\nClassify into exactly one: REAL_DEFECT, EXPECTED_CHANGE, LOCATOR_ISSUE, LAYOUT_SHIFT",
                gson.toJson(metadata), score, gson.toJson(domDiff));

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", prompt);

        JsonArray messages = new JsonArray();
        messages.add(userMsg);

        JsonObject bodyObj = new JsonObject();
        bodyObj.addProperty("model", "gpt-4o");
        bodyObj.add("messages", messages);
        bodyObj.addProperty("max_tokens", 10);
        bodyObj.addProperty("temperature", 0.0);

        RequestBody body = RequestBody.create(bodyObj.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer " + Config.OPENAI_API_KEY)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                JsonObject jsonResponse = gson.fromJson(response.body().string(), JsonObject.class);
                return jsonResponse.getAsJsonArray("choices").get(0).getAsJsonObject().getAsJsonObject("message").get("content").getAsString().trim();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "UNKNOWN";
    }
}
