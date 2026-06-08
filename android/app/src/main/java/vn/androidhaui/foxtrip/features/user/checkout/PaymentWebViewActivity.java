package vn.androidhaui.foxtrip.features.user.checkout;

import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PaymentWebViewActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);

        String paymentUrl = getIntent().getStringExtra("paymentUrl");
        if (paymentUrl == null || paymentUrl.isEmpty()) {
            Toast.makeText(this, "URL không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                handleUrl(url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                return handleUrl(url);
            }

            private boolean handleUrl(String url) {
                if (url.contains("foxtrip.vn/payment-callback")) {
                    android.net.Uri uri = android.net.Uri.parse(url);
                    String responseCode = uri.getQueryParameter("vnp_ResponseCode");

                    if ("00".equals(responseCode)) {
                        Toast.makeText(PaymentWebViewActivity.this, "Thanh toán thành công!", Toast.LENGTH_LONG).show();
                        setResult(RESULT_OK);
                    } else {
                        Toast.makeText(PaymentWebViewActivity.this, "Thanh toán thất bại hoặc đã hủy",
                                Toast.LENGTH_LONG).show();
                        setResult(RESULT_CANCELED);
                    }
                    finish();
                    return true;
                }
                return false;
            }
        });

        webView.loadUrl(paymentUrl);
    }
}
