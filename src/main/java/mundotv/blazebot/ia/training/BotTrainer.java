package mundotv.blazebot.ia.training;

import java.util.List;
import mundotv.blazebot.bot.IABot;

/* Bot de treinamento para I.A ficar esperta! */
public class BotTrainer extends IABot {

    /* estatisticas */
    private final BotStates status = new BotStates();

    public BotTrainer() {
        super();
    }

    /* pegando ações da rede neural e processando */
    public void trane(List<Integer> history, int color) {
        color = (color == 0 ? 3 : color);
        
        if (history.size() < BotTrainer.getHistorySize()) {
            return;
        }
        if (isBroken()) {
            return;
        }

        loadBets(history);
        
        checkBets(color);
    }

    public void loadBets(List<Integer> history) {
        Integer[] action = this.getActions(history);
        
        boolean g = action[3] > 0;
        
        if (action[1] > 0) {
            status.addBet(1, getValue());
            doBet(1, g);
        }
        if (action[2] > 0) {
            status.addBet(2, getValue());
            doBet(2, g);
        }

        if (action[0] > 0) {
            status.addBet(3, 2);
            doBet(3, g);
        }
    }
    
    public void checkBets(int color) {
        if (getGaleColor() > 0) {
            getBets().clear();
            getBets().add(getGaleColor());
        }
        
        for(int bc:getBets()) {
            if (processBet(bc, color)) {
                if (color == bc) {
                    status.addWin(getValue() * (color == 3 ? 14 : 2));
                } else {
                    status.addWalletLoss(getValue());
                }
            } else {
                status.addLoss();
            }
        }
        getBets().clear();
    }
    
    /* resetando status */
    @Override
    public void reset() {
        super.reset();
        status.reset();
    }

    public BotStates getStatus() {
        return status;
    }
    
}
