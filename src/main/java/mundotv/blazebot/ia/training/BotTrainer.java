package mundotv.blazebot.ia.training;

import java.util.List;
import mundotv.blazebot.bot.BotListeners;
import mundotv.blazebot.bot.IABot;
import mundotv.blazebot.ia.NeuralNetwork;

/* Bot de treinamento para I.A ficar esperta! */
public class BotTrainer extends IABot implements BotListeners {

    /* estatisticas */
    private final BotStates status = new BotStates();
    private boolean debug = false;

    public BotTrainer() {
        super();
        this.listener = (BotListeners) this;
    }
    
    public BotTrainer(NeuralNetwork nn) {
        super(nn);
        this.listener = (BotListeners) this;
    }

    /* pegando ações da rede neural e processando */
    public void trane(List<Integer> history, int color) {
        color = (color == 0 ? 3 : color);

        if (history.size() < BotTrainer.getHistorySize()) {
            return;
        }
        if (isBroken()) {
            return;
        }

        boolean gale = loadBets(history);
        if (!getBets().isEmpty()) {
            for (int c : getBets()) {
                if (debug) {
                    System.out.println("Jogue R$: " + getValue() + " na cor: " + c + " R$: " + getWallet());
                }
            }
            if (gale) {
                if (debug) {
                    System.out.println("obs: prepare um possível gale");
                }
            }
            if (debug) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
            }
        }

        processBets(color);
        if (isBroken()) {
            if (debug) {
                System.out.println("quebrou!");
            }
        }
    }

    public boolean loadBets(List<Integer> history) {
        Integer[] action = getActions(history);

        boolean g = action[3] > 0;

        if (action[1] > 0) {
            doBet(1, g);
        }
        if (action[2] > 0) {
            doBet(2, g);
        }

        if (action[0] > 0) {
            doBet(3, false);
        }

        return g;
    }

    /* resetando status */
    @Override
    public void reset() {
        super.reset();
        status.reset();
    }

    public BotStates getStatus() {
        return status;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    @Override
    public void onGale(int color, int gale) {
        status.addWalletLoss(getValue());
        if (debug) {
            System.out.println("Perdeu faça gale de R$: " + getValue() + " na cor " + getGaleColor() + " R$: " + getWallet());
        }
    }

    @Override
    public void onWin(int color, int gale) {
        status.addWin(getValue() * (color == 3 ? 14 : 2));
        if (debug) {
            System.out.println("Ganhou na cor " + color + " R$: " + getWallet());
        }
    }

    @Override
    public void onLoss(int color, int gale) {
        status.addLoss();
        if (debug) {
            System.out.println("Perdeu! R$: " + getWallet());
        }
    }

    @Override
    public void onBet(int color, boolean gale) {
        status.addBet(color, (color == 3 ? 2 : getValue()));
    }
}
