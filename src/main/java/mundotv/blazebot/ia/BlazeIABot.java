package mundotv.blazebot.ia;

import java.util.List;
import lombok.Getter;
import mundotv.blazebot.bot.BlazeBot;
import mundotv.blazebot.bot.ColorEnum;
import mundotv.ia.NeuralNetwork;

@Getter
public class BlazeIABot extends BlazeBot {

    public static int HISTORY_LIMIT = 20;
    private final NeuralNetwork network;

    public BlazeIABot(double wallet, float white, float... gales) {
        super(wallet, white, gales);
        int outputs = 3;
        this.network = new NeuralNetwork(HISTORY_LIMIT, HISTORY_LIMIT, Math.round((HISTORY_LIMIT + outputs) / 2), outputs);
    }

    public BlazeIABot(NeuralNetwork network, double wallet, float white, float... gales) {
        super(wallet, white, gales);
        this.network = network;
    }

    public boolean doBet(List<Integer> history) {
        Integer[] inputs = new Integer[HISTORY_LIMIT];
        history.toArray(inputs);
        Integer[] actions = network.getActions(inputs);

        int action = (actions[0] > 0 ? 1 : 0) + (actions[1] > 0 ? 1 : 0);
        boolean gale = actions[2] > 0;
        switch (action) {
            case 1:
                return this.doBet(ColorEnum.RED.getColor(), gale);
            case 2:
                return this.doBet(ColorEnum.BLACK.getColor(), gale);
        }

        return false;
    }

}
