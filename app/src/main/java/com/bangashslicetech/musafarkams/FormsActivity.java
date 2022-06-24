package com.bangashslicetech.musafarkams;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;

public class FormsActivity extends AppCompatActivity implements LocationListener {

    private AutoCompleteTextView type, subType;
    private EditText input_visit_date, input_organization_name, input_address, input_contact, input_owner_name,
            input_owner_contact, input_owner_email, input_poc_name, input_poc_contact, input_remarks,
            input_last_mon_sale_report;

    private static final long LOCATION_UPDATE_INTERVAL = 101;
    private static final long LOCATION_UPDATE_FASTEST_INTERVAL = 102;
    private static final int REQUEST_CODE_CHECK_SETTINGS = 103;

    private String typeSelection="", subTypeSelection="";

    private Button btn_submit;

    private String visit_Date, organization, add, cont_no, owner, owner_cont, owner_email,
            poc_name, poc_cont, remarks, last_mon_report, latitude, longitude;

    private AsyncHttpClient client;
    ShowNow showNow;
    SharedPreferences pref;

    String token, personID;

    private LinearLayout form1, form2, form3;

    private EditText input_airlineName, input_percentage, input_supplierName;
    private String airlineName, percentage, supplierName;
    private Button btn_submit_two;
    private Button getBtn_submit_three;
    private EditText upload_img;

    private String lastVisitInsertedID;

    public static final int PICK_IMG = -1;
    public static final int GALLERY_REQUEST = 10;


    List<File> filesList = new ArrayList<>();

    LottieAnimationView lottieAnimationView;
    Button btnOK;
    TextView headingTxt, msgTxt;

    TextView btnLogout;
    EditText input_long, input_lat;

    Location location;
    private LocationManager locationManager;
    String provider;

