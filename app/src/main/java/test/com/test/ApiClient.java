package test.com.test;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    public static final String API_BASE_URL ="https://jsonplaceholder.typicode.com";
    private static Retrofit retrofit = null;

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create());

    public static <S> S createService(Class<S> serviceClass, final Context ctx) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClient.addInterceptor(logging);
        httpClient.interceptors().add(new RequestInterceptor((ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE), ctx));
        Retrofit retrofit = builder.client(httpClient.connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build()).build();
        return retrofit.create(serviceClass);
    }

    public static Retrofit parseError() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().serializeNulls().create()))
                    .client(httpClient.build())
                    .build();
        }
        return retrofit;
    }

    public static class RequestInterceptor implements Interceptor {

        final ConnectivityManager connectivityManager;
        Context mCtx;

        // @Inject
        public RequestInterceptor(ConnectivityManager connectivityManager, Context ctx) {
            this.connectivityManager = connectivityManager;
            this.mCtx = ctx;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            if (!isConnected()) {
//                throw new OfflineException("no internet");
                ((Activity)mCtx).runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(mCtx, "Please check internet connection", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            Request request = chain.request();
            if (request.body() == null) {
                return chain.proceed(request);
            }
            Response response = chain.proceed(request);
            if (response.code() == 401 && mCtx != null) {
                LocalBroadcastManager.getInstance(mCtx).sendBroadcast(new Intent("unAuthorized"));
            } else if (response.code() == 500 && mCtx != null) {
                ((Activity)mCtx).runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(mCtx, "Internal Server Error", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            return response;
        }

        protected boolean isConnected() {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnectedOrConnecting();
        }
    }
}
