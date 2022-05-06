package mundotv.blazebot.ia.training;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TrainerThread extends Thread {

    private final ResultSet history;
    private final int start, stop;
    private final List<BotTrainer> bots;
    private final List<Integer> historyCache = new ArrayList();

    public TrainerThread(ResultSet history, int start, int stop, List<BotTrainer> bots) {
        this.history = history;
        this.start = start;
        this.stop = (stop > bots.size() ? bots.size() : stop);
        this.bots = bots;
    }

    @Override
    public void run() {
        try {
            while (history.next()) {
                int color = history.getInt("color_id");
                
                color = color == 0 ? 3 : color;

                for (int c = start; c < stop; c++) {
                    BotTrainer bot = bots.get(c);
                    bot.trane(historyCache, color);
                }

                historyCache.add(color);

                if (historyCache.size() > BotTrainer.getHistorySize()) {
                    historyCache.remove(0);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        this.interrupt();
    }

}
