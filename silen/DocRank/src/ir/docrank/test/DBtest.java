package ir.docrank.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.tokenizer.NLPTokenizer;

import ir.docrank.DBOperator;
import ir.docrank.DBUtil;
import ir.docrank.DocRank;
import ir.docrank.domain.Document;
import ir.docrank.domain.Term;
import ir.docrank.domain.TimeAndCatg;
import net.sf.json.JSONObject;

public class DBtest {
	
	@Test
	public void testRankDoc() throws SQLException {
		DocRank dr = new DocRank();
		Map<String, String> input = new HashMap<String, String>();
		
		input.put("query", "十九大");
		input.put("page", "1");
		input.put("category", "all");
		input.put("source", "all");
		input.put("from", "all");
		input.put("to", "all");
		input.put("sort", "0");
		
		JSONObject jsonInput = JSONObject.fromObject(input);
		String jsonOut = dr.rankDocs(jsonInput.toString());
		Map<String, Object> out = dr.parseJson2Map(jsonOut);
		System.out.println(out.get("keywords").toString());
		System.out.println(out.get("resultCount").toString());
		System.out.println(out.get("docList").toString());
			
	}

//	@Test
//	public void testTerm() throws SQLException {
//		Connection con = DBUtil.getConnection();
//		DBOperator dbop = new DBOperator(con);
//		
//		Term term = dbop.getPostingListByTerm("上");
//		for(String k:term.getPosting_list().keySet()) {
//			Document doc = term.getPosting_list().get(k);
//			System.out.println(doc.getFlag()+"---"+doc.getDocID()+"---"+doc.getTf());
//			for(int loc:doc.getLocation_list()) {
//				System.out.print(loc+" ");
//			}
//			System.out.println();
//		}
//		
//	}
	
//	@Test
//	public void testGetNewsInfo() throws SQLException{
//		Connection con = DBUtil.getConnection();
//		DBOperator dbop = new DBOperator(con);
//		
//		TimeAndCatg tc = dbop.getTimeAndCatgByDocID("0+10");
//		
//		System.out.println(tc.getTs().toString());
//	}
	
	
//	@Test
//	public void testGetDocSize() throws SQLException {
//		Connection con = DBUtil.getConnection();
//		DBOperator dbop = new DBOperator(con);
//		
//		int size = dbop.getDocSize();
//		System.out.println(size);
//	}
	
//	@Test
//	public void testLP() {
//		 List<com.hankcs.hanlp.seg.common.Term> arr = HanLP.segment("十九大科技发展");
//		for(com.hankcs.hanlp.seg.common.Term s:arr) {
//			System.out.println(s.word);
//		}
//		
//	}
	
//	@Test
//	public void readStopWord() {
//
//		Collection<String> stopWord = new ArrayList<String>();
//		try {
//			File file = new File("src/stopWord.txt");
//			InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
//			BufferedReader br = new BufferedReader(reader);
//			String line = "";
//			while((line=br.readLine()) != null) {
//				stopWord.add(line);
//			}
//		}catch(IOException e) {
//			e.printStackTrace();
//		}
//		
//		for(String t:stopWord) {
//			System.out.println(t);
//		}
//	}
	
//	@Test
//	public void testSort() {
//		Map<String, Integer> map = new HashMap<String, Integer>();
//		map.put("d", 2);
//		map.put("c", 1);
//		map.put("b", 1);
//		map.put("a", 3);
//
//		List<Map.Entry<String, Integer>> infoIds =
//		    new ArrayList<Map.Entry<String, Integer>>(map.entrySet());
//
//		//排序前
//		for (int i = 0; i < infoIds.size(); i++) {
//		    String id = infoIds.get(i).toString();
//		    System.out.println(id);
//		}
//
//		//排序
//		Collections.sort(infoIds, new Comparator<Map.Entry<String, Integer>>() {   
//		    public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {      
//		        //return (o2.getValue() - o1.getValue()); 
//		        return -(o1.getValue()).compareTo(o2.getValue());
//		    }
//		}); 
//
//		//排序后
//		for (int i = 0; i < infoIds.size(); i++) {
//		    String[] ss = infoIds.get(i).toString().split("=");
//		    Integer sc = Integer.parseInt(ss[1]);
//		    System.out.println(sc);
//		}
//	}
	

//	public Map<String, Object> testPack(){
//		Map<String, Object> pack = new HashMap<String, Object>();
//		
//		Map<String, Double> score = new HashMap<String, Double>();
//		score.put("0+12", 0.32);
//		
//		List<String> rank = new ArrayList<String>();
//		rank.add("0+21");
//		rank.add("1+5");
//		
//		pack.put("s", score);
//		pack.put("rank", rank);
//		
//		return pack;
//	}
//	
//	@Test
//	public void testP() {
//		Map<String, Object> pack = this.testPack();
//		System.out.println(((Map<String, Double>)pack.get("s")).get("0+12"));
//		System.out.println((List<String>)pack.get("rank"));
//	}
}
