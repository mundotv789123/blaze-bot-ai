package mundotv.blazebot.ia.training;

public final class BotStates {

    private int win, loss, bets;
    private int red, black, white;
    private double walletLoss, walletWin;

    public BotStates() {
        reset();
    }
    
    /* percent getters */
    public int getWinPercent() {
        if (loss == 0 || loss > win) {
            return 0;
        }
        int sum = loss + win;
        return ((win * 100) / sum);
    }

    public int getWalletPercent() {
        if (walletWin == 0 || walletWin < walletLoss) {
            return 0;
        }
        double sun = walletWin + walletLoss;
        return Math.round((float)((walletWin * 100) / sun));
    }
    
    public int getColorPercent() {
        if (red == 0 && black == 0) {
            return 0;
        }
        int sum = red + black;
        
        if (red > black) {
            return ((red * 100) / sum);
        } else {
            return ((black * 100) / sum);
        }
    }

    /* getters */
    public int getWin() {
        return win;
    }

    public int getLoss() {
        return loss;
    }

    public int getBets() {
        return bets;
    }

    public int getBlack() {
        return black;
    }

    public int getWhite() {
        return white;
    }

    public int getRed() {
        return red;
    }

    public double getWalletWin() {
        return walletWin;
    }

    public double getWalletLoss() {
        return walletLoss;
    }
    
    /* setters */
    public void addWin() {
        win++;
    }
    
    public void addWalletWin(double value) {
        walletWin += value;
    }
    
    public void addWin(double value) {
        addWin();
        addWalletWin(value);
    }
    
    public void addLoss() {
        loss++;
    }
    
    public void addWalletLoss(double value) {
        walletLoss += value;
    }
    
    public void addLoss(double value) {
        addLoss();
        addWalletLoss(value);
    }
    
    
    public void addBet() {
        bets++;
    }
    
    public void addBet(int color, double value) {
        addBet();
        addColor(color);
        addWalletLoss(value);
    }
    
    public void addColor(int color) {
        switch (color) {
            case 3:
                white++;
                return;
            case 2:
                black++;
                return;
            case 1:
                red++;
        }
    }
    
    public void reset() {
        red = 0;
        black = 0;
        white = 0;
        
        win = 0;
        loss = 0;
        bets = 0;
        
        walletWin = 0;
        walletLoss = 0;
    }
}
