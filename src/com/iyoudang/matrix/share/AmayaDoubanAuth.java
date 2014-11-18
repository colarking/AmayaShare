package com.iyoudang.matrix.share;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import com.iyoudang.matrix.share.util.AmayaShare;
import com.iyoudang.matrix.share.util.AmayaShareConstants;
import com.iyoudang.matrix.share.util.AmayaTokenKeeper;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 授权webview
 *
 * @author liyanju1
 *
 */
public class AmayaDoubanAuth extends Activity {

    private static final String TAG = AmayaDoubanAuth.class.getSimpleName();
    private static AmayaDoubanAuth authorWebView;
    private View waitView;
    private WebView mWebView;

    public static void finishActivity() {
        if (null != authorWebView)
            authorWebView.finish();
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authorize_webview);
        authorWebView = this;
        Intent intent = getIntent();
        mWebView = (WebView) findViewById(R.id.authorize_webview_id);
        waitView = findViewById(R.id.wait);
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        WebView.enablePlatformNotifications();
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WeiboWebViewClient());

        final String url = AmayaShareConstants.AMAYA_DOUBAN_AUTH_URL + "?client_id=" + AmayaShareConstants.AMAYA_DOUBAN_ID + "&redirect_uri=" + AmayaShareConstants.AMAYA_DOUBAN_REDIRECT_URI + "&response_type=code";
        mWebView.loadUrl(url);
    }

    public Bundle getAccessToken(String url) {
        String code = url.substring(url.indexOf("code=") + 5, url.length());
        Log.e(TAG, "getAccessToken()...code : " + code);
        Bundle bundle = new Bundle();
        HashMap<String,String> params = new HashMap<String, String>();
        params.put("client_id", AmayaShareConstants.AMAYA_DOUBAN_ID);
        params.put("client_secret", AmayaShareConstants.AMAYA_DOUBAN_SECRET);
        params.put("redirect_uri", AmayaShareConstants.AMAYA_DOUBAN_REDIRECT_URI);
        params.put("code", code);
        params.put("grant_type", "authorization_code");
        try {
            String s = amayaHttpPost(AmayaShareConstants.AMAYA_DOUBAN_TOKEURL, params);
            JSONObject jsonObject = new JSONObject(s);
            String access_token = jsonObject.getString("access_token");
            String douban_user_name = jsonObject.getString("douban_user_name");
            String douban_user_id = jsonObject.getString("douban_user_id");
            String expires_in = jsonObject.getString("expires_in");
            //String refresh_token = jsonObject.getString("refresh_token");
            bundle.putString(AmayaShareConstants.AMAYA_RESULT_ACCESS_TOKEN, access_token);
            bundle.putString(AmayaShareConstants.AMAYA_RESULT_EXPIRES_IN, expires_in);
            bundle.putString(AmayaShareConstants.AMAYA_RESULT_USER_ID, douban_user_id);
            bundle.putString(AmayaShareConstants.AMAYA_RESULT_USER_NAME, douban_user_name);
//            bundle.putInt(SNSManager.SNS_TYPE, SNSManager.DOUBAN_TYPE);
            AmayaTokenKeeper.saveDoubanToken(this, access_token, expires_in);
            String j = amayaHttpGet(AmayaShareConstants.AMAYA_DOUBAN_USER_INFO_URL + douban_user_id);
            JSONObject jo = new JSONObject(j);
            if(jo != null){
                Log.e("amaya","getAccessToken()...180..."+jo.toString());
                //{"msg":"uri_not_found","request":"POST \/v2\/user\/74bcaa8d5ba7b9645e35ffa812f72c2c","code":1001}
                String img = jo.getString("large_avatar");
                if(!TextUtils.isEmpty(img)) bundle.putString(AmayaShareConstants.AMAYA_RESULT_USER_IMG,img);
            }

            Log.e(TAG, "getAccessToken()...access_token : " + access_token
                    + " douban_user_id : " + douban_user_id + " expires_in : "
                    + expires_in);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return bundle;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWebView != null) {
            ((RelativeLayout)mWebView.getParent()).removeView(mWebView);
            mWebView.stopLoading();
            mWebView.clearView();
            mWebView.removeAllViews();
            mWebView.destroy();
            mWebView = null;
        }
        CookieManager.getInstance().removeAllCookie();
        System.gc();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if (isEnter) {
//			return false;
//		} else {
//		}
        return super.onKeyDown(keyCode, event);
    }

    public String amayaHttpGet(String url) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            int responseCode = conn.getResponseCode();
            InputStream stream;
            if (responseCode == 200) {
                stream = conn.getInputStream();
            } else {
                stream = conn.getErrorStream();
            }
            String res = getStringFromStream(stream);
            return res;
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    public String amayaHttpPost(String url, HashMap<String, String> params) {

        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            if (params != null) {
                Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, String> next = iterator.next();
                    conn.setRequestProperty(next.getKey(), next.getValue());
                }
            }
            conn.connect();
            int responseCode = conn.getResponseCode();
            InputStream stream = null;
            if(responseCode == 200){
                stream = conn.getInputStream();
            }else{
                stream = conn.getErrorStream();
            }
            String res = getStringFromStream(stream);
            return res;
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    private String getStringFromStream(InputStream stream) {
        byte[] buf = new byte[2048];
        int len;
        StringBuffer sb = new StringBuffer();
        try {
            while ((len = stream.read(buf)) != -1) {
                sb.append(new String(buf, 0, len));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private class WeiboWebViewClient extends WebViewClient {

        /**
         * 地址改变都会调用
         */
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, final String url) {
//			if (!url.startsWith(AmayaShareConstants.AMAYA_DOUBAN_AUTH_URL) && !isEnter) {
//				waitProgress.setVisibility(View.VISIBLE);
//				new Thread(new Runnable() {
//
//					@Override
//					public void run() {
//
//						Bundle bundle = getAccessToken(url);
//						Message msg = mHandler.obtainMessage();
//						msg.obj = bundle;
//						msg.sendToTarget();
//					}
//				}).start();
//				return true;
//			}
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);

        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler,
                                       SslError error) {
            handler.proceed();
        }

        @Override
        public void onPageStarted(WebView view, final String url, Bitmap favicon) {
            if (url.startsWith(AmayaShareConstants.AMAYA_DOUBAN_REDIRECT_URI)) {
                waitView.setVisibility(View.VISIBLE);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        Bundle bundle = getAccessToken(url);
                        Intent in = new Intent();
                        in.putExtras(bundle);
                        AmayaShare.instance().onActivityResult(AmayaDoubanAuth.this, AmayaShareConstants.AMAYA_ACTIVITY_RESULT_DOUBAN, RESULT_OK, in);
                        finish();
                    }
                }).start();
            }
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            waitView.setVisibility(View.GONE);
        }
    }

}
