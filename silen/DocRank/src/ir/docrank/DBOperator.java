package ir.docrank;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.zip.CRC32;

import ir.docrank.domain.Term;
import ir.docrank.domain.TimeAndCatg;

public class DBOperator {
	
	private Connection con;
	
	public DBOperator(Connection con) {
		this.con = con;
	}
	
	//计算词项hash
	private long term2hash(String term) {
		CRC32 crc32 = new CRC32();
		crc32.update(term.getBytes());
		return crc32.getValue();
	}
	
	//根据词项哈希获取词项倒排记录表
	public Term getPostingListByTerm(String t) throws SQLException{
		String sql = "select * from posting_list where term_hash = " + this.term2hash(t);
		PreparedStatement preparedStatement = con.prepareStatement(sql);
		ResultSet resultSet = preparedStatement.executeQuery();
		
		Term term = null;
		while(resultSet.next()) {
			String tmp = resultSet.getString("term");
			if(tmp.equals(t)) {//过滤哈希值相同的不同词项
				if(term == null) {//读到该词项第一条记录
					term = new Term();
					term.setTerm(resultSet.getString("term"));
					term.setDf(resultSet.getInt("df"));
					term.setTermhash(resultSet.getLong("term_hash"));
					term.setPosting_list(resultSet.getString("content"));
				}else {//读取该此项剩余记录
					term.addDf(resultSet.getInt("df"));
					term.setPosting_list(resultSet.getString("content"));
				}
			}
		}
		
		return term;
		
	}
	
	//获取总文档数
	public int getDocSize() throws SQLException {
		String sql = "select count(*) from news_info";
		PreparedStatement preparedStatement = con.prepareStatement(sql);
		ResultSet resultSet = preparedStatement.executeQuery();
		
		int count;
		resultSet.next();
		count = resultSet.getInt(1);
		return count;
	}
	
	//根据文档ID查询新闻的时间、类型和来源
	public TimeAndCatg getTimeAndCatgByDocID(String docID) throws SQLException {
		long newsID;
		//获取news id
		ResultSet resultSet = null;
		String[] infos = docID.split("\\+");
		int flag = Integer.parseInt(infos[0]);
		long id = Long.parseLong(infos[1]);
		if(flag ==1){
			String sql = "select news_id from comment_info where id=" + id;
			PreparedStatement preparedStatement = con.prepareStatement(sql);
			resultSet = preparedStatement.executeQuery();
			resultSet.next();
			newsID = resultSet.getInt(1);
		}else {
			newsID = id;
		}
		
		//获取时间和类型
		String sql = "select category,source,publish_time from news_info where id=" + newsID;
		PreparedStatement preparedStatement = con.prepareStatement(sql);
		resultSet = preparedStatement.executeQuery();
		
		resultSet.next();
		TimeAndCatg tc = new TimeAndCatg();
		tc.setSource(resultSet.getString("source"));
		tc.setCatg(resultSet.getString("category"));
		tc.setTs(resultSet.getTimestamp("publish_time"));
		
		return tc;
	}

}
