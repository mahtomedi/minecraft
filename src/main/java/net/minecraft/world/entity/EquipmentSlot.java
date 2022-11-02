package net.minecraft.world.entity;

public enum EquipmentSlot {
    MAINHAND(EquipmentSlot.Type.HAND, 0, 0, "mainhand"),
    OFFHAND(EquipmentSlot.Type.HAND, 1, 5, "offhand"),
    FEET(EquipmentSlot.Type.ARMOR, 0, 1, "feet"),
    LEGS(EquipmentSlot.Type.ARMOR, 1, 2, "legs"),
    CHEST(EquipmentSlot.Type.ARMOR, 2, 3, "chest"),
    HEAD(EquipmentSlot.Type.ARMOR, 3, 4, "head");

    private final EquipmentSlot.Type type;
    private final int index;
    private final int filterFlag;
    private final String name;

    private EquipmentSlot(EquipmentSlot.Type param0, int param1, int param2, String param3) {
        this.type = param0;
        this.index = param1;
        this.filterFlag = param2;
        this.name = param3;
    }

    public EquipmentSlot.Type getType() {
        return this.type;
    }

    public int getIndex() {
        return this.index;
    }

    public int getIndex(int param0) {
        return param0 + this.index;
    }

    public int getFilterFlag() {
        return this.filterFlag;
    }

    public String getName() {
        return this.name;
    }

    public boolean isArmor() {
        return this.type == EquipmentSlot.Type.ARMOR;
    }

    public static EquipmentSlot byName(String param0) {
        for(EquipmentSlot var0 : values()) {
            if (var0.getName().equals(param0)) {
                return var0;
            }
        }

        throw new IllegalArgumentException("Invalid slot '" + param0 + "'");
    }

    public static EquipmentSlot byTypeAndIndex(EquipmentSlot.Type param0, int param1) {
        for(EquipmentSlot var0 : values()) {
            if (var0.getType() == param0 && var0.getIndex() == param1) {
                return var0;
            }
        }

        throw new IllegalArgumentException("Invalid slot '" + param0 + "': " + param1);
    }

    public static enum Type {
        HAND,
        ARMOR;
    }
}
