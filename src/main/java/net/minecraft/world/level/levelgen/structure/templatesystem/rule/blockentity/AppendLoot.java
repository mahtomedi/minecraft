package net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.slf4j.Logger;

public class AppendLoot implements RuleBlockEntityModifier {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Codec<AppendLoot> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(ResourceLocation.CODEC.fieldOf("loot_table").forGetter(param0x -> param0x.lootTable)).apply(param0, AppendLoot::new)
    );
    private final ResourceLocation lootTable;

    public AppendLoot(ResourceLocation param0) {
        this.lootTable = param0;
    }

    @Override
    public CompoundTag apply(RandomSource param0, @Nullable CompoundTag param1) {
        CompoundTag var0 = param1 == null ? new CompoundTag() : param1.copy();
        ResourceLocation.CODEC.encodeStart(NbtOps.INSTANCE, this.lootTable).resultOrPartial(LOGGER::error).ifPresent(param1x -> var0.put("LootTable", param1x));
        var0.putLong("LootTableSeed", param0.nextLong());
        return var0;
    }

    @Override
    public RuleBlockEntityModifierType<?> getType() {
        return RuleBlockEntityModifierType.APPEND_LOOT;
    }
}
