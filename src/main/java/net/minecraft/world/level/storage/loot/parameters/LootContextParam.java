package net.minecraft.world.level.storage.loot.parameters;

import net.minecraft.resources.ResourceLocation;

public class LootContextParam<T> {
    private final ResourceLocation name;

    public LootContextParam(ResourceLocation param0) {
        this.name = param0;
    }

    public ResourceLocation getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return "<parameter " + this.name + ">";
    }
}
