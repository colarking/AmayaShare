package com.fone.player.share;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import com.fone.player.R;
import com.fone.player.share.util.AmayaShareConstants;
import com.fone.player.share.util.AmayaShareEnums;
import com.fone.player.share.util.AmayaShareListener;
import com.fone.player.share.view.AmayaButton;
import com.tencent.tauth.Tencent;

public class AmayaMainActivity extends Activity implements AmayaShareListener {
    private AmayaButton loginBtn;
    private AmayaButton loginQQBtn;

	private AmayaButton loginTXBtn;
    private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			Toast.makeText(AmayaMainActivity.this, (String)msg.obj,Toast.LENGTH_LONG).show();
		};

	};
    private Tencent mTencent;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        loginBtn = (AmayaButton)findViewById(R.id.amaya_sinw_weido);
        loginTXBtn = (AmayaButton)findViewById(R.id.amaya_tx_weido);
        loginQQBtn = (AmayaButton)findViewById(R.id.amaya_tx_qq);
        loginBtn.addShareListener(this);
        loginTXBtn.addShareListener(this);
        loginQQBtn.addShareListener(this);

//        int white = getResources().getColor(R.color.white);
//        int l = getResources().getColor(R.color.little_matrix);
//        ColorStateList colorStateList = createColorStateList(R.color.white, R.color.text_pressed_green, R.color.text_pressed_green, R.color.text_pressed_green);
//        loginBtn.setTextColor(colorStateList);
//        loginTXBtn.setTextColor(colorStateList);
//        loginQQBtn.setTextColor(colorStateList);
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    public static ColorStateList createColorStateList(int normal, int pressed, int focused, int unable) {
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == AmayaShareConstants.AMAYA_ACTIVITY_RESULT_SINAWEIBO){
        	loginBtn.onActivityResult(requestCode, resultCode, data);
        }else if(requestCode == AmayaShareConstants.AMAYA_ACTIVITY_RESULT_TXWEIBO){
        	loginTXBtn.onActivityResult(requestCode, resultCode, data);
        }else if(requestCode == AmayaShareConstants.AMAYA_ACTIVITY_RESULT_QQ){
            loginQQBtn.onActivityResult(requestCode, resultCode, data);
        }
    }


	@Override
	public void onComplete(AmayaShareEnums enumKey, Bundle values) {
		if(values != null){
            Log.e("amaya","onComplete()...enumKey="+enumKey);
            String name= values.getString(AmayaShareConstants.AMAYA_USER_NAME);
            String id= values.getString(AmayaShareConstants.AMAYA_USER_ID);
            String expires_in= values.getString(AmayaShareConstants.AMAYA_EXPIRES_IN);
            String token= values.getString(AmayaShareConstants.AMAYA_ACCESS_TOKEN);
            Log.e("amaya","onComplete()...name="+name);
            Log.e("amaya","onComplete()...id="+id);
            Log.e("amaya","onComplete()...expires_in="+expires_in);
            Log.e("amaya","onComplete()...token="+token);
        }else{
            onException(enumKey,"bundle is null error");
        }
	}


	@Override
	public void onCancel(AmayaShareEnums enumKey) {
		String s=enumKey+"---onCancel()...";
		Toast.makeText(AmayaMainActivity.this,s,Toast.LENGTH_LONG).show();
		
//		Message msg = mHandler.obtainMessage();
//		msg.what =1;
//		msg.obj =s;
//		mHandler.sendMessage(msg);
	}


	@Override
	public void onException(AmayaShareEnums enumKey, String msg) {
		Toast.makeText(AmayaMainActivity.this,enumKey+"---onException()..."+msg,Toast.LENGTH_LONG).show();
//		Message m = mHandler.obtainMessage();
//		m.what =1;
//		m.obj =enumKey+"---onException()..."+msg;
//		mHandler.sendMessage(m);
	}
}
