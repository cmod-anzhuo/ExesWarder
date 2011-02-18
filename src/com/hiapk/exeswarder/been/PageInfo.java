package com.hiapk.exeswarder.been;

/**
 * 2010-12-23<br>
 * 页面统计信息
 * 
 */
public class PageInfo {

	// 每页的大小(默认)
	private int pageSize = 10;
	// 当前已经加载到第几页
	private int pageIndex;
	// 总的记录数
	private int recordNum;
	// 总共几页
	private int pageNum;

	/**
	 * @return the recordNum
	 */
	public int getRecordNum() {
		return recordNum;
	}

	/**
	 * @param recordNum
	 *            the recordNum to set
	 */
	public void setRecordNum(int recordNum) {
		this.recordNum = recordNum;
	}

	/**
	 * @return the pageSize
	 */
	public int getPageSize() {
		return pageSize;
	}

	/**
	 * @param pageSize
	 *            the pageSize to set
	 */
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	/**
	 * @return the pageIndex
	 */
	public int getPageIndex() {
		return pageIndex;
	}

	/**
	 * 获得下一页的所有<br>
	 * 请求列表的时候必须调用这个方法
	 * 
	 * @return
	 */
	public int getNextPageIndex() {
		return pageIndex + 1;
	}

	/**
	 * @param pageIndex
	 *            the pageIndex to set
	 */
	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}

	/**
	 * @return the pageNum
	 */
	public int getPageNum() {
		return pageNum;
	}

	/**
	 * @param pageNum
	 *            the pageNum to set
	 */
	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PageInfo [pageIndex=" + pageIndex + ", pageNum=" + pageNum
				+ ", pageSize=" + pageSize + ", recordNum=" + recordNum + "]";
	}

}
