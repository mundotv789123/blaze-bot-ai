package mundotv.blazebot.bot.online;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import mundotv.blazebot.api.BlazeRestAPI;
import mundotv.blazebot.api.results.ColorResult;
import mundotv.blazebot.bot.IABot;
import mundotv.blazebot.api.listenners.DoubleSocket;
import mundotv.blazebot.bot.BotListeners;
import org.java_websocket.handshake.ServerHandshake;

public class OnlineIABot extends DoubleSocket implements BotListeners {

    private final IABot bot;
    private final List<Integer> history = new ArrayList();
    private final BlazeRestAPI api = new BlazeRestAPI();

    public OnlineIABot(IABot bot) throws URISyntaxException {
        super();
        this.bot = bot;
        bot.setListener((BotListeners)this);
    }

    @Override
    public void onOpen(ServerHandshake sh) {
        super.onOpen(sh);
        loadLastHistory();
    }

    private void loadLastHistory() {
        history.clear();
        ColorResult[] lastHistory = api.getLastHistory();
        for (int i = IABot.getHistorySize() - 1; (i >= 0); i--) {
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

        bot.processBets(color);
        loadBets(history);
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
            bot.doBet(3, false);
        }

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

    @Override
    public void onGale(int color, int gale) {
        System.out.println("Perdeu! faça G" + gale + " R$: " + bot.getValue() + " na cor: " + getColorName(color));
        System.out.println("R$: "+bot.getWallet());
    }

    @Override
    public void onWin(int color, int gale) {
        System.out.println("Ganhou G" + gale);
        System.out.println("R$: "+bot.getWallet());
    }

    @Override
    public void onLoss(int color, int gale) {
        System.out.println("Perdeu G" + gale);
        System.out.println("R$: "+bot.getWallet());
    }

    @Override
    public void onBet(int color, boolean gale) {
        System.out.println("Jogue R$: " + (color == 3 ? 2.0 : bot.getValue()) + " na cor: " + getColorName(color));
        if (gale) {
            System.out.println("obs: prepare um possível gale");
        }
        System.out.println("R$: "+bot.getWallet());
    }

}
