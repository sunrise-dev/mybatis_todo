package sunrise.Util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;

public final class DataCenter {

        //此方法为获取数据库连接
        private static Connection getConnection() {
                Connection conn = null;

                try {
                        String driver = "com.mysql.jdbc.Driver"; //数据库驱动

                        String url = "jdbc:MySQL://127.0.0.1:3306/hangersystem?serverTimezone=UTC";//数据库

                        String user = "root"; //用户名

                        String password = "root123"; //密码

                        Class.forName(driver); //加载数据库驱动

                        if (null == conn) {
                                conn = DriverManager.getConnection(url, user, password);

                        }

                } catch (ClassNotFoundException e) {
                        System.out.println("Sorry,can't find the Driver!");

                        e.printStackTrace();

                } catch (SQLException e) {
                        e.printStackTrace();

                } catch (Exception e) {
                        e.printStackTrace();

                }

                return conn;

        }

        /**
         * 查【Query】
         *
         * @paramsql
         * @returnResultSet
         */

        public static ResultSet Query(String route, String orderby, int pageNo, int pageSize, List<SearchRule> params) {
                Connection conn = null;
                Statement stmt = null;
                ResultSet rs = null;

                pageNo = Math.max(1, pageNo);
                pageSize = Math.max(1, pageSize);

                try {
                        String where = "";
                        String sql = ReadXml.getSql(route);
                        for (SearchRule rule : params) {
                                if (rule.op.isEmpty()) {
                                        sql = sql.replace(String.format("@%s", rule.field), String.format("\'%s\'", rule.value));
                                        continue;
                                }

                                where += MakeWhere(rule);
                        }

                        if (!orderby.isEmpty()){
                                where += " order by " + orderby;
                        }

                        sql += String.format("%s limit %d,%d", where, (pageNo - 1) * pageSize, pageSize);
                        conn = getConnection();
                        stmt = conn.prepareStatement(sql);
                        rs = stmt.executeQuery(sql);

                } catch (SQLException err) {
                        err.printStackTrace();

                        free(rs, stmt, conn);
                }

                return rs;
        }

        private static int QueryCount(String route, List<SearchRule> params) {
                Connection conn = null;
                Statement stmt = null;
                ResultSet rs = null;
                int num = 0;

                try {
                        String where = "";
                        String sql = ReadXml.getSql(route + "Count");
                        for (SearchRule rule : params) {
                                if (rule.op.isEmpty()) {
                                        sql = sql.replace(String.format("@%s", rule.field), String.format("\'%s\'", rule.value));
                                        continue;
                                }

                                where += MakeWhere(rule);
                        }

                        sql += where;
                        conn = getConnection();
                        stmt = conn.prepareStatement(sql);
                        rs = stmt.executeQuery(sql);

                        // 结果集是否存在数据
                        if (rs.next()){
                                num = Integer.parseInt(rs.getObject(1).toString());
                        }

                } catch (SQLException err) {
                        err.printStackTrace();

                        free(rs, stmt, conn);
                }

                return num;
        }


                /**
         * 增删改【Add、Delete、Update】
         *
         * @paramsql
         * @paramobj
         * @returnint
         */

        public static String SaveData(String route, List<SearchRule> params) {

                int code = 0;
                String msg = "提交成功！";


                int result = 0;

                Connection conn = null;
                PreparedStatement pstmt = null;

                try {
                        String sql = ReadXml.getSql(route);
                        for (SearchRule rule : params) {
                                sql = sql.replace(String.format("@%s", rule.field), String.format("\'%s\'", rule.value));
                        }

                        conn = getConnection();
                        pstmt = conn.prepareStatement(sql);
                        result = pstmt.executeUpdate();

                } catch (SQLException err) {
                        err.printStackTrace();

                        free(null, pstmt, conn);

                        code = -1;
                        msg = err.getMessage();

                } finally {
                        free(null, pstmt, conn);

                }

                Map<String, Object> resultData = new HashMap<String, Object>();
                        resultData.put("code", code);
                        resultData.put("msg", msg);
                        resultData.put("data", result);

                return JSON.toJSONString(resultData);
        }

        /**
         * 查【Query】
         *
         * @paramsql
         * @returnResultSet
         */

        public static String QueryPageData(String route, String orderby, int pageNo, int pageSize, List<SearchRule> params) {

                int count = 0;
                int code = 0;
                String msg = "数据获取成功！";
                List<Object> list = new ArrayList<Object>();

                try {
                        list = RecordList(Query(route, orderby, pageNo, pageSize, params));
                        count = list.size();
                        pageSize = Math.max(1, pageSize);
                        if (count >= pageSize) {
                                count = QueryCount(route, params);
                        }

                } catch (SQLException err) {
                        code = -1;
                        msg = err.getMessage();
                }

                Map<String, Object> resultData = new HashMap<String, Object>();
                        resultData.put("code", code);
                        resultData.put("msg", msg);
                        resultData.put("pageNo", pageNo);
                        resultData.put("pageSize", pageSize);
                        resultData.put("count", (count-1)/pageSize+1);
                        resultData.put("data", list);

                        return JSON.toJSONString(resultData);
        }

        private static List<Object> RecordList(ResultSet rs) throws SQLException {
                List<Object> list = new ArrayList<Object>();
                ResultSetMetaData resultSetMetaData = rs.getMetaData();
                int colCount = resultSetMetaData.getColumnCount();
                while (rs.next()) {
                        Map<String, Object> rowData = new HashMap<String, Object>();
                        for (int i = 1; i <= colCount; i++) {
                                rowData.put(resultSetMetaData.getColumnName(i), rs.getObject(i));
                        }
                        list.add(rowData);
                }
                return list;
        }


        private static String MakeWhere(SearchRule rule) {

                switch (rule.op) {

                        case "in":
                        case "not in":
                                rule.value = "('" + rule.value.replace(",", "','") + "')";
                                break;

                        case "like":
                                rule.value = "'%" + rule.value + "%'";
                                break;

                        default:
                                rule.value = "'" + rule.value + "'";
                                break;

                }

                return String.format(" and %s %s %s", rule.field, rule.op, rule.value);
        }

        /**
         * 释放【ResultSet】资源
         *
         * @paramrs
         */

        private static void free(ResultSet rs) {
                try {
                        if (rs != null) {
                                rs.close();

                        }

                } catch (SQLException err) {
                        err.printStackTrace();

                }

        }

        /**
         * 释放【Statement】资源
         *
         * @paramst
         */

        private static void free(Statement st) {
                try {
                        if (st != null) {
                                st.close();
                        }

                } catch (SQLException err) {
                        err.printStackTrace();
                }
        }

        /**
         * 释放【Connection】资源
         *
         * @paramconn
         */

        private static void free(Connection conn) {
                try {
                        if (conn != null) {
                                conn.close();

                        }

                } catch (SQLException err) {
                        err.printStackTrace();

                }

        }

        /**
         * 释放所有数据资源
         *
         * @paramrs
         * @paramst
         * @paramconn
         */

        private static void free(ResultSet rs, Statement st, Connection conn) {
                free(rs);
                free(st);
                free(conn);

        }
}
