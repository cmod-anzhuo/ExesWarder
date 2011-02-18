package com.hiapk.exeswarder.service;

import java.util.ArrayList;

import com.hiapk.exeswarder.been.AppDetail;
import com.hiapk.exeswarder.been.AppLog;
import com.hiapk.exeswarder.been.MyPackageInfo;

/**
 * 程序所有服务业务的接口
 * 
 * @author LinLin
 */
public interface IBaseService {

	/**
	 * 清除日志
	 */
	public void clearLog();

	/**
	 * 获取全部的“被防火软件”列表
	 * 
	 * @param appContext
	 * @return ArrayList<AppDetail>
	 */
	public ArrayList<AppDetail> getAllAppDetailList();

	/**
	 * 获取全部的“被防火软件发送日志”列表
	 * 
	 * @param appContext
	 * @return ArrayList<AppLog>
	 */
	public ArrayList<AppLog> getTopAppLogList(int topNum);

	/**
	 * 得到一个AppDetail项from id
	 * 
	 * @param id
	 * @return AppDetail
	 */
	public AppDetail getAppDetailForDB(AppDetail appDetail);

	/**
	 * 得到一个AppDetail项from AppDetail
	 * 
	 * @param appDetail
	 * @return AppDetail
	 */
	public AppDetail getAppDetailForCache(AppDetail appDetail);

	/**
	 * 得到一个AppLog项from id
	 * 
	 * @param id
	 * @return AppLog
	 */
	public AppLog getAppLogForDB(AppLog appLog);

	/**
	 * 得到一个AppLog项from AppLog
	 * 
	 * @param appLog
	 * @return AppLog
	 */
	public AppLog getAppLogForCache(AppLog appLog);

	/**
	 * 添加一条AppDetail，并把添加的这条返回来<br>
	 * 里面多了主键,即:_id
	 * 
	 * @param appDetail
	 * @return AppDetail
	 */
	public AppDetail saveAppDetail(AppDetail appDetail);

	/**
	 * 添加一条AppLog，并把添加的这条返回来<br>
	 * 里面多了主键,即:_id
	 * 
	 * @param appLog
	 * @return AppLog
	 */
	public AppLog saveAppLog(AppLog appLog);

	/**
	 * 更新一 条AppDetail，并把添加的这条返回来
	 * 
	 * @param appDetail
	 * @return updateNum
	 */
	public int updateAppDetail(AppDetail appDetail);

	/**
	 * 更新一 条AppLog，并把添加的这条返回来
	 * 
	 * @param appLog
	 * @return updateNum
	 */
	public int updateAppLog(AppLog appLog);

	/**
	 * 更新一 条AppDetail，并把添加的这条返回来
	 * 
	 * @param appDetail
	 * @return delNum
	 */
	public void delAppDetail(AppDetail appDetail);

	/**
	 * 检测不良的软件
	 */
	public ArrayList<MyPackageInfo> checkBadAppList();

}
