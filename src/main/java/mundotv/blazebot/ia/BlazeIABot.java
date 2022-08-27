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

    private final NeuralNetwork network;

    public BlazeIABot(double wallet, float... gales) {
        super(wallet, gales);
        this.network = new NeuralNetwork(20, 20, 15, 3);
    }

    public BlazeIABot(File network, double wallet, float... gales) throws IOException, FileNotFoundException, ClassNotFoundException {
        super(wallet, gales);
        this.network = NeuralNetwork.inportFile(network);
    }

    public boolean doBet(List<Integer> history) {
        Integer[] inputs = new Integer[20];
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
