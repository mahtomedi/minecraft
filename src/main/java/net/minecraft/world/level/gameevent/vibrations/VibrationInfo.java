package net.minecraft.world.level.gameevent.vibrations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public record VibrationInfo(GameEvent gameEvent, float distance, Vec3 pos, @Nullable UUID uuid, @Nullable UUID projectileOwnerUuid, @Nullable Entity entity) {
    public static final Codec<VibrationInfo> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    BuiltInRegistries.GAME_EVENT.byNameCodec().fieldOf("game_event").forGetter(VibrationInfo::gameEvent),
                    Codec.floatRange(0.0F, Float.MAX_VALUE).fieldOf("distance").forGetter(VibrationInfo::distance),
                    Vec3.CODEC.fieldOf("pos").forGetter(VibrationInfo::pos),
                    UUIDUtil.CODEC.optionalFieldOf("source").forGetter(param0x -> Optional.ofNullable(param0x.uuid())),
                    UUIDUtil.CODEC.optionalFieldOf("projectile_owner").forGetter(param0x -> Optional.ofNullable(param0x.projectileOwnerUuid()))
                )
                .apply(
                    param0, (param0x, param1, param2, param3, param4) -> new VibrationInfo(param0x, param1, param2, param3.orElse(null), param4.orElse(null))
                )
    );

    public VibrationInfo(GameEvent param0, float param1, Vec3 param2, @Nullable UUID param3, @Nullable UUID param4) {
        this(param0, param1, param2, param3, param4, null);
    }

    public VibrationInfo(GameEvent param0, float param1, Vec3 param2, @Nullable Entity param3) {
        this(param0, param1, param2, param3 == null ? null : param3.getUUID(), getProjectileOwner(param3), param3);
    }

    @Nullable
    private static UUID getProjectileOwner(@Nullable Entity param0) {
        if (param0 instanceof Projectile var0 && var0.getOwner() != null) {
            return var0.getOwner().getUUID();
        }

        return null;
    }

    public Optional<Entity> getEntity(ServerLevel param0) {
        return Optional.ofNullable(this.entity).or(() -> Optional.ofNullable(this.uuid).map(param0::getEntity));
    }

    public Optional<Entity> getProjectileOwner(ServerLevel param0) {
        return this.getEntity(param0)
            .filter(param0x -> param0x instanceof Projectile)
            .map(param0x -> (Projectile)param0x)
            .map(Projectile::getOwner)
            .or(() -> Optional.ofNullable(this.projectileOwnerUuid).map(param0::getEntity));
    }
}
