package me.m1dnightninja.midnightcore.api.math;

public class Color {
    private static final Color[] mcLegacyColors = new Color[]{new Color(0, 0, 0), new Color(0, 0, 170), new Color(0, 170, 0), new Color(0, 170, 170), new Color(170, 0, 0), new Color(170, 0, 170), new Color(255, 170, 0), new Color(170, 170, 170), new Color(85, 85, 85), new Color(85, 85, 255), new Color(85, 255, 85), new Color(85, 255, 255), new Color(255, 85, 85), new Color(255, 85, 255), new Color(255, 255, 85), new Color(255, 255, 255)};
    private static final String[] mcLegacyNames = new String[] { "black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple", "gold", "gray", "dark_gray", "blue", "green", "aqua", "red", "light_purple", "yellow", "white" };

    private final int red;
    private final int green;
    private final int blue;
    private int closest4bitColor = -1;

    public Color(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public Color(int rgb) {
        this(Integer.toHexString(rgb));
    }

    public Color(String name) {
        int b;
        int g;
        int r;
        try {
            if (name.startsWith("#")) {
                name = name.substring(1);
            }
            if (name.length() != 6) {
                throw new IllegalStateException();
            }
            r = Integer.parseInt(name.substring(0, 2), 16);
            g = Integer.parseInt(name.substring(2, 4), 16);
            b = Integer.parseInt(name.substring(4, 6), 16);
        }
        catch (NumberFormatException ex) {
            r = 255;
            g = 255;
            b = 255;
        }
        this.red = r;
        this.green = g;
        this.blue = b;
    }

    public String toHex() {
        String r = Integer.toHexString(this.red);
        String g = Integer.toHexString(this.green);
        String b = Integer.toHexString(this.blue);
        if (r.length() == 1) {
            r = "0" + r;
        }
        if (g.length() == 1) {
            g = "0" + g;
        }
        if (b.length() == 1) {
            b = "0" + b;
        }
        return "#" + r + g + b;
    }

    public int toDecimal() {
        return Integer.parseInt(this.toHex().substring(1), 16);
    }

    public int toRGBI() {

        if (this.closest4bitColor == -1) {

            int out = 0;
            double lowest = getDistanceSquaredTo(mcLegacyColors[0]);

            for (int i = 1; i < mcLegacyColors.length; ++i) {

                double distance = getDistanceSquaredTo(mcLegacyColors[i]);
                if (!(distance < lowest)) continue;
                lowest = distance;
                out = i;
            }

            this.closest4bitColor = out;
        }

        return this.closest4bitColor;
    }

    public static Color fromRGBI(int value) {
        return mcLegacyColors[value];
    }

    public String toDyeColor() {
        switch (this.toRGBI()) {
            case 0: {
                return "black";
            }
            case 1: 
            case 9: {
                return "blue";
            }
            case 2: {
                return "green";
            }
            case 3: {
                return "cyan";
            }
            case 4: 
            case 12: {
                return "red";
            }
            case 5: {
                return "purple";
            }
            case 6: {
                return "orange";
            }
            case 7: {
                return "light_gray";
            }
            case 8: {
                return "gray";
            }
            case 10: {
                return "lime";
            }
            case 11: {
                return "light_blue";
            }
            case 13: {
                return "pink";
            }
            case 14: {
                return "yellow";
            }
            case 15: {
                return "white";
            }
        }
        return "white";
    }

    public static Color parse(String s) {

        for(int i = 0 ; i < mcLegacyNames.length ; i++) {
            if(mcLegacyNames[i].equals(s)) {
                return mcLegacyColors[i];
            }
        }

        return new Color(s);

    }

    @Override
    public String toString() {
        return toHex();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Color)) return false;

        Color c = (Color) obj;

        return c.red == red && c.green == green && c.blue == blue;
    }

    public double getDistanceTo(Color c) {
        return getDistance(this, c);
    }

    public double getDistanceSquaredTo(Color c) {
        return getDistanceSquared(this, c);
    }

    public static double getDistance(Color c1, Color c2) {
        return Math.sqrt(getDistanceSquared(c1, c2));
    }

    public static double getDistanceSquared(Color c1, Color c2) {
        int r = c2.red - c1.red;
        int g = c2.green - c1.green;
        int b = c2.blue - c1.blue;
        return r * r + g * g + b * b;
    }

    public Color multiply(double multiplier) {
        return new Color((int) (red * multiplier), (int) (green * multiplier), (int) (blue * multiplier));
    }

    public static Color WHITE = new Color(16777215);
}

