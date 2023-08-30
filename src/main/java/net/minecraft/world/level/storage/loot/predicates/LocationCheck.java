package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public record LocationCheck(Optional<LocationPredicate> predicate, BlockPos offset) implements LootItemCondition {
    private static final MapCodec<BlockPos> OFFSET_CODEC = RecordCodecBuilder.mapCodec(
        param0 -> param0.group(
                    ExtraCodecs.strictOptionalField(Codec.INT, "offsetX", 0).forGetter(Vec3i::getX),
                    ExtraCodecs.strictOptionalField(Codec.INT, "offsetY", 0).forGetter(Vec3i::getY),
                    ExtraCodecs.strictOptionalField(Codec.INT, "offsetZ", 0).forGetter(Vec3i::getZ)
                )
                .apply(param0, BlockPos::new)
    );
    public static final Codec<LocationCheck> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ExtraCodecs.strictOptionalField(LocationPredicate.CODEC, "predicate").forGetter(LocationCheck::predicate),
                    OFFSET_CODEC.forGetter(LocationCheck::offset)
                )
                .apply(param0, LocationCheck::new)
    );

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.LOCATION_CHECK;
    }

    public boolean test(LootContext param0) {
        Vec3 var0 = param0.getParamOrNull(LootContextParams.ORIGIN);
        return var0 != null
            && (
                this.predicate.isEmpty()
                    || this.predicate
                        .get()
                        .matches(
                            param0.getLevel(),
                            var0.x() + (double)this.offset.getX(),
                            var0.y() + (double)this.offset.getY(),
                            var0.z() + (double)this.offset.getZ()
                        )
            );
    }

    public static LootItemCondition.Builder checkLocation(LocationPredicate.Builder param0) {
        return () -> new LocationCheck(Optional.of(param0.build()), BlockPos.ZERO);
    }

    public static LootItemCondition.Builder checkLocation(LocationPredicate.Builder param0, BlockPos param1) {
        return () -> new LocationCheck(Optional.of(param0.build()), param1);
    }
}
