package com.hofang.bookchainfe.network;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ChatAIApiService {
    
    @POST("/api/chatai/message")
    Call<ResponseBody> sendChatMessage(@Body RequestBody body);
    
    @POST("/api/chatai/image")
    Call<ResponseBody> sendChatImage(@Body RequestBody body);
    
    @POST("/api/chatai/search-book")
    Call<ResponseBody> searchBook(@Body RequestBody body);
}
