package mundotv.blazebot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import mundotv.blazebot.bot.IABot;
import mundotv.blazebot.ia.NeuralNetwork;
import mundotv.blazebot.ia.training.BotTrainer;
import mundotv.blazebot.ia.training.Trainer;
import mundotv.blazebot.bot.online.OnlineIABot;
import mundotv.blazebot.ia.NetworkTrainer;
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
            int threads = Runtime.getRuntime().availableProcessors() - 2;
            System.out.println("Executando treinamento em " + threads + " threads");
            bot = traneIA(25000, threads, 25);
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
    private BotTrainer traneIA(int bots, int threads, int generations) throws SQLException, IOException, FileNotFoundException, ClassNotFoundException {
        /* banco de dados */
        int day = 7;
        PreparedStatement ps = getConn().prepareStatement("SELECT `color_id` FROM `history` WHERE DAY(`created_at`) = ?");
        ps.setInt(1, day);
        ResultSet rs = ps.executeQuery();

        /* pegando histórico */
        List<Integer> history = new ArrayList();
        while (rs.next()) {
            history.add(rs.getInt("color_id"));
        }

        System.out.println("Treinando bots com [" + history.size() + "] jogadas...");
        
        /* criando ia base */
        NetworkTrainer nett = new NetworkTrainer(10000, 5, 5, 8, 4);
        
        //padrão zadrez
        nett.getDatas().put(getArray(1, 2, 1, 2, 1), getArray(0, 0, 1, 1));
        nett.getDatas().put(getArray(2, 1, 2, 1, 2), getArray(0, 1, 0, 1));
        
        //padrão dois em dois
        nett.getDatas().put(getArray(1, 2, 2, 1, 2), getArray(0, 0, 1, 1));
        nett.getDatas().put(getArray(2, 1, 1, 2, 1), getArray(0, 1, 0, 1));
        
        //padrão sequencia
        nett.getDatas().put(getArray(1, 1, 1, 1, 1), getArray(0, 1, 0, 1));
        nett.getDatas().put(getArray(2, 2, 2, 2, 2), getArray(0, 0, 1, 1));
        
        nett.traineAll();
        BotTrainer btt = new BotTrainer(nett.getBestNetwork());
        
        /* treinamento */
        Trainer trainer = new Trainer(bots);
        trainer.setBestBot(btt);
        
        /* melhorando habilidade da ia */
        BotTrainer tbot = null;
        for (int i = 1; i <= generations; i++) {
            tbot = trainer.traneBots(threads, history);
            System.out.println("--------------[geração (" + i + ")]--------------");
            if (tbot != null) {
                BotStates states = tbot.getStatus();
                System.out.println("=================[Informações do melhor bot da geração]=================");
                System.out.println("Cateira do melhor bot: " + tbot.getWallet());
                System.out.println("Apostas do melhor bot: " + states.getBets());
                System.out.println("Ganhou: " + states.getWin() + " apostas");
                System.out.println("Perdeu: " + states.getLoss() + " apostas");
                System.out.println("Red: " + states.getRed() + ", Black: " + states.getBlack() + ", White: " + states.getWhite());
                System.out.println("Variação de cores: " + states.getColorPercent() + "%");
                System.out.println("Ganhou R$: " + states.getWalletWin() + " Perdeu R$: " + states.getWalletLoss());
                System.out.println("Percentual da carteira: " + states.getWalletPercent() + "%");
                System.out.println("Percentual de acertos: " + states.getWinPercent() + "%");
                System.out.println("========================================================================");
            } else {
                i--;
            }
            System.out.println("---------------[Informações]---------------");
            System.out.println("Bots quebrados: " + trainer.getBrokens());
            System.out.println("Bots poucas apostas " + trainer.getLowBets());
            System.out.println("Bots carteira baixa " + trainer.getLowWallets());
            System.out.println("-------------------------------------------");
            trainer.resetStatus();
        }
        return tbot;
    }

    public static Connection getConn() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection("jdbc:mariadb://191.96.225.102/bombcrypto_million_bot?useSSL=false", "admin", "wIqc4ZYV8ESGk1xmwSYoV5J9j1Gay4FF");
        }
        return conn;
    }
    
    public Integer[] getArray(Integer... array) {
        return array;
    }

}
