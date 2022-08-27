package mundotv.blazebot.bot.online;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import mundotv.blazebot.api.BlazeRestAPI;
import mundotv.blazebot.api.listenners.DoubleSocket;
import mundotv.blazebot.api.results.ColorResult;
import mundotv.blazebot.bot.BlazeBot;
import mundotv.blazebot.bot.ColorEnum;
import mundotv.blazebot.ia.BlazeIABot;

public class BlazeDoubleIABot extends DoubleSocket {

    private final BlazeIABot bot;
    private final List<Integer> history = new ArrayList();

    public BlazeDoubleIABot(BlazeIABot bot) throws URISyntaxException {
        super();
        this.bot = bot;
    }

    @Override
    public void handlerColor(ColorResult result) {
        int color = Math.round(result.getColor());
        history.add(color);
        if (history.size() < 20) {
            return;
        }

        while (history.size() > 20) {
            history.remove(0);
        }

        BlazeBot.Status status = bot.processBets(color);
        int gale = bot.getCgale();
        switch (status) {
            case NONE:
                if (bot.doBet(history)) {
                    System.out.println("Jogue na cor: " + ColorEnum.getColor(bot.getBet()) + " (proteção no branco)");
                } else {
                    return;
                }
                break;
            case GALE:
                System.out.println("Faça G" + bot.getCgale() + " (proteção no branco)");
                break;
            case LOSS:
                System.out.println("Perdeu");
                break;
            case WIN:
                System.out.println("Ganhou G" + gale + (color == 0 ? "(BRANCO!)" : ""));
                break;
        }
        System.out.println("R$ " + bot.getWallet());
    }

    @Override
    public void connect() {
        BlazeRestAPI api = new BlazeRestAPI();
        ColorResult[] colors = api.getLastHistory();
        for (int c = (colors.length - 1); c >= 0; c--) {
            history.add(Math.round(colors[c].getColor()));
        }
        super.connect();
    }

}
