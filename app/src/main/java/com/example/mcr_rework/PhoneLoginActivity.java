package com.example.mcr_rework;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity implements LocationListener {
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 401;
    private Button SendVerificationCodeButton, VerifyButton;
    private EditText InputPhoneNumber, InputVerificationCode;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private DatabaseReference RootRef;
    private String currentUserID;
    private DatabaseReference UsersRef;
    private String Lat, Lon, currentDate, currentTime;
    boolean isLocationFetched = false;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);


        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        SendVerificationCodeButton = (Button)findViewById(R.id.send_ver_code_button);
        VerifyButton = (Button)findViewById(R.id.verify_button);
        InputPhoneNumber = (EditText)findViewById(R.id.phone_number_input);
        InputVerificationCode = (EditText)findViewById(R.id.verification_code_input);
        loadingBar = new ProgressDialog(this);



        SendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String phoneNumber = InputPhoneNumber.getText().toString();
                Log.e("TAG",phoneNumber);
                if(TextUtils.isEmpty(phoneNumber)){
                    Toast.makeText(PhoneLoginActivity.this, "Please Enter a Phone Number first.....", Toast.LENGTH_SHORT).show();
                }else {

                    loadingBar.setTitle("Phone Verification");
                    loadingBar.setMessage("Please Wait, while we are sending the verification code....");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneLoginActivity.this,               // Activity (for callback binding)
                            callbacks);        // OnVerificationStateChangedCallbacks
                }

            }
        });

        VerifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendVerificationCodeButton.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);

                String verificationCode = InputVerificationCode.getText().toString();

                if(TextUtils.isEmpty(verificationCode)){
                    Toast.makeText(PhoneLoginActivity.this, "Please enter a verification code.....", Toast.LENGTH_SHORT).show();
                }else{
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);

                }


            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);


            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                Log.w("tag", "onVerificationFailed", e);

                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Invalid Phone Number, Please enter a valid phone number with country code...", Toast.LENGTH_SHORT).show();
                SendVerificationCodeButton.setVisibility(View.VISIBLE);
                InputPhoneNumber.setVisibility(View.VISIBLE);


                VerifyButton.setVisibility(View.INVISIBLE);
                InputVerificationCode.setVisibility(View.INVISIBLE);

            }
            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {

                mVerificationId = verificationId;
                mResendToken = token;

                Toast.makeText(PhoneLoginActivity.this, "Code has been sent, Please check and verify", Toast.LENGTH_SHORT).show();
                SendVerificationCodeButton.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);


                VerifyButton.setVisibility(View.VISIBLE);
                InputVerificationCode.setVisibility(View.VISIBLE);


            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            currentUserID = mAuth.getCurrentUser().getUid();

                            String deviceToken = FirebaseInstanceId.getInstance().getToken();




                            String currentUserID = mAuth.getCurrentUser().getUid();
                            RootRef.child("Users").child(currentUserID).setValue("");

                            RootRef.child("Users").child(currentUserID).child("device_token")
                                    .setValue(deviceToken)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            getLocation();
                                        }
                                    });

                            RootRef.child("Users").child(currentUserID).child("Type").setValue(2);
                            RootRef.child("Users").child(currentUserID).child("bId").setValue("N/A");
                            SendUserToSplashActivity();
                            Toast.makeText(PhoneLoginActivity.this, "account successfully created", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                        else{
                            String message = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, "Error : "+ message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
    }

    private void SendUserToMainActivity() {

        Intent mainIntent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
    private void SendUserToSplashActivity() {
        Intent mainIntent = new Intent(PhoneLoginActivity.this, SplashActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
    private void sendLocation() {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd,yyyy");
        currentDate = currentDateFormat.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
        currentTime = currentTimeFormat.format(calForTime.getTime());


        HashMap<String, Object> ContactInfoMap = new HashMap<>();
        ContactInfoMap.put("Latitude", Lat);
        ContactInfoMap.put("Longitude", Lon);
        ContactInfoMap.put("Date", currentDate);
        ContactInfoMap.put("Time", currentTime);
        UsersRef.child(currentUserID).child("Login").updateChildren(ContactInfoMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            SendUserToSplashActivity();
                            Toast.makeText(PhoneLoginActivity.this, "contact added to phone book successfully", Toast.LENGTH_SHORT).show();


                        } else {
                            String message = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        Log.e("error", "permission location" + requestCode);
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("error", "permission granted");


                    getLocation();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.e("error", "permission denied");
                }
                return;
            }
            case 401: {
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, (LocationListener) this);

            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    void getLocation() {

        try {
            if ((ContextCompat.checkSelfPermission(PhoneLoginActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(PhoneLoginActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED)){
                ActivityCompat.requestPermissions(PhoneLoginActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                return;

            }

            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, (LocationListener) this);
        }
        catch(SecurityException e) {
            Log.e("error","crash"+e.getMessage());
            e.printStackTrace();
        }
    }


    @Override
    public void onLocationChanged(Location location) {


        Log.e("error","location changed");


        Lat= String.valueOf(location.getLatitude());
        Lon = String.valueOf(location.getLongitude());
        if(!isLocationFetched==true) {

            isLocationFetched = true;

            sendLocation();
        }
        loadingBar.dismiss();

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Toast.makeText(PhoneLoginActivity.this, "Please Enable GPS and Internet", Toast.LENGTH_SHORT).show();


    }

    @Override
    public void onProviderEnabled(String s) {
        Log.e("error","status changed");


    }

    @Override
    public void onProviderDisabled(String s) {

        Log.e("error","Provider enabled");

    }
}
