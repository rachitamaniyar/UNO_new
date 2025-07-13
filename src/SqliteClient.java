import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class SqliteClient {
    private final Connection connection;

    public SqliteClient(String dbName) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbName);
    }

    public boolean tableExists(String tableName) throws SQLException {
        String query = "SELECT name FROM sqlite_master WHERE type='table' AND name=?;";
        ArrayList<HashMap<String, String>> result = executePreparedQuery(query, new Object[]{tableName});
        return !result.isEmpty();
    }

    public void executeStatement(String sqlStatement) throws SQLException {
        Statement statement = connection.createStatement();
        statement.setQueryTimeout(30);
        statement.executeUpdate(sqlStatement);
        statement.close();
    }

    // Prepared statement for INSERT/UPDATE/DELETE
    public void executePreparedStatement(String sql, Object[] params) throws SQLException {
        PreparedStatement pstmt = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
        pstmt.executeUpdate();
        pstmt.close();
    }

    // Prepared statement for SELECT
    public ArrayList<HashMap<String, String>> executePreparedQuery(String sql, Object[] params) throws SQLException {
        PreparedStatement pstmt = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
        ResultSet rs = pstmt.executeQuery();
        ResultSetMetaData rsmd = rs.getMetaData();
        int columns = rsmd.getColumnCount();
        ArrayList<HashMap<String, String>> result = new ArrayList<>();
        while (rs.next()) {
            HashMap<String, String> map = new HashMap<>();
            for (int i = 1; i <= columns; i++) {
                map.put(rsmd.getColumnName(i), rs.getString(i));
            }
            result.add(map);
        }
        rs.close();
        pstmt.close();
        return result;
    }
}
