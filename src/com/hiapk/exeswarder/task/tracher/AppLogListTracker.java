package com.hiapk.exeswarder.task.tracher;

import com.hiapk.exeswarder.task.IResultReceiver;
import com.hiapk.exeswarder.task.OperateResult;

public class AppLogListTracker extends AInvokeTracker {
	public static final String TAG = "exeswarder.task.tracher.AppLogListTracker";

	public AppLogListTracker(IResultReceiver resultReceiver) {
		super(resultReceiver);
	}

	@Override
	public String TAG() {
		return TAG;
	}

	@Override
	public void handleResult(OperateResult result) {
		trackerResult = result.getResultData();
	}
}
