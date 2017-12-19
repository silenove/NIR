package ir.docrank.domain;

import java.sql.Timestamp;

public class TimeAndCatg {
	//时间戳信息
	private Timestamp ts = null;
	//类型信息
	private String catg = null;
	//来源
	private String source;
	
	public TimeAndCatg() {}

	public Timestamp getTs() {
		return ts;
	}

	public void setTs(Timestamp ts) {
		this.ts = ts;
	}

	public String getCatg() {
		return catg;
	}

	public void setCatg(String catg) {
		this.catg = catg;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
	
}
