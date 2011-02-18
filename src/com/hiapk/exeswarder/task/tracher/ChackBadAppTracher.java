package com.hiapk.exeswarder.task.tracher;

import com.hiapk.exeswarder.task.IResultReceiver;
import com.hiapk.exeswarder.task.OperateResult;

public class ChackBadAppTracher extends AInvokeTracker {
	private static final String TAG = "exeswarder.task.tracherChackBadAppTracher";

	public ChackBadAppTracher(IResultReceiver resultReceiver) {
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
