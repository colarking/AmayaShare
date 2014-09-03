package com.iyoudang.matrix.share;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.iyoudang.matrix.share.util.AmayaShareConstants;
import com.iyoudang.matrix.share.util.AmayaShareEnums;
import com.iyoudang.matrix.share.util.AmayaShareListener;
import com.iyoudang.matrix.share.util.AmayaShareUtils;
import com.tencent.connect.share.QzoneShare;

public class AmayaMainActivity extends Activity implements AmayaShareListener, View.OnClickListener {
    private ColorStateList colorStateList;
    private int bgSelector = R.drawable.text_view_bg_selector;
    private AmayaShareUtils amayaShareUtils;
    private boolean showLoading;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        TextView amayaQzone = (TextView) findViewById(R.id.amaya_share_qzone);
        TextView amayaSina = (TextView) findViewById(R.id.amaya_share_sina);
        TextView amayaQQ = (TextView) findViewById(R.id.amaya_share_qq);
        TextView amayaWeixin = (TextView) findViewById(R.id.amaya_share_weixin);
        TextView amayaWXCircle = (TextView) findViewById(R.id.amaya_share_weixin_circle);
        TextView amayaTXWeibo = (TextView) findViewById(R.id.amaya_share_tencent_weibo);
        TextView amayaRenRen = (TextView) findViewById(R.id.amaya_share_renren);
        colorStateList = getResources().getColorStateList(R.drawable.text_view_selector);

        initClickView(amayaSina);
        initClickView(amayaTXWeibo);
        initClickView(amayaQzone);
        initClickView(amayaQQ);
        initClickView(amayaWeixin);
        initClickView(amayaWXCircle);
        initClickView(amayaRenRen);

