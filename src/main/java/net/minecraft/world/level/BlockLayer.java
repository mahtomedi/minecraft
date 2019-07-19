package net.minecraft.world.level;

public enum BlockLayer {
    SOLID("Solid"),
    CUTOUT_MIPPED("Mipped Cutout"),
    CUTOUT("Cutout"),
    TRANSLUCENT("Translucent");

    private final String name;

    private BlockLayer(String param0) {
        this.name = param0;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
