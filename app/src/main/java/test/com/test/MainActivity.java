package test.com.test;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    List<ModelClass> listItems = new ArrayList<>();
    TextView tvEntries,tvText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvText=(TextView)findViewById(R.id.tvText);
        tvEntries = (TextView)findViewById(R.id.tvEntries);

        getItems();

    }

    private void getItems(){
        String url = "https://jsonplaceholder.typicode.com/posts";
        ApiService apiService = ApiClient.createService(ApiService.class, this);
        Call<List<ModelClass>> call= apiService.getList(url);
        call.enqueue(new Callback<List<ModelClass>>() {
            @Override
            public void onResponse(Call<List<ModelClass>> call, Response<List<ModelClass>> response) {
                if(response.isSuccessful()){
                    listItems.addAll(response.body());
                    Log.e("MainActivity", "email list size:" + listItems.size());
                    tvEntries.setText(String.valueOf(listItems.size()));
                }
            }
            @Override
            public void onFailure(Call<List<ModelClass>> call, Throwable t) {

            }
        });
    }
}
