package com.fone.player.share.util;

import android.os.Bundle;

public interface AmayaShareListener {
    /**
     *
     * @param enumKey       (@link AmayaShareEnums)
     * @param authOrShare   true:AUTH授权；false:SHARE分享
     * @param values        返回数据
     */
	void onComplete(AmayaShareEnums enumKey,boolean authOrShare,Bundle values);

    /**
     *
     * @param enumKey      (@link AmayaShareEnums)
     * @param authOrShare  true:AUTH授权；false:SHARE分享
     */
	void onCancel(AmayaShareEnums enumKey,boolean authOrShare);

    /**
     *
     * @param enumKey       (@link AmayaShareEnums)
     * @param authOrShare   true:AUTH授权；false:SHARE分享
     * @param msg           异常信息
     */
	void onException(AmayaShareEnums enumKey,boolean authOrShare,String msg);
}
