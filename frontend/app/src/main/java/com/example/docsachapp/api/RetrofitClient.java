package com.example.docsachapp.api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    /**
     * CHÚ Ý VỀ ĐƯỜNG DẪN (BASE_URL):
     * 1. Nếu dùng Android Emulator (Máy ảo): Dùng "http://10.0.2.2:8000/api/"
     * 2. Nếu dùng Máy thật (Physical Device): Dùng "http://IP_CUA_MAY_TINH:8000/api/"
     *    (Ví dụ: http://192.168.1.5:8000/api/ - Máy tính và điện thoại phải cùng WiFi)
     * 3. Đảm bảo server Django đang chạy (python manage.py runserver 0.0.0.0:8000)
     */
    private static final String BASE_URL = "http://10.0.2.2:8000/api/";

    private static Retrofit retrofitInstance;

    public static Retrofit getInstance() {
        if (retrofitInstance == null) {
            // Logging để theo dõi request/response trong Logcat
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
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
