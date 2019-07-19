package net.minecraft.world.entity.npc;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class VillagerData {
    private static final int[] NEXT_LEVEL_XP_THRESHOLDS = new int[]{0, 10, 70, 150, 250};
    private final VillagerType type;
    private final VillagerProfession profession;
    private final int level;

    public VillagerData(VillagerType param0, VillagerProfession param1, int param2) {
        this.type = param0;
        this.profession = param1;
        this.level = Math.max(1, param2);
    }

    public VillagerData(Dynamic<?> param0) {
        this(
            Registry.VILLAGER_TYPE.get(ResourceLocation.tryParse(param0.get("type").asString(""))),
            Registry.VILLAGER_PROFESSION.get(ResourceLocation.tryParse(param0.get("profession").asString(""))),
            param0.get("level").asInt(1)
        );
    }

    public VillagerType getType() {
        return this.type;
    }

    public VillagerProfession getProfession() {
        return this.profession;
    }

    public int getLevel() {
        return this.level;
    }

    public VillagerData setType(VillagerType param0) {
        return new VillagerData(param0, this.profession, this.level);
    }

    public VillagerData setProfession(VillagerProfession param0) {
        return new VillagerData(this.type, param0, this.level);
    }

    public VillagerData setLevel(int param0) {
        return new VillagerData(this.type, this.profession, param0);
    }

    public <T> T serialize(DynamicOps<T> param0) {
        return param0.createMap(
            ImmutableMap.of(
                param0.createString("type"),
                param0.createString(Registry.VILLAGER_TYPE.getKey(this.type).toString()),
                param0.createString("profession"),
                param0.createString(Registry.VILLAGER_PROFESSION.getKey(this.profession).toString()),
                param0.createString("level"),
                param0.createInt(this.level)
            )
        );
    }

    @OnlyIn(Dist.CLIENT)
    public static int getMinXpPerLevel(int param0) {
        return canLevelUp(param0) ? NEXT_LEVEL_XP_THRESHOLDS[param0 - 1] : 0;
    }

    public static int getMaxXpPerLevel(int param0) {
        return canLevelUp(param0) ? NEXT_LEVEL_XP_THRESHOLDS[param0] : 0;
    }

    public static boolean canLevelUp(int param0) {
        return param0 >= 1 && param0 < 5;
    }
}
