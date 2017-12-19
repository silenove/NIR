package ir.docrank.domain;

import java.util.ArrayList;

public class Document {
	
	private String docID;//docID = flag + id
	private int tf; //tf值
	private ArrayList<Integer> location_list; //位置信息
	private int flag; //0新闻，1评论
	
	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	public Document() {
		this.location_list = new ArrayList();
	}
	
	public String getDocID() {
		return docID;
	}
	public void setDocID(String docID) {
		this.docID = docID;
	}
	public int getTf() {
		return tf;
	}
	public void setTf(int tf) {
		this.tf = tf;
	}
	public void addTf(int tf) {
		this.tf += tf;
	}
	
	public ArrayList<Integer> getLocation_list() {
		return location_list;
	}

	public void setLocation_list(ArrayList<Integer> location_list) {
		this.location_list = location_list;
	}

	public void addLocation(int location) {
		this.location_list.add(location);
	}
	
	
	public void addLocations(String locations) {
		locations = locations.substring(1, locations.length()-1);
		String[] locs = locations.split(",");
		for(String loc:locs) {
			this.location_list.add(Integer.parseInt(loc));
		}		
	}
}
