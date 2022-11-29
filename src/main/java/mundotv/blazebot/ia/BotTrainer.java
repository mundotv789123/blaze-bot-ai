package mundotv.blazebot.ia;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import mundotv.ia.NeuralNetwork;

public class BotTrainer {

    @Getter
    @Setter
    private BlazeIABot bestBot = null;
    private final float wallet;
    private final List<BlazeIABot> bots = new ArrayList();
    private int count = 0, percent;

    public BotTrainer(int botsCount, int percent, float wallet, float[] whites, float[] gales) {
        this.wallet = wallet;
        this.percent = percent;
        for (int i = 0; i < botsCount; i++) {
            bots.add(new BlazeIABot(wallet, whites, gales));
        }
    }

    public BotTrainer(int botsCount, NeuralNetwork network, int percent, float wallet, float[] whites, float... gales) {
        this.wallet = wallet;
        this.percent = percent;
        for (int i = 0; i < botsCount; i++) {
            bots.add(new BlazeIABot(network, wallet, whites, gales));
        }
    }

    public void traine(List<Integer> datas) {
        this.count = 0;
        for (BlazeIABot bot : bots) {
            /* preparando jogadas */
            count++;
            if (bestBot == null) {
                bot.getNetwork().sortWeights();
            } else if (!bestBot.equals(bot)) {
                bot.getNetwork().sortWeights(bestBot.getNetwork());
            }
            bot.resetAll();
            bot.setWallet(wallet);

            /* simulando jogos */
            List<Integer> history = new ArrayList();
            for (int color : datas) {
                history.add(color);
                if (history.size() < BlazeIABot.HISTORY_LIMIT) {
                    continue;
                }
                while (history.size() > BlazeIABot.HISTORY_LIMIT) {
                    history.remove(0);
                }
                bot.processBets(color);
                bot.doBet(history);
                if (!bot.hasWallet()) {
                    break;
                }
            }

            /* verificando se foi o melhor */
            history.clear();
            if (isBetter(bot)) {
                bestBot = bot;
                System.out.println("\033[1K\rBest: R$ " + (Math.round(bestBot.getWallet() * 100f) / 100f) + " win: " + bestBot.getPercentWins() + "%" + " score: " + bestBot.getScore(wallet));
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
        // ignorar se tiver mais percas do que ganhos
        if (bot.getWins() < bot.getLoss()) {
            return false;
        }

        // ignorar se a carteira estiver menor que no inicio da jogada
        if (bot.getWallet() < wallet) {
            return false;
        }

        // ignorar se não atingir porcentagem de ganhos defindo
        if (bot.getPercentWins() < percent) {
            return false;
        }

        // ignorar se os debidos forem maiores que os depositos
        if (bot.getWloss() > bot.getWwins()) {
            return false;
        }

        // se não tiver nenhum melhor concidere-se o primeiro
        if (bestBot == null || !bestBot.hasWallet()) {
            return true;
        }

        // verificando score para tribuir o melhor
        return bot.getScore(wallet) > bestBot.getScore(wallet);
    }

    public int getPercent() {
        return count * 100 / bots.size();
    }

}
