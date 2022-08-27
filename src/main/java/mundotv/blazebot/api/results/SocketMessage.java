package mundotv.blazebot.api.results;

import javax.annotation.Nullable;
import lombok.ToString;

@ToString
public class SocketMessage<T> {

    private final String id;
    private final T payload;

    public SocketMessage(String id, T payload) {
        this.id = id;
        this.payload = payload;
    }

    @Nullable
    public String getId() {
        return id;
    }

    public T getPayload() {
        return payload;
    }
   
}
