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
import com.renn.rennsdk.RennClient;
import com.renn.rennsdk.RennExecutor;
import com.renn.rennsdk.RennResponse;
import com.renn.rennsdk.exception.RennException;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.AsyncWeiboRunner;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.net.WeiboParameters;
import com.sina.weibo.sdk.utils.LogUtil;
import com.tencent.connect.UserInfo;
import com.tencent.connect.common.Constants;
import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzoneShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.tencent.weibo.sdk.android.api.WeiboAPI;
import com.tencent.weibo.sdk.android.api.util.Util;
import com.tencent.weibo.sdk.android.component.sso.AuthHelper;
import com.tencent.weibo.sdk.android.component.sso.OnAuthListener;
import com.tencent.weibo.sdk.android.component.sso.WeiboToken;
import com.tencent.weibo.sdk.android.model.AccountModel;
import com.tencent.weibo.sdk.android.model.BaseVO;
import com.tencent.weibo.sdk.android.model.ModelResult;
import com.tencent.weibo.sdk.android.network.HttpCallback;
import com.tencent.weibo.sdk.android.network.HttpReqWeiBo;
import com.tencent.weibo.sdk.android.network.HttpService;
import com.tencent.weibo.sdk.android.network.ReqParam;
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
public class AmayaShare implements RequestListener, IUiListener, HttpCallback {

    private static final String TAG = "AmayaShare";
    private static AmayaShare amaya;

    /**
     *  0：新浪微博Token    可用于分享到新浪微博
     *  1：腾讯微博Token    可用于分享到腾讯微博
     *  2：腾讯Token；      可用于分享到QQ好友及QQ空间
     */
    private String[] tokens = new String[3];

    /**
     * 腾讯相关实例
     */
    private Tencent amayaTencent;
    private QzoneShare amayaShare;
    private AmayaWeiXinShare amayaWeiXin;
    private IUiListener amayaIUListener;


    /**
     * Sina微博相关实例
     */
    private WeiboAuth.AuthInfo amayaAuthInfo;
    private SsoHandler mSsoHandler;
    private AmayaSinaAPI amayaSinaApi;
    private AmayaShareListener amayaListener;

    /**
     * 人人相关实例
     */
    private RennClient amayaRenren;


    private AmayaShare(){}
    
    public synchronized static AmayaShare instance(){
    	if(amaya == null){
    		amaya = new AmayaShare();
    	}
    	return amaya;
    }

