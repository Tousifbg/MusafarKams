package com.bangashslicetech.musafarkams;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class FormsActivity extends AppCompatActivity implements LocationListener{

    private AutoCompleteTextView type, subType;
    private EditText input_visit_date, input_organization_name, input_address, input_contact, input_owner_name,
            input_owner_contact, input_owner_email, input_pocname, input_poccontact, input_remarks,
            input_last_mon_sale_report;

    private String typeSelection, subTypeSelection;

    private Button btn_submit;

    private String visit_Date, organization, add, cont_no, owner, owner_cont, owner_email,
            poc_name, poc_cont, remarks, last_mon_report, latitude, longitude;

    private AsyncHttpClient client;
    ShowNow showNow;
    SharedPreferences pref;

    String token, personID;

    private LocationManager locationManager;
    String provider;

    private LinearLayout form1, form2, form3;

    private EditText input_airlineName, input_percentage, input_supplierName;
    private String airlineName, percentage, supplierName;
    private Button btn_submit_two;
    private Button getBtn_submit_three;
    private EditText upload_img;

    private String lastVisitInsertedID;

    public static final int PICK_IMG = -1;
    public static final int GALLERY_REQUEST = 10;

    Uri uri;
    String filePath;
    FileUtils fileUtils;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forms);

        initViews();

        statusCheck();
        grantLocationPermission();

        locationManager = (LocationManager) getSystemService(
                Context.LOCATION_SERVICE);

        // Creating an empty criteria object
        Criteria criteria = new Criteria();

        // Getting the name of the provider that meets the criteria
        provider = locationManager.getBestProvider(criteria, false);

        if (provider != null && !provider.equals("")) {
            if (!provider.contains("gps")) { // if gps is disabled
                final Intent poke = new Intent();
                poke.setClassName("com.android.settings",
                        "com.android.settings.widget.SettingsAppWidgetProvider");
                poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
                poke.setData(Uri.parse("3"));
                sendBroadcast(poke);
            }
            // Get the location from the given provider
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 0, this);

            if (location != null)
                onLocationChanged(location);
            else
                location = locationManager.getLastKnownLocation(provider);
            if (location != null)
                onLocationChanged(location);
            else

                Toast.makeText(getBaseContext(), "Location can't be retrieved",
                        Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(getBaseContext(), "No Provider Found",
                    Toast.LENGTH_SHORT).show();
        }

        Log.e("LAT", latitude + "LONG" + longitude);

        //SHARED PREFERENCES TO CHECK IF USER ID IS ALREADY SAVED
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        token = pref.getString("token", "No Data");
        personID = pref.getString("user_id", "No Data");
        Log.e("SHARED_PREF", token);

        hasStoragePermission(1);

        // CUSTOM AUTOCOMPLETE TEXTVIEW FOR GENDER SPINNER
        //final ImageView delButton = findViewById(R.id.delButton);
        ArrayAdapter<String> arrayAdapternew = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, getResources()
                .getStringArray(R.array.Type_values));
        type.setAdapter(arrayAdapternew);
        type.setCursorVisible(false);
        type.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                type.showDropDown();
                typeSelection = (String) adapterView.getItemAtPosition(i);
                Toast.makeText(FormsActivity.this, typeSelection,
                        Toast.LENGTH_SHORT).show();
            }
        });
        type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                type.showDropDown();
            }
        });

        // CUSTOM AUTOCOMPLETE TEXTVIEW FOR GENDER SPINNER
        //final ImageView delButton = findViewById(R.id.delButton);
        ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, getResources()
                .getStringArray(R.array.SubType_values));
        subType.setAdapter(arrayAdapter2);
        subType.setCursorVisible(false);
        subType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                subType.showDropDown();
                subTypeSelection = (String) adapterView.getItemAtPosition(i);
                Toast.makeText(FormsActivity.this, subTypeSelection,
                        Toast.LENGTH_SHORT).show();
            }
        });
        subType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subType.showDropDown();
            }
        });

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                visit_Date = input_visit_date.getText().toString();
                organization = input_organization_name.getText().toString();
                add = input_address.getText().toString();
                cont_no = input_contact.getText().toString();
                owner = input_owner_name.getText().toString();
                owner_cont = input_owner_contact.getText().toString();
                owner_email = input_owner_email.getText().toString();
                poc_name = input_pocname.getText().toString();
                poc_cont = input_poccontact.getText().toString();
                remarks = input_remarks.getText().toString();
                last_mon_report = input_last_mon_sale_report.getText().toString();

                if (TextUtils.isEmpty(visit_Date)) {
                    input_visit_date.setError("This field can't be empty");
                } else if (TextUtils.isEmpty(organization)) {
                    input_organization_name.setError("This field can't be empty");
                } else if (TextUtils.isEmpty(add)) {
                    input_address.setError("This field can't be empty");
                } else if (TextUtils.isEmpty(cont_no)) {
                    input_contact.setError("This field can't be empty");
                } else if (TextUtils.isEmpty(owner)) {
                    input_owner_name.setError("This field can't be empty");
                } else if (TextUtils.isEmpty(owner_cont)) {
                    input_owner_contact.setError("This field can't be empty");
                } else if (TextUtils.isEmpty(owner_email)) {
                    input_owner_email.setError("This field can't be empty");
                } else if (TextUtils.isEmpty(poc_name)) {
                    input_pocname.setError("This field can't be empty");
                } else if (TextUtils.isEmpty(poc_cont)) {
                    input_poccontact.setError("This field can't be empty");
                } else if (TextUtils.isEmpty(remarks)) {
                    input_remarks.setError("This field can't be empty");
                } else if (TextUtils.isEmpty(last_mon_report)) {
                    input_last_mon_sale_report.setError("This field can't be empty");
                } else {
                    if (NetworkUtils.isNetworkConnected(FormsActivity.this)) {
                        callApi();
                    } else {
                        Toast.makeText(FormsActivity.this, "Check your internet", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

        input_visit_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //To show current date in the datepicker
                Calendar mcurrentDate = Calendar.getInstance();
                int mYear = mcurrentDate.get(Calendar.YEAR);
                int mMonth = mcurrentDate.get(Calendar.MONTH);
                int mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog mDatePicker;
                mDatePicker = new DatePickerDialog(FormsActivity.this,R.style.my_dialog_theme,
                        new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                        // TODO Auto-generated method stub
                        /*      Your code   to get date and time    */
                        selectedmonth = selectedmonth + 1;
                        input_visit_date.setText("" + selectedday + "/" + selectedmonth + "/" + selectedyear);
                    }
                }, mYear, mMonth, mDay);
                mDatePicker.setTitle("Select Date");
                mDatePicker.show();
            }
        });

        btn_submit_two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                airlineName = input_airlineName.getText().toString();
                percentage = input_percentage.getText().toString();
                supplierName = input_supplierName.getText().toString();

                if (TextUtils.isEmpty(airlineName)) {
                    input_airlineName.setError("This field can't be empty");
                } else if (TextUtils.isEmpty(percentage)) {
                    input_percentage.setError("This field can't be empty");
                } else if (TextUtils.isEmpty(supplierName)) {
                    input_supplierName.setError("This field can't be empty");
                } else {
                    if (NetworkUtils.isNetworkConnected(FormsActivity.this)) {
                        callApiForm2();
                    } else {
                        Toast.makeText(FormsActivity.this, "Check your internet", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        upload_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImage();
            }
        });

        getBtn_submit_three.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callApiForm3();
            }
        });

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

        Log.e("JSONPARAMS2", jsonParams.toString());

        getClient().post(Constants.KAMS_VISIT_DETAIL, jsonParams, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                showNow.showLoadingDialog(FormsActivity.this);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                String json = new String(responseBody);
                Log.e("RESPONSE", "onSuccess: " + json);
                showNow.scheduleDismiss();
                showNow.desplayPositiveToast(FormsActivity.this, "Data submitted");

                form2.setVisibility(View.GONE);
                form3.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String json = new String(responseBody);
                Log.e("REPONSE2", "onSuccess: " + json);
                showNow.desplayErrorToast(FormsActivity.this, json);
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

    private void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(
                Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(
                "Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false).setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog,
                                        final int id) {
                        startActivity(new Intent(
                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog,
                                        final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void grantLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        }
    }

    private AsyncHttpClient getClient() {
        if (client == null) {
            client = new AsyncHttpClient();
            client.setTimeout(46000);
            client.setConnectTimeout(40000); // default is 10 seconds, minimum is 1 second
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
        Log.e("JSONPARAMS", jsonParams.toString());

        getClient().post(Constants.KAMS_VISIT, jsonParams, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                showNow.showLoadingDialog(FormsActivity.this);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                String json = new String(responseBody);
                Log.e("RESPONSE", "onSuccess: " + json);
                showNow.scheduleDismiss();

                try {
                    JSONObject object = new JSONObject(json);
                    lastVisitInsertedID = object.getString("lastVisitInsertedID");

                    Log.e("lastVisitInsertedID", lastVisitInsertedID);
                    showNow.desplayPositiveToast(FormsActivity.this, "Data submitted");

                    form1.setVisibility(View.GONE);
                    form2.setVisibility(View.VISIBLE);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("RESPONSEERROR", e.getMessage());
                    showNow.desplayErrorToast(FormsActivity.this, e.getMessage());
                    //progressBar.setVisibility(View.GONE);
                    showNow.scheduleDismiss();

                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String json = new String(responseBody);
                Log.e("REPONSE2", "onSuccess: " + json);
                showNow.desplayErrorToast(FormsActivity.this, json);
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

    private void callApiForm3() {
            RequestParams jsonParams = new RequestParams();
            jsonParams.put("_token", token);
            jsonParams.put("kams_visit_id", lastVisitInsertedID);
            jsonParams.put("document", filePath);
            Log.e("JSONPARAMS3", jsonParams.toString());
            //Log.e("SD",lastVisitInsertedID);

        //jsonParams.setForceMultipartEntityContentType(true);

        getClient().post(Constants.KAMS_VISIT_DOC, jsonParams, new AsyncHttpResponseHandler() {

                @Override
                public void onStart() {
                    super.onStart();
                    showNow.showLoadingDialog(FormsActivity.this);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                    String json = new String(responseBody);
                    Log.e("RES", "onSuccess: " + json);
                    showNow.scheduleDismiss();
                    showNow.desplayPositiveToast(FormsActivity.this,"Data submitted");

                    Toast.makeText(FormsActivity.this, "SUCCESS", Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    String json = new String(responseBody);
                    Log.e("REPONSEErrorr", "onSuccess: " + json);
                    showNow.desplayErrorToast(FormsActivity.this, json);
                    showNow.scheduleDismiss();
                    Toast.makeText(FormsActivity.this, "OPSS"+error, Toast.LENGTH_SHORT).show();


                }

                @Override
                public void onCancel() {
                    super.onCancel();
                    //progressBar.setVisibility(View.GONE);
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
        input_pocname = findViewById(R.id.input_pocname);
        input_poccontact = findViewById(R.id.input_poccontact);
        input_remarks = findViewById(R.id.input_remarks);
        input_last_mon_sale_report = findViewById(R.id.input_last_mon_sale_report);
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
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        // Setting Current Longitude
        latitude = String.valueOf(location.getLatitude());
        longitude = String.valueOf(location.getLongitude());

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case GALLERY_REQUEST:
                    if (data.getData() != null){
                        uri = data.getData();
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                            if (bitmap != null){
                                upload_img.setText("Image Picked!");
                            }else {
                                upload_img.setText("Attach image (if any)");
                            }
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG,75,byteArrayOutputStream);
                            byte[] imageInByte = byteArrayOutputStream.toByteArray();
                            filePath = Base64.encodeToString(imageInByte, Base64.DEFAULT);
                            //Toast.makeText(this, filePath, Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

             /*       if (data.getData() != null) {
                        //int count = data.getClipData().getItemCount();
                        // Get a list of picked images
                        uri = data.getData();
                        String imgPath = fileUtils.getPath(this, uri);
                        filePath = imgPath;
                        Log.e("PATH",filePath);
                        if (filePath != null){
                            upload_img.setText("Image Picked!");
                        }else {
                            upload_img.setText("Attach image (if any)");
                        }

                    } else {
                        Uri image_uri = data.getData();
                        try {
                            String imgPath = fileUtils.getPath(this, image_uri);
                            Log.e("FILES", imgPath);
                            if (imgPath != null) {
                                filePath = imgPath;

                                upload_img.setText("Image Picked!");

                            } else {
                                upload_img.setText("Attach image (if any)");
                            }
                        } catch (Exception e) {
                            Log.i("TAG", "Some exception " + e);
                        }
                    }*/

                    break;
            }
        }
    }
    private boolean hasStoragePermission(int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == PICK_IMG)
                pickImage();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }
}