package mundotv.blazebot.ia;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import mundotv.blazebot.bot.BlazeBot;

public class BotTrainer {

    @Getter
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
                if (history.size() < 20) {
                    continue;
                }
                while (history.size() > 20) {
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
                System.out.println(bot.getWallet() + " é o melhor agora");
            }
        }
    }
    
    public Thread traneThread(List<Integer> datas) {
        Thread thread = new Thread(() -> {
            this.trane(datas);
        });
        thread.start();
        return thread;
    }

    public boolean isBetter(BlazeIABot bot) {
        return bot.getWallet() > 50 && (bestBot == null || bestBot.getWallet() < bot.getWallet());
    }
    
}
