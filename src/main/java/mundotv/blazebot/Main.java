package mundotv.blazebot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mundotv.blazebot.api.results.ColorResult;
import mundotv.blazebot.bot.online.BlazeDoubleIABot;
import mundotv.blazebot.database.Database;
import mundotv.blazebot.bot.online.HistoryCollector;
import mundotv.blazebot.ia.BlazeIABot;
import mundotv.blazebot.ia.BotTrainerThreads;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;

public class Main {

    private Database database;

    public static void main(String[] args) throws Exception {
        CommandLine command = loadOptions(args);

        /* gerando arquivo de configuração */
        File config = new File("./config.json");
        if (!config.exists()) {
            FileUtils.copyInputStreamToFile(Main.class.getResourceAsStream("/config.json"), config);
            System.out.println("File config.json created!");
            return;
        }

        /* carregando configurações */
        Gson gson = new GsonBuilder().create();
        Main main = gson.fromJson(new FileReader(config), Main.class);
        if (main == null) {
            throw new NullPointerException("Config not found!");
        }

        if (command.hasOption("history")) {
            main.history();
            return;
        }

        File file = new File("./neural_network.dat");
        if (command.hasOption("trane")) {
            main.trane(file);
            return;
        }

        main.start(file);
    }

    public static CommandLine loadOptions(String[] args) {
        Options options = new Options();

        options.addOption(new Option("h", "help", false, "Print help options"));
        options.addOption(new Option("history", "history", false, "Collect data history"));
        options.addOption(new Option("trane", "trane", false, "Collect data history"));

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            CommandLine command = parser.parse(options, args);
            if (!command.hasOption("help")) {
                return command;
            }
        } catch (ParseException e) {
            System.out.println(e.getMessage());
        }

        formatter.printHelp("bz_bot", options);
        System.exit(0);
        return null;
    }

    public void history() {
        try {
            HistoryCollector history = new HistoryCollector(database);
            history.connect();
        } catch (URISyntaxException | SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public BotTrainerThreads trane(File file) throws SQLException, IOException {
        /* preparando dados para o treinamento */
        List<Integer> datas = new ArrayList();
        List<ColorResult> colors = database.getAllHistory(1550);
        for (int c = (colors.size() - 1); c >= 0; c--) {
            datas.add(Math.round(colors.get(c).getColor()));
        }
        
        /* dados para colocar na class main depois */
        float[] gales = {4, 8, 16}; //modo de jogo com gales
        int threads = 10, bots_count = 2000000; //dados para o treinamento

        /* treinando */
        BotTrainerThreads bt = new BotTrainerThreads(threads, bots_count, gales);
        int g = 0;
        do {
            System.out.println((g == 0 ? "Processando..." : ("Geração: " + g)));
            bt.trane(datas);
            if (bt.getBestBot() != null) {
                g++;
                System.out.println("Melhor da geração: R$ "+bt.getBestBot().getWallet());
            }
        } while (bt.getBestBot() == null || bt.getBestBot().getWallet() < 50 || g <= 10);

        /* exportando arquivo i.a */
        if (file != null) {
            bt.getBestBot().getNetwork().exportFile(file);
        }

        return bt;
    }

    public void start(File file) throws SQLException, IOException {
        if (!file.exists()) {
            trane(file).getBestBot();
        }
        try {
            BlazeDoubleIABot iabot = new BlazeDoubleIABot(new BlazeIABot(file, 50, 4, 8, 16));
            iabot.connect();
        } catch (FileNotFoundException | ClassNotFoundException | URISyntaxException e) {
            throw new IOException(e.getMessage());
        }
    }

}
