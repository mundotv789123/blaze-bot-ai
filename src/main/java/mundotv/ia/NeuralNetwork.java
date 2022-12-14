package mundotv.ia;

import com.google.gson.Gson;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class NeuralNetwork implements Serializable {

    private final int inputs;
    private final Neuron[] neurons;
    private final NeuralNetwork children;

    /* construtores */
    public NeuralNetwork(int inputs, int... outputs) {
        this.inputs = inputs;
        this.neurons = new Neuron[outputs[0]];

        /* interligando as redes de acordo com os valores */
        NeuralNetwork nn = null;
        for (int i = (outputs.length - 1); i > 0; i--) {
            nn = new NeuralNetwork(outputs[i - 1], outputs[i], nn);
        }
        children = nn;

        /* gerando neurônios */
        for (int i = 0; i < this.neurons.length; i++) {
            this.neurons[i] = new Neuron(inputs);
        }
    }

    /* esse construtor serve apenas para interligar as redes */
    private NeuralNetwork(int inputs, int neurons, NeuralNetwork children) {
        this.inputs = inputs;
        this.neurons = new Neuron[neurons];
        this.children = children;
        for (int i = 0; i < neurons; i++) {
            this.neurons[i] = new Neuron(inputs);
        }
    }

    /* pegando os sinais */
    private Integer[] getSignals(Integer... inputs) {
        if (inputs.length != this.inputs) {
            throw new IndexOutOfBoundsException("inputs length need to be " + this.inputs + " not " + inputs.length);
        }

        Integer[] outputs = new Integer[neurons.length];

        for (int i = 0; i < outputs.length; i++) {
            outputs[i] = neurons[i].getSignal(inputs);
        }
        return outputs;
    }

    /* calculando os sinais para pegar as saidas */
    public Integer[] getActions(Integer... inputs) {
        if (children != null) {
            return children.getActions(this.getSignals(inputs));
        }
        return this.getSignals(inputs);
    }

    /* sorteando pesos */
    public void sortWeights(NeuralNetwork network, int variation) {
        Neuron[] neurons2 = network.getNeurons();
        if (neurons2.length != this.neurons.length) {
            throw new IndexOutOfBoundsException("neural network has incompatible neurons");
        }

        for (int i = 0; i < neurons.length; i++) {
            this.neurons[i].sortWeights(neurons2[i].getWeights(), variation);
        }
        if (children != null) {
            children.sortWeights(network.getChildren(), variation);
        }
    }

    /* sorteando pesos (variante padrão) */
    public void sortWeights(NeuralNetwork network) {
        sortWeights(network, 1);
    }
    
    /* sorteando aleatório */
    public void sortWeights() {
        sortWeights(0);
    }

    /* sorteando aleatório baseados em variação*/
    public void sortWeights(int variation) {
        for (Neuron neuron : neurons) {
            if (variation < 0) {
                neuron.sortWeights();
            } else {
                neuron.sortWeights(variation);
            }
        }
        if (children != null) {
            children.sortWeights(variation);
        }
    }

    /* getters */
    public int getInputs() {
        return inputs;
    }

    public Neuron[] getNeurons() {
        return neurons;
    }

    public NeuralNetwork getChildren() {
        return children;
    }

    /* funções para importar/exportar arquivo da I.A. */
    public void exportFile(File file, boolean json) throws FileNotFoundException, IOException {
        if (!json) {
            try ( FileOutputStream fout = new FileOutputStream(file);  ObjectOutputStream oout = new ObjectOutputStream(fout)) {
                oout.writeObject(this);
            }
            return;
        }
        try (FileWriter fw = new FileWriter(file)) {
            Gson gson = new Gson();
            gson.toJson(this, fw);
        }
    }

    public static NeuralNetwork importFile(File file, boolean json) throws FileNotFoundException, IOException, ClassNotFoundException {
        NeuralNetwork nn;
        if (!json) {
            try ( FileInputStream fin = new FileInputStream(file);  ObjectInputStream oin = new ObjectInputStream(fin)) {
                nn = (NeuralNetwork) oin.readObject();
            }
        } else {
            Gson gson = new Gson();
            nn = gson.fromJson(new FileReader(file), NeuralNetwork.class);
        }
        return nn;
    }

}
