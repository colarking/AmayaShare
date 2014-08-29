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
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import com.fone.player.share.util.*;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuth.AuthInfo;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.utils.LogUtil;

/**
 * 该类提供了一个简单的登录控件。
 * 该登陆控件只提供登录功能（SSO 登陆授权），它有三种内置的样式。
 * 
 * @author SINA
 * @since 2013-11-04
 */
public class AmayaSinaWeiboButton extends AmayaButton implements OnClickListener,WeiboAuthListener {	
    private static final String TAG = "LoginButton";

    /** 微博授权时，启动 SSO 界面的 Activity */
	private Context mContext;
    /** 授权认证所需要的信息 */
    private AuthInfo mAuthInfo;
    /** SSO 授权认证实例 */
    private SsoHandler mSsoHandler;
    /** 点击 Button 时，额外的 Listener */
    private OnClickListener mExternalOnClickListener;

	private AmayaShareListener amayaListener;
    
    /**
     * 创建一个登录按钮。
     * 
     * @see View#View(Context)
     */
	public AmayaSinaWeiboButton(Context context) {
		this(context, null);
	}
	
	/**
	 * 从 XML 配置文件中创建一个按钮。
	 * 
	 * @see View#View(Context, AttributeSet)
	 */
	public AmayaSinaWeiboButton(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

    /**
     * 从 XML 配置文件以及样式中创建一个按钮。
     * 
     * @see View#View(Context, AttributeSet, int)
     */
	public AmayaSinaWeiboButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize(context);
	}

    /**
     * 设置一个额外的 Button 点击时的 Listener。
     * 当触发 Button 点击事件时，会先调用该 Listener，给使用者一个可访问的机会，
     * 然后再调用内部默认的处理。
     * <p><b>注意：一般情况下，使用者不需要调用该方法，除非有其它必要性。<b></p>
     * 
     * @param listener Button 点击时的 Listener
     */
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

        readyAuth();
	}

    public void readyAuth() {
        if (null == mSsoHandler && mAuthInfo != null) {
            WeiboAuth weiboAuth = new WeiboAuth(mContext, mAuthInfo);
            mSsoHandler = new SsoHandler((Activity)mContext, weiboAuth);
        }

        if (mSsoHandler != null) {
            mSsoHandler.authorize(this);
        } else {
            LogUtil.e(TAG, "Please setWeiboAuthInfo(...) for first");
        }
    }

    /**
     * 使用该控件进行授权登陆时，需要手动调用该函数。
     * <p>
     * 重要：使用该控件的 Activity 必须重写 {@link Activity#onActivityResult(int, int, Intent)}，
     *       并在内部调用该函数，否则无法授权成功。</p>
     * <p>Sample Code：</p>
     * <pre class="prettyprint">
     * protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     *     super.onActivityResult(requestCode, resultCode, data);
     *     
     *     // 在此处调用
     *     mLoginButton.onActivityResult(requestCode, resultCode, data);
     * }
     * </pre>
     * @param requestCode 请查看 {@link Activity#onActivityResult(int, int, Intent)}
     * @param resultCode  请查看 {@link Activity#onActivityResult(int, int, Intent)}
     * @param data        请查看 {@link Activity#onActivityResult(int, int, Intent)}
     */
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }

    /**
     * 按钮初始化函数。
     * 
     * @param context 上下文环境，一般为放置该 Button 的 Activity
     */
    private void initialize(Context context) {
    	mContext = context;
    	setOnClickListener(this);
    	mAuthInfo = new AuthInfo(context, AmayaShareConstants.AMAYA_SINA_KEY, AmayaShareConstants.AMAYA_SINA_REDIRECTURL,AmayaShareConstants.AMAYA_SINA_SCOPE);
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
	public void onComplete(Bundle values) {
		 Oauth2AccessToken accessToken = Oauth2AccessToken.parseAccessToken(values);
         if (accessToken != null && accessToken.isSessionValid()) {
             AmayaTokenKeeper.saveSinaToken(getContext(), accessToken);
         }
         if(amayaListener != null) amayaListener.onComplete(AmayaShareEnums.SINA_WEIBO,AmayaShareConstants.AMAYA_TYPE_AUTH, values);
	}

	@Override
	public void onWeiboException(WeiboException arg0) {
		if(amayaListener != null) amayaListener.onException(AmayaShareEnums.SINA_WEIBO,AmayaShareConstants.AMAYA_TYPE_AUTH,arg0.getMessage());
		
	}
}
