package mundotv.blazebot.ia.training;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

public class Trainer {

    private final List<TrainerThread> threads = new ArrayList();
    private final List<BotTrainer> bots = new ArrayList();

    private BotTrainer bestBot = null;

    public Trainer(int bots) {
        for (int i = 0; i < bots; i++) {
            this.bots.add(new BotTrainer());
        }
    }
    
    @Nullable
    public BotTrainer traneBots(int threads, PreparedStatement ps) throws SQLException {
        refreshNetworks();
        bestBot = null;

        int range = (bots.size() / threads);
        for (int i = 0; i < 6; i++) {
            int start = range * i;
            TrainerThread tt = new TrainerThread(ps.executeQuery(), start, (start + range), bots);
            this.threads.add(tt);
            tt.start();
        }
        while (hasThreads()) {
        }
        
        for (BotTrainer b : bots) {
            bestBot = getBetter(bestBot, b);
        }

        return bestBot;
    }

    public boolean hasThreads() {
        for (TrainerThread tt : threads) {
            if (!tt.isInterrupted()) {
                return true;
            }
        }
        threads.clear();
        return false;
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

            b.getNeuralNetwork().sortWeights(bestBot.getNeuralNetwork());
        }
    }

    private BotTrainer getBetter(BotTrainer bot1, BotTrainer bot2) {
        /* eliminando os piores bots */
        if (bot2.isBroken()) {
            return bot1;
        }

        if (bot2.getStatus().getBets() < 250) {
            return bot1;
        }

        if (bot2.getStatus().getLoss() > bot2.getStatus().getWin()) {
            return bot1;
        }
        
        if (bot2.getStatus().getWalletWin() < bot2.getStatus().getWalletLoss()) {
            return bot1;
        }

        /* pegando o primeiro */
        if (bot1 == null) {
            return bot2;
        }

        /* selecionando os melhores bots */
        if (bot2.getStatus().getWinPercent() > bot1.getStatus().getWinPercent()) {
            if ((bot1.getStatus().getWalletPercent() * 0.85) > bot2.getStatus().getWalletPercent()) {
                return bot1;
            }
            //if (bot2.getStatus().getWalletPercent() <= 50) {
                /*if (bot1.getStatus().getWhite() > bot2.getStatus().getWhite()) {
                    return bot2;
                }*/
                //return bot1;
            //}
            return bot2;
        }

        if (bot2.getStatus().getWalletPercent() < bot1.getStatus().getWalletPercent()) {
            if ((bot1.getStatus().getWinPercent() * 0.85) > bot2.getStatus().getWinPercent()) {
                return bot1;
            }
            //if (bot2.getStatus().getWalletPercent() <= 50) {
                /*if (bot1.getStatus().getWhite() > bot2.getStatus().getWhite()) {
                    return bot2;
                }*/
                //return bot1;
            //}
            return bot2;
        }

        return bot1;
    }
    
    @Nullable
    public BotTrainer getBestBot() {
        return bestBot;
    }
}
