package net.minecraft.world.level.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

public interface SuspiciousEffectHolder {
    List<SuspiciousEffectHolder.EffectEntry> getSuspiciousEffects();

    static List<SuspiciousEffectHolder> getAllEffectHolders() {
        return BuiltInRegistries.ITEM.stream().map(SuspiciousEffectHolder::tryGet).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Nullable
    static SuspiciousEffectHolder tryGet(ItemLike param0) {
        Item var3 = param0.asItem();
        if (var3 instanceof BlockItem var0) {
            Block var6 = var0.getBlock();
            if (var6 instanceof SuspiciousEffectHolder var1) {
                return var1;
            }
        }

        Item var1 = param0.asItem();
        return var1 instanceof SuspiciousEffectHolder var2x ? var2x : null;
    }

    public static record EffectEntry(MobEffect effect, int duration) {
        public static final Codec<SuspiciousEffectHolder.EffectEntry> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        BuiltInRegistries.MOB_EFFECT.byNameCodec().fieldOf("id").forGetter(SuspiciousEffectHolder.EffectEntry::effect),
                        Codec.INT.optionalFieldOf("duration", Integer.valueOf(160)).forGetter(SuspiciousEffectHolder.EffectEntry::duration)
                    )
                    .apply(param0, SuspiciousEffectHolder.EffectEntry::new)
        );
        public static final Codec<List<SuspiciousEffectHolder.EffectEntry>> LIST_CODEC = CODEC.listOf();

        public MobEffectInstance createEffectInstance() {
            return new MobEffectInstance(this.effect, this.duration);
        }
    }
}
