package net.minecraft.world.level.gameevent;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EntityPositionSource implements PositionSource {
    public static final Codec<EntityPositionSource> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    UUIDUtil.CODEC.fieldOf("source_entity").forGetter(EntityPositionSource::getUuid),
                    Codec.FLOAT.fieldOf("y_offset").orElse(0.0F).forGetter(param0x -> param0x.yOffset)
                )
                .apply(param0, (param0x, param1) -> new EntityPositionSource(Either.right(Either.left(param0x)), param1))
    );
    private Either<Entity, Either<UUID, Integer>> entityOrUuidOrId;
    final float yOffset;

    public EntityPositionSource(Entity param0, float param1) {
        this(Either.left(param0), param1);
    }

    EntityPositionSource(Either<Entity, Either<UUID, Integer>> param0, float param1) {
        this.entityOrUuidOrId = param0;
        this.yOffset = param1;
    }

    @Override
    public Optional<Vec3> getPosition(Level param0) {
        if (this.entityOrUuidOrId.left().isEmpty()) {
            this.resolveEntity(param0);
        }

        return this.entityOrUuidOrId.left().map(param0x -> param0x.position().add(0.0, (double)this.yOffset, 0.0));
    }

    private void resolveEntity(Level param0) {
        this.entityOrUuidOrId
            .map(
                Optional::of,
                param1 -> Optional.ofNullable(param1.map(param1x -> param0 instanceof ServerLevel var0x ? var0x.getEntity(param1x) : null, param0::getEntity))
            )
            .ifPresent(param0x -> this.entityOrUuidOrId = Either.left(param0x));
    }

    private UUID getUuid() {
        return this.entityOrUuidOrId.map(Entity::getUUID, param0 -> param0.map(Function.identity(), param0x -> {
                throw new RuntimeException("Unable to get entityId from uuid");
            }));
    }

    int getId() {
        return this.entityOrUuidOrId.map(Entity::getId, param0 -> param0.map(param0x -> {
                throw new IllegalStateException("Unable to get entityId from uuid");
            }, Function.identity()));
    }

    @Override
    public PositionSourceType<?> getType() {
        return PositionSourceType.ENTITY;
    }

    public static class Type implements PositionSourceType<EntityPositionSource> {
        public EntityPositionSource read(FriendlyByteBuf param0) {
            return new EntityPositionSource(Either.right(Either.right(param0.readVarInt())), param0.readFloat());
        }

        public void write(FriendlyByteBuf param0, EntityPositionSource param1) {
            param0.writeVarInt(param1.getId());
            param0.writeFloat(param1.yOffset);
        }

        @Override
        public Codec<EntityPositionSource> codec() {
            return EntityPositionSource.CODEC;
        }
    }
}
