package mundotv.blazebot.api.results;

public class ColorResult {

    private final String id, created_at, server_seed;
    private final int color, roll;

    public ColorResult(String id, String created_at, String server_seed, int color, int roll) {
        this.id = id;
        this.created_at = created_at;
        this.server_seed = server_seed;
        this.color = color;
        this.roll = roll;
    }

    public int getColor() {
        return color;
    }

    public String getCreatedAt() {
        return created_at;
    }

    public String getId() {
        return id;
    }

    public int getRoll() {
        return roll;
    }

    public String getServerSeed() {
        return server_seed;
    }

    @Override
    public String toString() {
        return "ColorResult{" + "id=" + id + ", created_at=" + created_at + ", server_seed=" + server_seed + ", color=" + color + ", roll=" + roll + '}';
    }
}
