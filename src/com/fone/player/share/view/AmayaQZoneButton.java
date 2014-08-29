/*
 * Copyright (C) 2010-2013 The SINA WEIBO Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fone.player.share.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import com.fone.player.share.util.AmayaTokenKeeper;
import com.fone.player.share.util.AmayaShareConstants;
import com.fone.player.share.util.AmayaShareEnums;
import com.fone.player.share.util.AmayaShareListener;
import com.tencent.connect.UserInfo;
import com.tencent.connect.common.Constants;
import com.tencent.connect.share.QzoneShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * 该类提供了一个简单的登录控件。
 * 该登陆控件只提供登录功能（SSO 登陆授权），它有三种内置的样式。
 * 
 * @author SINA
 * @since 2013-11-04
 */
public class AmayaQZoneButton extends AmayaButton implements OnClickListener,IUiListener {
    private static final String TAG = "AmayaQZoneButton";

    private OnClickListener mExternalOnClickListener;

	private AmayaShareListener amayaListener;
    private Tencent mTencent;
    private QzoneShare amayaShare;

    public AmayaQZoneButton(Context context) {
		this(context, null);
        initialize();
	}

	public AmayaQZoneButton(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
        initialize();
	}

	public AmayaQZoneButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize();
	}
    
    public void setExternalOnClickListener(OnClickListener listener) {
        mExternalOnClickListener = listener;
    }

    /**
	 * 按钮被点击时，调用该函数。
	 */
	@Override
	public void onClick(View v) {
	    // Give a chance to external listener
        if (mExternalOnClickListener != null) {
            mExternalOnClickListener.onClick(v);
        }
        if(mTencent == null){
            mTencent = Tencent.createInstance(AmayaShareConstants.AMAYA_QQ_ID, (Activity)getContext());
            AmayaTokenKeeper.readQQToken(getContext(), mTencent);
        }
        if(mTencent.isSessionValid()){
            amayaShare = new QzoneShare(getContext(),mTencent.getQQToken());
            readyShare(QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT,"http://www.qq.com","title","content",null);
        }else{
            mTencent.login((Activity)getContext(), "all", this);
        }
	}
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "-->onActivityResult " + requestCode  + " resultCode=" + resultCode);
            mTencent.handleLoginData(data, this);
            if(resultCode == Constants.RESULT_LOGIN) {
            Log.d(TAG, "-->onActivityResult handle logindata");
        }
    }

    private void initialize() {
    	setOnClickListener(this);
        mTencent = Tencent.createInstance(AmayaShareConstants.AMAYA_QQ_ID, getContext());
        AmayaTokenKeeper.readQQToken(getContext(), mTencent);
        Log.e("amaya","initQQ()...mTencent="+mTencent);
    }

    @Override
    public void addShareListener(AmayaShareListener amayaListener){
    	this.amayaListener = amayaListener;
    }

	@Override
	public void onCancel() {
		if(amayaListener != null) amayaListener.onCancel(AmayaShareEnums.SINA_WEIBO,AmayaShareConstants.AMAYA_TYPE_AUTH);
	}

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
                mTencent.setAccessToken(token, expires);
                mTencent.setOpenId(openId);
                AmayaTokenKeeper.saveQQToken(getContext(), mTencent);
            }
            Toast.makeText(getContext(),"授权成功,分享ing",Toast.LENGTH_SHORT).show();
            if(amayaShare == null) amayaShare = new QzoneShare(getContext(),mTencent.getQQToken());
            readyShare(QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT, "http://www.qq.com", "title", "content", null);
//            Bundle bundle = new Bundle();
//            bundle.putString(AmayaShareConstants.AMAYA_RESULT_USER_ID, openId);
//            bundle.putString(AmayaShareConstants.AMAYA_RESULT_EXPIRES_IN, expires);
//            bundle.putString(AmayaShareConstants.AMAYA_RESULT_ACCESS_TOKEN, token);
//            updateUserInfo(bundle);
//            Bundle bundle = new Bundle();
//            bundle.putString(AmayaShareConstants.AMAYA_RESULT_USER_NAME, name);
//            bundle.putString(AmayaShareConstants.AMAYA_RESULT_USER_ID, openid);
//            bundle.putString(AmayaShareConstants.AMAYA_RESULT_EXPIRES_IN, expiresIn);
//            bundle.putString(AmayaShareConstants.AMAYA_RESULT_ACCESS_TOKEN, accessToken);
//            if(amayaListener != null) amayaListener.onComplete(AmayaShareEnums.TENCENT_QQ, bundle);
        } catch(Exception e) {
            if(amayaListener != null) amayaListener.onException(AmayaShareEnums.TENCENT_QQ,AmayaShareConstants.AMAYA_TYPE_AUTH,e.getMessage());
        }
    }

    @Override
    public void onError(UiError uiError) {
        if(amayaListener != null) amayaListener.onException(AmayaShareEnums.TENCENT_QQ,AmayaShareConstants.AMAYA_TYPE_AUTH,uiError.errorDetail);
    }

    private void updateUserInfo(final Bundle bundle) {
        if (mTencent != null && mTencent.isSessionValid()) {
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
                            bundle.putString(AmayaShareConstants.AMAYA_RESULT_USER_NAME, name);
                            if(amayaListener != null) amayaListener.onComplete(AmayaShareEnums.TENCENT_QQ,AmayaShareConstants.AMAYA_TYPE_AUTH,bundle);
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
                    if(amayaListener != null) amayaListener.onCancel(AmayaShareEnums.TENCENT_QQ,AmayaShareConstants.AMAYA_TYPE_SHARE);
                }
            };
            UserInfo mInfo = new UserInfo(getContext(), mTencent.getQQToken());
            mInfo.getUserInfo(listener);
        } else {
            Log.e("amaya","授权失效....");
            mTencent.login((Activity)getContext(),"all",this);
        }
    }


    public static Bitmap getbitmap(String imageUri) {
        Log.v(TAG, "getbitmap:" + imageUri);
        // 显示网络上的图片
        Bitmap bitmap = null;
        try {
            URL myFileUrl = new URL(imageUri);
            HttpURLConnection conn = (HttpURLConnection) myFileUrl
                    .openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();

            Log.v(TAG, "image download finished." + imageUri);
        } catch (IOException e) {
            e.printStackTrace();
            Log.v(TAG, "getbitmap bmp fail---");
            return null;
        }
        return bitmap;
    }

    /**
     *
     * @param shareType     分享类型
     * @param title         标题
     * @param summary       内容
     * @param urls          图片url集合
     * @param targetUrl     目标链接(shareType != SHARE_TO_QZONE_TYPE_APP有效)
     */
    private void readyShare(int shareType,String targetUrl,String title,String summary,ArrayList<String> urls) {
        //QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT

        if(TextUtils.isEmpty(targetUrl)){
            Toast.makeText(getContext(),"targetUrl为必填项，请补充后分享",0).show();
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
        // String imageUrl = "XXX";
        // params.putString(Tencent.SHARE_TO_QQ_IMAGE_URL, imageUrl);
        params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imageUrls);
        doShareToQzone(params);
    }

    private void doShareToQzone(final Bundle params) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                amayaShare.shareToQzone((Activity)getContext(), params, new IUiListener() {

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
}
