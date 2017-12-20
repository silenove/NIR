package ir.docrank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.tokenizer.NLPTokenizer;

import ir.docrank.domain.Document;
import ir.docrank.domain.Term;
import ir.docrank.domain.TimeAndCatg;
import net.sf.json.JSONObject;

public class DocRank {

	//停用词
	private Collection<String> stopWord = new ArrayList<String>();
	//文档总数
	private int docsSize = 0;
	
	//网页每页现实的条目数
	private int pageNumber = 10;
	
	public DocRank() {
		//读入停用表
		try {
			File file = new File("src/stopWord.txt");
			InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
			BufferedReader br = new BufferedReader(reader);
			String line = "";
			while((line=br.readLine()) != null) {
				this.stopWord.add(line);
			}
		}catch(IOException e) {
			e.printStackTrace();
		}
		
	}
	
		
	public int getPageNumber() {
		return pageNumber;
	}

	//设置每页条目数
	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}



	public String rankDocs(String input) throws SQLException {
		//解析json
		Map<String, Object> map = this.parseJson2Map(input);
		
		//查询单词
		String query = map.get("query").toString();
		//类型
		String catg = map.get("category").toString();
		//来源
		String source = map.get("source").toString();
		//页数
		int page = Integer.parseInt(map.get("page").toString());
		//起止时间
		String from = map.get("from").toString();
		String to = map.get("to").toString();
		//排序类型 0-相关度排序 1-时间排序
		int sortType = Integer.parseInt(map.get("sort").toString());
		
		//字符串到词项的映射
		HashMap<String, Term> string2Term = new HashMap<String, Term>();
		
		//分词
		List<String> querys = this.wordSegment(query);
		//相关文档总数
		int relatedDocCount = 0;
		//查询Wt_q
		HashMap<String, Double> Wt_q = null;
		//文档分数
		Map<String, Double> scores = new HashMap<String, Double>();
		//文档排名
		List<String> rank = null;
		//结果
		List<String> result = new ArrayList<String>();
			
		
		//构建本次查询的倒排索引
		string2Term = this.createPostingList(querys);
		
		//相关文档
		List<String> relatedDocs = this.intersectPostingList(string2Term);
		
		//过滤文档
		relatedDocs = this.filterRelatedDocsByTSC(relatedDocs, source, catg, from, to);
		
		//获取文档数量
		this.docsSize = this.getDocSize();
		
		//计算Wt_q
		Wt_q = this.calculateWt_q(querys, string2Term);
		
		//文档排序
		if(sortType == 0) {
			Map<String, Object> pack = this.rankDocsByTfIdf(Wt_q, relatedDocs, string2Term);
			scores = (Map<String, Double>) pack.get("scores");
			rank = (List<String>) pack.get("rank");
		}else {
			Map<String, Object> pack = this.rankDocsByTime(relatedDocs);
			scores = (Map<String, Double>) pack.get("scores");
			rank = (List<String>) pack.get("rank");
		}
		
		//相关文档总数
		relatedDocCount = rank.size();
		
		//选取制定page的文档
		result = this.filterPage(rank, page);
		
		//结果
		List<Map<String, String>> docList = new ArrayList<Map<String, String>>();;
		for(String doc:result) {
			Map<String, String> docInfo = new HashMap<String, String>();
			String[] info = doc.split("\\+");
			docInfo.put("id", info[1]);
			docInfo.put("kind", info[0]);
			docInfo.put("relationship", scores.get(doc).toString());
			docList.add(docInfo);
		}
		
		//构建输出json
		Map<String, Object> output = new HashMap<String, Object>();
		//放入文档总数
		output.put("resultCount", relatedDocCount);
		//放入关键词
		output.put("keywords", querys);
		//放入排序文档
		output.put("docList", docList);
		
		JSONObject jsonOut = JSONObject.fromObject(output);
		
		return jsonOut.toString();
	}
	
	//分词
	public List<String> wordSegment(String query){
		
		//过滤空格和Tab
		if(query != "") {
			Pattern pattern = Pattern.compile("\\s*|\t|\r|\n");
			Matcher matcher = pattern.matcher(query);
			query = matcher.replaceAll("");
		}
		
		//分词
		List<String> querys = new ArrayList<String>();
		List<com.hankcs.hanlp.seg.common.Term> query_seg = HanLP.segment(query);
		for(com.hankcs.hanlp.seg.common.Term t: query_seg) {
			if(!this.isStopWord(t.word)) {
				querys.add(t.word);
			}		
		}
		return querys;
	}
	
	//解析json
	public Map<String, Object> parseJson2Map(String jsonStr){
		Map<String, Object> map = new HashMap<String, Object>();
		JSONObject json = JSONObject.fromObject(jsonStr);
		for(Object key:json.keySet()) {
			Object value = json.get(key);
			map.put(key.toString(), value);
		}
		return map;
	}
	
	//根据查询构建对应词项倒排索引表
	private HashMap<String, Term> createPostingList(List<String> querys) throws SQLException{
		HashMap<String, Term> s2Term = new HashMap<String, Term>();
		
		//连接数据库
		Connection con = DBUtil.getConnection();
		DBOperator dbop = new DBOperator(con);
		
		//查询倒排记录表
		for(String query: querys) {
			if(!isStopWord(query)) {
				if(!s2Term.containsKey(query)) {
					
					Term term = dbop.getPostingListByTerm(query);
					if(term != null) {
						s2Term.put(query, term);	
					}else {
						System.out.println(query + " can't be found in DB!");
					}				
				}
			}
		}
		
		return s2Term;
	}
	
	//判断是否为停用词
	private boolean isStopWord(String term) {
		return this.stopWord.contains(term);
	}
	
	//求2个倒排索引的交集
	public static List<String> intersectDocs(List<String> docs1, List<String> docs2) {
		List<String> intersection = new ArrayList<String>();
		for(String id: docs1) {
			if(docs2.contains(id)) {
				intersection.add(id);
			}
		}
		return intersection;
	}
	
	//求倒排记录表的交集
	public static List<String> intersectPostingList(HashMap<String, Term> s2Term){
		List<String> intersection = new ArrayList<String>();
		Iterator iter = s2Term.entrySet().iterator();
		Map.Entry entry = null;
		
		if(iter.hasNext()) {
			entry = (Map.Entry)iter.next();
			Term t = (Term)entry.getValue();
			intersection = ((Term)entry.getValue()).getDocsID();
		}
		while(iter.hasNext()) {
			entry = (Map.Entry)iter.next();
			intersection = intersectDocs(intersection, ((Term)entry.getValue()).getDocsID());
		}
		
		return intersection;
	}
	
	//文档筛选
	private List<String> filterRelatedDocsByTSC(List<String> relatedDocs, String source, String catg, String from, String to) throws SQLException{
		List<String> filtered = new ArrayList<String>();
		
		//连接数据库
		Connection con = DBUtil.getConnection();
		DBOperator dbop = new DBOperator(con);
		
		TimeAndCatg tc = null;
		
		for(String docID:relatedDocs) {
			boolean flag = true;
			tc = dbop.getTimeAndCatgByDocID(docID);
			
			//判断来源
			if(!source.equals("all")) {
				if(!source.equals(tc.getSource())){
					flag = false;
				}
			}
			
			//判断类型
			if(!catg.equals("all")) {
				if(!catg.equals(tc.getCatg())) {
					flag = false;
				}
			}
			
			//判断时间
			if(!from.equals("all") && !to.equals("all")) {
				Timestamp fromTs = Timestamp.valueOf(from);
				Timestamp toTs = Timestamp.valueOf(to);
				if(!(tc.getTs().getTime() >= fromTs.getTime() && tc.getTs().getTime() <= toTs.getTime())) {
					flag = false;
				}
			}else if(!from.equals("all") && to.equals("all")) {
				Timestamp fromTs = Timestamp.valueOf(from);
				if(tc.getTs().getTime() <= fromTs.getTime()) {
					flag = false;
				}
			}else if(from.equals("all") && !to.equals("all")) {
				Timestamp toTs = Timestamp.valueOf(to);
				if(tc.getTs().getTime() >= toTs.getTime()) {
					flag = false;
				}
			}
			
			if(flag) {
				filtered.add(docID);
			}
		}
		
		return filtered;
	}
	
	//计算查询的归一化tf-idf值
	private HashMap<String, Double> calculateWt_q(List<String> querys, HashMap<String, Term> s2Term) {
		HashMap<String, Integer> tfs = new HashMap<String, Integer>();
		HashMap<String, Double> Wt_q = new HashMap<String, Double>();
		double length = 0;
		
		for(String term:querys) {
			if(tfs.containsKey(term)) {
				tfs.put(term, tfs.get(term)+1);		
			}else {
				tfs.put(term, 1);
			}
		}
		
		//计算Wt_q.
		for(String term:tfs.keySet()) {
			int df = s2Term.get(term).getDf();
			double idf = Math.log10(this.docsSize/df);
			Wt_q.put(term, this.calculateWF(tfs.get(term))*idf);
			length += Math.pow(Wt_q.get(term), 2);
		}
		
		length = Math.sqrt(length);
		
		//归一化
		if(length != 0) {
			for(String term:Wt_q.keySet()) {
				Wt_q.put(term, Wt_q.get(term)/length);
			}
		}
		
		return Wt_q;

	}
	
	//获取文档总数
	public int getDocSize() throws SQLException {
		//连接数据库
		Connection con = DBUtil.getConnection();
		DBOperator dbop = new DBOperator(con);
		return dbop.getDocSize();
	}
	
	//计算查询文档间的相似度
	private double calculateSimilarity(HashMap<String, Double> Wt_q, String docID, HashMap<String, Term> string2Term) {
		HashMap<String, Double> Wt_d = new HashMap<String, Double>();
		double length = 0;
		double score = 0;
		
		//计算Wt_d
		for(String s:Wt_q.keySet()) {
			int tf = string2Term.get(s).getTfByDocID(docID);
			int df = 1;
			double idf = 1.0;
			Wt_d.put(s, this.calculateWF(tf)*idf);
			length += Math.pow(Wt_d.get(s), 2);
		}
		
		length = Math.sqrt(length);
		
		//归一化
		if(length != 0) {
			for(String term:Wt_d.keySet()) {
				Wt_d.put(term, Wt_d.get(term)/length);
			}
		}
		
		//计算余弦相似度
		for(String s:Wt_q.keySet()) {
			score += Wt_q.get(s)*Wt_d.get(s);
		}
		
		return score;
	}
	
	//对文档进行排序,返回排序文档和相关度
	public Map<String, Object> rankDocsByTfIdf(HashMap<String, Double> Wt_q, List<String> relatedDocs, HashMap<String, Term> string2Term){
		Map<String, Double> scoreDocs = new HashMap<String, Double>();
		List<String> rank = new ArrayList<String>();
		for(String docID: relatedDocs) {
			double score = this.calculateSimilarity(Wt_q, docID, string2Term);
			scoreDocs.put(docID, score);
		}
		
		//排序
		List<Map.Entry<String, Double>> folds = new ArrayList<Map.Entry<String, Double>>(scoreDocs.entrySet());
		Collections.sort(folds, new Comparator<Map.Entry<String, Double>>(){

			@Override
			public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
				// TODO Auto-generated method stub
				return -(o1.getValue().compareTo(o2.getValue()));
			}
			
		});
		
		for(int i=0; i < folds.size(); i++) {
			String[] ss = folds.get(i).toString().split("=");
		    rank.add(ss[0]);
		}
		
		//封装
		Map<String, Object> pack = new HashMap<String, Object>();
		pack.put("scores", scoreDocs);
		pack.put("rank", rank);
		
		return pack;
	}
	
	//按时间对文档排序
	public Map<String, Object> rankDocsByTime(List<String> relatedDocs) throws SQLException{
		Map<String, Long> time = new HashMap<String, Long>();
		List<String> rank = new ArrayList<String>();
		Map<String, Double> scoreDocs = new HashMap<String, Double>();//都为0
		
		//连接数据库
		Connection con = DBUtil.getConnection();
		DBOperator dbop = new DBOperator(con);
				
		for(String docID:relatedDocs) {
			TimeAndCatg tc = dbop.getTimeAndCatgByDocID(docID);
			Timestamp ts = tc.getTs();
			time.put(docID, ts.getTime());
		}
		
		//排序
		List<Map.Entry<String, Long>> folds = new ArrayList<Map.Entry<String, Long>>(time.entrySet());
		Collections.sort(folds, new Comparator<Map.Entry<String, Long>>(){

			@Override
			public int compare(Entry<String, Long> o1, Entry<String, Long> o2) {
				// TODO Auto-generated method stub
				return -(o1.getValue().compareTo(o2.getValue()));
			}
			
		});
		
		for(int i=0; i < folds.size(); i++) {
			String[] ss = folds.get(i).toString().split("=");
		    rank.add(ss[0]);
		    scoreDocs.put(ss[0], new Double(0));
		}
		
		//封装
		Map<String, Object> pack = new HashMap<String, Object>();
		pack.put("scores", scoreDocs);
		pack.put("rank", rank);
				
		return pack;
	}
	
	//选取指定Page的文档
	public List<String> filterPage(List<String> rank, int page){
		List<String> filtered = new ArrayList<String>();
		for(int i=(page-1)*this.pageNumber;i < page*this.pageNumber;i++) {
			if(i < rank.size()) {
				filtered.add(rank.get(i));
			}
		}
		return filtered;
	}
	
	//tf的亚线性尺度变换
	private double calculateWF(int tf) {
		if(tf > 0) {
			return 1+Math.log10(tf);
		}else {
			return 0;
		}
	}

	public static void main(String[] args) {
		DocRank dr = new DocRank();
		try {
			String output = dr.rankDocs(args[0]);
			System.out.println(output);
		} catch (NullPointerException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
