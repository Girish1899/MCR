package com.example.mcr_rework;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class UsersOwnerActivity extends AppCompatActivity {

    WebView web4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_owner);
        web4 = (WebView)findViewById(R.id.web4);
        WebSettings webSettings = web4.getSettings();
        webSettings.setJavaScriptEnabled(true);
        web4.loadUrl("file:///android_asset/userso.html");
    }
}
