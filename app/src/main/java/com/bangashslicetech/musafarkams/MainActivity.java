package com.bangashslicetech.musafarkams;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    private EditText input_uname,input_pass;
    private Button btn_login;

    String uname,pass;

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
        showNow=new ShowNow(this);

        //SHARED PREFERENCES TO CHECK IF USER ID IS ALREADY SAVED
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        String uid = pref.getString("user_id", "No Data");
        Log.e("SAVED_SHARED_PREF", uid);
        if (uid.equals("No Data")){

        }
        else {
            goToNextScreen();
        }

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uname = input_uname.getText().toString();
                pass = input_pass.getText().toString();

                if (TextUtils.isEmpty(uname))
                {
                    input_uname.setError("Username cannot be empty.");
                }
                else if (TextUtils.isEmpty(pass))
                {
                    input_pass.setError("Password cannot be empty.");
                }
                else {
                    if (NetworkUtils.isNetworkConnected(MainActivity.this)){
                        loginUser();
                    }
                    else {
                        Toast.makeText(MainActivity.this, "Check your internet",
                                Toast.LENGTH_SHORT).show();
                    }
                    }
                }
        });
    }

    private void goToNextScreen() {
        Intent intent = new Intent(MainActivity.this,VisitUser.class);
        startActivity(intent);

    }

    private AsyncHttpClient getClient(){
        if (client == null)
        {
            client = new AsyncHttpClient();
            client.setTimeout(46000);
            client.setConnectTimeout(40000); // default is 10 seconds, minimum is 1 second
            client.setResponseTimeout(40000);
        }

        return client;
    }

    private void loginUser() {
        RequestParams jsonParams = new RequestParams();

        jsonParams.put("email",uname);
        jsonParams.put("password",pass);

        getClient().post(Constants.LOGIN_URL, jsonParams, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                showNow.showLoadingDialog(MainActivity.this);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                String json = new String(responseBody);
                Log.e("RESPONSE", "onSuccess: " + json);
                showNow.scheduleDismiss();

                try {
                    JSONObject object=new JSONObject(json);
                    String token = object.getString("_token");
                    Log.e("TOKEN",token);

                    JSONObject jsonObject  = object.getJSONObject("user_info");
                    String id = jsonObject.getString("id");
                    String agency_id = jsonObject.getString("agency_id");
                    String full_name = jsonObject.getString("full_name");
                    String username = jsonObject.getString("username");
                    String email = jsonObject.getString("email");
                    String email_verified = jsonObject.getString("email_verified");
                    String dds_license = jsonObject.getString("dds_license");
                    String sales_report = jsonObject.getString("sales_report");
                    String bank_statement = jsonObject.getString("bank_statement");
                    Log.e("RESPONSE_DATA","id: " +id+ "\nagency_id: " +agency_id+ "\nfull_name: " +full_name+
                            "\nusername: " +username+ "\nemail: " +email+ "\nemail_verified: " +email_verified+ "\ndds_license: " +dds_license
                            + "\nsales_report: " +sales_report + "\nbank_statement: " +bank_statement);

                    editor = pref.edit();
                    editor.putString("user_id", id);
                    editor.putString("token", token);
                    Log.e("SHARED_OK", "ok");
                    editor.commit(); // commit changes
                    //progressBar.setVisibility(View.GONE);
                    showNow.desplayPositiveToast(MainActivity.this,"You are logged in");
                    Toast.makeText(MainActivity.this, "You are logged in",
                            Toast.LENGTH_SHORT).show();

                    goToNextScreen();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("RESPONSEERROR", e.getMessage());
                    showNow.desplayErrorToast(MainActivity.this,e.getMessage());
                    //progressBar.setVisibility(View.GONE);
                    showNow.scheduleDismiss();

                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String json = new String(responseBody);
                Log.e("REPONSE2", "onSuccess: " + json);
                showNow.desplayErrorToast(MainActivity.this,json);
                showNow.scheduleDismiss();

                /*if(callBack!=null){

                    callBack.responseCallback(false,"Status Code:"+statusCode+" Uknown Error Occured","");
                }*/
            }

            @Override
            public void onCancel() {
                super.onCancel();
                //progressBar.setVisibility(View.GONE);
                showNow.scheduleDismiss();

                /*if(callBack!=null){

                    callBack.responseCallback(false,"Request Cancelled","");
                }*/
            }
        });
    }
}