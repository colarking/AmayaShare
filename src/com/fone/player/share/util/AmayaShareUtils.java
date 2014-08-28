package com.fone.player.share.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.fone.player.R;
import com.sina.weibo.sdk.api.*;
import com.sina.weibo.sdk.api.share.*;
import com.sina.weibo.sdk.api.share.IWeiboHandler.Response;
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.utils.Utility;
import com.tencent.connect.UserInfo;
import com.tencent.connect.common.Constants;
import com.tencent.connect.share.QzoneShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.SoftReference;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Smith
 * Date: 14-8-28
 * Time: 下午2:34
 * To change this template use File | Settings | File Templates.
 */
public class AmayaShareUtils {

    private Tencent amayaTencent;
    private QzoneShare amayaShare;
	private IWeiboShareAPI amayaSinaWeibo;

    private static AmayaShareUtils amaya;
    private AmayaShareUtils(){
    	
    }
    
    public synchronized static AmayaShareUtils instance(){
    	if(amaya == null){
    		amaya = new AmayaShareUtils();
    	}
    	return amaya;
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
    }

    public void shareToQZone(final Activity context, final AmayaShareListener amayaListener,final int shareType,final String targetUrl,final String title,final String summary,final ArrayList<String> urls){
        if(context == null) {
            amayaListener.onException(AmayaShareEnums.TENCENT_QZONE,AmayaShareConstants.AMAYA_TYPE_SHARE,"Error:context is null");
            return;
        }
        initQQ(context);
        if(amayaTencent == null) {
            amayaListener.onException(AmayaShareEnums.TENCENT_QZONE,AmayaShareConstants.AMAYA_TYPE_SHARE,context.getString(R.string.amaya_auth_fail_step_0));
        }else if(amayaTencent.isSessionValid()){
            amayaShare = new QzoneShare(context,amayaTencent.getQQToken());
            readyShare(context,amayaListener,shareType,targetUrl,title,summary,urls);
        }else if(amayaListener instanceof Activity){
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
                        amayaShare = new QzoneShare(context,amayaTencent.getQQToken());
                        readyShare(context,amayaListener,shareType,targetUrl,title,summary,urls);
//                        updateUserInfo(context,amayaListener,bundle);
                    } catch(Exception e) {
                        if(amayaListener != null) amayaListener.onException(AmayaShareEnums.TENCENT_QQ,AmayaShareConstants.AMAYA_TYPE_AUTH,e.getMessage());
                    }
                }

                @Override
                public void onError(UiError uiError) {
                    if(amayaListener != null) amayaListener.onException(AmayaShareEnums.TENCENT_QZONE, AmayaShareConstants.AMAYA_TYPE_AUTH, context.getString(R.string.amaya_auth_no));

                }

