package me.cprox.practice.util;

public class Damage {

    private boolean sharpness, critical, burn;

    public Damage(boolean sharpness, boolean critical, boolean burn) {
        this.sharpness = sharpness;
        this.critical = critical;
        this.burn = burn;
    }

    public boolean isBurn() {
        return burn;
    }

    public boolean isCritical() {
        return critical;
    }

    public boolean isSharpness() {
        return sharpness;
    }

    public static Damage fromString(String str) {
        String[] s = str.split(":");
        try {
            return new Damage(Boolean.parseBoolean(s[0]),
                    Boolean.parseBoolean(s[1]), Boolean.parseBoolean(s[2]));
        } catch (Exception e) {
            return new Damage(false, false, false);
        }
    }

    @Override
    public String toString() {
        return sharpness + ":" + critical + ":" + burn;
    }
}
