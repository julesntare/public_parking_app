package com.example.parkinapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ProceedPayment extends AppCompatActivity {
    EditText tel_input;
    TextView total_amount;
    LinearLayout cash_payment, momo_payment, airtel_payment;
    Button payment_btn, previous_btn;
    int payment_id = 1, parkId;
    String plate, cashToPay, total_hours;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proceed_payment);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        cash_payment = findViewById(R.id.cash_payment);
        momo_payment = findViewById(R.id.momo_payment);
        airtel_payment = findViewById(R.id.airtel_payment);
        total_amount = findViewById(R.id.total_amount);
        tel_input = findViewById(R.id.tel_input);
        previous_btn = findViewById(R.id.previous_btn);
        payment_btn = findViewById(R.id.payment_btn);
        parkId = getIntent().getIntExtra("park_id", 0);
        plate = getIntent().getStringExtra("plate");
        cashToPay = getIntent().getStringExtra("cash") + " RWF";
        total_hours = getIntent().getStringExtra("total_hours");
        int user = getIntent().getIntExtra("user", 0);
        total_amount.setText(cashToPay);

        momo_payment.setOnClickListener(view -> {
            payment_id = 2;
            momo_payment.setBackgroundResource(R.drawable.selected_rounded_corner);
            cash_payment.setBackgroundResource(R.drawable.rounded_corner);
            airtel_payment.setBackgroundResource(R.drawable.rounded_corner);
            tel_input.setVisibility(View.VISIBLE);
            tel_input.setText("");
        });

        airtel_payment.setOnClickListener(view -> {
            payment_id = 3;
            airtel_payment.setBackgroundResource(R.drawable.selected_rounded_corner);
            momo_payment.setBackgroundResource(R.drawable.rounded_corner);
            cash_payment.setBackgroundResource(R.drawable.rounded_corner);
            tel_input.setVisibility(View.VISIBLE);
            tel_input.setText("");
        });
        cash_payment.setOnClickListener(view -> {
            payment_id = 1;
            cash_payment.setBackgroundResource(R.drawable.selected_rounded_corner);
            momo_payment.setBackgroundResource(R.drawable.rounded_corner);
            airtel_payment.setBackgroundResource(R.drawable.rounded_corner);
            tel_input.setVisibility(View.GONE);
            tel_input.setText("");
        });

        previous_btn.setOnClickListener(view -> {
            Intent intent = new Intent(ProceedPayment.this, CarLayout.class);
            intent.putExtra("lplate", plate);
            startActivity(intent);
        });

        payment_btn.setOnClickListener(view -> {
            //validating cash input
            final SweetAlertDialog pDialog = new SweetAlertDialog(ProceedPayment.this, SweetAlertDialog.WARNING_TYPE);
            pDialog.setTitleText("Are you sure?");
            pDialog.setContentText("You won't be able to undo this!");
            pDialog.setConfirmText("Yes");
            pDialog.setConfirmClickListener(sDialog -> {
                sDialog.dismissWithAnimation();
                removeCar(parkId, tel_input.getText().toString(), getIntent().getStringExtra("cash"), total_hours, user, payment_id);
            });
            pDialog.setCancelButton("No", SweetAlertDialog::dismissWithAnimation);
            pDialog.show();
            pDialog.setCanceledOnTouchOutside(false);
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
            Intent intent = new Intent(this, CarLayout.class);
            intent.putExtra("lplate", plate);
            startActivity(intent);
            return true;
        }
        else if (item.getItemId() == R.id.action_logout) {
            LandingPage landingPage = new LandingPage();
            landingPage.logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void removeCar(int id, String phone, String pay, String total_hours, final int user, int payment_id) {
        final ProgressDialog pd = new ProgressDialog(ProceedPayment.this);
        pd.setMessage("Please Wait...");
        pd.setCancelable(false);
        pd.setIndeterminate(true);
        pd.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Urls.URL_CAR_LIST_REMOVE,
                response -> {
                try {
                    //converting response to json object
                    JSONObject obj;
                    obj = new JSONObject(response);

                    //if no error in response
                    if (!obj.getBoolean("error")) {
                        pd.dismiss();
                        final SweetAlertDialog pDialog = new SweetAlertDialog(ProceedPayment.this, SweetAlertDialog.SUCCESS_TYPE);
                        pDialog.setTitleText("Payment done Successfully");
                        pDialog.setContentText("Click to finish");
                        pDialog.setConfirmText("Finish");
                        pDialog.setConfirmClickListener(sDialog -> {
                            Intent intent= new Intent(getApplicationContext(), LandingPage.class);
                            startActivity(intent);
                        });
                        pDialog.show();
                        pDialog.setCanceledOnTouchOutside(false);
                    }
                } catch (JSONException e) {
                    pd.dismiss();
                    e.printStackTrace();
                }
                },
                error -> {
                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }) {
                @Override
                protected Map<String, String> getParams() {
                //creating request parameters
                HashMap<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(id));
                params.put("phone", String.valueOf(phone));
                params.put("total_hours", String.valueOf(total_hours));
                params.put("total_paid", String.valueOf(pay));
                params.put("served_by", String.valueOf(user));
                params.put("payment_mode", String.valueOf(payment_id));
                return params;
                }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}