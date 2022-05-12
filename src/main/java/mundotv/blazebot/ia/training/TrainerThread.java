package mundotv.blazebot.ia.training;

import java.util.ArrayList;
import java.util.List;

public class TrainerThread extends Thread {

    private final List<Integer> history;
    private final int start, stop;
    private final List<BotTrainer> bots;
    private final List<Integer> historyCache = new ArrayList();

    public TrainerThread(List<Integer> history, int start, int stop, List<BotTrainer> bots) {
        this.history = history;
        this.start = start;
        this.stop = (stop > bots.size() ? bots.size() : stop);
        this.bots = bots;
    }

    @Override
    public void run() {
        for (int color : history) {
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
        this.interrupt();
    }

}
