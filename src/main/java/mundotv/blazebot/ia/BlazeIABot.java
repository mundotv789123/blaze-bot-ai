package mundotv.blazebot.ia;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import lombok.Getter;
import mundotv.blazebot.bot.BlazeBot;
import mundotv.blazebot.bot.ColorEnum;
import mundotv.ia.NeuralNetwork;

@Getter
public class BlazeIABot extends BlazeBot {

    public static int HISTORY_LIMIT = 20;
    private final NeuralNetwork network;

    public BlazeIABot(double wallet, float... gales) {
        super(wallet, gales);
        this.network = new NeuralNetwork(HISTORY_LIMIT, HISTORY_LIMIT, Math.round((HISTORY_LIMIT+3)/2), 3);
    }

    public BlazeIABot(File network, double wallet, float... gales) throws IOException, FileNotFoundException, ClassNotFoundException {
        super(wallet, gales);
        this.network = NeuralNetwork.inportFile(network);
    }

    public boolean doBet(List<Integer> history) {
        Integer[] inputs = new Integer[HISTORY_LIMIT];
        history.toArray(inputs);
        Integer[] actions = network.getActions(inputs);
        
        if (actions[0] > 0) {
            return this.doBet(ColorEnum.WHITE.getColor());
        }
        
        if (actions[1] > 0) {
            return this.doBet(ColorEnum.RED.getColor());
        }
        
        if (actions[2] > 0) {
            return this.doBet(ColorEnum.BLACK.getColor());
        }
        return false;
    }

}
