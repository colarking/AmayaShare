package com.fone.player.share.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.fone.player.R;
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
public class AmayaShareUtils implements RequestListener, IUiListener {

    private static final String TAG = "AmayaShareUtils";
    private static AmayaShareUtils amaya;


    /**
     * 腾讯相关实例
     */
    private Tencent amayaTencent;
    private QzoneShare amayaShare;


    /**
     * Sina微博相关实例
     */
    private WeiboAuth.AuthInfo amayaAuthInfo;
    private SsoHandler mSsoHandler;
    private StatusesAPI amayaSinaApi;
    private AmayaShareListener amayaListener;

    private AmayaShareUtils(){}
    
    public synchronized static AmayaShareUtils instance(){
    	if(amaya == null){
    		amaya = new AmayaShareUtils();
    	}
    	return amaya;
    }

    public boolean isAuthed(AmayaShareEnums type, Context context) {
        boolean authed = false;
        switch (type){
            case SINA_WEIBO:
                authed = initSinaWeibo(context);
                break;
            case TENCENT_QQ:
            case TENCENT_WEIBO:
            case TENCENT_QZONE:
                initQQ(context);
                if(amayaTencent != null) authed = amayaTencent.isSessionValid();
                break;

        }
        return authed;
    }
    /************************************************QQ分享部分 START*************************************************
     */
    /**
     *
     * @param context
     */
    private void initQQ(Context context) {
        if(amayaTencent == null) {
            amayaTencent =  Tencent.createInstance(AmayaShareConstants.AMAYA_QQ_ID,context);
            AmayaTokenKeeper.readQQToken(context,amayaTencent);
        }
        if(amayaShare == null && amayaTencent != null){
            amayaShare = new QzoneShare(context,amayaTencent.getQQToken());
        }
    }

    public void auth(AmayaShareEnums enums,Activity activity,AmayaShareListener listener){
        switch (enums){
            case SINA_WEIBO:
                authSinaWeibo(activity,listener);
                break;
            case TENCENT_QQ:
            case TENCENT_QZONE:
                authQQ(activity,listener,enums);
                break;
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
        amayaTencent.login(context, "all", new IUiListener() {
            @Override
            public void onComplete(Object values) {
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
                    }
                    Bundle bundle = new Bundle();
                    bundle.putString(AmayaShareConstants.AMAYA_RESULT_USER_ID, openId);
                    bundle.putString(AmayaShareConstants.AMAYA_RESULT_EXPIRES_IN, expires);
                    bundle.putString(AmayaShareConstants.AMAYA_RESULT_ACCESS_TOKEN, token);
                    if(amayaShare == null) amayaShare = new QzoneShare(context,amayaTencent.getQQToken());
                    if(amayaListener != null) amayaListener.onComplete(enums, AmayaShareConstants.AMAYA_TYPE_AUTH, bundle);
                } catch(Exception e) {
                    if(amayaListener != null) amayaListener.onException(enums,AmayaShareConstants.AMAYA_TYPE_AUTH,e.getMessage());
                }
            }

            @Override
            public void onError(UiError uiError) {
                if(amayaListener != null) amayaListener.onException(enums, AmayaShareConstants.AMAYA_TYPE_AUTH, context.getString(R.string.amaya_auth_no));
            }

            @Override
            public void onCancel() {
                if(amayaListener != null) amayaListener.onCancel(enums,AmayaShareConstants.AMAYA_TYPE_AUTH);
            }
        });
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

    public void shareToQQ(final Activity activity,final AmayaShareListener amayaListener)
    {
        this.amayaListener = amayaListener;
        Bundle bundle = new Bundle();
        //这条分享消息被好友点击后的跳转URL。
        bundle.putString( QQShare.SHARE_TO_QQ_TARGET_URL, "http://connect.qq.com/");
        //分享的标题。注：PARAM_TITLE、PARAM_IMAGE_URL、PARAM_SUMMARY不能全为空，最少必须有一个是有值的。
        bundle.putString(QQShare.SHARE_TO_QQ_TITLE, "我在测试");
        //分享的图片URL
        bundle.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, "http://img3.cache.netease.com/photo/0005/2013-03-07/8PBKS8G400BV0005.jpg");
        //分享的消息摘要，最长50个字
        bundle.putString(QQShare.SHARE_TO_QQ_SUMMARY, "测试");
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
    /************************************************SINA WEIBO分享部分 START**************************************************/
    public boolean initSinaWeibo(Context context) {
        Oauth2AccessToken token = AmayaTokenKeeper.readSinaToken(context);
        boolean authed = token.isSessionValid();
        if(authed){
            amayaSinaApi = new StatusesAPI(token);
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
    public void onActivityResult(final AmayaShareEnums enums,final Context mContext,int requestCode, int resultCode, Intent data) {
        switch (enums){
            case SINA_WEIBO:
                if (mSsoHandler != null) {
                    mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
                }
                break;
            case TENCENT_QQ:
            case TENCENT_QZONE:
            case TENCENT_WEIBO:
                final boolean isShare = amayaTencent.isSessionValid();
                Log.e("amaya","onActivityResult()...enums="+enums+"--isShare="+isShare);
                amayaTencent.handleLoginData(data,new IUiListener() {
                    @Override
                    public void onComplete(Object values) {
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
                                AmayaTokenKeeper.saveQQToken(mContext, amayaTencent);
                            }
                            Bundle bundle = new Bundle();
                            bundle.putString(AmayaShareConstants.AMAYA_RESULT_USER_ID, openId);
                            bundle.putString(AmayaShareConstants.AMAYA_RESULT_EXPIRES_IN, expires);
                            bundle.putString(AmayaShareConstants.AMAYA_RESULT_ACCESS_TOKEN, token);
                //            Bundle bundle = new Bundle();
                //            bundle.putString(AmayaShareConstants.AMAYA_RESULT_USER_NAME, name);
                //            bundle.putString(AmayaShareConstants.AMAYA_RESULT_USER_ID, openid);
                //            bundle.putString(AmayaShareConstants.AMAYA_RESULT_EXPIRES_IN, expiresIn);
                //            bundle.putString(AmayaShareConstants.AMAYA_RESULT_ACCESS_TOKEN, accessToken);
                            if(amayaListener != null) amayaListener.onComplete(enums,isShare, bundle);
                        } catch(Exception e) {
                            if(amayaListener != null) amayaListener.onException(enums,isShare,e.getMessage());
                        }
                    }

                    @Override
                    public void onError(UiError uiError) {
                        if(amayaListener != null) amayaListener.onException(enums,isShare,uiError.errorDetail);
                    }

                    @Override
                    public void onCancel() {
                        if(amayaListener != null) amayaListener.onCancel(enums,isShare);
                    }
                });
                break;
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
        if(amayaSinaApi != null && listener != null){
            this.amayaListener = listener;
            amayaSinaApi.update(content,lat,lon,this);
        }else{

        }
//        WeiboParameters params = buildUpdateParams(content, lat, lon);
//        requestAsync(sAPIList.get(WRITE_API_UPDATE), params, HTTPMETHOD_POST, listener);
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

    
}
