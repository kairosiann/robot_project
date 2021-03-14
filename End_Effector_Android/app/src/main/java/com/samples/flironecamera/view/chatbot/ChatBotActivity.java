package com.samples.flironecamera.view.chatbot;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.Lists;
import com.samples.flironecamera.App;
import com.samples.flironecamera.R;
import com.samples.flironecamera.databinding.ActivityChatbotBinding;
import com.samples.flironecamera.dialogflow.DialogFlowRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ChatBotActivity extends AppCompatActivity {
    private final static int REQUEST_CODE_RECOGNIZE_SPEECH = 1000;

    private ActivityChatbotBinding binding;
    private ChatBotViewModel chatBotViewModel;
    private TTSViewModel ttsViewModel;

    private Runnable isSpeakingRunnable = new Runnable() {
        @Override
        public void run() {
            if (ttsViewModel.isSpeaking()) {
                binding.getRoot().postDelayed(isSpeakingRunnable, 1000);
            } else {
                runOnUiThread(recognizeSpeechRunnable);
            }
        }
    };

    private Runnable recognizeSpeechRunnable = () -> {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        intent.putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                "대답하세요"
        );

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_RECOGNIZE_SPEECH);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatbotBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        try {
            InputStream stream = this.getResources().openRawResource(R.raw.credentials);
            GoogleCredentials credentials = GoogleCredentials.fromStream(stream)
                    .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));

            App app = (App) getApplication();
            DialogFlowRepository repository = new DialogFlowRepository(
                    app.executorService,
                    app.mainThreadHandler,
                    credentials
            );

            chatBotViewModel = new ChatBotViewModel(repository);
            ttsViewModel = new TTSViewModel(this);

            chatBotViewModel.sendMessage("Excuse me your temperature seems a little bit high");

            // 응답
            chatBotViewModel.detectIntentResponseMutableLiveData.observe(this, detectIntentResponse -> {
                final String text = detectIntentResponse.getQueryResult().getFulfillmentText();
                binding.chatbotText.setText(text);
                binding.userText.setText("");

                ttsViewModel.speak(text);

                binding.getRoot().postDelayed(isSpeakingRunnable, 3000);
            });

            binding.sendButton.setOnClickListener(v -> {
                chatBotViewModel.sendMessage(binding.userText.getText().toString());
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_RECOGNIZE_SPEECH && resultCode == RESULT_OK) {
            if (data != null) {
                // 내가 말한 내용 표시
                ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                binding.userText.setText(results.get(0));

                // dialog flow 요청
                chatBotViewModel.sendMessage(results.get(0));
            }
        }
    }
}