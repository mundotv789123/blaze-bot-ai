package mundotv.blazebot.api.listenners;

import com.google.gson.reflect.TypeToken;
import java.net.URI;
import java.net.URISyntaxException;
import mundotv.blazebot.api.BlazeWebSocket;
import mundotv.blazebot.api.results.ColorResult;
import mundotv.blazebot.api.results.SocketMessage;
import org.java_websocket.handshake.ServerHandshake;

public abstract class DoubleSocket extends BlazeWebSocket<ColorResult> {

    private String lastUUID = "";

    public DoubleSocket() throws URISyntaxException {
        super(new URI("wss://api-v2.blaze.com/replication/?EIO=3&transport=websocket"), new TypeToken<SocketMessage<ColorResult>>() {}.getType());
    }

    @Override
    public void onOpen(ServerHandshake sh) {
        super.onOpen(sh);
        sendMessage(420, new SocketMessage("subscribe", new Room("double_v2")));
    }

    @Override
    public void handlerMessage(SocketMessage<ColorResult> message) {
        ColorResult color = message.getPayload();
        if (color == null || color.getColor() == null) {
            return;
        }
        if (!lastUUID.equals(color.getId())) {
            lastUUID = color.getId();
            handlerColor(color);
        }
    }

    public abstract void handlerColor(ColorResult color);
}
