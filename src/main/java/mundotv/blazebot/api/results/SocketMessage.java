package mundotv.blazebot.api.results;

import java.util.HashMap;
import javax.annotation.Nullable;

public class SocketMessage {

    private final String id;
    private final HashMap<String, Object> payload;

    public SocketMessage(String id) {
        this.id = id;
        this.payload = new HashMap();
    }

    @Nullable
    public String getId() {
        return id;
    }

    @Nullable
    public HashMap<String, Object> getPayload() {
        return payload;
    }
}
