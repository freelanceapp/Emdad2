package com.apps.emdad.activities_fragments.activity_sign_up_delegate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.apps.emdad.R;
import com.apps.emdad.databinding.ActivitySignUpDelegateBinding;

public class SignUpDelegateActivity extends AppCompatActivity {
    private ActivitySignUpDelegateBinding binding;
    private String url="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_sign_up_delegate);
        getDataFromIntent();
        initView();
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        url = intent.getStringExtra("url");

    }

    private void initView() {
        binding.webView.getSettings().setAllowContentAccess(true);
        binding.webView.getSettings().setAllowFileAccess(true);
        binding.webView.getSettings().setBuiltInZoomControls(false);
        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.loadUrl(url);
        binding.webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

            }

            @Override
            public void onPageCommitVisible(WebView view, String url) {
                super.onPageCommitVisible(view, url);
                binding.progBar.setVisibility(View.GONE);

            }


        });
    }
}