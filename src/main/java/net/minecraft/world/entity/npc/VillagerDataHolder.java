package net.minecraft.world.entity.npc;

import net.minecraft.world.entity.VariantHolder;

public interface VillagerDataHolder extends VariantHolder<VillagerType> {
    VillagerData getVillagerData();

    void setVillagerData(VillagerData var1);

    default VillagerType getVariant() {
        return this.getVillagerData().getType();
    }

    default void setVariant(VillagerType param0) {
        this.setVillagerData(this.getVillagerData().setType(param0));
    }
}
