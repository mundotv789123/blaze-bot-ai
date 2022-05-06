package mundotv.blazebot.bot.online;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import mundotv.blazebot.api.BlazeRestAPI;
import mundotv.blazebot.api.results.ColorResult;
import mundotv.blazebot.bot.IABot;
import mundotv.blazebot.api.listenners.DoubleSocket;
import org.java_websocket.handshake.ServerHandshake;

public class OnlineIABot extends DoubleSocket {

    private final IABot bot;
    private final List<Integer> history = new ArrayList();
    private final BlazeRestAPI api = new BlazeRestAPI();

    public OnlineIABot(IABot bot) throws URISyntaxException {
        super();
        this.bot = bot;
    }

    @Override
    public void onOpen(ServerHandshake sh) {
        super.onOpen(sh);
        loadLastHistory();
    }

    private void loadLastHistory() {
        history.clear();
        ColorResult[] lastHistory = api.getLastHistory();
        for (int i = IABot.getHistorySize(); (i >= 0); i--) {
            int color = lastHistory[i].getColor();
            history.add(color == 0 ? 3 : color);
        }
    }

    @Override
    public void hanlderColor(int color) {
        color = color == 0 ? 3 : color;
        /* preparando as informações */
        history.add(color);
        if (history.size() > IABot.getHistorySize()) {
            history.remove(0);
        }

        if (bot.isBroken()) {
            System.out.println("Sem banca!");
            System.exit(0);
        }

        System.out.println("Caiu na cor: " + getColorName(color));

        /* verificando a aposta */
        checkBets(color);

        /* pegando próxima jogada */
        loadBets(history);
        for (int c : bot.getBets()) {
            System.out.println("Jogue R$: " + bot.getValue() + " na cor: " + getColorName(c) + " R$: " + bot.getWallet());
        }

    }

    public void loadBets(List<Integer> history) {
        Integer[] action = bot.getActions(history);
        
        boolean g = action[3] > 0;
        
        if (action[1] > 0) {
            bot.doBet(1, g);
        }
        if (action[2] > 0) {
            bot.doBet(2, g);
        }

        if (action[0] > 0) {
            bot.doBet(3, g);
        }

    }

    public void checkBets(int color) {
        if (bot.getGaleColor() > 0) {
            bot.getBets().clear();
            bot.getBets().add(bot.getGaleColor());
        }

        for (int bc : bot.getBets()) {
            if (bot.processBet(bc, color)) {
                if (color == bc) {
                    System.out.println("Ganhou na cor " + getColorName(color) + " R$: " + bot.getWallet());
                } else {
                    System.out.println("Perdeu faça gale de R$: " + bot.getValue() + " na cor " + getColorName(bot.getGaleColor()) + " R$: " + bot.getWallet());
                }
            } else {
                System.out.println("Perdeu! R$: " + bot.getWallet());
            }
        }
        bot.getBets().clear();
    }

    public String getColorName(int color) {
        switch (color) {
            case 1:
                return "Vermelho";
            case 2:
                return "Preto";
            case 3:
                return "Branco";
            default:
                return "Error!";
        }
    }

}