    /**
     * 获取手机经纬度
     *
     * @param context 上下文
     * @return 可用的location 可能为空
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

    public boolean isAuthed(AmayaShareEnums type, Context context) {
        if (type == AmayaShareEnums.RENREN) {
            initRenRen(context);
            return amayaRenren.isLogin();
        } else {
            String token = getToken(context, type);
            return !TextUtils.isEmpty(token);
        }
    }

    public String getToken(Context mContext, AmayaShareEnums enums) {
        int index = -1;
        switch (enums) {
            case SINA_WEIBO:
                index = 0;
                if (TextUtils.isEmpty(tokens[index])) {
                    initSinaWeibo(mContext);
                }
                break;
            case TENCENT_WEIBO:
                index = 1;
                if (TextUtils.isEmpty(tokens[index])) {
                    tokens[index] = AmayaTokenKeeper.getTXWeiboToken(mContext);
                }
                break;
            case TENCENT_QQ:
            case TENCENT_QZONE:
                index = 2;
                if (TextUtils.isEmpty(tokens[index])) {
                    initQQ(mContext);
                }
                break;
            case RENREN:

                break;
        }
        return tokens[index];
    }

    public void auth(AmayaShareEnums enums, Activity activity, AmayaShareListener listener) {
        Log.e("amaya", "auth()...enums=" + enums + "--enums.ordinal()=" + enums.ordinal());
        switch (enums) {
            case SINA_WEIBO:
                authSinaWeibo(activity, listener);
                break;
            case TENCENT_WEIBO:
                authTXWeibo(activity, listener);
                break;
            case TENCENT_QQ:
            case TENCENT_QZONE:
                authQQ(activity, listener, enums);
                break;
            case RENREN:
                authRenRen(activity, listener);

                break;
        }

    }

    public void onActivityResult(final Context mContext, int requestCode, int resultCode, Intent data) {
        if (requestCode == AmayaShareConstants.AMAYA_ACTIVITY_RESULT_SINAWEIBO) {
            if (mSsoHandler != null) {
                mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
            }
        } else if (requestCode == AmayaShareConstants.AMAYA_ACTIVITY_RESULT_TXWEIBO) {
            tokens[1] = data.getExtras().getString(AmayaShareConstants.AMAYA_RESULT_ACCESS_TOKEN);
            if (amayaListener != null) {
                if (amayaListener != null)
                    amayaListener.onComplete(AmayaShareEnums.TENCENT_WEIBO, AmayaShareConstants.AMAYA_TYPE_AUTH, data == null ? null : data.getExtras());
            }
        } else if (requestCode == AmayaShareConstants.AMAYA_ACTIVITY_RESULT_RENREN) {
            if (data != null) {
                Bundle extras = data.getExtras();
                Log.e("amaya", "" + extras.toString());
            }
        } else {
            final boolean isShare = !amayaTencent.isSessionValid();
            final AmayaShareEnums enums = requestCode == AmayaShareConstants.AMAYA_ACTIVITY_RESULT_QQ ? AmayaShareEnums.TENCENT_QZONE : AmayaShareEnums.TENCENT_QQ;
            Log.e("amaya", "onActivityResult()...enums=" + enums + "--isShare=" + isShare + "--data=" + data);
            if (data == null) data = new Intent();
            initAmayaIUListener(mContext, amayaListener, AmayaShareEnums.TENCENT_QQ, false);
            amayaTencent.handleLoginData(data, amayaIUListener);
        }
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
            Toast.makeText(activity, "targetUrl为必填项，请补充后分享", Toast.LENGTH_SHORT).show();
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
    private void authTXWeibo(final Activity activity, AmayaShareListener listener) {
        // 注册当前应用的appid和appkeysec，并指定一个OnAuthListener
        // OnAuthListener在授权过程中实施监听
        this.amayaListener = listener;
        AuthHelper.register(activity, AmayaShareConstants.AMAYA_TENCENT_WEIBO_KEY, AmayaShareConstants.AMAYA_TENCENT_WEIBO_SECRET, new OnAuthListener() {

            // 如果当前设备没有安装腾讯微博客户端，走这里
            @Override
            public void onWeiBoNotInstalled() {
                Intent i = new Intent(activity, AmayaAuthorize.class);
                activity.startActivityForResult(i, AmayaShareConstants.AMAYA_ACTIVITY_RESULT_TXWEIBO);

            }

            // 如果当前设备没安装指定版本的微博客户端，走这里
            @Override
            public void onWeiboVersionMisMatch() {
                AuthHelper.unregister(activity);
                Intent i = new Intent(activity, AmayaAuthorize.class);
                activity.startActivity(i);
            }

            // 如果授权失败，走这里
            @Override
            public void onAuthFail(int result, String err) {
                AuthHelper.unregister(activity);
                if (amayaListener != null) amayaListener.onException(AmayaShareEnums.TENCENT_WEIBO, true, err);
            }

            // 授权成功，走这里
            // 授权成功后，所有的授权信息是存放在WeiboToken对象里面的，可以根据具体的使用场景，将授权信息存放到自己期望的位置，
            // 在这里，存放到了applicationcontext中
            @Override
            public void onAuthPassed(String name, WeiboToken token) {
                AuthHelper.unregister(activity);
                Util.saveSharePersistent(activity, "ACCESS_TOKEN", token.accessToken);
                Util.saveSharePersistent(activity, "EXPIRES_IN", token.expiresIn);// accesstoken过期时间，以返回的时间的准，单位为秒，注意过期时提醒用户重新授权
                Util.saveSharePersistent(activity, "OPEN_ID", token.openID);
                Util.saveSharePersistent(activity, "OPEN_KEY", token.omasKey);
                Util.saveSharePersistent(activity, "REFRESH_TOKEN", token.refreshToken);
                Util.saveSharePersistent(activity, "CLIENT_ID", String.valueOf(AmayaShareConstants.AMAYA_TENCENT_WEIBO_KEY));
                Util.saveSharePersistent(activity, "NAME", name);
                Util.saveSharePersistent(activity, "NICK", name);
                Util.saveSharePersistent(activity, "AUTHORIZETIME",
                        String.valueOf(System.currentTimeMillis() / 1000l));
                AmayaTokenKeeper.saveTXWeiboToken(activity, token.accessToken, String.valueOf(token.expiresIn));
                final Bundle bundle = new Bundle();
                bundle.putString(AmayaShareConstants.AMAYA_RESULT_USER_NAME, name);
                bundle.putString(AmayaShareConstants.AMAYA_RESULT_USER_ID, token.openID);
                bundle.putString(AmayaShareConstants.AMAYA_RESULT_EXPIRES_IN, String.valueOf(token.expiresIn));
                bundle.putString(AmayaShareConstants.AMAYA_RESULT_ACCESS_TOKEN, token.accessToken);
                //{uid=2857043267, com.sina.weibo.intent.extra.USER_ICON=[B@415b94c8, _weibo_appPackage=com.sina.weibo, com.sina.weibo.intent.extra.NICK_NAME=sae_otaku, remind_in=7816692, userName=sae_otaku, expires_in=7816692, _weibo_transaction=1409038891040, access_token=2.00BTr2HDyY87OCc5df65dfe8VVYhfD}
                HttpReqWeiBo weibo = new HttpReqWeiBo(activity, "https://open.t.qq.com/api/user/info", new HttpCallback() {
                    @Override
                    public void onResult(Object o) {
                        /**
                         {"idolnum":2,"sex":1,"location":"中国 北京","province_code":"11","tag":null,"ismyblack":0,"verifyinfo":"","ismyfans":0,"send_private_flag":2,"homecountry_code":"","isent":0,"homeprovince_code":"","hometown_code":"","level":1,"favnum":0,"name":"iyoudang2014","openid":"DFF9DE3BE5CE97659A48B0FE2C455630","edu":null,"head":"","industry_code":0,"tweetinfo":[{"fromurl":"http:\/\/wiki.open.t.qq.com\/index.php\/%E4%BA%A7%E5%93%81%E7%B1%BBFAQ#.E6.8F.90.E4.BA.A4.E5.BA.94.E7.94.A8.E6.9D.A5.E6.BA.90.E5.AD.97.E6.AE.B5.E5.AE.A1.E6.A0.B8.E8.83.BD.E5.BE.97.E5.88.B0.E4.BB.80.E4.B9.88.E5.A5.BD.E5.A4.84.EF.BC.9F\n","music":null,"text":"vivo Xshot旗舰版(送移动电源 蓝牙来电提示器 鱼眼镜头)...","geo":"","location":"中国 四川 南充","status":0,"province_code":"51","image":["http:\/\/app.qpic.cn\/mblogpic\/801768fa78d8336d6a04"],"origtext":"vivo phone ","self":1,"from":"微博开放平台","type":1,"country_code":"1","id":"440458010773005","timestamp":1411750678,"city_code":"13","longitude":"0","latitude":"0","emotionurl":"","video":null,"emotiontype":0}],"comp":null,"https_head":"","isrealname":2,"birth_year":1998,"country_code":"1","homepage":"","exp":150,"regtime":1405762726,"fansnum":0,"birth_month":9,"ismyidol":0,"nick":"aaaaa","email":"","birth_day":24,"homecity_code":"","city_code":"","mutual_fans_num":0,"introduction":"","isvip":0,"tweetnum":3}
                         */
                        ModelResult modelResult = (ModelResult) o;
                        Log.e("amaya", "onResult()...o=" + o.toString());
                        try {
                            if ("success".equals(modelResult.getError_message())) {
                                JSONObject jo = new JSONObject(modelResult.getObj().toString());
                                int code = jo.getInt("ret");
                                if (code == 0) {
                                    JSONObject obj = jo.getJSONObject("data");
                                    String headUrl = obj.getString("head");
                                    String nickName = obj.getString("nick");
                                    if (!TextUtils.isEmpty(headUrl)) {
                                        bundle.putString(AmayaShareConstants.AMAYA_RESULT_USER_IMG, headUrl);
                                    }
                                    if (!TextUtils.isEmpty(nickName)) {
                                        bundle.putString(AmayaShareConstants.AMAYA_RESULT_USER_NAME, nickName);
                                    }
                                    if (amayaListener != null)
                                        amayaListener.onComplete(AmayaShareEnums.TENCENT_WEIBO, AmayaShareConstants.AMAYA_TYPE_AUTH, bundle);
                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            if (amayaListener != null)
                                amayaListener.onException(AmayaShareEnums.TENCENT_WEIBO, true, e.getMessage());
                        }
                    }
                }, null, "GET", BaseVO.TYPE_JSON);
                ReqParam mParam = new ReqParam();
                mParam.addParam("scope", "all");
                mParam.addParam("clientip", Util.getLocalIPAddress(activity));
                mParam.addParam("oauth_version", "2.a");
                mParam.addParam("oauth_consumer_key",
                        Util.getSharePersistent(activity, "CLIENT_ID"));
                mParam.addParam("openid", Util.getSharePersistent(activity, "OPEN_ID"));
                mParam.addParam("format", "json");
                mParam.addParam("access_token", token.accessToken);
                weibo.setParam(mParam);
                HttpService.getInstance().addImmediateReq(weibo);
            }
        });

        AuthHelper.auth(activity, "");
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
                    final Oauth2AccessToken accessToken = Oauth2AccessToken.parseAccessToken(bundle);
                    if (accessToken != null && accessToken.isSessionValid()) {
                        AmayaTokenKeeper.saveSinaToken(mContext, accessToken);
                        tokens[0] = accessToken.getToken();
//                        bundle.getString() ;
                        amayaSinaApi = new AmayaSinaAPI(accessToken);
                    }
                    getSinaInfo(accessToken,amayaListener,bundle);

                }

                private void getSinaInfo(final Oauth2AccessToken token, final AmayaShareListener amayaListener, final Bundle bundle) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            /**
                             {"id":2857043267,"idstr":"2857043267","class":1,"screen_name":"sae_otaku","name":"sae_otaku","province":"11","city":"14","location":"北京 昌平区","description":"","url":"","profile_image_url":"http://tp4.sinaimg.cn/2857043267/50/5695270568/1","profile_url":"u/2857043267","domain":"","weihao":"","gender":"m","followers_count":10,"friends_count":30,"pagefriends_count":0,"statuses_count":20,"favourites_count":3,"created_at":"Tue Aug 28 23:05:34 +0800 2012","following":false,"allow_all_act_msg":false,"geo_enabled":true,"verified":false,"verified_type":-1,"remark":"","status":{"created_at":"Wed Sep 24 11:26:20 +0800 2014","id":3758288141096461,"mid":"3758288141096461","idstr":"3758288141096461","text":"this is demo ttttest","source":"<a href=\"http://app.weibo.com/t/feed/RzRDU\" rel=\"nofollow\">iyoudang</a>","source_type":1,"favorited":false,"truncated":false,"in_reply_to_status_id":"","in_reply_to_user_id":"","in_reply_to_screen_name":"","pic_urls":[],"geo":null,"reposts_count":0,"comments_count":0,"attitudes_count":0,"mlevel":0,"visible":{"type":0,"list_id":0},"darwin_tags":[]},"ptype":0,"allow_all_comment":true,"avatar_large":"http://tp4.sinaimg.cn/2857043267/180/5695270568/1","avatar_hd":"http://ww3.sinaimg.cn/crop.0.0.600.600.1024/aa4b0543jw8egh6x33c2nj20go0got9b.jpg","verified_reason":"","verified_trade":"","verified_reason_url":"","verified_source":"","verified_source_url":"","follow_me":false,"online_status":1,"bi_followers_count":0,"lang":"zh-cn","star":0,"mbtype":0,"mbrank":0,"block_word":0,"block_app":0,"credit_score":0}
                             */
                            try {
                                WeiboParameters params = new WeiboParameters();
                                params.put("access_token",token.getToken());
                                params.put("source", AmayaShareConstants.AMAYA_SINA_KEY);
                                params.put("uid", token.getUid());
                                AsyncWeiboRunner.requestAsync("https://api.weibo.com/2/users/show.json", params, "GET", new RequestListener() {
                                    @Override
                                    public void onComplete(String s) {
                                        Log.e("amaya", "onComplete()...s=" + s);
                                        try {
                                            JSONObject jo = new JSONObject(s);
                                            String imgUrl = jo.getString("profile_image_url");
                                            bundle.putString(AmayaShareConstants.AMAYA_RESULT_USER_IMG,imgUrl);
                                            bundle.putString(AmayaShareConstants.AMAYA_RESULT_USER_NAME, jo.getString("screen_name"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        if(amayaListener != null) amayaListener.onComplete(AmayaShareEnums.SINA_WEIBO,AmayaShareConstants.AMAYA_TYPE_AUTH, bundle);
                                    }

                                    @Override
                                    public void onWeiboException(WeiboException e) {
                                        Log.e("amaya", "onWeiboException()...s=" + e.getMessage());
                                        e.printStackTrace();
                                        if(amayaListener != null) amayaListener.onException(AmayaShareEnums.SINA_WEIBO, AmayaShareConstants.AMAYA_TYPE_AUTH, e.getMessage());
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
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
    /************************************************RenRen START**************************************************/
    private void authRenRen(Activity activity,final AmayaShareListener listener) {
        initRenRen(activity);
        if(!amayaRenren.isLogin()){
            amayaRenren.init(AmayaShareConstants.AMAYA_RENREN_APP_ID,AmayaShareConstants.AMAYA_RENREN_API_KEY,  AmayaShareConstants.AMAYA_RENREN_SECRET_KEY);
            amayaRenren.setScope("read_user_blog read_user_photo read_user_status read_user_album "
                    + "read_user_comment read_user_share publish_blog publish_share "
                    + "send_notification photo_upload status_update create_album "
                    + "publish_comment publish_feed");
            amayaRenren.setTokenType("bearer");
            amayaRenren.setLoginListener(new RennClient.LoginListener() {
                @Override
                public void onLoginSuccess() {
//                Toast.makeText(MainActivity.this, "登录成功",
//                        Toast.LENGTH_SHORT).show();
                    if(listener != null) listener.onComplete(AmayaShareEnums.RENREN,AmayaShareConstants.AMAYA_TYPE_AUTH,null);
                }

                @Override
                public void onLoginCanceled() {
                    if(listener != null) listener.onCancel(AmayaShareEnums.RENREN,AmayaShareConstants.AMAYA_TYPE_AUTH);
                }
            });
            amayaRenren.login(activity);
        }
    }

    public void shareToRenRen(Context context,final AmayaShareListener listener,String title,String message,String description,String imgUrl){
        initRenRen(context);
        if(amayaRenren.isLogin()){
            if(imgUrl.startsWith("http")){
                AmayaRRBean param = new AmayaRRBean();
                if(TextUtils.isEmpty(title)){
                    param.setTitle(context.getString(R.string.app_name));
                }else{
                    if(description.length()<30)
                        param.setTitle(title);
                    else
                        param.setTitle(title.substring(0,30));
                }
                param.setMessage(message);
                if(!TextUtils.isEmpty(description)){
                    if(description.length()<200) {
                        param.setDescription(description);
                    }
                    else {
                        param.setDescription(description.substring(0,200));
                    }
                }
                param.setActionName("爱游荡");
                param.setActionTargetUrl("http://www.iyoudang.com");
//                param.setSubtitle("subtitle");
                param.setImageUrl(imgUrl);
//                param.setImageUrl("http://t04.pic.sogou.com/49a81c7bb4e60fa9_i.jpg");
                param.setTargetUrl(imgUrl);
                try {
                    amayaRenren.getRennService().sendAsynRequest(param, new RennExecutor.CallBack() {

                        @Override
                        public void onSuccess(RennResponse response) {
                            if(listener != null) listener.onComplete(AmayaShareEnums.RENREN,AmayaShareConstants.AMAYA_TYPE_SHARE,null);
                        }

                        @Override
                        public void onFailed(String errorCode, String errorMessage) {
                            if(listener != null) listener.onException(AmayaShareEnums.RENREN,AmayaShareConstants.AMAYA_TYPE_SHARE,errorMessage);
                        }
                    });
                } catch (RennException e1) {
                    e1.printStackTrace();
                    if(listener != null) listener.onException(AmayaShareEnums.RENREN,AmayaShareConstants.AMAYA_TYPE_SHARE,e1.getMessage());
                }
            }else{
                listener.onException(AmayaShareEnums.RENREN,AmayaShareConstants.AMAYA_TYPE_SHARE,"Error:httpUrl isn't start with 'http://'");
            }
        }else if(listener != null){
            listener.onException(AmayaShareEnums.RENREN,AmayaShareConstants.AMAYA_TYPE_SHARE,"Error:not auth.");
        }
    }

    private void initRenRen(Context context) {
        if(amayaRenren == null) amayaRenren = RennClient.getInstance(context);
    }


    /************************************************RenRen END**************************************************/







    public void onDestroy(){
        amayaListener = null;
        tokens = null;
        amayaTencent = null;
        amayaShare = null;
        amayaWeiXin = null;
        amayaAuthInfo = null;
        mSsoHandler = null;
        amayaSinaApi = null;
        amayaIUListener = null;
        amaya = null;
    }
    
}
