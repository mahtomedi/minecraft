package net.minecraft.world.level.material;

public final class Material {
    public static final Material PLANT = new Material(MaterialColor.PLANT, false);
    public static final Material DEPRECATED_NONSOLID = new Material(MaterialColor.NONE, false);
    public static final Material DEPRECATED = new Material(MaterialColor.NONE, true);
    private final MaterialColor color;
    private final boolean solidBlocking;

    public Material(MaterialColor param0, boolean param1) {
        this.color = param0;
        this.solidBlocking = param1;
    }

    public boolean isSolidBlocking() {
        return this.solidBlocking;
    }

    public MaterialColor getColor() {
        return this.color;
    }
}
