package mundotv.blazebot.ia;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;

public final class Neuron implements Serializable {

    private final int[] weights;

    public Neuron(int inputs) {
        this.weights = new int[inputs];

        sortWeights();
    }

    /* gerando cada peso com valores aleatorios de -1000 a 1000 */
    public void sortWeights() {
        Random r = new Random();
        for (int i = 0; i < weights.length; i++) {
            weights[i] = r.nextInt(2000) - 1000;
        }
    }

    /* sorteando pesos com base nos pesos informados */
    public void sortWeights(int[] weights, int variation) {
        if (weights.length != this.weights.length) {
            throw new IndexOutOfBoundsException("weights length need to be " + this.weights.length);
        }

        Random random = new Random();
        for (int i = 0; i < this.weights.length; i++) {
            int nr = random.nextInt((variation * 2) + 1) - variation;
            this.weights[i] = weights[i] + nr;
        }
    }

    /* multiplica cada input por cada peso depois soma tudo */
    public int getSignal(Integer... inputs) {
        if (inputs.length != weights.length) {
            throw new IndexOutOfBoundsException("inputs length need to be " + weights.length);
        }

        int output = 0;
        for (int i = 0; i < weights.length; i++) {
            output = output + (inputs[i] * weights[i]);
        }
        return (output < 0 ? 0 : output);
    }

    public int[] getWeights() {
        return weights;
    }

    /* função apenas para mostrar os pesos de uma rede */
    @Override
    public String toString() {
        return "Neuron{" + "weights=" + Arrays.toString(weights) + "}";
    }

}
