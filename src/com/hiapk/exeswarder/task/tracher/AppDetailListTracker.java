package com.hiapk.exeswarder.task.tracher;

import com.hiapk.exeswarder.task.IResultReceiver;
import com.hiapk.exeswarder.task.OperateResult;

public class AppDetailListTracker extends AInvokeTracker {
	public static final String TAG = "exeswarder.task.tracher.AppDetailListTracker";

	public AppDetailListTracker(IResultReceiver resultReceiver) {
		super(resultReceiver);
	}

	@Override
	public String TAG() {
		return TAG;
	}

	@Override
	public void handleResult(OperateResult result) {
		trackerResult= result.getResultData();
	}

}
