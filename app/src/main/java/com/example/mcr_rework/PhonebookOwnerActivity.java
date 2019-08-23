package com.example.mcr_rework;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class PhonebookOwnerActivity extends AppCompatActivity {

    WebView web1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phonebook_owner);
        web1 = (WebView)findViewById(R.id.web1);
        WebSettings webSettings = web1.getSettings();
        webSettings.setJavaScriptEnabled(true);
        web1.loadUrl("file:///android_asset/phonebooko.html");
    }
}
