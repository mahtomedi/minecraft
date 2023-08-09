package net.minecraft.advancements.critereon;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.phys.Vec3;

public record LightningBoltPredicate(MinMaxBounds.Ints blocksSetOnFire, Optional<EntityPredicate> entityStruck) implements EntitySubPredicate {
    public static final MapCodec<LightningBoltPredicate> CODEC = RecordCodecBuilder.mapCodec(
        param0 -> param0.group(
                    ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "blocks_set_on_fire", MinMaxBounds.Ints.ANY)
                        .forGetter(LightningBoltPredicate::blocksSetOnFire),
                    ExtraCodecs.strictOptionalField(EntityPredicate.CODEC, "entity_struck").forGetter(LightningBoltPredicate::entityStruck)
                )
                .apply(param0, LightningBoltPredicate::new)
    );

    public static LightningBoltPredicate blockSetOnFire(MinMaxBounds.Ints param0) {
        return new LightningBoltPredicate(param0, Optional.empty());
    }

    @Override
    public EntitySubPredicate.Type type() {
        return EntitySubPredicate.Types.LIGHTNING;
    }

    @Override
    public boolean matches(Entity param0, ServerLevel param1, @Nullable Vec3 param2) {
        if (!(param0 instanceof LightningBolt)) {
            return false;
        } else {
            LightningBolt var0 = (LightningBolt)param0;
            return this.blocksSetOnFire.matches(var0.getBlocksSetOnFire())
                && (this.entityStruck.isEmpty() || var0.getHitEntities().anyMatch(param2x -> this.entityStruck.get().matches(param1, param2, param2x)));
        }
    }
}
