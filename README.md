第三方继承分享组件；通过对新浪微博，腾讯微博，QQ好友，QQ空间，微信好友，微信朋友圈6种授权+分享进行一系列封装达到对外简洁调用的目的

重点：各种分享所需要的key和redirect_url等配置信息都存储在AmayaShareConstants.java类中；

在Activity中分享步骤仅需3三步，代码简洁易懂：<br />
Step.1    在Activity中调用下面一行代码:<br />
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {<br />
        &#9;super.onActivityResult(requestCode, resultCode, data);<br />&#9;if(requestCode != 0 && data != null)<br />
        &#9;&#9;AmayaShareUtils.instance().onActivityResult(this, requestCode, resultCode, data);<br />
  }

Step.2  检验某个三方分享是否已授权(6种枚举类型:SINA_WEIBO,TENCENT_WEIBO,TENCENT_QQ,TENCENT_QZONE,TENCENT_WEIXIN),其中TENCENT_WEIXIN不需要检验(但是必须要在微信开发平台申请到key)即可直接分享.

    AmayaShareUtils.instance().isAuthed(AmayaShareEnums.TENCENT_QZONE,this);   //第二个参数为回调接口AmayaShareListener


    AmayaShareListener.java通用回调接口集成了授权和分享以及6种分享组件的不同回调.
        /**
     *
     * @param enumKey       (@link AmayaShareEnums)
     * @param authOrShare   true:AUTH授权；false:SHARE分享
     * @param values        返回数据 需要判空
     */
	void onComplete(AmayaShareEnums enumKey,boolean authOrShare,Bundle values);



Step.3  授权成功后进入此流程.在分享按钮点击事件中调用相应的分享代码,所有三方组件的分享都调用类似shareToXX(...)方法即可实现分享.
  授权成功后即可调用AmayaShareUtils类中的相应方法，传入需要分享的数据，整个分享流程即可完成.<br />
  &#9;1.shareToTXWeiBo();<br />
  &#9;2.shareToWeixin();<br />
  &#9;3.shareToQQ();<br />
  &#9;4.shareToSina();<br />
  &#9;5.shareToQZone();<br />
Step.4  如果未授权成功，则调用AmayaShareUtils类中的auth(AmayaShareEnums enums,Activity activity,AmayaShareListener listener)通用授权方法，在AmayaShareListener接口回调中的回调方法中可继续Step.3步骤<br />

最后:程序退出时注意调用一次AmayaShareUtils类中的onDestroy();方法，
因为此时该类通过AmayaShareListener接口持有了Activity的句柄,且该类通过单例模式持有static的自身的句柄，导致该activity不能及时被系统回收，故请留意一下。

  ------------------------------------------------END------------------------------------------------------

