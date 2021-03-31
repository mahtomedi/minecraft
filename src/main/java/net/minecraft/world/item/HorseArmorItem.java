package net.minecraft.world.item;

import net.minecraft.resources.ResourceLocation;

public class HorseArmorItem extends Item {
    private static final String TEX_FOLDER = "textures/entity/horse/";
    private final int protection;
    private final String texture;

    public HorseArmorItem(int param0, String param1, Item.Properties param2) {
        super(param2);
        this.protection = param0;
        this.texture = "textures/entity/horse/armor/horse_armor_" + param1 + ".png";
    }

    public ResourceLocation getTexture() {
        return new ResourceLocation(this.texture);
    }

    public int getProtection() {
        return this.protection;
    }
}
