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

package com.iyoudang.matrix.share.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import com.iyoudang.matrix.share.AmayaAuthorize;
import com.iyoudang.matrix.share.util.AmayaShareListener;
import com.iyoudang.matrix.share.util.AmayaShareConstants;
import com.iyoudang.matrix.share.util.AmayaShareEnums;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.exception.WeiboException;

/**
 * 该类提供了一个简单的登录控件。
 * 该登陆控件只提供登录功能（SSO 登陆授权），它有三种内置的样式。
 * 
 * @author SINA
 * @since 2013-11-04
 */
public class AmayaTXWeiboButton extends AmayaButton implements OnClickListener,WeiboAuthListener {
    private static final String TAG = "LoginButton";

    private OnClickListener mExternalOnClickListener;

	private AmayaShareListener amayaListener;
    
	public AmayaTXWeiboButton(Context context) {
		this(context, null);
        initialize(context);
	}
	
	public AmayaTXWeiboButton(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
        initialize(context);
	}

	public AmayaTXWeiboButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize(context);
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
        Context c = getContext();
        if(c instanceof Activity){
        	Intent i = new Intent(c, AmayaAuthorize.class);
        	((Activity)c).startActivityForResult(i, AmayaShareConstants.AMAYA_ACTIVITY_RESULT_TXWEIBO);
        }
	    
	}
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(amayaListener != null && data != null){
			amayaListener.onComplete(AmayaShareEnums.TENCENT_WEIBO,AmayaShareConstants.AMAYA_TYPE_AUTH, data.getExtras());
		}
    }

    /**
     * 按钮初始化函数。
     * 
     * @param context 上下文环境，一般为放置该 Button 的 Activity
     */
    private void initialize(Context context) {
    	setOnClickListener(this);
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
         if(amayaListener != null) amayaListener.onComplete(AmayaShareEnums.SINA_WEIBO,AmayaShareConstants.AMAYA_TYPE_AUTH, values);
	}

	@Override
	public void onWeiboException(WeiboException arg0) {
		if(amayaListener != null) amayaListener.onException(AmayaShareEnums.SINA_WEIBO,AmayaShareConstants.AMAYA_TYPE_AUTH,arg0.getMessage());
		
	}
}
