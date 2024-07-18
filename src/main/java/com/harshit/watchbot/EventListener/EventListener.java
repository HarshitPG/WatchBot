package com.harshit.watchbot.EventListener;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import okhttp3.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class EventListener extends ListenerAdapter {


    private final String perspectiveApiKey;
    private static final OkHttpClient httpClient = new OkHttpClient();
    private static final Gson gson = new Gson();
    private final List<String> inappropriateStrings;

    public EventListener(String perspectiveApiKey) {
        this.perspectiveApiKey = perspectiveApiKey;
        this.inappropriateStrings = loadInappropriateStrings();

    }

    private List<String> loadInappropriateStrings() {
        Dotenv dotenv = Dotenv.load();
        String inappropriateWords = dotenv.get("INAPPROPRIATE_WORDS");
        return Arrays.asList(inappropriateWords.split(","));
    }


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        handleMessage(event.getMessage());
    }

    @Override
    public void onMessageUpdate(MessageUpdateEvent event) {
        handleMessage(event.getMessage());
    }
        public void handleMessage( Message message) {
//        Message content = event.getMessage();
//        System.out.println("content"+content);


        try{
            if(containsInappropriateContent(message.getContentStripped())){
                message.delete().queue(success -> message.getChannel().sendMessage("Inappropriate content detected and removed.").queue(),
                        failure -> System.err.println("Failed to delete message."));
                message.getChannel().sendMessage("Inappropriate content detected and removed.");
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public boolean containsInappropriateContent(String content) throws IOException {

        if (content.equals("Inappropriate content detected and removed.")) {
            return false;
        }

        for (String inappropriateString : inappropriateStrings) {
            if (content.toLowerCase().contains(inappropriateString.toLowerCase())) {
                return true;
            }
        }

        String url = "https://commentanalyzer.googleapis.com/v1alpha1/comments:analyze?key=" + perspectiveApiKey;

        JsonObject commentJson = new JsonObject();
        commentJson.addProperty("text", content);

        JsonObject requestJson = new JsonObject();
        requestJson.add("comment", commentJson);
        requestJson.add("languages", gson.toJsonTree(new String[]{"en"}));
        JsonObject requestedAttributes = new JsonObject();
        requestedAttributes.add("TOXICITY", new JsonObject());
        requestJson.add("requestedAttributes", requestedAttributes);

        RequestBody requestBody = RequestBody.create(gson.toJson(requestJson), MediaType.get("application/json; charset=utf-8"));

        Request httpRequest = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build();

        try (Response response = httpClient.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response.code());
            }

            String responseBody = response.body().string();
            JsonObject responseJson = gson.fromJson(responseBody, JsonObject.class);
            JsonObject attributeScores = responseJson.getAsJsonObject("attributeScores");
            JsonObject toxicityScore = attributeScores.getAsJsonObject("TOXICITY");
            double scoreValue = toxicityScore.getAsJsonObject("summaryScore").getAsJsonPrimitive("value").getAsDouble();

            System.out.println("Toxicity Score: " + scoreValue);

            return scoreValue > 0.75; // Adjust threshold as needed
        } catch (IOException e) {
            System.err.println("Error executing request: " + e.getMessage());
            throw e;
        }
    }



}