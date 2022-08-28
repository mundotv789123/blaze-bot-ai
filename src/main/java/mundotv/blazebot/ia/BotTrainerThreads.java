package mundotv.blazebot.ia;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

public class BotTrainerThreads {

    @Getter
    private BlazeIABot bestBot = null;
    private final List<BotTrainer> bots = new ArrayList();

    public BotTrainerThreads(int threads, int bots, float... gales) {
        int botpt = Math.round(bots / threads);
        for (int t = 0; t < threads; t++) {
            this.bots.add(new BotTrainer(botpt, gales));
        }
    }

    public void trane(List<Integer> datas) {
        List<Thread> threads = new ArrayList();
        for (BotTrainer bot : bots) {
            threads.add(bot.traneThread(datas));
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ex) {
            }
        }
        for (BotTrainer bot : bots) {
            if (bestBot == null || (bot.getBestBot() != null && !bot.isBetter(bestBot))) {
                bestBot = bot.getBestBot();
            }
        }
        /*for (BotTrainer bot : bots) {
            bot.setBestBot(bestBot);
        }*/
    }

}
