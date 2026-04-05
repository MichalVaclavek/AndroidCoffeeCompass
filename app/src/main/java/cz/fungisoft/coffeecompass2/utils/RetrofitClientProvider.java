package cz.fungisoft.coffeecompass2.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Singleton provider of Retrofit instances keyed by base URL.
 * <p>
 * Replaces the pattern where every AsyncTask created its own OkHttpClient + Retrofit
 * on every call. Instances are cached and reused, which is both faster and uses
 * fewer resources (connection pool, thread pool, etc.).
 * <p>
 * Usage:
 * <pre>
 *   CoffeeSiteRESTInterface api = RetrofitClientProvider.getInstance()
 *       .getRetrofit(CoffeeSiteRESTInterface.COFFEESITE_API_PUBLIC_SEARCH_URL)
 *       .create(CoffeeSiteRESTInterface.class);
 * </pre>
 */
public final class RetrofitClientProvider {

    private static volatile RetrofitClientProvider instance;

    private final OkHttpClient client;
    private final Gson gson;
    private final Map<String, Retrofit> retrofitCache = new ConcurrentHashMap<>();

    private RetrofitClientProvider() {
        client = Utils.getOkHttpClientBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();

        gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .setDateFormat("dd. MM. yyyy HH:mm")
                .create();
    }

    public static RetrofitClientProvider getInstance() {
        if (instance == null) {
            synchronized (RetrofitClientProvider.class) {
                if (instance == null) {
                    instance = new RetrofitClientProvider();
                }
            }
        }
        return instance;
    }

    /**
     * Returns a Retrofit instance for the given base URL.
     * Instances are cached so that the same base URL always returns the same Retrofit object.
     *
     * @param baseUrl the base URL for the REST API
     * @return cached Retrofit instance
     */
    public Retrofit getRetrofit(String baseUrl) {
        return retrofitCache.computeIfAbsent(baseUrl, url ->
                new Retrofit.Builder()
                        .client(client)
                        .baseUrl(url)
                        .addConverterFactory(ScalarsConverterFactory.create())
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build()
        );
    }

    /**
     * Returns a Retrofit instance for the given base URL with an additional interceptor
     * (e.g. for Authorization headers). These are NOT cached since the interceptor
     * may change per request (different tokens, etc.).
     *
     * @param baseUrl     the base URL for the REST API
     * @param interceptor the interceptor to add (e.g. authorization header)
     * @return a new Retrofit instance with the interceptor
     */
    public Retrofit getRetrofitWithInterceptor(String baseUrl, Interceptor interceptor) {
        OkHttpClient authenticatedClient = client.newBuilder()
                .addInterceptor(interceptor)
                .build();

        return new Retrofit.Builder()
                .client(authenticatedClient)
                .baseUrl(baseUrl)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    /**
     * Returns the shared OkHttpClient instance, e.g. for use with Picasso
     * or other libraries that need it.
     */
    public OkHttpClient getClient() {
        return client;
    }

    /**
     * Returns the shared Gson instance.
     */
    public Gson getGson() {
        return gson;
    }
}
