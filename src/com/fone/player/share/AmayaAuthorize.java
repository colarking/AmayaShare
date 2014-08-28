package com.fone.player.share;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.fone.player.R;
import com.fone.player.share.util.AmayaShareConstants;
import com.tencent.weibo.sdk.android.api.util.Util;

import java.lang.reflect.Method;

/**
 * 用户授权组件
 * 
 */
public class AmayaAuthorize extends Activity {

	WebView webView;
	String _url;
	String _fileName;
	public static int WEBVIEWSTATE_1 = 0;
	int webview_state = 0;
	String path;
	Dialog _dialog;
	public static final int ALERT_DOWNLOAD = 0;
	public static final int ALERT_FAV = 1;
	public static final int PROGRESS_H = 3;
	public static final int ALERT_NETWORK = 4;
	private ProgressDialog dialog;
	private LinearLayout layout = null;
	private String redirectUri = null;
	private String clientId = null;
	private boolean isShow = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		if (!isNetworkAvailable()) {
			showDialog(ALERT_NETWORK);
		} else {
//			DisplayMetrics displaysMetrics = new DisplayMetrics();
//			getWindowManager().getDefaultDisplay().getMetrics(displaysMetrics);
//			String pix = displaysMetrics.widthPixels + "x"
//					+ displaysMetrics.heightPixels;
//			BackGroudSeletor.setPix(pix);
//
//			try {
//				// Bundle bundle = getIntent().getExtras();
				clientId = Util.getConfig().getProperty("APP_KEY");// bundle.getString("APP_KEY");
				redirectUri = Util.getConfig().getProperty("REDIRECT_URI");// bundle.getString("REDIRECT_URI");
				if (clientId == null || "".equals(clientId)
						|| redirectUri == null || "".equals(redirectUri)) {
					Toast.makeText(AmayaAuthorize.this, "请在配置文件中填写相应的信息",
                            Toast.LENGTH_SHORT).show();
				}
//				Log.d("redirectUri", redirectUri);
//				getWindow().setFlags(
//						WindowManager.LayoutParams.FLAG_FULLSCREEN,
//						WindowManager.LayoutParams.FLAG_FULLSCREEN);
//				requestWindowFeature(Window.FEATURE_NO_TITLE);
				int state = (int) Math.random() * 1000 + 111;
				path = "https://open.t.qq.com/cgi-bin/oauth2/authorize?client_id="
						+ clientId
						+ "&response_type=token&redirect_uri="
						+ redirectUri + "&state=" + state;
				this.initLayout();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
		}
	}
	public boolean isNetworkAvailable() {
		ConnectivityManager cm=(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] allNetworkInfo = cm.getAllNetworkInfo();
		boolean isOK = false;
		for(NetworkInfo ni:allNetworkInfo){
			if(ni.getState() == State.CONNECTED){
				isOK =true;
				break;
			}
		}
		return isOK;
	}

	/**
	 * 初始化界面使用控件，并设置相应监听
	 * */
	public void initLayout() {
		setContentView(R.layout.base_layout);
		RelativeLayout.LayoutParams fillParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		RelativeLayout.LayoutParams fillWrapParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		RelativeLayout.LayoutParams wrapParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);

		dialog = new ProgressDialog(this);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setMessage("请稍后...");
		dialog.setIndeterminate(false);
		dialog.setCancelable(false);
		dialog.show();
		showLoading(true);

		layout = (LinearLayout) findViewById(R.id.amaya_content_view);
		

		webView = new WebView(this);
		
		//Build.VERSION_CODES.HONEYCOMB = 11
		if(Build.VERSION.SDK_INT >= 11)
		{
			Class[] name = new Class[]{String.class};
			Object[] rmMethodName = new Object[]{"searchBoxJavaBridge_"};
			Method rji;
			try {
				rji = webView.getClass().getDeclaredMethod("removeJavascriptInterface", name);				
				rji.invoke(webView, rmMethodName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		LinearLayout.LayoutParams wvParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		webView.setLayoutParams(wvParams);
		WebSettings webSettings = webView.getSettings();
		webView.setVerticalScrollBarEnabled(false);
		webSettings.setJavaScriptEnabled(true);
		webSettings.setUseWideViewPort(true);
		webSettings.setLoadWithOverviewMode(false);
		webView.loadUrl(path);
		webView.setWebChromeClient(new WebChromeClient() {

			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				super.onProgressChanged(view, newProgress);
				Log.e("amaya", newProgress + "..");
				// if (dialog!=null&& !dialog.isShowing()) {
				// dialog.show();
				// }

			}

		});
		webView.setWebViewClient(new WebViewClient() {
			
			@Override
			public void onPageFinished(WebView view, String url) {
				Log.d("backurl", "page finished:" + url);
				if (url.indexOf("access_token") != -1 && !isShow) {
					jumpResultParser(url);
				}
				if (dialog != null && dialog.isShowing()) {
					dialog.cancel();
				}
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (url.indexOf("access_token") != -1 && !isShow) {
					jumpResultParser(url);
				}
				return false;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				Log.d("backurl", "page start:" + url);
				if (url.indexOf("access_token") != -1 && !isShow) {
					jumpResultParser(url);
				}
				if (dialog != null && dialog.isShowing()) {
					dialog.cancel();
				}
//				showLoading(false);
			}
		});
		layout.addView(webView);
//        layout.setBackgroundResource();
		this.setContentView(layout);
	}

	private void showLoading(boolean show) {
		findViewById(R.id.amaya_title_pdbar).setVisibility(show?View.VISIBLE:View.GONE);	
	}
	/**
	 * 
	 * 获取授权后的返回地址，并对其进行解析
	 */
	public void jumpResultParser(String result) {

		String resultParam = result.split("#")[1];
		String params[] = resultParam.split("&");
		String accessToken = params[0].split("=")[1];
		String expiresIn = params[1].split("=")[1];
		String openid = params[2].split("=")[1];
		String openkey = params[3].split("=")[1];
		String refreshToken = params[4].split("=")[1];
		String state = params[5].split("=")[1];
		String name = params[6].split("=")[1];
		String nick = params[7].split("=")[1];
		Context context = this.getApplicationContext();
		if (accessToken != null && !"".equals(accessToken)) {
            Util.saveSharePersistent(context, "ACCESS_TOKEN", accessToken);
            Util.saveSharePersistent(context, "EXPIRES_IN", expiresIn);// accesstoken过期时间，以返回的时间的准，单位为秒，注意过期时提醒用户重新授权
            Util.saveSharePersistent(context, "OPEN_ID", openid);
            Util.saveSharePersistent(context, "OPEN_KEY", openkey);
            Util.saveSharePersistent(context, "REFRESH_TOKEN", refreshToken);
            Util.saveSharePersistent(context, "NAME", name);
            Util.saveSharePersistent(context, "NICK", nick);
            Util.saveSharePersistent(context, "CLIENT_ID", clientId);
            Util.saveSharePersistent(context, "AUTHORIZETIME",
                    String.valueOf(System.currentTimeMillis() / 1000l));
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString(AmayaShareConstants.AMAYA_RESULT_USER_NAME, name);
            bundle.putString(AmayaShareConstants.AMAYA_RESULT_USER_ID, openid);
            bundle.putString(AmayaShareConstants.AMAYA_RESULT_EXPIRES_IN, expiresIn);
            bundle.putString(AmayaShareConstants.AMAYA_RESULT_ACCESS_TOKEN, accessToken);
            //{uid=2857043267, com.sina.weibo.intent.extra.USER_ICON=[B@415b94c8, _weibo_appPackage=com.sina.weibo, com.sina.weibo.intent.extra.NICK_NAME=sae_otaku, remind_in=7816692, userName=sae_otaku, expires_in=7816692, _weibo_transaction=1409038891040, access_token=2.00BTr2HDyY87OCc5df65dfe8VVYhfD}
			intent.putExtras(bundle);
            setResult(RESULT_OK,intent);
            finish();
//            UserAPI ua = new UserAPI(new AccountModel(accessToken));
//            ua.getUserInfo(context, "json", new HttpCallback(){
//                @Override
//                public void onResult(Object o) {
//                    ModelResult result = (ModelResult) o;
//                    AmayaLog.e("amaya", "onResult()..." + result.toString());
//
//                }
//            }, null, BaseVO.TYPE_JSON);
            isShow = true;
		}
	}

	Handler handle = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case 100:
				// Log.i("showDialog", "showDialog");
                AmayaAuthorize.this.showDialog(ALERT_NETWORK);
				break;
			}
		}
	};

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case PROGRESS_H:
			_dialog = new ProgressDialog(this);
			((ProgressDialog) _dialog).setMessage("加载中...");
			break;
		case ALERT_NETWORK:
			AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
			builder2.setTitle("网络连接异常，是否重新连接？");
			builder2.setPositiveButton("是",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (Util.isNetworkAvailable(AmayaAuthorize.this)) {
								webView.loadUrl(path);
							} else {
								Message msg = Message.obtain();
								msg.what = 100;
								handle.sendMessage(msg);
							}
						}

					});
			builder2.setNegativeButton("否",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
                            AmayaAuthorize.this.finish();
						}
					});
			_dialog = builder2.create();
			break;
		}
		return _dialog;
	}
	
	public ColorStateList createColorStateList(int normal, int pressed, int focused, int unable) {
	        int[] colors = new int[]{pressed, focused, normal, focused, unable, normal};
	        int[][] states = new int[6][];
	        states[0] = new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled};
	        states[1] = new int[]{android.R.attr.state_enabled, android.R.attr.state_focused};
	        states[2] = new int[]{android.R.attr.state_enabled};
	        states[3] = new int[]{android.R.attr.state_focused};
	        states[4] = new int[]{android.R.attr.state_window_focused};
	        states[5] = new int[]{};
	        ColorStateList colorList = new ColorStateList(states, colors);
	        return colorList;
    }

}
