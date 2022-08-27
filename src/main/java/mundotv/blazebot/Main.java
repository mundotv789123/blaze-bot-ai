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

        File config = new File("./config.json");
        if (!config.exists()) {
            FileUtils.copyInputStreamToFile(Main.class.getResourceAsStream("/config.json"), config);
            System.out.println("File config.json created!");
            return;
        }

        Gson gson = new GsonBuilder().create();
        Main main = gson.fromJson(new FileReader(config), Main.class);

        if (main == null) {
            throw new NullPointerException("Config not found!");
        }

        if (command.hasOption("history")) {
            main.history();
            return;
        }

        if (command.hasOption("trane")) {
            main.trane();
            return;
        }

        main.start();
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

    public BotTrainerThreads trane() throws SQLException {
        /* preparando dados para o treinamento */
        List<Integer> datas = new ArrayList();
        List<ColorResult> colors = database.getAllHistory();
        for (ColorResult cr : colors) {
            datas.add(Math.round(cr.getColor()));
        }

        /* treinando */
        BotTrainerThreads bt = new BotTrainerThreads(5, 500000, 4, 8, 16);
        int g = 0;
        do {
            System.out.println("Geração: " + g);
            bt.trane(datas);
            if (bt.getBestBot() != null) {
                g++;
            }
        } while (bt.getBestBot() == null || bt.getBestBot().getWallet() < 50 || g <= 1);

        return bt;
    }

    public void start() throws SQLException, IOException, FileNotFoundException, ClassNotFoundException, URISyntaxException {
        File file = new File("./neural_network.dat");
        if (!file.exists()) {
            trane().getBestBot().getNetwork().exportFile(file);
        }
        BlazeDoubleIABot iabot = new BlazeDoubleIABot(new BlazeIABot(file, 50, 4, 8, 16));
        iabot.connect();
    }

}
