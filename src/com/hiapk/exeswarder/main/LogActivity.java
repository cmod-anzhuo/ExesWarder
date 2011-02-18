package com.hiapk.exeswarder.main;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.hiapk.exeswarder.bd.DBHelper;
import com.hiapk.exeswarder.been.AppLog;
import com.hiapk.exeswarder.mark.ATaskMark;
import com.hiapk.exeswarder.mark.AppLogListMark;
import com.hiapk.exeswarder.service.ActionException;
import com.hiapk.exeswarder.task.ServiceWraper;
import com.hiapk.exeswarder.task.IResultReceiver;
import com.hiapk.exeswarder.been.AppDetail;
import com.hiapk.exeswarder.R;

public class LogActivity extends ListActivity implements IResultReceiver {
	@SuppressWarnings("unused")
	private static final String TAG = "Su.LogActivity";

	private static final int MENU_CLEAR_LOG = 1;

	private AppLogAdapter mAdapter;
	// private LogAdapter mAdapter;

	private List<AppLog> logList;
	private ServiceWraper service = null;
	private BaseContext baseContext = BaseContext.getInstance();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.log_list);

		logList = baseContext.getAppLogList();
		mAdapter = new AppLogAdapter(this);
		setListAdapter(mAdapter);

		setupListView();
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_CLEAR_LOG, Menu.NONE, R.string.pref_clear_log)
				.setIcon(R.drawable.ic_menu_clear);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == MENU_CLEAR_LOG) {
			service.clearLog();
			refreshList();
			return true;
		}
		return false;
	}

	private void setupListView() {
		service = baseContext.getServiceWarper();
		ATaskMark appLogListMark = new AppLogListMark();
		service.getTopAppLogList(this, appLogListMark, null);
	}

	private void refreshList() {
		ATaskMark appLogListMark = new AppLogListMark();
		service.getTopAppLogList(this, appLogListMark, null);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		AppLog appLog = logList.get(position);
		appLog(appLog);
	}

	private void appLog(final AppLog appLog) {
		LayoutInflater inflater = LayoutInflater.from(this);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		AlertDialog alert;

		View layout = inflater.inflate(R.layout.log_dialog, null);

		TextView dateView = (TextView) layout.findViewById(R.id.date);
		TextView phoneNumView = (TextView) layout.findViewById(R.id.phoneNum);
		TextView statusView = (TextView) layout.findViewById(R.id.status);
		TextView contentView = (TextView) layout.findViewById(R.id.content);
		TextView nameView = (TextView) layout.findViewById(R.id.app_name);
		TextView packageNameView = (TextView) layout
				.findViewById(R.id.package_name);

		AppDetail appDetail = new AppDetail();
		appDetail.setId(appLog.getAppId());
		appDetail = service.getAppDetail(appDetail);
		if (appDetail != null) {
			nameView.setText(appDetail.getName());
			packageNameView.setText(appDetail.getPackageName());
		}
		final AppDetail appDetailTmp = appDetail;

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
		titleName.setText(appDetail.getName());
		TextView titleUid = (TextView) customTitle.findViewById(R.id.appUid);
		titleUid.setText(Integer.toString(appDetail.getUid()));

		Date dd = new Date(appLog.getDate());
		SimpleDateFormat myFmt = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");
		String str = "";
		try {
			str = myFmt.format(dd);
			dateView.setText(str);
		} catch (Exception e) {
			dateView.setText("无法获得日期"); // 这个要改到xml
		}

		if (DBHelper.AllowType.ALLOW == appLog.getAllow()) {
			statusView.setText("已发送");
		} else if ((DBHelper.AllowType.SINGLE_ALLOW == appLog.getAllow())) {
			statusView.setText("已发送");
		} else if ((DBHelper.AllowType.ASK == appLog.getAllow())) {
			statusView.setText("询问");
		} else if ((DBHelper.AllowType.DENY == appLog.getAllow())) {
			statusView.setText("禁止");
		} else if ((DBHelper.AllowType.TIMEOUT == appLog.getAllow())) {
			statusView.setText("超时");
		}

		phoneNumView.setText(appLog.getPhoneNum());
		contentView.setText(appLog.getContent());

		builder.setCustomTitle(customTitle).setView(layout).setPositiveButton(
				"发送", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// 发送短信
						service.sendSMS(appDetailTmp, appLog);
						refreshList();
					}
				}).setNegativeButton(getString(R.string.cancel), null);
		alert = builder.create();
		alert.show();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void receiveResult(ATaskMark taskMark, ActionException exception,
			Object trackerResult) {
		baseContext.setAppLogList((ArrayList<AppLog>) trackerResult);
		logList = baseContext.getAppLogList();
		mAdapter.notifyDataSetChanged();
	}

	private final class AppLogAdapter extends BaseAdapter implements
			ListAdapter {
		@SuppressWarnings("unused")
		private Context context;

		AppLogAdapter(Context context) {
			this.context = context;
		}

		@Override
		public int getCount() {
			return logList.size();
		}

		@Override
		public Object getItem(int position) {
			return logList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			AppLog appLog = logList.get(position);
			LayoutInflater mInflater = getLayoutInflater(); // 得到一个布局控制器

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.log_list_item, null);
			}

			TextView dateView = (TextView) convertView.findViewById(R.id.date);
			Date dd = new Date(appLog.getDate());
			SimpleDateFormat myFmt = new SimpleDateFormat("MM月dd日 HH时mm分");
			String str = "";
			try {
				str = myFmt.format(dd);
				dateView.setText(str);
			} catch (Exception e) {
				dateView.setText("无法获得日期"); // 这个要改到xml
			}

			TextView phoneNumView = (TextView) convertView
					.findViewById(R.id.phone_num);
			phoneNumView.setText(appLog.getPhoneNum());

			TextView allowView = (TextView) convertView
					.findViewById(R.id.allow_type);

			if (DBHelper.AllowType.ALLOW == appLog.getAllow()) {
				allowView.setText("允许");
			} else if ((DBHelper.AllowType.SINGLE_ALLOW == appLog.getAllow())) {
				allowView.setText("允许");
			} else if ((DBHelper.AllowType.ASK == appLog.getAllow())) {
				allowView.setText("询问");
			} else if ((DBHelper.AllowType.DENY == appLog.getAllow())) {
				allowView.setText("禁止");
			} else if ((DBHelper.AllowType.TIMEOUT == appLog.getAllow())) {
				allowView.setText("超时");
			}

			TextView contentView = (TextView) convertView
					.findViewById(R.id.content);
			contentView.setText("内容:" + appLog.getContent());

			return convertView;
		}

	}

}
