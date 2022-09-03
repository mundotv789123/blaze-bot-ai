package mundotv.blazebot.ia;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import mundotv.blazebot.bot.BlazeBot;
import mundotv.ia.NeuralNetwork;

public class BotTrainer {

    @Getter
    @Setter
    private BlazeIABot bestBot = null;
    private final float wallet;
    private final List<BlazeIABot> bots = new ArrayList();
    private int count = 0;

    public BotTrainer(int botsCount, float wallet, float white, float... gales) {
        this.wallet = wallet;
        for (int i = 0; i < botsCount; i++) {
            bots.add(new BlazeIABot(wallet, white, gales));
        }
    }

    public BotTrainer(int botsCount, NeuralNetwork network, float wallet, float white, float... gales) {
        this.wallet = wallet;
        for (int i = 0; i < botsCount; i++) {
            bots.add(new BlazeIABot(network, wallet, white, gales));
        }
    }

    public void traine(List<Integer> datas) {
        this.count = 0;
        for (BlazeIABot bot : bots) {
            count++;
            if (bestBot == null) {
                bot.getNetwork().sortWeights();
            } else {
                bot.getNetwork().sortWeights(bestBot.getNetwork());
            }
            bot.resetAll();
            bot.setWallet(wallet);

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
                System.out.println("\033[1K\rBest: R$ " + Math.round(bestBot.getWallet() * 100f) / 100f);
            }
        }
    }

    public Thread traineThread(List<Integer> datas) {
        Thread thread = new Thread(() -> {
            this.traine(datas);
        });
        thread.start();
        return thread;
    }

    public boolean isBetter(BlazeIABot bot) {
        if (bot.getWins() < bot.getLoss()) {
            return false;
        }
        if (bot.getWallet() < wallet) {
            return false;
        }
        /*if (bot.getPercentWins() < 80) {
            return false;
        }*/
        if (bestBot == null || bestBot.getWallet() < wallet) {
            return true;
        }
        return bot.getPercentWins() > bestBot.getPercentWins();
    }

    public int getPercent() {
        return count * 100 / bots.size();
    }

}
