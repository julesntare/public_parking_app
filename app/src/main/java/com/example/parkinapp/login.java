package com.example.parkinapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

public class login extends AppCompatActivity {
    EditText editTextUsername, editTextPassword;
    TextView errorMsgBox;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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

        if (SharedPrefManager.getInstance(this).isLoggedIn()) {
            finish();
            startActivity(new Intent(this, LandingPage.class));
            return;
        }

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        errorMsgBox = findViewById(R.id.errorMsg);

        findViewById(R.id.buttonLogin).setOnClickListener(view -> userLogin());

        editTextPassword.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId== EditorInfo.IME_ACTION_DONE){
                userLogin();
            }
            return false;
        });
    }

    private void userLogin() {
        //first getting the values
        final String username = editTextUsername.getText().toString();
        final String password = editTextPassword.getText().toString();
        final ProgressDialog pd = new ProgressDialog(this);
        //validating inputs
        if (TextUtils.isEmpty(username)) {
            editTextUsername.setError("Please enter your username");
            editTextUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Please enter your password");
            editTextPassword.requestFocus();
            return;
        }
        pd.setMessage("Logging in...");
        pd.setCancelable(false);
        pd.setIndeterminate(true);
        pd.show();
        //if everything is fine
        //Start ProgressBar first (Set visibility VISIBLE)
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Urls.URL_LOGIN,
                response -> {
                    try {
                        //converting response to json object
                        JSONObject obj;
                        obj = new JSONObject(response);

                        //if no error in response
                        if (!obj.getBoolean("error")) {
                            pd.dismiss();
                            Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();

                            //getting the user from the response
                            JSONObject userJson = obj.getJSONObject("user");

                            //creating a new user object
                            User user = new User(
                                    userJson.getInt("id"),
                                    userJson.getString("username")
                            );

                            //storing the user in shared preferences
                            SharedPrefManager.getInstance(getApplicationContext()).userLogin(user);

                            //starting the Landing page activity
                            finish();
                            startActivity(new Intent(getApplicationContext(), LandingPage.class));
                        } else {
                            pd.dismiss();
                            errorMsgBox.setText("Invalid Username and Password");
                            Toast.makeText(getApplicationContext(), "Invalid username or password", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        pd.dismiss();
                        errorMsgBox.setText("Something Went Wrong!!!");
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                    errorMsgBox.setText("Something Went Wrong!!!");
                }) {
            @Override
            protected Map<String, String> getParams() {
                //creating request parameters
                HashMap<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("password", password);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
    }