    private Button btn_next_Screen;
    private String uid, name;
    @Override
    public void onBackPressed() {
        startActivity(new Intent(FormsActivity.this, VisitUser.class));
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forms);
        initViews();
        grantLocPermission();
        locationManager = (LocationManager) getSystemService(
                Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        uid = pref.getString("user_id", "No Data");
        name = pref.getString("user_name", "No Data");

        provider = locationManager.getBestProvider(criteria, false);
        if (provider != null && !provider.equals("")) {
            if (!provider.contains("gps")) {
                final Intent poke = new Intent();
                poke.setClassName("com.android.settings",
                        "com.android.settings.widget.SettingsAppWidgetProvider");
                poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
                poke.setData(Uri.parse("3"));
                sendBroadcast(poke);
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 0, this);

            if (location != null)
                onLocationChanged(location);
            else
                location = locationManager.getLastKnownLocation(provider);
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 2, this);
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        token = pref.getString("token", "No Data");
        personID = pref.getString("user_id", "No Data");
        ArrayAdapter<String> arrayAdapternew = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, getResources()
                .getStringArray(R.array.Type_values));
        type.setAdapter(arrayAdapternew);
        type.setCursorVisible(false);
        type.setOnItemClickListener((adapterView, view, i, l) -> {
            type.showDropDown();
            typeSelection = (String) adapterView.getItemAtPosition(i);
        });
        type.setOnClickListener(view -> type.showDropDown());
        ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, getResources()
                .getStringArray(R.array.SubType_values));
        subType.setAdapter(arrayAdapter2);
        subType.setCursorVisible(false);
        subType.setOnItemClickListener((adapterView, view, i, l) -> {
            subType.showDropDown();
            subTypeSelection = (String) adapterView.getItemAtPosition(i);
        });
        subType.setOnClickListener(view -> subType.showDropDown());

        btn_submit.setOnClickListener(view -> {
            visit_Date = input_visit_date.getText().toString();
            organization = input_organization_name.getText().toString();
            add = input_address.getText().toString();
            cont_no = input_contact.getText().toString();
            owner = input_owner_name.getText().toString();
            owner_cont = input_owner_contact.getText().toString();
            owner_email = input_owner_email.getText().toString().trim();
            poc_name = input_poc_name.getText().toString();
            poc_cont = input_poc_contact.getText().toString();
            remarks = input_remarks.getText().toString();
            last_mon_report = input_last_mon_sale_report.getText().toString();

            if (longitude.isEmpty() || latitude.isEmpty()) {
                String msg = "Location missing. Turn your location ON";
                showErrorDialog(view, msg);
            }
            else if(typeSelection.isEmpty()){
                String msg = "You haven't selected Type";
                showErrorDialog(view, msg);
            }
            else if(subTypeSelection.isEmpty()){
                String msg = "You haven't selected subType";
                showErrorDialog(view, msg);
            }
            else if (TextUtils.isEmpty(visit_Date)) {
                String msg = "You haven't selected date";
                showErrorDialog(view, msg);
            } else if (TextUtils.isEmpty(organization)) {
                String msg = "Please enter organization name";
                showErrorDialog(view, msg);
            } else if (TextUtils.isEmpty(add)) {
                String msg = "Please enter address";
                showErrorDialog(view, msg);
            } else if (TextUtils.isEmpty(cont_no)||(cont_no.toCharArray().length<11)) {
                String msg = "Please enter valid contact number";
                showErrorDialog(view, msg);
            } else if (TextUtils.isEmpty(owner)) {
                String msg = "Please enter owner name";
                showErrorDialog(view, msg);
            } else if (TextUtils.isEmpty(owner_cont)||(owner_cont.toCharArray().length<11)) {
                String msg = "Please enter valid owner contact number";
                showErrorDialog(view, msg);
            } else if (TextUtils.isEmpty(owner_email)) {
                String msg = "Owner email may not left empty";
                showErrorDialog(view, msg);
            } else if (TextUtils.isEmpty(poc_name)) {
                String msg = "Please enter POC name";
                showErrorDialog(view, msg);
            } else if (TextUtils.isEmpty(poc_cont)||(poc_cont.toCharArray().length<11)) {
                String msg = "Please enter valid POC contact number";
                showErrorDialog(view, msg);
            } else if (TextUtils.isEmpty(remarks)) {
                String msg = "Please enter remarks";
                showErrorDialog(view, msg);
            } else if (TextUtils.isEmpty(last_mon_report)) {
                String msg = "Please enter last month sale report";
                showErrorDialog(view, msg);
            } else {
                if (NetworkUtils.isNetworkConnected(FormsActivity.this)) {
                    callApi();
                } else {
                    Toast.makeText(FormsActivity.this, "Check your internet", Toast.LENGTH_SHORT).show();
                }
            }

        });

        input_visit_date.setOnClickListener(view -> {
            Calendar mCurrentDate = Calendar.getInstance();
            int mYear = mCurrentDate.get(Calendar.YEAR);
            int mMonth = mCurrentDate.get(Calendar.MONTH);
            int mDay = mCurrentDate.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog mDatePicker;
            mDatePicker = new DatePickerDialog(FormsActivity.this, R.style.my_dialog_theme,
                    (datePicker, selectedYear, selectedmonth, selectedDay) -> {
                        selectedmonth = selectedmonth + 1;
                        input_visit_date.setText("" + selectedDay + "/" + selectedmonth + "/" + selectedYear);
                    }, mYear, mMonth, mDay);
            mDatePicker.setTitle("Select Date");
            mDatePicker.show();
        });

        btn_submit_two.setOnClickListener(view -> {
            airlineName = input_airlineName.getText().toString();
            percentage = input_percentage.getText().toString();
            supplierName = input_supplierName.getText().toString();

            if (TextUtils.isEmpty(airlineName)) {
                String msg = "You must enter airline name";
                showErrorDialog(view, msg);
            } else if (TextUtils.isEmpty(percentage)) {
                String msg = "You must enter percentage";
                showErrorDialog(view, msg);
            } else if (TextUtils.isEmpty(supplierName)) {
                String msg = "You must enter supplier name";
                showErrorDialog(view, msg);
            } else {
                if (NetworkUtils.isNetworkConnected(FormsActivity.this)) {
                    callApiForm2();
                } else {
                    Toast.makeText(FormsActivity.this, "Check your internet", Toast.LENGTH_SHORT).show();
                }
            }
        });

        upload_img.setOnClickListener(view -> pickImage());

        getBtn_submit_three.setOnClickListener(view -> callApiForm3());

        btnLogout.setOnClickListener(view -> {
            showNow.displayPositiveToast("You have logout");
            SharedPreferences.Editor editor = pref.edit();
            editor.clear();
            editor.apply();
            startActivity(new Intent(FormsActivity.this, MainActivity.class));
            finish();
        });
        Button btn_previous = findViewById(R.id.btn_previous_three);
        btn_previous.setOnClickListener(view->{
            form1.setVisibility(View.GONE);
            form3.setVisibility(View.GONE);
            form2.setVisibility(View.VISIBLE);
        });
        btn_next_Screen.setOnClickListener(view -> {
            form2.setVisibility(View.GONE);
            form3.setVisibility(View.VISIBLE);
        });
    }

    private void grantLocPermission() {
        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(LOCATION_UPDATE_INTERVAL)
                .setFastestInterval(LOCATION_UPDATE_FASTEST_INTERVAL)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        LocationServices
                .getSettingsClient(this)
                .checkLocationSettings(builder.build())
                .addOnSuccessListener(this, (LocationSettingsResponse response) -> {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 0, this);
                })
                .addOnFailureListener(this, ex -> {
                    if (ex instanceof ResolvableApiException) {
                        try {
                            ResolvableApiException resolvable = (ResolvableApiException) ex;
                            resolvable.startResolutionForResult(FormsActivity.this,
                                    REQUEST_CODE_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException ignored) {
                        }
                    }
                });
    }

    private void showErrorDialog(View view, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(FormsActivity.this);
        ViewGroup viewGroup = findViewById(R.id.content);
        View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.customview,
                viewGroup, false);
        builder.setView(dialogView);
        lottieAnimationView = dialogView.findViewById(R.id.animationView);
        btnOK = dialogView.findViewById(R.id.buttonOk);
        headingTxt = dialogView.findViewById(R.id.headingtxt);
        msgTxt = dialogView.findViewById(R.id.msgtxt);

        lottieAnimationView.setAnimation(R.raw.error);
        msgTxt.setText(msg);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        btnOK.setOnClickListener(view1 -> alertDialog.dismiss());
    }

    private void pickImage() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        photoPickerIntent.setType("image/*");
        photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(photoPickerIntent, "Pictures"), GALLERY_REQUEST);
    }

    private void callApiForm2() {
        RequestParams jsonParams = new RequestParams();
        jsonParams.put("_token", token);
        jsonParams.put("kams_visit_id", lastVisitInsertedID);
        jsonParams.put("airlineName", airlineName);
        jsonParams.put("percentage", percentage);
        jsonParams.put("supplierName", supplierName);

        getClient().post(Constants.KAMS_VISIT_DETAIL, jsonParams, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                showNow.showLoadingDialog(FormsActivity.this);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                showNow.scheduleDismiss();
                showNow.displayPositiveToast("Data submitted");

                btn_next_Screen.setEnabled(true);
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
        jsonParams.put("latitude", latitude);
        jsonParams.put("longitude", longitude);
        jsonParams.put("type", typeSelection);
        jsonParams.put("subType", subTypeSelection);
        jsonParams.put("visitDate", visit_Date);
        jsonParams.put("organizationName", organization);
        jsonParams.put("address", add);
        jsonParams.put("contactNo", cont_no);
        jsonParams.put("ownerName", owner);
        jsonParams.put("ownerContact", owner_cont);
        jsonParams.put("owenerEmail", owner_email);
        jsonParams.put("POCName", poc_name);
        jsonParams.put("POCContact", poc_cont);
        jsonParams.put("remarks", remarks);
        jsonParams.put("lastMonthSaleReport", last_mon_report);
        getClient().post(Constants.KAMS_VISIT, jsonParams, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                showNow.showLoadingDialog(FormsActivity.this);
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String json = new String(responseBody);
                showNow.scheduleDismiss();
                try {
                    JSONObject object = new JSONObject(json);
                    lastVisitInsertedID = object.getString("lastVisitInsertedID");
                    showNow.displayPositiveToast("Data submitted");

                    Log.e("lastVisitInsertedID: ", lastVisitInsertedID.toString());

                    //firebase analytics
                    FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(FormsActivity.this);
                    Bundle params = new Bundle();
                    Date date = new Date();
                    params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Text");
                    params.putString("id", uid);
                    params.putString("name", name);
                    params.putString("date", date.toString());
                    mFirebaseAnalytics.logEvent("Report_First", params);

                    Log.e("FIREBASE: ",params.toString());

                    form1.setVisibility(View.GONE);
                    form2.setVisibility(View.VISIBLE);
                } catch (JSONException e) {
                    e.printStackTrace();
                    showNow.displayErrorToast(e.getMessage());
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

    private void callApiForm3() {
        File[] files = new File[filesList.size()];
        if(files.length==0){
            showNow.displayErrorToast("Please select at least one file");
            return;
        }
        for (int i = 0; i < filesList.size(); i++) {
            files[i] = filesList.get(i);
        }
        RequestParams jsonParams = new RequestParams();
        jsonParams.put("_token", token);
        jsonParams.put("kams_visit_id", lastVisitInsertedID);
        try {
            jsonParams.put("document", files);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Log.e("IMAGE_DATA: ",jsonParams.toString());

        getClient().post(Constants.KAMS_VISIT_DOC, jsonParams, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                showNow.showLoadingDialog(FormsActivity.this);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                showNow.scheduleDismiss();
                showNow.displayPositiveToast("Data submitted");

                FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(FormsActivity.this);
                Bundle params = new Bundle();
                Date date = new Date();
                params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Text");
                params.putString("id", uid);
                params.putString("name", name);
                params.putString("date", date.toString());
                mFirebaseAnalytics.logEvent("Report_Submit", params);

                Toast.makeText(FormsActivity.this, "Data Submitted!!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(FormsActivity.this, VisitUser.class));
                finish();
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

    private void initViews() {
        type = findViewById(R.id.type);
        subType = findViewById(R.id.subType);
        input_visit_date = findViewById(R.id.input_visit_date);
        input_organization_name = findViewById(R.id.input_organization_name);
        input_address = findViewById(R.id.input_address);
        input_contact = findViewById(R.id.input_contact);
        input_owner_name = findViewById(R.id.input_owner_name);
        input_owner_contact = findViewById(R.id.input_owner_contact);
        input_owner_email = findViewById(R.id.input_owner_email);
        input_poc_name = findViewById(R.id.input_pocname);
        input_poc_contact = findViewById(R.id.input_poccontact);
        input_remarks = findViewById(R.id.input_remarks);
        input_last_mon_sale_report = findViewById(R.id.input_last_mon_sale_report);
        btn_next_Screen = findViewById(R.id.btn_next_Screen);
        btn_submit = findViewById(R.id.btn_submit);
        showNow = new ShowNow(this);

        form1 = findViewById(R.id.form1);
        form2 = findViewById(R.id.form2);
        form3 = findViewById(R.id.form3);

        input_airlineName = findViewById(R.id.input_airlineName);
        input_percentage = findViewById(R.id.input_percentage);
        input_supplierName = findViewById(R.id.input_supplierName);
        btn_submit_two = findViewById(R.id.btn_submit_two);
        getBtn_submit_three = findViewById(R.id.btn_submit_three);
        upload_img = findViewById(R.id.upload_img);

        btnLogout = findViewById(R.id.btnlogout);

        input_long = findViewById(R.id.input_long);
        input_lat = findViewById(R.id.input_lat);
    }
    //hhh

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_REQUEST) {
                if (Objects.requireNonNull(data).getClipData() != null) {

                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        String imgPath;
                        imgPath = FileUtils.getPath(FormsActivity.this, data.getClipData().getItemAt(i).getUri());
                        if (imgPath != null) {
                            filesList.add(new File(imgPath));
                        }
                        upload_img.setText("Image Picked!");
                    }

                } else if (data.getData() != null) {
                    Uri imagePath = data.getData();
                    try {
                        String imgPath;
                        imgPath = FileUtils.getPath(FormsActivity.this, imagePath);
                        Log.e("FILES", imgPath);
                        assert imgPath != null;
                        filesList.add(new File(imgPath));

                        upload_img.setText("Image Picked!");

                    } catch (Exception ignored) {
                    }
                }
            }
        }
        if (REQUEST_CODE_CHECK_SETTINGS == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                myMethod();
            }
        }
    }

    private void myMethod() {
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        if (provider != null && !provider.equals("")) {
            if (!provider.contains("gps")) {
                final Intent poke = new Intent();
                poke.setClassName("com.android.settings",
                        "com.android.settings.widget.SettingsAppWidgetProvider");
                poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
                poke.setData(Uri.parse("3"));
                sendBroadcast(poke);
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 0, this);

            if (location != null)
                onLocationChanged(location);
            else
                location = locationManager.getLastKnownLocation(provider);
            if (location != null)
                onLocationChanged(location);

        } else {
            Toast.makeText(getBaseContext(), "No Provider Found",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == PICK_IMG)
                pickImage();
        }
    }
    @Override
    public void onLocationChanged(@NonNull Location location) {
        latitude = String.valueOf(location.getLatitude());
        longitude = String.valueOf(location.getLongitude());
        input_lat.setText(latitude);
        input_long.setText(longitude);
    }
    @Override
    public void onProviderEnabled(@NonNull String provider) {
    }
    @Override
    public void onProviderDisabled(@NonNull String provider) {
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
}