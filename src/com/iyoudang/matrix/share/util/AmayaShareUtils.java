package com.iyoudang.matrix.share.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.iyoudang.matrix.R;
import com.iyoudang.matrix.share.AmayaAuthorize;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.utils.LogUtil;
import com.tencent.connect.UserInfo;
import com.tencent.connect.common.Constants;
import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzoneShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.tencent.weibo.sdk.android.api.WeiboAPI;
import com.tencent.weibo.sdk.android.model.AccountModel;
import com.tencent.weibo.sdk.android.model.BaseVO;
import com.tencent.weibo.sdk.android.model.ModelResult;
import com.tencent.weibo.sdk.android.network.HttpCallback;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Smith
 * Date: 14-8-28
 * Time: 下午2:34
 * To change this template use File | Settings | File Templates.
 */
public class AmayaShareUtils implements RequestListener, IUiListener, HttpCallback {

    private static final String TAG = "AmayaShareUtils";
    private static AmayaShareUtils amaya;

    /**
     *  0：新浪微博Token
     *  1：腾讯微博Token
     *  2：腾讯Token
     */
    private String[] tokens = new String[3];

    /**
     * 腾讯相关实例
     */
    private Tencent amayaTencent;
    private QzoneShare amayaShare;
    private AmayaWeiXinShare amayaWeiXin;


    /**
     * Sina微博相关实例
     */
    private WeiboAuth.AuthInfo amayaAuthInfo;
    private SsoHandler mSsoHandler;
    private AmayaSinaAPI amayaSinaApi;
    private AmayaShareListener amayaListener;
    private IUiListener amayaIUListener;


    private AmayaShareUtils(){}
    
    public synchronized static AmayaShareUtils instance(){
    	if(amaya == null){
    		amaya = new AmayaShareUtils();
    	}
    	return amaya;
    }
    public boolean isAuthed(AmayaShareEnums type, Context context) {
        String token = getToken(context,type);
        return !TextUtils.isEmpty(token);
    }

    public String getToken(Context mContext,AmayaShareEnums enums){
        int index = -1;
        switch (enums){
            case SINA_WEIBO:
                index = 0;
                if(TextUtils.isEmpty(tokens[index])){
                    initSinaWeibo(mContext);
                }
                break;
            case TENCENT_WEIBO:
                index = 1;
                if(TextUtils.isEmpty(tokens[index])){
                    tokens[index] = AmayaTokenKeeper.getTXWeiboToken(mContext);
                }
                break;
            case TENCENT_QQ:
            case TENCENT_QZONE:
                index = 2;
                if(TextUtils.isEmpty(tokens[index])){
                    initQQ(mContext);
                }
                break;
        }
        return tokens[index];
    }
    public void auth(AmayaShareEnums enums,Activity activity,AmayaShareListener listener){
        switch (enums){
            case SINA_WEIBO:
                authSinaWeibo(activity,listener);
                break;
            case TENCENT_WEIBO:
                authTXWeibo(activity,listener);
                break;
            case TENCENT_QQ:
            case TENCENT_QZONE:
                authQQ(activity,listener,enums);
                break;
        }

    }
    public void onActivityResult(final Context mContext,int requestCode, int resultCode, Intent data) {
        if(requestCode == AmayaShareConstants.AMAYA_ACTIVITY_RESULT_SINAWEIBO){
            if (mSsoHandler != null) {
                mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
            }
        }else if( requestCode == AmayaShareConstants.AMAYA_ACTIVITY_RESULT_TXWEIBO){
            tokens[1] = data.getExtras().getString(AmayaShareConstants.AMAYA_RESULT_ACCESS_TOKEN);
            if(amayaListener != null){
                if(amayaListener != null) amayaListener.onComplete(AmayaShareEnums.TENCENT_WEIBO,AmayaShareConstants.AMAYA_TYPE_AUTH, data ==null?null:data.getExtras());
            }
        }else{
            final boolean isShare = !amayaTencent.isSessionValid();
            final AmayaShareEnums enums =  requestCode == AmayaShareConstants.AMAYA_ACTIVITY_RESULT_QQ?AmayaShareEnums.TENCENT_QZONE:AmayaShareEnums.TENCENT_QQ;
            Log.e("amaya","onActivityResult()...enums="+enums+"--isShare="+isShare+"--data="+data);
            if(data == null) data = new Intent();
            initAmayaIUListener(mContext,amayaListener,AmayaShareEnums.TENCENT_QQ,false);
            amayaTencent.handleLoginData(data,amayaIUListener);
        }
    }

