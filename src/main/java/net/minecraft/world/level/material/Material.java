package net.minecraft.world.level.material;

public final class Material {
    public static final Material PLANT = new Material.Builder(MaterialColor.PLANT).noCollider().notSolidBlocking().nonSolid().build();
    public static final Material SAND = new Material.Builder(MaterialColor.SAND).build();
    public static final Material WOOD = new Material.Builder(MaterialColor.WOOD).build();
    public static final Material STONE = new Material.Builder(MaterialColor.STONE).build();
    public static final Material GLASS = new Material.Builder(MaterialColor.NONE).notSolidBlocking().build();
    public static final Material DEPRECATED_REPLACEABLE = new Material.Builder(MaterialColor.NONE)
        .noCollider()
        .notSolidBlocking()
        .nonSolid()
        .replaceable()
        .build();
    public static final Material DEPRECATED_NONSOLID = new Material.Builder(MaterialColor.NONE).noCollider().notSolidBlocking().nonSolid().build();
    public static final Material DEPRECATED_NOCOLLIDER = new Material.Builder(MaterialColor.NONE).noCollider().build();
    public static final Material DEPRECATED_NOTSOLIDBLOCKING = new Material.Builder(MaterialColor.NONE).notSolidBlocking().build();
    public static final Material DEPRECATED_NOCOLLIDER_NONSOLIDBLOCKING = new Material.Builder(MaterialColor.NONE).noCollider().notSolidBlocking().build();
    public static final Material DEPRECATED_NOCOLLIDER_NONSOLID = new Material.Builder(MaterialColor.NONE).nonSolid().noCollider().build();
    public static final Material DEPRECATED = new Material.Builder(MaterialColor.NONE).build();
    private final MaterialColor color;
    private final boolean blocksMotion;
    private final boolean solidBlocking;
    private final boolean replaceable;
    private final boolean solid;

    public Material(MaterialColor param0, boolean param1, boolean param2, boolean param3, boolean param4) {
        this.color = param0;
        this.solid = param1;
        this.blocksMotion = param2;
        this.solidBlocking = param3;
        this.replaceable = param4;
    }

    public boolean isSolid() {
        return this.solid;
    }

    public boolean blocksMotion() {
        return this.blocksMotion;
    }

    public boolean isReplaceable() {
        return this.replaceable;
    }

    public boolean isSolidBlocking() {
        return this.solidBlocking;
    }

    public MaterialColor getColor() {
        return this.color;
    }

    public static class Builder {
        private boolean blocksMotion = true;
        private boolean replaceable;
        private boolean solid = true;
        private final MaterialColor color;
        private boolean solidBlocking = true;

        public Builder(MaterialColor param0) {
            this.color = param0;
        }

        public Material.Builder nonSolid() {
            this.solid = false;
            return this;
        }

        public Material.Builder noCollider() {
            this.blocksMotion = false;
            return this;
        }

        Material.Builder notSolidBlocking() {
            this.solidBlocking = false;
            return this;
        }

        public Material.Builder replaceable() {
            this.replaceable = true;
            return this;
        }

        public Material build() {
            return new Material(this.color, this.solid, this.blocksMotion, this.solidBlocking, this.replaceable);
        }
    }
}
