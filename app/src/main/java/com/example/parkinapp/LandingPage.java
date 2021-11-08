package com.example.parkinapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

public class LandingPage extends AppCompatActivity {
    EditText lPlate, phoneNumber;
    TextView errorMsgBox, postPaid;
    ProgressDialog pd;
    Button checkButton;
    TextView appTitle;
    SwipeRefreshLayout swipeRefreshLayout;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);

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

        lPlate = findViewById(R.id.license_plate_id);
        phoneNumber = findViewById(R.id.phone_number_id);
        checkButton = findViewById(R.id.entry_button_id);
        appTitle = findViewById(R.id.appTitle);
        phoneNumber.setVisibility(View.GONE);
        errorMsgBox = findViewById(R.id.errorMsg);
        errorMsgBox.setVisibility(View.GONE);
        postPaid = findViewById(R.id.postPaid);
        postPaid.setVisibility(View.GONE);

        lPlate.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId==EditorInfo.IME_ACTION_DONE){
                if (TextUtils.isEmpty(lPlate.getText().toString())) {
                    lPlate.setError("Please add license plate");
                    lPlate.requestFocus();
                }
                else if (checkButton.getText() == "Go") {
                    addCar(phoneNumber.getText().toString());
                }
                else {
                    addCar("null");
                }
            }
            return false;
        });

        phoneNumber.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId==EditorInfo.IME_ACTION_DONE){
                if (TextUtils.isEmpty(lPlate.getText().toString())) {
                    lPlate.setError("Please add license plate");
                    lPlate.requestFocus();
                }
                else if (checkButton.getText() == "Go") {
                    addCar(phoneNumber.getText().toString());
                }
                else {
                    addCar("null");
                }
            }
            return false;
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
            logout();
            return true;
        }
        else if (item.getItemId() == R.id.action_balance) {
            myBalance();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void myBalance() {
        LayoutInflater layoutInflater = LayoutInflater.from(LandingPage.this);
        View promptView = layoutInflater.inflate(R.layout.activity_balance_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(LandingPage.this);
        alertDialogBuilder.setView(promptView);

        final EditText editText = (EditText) promptView.findViewById(R.id.edit_balance);
        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("Proceed", (dialog, id) -> {

                })
                .setNegativeButton("Cancel",
                        (dialog, id) -> dialog.cancel());

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (editText.getText().toString().trim().equals("")) {
                editText.setError("Fill out this field");
                editText.requestFocus();
                return;
            }
            pd = new ProgressDialog(this);
            pd.setMessage("Please wait...");
            pd.setCancelable(false);
            pd.setIndeterminate(true);
            pd.show();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Urls.URL_CAR_SEND_BALANCE,
                    response -> {
                        pd.dismiss();
                        try {
                            //converting response to json object
                            JSONObject obj = new JSONObject(response);

                            //if no error in response
                            if (obj.getBoolean("error") && obj.getInt("status") == 1) {
                                final SweetAlertDialog pDialog = new SweetAlertDialog(LandingPage.this, SweetAlertDialog.ERROR_TYPE);
                                pDialog.setTitleText("Whoops!!");
                                pDialog.setContentText(obj.getString("message"));
                                pDialog.setConfirmText("Retry");
                                pDialog.setConfirmClickListener(sDialog -> pDialog.dismissWithAnimation());
                                pDialog.show();
                                pDialog.setCanceledOnTouchOutside(false);
                            }
                            else if(obj.getBoolean("error") && obj.getInt("status") == 2) {
                                final SweetAlertDialog pDialog = new SweetAlertDialog(LandingPage.this, SweetAlertDialog.ERROR_TYPE);
                                pDialog.setTitleText("Whoops!!");
                                pDialog.setContentText(obj.getString("message"));
                                pDialog.setConfirmText("Retry Later");
                                pDialog.setConfirmClickListener(sDialog -> {
                                    pDialog.dismissWithAnimation();
                                    alert.dismiss();
                                });
                                pDialog.show();
                                pDialog.setCanceledOnTouchOutside(false);
                            }
                            else {
                                final SweetAlertDialog pDialog = new SweetAlertDialog(LandingPage.this, SweetAlertDialog.SUCCESS_TYPE);
                                pDialog.setTitleText("Awesome");
                                pDialog.setContentText(obj.getString("message"));
                                pDialog.setConfirmText("Close now");
                                pDialog.setConfirmClickListener(sDialog -> {
                                    pDialog.dismissWithAnimation();
                                    alert.dismiss();
                                });
                                pDialog.show();
                                pDialog.setCanceledOnTouchOutside(false);
                            }
                        } catch (JSONException e) {
                            Toast.makeText(this, "Something went wrong!!!", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        Toast.makeText(this, "Something went wrong!!!", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    User user = SharedPrefManager.getInstance(getApplicationContext()).getUser();
                    params.put("user_id", String.valueOf(user.getId()));
                    params.put("balance", editText.getText().toString());
                    return params;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(stringRequest);
        });
    }

    public void checkBtn(View view) {
            if (TextUtils.isEmpty(lPlate.getText().toString())) {
                lPlate.setError("Please add license plate");
                lPlate.requestFocus();
                return;
            }
            if (checkButton.getText() == "Go") {
                addCar(phoneNumber.getText().toString());
            }
            else {
                addCar("null");
            }
        }

    public void logout() {
        pd = new ProgressDialog(LandingPage.this);
        pd.setMessage("Logging out...");
        pd.setCancelable(false);
        pd.setIndeterminate(true);
        pd.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Urls.URL_LOGOUT,
                response -> {
                    pd.dismiss();
                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(response);

                        //if no error in response
                        if (!obj.getBoolean("error")) {
                            SharedPreferences sp=getSharedPreferences("parkingapp",MODE_PRIVATE);
                            SharedPreferences.Editor e=sp.edit();
                            e.clear();
                            e.apply();
                            Intent log_out = new Intent(getApplicationContext(), login.class);
                            startActivity(log_out);
                            finish();
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Something went wrong!!!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), "Something went wrong!!!", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(getApplicationContext(), "Something went wrong!!!", Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                User user = SharedPrefManager.getInstance(getApplicationContext()).getUser();
                params.put("id", String.valueOf(user.getId()));
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    private void addCar(String s) {
        pd = new ProgressDialog(LandingPage.this);
        pd.setMessage("Checking...");
        pd.setCancelable(false);
        pd.setIndeterminate(true);
        final String plate = lPlate.getText().toString();
        final String phone = s;
        errorMsgBox.setVisibility(View.GONE);
        pd.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Urls.URL_REGISTER,
                response -> {
                    pd.dismiss();
                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(response);
                        //if no error in response
                        if(obj.has("status") && obj.getString("status").equals("1")) {
                            Intent intent = new Intent(getApplicationContext(), ReportCar.class);
                            intent.putExtra("lplate", obj.getString("park_id"));
                            startActivity(intent);
                        }
                        else if (!obj.getBoolean("error") && obj.getString("message").equals("add phone")) {
                            phoneNumber.setVisibility(View.VISIBLE);
                            phoneNumber.setText("");
                            phoneNumber.setFocusableInTouchMode(true);
                            phoneNumber.requestFocus();
                            appTitle.setText("Add Phone Number");
                            postPaid.setText(obj.getString("class"));
                            postPaid.setVisibility(View.VISIBLE);
                            checkButton.setText("Go");
                        }
                        else if (checkButton.getText() == "Go") {
                            final SweetAlertDialog pDialog = new SweetAlertDialog(LandingPage.this, SweetAlertDialog.SUCCESS_TYPE);
                            pDialog.setTitleText("Done");
                            pDialog.setContentText("Click to finish");
                            pDialog.setConfirmText("Finish");
                            pDialog.setConfirmClickListener(sDialog -> {
                                appTitle.setText("Add License Plate");
                                phoneNumber.setVisibility(View.GONE);
                                lPlate.setText("");
                                phoneNumber.setText("");
                                checkButton.setText("Check");
                                pDialog.dismissWithAnimation();
                            });
                            pDialog.show();
                            pDialog.setCanceledOnTouchOutside(false);
                        }
                        else {
                            Intent intent = new Intent(getApplicationContext(), CarLayout.class);
                            intent.putExtra("lplate", plate);
                            startActivity(intent);
                        }
                    } catch (JSONException e) {
                        errorMsgBox.setVisibility(View.VISIBLE);
                        errorMsgBox.setText("Something Went Wrong!!!");
                        e.printStackTrace();
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
                params.put("lplate", plate);
                params.put("phone", phone);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

}