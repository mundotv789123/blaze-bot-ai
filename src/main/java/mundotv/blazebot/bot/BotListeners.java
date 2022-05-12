package mundotv.blazebot.bot;

public abstract interface BotListeners {
    public abstract void onGale(int color, int gale);
    public abstract void onWin(int color, int gale);
    public abstract void onLoss(int color, int gale);
    public abstract void onBet(int color, boolean gale);
}
