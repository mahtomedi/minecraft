package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.phys.Vec3;

public record FishingHookPredicate(Optional<Boolean> inOpenWater) implements EntitySubPredicate {
    public static final FishingHookPredicate ANY = new FishingHookPredicate(Optional.empty());
    public static final MapCodec<FishingHookPredicate> CODEC = RecordCodecBuilder.mapCodec(
        param0 -> param0.group(ExtraCodecs.strictOptionalField(Codec.BOOL, "in_open_water").forGetter(FishingHookPredicate::inOpenWater))
                .apply(param0, FishingHookPredicate::new)
    );

    public static FishingHookPredicate inOpenWater(boolean param0) {
        return new FishingHookPredicate(Optional.of(param0));
    }

    @Override
    public EntitySubPredicate.Type type() {
        return EntitySubPredicate.Types.FISHING_HOOK;
    }

    @Override
    public boolean matches(Entity param0, ServerLevel param1, @Nullable Vec3 param2) {
        if (this.inOpenWater.isEmpty()) {
            return true;
        } else if (param0 instanceof FishingHook var0) {
            return this.inOpenWater.get() == var0.isOpenWaterFishing();
        } else {
            return false;
        }
    }
}
