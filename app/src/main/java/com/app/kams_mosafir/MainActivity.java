package com.app.kams_mosafir;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {
    private EditText input_uname, input_pass;
    protected Button btn_login;
    String uname, pass;
    private AsyncHttpClient client;
    ShowNow showNow;
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        input_uname = findViewById(R.id.input_uname);
        input_pass = findViewById(R.id.input_pass);
        btn_login = findViewById(R.id.btn_login);
        showNow = new ShowNow(this);

        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        String uid = pref.getString("user_id", "No Data");
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }
        if (!uid.equals("No Data")) {
            goToNextScreen();
        }
        btn_login.setOnClickListener(view -> {
            uname = input_uname.getText().toString();
            pass = input_pass.getText().toString();
            if (TextUtils.isEmpty(uname)) {
                input_uname.setError("Username cannot be empty.");
            } else if (TextUtils.isEmpty(pass)) {
                input_pass.setError("Password cannot be empty.");
            } else {
                if (NetworkUtils.isNetworkConnected(MainActivity.this)) {
                    loginUser();
                } else {
                    Toast.makeText(MainActivity.this, "Check your internet",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void goToNextScreen() {
        Intent intent = new Intent(MainActivity.this, VisitUser.class);
        startActivity(intent);
        finish();
    }

    private AsyncHttpClient getClient() {
        if (client == null) {
            client = new AsyncHttpClient();
            client.setTimeout(46000);
            client.setConnectTimeout(40000);
            client.setResponseTimeout(40000);
        }
        return client;
    }

    private void loginUser() {
        RequestParams jsonParams = new RequestParams();
        jsonParams.put("email", uname);
        jsonParams.put("password", pass);
        getClient().post(Constants.LOGIN_URL, jsonParams, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                showNow.showLoadingDialog(MainActivity.this);
            }

            @SuppressLint("InvalidAnalyticsName")
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String json = new String(responseBody);
                showNow.scheduleDismiss();
                try {
                    JSONObject object = new JSONObject(json);
                    String token = object.getString("_token");
                    JSONObject jsonObject = object.getJSONObject("user_info");
                    String id = jsonObject.getString("id");
                    String name = jsonObject.getString("full_name");
                    editor = pref.edit();
                    editor.putString("user_id", id);
                    editor.putString("user_name", name);
                    editor.putString("token", token);
                    editor.commit();
                    showNow.displayPositiveToast("You are logged in");

                    FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(MainActivity.this);
                    Bundle params = new Bundle();
                    Date date = new Date();

                    params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Text");
                    params.putString("id", id);
                    params.putString("name", name);
                    params.putString("date", date.toString());
                    mFirebaseAnalytics.logEvent("login_user", params);

                    goToNextScreen();

                } catch (JSONException e) {
                    e.printStackTrace();
                    showNow.displayErrorToast("Invalid data found");
                    showNow.scheduleDismiss();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                showNow.displayErrorToast("Connection failed! Network Error");
                showNow.scheduleDismiss();
            }

            @Override
            public void onCancel() {
                super.onCancel();
                showNow.scheduleDismiss();
            }
        });
    }
}