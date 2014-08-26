package com.fone.player.share.util;

import android.os.Bundle;

public interface AmayaShareListener {
	void onComplete(AmayaShareEnums enumKey,Bundle values);
	void onCancel(AmayaShareEnums enumKey);
	void onException(AmayaShareEnums enumKey,String msg);
}
