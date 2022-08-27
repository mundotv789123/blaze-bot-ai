package mundotv.blazebot.api;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import mundotv.blazebot.api.results.SocketMessage;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public abstract class BlazeWebSocket<T> extends WebSocketClient {

    private boolean reconnect = true;
    private boolean ping = true;

    private Thread pingThread = null;
    protected final Type type;

    public BlazeWebSocket(URI uri, Type type) {
        super(uri);
        this.type = type;
    }

    public BlazeWebSocket(URI uri, boolean reconnect, Type type) {
        super(uri);
        this.reconnect = reconnect;
        this.type = type;
    }

    @Override
    public void onOpen(ServerHandshake sh) {
        System.out.println("connection is ready");
        if (!ping) {
            return;
        }
        pingThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(3000);
                    send("2");
                } catch (InterruptedException ex) {
                }
            }
        });
        pingThread.start();
    }

    @Override
    public void onMessage(String msg) {
        if (msg == null) {
            return;
        }

        Pattern p = Pattern.compile("^([\\d]{0,4}\\[\\\"[\\w]+\\\",)(.+)(\\])$");
        Matcher m = p.matcher(msg);

        if (!m.matches()) {
            return;
        }
        
        try {
            SocketMessage<T> message = new Gson().fromJson(m.group(2), type);
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

    public abstract void handlerMessage(SocketMessage<T> message);
    
    public static class Room {
        @Getter
        private final String room;
        public Room(String room) {
            this.room = room;
        }
    }
}
