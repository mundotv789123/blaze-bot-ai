package mundotv.blazebot.ia;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import mundotv.blazebot.bot.BlazeBot;

public class BotTrainer {

    @Getter
    @Setter
    private BlazeIABot bestBot = null;
    private final List<BlazeIABot> bots = new ArrayList();

    public BotTrainer(int botsCount, float... gales) {
        for (int i = 0; i < botsCount; i++) {
            bots.add(new BlazeIABot(50, gales));
        }
    }

    public void trane(List<Integer> datas) {
        for (BlazeIABot bot : bots) {
            if (bestBot == null) {
                bot.getNetwork().sortWeights();
            } else {
                bot.getNetwork().sortWeights(bestBot.getNetwork());
            }
            bot.reset();
            bot.setWallet(50);

            List<Integer> history = new ArrayList();
            for (int color : datas) {
                history.add(color);
                if (history.size() < BlazeIABot.HISTORY_LIMIT) {
                    continue;
                }
                while (history.size() > BlazeIABot.HISTORY_LIMIT) {
                    history.remove(0);
                }
                BlazeBot.Status status = bot.processBets(color);
                if (status == BlazeBot.Status.NONE) {
                    bot.doBet(history);
                }
                if (bot.getWallet() <= 0) {
                    break;
                }
            }
            history.clear();
            if (isBetter(bot)) {
                bestBot = bot;
                System.out.println("Melhor: R$ "+bestBot.getWallet());
            }
        }
    }
    
    public Thread traneThread(List<Integer> datas) {
        Thread thread = new Thread(() -> {
            this.trane(datas);
            System.out.println("thread finalizada.");
        });
        thread.start();
        return thread;
    }

    public boolean isBetter(BlazeIABot bot) {
        return bot.getWallet() > 50 && (bestBot == null || bestBot.getWallet() < bot.getWallet());
    }
    
}
