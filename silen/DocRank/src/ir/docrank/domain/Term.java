package ir.docrank.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Term {

	private String term;    //词项
	private int df;         //df值
	private long termhash;  //哈希值
	private HashMap<String, Document> posting_list;  //倒排记录表
	private List<String> docsID;  //倒排记录表中的文档集合，用于倒排记录表合并
	
	public Term() {
		this.posting_list = new HashMap<String, Document>();
		this.docsID = new ArrayList<String>();
	}
	
	public String getTerm() {
		return term;
	}
	public void setTerm(String term) {
		this.term = term;
	}
	public long getTermhash() {
		return termhash;
	}

	public void setTermhash(long termhash) {
		this.termhash = termhash;
	}

	public int getDf() {
		return df;
	}
	public void setDf(int df) {
		this.df = df;
	}
	public void addDf(int df) {
		this.df += df;
	}
	public HashMap<String, Document> getPosting_list() {
		return posting_list;
	}

	public void setPosting_list(HashMap<String, Document> posting_list) {
		this.posting_list = posting_list;
	}

	public void setPosting_list(String content) {
		String[] docs = content.split("\\|");
		
		for(String doc:docs) {
			int tf, flag;
			String docID, locations;
			StringBuilder sb = new StringBuilder();
			String[] infos = doc.split(" ");
			flag = Integer.parseInt(infos[0]);
			
			sb.append(infos[0]);
			sb.append("+");
			sb.append(infos[1]);
			docID = sb.toString(); //docID = flag+id
			
			tf = Integer.parseInt(infos[2]);
			locations = infos[3];
			
			//倒排记录表中已包含该文档
			if(this.docsID.contains(docID)) {
				
				this.posting_list.get(docID).addTf(tf);
				this.posting_list.get(docID).addLocations(locations);
				
			}else {//倒排记录表未包含该文档
				Document d = new Document();
				d.setFlag(flag);
				d.setDocID(docID);
				d.setTf(tf);
				d.addLocations(locations);
				this.posting_list.put(docID, d);
				this.docsID.add(docID);
			}				
		}
	}

	public List getDocsID() {
		return docsID;
	}

	public void setDocsID(List docsID) {
		this.docsID = docsID;
	}
	
	//获取词项在文档中的tf值
	public int getTfByDocID(String docID) {
		int tf = 0;
		if(this.docsID.contains(docID)) {
			tf = this.posting_list.get(docID).getTf();
		}
		
		return tf;
	}
}
