package mundotv.blazebot.api.listenners;

import java.net.URI;
import java.net.URISyntaxException;
import mundotv.blazebot.api.BlazeWebSocket;
import mundotv.blazebot.api.results.SocketMessage;
import org.java_websocket.handshake.ServerHandshake;

public abstract class DoubleSocket extends BlazeWebSocket {

    private String lastUUID = "";

    public DoubleSocket() throws URISyntaxException {
        super(new URI("wss://api-v2.blaze.com/replication/?EIO=3&transport=websocket"));
    }

    @Override
    public void onOpen(ServerHandshake sh) {
        super.onOpen(sh);
        SocketMessage message = new SocketMessage("subscribe");
        message.addPayload("room", "double_v2");
        sendMessage(420, message);
    }

    @Override
    public void handlerMessage(SocketMessage message) {
        Object uuid = message.getPayload().get("id");
        Object color = message.getPayload().get("color");

        if (uuid == null || color == null) {
            return;
        }

        if (lastUUID.equals((String) uuid)) {
            return;
        }

        lastUUID = (String) uuid;
        hanlderColor((int) (double) color);
    }

    public abstract void hanlderColor(int color);
}
