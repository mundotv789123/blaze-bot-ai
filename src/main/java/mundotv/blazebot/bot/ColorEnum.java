package mundotv.blazebot.bot;

import lombok.Getter;

public enum ColorEnum {
    WHITE(0), RED(1), BLACK(2);

    @Getter
    private final int color;

    private ColorEnum(int color) {
        this.color = color;
    }

    public boolean equals(int color) {
        return this.getColor() == color;
    }

    public boolean equals(ColorEnum color) {
        return color.getColor() == this.getColor();
    }

    public static ColorEnum getColor(int color) {
        switch (color) {
            case 0:
                return ColorEnum.WHITE;
            case 1:
                return ColorEnum.RED;
            case 2:
                return ColorEnum.BLACK;
        }
        return null;
    }
}
