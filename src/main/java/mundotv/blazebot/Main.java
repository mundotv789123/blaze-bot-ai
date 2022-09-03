package mundotv.blazebot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import lombok.ToString;
import mundotv.blazebot.bot.online.BlazeDoubleIABot;
import mundotv.blazebot.database.Database;
import mundotv.blazebot.bot.online.HistoryCollector;
import mundotv.blazebot.ia.BlazeIABot;
import mundotv.blazebot.ia.BotTrainer;
import mundotv.blazebot.ia.BotTrainerThreads;
import mundotv.ia.NeuralNetwork;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;

@ToString
public class Main {

    private final Database database;
    private final float[] gales;
    private final float wallet, white;
    private final int threads, bots_count, bets_to_traine, rand_offset, max_generations;

    public Main(Database database, float[] gales, float wallet, float white, int threads, int bots_count, int bets_to_traine, int rand_offset, int max_generations) {
        this.database = database;
        this.gales = gales;
        this.wallet = wallet;
        this.white = white;
        this.threads = threads;
        this.bots_count = bots_count;
        this.bets_to_traine = bets_to_traine;
        this.rand_offset = rand_offset;
        this.max_generations = max_generations;
    }

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
        if (command.hasOption("traine")) {
            main.traine(file);
            return;
        }

        main.start(file);
    }

    public static CommandLine loadOptions(String[] args) {
        Options options = new Options();

        options.addOption(new Option("h", "help", false, "Print help options"));
        options.addOption(new Option("c", "history", false, "Collect data history"));
        options.addOption(new Option("t", "traine", false, "Traine or re-traine networks"));

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

    public BotTrainerThreads traine(File file) throws SQLException, IOException, FileNotFoundException, ClassNotFoundException {
        /* verificando arquivo de geração */
        File genFile = new File("generation_network.dat");
        BotTrainerThreads bt;
        if (genFile.exists()) {
            System.out.println(genFile.getName() + " found! loading that...");
            bt = new BotTrainerThreads(threads, bots_count, NeuralNetwork.importFile(genFile), wallet, white, gales);
        } else {
            bt = new BotTrainerThreads(threads, bots_count, wallet, white, gales);
        }

        /* treinando */
        int g = 0;
        Thread display = new Thread(() -> {
            while (true) {
                String bestWallet = bt.getBestBot() != null ? "R$ " + (Math.round(bt.getBestBot().getWallet() * 100f) / 100f) + " " : "";
                int percent = 0;
                for (BotTrainer bot : bt.getBots()) {
                    percent += bot.getPercent();
                }
                percent = percent * 100 / (bt.getBots().size() * 100);
                System.out.print("\033[1K\r" + bestWallet + getProgress(percent, 50) + " " + percent + "%");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    break;
                }
            }
        });

        display.start();

        do {
            System.out.println("Training networks" + (g == 0 ? "..." : (" (Generation: " + g + ")...")));
            /* preparando dados para o treinamento */
            Random r = new Random();
            List<Integer> colors = database.getAllHistory(bets_to_traine, (rand_offset == 0 ? 0 : r.nextInt(rand_offset))).stream().map((c) -> Math.round(c.getColor())).collect(Collectors.toList());
            bt.traine(colors);
            if (bt.getBestBot() != null) {
                g++;
                bt.getBestBot().getNetwork().exportFile(new File("generation_network.dat"));
                System.out.println("\033[1K\rBest generation: R$ " + bt.getBestBot().getWallet() + " win: " + bt.getBestBot().getPercentWins() + "%");
            }
        } while (bt.getBestBot() == null || g <= max_generations);

        display.interrupt();
        System.out.println("\033[1K\r");

        /* exportando arquivo i.a */
        if (file != null) {
            bt.getBestBot().getNetwork().exportFile(file);
        }

        return bt;
    }

    public void start(File file) throws SQLException, IOException, FileNotFoundException, ClassNotFoundException {
        System.out.println("Start wallet: " + wallet);
        System.out.println("Gales: " + Arrays.toString(gales));
        System.out.println("White protection: " + white);
        if (!file.exists()) {
            System.out.println(file.getName() + " not found, traning...");
            traine(file).getBestBot();
        }
        try {
            BlazeDoubleIABot iabot = new BlazeDoubleIABot(new BlazeIABot(NeuralNetwork.importFile(file), wallet, white, gales));
            iabot.connect();
        } catch (FileNotFoundException | ClassNotFoundException | URISyntaxException e) {
            throw new IOException(e.getMessage());
        }
    }

    public String getProgress(int percent, int size) {
        int count = percent * size / 100;
        String progress = "";
        for (int i = 0; i < size; i++) {
            progress += (i >= count ? "=" : "#");
        }
        return progress;
    }

}
