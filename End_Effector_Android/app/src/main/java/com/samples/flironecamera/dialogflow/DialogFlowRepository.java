package com.samples.flironecamera.dialogflow;

import android.os.Handler;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.dialogflow.v2.DetectIntentRequest;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.SessionsSettings;
import com.google.cloud.dialogflow.v2.TextInput;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Executor;

public class DialogFlowRepository {
    private final Executor executor;
    private final Handler handler;

    private final SessionsClient sessionsClient;
    private final SessionName sessionName;

    public DialogFlowRepository(Executor executor, Handler handler, GoogleCredentials credentials) throws IOException {
        this.executor = executor;
        this.handler = handler;

        String projectId = ((ServiceAccountCredentials) credentials).getProjectId();

        SessionsSettings.Builder settingsBuilder = SessionsSettings.newBuilder();
        SessionsSettings sessionsSettings = settingsBuilder.setCredentialsProvider(
                FixedCredentialsProvider.create(credentials)).build();
        sessionsClient = SessionsClient.create(sessionsSettings);
        sessionName = SessionName.of(projectId, UUID.randomUUID().toString());
    }

    public void makeSendMessageRequest(final String message, final RepositoryCallback<DetectIntentResponse> callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final QueryInput queryInput = QueryInput.newBuilder()
                            .setText(TextInput.newBuilder().setText(message).setLanguageCode("en-US")).build();
                    DetectIntentRequest detectIntentRequest =
                            DetectIntentRequest.newBuilder()
                                    .setSession(sessionName.toString())
                                    .setQueryInput(queryInput)
                                    .build();

                    Result<DetectIntentResponse> result = new Result.Success<>(sessionsClient.detectIntent(detectIntentRequest));
                    notifyResult(result, callback, handler);
                } catch (Exception e) {
                    Result<DetectIntentResponse> errorResult = new Result.Error<>(e);
                    notifyResult(errorResult, callback, handler);
                }
            }
        });
    }

    private void notifyResult(
            final Result<DetectIntentResponse> response,
            final RepositoryCallback<DetectIntentResponse> callback,
            final Handler resultHandler
    ) {
        resultHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onComplete(response);
            }
        });
    }

}
