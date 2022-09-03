package mundotv.blazebot.bot;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
public class BlazeBot {

    @Setter
    private double wallet;
    private final float white;
    private final float[] gales;
    private int cgale = 0;
    private boolean gale = false;

    @Getter
    private int wins = 0, loss = 0;
    @Setter
    private int bet = -1;

    public BlazeBot(double wallet, float white, float... gales) {
        this.wallet = wallet;
        this.white = white;
        this.gales = gales;
    }

    public Status processBets(int color) {
        if (bet == -1) {
            return Status.NONE;
        }

        if (color == bet || (color == 0 && cgale == 0)) {
            wallet += (color == 0 ? white * 14 : (gales[cgale] * 2));  // verificando se for branco dar o valor 2 multiplicado por 14
            wins++;
            reset();
            return Status.WIN;
        }

        if (gale && gales.length > ++cgale) {
            wallet -= (gales[cgale]);
            return Status.GALE;
        }

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
        wallet -= (gales[cgale] + (color == 0 ? 0 : 2)); // apostando com proteção do branco
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
    }

    public int getPercentWins() {
        return wins * 100 / (wins + loss);
    }

    public static enum Status {
        GALE, WIN, LOSS, NONE
    }
}
