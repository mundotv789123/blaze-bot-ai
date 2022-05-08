package mundotv.blazebot.api.results;

import java.util.HashMap;
import javax.annotation.Nullable;

public class SocketMessage {

    private final String id;
    private HashMap<String, Object> payload;

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

    public void addPayload(String str, Object obj) {
        if (payload == null) {
            payload = new HashMap();
        }
        payload.put(str, obj);
    }
}
