package net.minecraft.advancements;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record AdvancementHolder(ResourceLocation id, Advancement value) {
    public void write(FriendlyByteBuf param0) {
        param0.writeResourceLocation(this.id);
        this.value.write(param0);
    }

    public static AdvancementHolder read(FriendlyByteBuf param0) {
        return new AdvancementHolder(param0.readResourceLocation(), Advancement.read(param0));
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else {
            if (param0 instanceof AdvancementHolder var0 && this.id.equals(var0.id)) {
                return true;
            }

            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public String toString() {
        return this.id.toString();
    }
}
