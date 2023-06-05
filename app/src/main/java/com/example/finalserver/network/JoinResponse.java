package com.example.finalserver.network;

import com.google.gson.annotations.SerializedName;

public class JoinResponse {
    @SerializedName("message")
    public String message;

    public String getMessage(){
        return message;
    }
}
