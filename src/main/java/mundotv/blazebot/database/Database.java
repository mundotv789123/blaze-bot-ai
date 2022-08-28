package mundotv.blazebot.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import mundotv.blazebot.api.results.ColorResult;

public class Database {

    private final String host, database, username, password;

    public Database(String host, String database, String username, String password) {
        this.host = host;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    private Connection getConn() throws SQLException {
        return DriverManager.getConnection("jdbc:mariadb://" + host + "/" + database + "?useSSL=false", username, password);
    }

    public void createHistoryTable() throws SQLException {
        Connection conn = getConn();
        conn.prepareStatement("CREATE TABLE IF NOT EXISTS `bz_history` (`id` INT AUTO_INCREMENT, `uuid` VARCHAR(16) NOT NULL, `color` INT NOT NULL, `roll` INT NOT NULL, `created_at` DATETIME DEFAULT NOW(), PRIMARY KEY(`id`))").execute();
    }

    public void addHistory(ColorResult c) throws SQLException {
        try ( Connection conn = getConn()) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO `bz_history` (`uuid`, `color`, `roll`) VALUES (?, ?, ?)");
            ps.setString(1, c.getId());
            ps.setInt(2, Math.round(c.getColor()));
            ps.setInt(3, Math.round(c.getRoll()));
            ps.execute();
        }
    }

    public List<ColorResult> getAllHistory(int limit) throws SQLException {
        List<ColorResult> colors = new ArrayList();
        try ( Connection conn = getConn()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM `bz_history` order by created_at desc limit ?");
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                colors.add(new ColorResult((float) rs.getInt("color"), (float) rs.getInt("roll")));
            }
        }
        return colors;
    }

}
