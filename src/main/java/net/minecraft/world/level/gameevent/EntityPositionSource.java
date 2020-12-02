package net.minecraft.world.level.gameevent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class EntityPositionSource implements PositionSource {
    public static final Codec<EntityPositionSource> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(Codec.INT.fieldOf("source_entity_id").forGetter(param0x -> param0x.sourceEntityId)).apply(param0, EntityPositionSource::new)
    );
    private final int sourceEntityId;
    private Optional<Entity> sourceEntity = Optional.empty();

    public EntityPositionSource(int param0) {
        this.sourceEntityId = param0;
    }

    @Override
    public Optional<BlockPos> getPosition(Level param0) {
        if (!this.sourceEntity.isPresent()) {
            this.sourceEntity = Optional.ofNullable(param0.getEntity(this.sourceEntityId));
        }

        return this.sourceEntity.map(Entity::blockPosition);
    }

    @Override
    public PositionSourceType<?> getType() {
        return PositionSourceType.ENTITY;
    }

    public static class Type implements PositionSourceType<EntityPositionSource> {
        public EntityPositionSource read(FriendlyByteBuf param0) {
            return new EntityPositionSource(param0.readVarInt());
        }

        public void write(FriendlyByteBuf param0, EntityPositionSource param1) {
            param0.writeVarInt(param1.sourceEntityId);
        }

        @Override
        public Codec<EntityPositionSource> codec() {
            return EntityPositionSource.CODEC;
        }
    }
}
