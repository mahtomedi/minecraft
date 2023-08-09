package net.minecraft.advancements.critereon;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.phys.Vec3;

public record SlimePredicate(MinMaxBounds.Ints size) implements EntitySubPredicate {
    public static final MapCodec<SlimePredicate> CODEC = RecordCodecBuilder.mapCodec(
        param0 -> param0.group(ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "size", MinMaxBounds.Ints.ANY).forGetter(SlimePredicate::size))
                .apply(param0, SlimePredicate::new)
    );

    public static SlimePredicate sized(MinMaxBounds.Ints param0) {
        return new SlimePredicate(param0);
    }

    @Override
    public boolean matches(Entity param0, ServerLevel param1, @Nullable Vec3 param2) {
        return param0 instanceof Slime var0 ? this.size.matches(var0.getSize()) : false;
    }

    @Override
    public EntitySubPredicate.Type type() {
        return EntitySubPredicate.Types.SLIME;
    }
}
