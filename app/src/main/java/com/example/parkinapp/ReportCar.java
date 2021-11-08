package com.example.parkinapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ReportCar extends AppCompatActivity {

    ProgressDialog pd;
    SwipeRefreshLayout swipeRefreshLayout;
    TextView textCredit, textViewPlate, textViewPhone, textViewTime, textServedName;
    Button stop_car_id, park_car_id;
    int plateId;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_car);

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

        if (!SharedPrefManager.getInstance(this).isLoggedIn()) {
            finish();
            startActivity(new Intent(this, login.class));
        }

        textCredit = findViewById(R.id.textCredit);
        textCredit.setText("0");
        textViewPlate = findViewById(R.id.textViewPlate);
        textViewPlate.setText("-");
        textViewPhone = findViewById(R.id.textViewPhone);
        textViewPhone.setText("-");
        textViewTime = findViewById(R.id.textViewTime);
        textViewTime.setText("-");
        textServedName = findViewById(R.id.textServedName);
        textServedName.setText("-");
        stop_car_id = findViewById(R.id.stop_car_id);
        park_car_id = findViewById(R.id.park_car_id);

        Intent intent = getIntent();
        final String str = intent.getStringExtra("lplate");
        loadCars(str);

        stop_car_id.setOnClickListener(view -> {
            finish();
            startActivity(new Intent(this, ProceedPayment.class));
        });

        park_car_id.setOnClickListener(view -> {
            finish();
            startActivity(new Intent(this, ProceedPayment.class));
        });
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
        pd = new ProgressDialog(ReportCar.this);
        pd.setMessage("Please wait...");
        pd.setCancelable(false);
        pd.setIndeterminate(true);
        pd.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Urls.URL_CREDIT_CAR,
                response -> {
                    pd.dismiss();
                    try {
                        JSONObject object = new JSONObject(response);
                            //converting the string to json array object
                            JSONArray array = object.getJSONArray("cars");
                            JSONObject car = array.getJSONObject(0);

                        plateId = car.getInt("id");
                        textViewPlate.setText(car.getString("plate"));
                        textViewPhone.setText(car.getString("phone"));
                        textViewTime.setText(car.getString("time"));
                        textServedName.setText(car.getString("names"));
                        textCredit.setText(car.getString("credit"));
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

    private void addCar(String plate) {
        pd = new ProgressDialog(ReportCar.this);
        pd.setMessage("Checking...");
        pd.setCancelable(false);
        pd.setIndeterminate(true);
        final String lp = plate;
        final String ph = "";
        pd.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Urls.URL_REGISTER,
                response -> {
                    pd.dismiss();

                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(response);
                        //if no error in response
                        final SweetAlertDialog pDialog = new SweetAlertDialog(ReportCar.this, SweetAlertDialog.SUCCESS_TYPE);
                        pDialog.setTitleText("Done");
                        pDialog.setContentText("Click to finish");
                        pDialog.setConfirmText("Finish");
                        pDialog.setConfirmClickListener(sDialog -> {
                            Intent intent = new Intent(getApplicationContext(), LandingPage.class);
                            startActivity(intent);
                        });
                        pDialog.show();
                        pDialog.setCanceledOnTouchOutside(false);
                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), "Check Your Internet", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                User user = SharedPrefManager.getInstance(getApplicationContext()).getUser();
                params.put("id", String.valueOf(user.getId()));
                params.put("lplate", lp);
                params.put("phone", ph);
                params.put("allow", "1");
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(ReportCar.this);
        requestQueue.add(stringRequest);

    }
}