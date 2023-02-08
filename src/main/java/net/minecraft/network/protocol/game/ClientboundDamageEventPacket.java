package net.minecraft.network.protocol.game;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record ClientboundDamageEventPacket(int entityId, int sourceTypeId, int sourceCauseId, int sourceDirectId, Optional<Vec3> sourcePosition)
    implements Packet<ClientGamePacketListener> {
    public ClientboundDamageEventPacket(Entity param0, DamageSource param1) {
        this(
            param0.getId(),
            param0.getLevel().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getId(param1.type()),
            param1.getEntity() != null ? param1.getEntity().getId() : -1,
            param1.getDirectEntity() != null ? param1.getDirectEntity().getId() : -1,
            Optional.ofNullable(param1.sourcePositionRaw())
        );
    }

    public ClientboundDamageEventPacket(FriendlyByteBuf param0) {
        this(
            param0.readVarInt(),
            param0.readVarInt(),
            readOptionalEntityId(param0),
            readOptionalEntityId(param0),
            param0.readOptional(param0x -> new Vec3(param0x.readDouble(), param0x.readDouble(), param0x.readDouble()))
        );
    }

    private static void writeOptionalEntityId(FriendlyByteBuf param0, int param1) {
        param0.writeVarInt(param1 + 1);
    }

    private static int readOptionalEntityId(FriendlyByteBuf param0) {
        return param0.readVarInt() - 1;
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.entityId);
        param0.writeVarInt(this.sourceTypeId);
        writeOptionalEntityId(param0, this.sourceCauseId);
        writeOptionalEntityId(param0, this.sourceDirectId);
        param0.writeOptional(this.sourcePosition, (param0x, param1) -> {
            param0x.writeDouble(param1.x());
            param0x.writeDouble(param1.y());
            param0x.writeDouble(param1.z());
        });
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleDamageEvent(this);
    }

    public DamageSource getSource(Level param0) {
        Holder<DamageType> var0 = (Holder)param0.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolder(this.sourceTypeId).get();
        if (this.sourcePosition.isPresent()) {
            return new DamageSource(var0, this.sourcePosition.get());
        } else {
            Entity var1 = param0.getEntity(this.sourceCauseId);
            Entity var2 = param0.getEntity(this.sourceDirectId);
            return new DamageSource(var0, var2, var1);
        }
    }
}
