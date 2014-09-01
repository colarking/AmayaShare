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
import com.fone.player.share.util.AmayaShareUtils;
import com.fone.player.share.view.AmayaButton;

public class AmayaButtonActivity extends Activity implements AmayaShareListener {
    private AmayaButton loginBtn;
    private AmayaButton loginQQBtn;

	private AmayaButton loginTXBtn;
    private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			Toast.makeText(AmayaButtonActivity.this, (String)msg.obj,Toast.LENGTH_LONG).show();
		};

	};
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_button);
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
        Log.e("amaya","onResume()...");
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
            /**
             * AmayaShareUtils 封装了授权和分享的逻辑，可通用
             */
            AmayaShareUtils.instance().onActivityResult(AmayaShareEnums.SINA_WEIBO,this,requestCode, resultCode, data);
            /**
             *   AmayaSinaWeiboButton 封装了授权逻辑，可单独使用
             */
//        	loginBtn.onActivityResult(requestCode, resultCode, data);
        }else if(requestCode == AmayaShareConstants.AMAYA_ACTIVITY_RESULT_TXWEIBO){
//        	loginTXBtn.onActivityResult(requestCode, resultCode, data);
            AmayaShareUtils.instance().onActivityResult(AmayaShareEnums.TENCENT_WEIBO,this,requestCode, resultCode, data);
        }else if(requestCode == AmayaShareConstants.AMAYA_ACTIVITY_RESULT_QQ){
//            loginQQBtn.onActivityResult(requestCode, resultCode, data);
            AmayaShareUtils.instance().onActivityResult(AmayaShareEnums.TENCENT_QQ,this,requestCode, resultCode, data);
        }
    }

	@Override
	public void onComplete(AmayaShareEnums enumKey,boolean authOrShare, Bundle values) {
        if(authOrShare){
            if(values != null){
                Log.e("amaya","onComplete()...enumKey="+enumKey);
                String name= values.getString(AmayaShareConstants.AMAYA_RESULT_USER_NAME);
                String id= values.getString(AmayaShareConstants.AMAYA_RESULT_USER_ID);
                String expires_in= values.getString(AmayaShareConstants.AMAYA_RESULT_EXPIRES_IN);
                String token= values.getString(AmayaShareConstants.AMAYA_RESULT_ACCESS_TOKEN);
                Log.e("amaya","onComplete()...name="+name);
                Log.e("amaya","onComplete()...id="+id);
                Log.e("amaya","onComplete()...expires_in="+expires_in);
                Log.e("amaya","onComplete()...token="+token);
                if(enumKey == AmayaShareEnums.SINA_WEIBO){
                    Log.e("amaya","onComplete()...准备分享到新浪微博");
                    shareToSina();
                }
            }else{
                onException(enumKey,authOrShare,"bundle is null error");
            }
        }else{
            Toast.makeText(this,enumKey+"分享成功",0).show();
        }
	}


	@Override
	public void onCancel(AmayaShareEnums enumKey,boolean authOrShare) {
		String s=enumKey+"---onCancel()...authOrShare="+authOrShare;
		Toast.makeText(AmayaButtonActivity.this,s,Toast.LENGTH_LONG).show();
		
	}


	@Override
	public void onException(AmayaShareEnums enumKey,boolean authOrShare, String msg) {
		Toast.makeText(AmayaButtonActivity.this,enumKey+"---onException()...authOrShare="+authOrShare+"---"+msg,Toast.LENGTH_LONG).show();
	}

    private void shareToSina() {
//        TextObject textObj = AmayaShareUtils.instance().getTextObj("这是一个测试微博");
//        AmayaShareUtils.instance().sendSingleMessage(this,this,textObj);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);    //To change body of overridden methods use File | Settings | File Templates.
        Log.e("amaya","onNewIntent()..."+intent);

    }
}
