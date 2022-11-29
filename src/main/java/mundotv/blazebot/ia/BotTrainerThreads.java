package mundotv.blazebot.ia;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import mundotv.ia.NeuralNetwork;

public class BotTrainerThreads {

    @Getter
    private BlazeIABot bestBot = null;
    @Getter
    private final List<BotTrainer> bots = new ArrayList();

    public BotTrainerThreads(int threads, int bots, int percent, float wallet, float[] whites, float[] gales) {
        int botspt = Math.round(bots / threads);
        for (int t = 0; t < threads; t++) {
            this.bots.add(new BotTrainer(botspt, percent, wallet, whites, gales));
        }
    }

    public BotTrainerThreads(int threads, int bots, NeuralNetwork network, int percent, float wallet, float[] whites, float[] gales) {
        int botspt = Math.round(bots / threads);
        for (int t = 0; t < threads; t++) {
            this.bots.add(new BotTrainer(botspt, network, percent, wallet, whites, gales));
        }
    }

    public void traine(List<Integer> datas) {
        /* executando os bots em threads */
        List<Thread> threads = bots.stream().map(b -> b.traineThread(datas)).collect(Collectors.toList());

        /* entrando e aguardando os threads terminarem */
        threads.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
            }
        });

        /* definindo a melhor rede */
        bots.forEach(b -> {
            if (bestBot == null || (b.getBestBot() != null && !b.isBetter(bestBot))) {
                bestBot = b.getBestBot();
            }
        });

        /* aplicando a melhor rede em threads sem melhor rede */
        bots.stream().filter(b -> b.getBestBot() == null).forEach(b -> {
            b.setBestBot(bestBot);
        });
    }

}
