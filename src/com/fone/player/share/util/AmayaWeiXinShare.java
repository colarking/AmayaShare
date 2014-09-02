package com.fone.player.share.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;
import com.fone.player.R;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;

public class AmayaWeiXinShare {

	private static final String TAG = "AmayaWeiXinShare";
	public static final String APP_ID = "wxb23baa60e487655b";
//	public static final String APP_ID = "wx5e579a4b2c31ad26";
	
	private static final int THUMB_SIZE = 100;
    private final Bitmap defaultBitmap;

    private IWXAPI wxApi;

	public AmayaWeiXinShare(Context mContext){
        defaultBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher);
        wxApi = WXAPIFactory.createWXAPI(mContext, APP_ID, true);
		wxApi.registerApp(APP_ID);
	}
	
	public void shareMessage(final boolean toCircle,final String title,final String description, final String imagePath,final String imageUrl,final String webpageUrl,
			final AmayaShareListener amayaShareListener) {
		Log.v(TAG, "shareMessage start --- ");
//		Log.v(TAG, "shareMessage  shareInfo : >> " + shareInfo.toString());
		if(TextUtils.isEmpty(title)){
			if(amayaShareListener!=null){
                amayaShareListener.onException(AmayaShareEnums.TENCENT_WEIXIN, AmayaShareConstants.AMAYA_TYPE_SHARE, "title is NULL");
			}
			return ;
		}
		
		if(!wxApi.isWXAppInstalled()){
            if(amayaShareListener!=null){
                amayaShareListener.onException(AmayaShareEnums.TENCENT_WEIXIN,AmayaShareConstants.AMAYA_TYPE_SHARE,"微信版本过低或未安装微信客户端");
            }
			return ;
		}

		new Thread(){
			public void run() {
				try {
					WXWebpageObject webpage = new WXWebpageObject();
					WXMediaMessage msg = new WXMediaMessage();
					Bitmap bmp = null;
					
//					测试数据
//					shareInfo.imageUrl = "http://ww3.sinaimg.cn/bmiddle/64f9539ejw1ef56zm81pvj20qo0is75s.jpg";
					if(!TextUtils.isEmpty(imagePath)
							&&new File(imagePath).exists()){
						bmp = BitmapFactory.decodeFile(imagePath);
					}else if(!TextUtils.isEmpty(imageUrl)){
                        String url = imageUrl;
						if(!url.startsWith("http://")){
							url = "http://" + imageUrl;
						}
						bmp = BitmapFactory.decodeStream(new URL(url).openStream());
					}
					
					webpage.webpageUrl = webpageUrl;
//					测试数据
//					webpage.webpageUrl = "http://ww3.sinaimg.cn/bmiddle/64f9539ejw1ef56zm81pvj20qo0is75s.jpg";
					
					msg.mediaObject = webpage;
					msg.title = title; //分享标题
					msg.description = description; //分享内容
					
//					测试数据
//					msg.title = "微信测试title";
//					msg.description = "微信分享测试";
					
					SendMessageToWX.Req req = new SendMessageToWX.Req();
					if(bmp==null){
						bmp = defaultBitmap;
					}
					if(bmp!=null){
						bmp = compressImage(bmp);
						Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
						bmp.recycle();
						msg.thumbData = bmpToByteArray(thumbBmp, true);//设置缩略图
						req.transaction = buildTransaction("img");
					}else{
//						L.v(TAG, "shareMessage","bmp is null");
					}
					req.message = msg;
                    req.scene =toCircle?SendMessageToWX.Req.WXSceneTimeline: SendMessageToWX.Req.WXSceneSession;
					wxApi.sendReq(req);
				} catch (Exception e) {
					e.printStackTrace();
					if(amayaShareListener!=null){
                        amayaShareListener.onException(AmayaShareEnums.TENCENT_WEIXIN, AmayaShareConstants.AMAYA_TYPE_SHARE, e.getMessage());
					}
				}
			};
			
		}.start();
	}
	
	public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		bmp.compress(CompressFormat.PNG, 100, output);
		if (needRecycle) {
			bmp.recycle();
		}
		
		byte[] result = output.toByteArray();
		try {
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	private Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 50, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while ( baos.toByteArray().length / 1024>25) {//循环判断如果压缩后图片是否大于30kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            options -= 10;//每次都减少10
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
        }  
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;  
    } 
	
	private String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
	}

}
