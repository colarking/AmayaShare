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

package com.fone.player.share.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.tencent.tauth.Tencent;

/**
 * 该类定义了微博授权时
 * @author SINA
 * @since 2013-10-07
 */
public class AmayaTokenKeeper {
    private static final String PREFERENCES_NAME = "com_weibo_sdk_android";

    private static final String KEY_UID           = "uid";
    private static final String KEY_ACCESS_TOKEN  = "access_token";
    private static final String KEY_EXPIRES_IN    = "expires_in";
    
    /**
     * 保存 Token 对象�?SharedPreferences�?     * 
     * @param context 应用程序上下文环�?     * @param token   Token 对象
     */
    public static void writeAccessToken(Context context, Oauth2AccessToken token) {
        if (null == context || null == token) {
            return;
        }
        
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        Editor editor = pref.edit();
        editor.putString(KEY_UID, token.getUid());
        editor.putString(KEY_ACCESS_TOKEN, token.getToken());
        editor.putLong(KEY_EXPIRES_IN, token.getExpiresTime());
        editor.commit();
    }

    /**
     * �?SharedPreferences 读取 Token 信息�?     * 
     * @param context 应用程序上下文环�?     * 
     * @return 返回 Token 对象
     */
    public static Oauth2AccessToken readAccessToken(Context context) {
        if (null == context) {
            return null;
        }
        
        Oauth2AccessToken token = new Oauth2AccessToken();
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        token.setUid(pref.getString(KEY_UID, ""));
        token.setToken(pref.getString(KEY_ACCESS_TOKEN, ""));
        token.setExpiresTime(pref.getLong(KEY_EXPIRES_IN, 0));
        return token;
    }

    /**
     * 清空 SharedPreferences �?Token信息�?     * 
     * @param context 应用程序上下文环�?     */
    public static void clear(Context context) {
        if (null == context) {
            return;
        }
        
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        Editor editor = pref.edit();
        editor.clear();
        editor.commit();
    }



    public static void readQQToken(Context context,Tencent mTencent) {
        if (null == context) {
            return;
        }
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        String expires = pref.getString("AMAYA_QQ_Expires", "");
        if(!TextUtils.isEmpty(expires)){
            mTencent.setAccessToken(pref.getString("AMAYA_QQ_TOKEN", ""), expires);
            mTencent.setOpenId(pref.getString("AMAYA_QQ_OPENID", ""));
        }
    }

    public static void saveQQToken(Context context,Tencent mTencent){
        if (null == context) {
            return;
        }
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        pref.edit().putString("AMAYA_QQ_TOKEN",mTencent.getAccessToken()).putString("AMAYA_QQ_Expires",String.valueOf(mTencent.getExpiresIn())).putString("AMAYA_QQ_OPENID",mTencent.getOpenId()).commit();
    }
}
