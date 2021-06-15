package com.bangashslicetech.musafarkams;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

public class VisitUser extends AppCompatActivity {

    Button btn;
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

        btn = findViewById(R.id.visit_user_btn);

        Animation animationUtils = AnimationUtils.loadAnimation(this,R.anim.myanim);
        btn.startAnimation(animationUtils);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VisitUser.this,FormsActivity.class);
                startActivity(intent);
            }
        });
    }
}