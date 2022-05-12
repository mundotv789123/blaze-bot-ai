package mundotv.blazebot.ia.training;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

public class Trainer {

    private final List<TrainerThread> threads = new ArrayList();
    private final List<BotTrainer> bots = new ArrayList();
    private int brokens = 0, lowBets = 0, lowWallets = 0;

    private BotTrainer bestBot = null;

    public Trainer(int bots) {
        for (int i = 0; i < bots; i++) {
            this.bots.add(new BotTrainer());
        }
    }

    @Nullable
    public BotTrainer traneBots(int threads, List<Integer> history) throws SQLException {
        refreshNetworks();
        bestBot = null;

        int range = (bots.size() / threads);
        for (int i = 0; i < threads; i++) {
            int start = range * i;
            TrainerThread tt = new TrainerThread(history, start, (start + range), bots);

            this.threads.add(tt);
            tt.start();
        }

        for (TrainerThread tt : this.threads) {
            try {
                tt.join();
            } catch (InterruptedException ex) {
            }
        }

        this.threads.clear();

        for (BotTrainer b : bots) {
            bestBot = getBetter(bestBot, b);
        }
        return bestBot;
    }

    public void refreshNetworks() {
        for (BotTrainer b : bots) {
            b.reset();

            if (bestBot == null) {
                b.getNeuralNetwork().sortWeights();
                continue;
            }

            if (b.equals(bestBot)) {
                continue;
            }

            b.getNeuralNetwork().sortWeights(bestBot.getNeuralNetwork(), 15);
        }
    }

    private BotTrainer getBetter(BotTrainer bot1, BotTrainer bot2) {
        /* eliminando os piores bots */
        if (bot2.isBroken()) {
            brokens++;
            return bot1;
        }

        /* requisitos mínimos */
        if (bot2.getStatus().getBets() < 200) {
            lowBets++;
            return bot1;
        }

        /*if (bot2.getStatus().getWalletPercent() <= 50) {
            lowWallets++;
            return bot1;
        }*/
        /*if (bot2.getStatus().getWalletPercent() < 55) {
            lowWallets++;
            return bot1;
        }*/

        if (bot2.getStatus().getLoss() > bot2.getStatus().getWin()) {
            return bot1;
        }

        /*if (bot2.getStatus().getWalletLoss() > bot2.getStatus().getWalletWin()) {
            return bot1;
        }*/
        if (bot2.getStatus().getColorPercent() > 80) {
            return bot1;
        }
        /* pegando o primeiro */
        if (bot1 == null) {
            return bot2;
        }

        /* selecionando os melhores bots */
        //if (bot2.getStatus().getWinPercent() > bot1.getStatus().getWinPercent()) {
        /*if ((bot1.getStatus().getWalletPercent() * 0.85) > bot2.getStatus().getWalletPercent()) {
                return bot1;
            }*/
        //return bot2;
        //}
        /*if (bot2.getStatus().getWalletPercent() > bot1.getStatus().getWalletPercent()) {
            if ((bot1.getStatus().getWinPercent() * 0.95) > bot2.getStatus().getWinPercent()) {
                return bot1;
            }
            return bot2;
        }
        
        if (bot2.getStatus().getWinPercent() > bot1.getStatus().getWinPercent()) {
            if ((bot1.getStatus().getWalletPercent() * 0.95) > bot1.getStatus().getWalletPercent()) {
                return bot1;
            }
            return bot2;
        }*/
 /*if (bot2.getStatus().getWalletPercent() > bot1.getStatus().getWalletPercent()) {
            if (bot2.getWallet() < bot1.getWallet()) {
                return bot1;
            }
            if (bot2.getStatus().getWinPercent() < bot1.getStatus().getWinPercent()) {
                return bot1;
            }
            return bot2;
        }*/
        if (/*bot2.getWallet() > bot1.getWallet() && */bot2.getStatus().getWalletPercent() > bot1.getStatus().getWalletPercent()) {
            return bot2;
        }

        return bot1;
    }

    public void setBestBot(BotTrainer bestBot) {
        this.bestBot = bestBot;
    }

    private BotTrainer getBetter2(BotTrainer bot1, BotTrainer bot2) {
        if (bot2.isBroken()) {
            return bot1;
        }
        if (bot2.getStatus().getBets() < 250) {
            return bot1;
        }
        if (bot1 == null) {
            return bot2;
        }
        if (bot1.equals(bot2)) {
            return bot1;
        }
        if (bot1.getStatus().getWalletPercent() > bot2.getStatus().getWalletPercent()) {
            return bot1;
        }
        if (bot1.getStatus().getWinPercent() * 0.85 >= bot2.getStatus().getWinPercent()) {
            return bot1;
        }
        return bot2;
    }

    @Nullable
    public BotTrainer getBestBot() {
        return bestBot;
    }

    public int getBrokens() {
        return brokens;
    }

    public int getLowBets() {
        return lowBets;
    }

    public int getLowWallets() {
        return lowWallets;
    }

    public void resetStatus() {
        brokens = 0;
        lowBets = 0;
        lowWallets = 0;
    }
}
