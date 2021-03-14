package com.samples.flironecamera.view.chatbot;

import androidx.lifecycle.MutableLiveData;

import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.samples.flironecamera.dialogflow.DialogFlowRepository;
import com.samples.flironecamera.dialogflow.RepositoryCallback;
import com.samples.flironecamera.dialogflow.Result;


public class ChatBotViewModel {
    private final DialogFlowRepository dialogFlowRepository;

    public MutableLiveData<DetectIntentResponse> detectIntentResponseMutableLiveData = new MutableLiveData<>();
    public MutableLiveData<Exception> exceptionMutableLiveData = new MutableLiveData<>();

    public ChatBotViewModel(DialogFlowRepository dialogFlowRepository) {
        this.dialogFlowRepository = dialogFlowRepository;
    }

    public void sendMessage(String message) {
        dialogFlowRepository.makeSendMessageRequest(message, new RepositoryCallback<DetectIntentResponse>() {
            @Override
            public void onComplete(Result<DetectIntentResponse> result) {
                if (result instanceof Result.Success) {
                    detectIntentResponseMutableLiveData.setValue(((Result.Success<DetectIntentResponse>) result).data);
                } else if (result instanceof Result.Error) {
                    exceptionMutableLiveData.setValue(((Result.Error<DetectIntentResponse>) result).exception);
                }
            }
        });
    }
}
