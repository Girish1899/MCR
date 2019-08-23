package com.example.mcr_rework;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class UserLoginLogoutActivity extends AppCompatActivity {

    WebView web5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login_logout);

        web5 = (WebView)findViewById(R.id.web5);
        WebSettings webSettings = web5.getSettings();
        webSettings.setJavaScriptEnabled(true);
        web5.loadUrl("file:///android_asset/userLoginLogouto.html");
    }
}
