package mundotv.blazebot.bot;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
public class BlazeBot {

    @Setter
    private double wallet;
    private final float[] gales;
    private int cgale = 0;

    @Setter
    private int bet = -1;

    public BlazeBot(double wallet, float... gales) {
        this.wallet = wallet;
        this.gales = gales;
    }

    public Status processBets(int color) {
        if (bet == -1) {
            return Status.NONE;
        }

        if (color == bet || (color == 0 && cgale == 0)) {
            // verificando se for branco dar o valor 2 multiplicado por 14
            wallet += (color == 0 ? 28 : (gales[cgale] * 2));
            reset();
            return Status.WIN;
        }

        if (gales.length > ++cgale) {
            wallet -= (gales[cgale]);
            return Status.GALE;
        }

        reset();
        return Status.LOSS;
    }

    public boolean doBet(Integer color) {
        if (bet > -1) {
            return false;
        }

        if (wallet < gales[cgale]) {
            return false;
        }

        bet = color;
        // apostando com proteção do branco
        wallet -= (gales[cgale] + (color == 0 ? 0 : 2));
        return true;
    }

    public void reset() {
        cgale = 0;
        bet = -1;
    }

    public static enum Status {
        GALE, WIN, LOSS, NONE
    }
}
