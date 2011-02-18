package com.hiapk.exeswarder.main;

import java.util.ArrayList;

import com.hiapk.exeswarder.bd.DBHelper;
import com.hiapk.exeswarder.been.AppDetail;
import com.hiapk.exeswarder.mark.ATaskMark;
import com.hiapk.exeswarder.mark.AppDetailListMark;
import com.hiapk.exeswarder.service.ActionException;
import com.hiapk.exeswarder.task.IResultReceiver;
import com.hiapk.exeswarder.task.ServiceWraper;
import com.hiapk.exeswarder.R;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class AppListActivity extends ListActivity implements IResultReceiver,
		View.OnClickListener {

	private AppListAdapter mAdapter;
	private BaseContext baseContext = BaseContext.getInstance();
	private ArrayList<AppDetail> appList = null;
	private ServiceWraper service = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_list);

		service = baseContext.getServiceWarper();

		appList = baseContext.getAppDetailList();
		mAdapter = new AppListAdapter();
		setListAdapter(mAdapter);

		setupListView();
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		refreshList();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void setupListView() {
		ATaskMark appDetailListMark = new AppDetailListMark();
		service.getAllAppDetailList(this, appDetailListMark, null);
	}

	public void onClick(View v) {
		// nothing
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		AppDetail appDetail = appList.get(position);
		showAppDetail(appDetail);
	}

	protected String getQuantityText(int count, int zeroResourceId,
			int pluralResourceId) {
		if (count == 0) {
			return getString(zeroResourceId);
		} else {
			String format = getResources().getQuantityText(pluralResourceId,
					count).toString();
			return String.format(format, count);
		}
	}

	private void refreshList() {
		ATaskMark appDetailListMark = new AppDetailListMark();
		service.getAllAppDetailList(this, appDetailListMark, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void receiveResult(ATaskMark taskMark, ActionException exception,
			Object trackerResult) {
		baseContext.setAppDetailList((ArrayList<AppDetail>) trackerResult);
		appList = baseContext.getAppDetailList();
		mAdapter.notifyDataSetChanged();
	}

	private void showAppDetail(final AppDetail appDetail) {
		LayoutInflater inflater = LayoutInflater.from(this);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		AlertDialog alert;

		View layout = inflater.inflate(R.layout.app_details,
				(ViewGroup) findViewById(R.id.detailLayout));

		TextView packageNameView = (TextView) layout
				.findViewById(R.id.packageName); // 包名
		TextView requestView = (TextView) layout
				.findViewById(R.id.requestDetail); // 类型
		TextView commandView = (TextView) layout.findViewById(R.id.command); // 请求
		TextView statusView = (TextView) layout.findViewById(R.id.status); // 状态

		final int appUid = appDetail.getUid();

		String appName = appDetail.getName();

		packageNameView.setText(appDetail.getPackageName());
		requestView.setText(appDetail.getExesType());
		commandView.setText(appDetail.getLastCalledNum());

		if (DBHelper.AllowType.ALLOW == appDetail.getAllow()) {
			statusView.setText("允许");
		} else if ((DBHelper.AllowType.SINGLE_ALLOW == appDetail.getAllow())) {
			statusView.setText("允许");
		} else if ((DBHelper.AllowType.ASK == appDetail.getAllow())) {
			statusView.setText("询问");
		} else if ((DBHelper.AllowType.DENY == appDetail.getAllow())) {
			statusView.setText("禁止");
		} else if ((DBHelper.AllowType.TIMEOUT == appDetail.getAllow())) {
			statusView.setText("超时");
		}

		// 标头的icon (title)
		View customTitle = inflater.inflate(R.layout.app_details_title,
				(ViewGroup) findViewById(R.id.customTitle));
		ImageView titleIcon = (ImageView) customTitle
				.findViewById(R.id.appIcon);
		try {
			Drawable drawable = getApplication().getPackageManager()
					.getApplicationIcon(appDetail.getPackageName());
			titleIcon.setImageDrawable(drawable);
		} catch (NameNotFoundException e) {
			titleIcon.setImageResource(R.drawable.sym_def_app_icon);
		}

		TextView titleName = (TextView) customTitle.findViewById(R.id.appName);
		titleName.setText(appName);
		TextView titleUid = (TextView) customTitle.findViewById(R.id.appUid);
		titleUid.setText(Integer.toString(appUid));

		builder
				.setCustomTitle(customTitle)
				.setView(layout)
				.setPositiveButton(
						appDetail.getAllow() == DBHelper.AllowType.ALLOW ? R.string.deny
								: R.string.allow,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// mDB.changeAppLogState(appLog.getId(),
								// AppDetail.ALLOW);
								if (appDetail.getAllow() == DBHelper.AllowType.ALLOW) {
									appDetail.setAllow(DBHelper.AllowType.DENY);
									service.updateAppDetail(appDetail);
								} else {
									appDetail
											.setAllow(DBHelper.AllowType.ALLOW);
									service.updateAppDetail(appDetail);
								}

								refreshList();
							}
						}).setNeutralButton(getString(R.string.forget),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								service.delAppDetail(appDetail);
								refreshList();
								dialog.cancel();
							}
						}).setNegativeButton(getString(R.string.cancel), null);
		alert = builder.create();
		alert.show();
	}

	private final class AppListAdapter extends BaseAdapter implements
			ListAdapter {

		@Override
		public int getCount() {
			return appList.size();
		}

		@Override
		public Object getItem(int position) {
			return appList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			AppDetail appDetail = appList.get(position);
			LayoutInflater mInflater = getLayoutInflater(); // 得到一个布局控制器

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.app_list_item, null);
			}

			ImageView appIconView = (ImageView) convertView
					.findViewById(R.id.appIcon);

			String packageName = appDetail.getPackageName();
			try {
				Drawable drawable = getApplication().getPackageManager()
						.getApplicationIcon(packageName);
				appIconView.setImageDrawable(drawable);
			} catch (NameNotFoundException e) {
				appIconView.setImageResource(R.drawable.sym_def_app_icon);
			}

			TextView appNameView = (TextView) convertView
					.findViewById(R.id.appName);
			appNameView.setText(appDetail.getName());

			TextView phoneNumView = (TextView) convertView
					.findViewById(R.id.request);
			phoneNumView.setText(appDetail.getLastCalledNum());

			ImageView allowView = (ImageView) convertView
					.findViewById(R.id.itemPermission);
			if (DBHelper.AllowType.ALLOW == appDetail.getAllow()) {
				allowView.setImageResource(R.drawable.perm_allow_dot);
			} else {
				allowView.setImageResource(R.drawable.perm_deny_dot);
			}

			return convertView;
		}
	}

}