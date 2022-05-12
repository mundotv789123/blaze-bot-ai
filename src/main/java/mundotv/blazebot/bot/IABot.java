package mundotv.blazebot.bot;

import java.util.ArrayList;
import java.util.List;
import mundotv.blazebot.ia.NeuralNetwork;

public class IABot {

    private final NeuralNetwork network;
    private static int historySize = 5;
    private static int inputs = 0; //inputs extras alem do histórico de cores

    protected int galeColor = 0, currentGale = 0;
    protected double[] gales = {4, 8, 16};

    /* informações das apostas */
    protected double wallet = 50;
    protected final List<Integer> bets = new ArrayList();

    protected BotListeners listener;

    public IABot(NeuralNetwork network) {
        if (network.getInputs() != (historySize + inputs)) {
            throw new IllegalArgumentException("A.I File is invalid");
        }
        this.network = network;
    }

    public IABot() {
        //melhor 8 6 9 4
        this.network = new NeuralNetwork((historySize + inputs), (historySize + inputs), 8, 4);
    }

    public double getValue() {
        return gales[currentGale];
    }

    public boolean upGale() {
        if ((currentGale + 1) >= gales.length) {
            return false;
        }
        currentGale++;
        return true;
    }

    public int getGaleColor() {
        return galeColor;
    }

    public int getCurrentGale() {
        return currentGale;
    }

    public void resetGale() {
        galeColor = -1;
        currentGale = 0;
    }

    public void setGaleColor(int galeColor) {
        this.galeColor = galeColor;
    }

    public static int getHistorySize() {
        return historySize;
    }

    public NeuralNetwork getNeuralNetwork() {
        return network;
    }

    public double getWallet() {
        return wallet;
    }

    public List<Integer> getBets() {
        return bets;
    }

    public void processBets(int color) {
        /* fazendo gale */
        if (currentGale > 0) {
            bets.clear();
            if (galeColor == color) {
                addValue(getValue() * (color == 3 ? 14.0 : 2.0));
                if (listener != null) {
                    listener.onWin(color, currentGale);
                }
            } else if (upGale()) {
                removeValue(getValue());
                if (listener != null) {
                    listener.onGale(galeColor, currentGale);
                }
                return;
            }
            resetGale();
            return;
        }

        /* processando apostas */
        for (int bcolor : bets) {
            if (bcolor == color) {
                addValue(getValue() * (color == 3 ? 14.0 : 2.0));
                resetGale();
                if (listener != null) {
                    listener.onWin(galeColor, currentGale);
                }
                continue;
            }
            if (galeColor == bcolor) {
                if (upGale()) {
                    removeValue(getValue());
                    if (listener != null) {
                        listener.onGale(galeColor, currentGale);
                    }
                    continue;
                }
                resetGale();
            }
            if (listener != null) {
                listener.onLoss(galeColor, currentGale);
            }
        }
        bets.clear();
    }

    public void doBet(int color, boolean gale) {
        if (currentGale > 0) {
            return;
        }
        bets.add(color);
        removeValue(color == 3 ? 2.0 : getValue());

        if (gale && currentGale <= 0 && color != 3) {
            galeColor = color;
        }
        if (listener != null) {
            listener.onBet(color, gale);
        }
    }

    public void setListener(BotListeners listener) {
        this.listener = listener;
    }

    public void addValue(double value) {
        wallet += value;
    }

    public void removeValue(double value) {
        wallet -= value;
    }

    public boolean isBroken() {
        return wallet <= 0;
    }

    public void reset() {
        wallet = 50;
        bets.clear();
        resetGale();
    }

    public Integer[] getActions(List<Integer> history) {
        Integer[] h = new Integer[historySize + inputs];
        history.toArray(h);
        //h[historySize] = currentGale;
        //h[historySize + 1] = Math.round((float) wallet);
        return network.getActions(h);
        //return network.getActions(history.toArray(Integer[]::new));
    }
}
