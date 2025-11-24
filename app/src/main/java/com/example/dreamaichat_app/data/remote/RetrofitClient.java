package com.example.dreamaichat_app.data.remote;

import com.example.dreamaichat_app.data.remote.api.ApiService;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

/**
 * Retrofit 客户端
 * 
 * 这个类负责创建和管理网络请求的客户端
 * Retrofit 是一个用于 Android 和 Java 的类型安全的 HTTP 客户端
 */
public class RetrofitClient {
    
    /**
     * API 服务器的基础地址
     * 
     * 配置说明：
     * 1. 这里填写你的后端 API 服务器的完整地址
     * 2. 必须以 http:// 或 https:// 开头
     * 3. 地址末尾必须有斜杠 "/"
     * 
     * 示例：
     * - 本地开发：http://localhost:8080/ 或 http://10.0.2.2:8080/ (Android模拟器)
     * - 测试服务器：https://test-api.example.com/
     * - 生产服务器：https://api.example.com/
     * 
     * 注意：如果你还没有后端服务器，可以先使用模拟数据，或者使用公共测试 API
     */
    private static final String BASE_URL = "http://10.0.2.2:8080/"; // TODO: 替换为你的实际API地址
    
    private static RetrofitClient instance;
    private final ApiService apiService;
    
    /**
     * 私有构造函数，使用单例模式
     * 单例模式确保整个应用只有一个 Retrofit 客户端实例
     */
    private RetrofitClient() {
        // 创建日志拦截器，用于在开发时查看网络请求详情
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        // 设置日志级别：BODY 会打印完整的请求和响应信息
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        
        // 创建 OkHttpClient，这是 Retrofit 底层使用的 HTTP 客户端
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)  // 添加日志拦截器
            .connectTimeout(5, TimeUnit.SECONDS)  // 连接超时时间：5秒（开发环境快速失败，触发模拟登录）
            .readTimeout(5, TimeUnit.SECONDS)     // 读取超时时间：5秒
            .writeTimeout(5, TimeUnit.SECONDS)    // 写入超时时间：5秒
            .build();
        
        // 创建 Retrofit 实例
        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)  // 设置基础 URL
            .client(okHttpClient)  // 设置 HTTP 客户端
            .addConverterFactory(GsonConverterFactory.create())  // 添加 Gson 转换器，用于 JSON 解析
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())  // 添加 RxJava 适配器，用于异步处理
            .build();
        
        // 创建 API 服务接口的实现
        apiService = retrofit.create(ApiService.class);
    }
    
    /**
     * 获取 RetrofitClient 单例实例
     * 
     * 使用单例模式的好处：
     * 1. 确保整个应用只有一个网络客户端
     * 2. 节省内存和资源
     * 3. 统一管理网络配置
     */
    public static RetrofitClient getInstance() {
        if (instance == null) {
            synchronized (RetrofitClient.class) {
                if (instance == null) {
                    instance = new RetrofitClient();
                }
            }
        }
        return instance;
    }
    
    /**
     * 获取 API 服务接口
     * 
     * 这个方法返回 ApiService 接口的实现
     * 你可以在其他地方调用这个方法来获取 API 服务，然后调用具体的 API 方法
     */
    public ApiService getApiService() {
        return apiService;
    }
}

