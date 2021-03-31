package net.minecraft.world.level;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.WeightedEntry;

public class SpawnData extends WeightedEntry.IntrusiveBase {
    public static final int DEFAULT_WEIGHT = 1;
    public static final String DEFAULT_TYPE = "minecraft:pig";
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
        ResourceLocation var0 = ResourceLocation.tryParse(param1.getString("id"));
        if (var0 != null) {
            param1.putString("id", var0.toString());
        } else {
            param1.putString("id", "minecraft:pig");
        }

    }

    public CompoundTag save() {
        CompoundTag var0 = new CompoundTag();
        var0.put("Entity", this.tag);
        var0.putInt("Weight", this.getWeight().asInt());
        return var0;
    }

    public CompoundTag getTag() {
        return this.tag;
    }
}