        amayaShareUtils = AmayaShareUtils.instance();
        amayaShareUtils.authDouban();


//        int white = getResources().getColor(R.color.white);
//        int l = getResources().getColor(R.color.little_matrix);
//        ColorStateList colorStateList = createColorStateList(R.color.white, R.color.text_pressed_green, R.color.text_pressed_green, R.color.text_pressed_green);
//        loginBtn.setTextColor(colorStateList);
//        loginTXBtn.setTextColor(colorStateList);
//        loginQQBtn.setTextColor(colorStateList);
    }

    private void initClickView(TextView clickView) {
        clickView.setBackgroundResource(bgSelector);
        clickView.setTextColor(colorStateList);
        clickView.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("amaya","onResume()...");
    }

    @Override
    protected void onPause() {
        super.onPause();    //To change body of overridden methods use File | Settings | File Templates.
        showLoading = false;
        invalidateOptionsMenu();
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
        if(requestCode != 0 && data != null)amayaShareUtils.onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        setActionBar(menu);
        return super.onCreateOptionsMenu(menu);

    }

    private void setActionBar(Menu menu) {
        if(showLoading){
            ProgressBar bar = new ProgressBar(this);
            bar.setScrollBarStyle(android.R.attr.progressBarStyleInverse);
            bar.setIndeterminateDrawable(getResources().getDrawable(
                    R.drawable.abs__progress_medium_holo));

            menu.add(0, 1, 1, R.string.title)
                    .setActionView(bar)
//                    .setIcon(R.drawable.actionbar_compose)
//                    .setOnMenuItemClickListener(this)
                    .setShowAsAction(
                            MenuItem.SHOW_AS_ACTION_ALWAYS
                                    | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        }
    }

    @Override
	public void onComplete(AmayaShareEnums enumKey,boolean authOrShare, Bundle values) {
        showLoading = false;
        invalidateOptionsMenu();
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
                Toast.makeText(this,enumKey+"授权成功...name="+name,0).show();
                if(enumKey == AmayaShareEnums.SINA_WEIBO){
                    Log.e("amaya","onComplete()...准备分享到新浪微博");
                    amayaShareSina();
                }
            }else{
                onException(enumKey,authOrShare,"bundle is null error");
            }
        }else{
            Toast.makeText(this,enumKey+"分享完成",0).show();
        }
	}

	@Override
	public void onCancel(AmayaShareEnums enumKey,boolean authOrShare) {
        showLoading = false;
        invalidateOptionsMenu();
		String s=enumKey+"---onCancel()...authOrShare="+authOrShare;
		Toast.makeText(AmayaMainActivity.this,s,Toast.LENGTH_LONG).show();
		
	}


	@Override
	public void onException(AmayaShareEnums enumKey,boolean authOrShare, String msg) {
        showLoading = false;
        invalidateOptionsMenu();
		Toast.makeText(AmayaMainActivity.this,enumKey+"---onException()...authOrShare="+authOrShare+"---"+msg,Toast.LENGTH_LONG).show();
	}

    @Override
    public void onClick(View v) {
        showLoading = true;
        invalidateOptionsMenu();
        switch (v.getId()){
            case R.id.amaya_share_sina:
                amayaShareSina();
                break;
            case R.id.amaya_share_tencent_weibo:
                amayaShareTXWeibo();
                break;
            case R.id.amaya_share_qzone:
                amayaShareQzone();
                break;
            case R.id.amaya_share_qq:
                amayaShareQQ();
                break;
            case R.id.amaya_share_weixin:
                amayaShareWeiXin(false);
                break;
            case R.id.amaya_share_weixin_circle:
                amayaShareWeiXin(true);
                break;
            case R.id.amaya_share_renren:
                amayaShareRenRen();
                break;
        }
    }

    private void amayaShareRenRen() {
        boolean authed = amayaShareUtils.isAuthed(AmayaShareEnums.RENREN, this);
        if(authed){
            String title = "标题";
            String desc = "内容详细请阅：www.sina.com";
            String message ="消息正文";
            String imgUrl = "http://121.199.31.3/lvYou/upload/1409022238408487.jpg";
            amayaShareUtils.shareToRenRen(this,this,title,message,desc,imgUrl);
        }else{
            amayaShareUtils.auth(AmayaShareEnums.RENREN,this,this);
        }
    }

    private void amayaShareTXWeibo() {
        boolean authed = amayaShareUtils.isAuthed(AmayaShareEnums.TENCENT_WEIBO, this);
        if(authed){
            String content = "这是内容content";
            double latitude =  29.345728;
            double longitude = 110.550432;
            Location location = AmayaShareUtils.getLocation(this);
            if(location != null){
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
            //分享一条文字文博
//            amayaShareUtils.shareToTXWeiBo(this,content,latitude,longitude,1,this);


            //分享一条图片微博
//            String picUrl = "http://h.hiphotos.baidu.com/image/pic/item/1c950a7b02087bf465c6d0e0f0d3572c11dfcf95.jpg";
//            amayaShareUtils.shareToTXWeiBo(this,content,latitude,longitude,picUrl,1,this);


            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.amaya_share);
            amayaShareUtils.shareToTXWeiBo(this,content,latitude,longitude,bitmap,1,this);

        }else{
            amayaShareUtils.auth(AmayaShareEnums.TENCENT_WEIBO, this, this);
        }
    }

    /**
     *
     * @param toCircle  true:朋友圈；false:好友
     */
    private void amayaShareWeiXin(boolean toCircle) {
        String title ="AmayaShare分享";
        String desc = "这是一个测试分享哟！";
        /**
         * imagePath 和imageUrl 二选一,优先选取imagePath路径
         */
        String imagePath = null;//Environment.getExternalStorageDirectory().getAbsolutePath()+"/testwx.jpg";
        String imageUrl =null;
        String webpaggUrl = "www.iyoudang.com";
        amayaShareUtils.shareToWeixin(this,toCircle,title,desc,imagePath,imageUrl,webpaggUrl,this);
    }

    private void amayaShareQQ() {
        String title;
        String summary;
        if(amayaShareUtils.isAuthed(AmayaShareEnums.TENCENT_QQ,this)){
        title = "标题";
        String imgUrl = "http://img3.cache.netease.com/photo/0005/2013-03-07/8PBKS8G400BV0005.jpg";
        //分享的消息摘要，最长50个字
        summary = "内容摘要";
        //这条分享消息被好友点击后的跳转URL。
        String tagetUrl = "http://connect.qq.com/";
        amayaShareUtils.shareToQQ(this, this,title,imgUrl,summary,tagetUrl);
    }else{
        amayaShareUtils.auth(AmayaShareEnums.TENCENT_QQ,this,this);
    }
    }

    private void amayaShareSina() {
        boolean authed = amayaShareUtils.isAuthed(AmayaShareEnums.SINA_WEIBO, this);
        if(authed){
            amayaShareUtils.shareToSina(this,"this is demo ttttest",null,null);
        }else{
            amayaShareUtils.auth(AmayaShareEnums.SINA_WEIBO, this, this);
        }
    }

    private void amayaShareQzone() {
        int shareType = QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT;
        String targetUrl = "http://www.qq.com";
        String title = "Title";
        String summary = "summary";
        if(amayaShareUtils.isAuthed(AmayaShareEnums.TENCENT_QZONE,this)){
            amayaShareUtils.shareToQZone(this, this, shareType, targetUrl, title, summary, null);
        }else{
            amayaShareUtils.auth(AmayaShareEnums.TENCENT_QZONE,this,this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);    //To change body of overridden methods use File | Settings | File Templates.
        Log.e("amaya","onNewIntent()..."+intent);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();    //To change body of overridden methods use File | Settings | File Templates.
        amayaShareUtils.onDestroy();
        amayaShareUtils = null;
    }
}