    /**
     * 获取手机经纬度
     *
     * @param context
     *            上下文
     * @return 可用的location 可能为空
     *
     */
    public static Location getLocation(Context context) {
        // LocationManager lm=(LocationManager)
        // context.getSystemService(Context.LOCATION_SERVICE);
        // Criteria criteria = new Criteria();
        // criteria.setAccuracy(Criteria.ACCURACY_FINE);//高精度
        // criteria.setAltitudeRequired(false);//不要求海拔
        // criteria.setBearingRequired(false);//不要求方位
        // criteria.setCostAllowed(true);//允许有花费
        // criteria.setPowerRequirement(Criteria.POWER_LOW);//低功耗
        // //从可用的位置提供器中，匹配以上标准的最佳提供器
        // String provider = lm.getBestProvider(criteria, true);
        // if (provider!=null) {
        // Location location=lm.getLastKnownLocation(provider);
        // return location;
        // }else {
        // return null;
        // }
        Location currentLocation = null;
        try {
            // 获取到LocationManager对象
            LocationManager locationManager = (LocationManager) context
                    .getSystemService(Context.LOCATION_SERVICE);
            // 创建一个Criteria对象
            Criteria criteria = new Criteria();
            // 设置粗略精确度
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            // 设置是否需要返回海拔信息
            criteria.setAltitudeRequired(false);
            // 设置是否需要返回方位信息
            criteria.setBearingRequired(false);
            // 设置是否允许付费服务
            criteria.setCostAllowed(true);
            // 设置电量消耗等级
            criteria.setPowerRequirement(Criteria.POWER_HIGH);
            // 设置是否需要返回速度信息
            criteria.setSpeedRequired(false);

            // 根据设置的Criteria对象，获取最符合此标准的provider对象
            String currentProvider = locationManager.getBestProvider(criteria,
                    true);
            Log.d("Location", "currentProvider: " + currentProvider);
            // 根据当前provider对象获取最后一次位置信息
            currentLocation = locationManager
                    .getLastKnownLocation(currentProvider);
        } catch (Exception e) {
            currentLocation = null;
        }
        return currentLocation;
    }

    /************************************************QQ分享部分 START**************************************************/
    /**
     *
     * @param context
     */
    private void initQQ(Context context) {
        if(amayaTencent == null) {
            amayaTencent =  Tencent.createInstance(AmayaShareConstants.AMAYA_QQ_ID,context);
            AmayaTokenKeeper.readQQToken(context,amayaTencent);
            tokens[2] = amayaTencent.getAccessToken();
        }
        if(amayaShare == null && amayaTencent != null){
            amayaShare = new QzoneShare(context,amayaTencent.getQQToken());
        }
    }



    private void authQQ(final Activity context, final AmayaShareListener amayaListener,final AmayaShareEnums enums){
        if(amayaTencent == null) initQQ(context);
        if(amayaTencent == null){
            if(amayaListener != null){
                if(amayaListener != null) amayaListener.onException(AmayaShareEnums.TENCENT_QZONE, AmayaShareConstants.AMAYA_TYPE_AUTH, context.getString(R.string.amaya_auth_no));
            }
            return;
        }

        initAmayaIUListener(context, amayaListener, enums,true);
        amayaTencent.login(context, "all",amayaIUListener);
    }

