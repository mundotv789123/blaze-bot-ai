package mundotv.blazebot.bot.online;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mundotv.blazebot.api.listenners.DoubleSocket;
import mundotv.blazebot.api.results.ColorResult;
import mundotv.blazebot.database.Database;

public class HistoryCollector extends DoubleSocket {

    private final Database database;

    public HistoryCollector(Database database) throws URISyntaxException, SQLException {
        this.database = database;
        this.database.createHistoryTable();
    }

    @Override
    public void handlerColor(ColorResult color) {
        try {
            database.addHistory(color);
        } catch (SQLException ex) {
            Logger.getLogger(HistoryCollector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
