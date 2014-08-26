package com.fone.player.share;

import java.text.SimpleDateFormat;

import com.fone.player.R;
import com.fone.player.share.util.AmayaShareConstants;
import com.fone.player.share.util.AmayaShareEnums;
import com.fone.player.share.util.AmayaShareListener;
import com.fone.player.share.view.AmayaButton;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements AmayaShareListener {
    private AmayaButton loginBtn;
	private AmayaButton loginTXBtn;

	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			Toast.makeText(MainActivity.this, (String)msg.obj,Toast.LENGTH_LONG).show();
		};
		
	};
	/**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        loginBtn = (AmayaButton)findViewById(R.id.amaya_sinw_weido);
        loginTXBtn = (AmayaButton)findViewById(R.id.amaya_tx_weido);
        loginBtn.addShareListener(this);
        loginTXBtn.addShareListener(this);
        
    }

    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == AmayaShareConstants.AMAYA_ACTIVITY_RESULT_SINAWEIBO){
        	loginBtn.onActivityResult(requestCode, resultCode, data);
        }else if(requestCode == AmayaShareConstants.AMAYA_ACTIVITY_RESULT_TXWEIBO){
        	loginTXBtn.onActivityResult(requestCode, resultCode, data);
        }
    }


	@Override
	public void onComplete(AmayaShareEnums enumKey, Bundle values) {
		String s = null;
		if(values != null){
			Log.e("amaya","onComplete()...enumKey="+enumKey+"--"+values.toString());
			Oauth2AccessToken accessToken = Oauth2AccessToken.parseAccessToken(values);
			String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(
					new java.util.Date(accessToken.getExpiresTime()));
			s = "data="+date+"--userName="+values.getString(AmayaShareConstants.AMAYA_USER_NAME);
		}else{
			Log.e("amaya","onComplete()...enumKey="+enumKey+"--"+values);
			s = "values = null";
		}
//		Message msg = mHandler.obtainMessage();
//		msg.what =1;
//		msg.obj =enumKey+"--onComplete()..."+ s;
//		mHandler.sendMessage(msg);
		Toast.makeText(MainActivity.this, enumKey+"--onComplete()..."+ s,Toast.LENGTH_LONG).show();
		
	}


	@Override
	public void onCancel(AmayaShareEnums enumKey) {
		String s=enumKey+"---onCancel()...";
		Toast.makeText(MainActivity.this,s,Toast.LENGTH_LONG).show();
		
//		Message msg = mHandler.obtainMessage();
//		msg.what =1;
//		msg.obj =s;
//		mHandler.sendMessage(msg);
	}


	@Override
	public void onException(AmayaShareEnums enumKey, String msg) {
		Toast.makeText(MainActivity.this,enumKey+"---onException()..."+msg,Toast.LENGTH_LONG).show();
//		Message m = mHandler.obtainMessage();
//		m.what =1;
//		m.obj =enumKey+"---onException()..."+msg;
//		mHandler.sendMessage(m);
	}
}