                @Override
                public void onCancel() {
                    if(amayaListener != null) amayaListener.onCancel(AmayaShareEnums.TENCENT_QZONE,AmayaShareConstants.AMAYA_TYPE_AUTH);

                }
            });
        }else{
            amayaListener.onException(AmayaShareEnums.TENCENT_QZONE,AmayaShareConstants.AMAYA_TYPE_AUTH,context.getString(R.string.amaya_auth_no));
        }
    }

    public void updateUserInfo(final Context context,final AmayaShareListener amayaListener,final Bundle bundle) {
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

    private void readyShare(final Activity context,AmayaShareListener amayaListener,int shareType,String targetUrl,String title,String summary,ArrayList<String> urls) {
        //QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT
        if(TextUtils.isEmpty(targetUrl)){
            Toast.makeText(context, "targetUrl为必填项，请补充后分享", 0).show();
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
        doShareToQzone(context,amayaListener,params);
    }

    private void doShareToQzone(final Activity context,final AmayaShareListener amayaListener,final Bundle params) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                amayaShare.shareToQzone(context, params, new IUiListener() {
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
    /************************************************QQ分享部分  END**************************************************/
    /************************************************SINA WEIBO分享部分 START**************************************************/
    protected void initSinaWeibo(final Context context) {
    	if(amayaSinaWeibo == null){
    		amayaSinaWeibo = WeiboShareSDK.createWeiboAPI(context, AmayaShareConstants.AMAYA_SINA_KEY);
    		// 注册第三方应用到微博客户端中，注册成功后该应用将显示在微博的应用列表中。
    		// 但该附件栏集成分享权限需要合作申请，详情请查看 Demo 提示
    		// NOTE：请务必提前注册，即界面初始化的时候或是应用程序初始化时，进行注册
    		amayaSinaWeibo.registerApp();
    	}
         // 如果未安装微博客户端，设置下载微博对应的回调
         if (!amayaSinaWeibo.isWeiboAppInstalled()) {
             amayaSinaWeibo.registerWeiboDownloadListener(new IWeiboDownloadListener() {
                 @Override
                 public void onCancel() {
                     Toast.makeText(context, 
                             "R.string.weibosdk_demo_cancel_download_weibo", 
                             Toast.LENGTH_SHORT).show();
                 }
             });
         }
         
 		// 当 Activity 被重新初始化时（该 Activity 处于后台时，可能会由于内存不足被杀掉了），
         // 需要调用 {@link IWeiboShareAPI#handleWeiboResponse} 来接收微博客户端返回的数据。
         // 执行成功，返回 true，并调用 {@link IWeiboHandler.Response#onResponse}；
         // 失败返回 false，不调用上述回调
    }
    
    private SoftReference<Response> amayaResponse = new SoftReference<Response>(null);
    public void onNewIntent(Intent intent,final AmayaShareListener amayaListener) {
        
        // 从当前应用唤起微博并进行分享后，返回到当前应用时，需要在此处调用该函数
        // 来接收微博客户端返回的数据；执行成功，返回 true，并调用
        // {@link IWeiboHandler.Response#onResponse}；失败返回 false，不调用上述回调
    	if(amayaListener == null ) return;
    	if(amayaSinaWeibo == null && amayaListener != null && amayaListener instanceof Context){
    		initSinaWeibo((Context)amayaListener);
    	}
        Response response = amayaResponse.get();
        if(response == null){
    		response = new Response(){

    			@Override
    			public void onResponse(BaseResponse baseResp) {
    				switch (baseResp.errCode) {
    		         case WBConstants.ErrorCode.ERR_OK:
    		        	 amayaListener.onComplete(AmayaShareEnums.SINA_WEIBO, AmayaShareConstants.AMAYA_TYPE_SHARE, null);
    		             break;
    		         case WBConstants.ErrorCode.ERR_CANCEL:
    		        	 amayaListener.onCancel(AmayaShareEnums.SINA_WEIBO, AmayaShareConstants.AMAYA_TYPE_SHARE);
    		             break;
    		         case WBConstants.ErrorCode.ERR_FAIL:
    		        	 amayaListener.onException(AmayaShareEnums.SINA_WEIBO, AmayaShareConstants.AMAYA_TYPE_SHARE,baseResp.errMsg);
    		             break;
    		         }
    			}

            };
            amayaResponse = new SoftReference<Response>(response);
        }
        if(amayaSinaWeibo != null) amayaSinaWeibo.handleWeiboResponse(intent, amayaResponse.get());
    }
    
    /**
     * 创建文本消息对象。
     * 
     * @return 文本消息对象。
     */
    private TextObject getTextObj(String text) {
        TextObject textObject = new TextObject();
        textObject.text = text;
        return textObject;
    }

    /**
     * 创建图片消息对象。
     * 
     * @return 图片消息对象。
     */
    private ImageObject getImageObj(Bitmap bitmap) {
        ImageObject imageObject = new ImageObject();
        imageObject.setImageObject(bitmap);
        return imageObject;
    }

    /**
     * 创建多媒体（网页）消息对象。
     * 
     * @return 多媒体（网页）消息对象。
     */
    private WebpageObject getWebpageObj(String title,String desc,String actionUrl,Bitmap bitmap) {
        WebpageObject mediaObject = new WebpageObject();
        mediaObject.identify = Utility.generateGUID();
        mediaObject.title = title;
        mediaObject.description =desc;
        
        // 设置 Bitmap 类型的图片到视频对象里
        mediaObject.setThumbImage(bitmap);
        mediaObject.actionUrl = actionUrl;
        mediaObject.defaultText = "Webpage 默认文案";
        return mediaObject;
    }

    /**
     * 创建多媒体（音乐）消息对象。
     * 
     * @return 多媒体（音乐）消息对象。
     */
    private MusicObject getMusicObj(String title,String desc,String actionUrl,Bitmap bitmap) {
        // 创建媒体消息
        MusicObject musicObject = new MusicObject();
        musicObject.identify = Utility.generateGUID();
        musicObject.title = title;
        musicObject.description = desc;
        
        // 设置 Bitmap 类型的图片到视频对象里
        musicObject.setThumbImage(bitmap);
        musicObject.actionUrl = actionUrl;
        musicObject.dataUrl = "www.weibo.com";
        musicObject.dataHdUrl = "www.weibo.com";
        musicObject.duration = 10;
        musicObject.defaultText = "Music 默认文案";
        return musicObject;
    }

    /**
     * 创建多媒体（视频）消息对象。
     * 
     * @return 多媒体（视频）消息对象。
     */
    private VideoObject getVideoObj(String title,String des,String actionUrl,Bitmap bitmap) {
        // 创建媒体消息
        VideoObject videoObject = new VideoObject();
        videoObject.identify = Utility.generateGUID();
        videoObject.title = title;
        videoObject.description = des;
        
        // 设置 Bitmap 类型的图片到视频对象里
        videoObject.setThumbImage(bitmap);
        videoObject.actionUrl = actionUrl;
        videoObject.dataUrl = "www.weibo.com";
        videoObject.dataHdUrl = "www.weibo.com";
        videoObject.duration = 10;
        videoObject.defaultText = "Vedio 默认文案";
        return videoObject;
    }

    /**
     * 创建多媒体（音频）消息对象。
     * 
     * @return 多媒体（音乐）消息对象。
     */
	private VoiceObject getVoiceObj(String title,String des,String actionUrl,Bitmap bitmap) {
        // 创建媒体消息
        VoiceObject voiceObject = new VoiceObject();
        voiceObject.identify = Utility.generateGUID();
        voiceObject.title =title;
        voiceObject.description = des;
        
        // 设置 Bitmap 类型的图片到视频对象里
        if(bitmap != null) voiceObject.setThumbImage(bitmap);
        voiceObject.actionUrl = actionUrl;
        voiceObject.dataUrl = "www.weibo.com";
        voiceObject.dataHdUrl = "www.weibo.com";
        voiceObject.duration = 10;
        voiceObject.defaultText = "Voice 默认文案";
        return voiceObject;
    }


    /**
     * 第三方应用发送请求消息到微博，唤起微博分享界面。
     * @see {@link #sendMultiMessage} 或者 {@link #sendSingleMessage}
     */
    private void sendMessage(Context context,boolean hasText, boolean hasImage,
                             boolean hasWebpage, boolean hasMusic, boolean hasVideo, boolean hasVoice) {

        if (amayaSinaWeibo.isWeiboAppSupportAPI()) {
            int supportApi = amayaSinaWeibo.getWeiboAppSupportAPI();
            if (supportApi >= 10351 /*ApiUtils.BUILD_INT_VER_2_2*/) {
//                sendMultiMessage(hasText, hasImage, hasWebpage, hasMusic, hasVideo, hasVoice);
            } else {
//                sendSingleMessage(hasText, hasImage, hasWebpage, hasMusic, hasVideo/*, hasVoice*/);
            }
        } else {
            Toast.makeText(context, R.string.weibosdk_demo_not_support_api_hint, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 第三方应用发送请求消息到微博，唤起微博分享界面。
     * 当{@link com.sina.weibo.sdk.api.share.IWeiboShareAPI#getWeiboAppSupportAPI()} < 10351 时，只支持分享单条消息，即
     * 文本、图片、网页、音乐、视频中的一种，不支持Voice消息。
     *
     */
    private void sendSingleMessage(Context context,AmayaShareListener amayaListener,BaseMediaObject obj) {
        initSinaWeibo(context);
        // 1. 初始化微博的分享消息
        // 用户可以分享文本、图片、网页、音乐、视频中的一种
        WeiboMessage weiboMessage = new WeiboMessage();
            weiboMessage.mediaObject = obj;
        // 2. 初始化从第三方到微博的消息请求
        SendMessageToWeiboRequest request = new SendMessageToWeiboRequest();
        // 用transaction唯一标识一个请求
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.message = weiboMessage;
        // 3. 发送请求消息到微博，唤起微博分享界面
        amayaSinaWeibo.sendRequest(request);
    }


    /**
     * 第三方应用发送请求消息到微博，唤起微博分享界面。
     * 注意：当 {@link com.sina.weibo.sdk.api.share.IWeiboShareAPI#getWeiboAppSupportAPI()} >= 10351 时，支持同时分享多条消息，
     * 同时可以分享文本、图片以及其它媒体资源（网页、音乐、视频、声音中的一种）。
     *
     * @param context    分享的内容是否有文本
     * @param amayaListener   分享的内容是否有图片
     * @param txtObj 分享的内容是否有网页
     */
    private void sendMultiMessage(Context context,AmayaShareListener amayaListener,TextObject txtObj,ImageObject imgObj,BaseMediaObject mediaObj) {

        // 1. 初始化微博的分享消息
        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
        weiboMessage.textObject = txtObj;
        weiboMessage.imageObject = imgObj;
        // 用户可以分享其它媒体资源（网页、音乐、视频、声音中的一种）
        weiboMessage.mediaObject = mediaObj;
        // 2. 初始化从第三方到微博的消息请求
        SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
        // 用transaction唯一标识一个请求
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.multiMessage = weiboMessage;

        // 3. 发送请求消息到微博，唤起微博分享界面
        amayaSinaWeibo.sendRequest(request);
    }
    /************************************************SINA WEIBO分享部分 END**************************************************/

    
}
