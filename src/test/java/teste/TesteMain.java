package teste;

import java.net.URISyntaxException;
import java.sql.SQLException;
import mundotv.blazebot.api.listenners.DoubleSocket;

public class TesteMain {

    public static void main(String[] args) throws SQLException, URISyntaxException, InterruptedException {
        /*BlazeRestAPI api = new BlazeRestAPI();

        List<Integer> history = new ArrayList();
        ColorResult[] lastHistory = api.getLastHistory();

        for (int i = 10; (i >= 0); i--) {
            System.out.println(lastHistory[i].getColor());
            history.add(lastHistory[i].getColor());
        }

        System.out.println(history.size());*/
        new DoubleSocket() {
            @Override
            public void hanlderColor(int color) {
                System.out.println("A cor que caiu foi a " + color);
            }
        }.connect();
    }
}
