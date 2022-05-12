package mundotv.blazebot.ia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkTrainer {

    protected final List<NeuralNetwork> leaners = new ArrayList();
    protected final Map<Integer[], Integer[]> datas = new HashMap();
    protected NeuralNetwork best;

    public NetworkTrainer(int leaners, int inputs, int... outputs) {
        for (int i = 0; i < leaners; i++) {
            this.leaners.add(new NeuralNetwork(inputs, outputs));
        }
    }

    public void traineAll() {
        while (best == null) {
            for (NeuralNetwork nn : leaners) {
                traine(nn);
            }
            refreshNetworks(1);
            System.out.println("atualizando rede");
        }
    }

    public void traine(NeuralNetwork nn) {
        for (Integer[] inputs : datas.keySet()) {
            Integer[] actions = datas.get(inputs);
            Integer[] actions2 = nn.getActions(inputs);
            
            if (actions.length != actions2.length) {
                throw new IndexOutOfBoundsException();
            }
            
            for (int i = 0; i < actions.length; i++) {
                if ((actions[i] > 0 && actions2[i] <= 0) || (actions[i] <= 0 && actions2[i] > 0)) {
                    return;
                }
            }
        }
        best = nn;
    }

    public void refreshNetworks(int variation) {
        for (NeuralNetwork leaner : leaners) {
            if (best != null) {
                leaner.sortWeights(best, variation);
                continue;
            }
            leaner.sortWeights();
        }
    }

    public NeuralNetwork getBestNetwork() {
        return best;
    }

    public List<NeuralNetwork> getLeaners() {
        return leaners;
    }

    public Map<Integer[], Integer[]> getDatas() {
        return datas;
    }

}
