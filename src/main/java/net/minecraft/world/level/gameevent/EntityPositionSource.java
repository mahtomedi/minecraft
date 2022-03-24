package net.minecraft.world.level.gameevent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EntityPositionSource implements PositionSource {
    public static final Codec<EntityPositionSource> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.INT.fieldOf("source_entity_id").forGetter(param0x -> param0x.sourceEntityId),
                    Codec.FLOAT.fieldOf("y_offset").forGetter(param0x -> param0x.yOffset)
                )
                .apply(param0, EntityPositionSource::new)
    );
    final int sourceEntityId;
    private Optional<Entity> sourceEntity = Optional.empty();
    final float yOffset;

    public EntityPositionSource(Entity param0, float param1) {
        this(param0.getId(), param1);
    }

    EntityPositionSource(int param0, float param1) {
        this.sourceEntityId = param0;
        this.yOffset = param1;
    }

    @Override
    public Optional<Vec3> getPosition(Level param0) {
        if (this.sourceEntity.isEmpty()) {
            this.sourceEntity = Optional.ofNullable(param0.getEntity(this.sourceEntityId));
        }

        return this.sourceEntity.map(param0x -> param0x.position().add(0.0, (double)this.yOffset, 0.0));
    }

    @Override
    public PositionSourceType<?> getType() {
        return PositionSourceType.ENTITY;
    }

    public static class Type implements PositionSourceType<EntityPositionSource> {
        public EntityPositionSource read(FriendlyByteBuf param0) {
            return new EntityPositionSource(param0.readVarInt(), param0.readFloat());
        }

        public void write(FriendlyByteBuf param0, EntityPositionSource param1) {
            param0.writeVarInt(param1.sourceEntityId);
            param0.writeFloat(param1.yOffset);
        }

        @Override
        public Codec<EntityPositionSource> codec() {
            return EntityPositionSource.CODEC;
        }
    }
}
