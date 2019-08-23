package com.example.mcr_rework;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class LocationActivity extends AppCompatActivity {
    WebView web3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        web3 = (WebView)findViewById(R.id.web3);
        WebSettings webSettings = web3.getSettings();
        webSettings.setJavaScriptEnabled(true);
        web3.loadUrl("file:///android_asset/locationo.html");
    }
}
