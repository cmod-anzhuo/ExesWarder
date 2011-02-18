package com.hiapk.exeswarder.mark;

import com.hiapk.exeswarder.been.PageInfo;

/**
 * 2010-6-19 <br>
 * 分页型的任务分装
 * 
 */
public abstract class APageTaskMark extends ATaskMark {

	// 页面信息, 初始从0开始
	private PageInfo pageInfo = new PageInfo();

	/**
	 * @return the pageInfo
	 */
	public PageInfo getPageInfo() {
		return pageInfo;
	}

	/**
	 * @param pageInfo
	 *            the pageInfo to set
	 */
	public void setPageInfo(PageInfo pageInfo) {
		this.pageInfo = pageInfo;
	}

	/**
	 * 设置每页的数量
	 */
	public void setPageSize(int count) {
		pageInfo.setPageSize(count);
	}

	/**
	 * @return the loadEnd
	 */
	public boolean isLoadEnd() {
		return pageInfo == null
				|| (pageInfo.getPageNum() != 0 && pageInfo.getPageNum() == pageInfo.getPageIndex())
				|| (pageInfo.getPageNum() == 0 && pageInfo.getPageIndex() != 0);
	}

	/**
	 * 是否是第一加载
	 */
	public boolean isFirstLoaded() {
		if (pageInfo == null) {
			return false;

		} else {
			return (pageInfo.getPageIndex() == 1);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "APageTaskMark [pageInfo=" + pageInfo + ", isLoadEnd()=" + isLoadEnd()
				+ ", toString()=" + super.toString() + "]";
	}

}
