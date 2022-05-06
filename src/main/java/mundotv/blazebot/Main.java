package mundotv.blazebot;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nullable;
import mundotv.blazebot.bot.IABot;
import mundotv.blazebot.ia.NeuralNetwork;
import mundotv.blazebot.ia.training.BotTrainer;
import mundotv.blazebot.ia.training.Trainer;
import mundotv.blazebot.bot.online.OnlineIABot;
import mundotv.blazebot.ia.training.BotStates;

public class Main {

    private static Connection conn = null;
    private IABot bot;

    public static void main(String[] args) throws Exception {
        new Main().start();
    }

    public void start() throws Exception {
        /* carregando ia */
        File f = new File("neuralnetwork.dat");
        if (f.exists()) {
            bot = new IABot(NeuralNetwork.inportFile(f));
        } else {
            bot = traneIA(100000, 12, 20);
            if (bot == null) {
                System.out.println("Nenhum bot encontrado!");
                return;
            }
            bot.getNeuralNetwork().exportFile(f);
            bot.reset();
        }

        /* iniciando bot online */
        OnlineIABot iabot = new OnlineIABot(bot);
        iabot.connect();
    }
    
    @Nullable
    private BotTrainer traneIA(int bots, int threads, int generations) throws SQLException {
        /* banco de dados */
        ResultSet rs = getConn().prepareStatement("SELECT COUNT(*) AS `total` FROM `history` WHERE DAY(`created_at`) = 6").executeQuery();
        if (rs.next()) {
            System.out.println("Treinando bots com [" + rs.getInt("total") + "] jogadas");
        }
        
        /* treinamento */
        Trainer trainer = new Trainer(bots);
        BotTrainer tbot = null;
        for (int i = 1; i <= generations; i++) {
            System.out.println("--------------[geração (" + i + ")]--------------");
            PreparedStatement ps = getConn().prepareStatement("SELECT `color_id` FROM `history` WHERE DAY(`created_at`) = 6");
            
            tbot = trainer.traneBots(threads, ps);
            if (tbot != null) {
                BotStates states = tbot.getStatus();
                System.out.println("=================[Informações do melhor bot da geração]=================");
                System.out.println("Cateira do melhor bot: " + tbot.getWallet());
                System.out.println("Apostas do melhor bot: " + states.getBets());
                System.out.println("Ganhou: " + states.getWin() + " apostas");
                System.out.println("Perdeu: " + states.getLoss() + " apostas");
                System.out.println("Red: " + states.getRed() + ", Black: " + states.getBlack() + ", White: " + states.getWhite());
                System.out.println("Ganhou R$: " + states.getWalletWin() + " Perdeu R$: " + states.getWalletLoss());
                System.out.println("Percentual da carteira: " + states.getWalletPercent() + "%");
                System.out.println("Percentual de acertos: " + states.getWinPercent() + "%");
                System.out.println("========================================================================");
            } else {
                i--;
            }
        }
        
        return tbot;
    }

    public static Connection getConn() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection("jdbc:mariadb://191.96.225.102/bombcrypto_million_bot?useSSL=false", "admin", "wIqc4ZYV8ESGk1xmwSYoV5J9j1Gay4FF");
        }
        return conn;
    }

}
