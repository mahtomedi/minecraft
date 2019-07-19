package net.minecraft.world.level;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.WeighedRandom;

public class SpawnData extends WeighedRandom.WeighedRandomItem {
    private final CompoundTag tag;

    public SpawnData() {
        super(1);
        this.tag = new CompoundTag();
        this.tag.putString("id", "minecraft:pig");
    }

    public SpawnData(CompoundTag param0) {
        this(param0.contains("Weight", 99) ? param0.getInt("Weight") : 1, param0.getCompound("Entity"));
    }

    public SpawnData(int param0, CompoundTag param1) {
        super(param0);
        this.tag = param1;
    }

    public CompoundTag save() {
        CompoundTag var0 = new CompoundTag();
        if (!this.tag.contains("id", 8)) {
            this.tag.putString("id", "minecraft:pig");
        } else if (!this.tag.getString("id").contains(":")) {
            this.tag.putString("id", new ResourceLocation(this.tag.getString("id")).toString());
        }

        var0.put("Entity", this.tag);
        var0.putInt("Weight", this.weight);
        return var0;
    }

    public CompoundTag getTag() {
        return this.tag;
    }
}
