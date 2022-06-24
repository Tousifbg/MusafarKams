package com.bangashslicetech.musafarkams;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Date;

public class VisitUser extends AppCompatActivity {

    Button btn, report;
    SharedPreferences pref;
    String token, personID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_user);

        //SHARED PREFERENCES TO CHECK IF USER ID IS ALREADY SAVED
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        token = pref.getString("token", "No Data");
        personID = pref.getString("user_id", "No Data");
        Log.e("SHARED_PREF", token);

        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle params = new Bundle();


        params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Text");
        params.putString("abc", "xyz");
        mFirebaseAnalytics.logEvent("visit_user", params);

        btn = findViewById(R.id.visit_user_btn);
        report = findViewById(R.id.report_user_btn);
        Animation animationUtils = AnimationUtils.loadAnimation(this,R.anim.myanim);
        btn.startAnimation(animationUtils);
        report.startAnimation(animationUtils);
        btn.setOnClickListener(view -> {
            Intent intent = new Intent(VisitUser.this,FormsActivity.class);
            startActivity(intent);
            finish();
        });
        report.setOnClickListener(view->{
            Intent intent = new Intent(VisitUser.this,ReportActivity.class);
            startActivity(intent);
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
}