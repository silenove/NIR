package ir.docrank;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {
	
	public static Connection getConnection() {
		//声明Connection对象
		Connection con = null;
		//驱动程序名
		String driver = "com.mysql.jdbc.Driver";
		//URL指向要访问的数据库名称
		String url = "jdbc:mysql://124.16.81.231:3306/information_retrieval";
		
		String user = "root";
		String passwd = "informationRetrieval";
		
		
		try {
			Class.forName(driver);
			con = DriverManager.getConnection(url, user, passwd);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return con;
		
	}

}
