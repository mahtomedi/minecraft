package net.minecraft.world.entity.npc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class VillagerData {
    private static final int[] NEXT_LEVEL_XP_THRESHOLDS = new int[]{0, 10, 70, 150, 250};
    public static final Codec<VillagerData> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Registry.VILLAGER_TYPE.fieldOf("type").withDefault(() -> VillagerType.PLAINS).forGetter(param0x -> param0x.type),
                    Registry.VILLAGER_PROFESSION.fieldOf("profession").withDefault(() -> VillagerProfession.NONE).forGetter(param0x -> param0x.profession),
                    Codec.INT.fieldOf("level").withDefault(1).forGetter(param0x -> param0x.level)
                )
                .apply(param0, VillagerData::new)
    );
    private final VillagerType type;
    private final VillagerProfession profession;
    private final int level;

    public VillagerData(VillagerType param0, VillagerProfession param1, int param2) {
        this.type = param0;
        this.profession = param1;
        this.level = Math.max(1, param2);
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
