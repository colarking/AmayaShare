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

import java.text.SimpleDateFormat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.fone.player.share.util.AccessTokenKeeper;
import com.fone.player.share.util.AmayaShareConstants;
import com.fone.player.share.util.AmayaShareEnums;
import com.fone.player.share.util.AmayaShareListener;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuth.AuthInfo;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.utils.LogUtil;

/**
 * �����ṩ��һ���򵥵ĵ�¼�ؼ���
 * �õ�½�ؼ�ֻ�ṩ��¼���ܣ�SSO ��½��Ȩ���������������õ���ʽ��
 * 
 * @author SINA
 * @since 2013-11-04
 */
public abstract class AmayaButton extends Button {	
    private static final String TAG = "LoginButton";

	public AmayaButton(Context context) {
		this(context, null);
	}
	
	/**
	 * �� XML �����ļ��д���һ����ť��
	 * 
	 * @see View#View(Context, AttributeSet)
	 */
	public AmayaButton(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AmayaButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public abstract void onActivityResult(int requestCode, int resultCode, Intent data);
	public abstract void addShareListener(AmayaShareListener amayaListener);
    
}
