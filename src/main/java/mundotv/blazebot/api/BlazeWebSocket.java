package mundotv.blazebot.api;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.net.URI;
import mundotv.blazebot.api.results.SocketMessage;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public abstract class BlazeWebSocket extends WebSocketClient {

    private boolean reconnect = true;
    private boolean ping = true;

    private Thread pingThread = null;

    public BlazeWebSocket(URI uri) {
        super(uri);
    }

    public BlazeWebSocket(URI uri, boolean reconnect) {
        super(uri);
        this.reconnect = reconnect;
    }

    @Override
    public void onOpen(ServerHandshake sh) {
        System.out.println("connection is ready");
        if (!ping) {
            return;
        }
        pingThread = new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(3000);
                    send("2");
                }
            } catch (InterruptedException ex) {
            }
        });
        pingThread.start();
    }

    @Override
    public void onMessage(String string) {
        try {
            if (string == null) {
                return;
            }

            while (!string.equals("") && !string.startsWith(",")) {
                string = string.substring(1);
            }
            if (string.equals("") || string.length() < 3) {
                return;
            }
            string = string.substring(1);
            string = string.substring(0, string.length() - 1);

            Gson gson = new Gson();
            SocketMessage message = gson.fromJson(string, SocketMessage.class);
            handlerMessage(message);
        } catch (JsonSyntaxException ex) {
        }
    }

    @Override
    public void onClose(int i, String string, boolean bln) {
        System.out.println("connection is closed");
        if (pingThread != null) {
            pingThread.interrupt();
            pingThread = null;
        }
        if (reconnect) {
            reconnect();
        }
    }

    @Override
    public void reconnect() {
        try {
            Thread.sleep(3000);
            new Thread(() -> {
                super.reconnect();
            }).start();
        } catch (InterruptedException ex) {
            onError(ex);
        }
    }
    
    @Override
    public void onError(Exception ex) {
        ex.printStackTrace(); //log erro
    }

    public void sendMessage(int code, SocketMessage message) {
        Gson gson = new Gson();
        send(code + "[\"cmd\"," + gson.toJson(message, SocketMessage.class) + "]");
    }

    public void setPing(boolean ping) {
        this.ping = ping;
    }

    public abstract void handlerMessage(SocketMessage message);
}
