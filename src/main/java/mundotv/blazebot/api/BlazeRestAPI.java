package mundotv.blazebot.api;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import mundotv.blazebot.api.results.ColorResult;

public class BlazeRestAPI {

    public List<ColorResult> getLastHistory(int limit) {
        Gson gson = new Gson();
        String rest = makeRequest("/roulette_games/recent", "GET", null);
        if (rest == null) {
            return null;
        }
        ColorResult[] colors = gson.fromJson(rest, ColorResult[].class);

        List<ColorResult> colorsList = new ArrayList();

        for (int c = 0; c < limit; c++) {
            colorsList.add(colors[c]);
        }
        Collections.reverse(colorsList);
        
        return colorsList;
    }

    @Nullable
    private String makeRequest(String path, String method, @Nullable String data) {
        URL url;
        try {
            url = new URL("https://blaze.com/api" + path);
        } catch (MalformedURLException ex) {
            return null;
        }

        try {
            HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
            urlc.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:47.0) Gecko/20100101 Firefox/47.0");

            urlc.setRequestMethod(method);
            urlc.setConnectTimeout(5000);

            urlc.connect();
            String res = "";
            BufferedReader br = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                res += line;
            }

            return res;
        } catch (IOException ex) {
        }
        return null;
    }

}
