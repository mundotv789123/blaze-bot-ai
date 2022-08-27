package mundotv.blazebot.api.results;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class ColorResult {
    private String id, created_at, server_seed;
    private final Float color, roll;

    public ColorResult(String id, String created_at, String server_seed, Float color, Float roll) {
        this.id = id;
        this.created_at = created_at;
        this.server_seed = server_seed;
        this.color = color;
        this.roll = roll;
    }

    public ColorResult(Float color, Float roll) {
        this.color = color;
        this.roll = roll;
    }
    
}
