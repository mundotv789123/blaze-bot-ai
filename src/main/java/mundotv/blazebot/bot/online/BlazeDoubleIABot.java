package mundotv.blazebot.bot.online;

import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;
import mundotv.blazebot.api.BlazeRestAPI;
import mundotv.blazebot.api.listenners.DoubleSocket;
import mundotv.blazebot.api.results.ColorResult;
import mundotv.blazebot.bot.BlazeBot;
import mundotv.blazebot.bot.ColorEnum;
import mundotv.blazebot.ia.BlazeIABot;

public class BlazeDoubleIABot extends DoubleSocket {

    private final BlazeIABot bot;
    private List<Integer> history;

    public BlazeDoubleIABot(BlazeIABot bot) throws URISyntaxException {
        super();
        this.bot = bot;
    }

    @Override
    public void handlerColor(ColorResult result) {
        int color = Math.round(result.getColor());
        history.add(color);
        if (history.size() < BlazeIABot.HISTORY_LIMIT) {
            return;
        }

        while (history.size() > BlazeIABot.HISTORY_LIMIT) {
            history.remove(0);
        }

        int gale = bot.getCgale();
        BlazeBot.Status status = bot.processBets(color);
        switch (status) {
            case NONE:
                if (bot.doBet(history)) {
                    System.out.println("Jogue na cor: " + ColorEnum.getColor(bot.getBet()) + " (proteção no branco)" + " R$ " + bot.getWallet());
                }
                break;
            case GALE:
                System.out.println("Faça G" + bot.getCgale() + " R$ " + bot.getWallet());
                break;
            case LOSS:
                System.out.println("Perdeu R$ " + bot.getWallet());
                break;
            case WIN:
                System.out.println("Ganhou G" + gale + (color == 0 ? " (BRANCO!)" : "") + " R$ " + bot.getWallet());
                break;
        }
    }

    @Override
    public void connect() {
        BlazeRestAPI api = new BlazeRestAPI();
        history = api.getLastHistory(BlazeIABot.HISTORY_LIMIT).stream().map((c) -> Math.round(c.getColor())).collect(Collectors.toList());
        super.connect();
    }
    
}