    private void initAmayaIUListener(final Context context, final AmayaShareListener amayaListener, final AmayaShareEnums enums,final boolean isAuth) {
        if(amayaIUListener == null){
            amayaIUListener = new IUiListener() {
                @Override
                public void onComplete(Object values) {
                    if(isAuth){
                        try {
                            //{"ret":0,"pay_token":"FCABB6BF240491F58A3A571976ABA41E","pf":"desktop_m_qq-10000144-android-2002-","query_authority_cost":134,"authority_cost":5764,"openid":"4006284219847AC2805B32E911ABCA52","expires_in":7776000,"pfkey":"c500c8b1867613fb54ecde6f9b27fd6c","msg":"","access_token":"08ED4C56A2CB1911B94EEC98B465CE5B","login_cost":578}
                            JSONObject jsonObject = (JSONObject) values;
                            String token = jsonObject.getString(Constants.PARAM_ACCESS_TOKEN);
                            String expires = jsonObject.getString(Constants.PARAM_EXPIRES_IN);
                            String openId = jsonObject.getString(Constants.PARAM_OPEN_ID);
                            if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(expires)
                                    && !TextUtils.isEmpty(openId)) {
                                amayaTencent.setAccessToken(token, expires);
                                amayaTencent.setOpenId(openId);
                                AmayaTokenKeeper.saveQQToken(context, amayaTencent);
                                tokens[2] = token;
                            }
                            Bundle bundle = new Bundle();
                            bundle.putString(AmayaShareConstants.AMAYA_RESULT_USER_ID, openId);
                            bundle.putString(AmayaShareConstants.AMAYA_RESULT_EXPIRES_IN, expires);
                            bundle.putString(AmayaShareConstants.AMAYA_RESULT_ACCESS_TOKEN, token);
                            if(amayaShare == null) amayaShare = new QzoneShare(context,amayaTencent.getQQToken());
                            updateUserInfo(context,amayaListener,bundle);
//                    if(amayaListener != null) amayaListener.onComplete(enums, AmayaShareConstants.AMAYA_TYPE_AUTH, bundle);
                        } catch(Exception e) {
                            if(amayaListener != null) amayaListener.onException(enums,AmayaShareConstants.AMAYA_TYPE_AUTH,e.getMessage());
                        }
                    }else{
                        if(amayaListener!= null) amayaListener.onComplete(enums,AmayaShareConstants.AMAYA_TYPE_SHARE,null);
                    }
                }

                @Override
                public void onError(UiError uiError) {
                    if(amayaListener != null) amayaListener.onException(enums,AmayaShareConstants.AMAYA_TYPE_AUTH,uiError.errorDetail);
                }

                @Override
                public void onCancel() {
                    if(amayaListener != null) amayaListener.onCancel(enums,AmayaShareConstants.AMAYA_TYPE_AUTH);
                }
            };
        }
    }

    public void shareToQZone(final Activity activity, final AmayaShareListener amayaListener,final int shareType,final String targetUrl,final String title,final String summary,final ArrayList<String> urls){
        if(activity == null) {
            amayaListener.onException(AmayaShareEnums.TENCENT_QZONE,AmayaShareConstants.AMAYA_TYPE_SHARE,"Error:context is null");
            return;
        }
        initQQ(activity);
        if(amayaTencent == null) {
            amayaListener.onException(AmayaShareEnums.TENCENT_QZONE,AmayaShareConstants.AMAYA_TYPE_SHARE,activity.getString(R.string.amaya_auth_fail_step_0));
        }else if(amayaTencent.isSessionValid()){
            readyShare(activity,amayaListener,shareType,targetUrl,title,summary,urls);
        }else{
            amayaListener.onException(AmayaShareEnums.TENCENT_QZONE,AmayaShareConstants.AMAYA_TYPE_AUTH,activity.getString(R.string.amaya_auth_no));
        }
    }

    private void updateUserInfo(final Context context,final AmayaShareListener amayaListener,final Bundle bundle) {
            if (amayaTencent != null && amayaTencent.isSessionValid()) {
                IUiListener listener = new IUiListener() {
                    @Override
                    public void onError(UiError e) {
                        if(amayaListener != null) amayaListener.onException(AmayaShareEnums.TENCENT_QQ,AmayaShareConstants.AMAYA_TYPE_AUTH,e.errorDetail);
                    }

                    @Override
                    public void onComplete(final Object response) {
                        JSONObject json = (JSONObject)response;
                        if(json.has("nickname")){
                            String name = null;
                            try {
                                name = json.getString("nickname");
                                if(bundle == null) {
                                	Bundle b = new Bundle();
                                	b.putString(AmayaShareConstants.AMAYA_RESULT_USER_NAME, name);
									if(amayaListener != null) amayaListener.onComplete(AmayaShareEnums.TENCENT_QQ,AmayaShareConstants.AMAYA_TYPE_AUTH,b);
                                }else{
                                	bundle.putString(AmayaShareConstants.AMAYA_RESULT_USER_NAME, name);
                                	if(amayaListener != null) amayaListener.onComplete(AmayaShareEnums.TENCENT_QQ,AmayaShareConstants.AMAYA_TYPE_AUTH,bundle);
                                }
                            } catch (JSONException e) {
                                if(amayaListener != null) amayaListener.onException(AmayaShareEnums.TENCENT_QQ,AmayaShareConstants.AMAYA_TYPE_AUTH,e.getMessage());
                                e.printStackTrace();
                            }

                        }
//                    new Thread(){
//                        @Override
//                        public void run() {
                        //{"is_yellow_year_vip":"0","ret":0,"figureurl_qq_1":"http:\/\/q.qlogo.cn\/qqapp\/100460854\/4006284219847AC2805B32E911ABCA52\/40","figureurl_qq_2":"http:\/\/q.qlogo.cn\/qqapp\/100460854\/4006284219847AC2805B32E911ABCA52\/100","nickname":"-","yellow_vip_level":"0","is_lost":0,"msg":"","city":"","figureurl_1":"http:\/\/qzapp.qlogo.cn\/qzapp\/100460854\/4006284219847AC2805B32E911ABCA52\/50","vip":"0","level":"0","figureurl_2":"http:\/\/qzapp.qlogo.cn\/qzapp\/100460854\/4006284219847AC2805B32E911ABCA52\/100","province":"北京","is_yellow_vip":"0","gender":"男","figureurl":"http:\/\/qzapp.qlogo.cn\/qzapp\/100460854\/4006284219847AC2805B32E911ABCA52\/30"}

//                            if(json.has("figureurl")){
//                                Bitmap bitmap = null;
//                                try {
//                                    bitmap = getbitmap(json.getString("figureurl_qq_2"));
//                                } catch (JSONException e) {
//
//                                }
//                            }
//                        }
//                    }.start();
                    }

                    @Override
                    public void onCancel() {
                        if(amayaListener != null) amayaListener.onCancel(AmayaShareEnums.TENCENT_QQ,AmayaShareConstants.AMAYA_TYPE_AUTH);
                    }
                };
                UserInfo mInfo = new UserInfo(context, amayaTencent.getQQToken());
                mInfo.getUserInfo(listener);
            } else {
                Log.e("amaya", "授权失效....");
                if(amayaListener != null) amayaListener.onException(AmayaShareEnums.TENCENT_QQ,AmayaShareConstants.AMAYA_TYPE_AUTH,context.getResources().getString(R.string.amaya_auth_fail));
            }
        }

    private void readyShare(final Activity activity,AmayaShareListener amayaListener,int shareType,String targetUrl,String title,String summary,ArrayList<String> urls) {
        //QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT
        if(TextUtils.isEmpty(targetUrl)){
            Toast.makeText(activity, "targetUrl为必填项，请补充后分享", 0).show();
            return;
        }
        final Bundle params = new Bundle();
        params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, shareType);
        params.putString(QzoneShare.SHARE_TO_QQ_TITLE, title);
        params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, summary);
        if (shareType != QzoneShare.SHARE_TO_QZONE_TYPE_APP ) {
            //app分享不支持传目标链接
            params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, targetUrl);
        }
        // 支持传多个imageUrl
        ArrayList<String> imageUrls = new ArrayList<String>();
        if(urls != null){
            for (int i = 0; i < urls.size(); i++) {
                imageUrls.add(urls.get(i));
            }
        }
        params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imageUrls);
        doShareToQzone(activity,amayaListener,params);
    }

    private void doShareToQzone(final Activity activity,final AmayaShareListener amayaListener,final Bundle params) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                amayaShare.shareToQzone(activity, params, new IUiListener() {
                    @Override
                    public void onCancel() {
                        if(amayaListener != null) amayaListener.onCancel(AmayaShareEnums.TENCENT_QZONE,AmayaShareConstants.AMAYA_TYPE_SHARE);
                    }

                    @Override
                    public void onError(UiError e) {
                        if(amayaListener != null) amayaListener.onException(AmayaShareEnums.TENCENT_QZONE,AmayaShareConstants.AMAYA_TYPE_SHARE, e.errorMessage);
                    }

                    @Override
                    public void onComplete(Object response) {
                        if(response != null){
                            Bundle b = new Bundle();
                            b.putString(AmayaShareConstants.AMAYA_RESULT_SHARE,response.toString());
                            if(amayaListener != null) amayaListener.onComplete(AmayaShareEnums.TENCENT_QZONE,AmayaShareConstants.AMAYA_TYPE_SHARE, b);
                        }else{
                            if(amayaListener != null) amayaListener.onComplete(AmayaShareEnums.TENCENT_QZONE,AmayaShareConstants.AMAYA_TYPE_SHARE, null);
                        }
                    }
                });
            }
        }).start();
    }

    public void shareToQQ(final Activity activity,final AmayaShareListener amayaListener,String title,String imgUrl,String summary,String tagetUrl)
    {
        this.amayaListener = amayaListener;
        Bundle bundle = new Bundle();
        //这条分享消息被好友点击后的跳转URL。
        bundle.putString( QQShare.SHARE_TO_QQ_TARGET_URL, tagetUrl);
        //分享的标题。注：PARAM_TITLE、PARAM_IMAGE_URL、PARAM_SUMMARY不能全为空，最少必须有一个是有值的。
        bundle.putString(QQShare.SHARE_TO_QQ_TITLE, title);
        //分享的图片URL
        bundle.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, imgUrl);
        //分享的消息摘要，最长50个字
        bundle.putString(QQShare.SHARE_TO_QQ_SUMMARY, summary);
        //手Q客户端顶部，替换“返回”按钮文字，如果为空，用返回代替
        bundle.putString(QQShare.SHARE_TO_QQ_APP_NAME, "??我在测试");
        //标识该消息的来源应用，值为应用名称+AppId。
