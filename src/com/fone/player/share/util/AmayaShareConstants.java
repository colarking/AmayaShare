package com.fone.player.share.util;

import com.tencent.connect.common.Constants;

public class AmayaShareConstants {

    /**
     * 新浪微博相关常量
     */
	public static final String APP_KEY      = "2045436852";
	public static final String REDIRECT_URL = "http://www.sina.com";
	public static String AMAYA_SINA_KEY = "1139717123";
	public static String AMAYA_SINA_CONSUMER_SECRET = "5a2831344a97502975729d16cad70219";
	public static String AMAYA_SINA_REDIRECTURL = "http://www.100tv.com/";
	public static final String AMAYA_SINA_SCOPE = 
            "email,direct_messages_read,direct_messages_write,"
            + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
            + "follow_app_official_microblog," + "invitation_write";
    /**
     * QQ常量
     */
    public static String AMAYA_QQ_ID= "100460854";


    /**
     * Activity回调requestCode
     */
    public static final int AMAYA_ACTIVITY_RESULT_TXWEIBO = 821;
	public static final int AMAYA_ACTIVITY_RESULT_SINAWEIBO = 32973;
	public static final int AMAYA_ACTIVITY_RESULT_QQ =  Constants.REQUEST_API;


    /**
     * Bundle数据参数
     */
    public static final String AMAYA_RESULT_USER_NAME = "userName";
	public static final String AMAYA_RESULT_USER_ID = "uid";
	public static final String AMAYA_RESULT_ACCESS_TOKEN = "access_token";
	public static final String AMAYA_RESULT_EXPIRES_IN = "expires_in";
	public static final String AMAYA_RESULT_SHARE = "share_back";

    public static boolean AMAYA_TYPE_AUTH = true;
    public static boolean AMAYA_TYPE_SHARE = false;
}
