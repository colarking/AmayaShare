package com.iyoudang.matrix.share.util;

import com.renn.rennsdk.RennParam;
import com.renn.rennsdk.RennRequest.Method;

import java.util.HashMap;
import java.util.Map;

/**
 * putFeed 接口的request参数
 */ 
public class AmayaRRBean extends RennParam {

	public AmayaRRBean() {
		super("/v2/feed/put", Method.POST);
	}

	/**
     * 用户输入的自定义内容。注意：最多200个字符
     */ 
    private String message;
    /**
     * 新鲜事标题 注意：最多30个字符
     */ 
    private String title;
    /**
     * 新鲜事动作模块链接
     */ 
    private String actionTargetUrl;
    /**
     * 新鲜事图片地址
     */ 
    private String imageUrl;
    /**
     * 新鲜事主体内容 注意：最多200个字符。
     */ 
    private String description;
    /**
     * 新鲜事副标题 注意：最多20个字符
     */ 
    private String subtitle;
    /**
     * 新鲜事动作模块文案。 注意：最多10个字符
     */ 
    private String actionName;
    /**
     * 新鲜事标题和图片指向的链接
     */ 
    private String targetUrl;

    /**
     * @return the message;
     */
    public String getMessage() {
        return message;
    }
    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }
    /**
     * @return the title;
     */
    public String getTitle() {
        return title;
    }
    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }
    /**
     * @return the actionTargetUrl;
     */
    public String getActionTargetUrl() {
        return actionTargetUrl;
    }
    /**
     * @param actionTargetUrl the actionTargetUrl to set
     */
    public void setActionTargetUrl(String actionTargetUrl) {
        this.actionTargetUrl = actionTargetUrl;
    }
    /**
     * @return the imageUrl;
     */
    public String getImageUrl() {
        return imageUrl;
    }
    /**
     * @param imageUrl the imageUrl to set
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    /**
     * @return the description;
     */
    public String getDescription() {
        return description;
    }
    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
    /**
     * @return the subtitle;
     */
    public String getSubtitle() {
        return subtitle;
    }
    /**
     * @param subtitle the subtitle to set
     */
    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }
    /**
     * @return the actionName;
     */
    public String getActionName() {
        return actionName;
    }
    /**
     * @param actionName the actionName to set
     */
    public void setActionName(String actionName) {
        this.actionName = actionName;
    }
    /**
     * @return the targetUrl;
     */
    public String getTargetUrl() {
        return targetUrl;
    }
    /**
     * @param targetUrl the targetUrl to set
     */
    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }
    
    public Map<String,String> toMap(){
        Map<String, String> textParams = new HashMap<String, String>();
        if (message != null) {
            textParams.put("message", message);
        }
        if (title != null) {
            textParams.put("title", title);
        }
        if (actionTargetUrl != null) {
            textParams.put("actionTargetUrl", actionTargetUrl);
        }
        if (imageUrl != null) {
            textParams.put("imageUrl", imageUrl);
        }
        if (description != null) {
            textParams.put("description", description);
        }
        if (subtitle != null) {
            textParams.put("subtitle", subtitle);
        }
        if (actionName != null) {
            textParams.put("actionName", actionName);
        }
        if (targetUrl != null) {
            textParams.put("targetUrl", targetUrl);
        }
        return textParams;
    }
}
