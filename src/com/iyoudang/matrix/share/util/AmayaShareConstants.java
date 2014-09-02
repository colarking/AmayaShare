package com.iyoudang.matrix.share.util;

public class AmayaShareConstants {

    /**
     * 新浪微博相关常量
     */
	public static String AMAYA_SINA_KEY = "538547686";//"1139717123";
	public static String AMAYA_SINA_REDIRECTURL = "http://www.sina.com";
	public static final String AMAYA_SINA_SCOPE = 
            "email,direct_messages_read,direct_messages_write,"
            + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
            + "follow_app_official_microblog," + "invitation_write";
    /**
     * QQ常量
     */
    public static String AMAYA_QQ_ID= "100460854";

    /**
     * 微信常量
     */
    public static final String AMAYA_WX_ID = "wx69ebdd4f380f47c7";//"wxb23baa60e487655b";

    /**
     * Activity回调requestCode
     */
    public static final int AMAYA_ACTIVITY_RESULT_TXWEIBO = 821;
	public static final int AMAYA_ACTIVITY_RESULT_SINAWEIBO = 32973;
	public static final int AMAYA_ACTIVITY_RESULT_QQ =  10100;//com.tencent.connect.common.Constants.REQUEST_API;


    /**
     * Bundle数据参数
     */
    public static final String AMAYA_RESULT_USER_NAME = "userName";
	public static final String AMAYA_RESULT_USER_ID = "uid";
	public static final String AMAYA_RESULT_ACCESS_TOKEN = "access_token";
	public static final String AMAYA_RESULT_EXPIRES_IN = "expires_in";
	public static final String AMAYA_RESULT_SHARE = "share_back";

    /**
     * 用于通用AmayaShareListener回调中第二个参数
     */
    public static boolean AMAYA_TYPE_AUTH = true;
    public static boolean AMAYA_TYPE_SHARE = false;
}