//        bundle.putString(QQShare.SHARE_TO_QQ_Constants.PARAM_APP_SOURCE, "星期几" + AppId);
        amayaTencent.shareToQQ(activity,bundle,this);
    }
    @Override
    public void onComplete(Object o) {
        if(o != null){
            Log.e("amaya","onComplete()...310..."+o.toString());
            if(amayaListener!= null) amayaListener.onComplete(AmayaShareEnums.TENCENT_QQ,AmayaShareConstants.AMAYA_TYPE_SHARE,null);
        }

    }

    @Override
    public void onError(UiError uiError) {
        if(amayaListener != null) amayaListener.onException(AmayaShareEnums.TENCENT_QZONE, AmayaShareConstants.AMAYA_TYPE_SHARE, uiError.errorDetail);

    }

    @Override
    public void onCancel() {
        if(amayaListener != null) amayaListener.onCancel(AmayaShareEnums.TENCENT_QZONE,AmayaShareConstants.AMAYA_TYPE_SHARE);
    }
    /************************************************QQ分享部分  END**************************************************/
    /************************************************Tencent WeiBo分享部分  START**************************************************/
    private void authTXWeibo(Activity activity, AmayaShareListener listener) {
        Intent i = new Intent(activity, AmayaAuthorize.class);
        this.amayaListener = listener;
        activity.startActivityForResult(i, AmayaShareConstants.AMAYA_ACTIVITY_RESULT_TXWEIBO);
    }

    /**
     * 发表一条微博
     *
     * @param context        上下文
     * @param content        微博内容（若在此处@好友，需正确填写好友的微博账号，而非昵称），不超过140字
     * @param longitude      经度，为实数，如113.421234（最多支持10位有效数字，可以填空）不是必填
     * @param latitude       纬度，为实数，如22.354231（最多支持10位有效数字，可以填空） 不是必填
     * @param syncflag       微博同步到空间分享标记（可选，0-同步，1-不同步，默认为0），目前仅支持oauth1.0鉴权方式 不是必填
     * @param amayaListener       回调接口
     *  compatibleflag 容错标志，支持按位操作，默认为0。 0x20-微博内容长度超过140字则报错 0-以上错误做容错处理，即发表普通微博
     *                       不是必填
     * param mCallBack      回调函数
     * param mTargetClass   返回对象类，如果返回json则为null
     * param resultType     BaseVO.TYPE_BEAN=0 BaseVO.TYPE_LIST=1 BaseVO.TYPE_OBJECT=2
     *                       BaseVO.TYPE_BEAN_LIST=3 BaseVO.TYPE_JSON=4
     */
    public void shareToTXWeiBo(Context context, String content,
                                double latitude, double longitude, int syncflag,
                                AmayaShareListener amayaListener) {
        String accessToken = AmayaTokenKeeper.getTXWeiboToken(context);
        if ((accessToken != null)) {
            this.amayaListener = amayaListener;
            AccountModel account = new AccountModel(accessToken);
            WeiboAPI weiboAPI = new WeiboAPI(account);
            weiboAPI.addWeibo(context, content, "json", longitude, latitude, syncflag,
                    0,this , null, BaseVO.TYPE_JSON);
        }else if(amayaListener != null){
             amayaListener.onException(AmayaShareEnums.TENCENT_WEIBO,AmayaShareConstants.AMAYA_TYPE_SHARE,"Error:haven't auth step.token is null");
        }
    }
