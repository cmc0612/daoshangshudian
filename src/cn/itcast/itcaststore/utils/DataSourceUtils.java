package cn.itcast.itcaststore.utils;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;//jdbc提供的一个获取连接对象的一个接口
import com.mchange.v2.c3p0.ComboPooledDataSource;//这个类是DataSource接口的实现类，也称之为数据源    
/**
 * 数据源工具
 */
public class DataSourceUtils {
	private static DataSource dataSource = new ComboPooledDataSource();//向上转型，把数据源转给dataSource，这里用的是无参构造方法。
	private static ThreadLocal<Connection> tl = new ThreadLocal<Connection>();

	public static DataSource getDataSource() {
		return dataSource;
	}
	/**
	 * 当DBUtils需要手动控制事务时，调用该方法获得一个连接
	 * @return
	 * @throws SQLException
	 */
	public static Connection getConnection() throws SQLException {//无参的登录连接
		Connection con = tl.get();
		if (con == null) {
			con = dataSource.getConnection();
			tl.set(con);
		}
		return con;
	}
	/**
	 * 开启事务
	 * @throws SQLException
	 */
	public static void startTransaction() throws SQLException {
		Connection con = getConnection();
		if (con != null)
			con.setAutoCommit(false);
	}
	/**
	 * 从ThreadLocal中释放并且关闭Connection,并结束事务
	 * @throws SQLException
	 */
	public static void releaseAndCloseConnection() throws SQLException {
		Connection con = getConnection();
		if (con != null) {
			con.commit();
			tl.remove();
			con.close();
		}
	}
	/**
	 * 事务回滚
	 * @throws SQLException 
	 */
	public static void rollback() throws SQLException {
		Connection con = getConnection();
		if (con != null) {
			con.rollback();
		}
	}
}
