package test.com.test;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by gleecus on 11/17/17.
 */

public interface ApiService {

    @GET
    Call<List<ModelClass>> getList(@Url String url);
}
