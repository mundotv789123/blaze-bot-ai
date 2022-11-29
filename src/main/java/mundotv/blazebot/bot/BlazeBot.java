package mundotv.blazebot.bot;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
public class BlazeBot {

    private final float[] gales, whites;
    private int cgale = 0;
    private boolean gale = false;
    @Setter
    private int bet = -1;

    /* variaveis usadas para ranquear os bots no treinamento */
    @Setter
    private double wallet;
    private int wins = 0, loss = 0;
    private double wwins = 0, wloss = 0;

    public BlazeBot(double wallet, float[] whites, float[] gales) {
        this.wallet = wallet;
        this.whites = whites;
        this.gales = gales;
    }

    public Status processBets(int color) {
        if (bet == -1) {
            return Status.NONE;
        }

        // validando a aposta
        if (color == bet) {
            deposit(gales[cgale] * 2);
            wins++;
            reset();
            return Status.WIN;
        }
        
        // verificando proteção do branco
        if (color == 0 && whites.length > cgale && whites[cgale] > 0) {
            deposit(whites[cgale] * 14);
            wins++;
            reset();
            return Status.WIN_WHITE;
        }
        
        // verificando os gales
        if (gale && gales.length > ++cgale) {
            debit(gales[cgale]);
            if (whites.length > cgale && whites[cgale] > 0) {
                debit(whites[cgale]);
            }
            return Status.GALE;
        }
        
        // perdeu :(
        loss++;
        reset();
        return Status.LOSS;
    }

    public boolean doBet(Integer color, boolean gale) {
        this.gale = gale;

        if (bet > -1) {
            return false;
        }

        if (wallet < gales[cgale]) {
            return false;
        }

        bet = color;
        debit(gales[cgale]);
        if (whites.length > cgale && whites[cgale] > 0) {
            debit(whites[cgale]);
        }
        return true;
    }

    public void reset() {
        cgale = 0;
        bet = -1;
        gale = false;
    }

    public void resetAll() {
        this.reset();
        wins = 0;
        loss = 0;
        wwins = 0;
        wloss = 0;
    }
    
    /* funções da carteira */
    private void deposit(float value) {
        wallet += value;
        wwins += value;
    }

    private void debit(float value) {
        wallet -= value;
        wloss += value;
    }

    /* funções para ranquear os bots no treinamento */
    public int getPercentWins() {
        if ((wins + loss) == 0) {
            return 0;
        }
        return wins * 100 / (wins + loss);
    }

    public boolean hasWallet() {
        return wallet >= gales[0];
    }
    
    /* calculando pontuação do bot para ranquea-lo */
    public int getScore(float wallet) {
        if (!hasWallet()) {
            return 0;
        }
        int walletPercent = Math.round((float) this.wallet * 100 / wallet);
        return getPercentWins() + walletPercent;
    }

    public static enum Status {
        GALE, WIN, LOSS, NONE, WIN_WHITE
    }
}
