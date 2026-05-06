package com.example.docsachapp.api;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // NẾU DÙNG MÁY THẬT: Thay 10.0.2.2 bằng IP của máy tính (ví dụ: 192.168.1.x)
    // NẾU DÙNG MÁY ẢO: Giữ nguyên 10.0.2.2
     private static final String BASE_URL = "http://10.0.2.2:8000/api/";
    //private static final String BASE_URL = "http://172.36.65.219:8000/api/";
    // ✅ production
    //private static final String BASE_URL = "https://four9k14-2-nhom3-docsachapp.onrender.com";


    private static Retrofit retrofitInstance;

    public static Retrofit getInstance() {
        if (retrofitInstance == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(60, TimeUnit.SECONDS) // Tăng timeout
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .build();

            retrofitInstance = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitInstance;
    }

    public static ApiService getApi() {
        return getInstance().create(ApiService.class);
    }
}
