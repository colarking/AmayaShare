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

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import com.fone.player.R;
import com.fone.player.share.util.AmayaShareListener;

/**
 * �����ṩ��һ���򵥵ĵ�¼�ؼ���
 * �õ�½�ؼ�ֻ�ṩ��¼���ܣ�SSO ��½��Ȩ���������������õ���ʽ��
 * 
 * @author SINA
 * @since 2013-11-04
 */
public abstract class AmayaButton extends Button {	
    private static final String TAG = "AmayaButton";

	public AmayaButton(Context context) {
		this(context, null);
        initUI();
	}
	
	/**
	 * �� XML �����ļ��д���һ����ť��
	 * 
	 * @see View#View(Context, AttributeSet)
	 */
	public AmayaButton(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
        initUI();
	}

    private void initUI() {
        setBackgroundResource(R.drawable.text_view_bg_selector);
        setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
        setPadding(15,15,15,15);
//        int white = getResources().getColor(R.color.white);
//        int l = getResources().getColor(R.color.text_pressed_green);
//        setTextColor(createColorStateList(white, l, l, l));
        ColorStateList list = getResources().getColorStateList(R.drawable.text_view_selector);
        setTextColor(list);
    }

    public AmayaButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
        initUI();
	}

    public ColorStateList createColorStateList(int normal, int pressed, int focused, int unable) {
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
	public abstract void onActivityResult(int requestCode, int resultCode, Intent data);
	public abstract void addShareListener(AmayaShareListener amayaListener);
    
}