//

    /**
     * 发表一条带图片的微博
     *
     * @param context
     *            上下文
     * @param content
     *            微博内容（若在此处@好友，需正确填写好友的微博账号，而非昵称），不超过140字
     * @param longitude
     *            经度，为实数，如113.421234（最多支持10位有效数字，可以填空）不是必填
     * @param latitude
     *            纬度，为实数，如22.354231（最多支持10位有效数字，可以填空） 不是必填
     * @param mBitmap
     *            本地图片bitmap对象
     * @param syncflag
     *            微博同步到空间分享标记（可选，0-同步，1-不同步，默认为0），目前仅支持oauth1.0鉴权方式 不是必填
     * param  compatibleflag
     *            容错标志，支持按位操作，默认为0。 0x20-微博内容长度超过140字则报错 0-以上错误做容错处理，即发表普通微博
     *            不是必填
     * @param amayaListener 回调函数
     * param mTargetClass
     *            返回对象类，如果返回json则为null
     * param resultType
     *            BaseVO.TYPE_BEAN=0 BaseVO.TYPE_LIST=1 BaseVO.TYPE_OBJECT=2
     *            BaseVO.TYPE_BEAN_LIST=3 BaseVO.TYPE_JSON=4
     */
    public void shareToTXWeiBo(Context context, String content,
                               double latitude, double longitude, Bitmap mBitmap, int syncflag,
                              AmayaShareListener amayaListener) {
        String accessToken = getToken(context,AmayaShareEnums.TENCENT_WEIBO);
        if (accessToken != null) {
            this.amayaListener = amayaListener;
            AccountModel account = new AccountModel(accessToken);
            WeiboAPI weiboAPI = new WeiboAPI(account);
            weiboAPI.addPic(context, content, "json", longitude, latitude,
                    mBitmap, syncflag, 0, this, null, BaseVO.TYPE_JSON);
//            weiboAPI.reAddWeibo(context,content,picPath,videoPath,musicPath,musicTitle,musicAuthor, mCallBack, null, BaseVO.TYPE_JSON);
        }else if(amayaListener != null){
            amayaListener.onException(AmayaShareEnums.TENCENT_WEIBO,AmayaShareConstants.AMAYA_TYPE_SHARE,"Error:haven't auth step.token is null");
        }
    }

    /**
	 * 用图片URL发表带图片的微博
	 *
	 * @param context
	 *            上下文
	 * @param content
	 *            微博内容（若在此处@好友，需正确填写好友的微博账号，而非昵称），不超过140字
	 * @param longitude
	 *            经度，为实数，如113.421234（最多支持10位有效数字，可以填空）不是必填
	 * @param latitude
	 *            纬度，为实数，如22.354231（最多支持10位有效数字，可以填空） 不是必填
	 * @param picUrl
	 *            网络图片url
	 * @param syncflag
	 *            微博同步到空间分享标记（可选，0-同步，1-不同步，默认为0），目前仅支持oauth1.0鉴权方式 不是必填
	 * param compatibleflag
	 *            容错标志，支持按位操作，默认为0。 0x20-微博内容长度超过140字则报错 0-以上错误做容错处理，即发表普通微博
	 *            不是必填
	 * @param amayaListener
	 *            回调函数
	 * param mTargetClass
	 *            返回对象类，如果返回json则为null
	 * param resultType
	 *            BaseVO.TYPE_BEAN=0 BaseVO.TYPE_LIST=1 BaseVO.TYPE_OBJECT=2
	 *            BaseVO.TYPE_BEAN_LIST=3 BaseVO.TYPE_JSON=4
	 */
    public void shareToTXWeiBo(Context context, String content,
                                 double latitude, double longitude, String picUrl,
                                 int syncflag, AmayaShareListener amayaListener) {
        String accessToken = getToken(context, AmayaShareEnums.TENCENT_WEIBO);
        if (accessToken != null) {
            this.amayaListener = amayaListener;
            AccountModel account = new AccountModel(accessToken);
            WeiboAPI weiboAPI = new WeiboAPI(account);
            weiboAPI.addPicUrl(context, content, "json", longitude, latitude,
                    picUrl, syncflag, 0, this, null, BaseVO.TYPE_JSON);
        }else if(amayaListener != null){
            amayaListener.onException(AmayaShareEnums.TENCENT_WEIBO,AmayaShareConstants.AMAYA_TYPE_SHARE,"Error:haven't auth step.token is null");
        }
    }


    @Override
    public void onResult(Object object) {
        ModelResult result = (ModelResult) object;
        Log.e("amaya", "onResult()..isSuccess=" + result.isSuccess() + "--getError_message=" + result.getError_message() + "--" + result.toString());
        if (result != null) {
            if (!result.isExpires()) {
                if (result.isSuccess()) {
                    if(amayaListener != null) amayaListener.onComplete(AmayaShareEnums.TENCENT_WEIBO,AmayaShareConstants.AMAYA_TYPE_SHARE,null);
                    try {
                        JSONObject json = (JSONObject) result.getObj();
                        Log.e("amaya", "onResult()..json=" + json);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    if(amayaListener != null) amayaListener.onException(AmayaShareEnums.TENCENT_WEIBO,AmayaShareConstants.AMAYA_TYPE_SHARE,result.getError_message());
                }
            } else {
                if(amayaListener != null) amayaListener.onException(AmayaShareEnums.TENCENT_WEIBO,AmayaShareConstants.AMAYA_TYPE_SHARE,result.getError_message());
            }
        } else {
            if(amayaListener != null) amayaListener.onException(AmayaShareEnums.TENCENT_WEIBO,AmayaShareConstants.AMAYA_TYPE_SHARE,result.getError_message());
        }
    }
    /************************************************QQ分享部分  END**************************************************/
    /************************************************SINA WEIBO分享部分 START**************************************************/
    public boolean initSinaWeibo(Context context) {
        Oauth2AccessToken token = AmayaTokenKeeper.readSinaToken(context);
        boolean authed = token.isSessionValid();
        tokens[0]=token.getToken();
        if(authed){
            amayaSinaApi = new AmayaSinaAPI(token);
        }

        return authed;
    }
    private void authSinaWeibo(final Context mContext,final AmayaShareListener amayaListener) {
        if(amayaAuthInfo == null) amayaAuthInfo = new WeiboAuth.AuthInfo(mContext, AmayaShareConstants.AMAYA_SINA_KEY, AmayaShareConstants.AMAYA_SINA_REDIRECTURL,AmayaShareConstants.AMAYA_SINA_SCOPE);
        if (null == mSsoHandler && amayaAuthInfo != null) {
            WeiboAuth weiboAuth = new WeiboAuth(mContext, amayaAuthInfo);
            mSsoHandler = new SsoHandler((Activity)mContext, weiboAuth);
        }
        if (mSsoHandler != null) {
            mSsoHandler.authorize(new WeiboAuthListener(){
                @Override
                public void onComplete(Bundle bundle) {
                    Oauth2AccessToken accessToken = Oauth2AccessToken.parseAccessToken(bundle);
                    if (accessToken != null && accessToken.isSessionValid()) {
                        AmayaTokenKeeper.saveSinaToken(mContext, accessToken);
                        tokens[0] = accessToken.getToken();
                    }
                    if(amayaListener != null) amayaListener.onComplete(AmayaShareEnums.SINA_WEIBO,AmayaShareConstants.AMAYA_TYPE_AUTH, bundle);
                }

                @Override
                public void onWeiboException(WeiboException e) {
                    if(amayaListener != null) amayaListener.onException(AmayaShareEnums.SINA_WEIBO,AmayaShareConstants.AMAYA_TYPE_AUTH,e.getMessage());
                }

                @Override
                public void onCancel() {
                    if(amayaListener != null) amayaListener.onCancel(AmayaShareEnums.SINA_WEIBO,AmayaShareConstants.AMAYA_TYPE_AUTH);
                }
            });
        } else {
            LogUtil.e(TAG, "Please setWeiboAuthInfo(...) for first");
        }
    }

    /**
     * 发布一条新微博（连续两次发布的微博不可以重复）。
     *
     * @param content  要发布的微博文本内容，内容不超过140个汉字。
     * @param lat      纬度，有效范围：-90.0到+90.0，+表示北纬，默认为0.0。
     * @param lon      经度，有效范围：-180.0到+180.0，+表示东经，默认为0.0。
     * @param listener 异步请求回调接口
     */
    public void shareToSina(AmayaShareListener listener,String content, String lat, String lon) {
        if(amayaSinaApi != null){
            this.amayaListener = listener;
            amayaSinaApi.update(content,lat,lon,this);
        }else{
            if(listener != null) listener.onException(AmayaShareEnums.SINA_WEIBO,AmayaShareConstants.AMAYA_TYPE_SHARE,"Error:not called method initSinaWeibo() or isAuthed()");
        }
//        WeiboParameters params = buildUpdateParams(content, lat, lon);
//        requestAsync(sAPIList.get(WRITE_API_UPDATE), params, HTTPMETHOD_POST, listener);
    }
    /**
     * 指定一个图片URL地址抓取后上传并同时发布一条新微博，此方法会处理URLencode。
     *
     * @param content    要发布的微博文本内容，内容不超过140个汉字
     * @param imageUrl  图片的URL地址，必须以http开头
     * @param lat       纬度，有效范围：-90.0到+90.0，+表示北纬，默认为0.0
     * @param lon       经度，有效范围：-180.0到+180.0，+表示东经，默认为0.0
     * @param listener  异步请求回调接口
     */
    public void shareToSina(AmayaShareListener listener,String content, String imageUrl, String lat, String lon) {
        if(listener != null){
            if(amayaSinaApi != null){
                this.amayaListener = listener;
                amayaSinaApi.uploadUrlText(content, imageUrl, null, lat, lon, this);
            }else {
                listener.onException(AmayaShareEnums.SINA_WEIBO,AmayaShareConstants.AMAYA_TYPE_SHARE,"Error:not called method initSinaWeibo() or isAuthed()");
            }
        }
    }

    /**
     * 上传图片并发布一条新微博，此方法会处理urlencode。
     *
     * @param content   要发布的微博文本内容，内容不超过140个汉字
     * @param bitmap    要上传的图片，仅支持JPEG、GIF、PNG格式，图片大小小于5M
     * @param lat       纬度，有效范围：-90.0到+90.0，+表示北纬，默认为0.0
     * @param lon       经度，有效范围：-180.0到+180.0，+表示东经，默认为0.0
     * @param listener  异步请求回调接口

     */
    public void shareToSina( AmayaShareListener listener,String content, Bitmap bitmap, String lat, String lon) {
        if(listener != null){
            if(amayaSinaApi != null){
                this.amayaListener = listener;
                amayaSinaApi.upload(content, bitmap, lat, lon, this);
            }else{
                listener.onException(AmayaShareEnums.SINA_WEIBO,AmayaShareConstants.AMAYA_TYPE_SHARE,"Error:not called method initSinaWeibo() or isAuthed()");
            }
        }
    }

    @Override
    public void onComplete(String s) {
        Log.e("amaya","onComplete()..."+s);
        if(amayaListener != null){
            amayaListener.onComplete(AmayaShareEnums.SINA_WEIBO,AmayaShareConstants.AMAYA_TYPE_SHARE,null);
        }
    }

    @Override
    public void onWeiboException(WeiboException e) {
        Log.e("amaya","onWeiboException()..."+e.getMessage());
        if(amayaListener != null){
            amayaListener.onException(AmayaShareEnums.SINA_WEIBO, AmayaShareConstants.AMAYA_TYPE_SHARE, e.getMessage());
        }
    }
    /************************************************SINA WEIBO分享部分 END**************************************************/
    /************************************************WeiXin分享部分 START**************************************************/
    public void shareToWeixin(Context context, final boolean toCircle,final String title,final String description,final String imagePath,final String imageUrl,final String webpageUrl,
                             final AmayaShareListener amayaShareListener) {
        initWeixin(context);
        if(amayaWeiXin != null)
            amayaWeiXin.shareMessage(toCircle,title,description,imagePath,imageUrl,webpageUrl,amayaShareListener);
    }
    private void initWeixin(Context context) {
        if(amayaWeiXin == null) amayaWeiXin =  new AmayaWeiXinShare(context);
    }
    /************************************************WeiXin END**************************************************/

    
}
