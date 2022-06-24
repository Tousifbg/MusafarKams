package com.app.kams_mosafir;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import cz.msebera.android.httpclient.Header;

public class ReportActivity extends AppCompatActivity {
    ListView listView;
    EditText startDate, endDate;
    int mMonth, mYear, mDay;
    DatePickerDialog mDatePicker;
    Button getReport;
    private AsyncHttpClient client;
    ShowNow showNow;
    SharedPreferences pref;
    String token, personID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        token = pref.getString("token", "No Data");
        personID = pref.getString("user_id", "No Data");

        getReport = findViewById(R.id.findReport);
        startDate = findViewById(R.id.start_date);
        endDate = findViewById(R.id.end_date);
        listView = findViewById(R.id.listView);
        Calendar mCurrentDate = Calendar.getInstance();
        mYear = mCurrentDate.get(Calendar.YEAR);
        mMonth = mCurrentDate.get(Calendar.MONTH);
        mDay = mCurrentDate.get(Calendar.DAY_OF_MONTH);
        startDate.setOnClickListener(view -> datePicker(1));
        endDate.setOnClickListener(view -> datePicker(2));

        getReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //callApi();
                Toast.makeText(ReportActivity.this, "No report for now", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @SuppressLint("SetTextI18n")
    private void datePicker(int a){
        mDatePicker = new DatePickerDialog(ReportActivity.this, R.style.my_dialog_theme,
                (datePicker, selectedYear, selectedmonth, selectedDay) -> {
                    selectedmonth = selectedmonth + 1;

                    if(a==1){
                        startDate.setText("" + selectedDay + "/" + selectedmonth + "/" + selectedYear);
                    }
                    if(a==2){
                        endDate.setText("" + selectedDay + "/" + selectedmonth + "/" + selectedYear);
                    }
                }, mYear, mMonth, mDay);
        mDatePicker.setTitle("Select Date");
        mDatePicker.show();
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

    private void callApi() {
        RequestParams jsonParams = new RequestParams();
        jsonParams.put("_token", token);
        jsonParams.put("id", personID);
        jsonParams.put("startDate", startDate.getText().toString());
        jsonParams.put("endDate", endDate.getText().toString());

        getClient().post(Constants.REPORT_URL, jsonParams, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                showNow.showLoadingDialog(ReportActivity.this);
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String json = new String(responseBody);
                showNow.scheduleDismiss();
                try {
                    JSONObject object = new JSONObject(json);
//                    lastVisitInsertedID = object.getString("lastVisitInsertedID");
                    showNow.displayPositiveToast("Data submitted");
//                    FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(FormsActivity.this);
//                    Bundle params = new Bundle();
//                    Date date = new Date();
//                    params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Text");
//                    params.putString("id", uid);
//                    params.putString("name", name);
//                    params.putString("date", date.toString());
//                    mFirebaseAnalytics.logEvent("Report_First", params);
//                    form1.setVisibility(View.GONE);
//                    form2.setVisibility(View.VISIBLE);
                } catch (JSONException e) {
                    e.printStackTrace();
                    showNow.displayErrorToast(e.getMessage());
                    showNow.scheduleDismiss();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String json = new String(responseBody);
                showNow.displayErrorToast(json);
                showNow.scheduleDismiss();
            }

            @Override
            public void onCancel() {
                super.onCancel();
                showNow.scheduleDismiss();
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}