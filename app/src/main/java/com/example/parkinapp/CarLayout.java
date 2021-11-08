package com.example.parkinapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CarLayout extends AppCompatActivity {

    //a list to store all the products
    private List<CarData>carList;

    //the recyclerview
    RecyclerView recyclerView;
    private CarAdapter adapter;
    TextView emptyView;
    ProgressDialog pd;
    SwipeRefreshLayout swipeRefreshLayout;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_layout);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        swipeRefreshLayout = findViewById(R.id.swipe);
        swipeRefreshLayout.setOnRefreshListener(() -> new Handler().postDelayed(() -> {
            // Stop animation (This will be after 3 seconds)
            finish();
            startActivity(getIntent());
            swipeRefreshLayout.setRefreshing(false);
        }, 3000));

        swipeRefreshLayout.setColorSchemeColors(
                getResources().getColor(R.color.colorPrimary)
        );

        recyclerView = findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        carList = new ArrayList<>();
        adapter = new CarAdapter(carList, this);
        emptyView = findViewById(R.id.empty_view);

        String str = getIntent().getStringExtra("lplate");
        loadCars(str);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {// todo: goto back activity from here

            finish();
            Intent intent = new Intent(this, LandingPage.class);
            startActivity(intent);
            return true;
        }
        else if (item.getItemId() == R.id.action_logout) {
            LandingPage lp = new LandingPage();
            lp.logout();
                return true;
            }
        return super.onOptionsItemSelected(item);
    }

    private void loadCars(final String plate) {
        pd = new ProgressDialog(CarLayout.this);
        pd.setMessage("Please wait...");
        pd.setCancelable(false);
        pd.setIndeterminate(true);
        pd.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Urls.URL_CAR_LIST,
                response -> {
                    pd.dismiss();
                    try {
                        JSONObject object = new JSONObject(response);
                        if (object.getInt("rowNums") == 0) {
                            recyclerView.setVisibility(View.GONE);
                            emptyView.setVisibility(View.VISIBLE);
                            }
                            else {
                            recyclerView.setVisibility(View.VISIBLE);
                            emptyView.setVisibility(View.GONE);
                            //converting the string to json array object
                            JSONArray array = object.getJSONArray("cars");
                            JSONObject car = array.getJSONObject(0);

                                //adding the product to product list
                                CarData carlist = new CarData(
                                        car.getInt("id"),
                                        car.getString("plate"),
                                        car.getString("phone"),
                                        car.getString("time"),
                                        car.getString("timeout"),
                                        car.getString("names"),
                                        car.getString("hours_used"),
                                        car.getString("total_time"),
                                        car.getString("pay"),
                                        car.getString("cardetails")
                                );
                                carList.add(carlist);
                                recyclerView.setAdapter(adapter);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> pd.dismiss())
        {
            @Override
            protected Map<String, String> getParams () {
            Map<String, String> params = new HashMap<>();
            params.put("lplate", plate);
            return params;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

}