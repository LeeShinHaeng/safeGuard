package com.capstone.safeGuard.apis.speech.application;

import okhttp3.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class NaverSpeechService {

    //TODO ID, SECRET 변경 필요
    private static final String CLIENT_ID = "YOUR_CLIENT_ID"; // 네이버 클라우드 애플리케이션 클라이언트 ID
    private static final String CLIENT_SECRET = "YOUR_CLIENT_SECRET"; // 네이버 클라우드 애플리케이션 클라이언트 시크릿
    private static final String API_URL = "https://naveropenapi.apigw.ntruss.com/recog/v1/stt";

    public String transcribe(MultipartFile file) throws IOException {
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/octet-stream"), file.getBytes());

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("X-NCP-APIGW-API-KEY-ID", CLIENT_ID)
                .addHeader("X-NCP-APIGW-API-KEY", CLIENT_SECRET)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            return response.body().string();
        }
    }
}
