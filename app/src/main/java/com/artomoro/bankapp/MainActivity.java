package com.artomoro.bankapp;

//import android.graphics.Bitmap;
//import android.view.View;
//import android.content.DialogInterface;
//import android.webkit.WebSettings;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import androidx.annotation.Nullable;

import android.graphics.Color;
import android.view.Window;
import androidx.activity.EdgeToEdge;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private ActivityResultLauncher<Intent> fileChooserLauncher;
    private ValueCallback<Uri[]> filePathCallback;
    private static final int FILE_CHOOSER_REQUEST_CODE = 1;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Window window = getWindow();
        window.setNavigationBarColor(Color.BLACK);

        fileChooserLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (filePathCallback != null) {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            Uri[] results = new Uri[]{result.getData().getData()};
                            filePathCallback.onReceiveValue(results);
                        } else {
                            filePathCallback.onReceiveValue(null);
                        }
                        filePathCallback = null;
                    }
                }
        );


        if (!NetworkUtil.isConnectedToInternet(this)) {
            showNoInternetDialog();
        }

        webView = findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);

        //WebClient
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                if (MainActivity.this.filePathCallback != null) {
                    MainActivity.this.filePathCallback.onReceiveValue(null);
                }
                MainActivity.this.filePathCallback = filePathCallback;

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*"); // Tipe File
                fileChooserLauncher.launch(Intent.createChooser(intent, "Pilih file"));
                return true;
            }
        });

        // Per-URL an
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();

                if (url.startsWith("https://www.google.com/maps")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.setPackage("com.google.android.apps.maps");
                    try {
                        view.getContext().startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    }
                    return true;
                }
                if (url.startsWith("https://api.whatsapp.com/") || url.startsWith("https://wa.me/")) {
                    String nomerHp = url.substring(url.lastIndexOf("/") + 1);
                    String whatsappUrl = "whatsapp://send?phone=" + nomerHp;
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUrl));
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Tidak ada aplikasi WhatsApp yang terpasang", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                } else if (url.startsWith("mailto:")) {
                    // Handle mailto link
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                    emailIntent.setData(Uri.parse(url));
                    try {
                        startActivity(emailIntent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(MainActivity.this, "Tidak ada aplikasi EMAIL yang terpasang", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                return false;
            }
        });

        webView.loadUrl("https://bprartomoro.co.id/new");

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack(); //Kembali
                } else {
                    finish(); // Keluar
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            if (filePathCallback != null) {
                Uri[] results = null;
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String dataString = data.getDataString();
                    if (dataString != null) {
                        results = new Uri[]{Uri.parse(dataString)};
                    }
                }
                filePathCallback.onReceiveValue(results);
                filePathCallback = null;
            }
        }
    }

    private void showNoInternetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Anda Tidak Memiliki Jaringan Internet, Pastikan Anda Memiliki Jaringan Internet Yang Baik")
                .setCancelable(false)
                .setPositiveButton("Keluar", (dialog, id) -> finish());
        AlertDialog alert = builder.create();
        alert.show();
    }
}