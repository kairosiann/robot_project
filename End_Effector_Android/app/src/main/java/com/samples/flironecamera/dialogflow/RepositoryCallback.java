package com.samples.flironecamera.dialogflow;

public interface RepositoryCallback<T> {
    void onComplete(Result<T> result);
}