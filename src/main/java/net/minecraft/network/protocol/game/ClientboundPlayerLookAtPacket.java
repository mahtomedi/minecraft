package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ClientboundPlayerLookAtPacket implements Packet<ClientGamePacketListener> {
    private final double x;
    private final double y;
    private final double z;
    private final int entity;
    private final EntityAnchorArgument.Anchor fromAnchor;
    private final EntityAnchorArgument.Anchor toAnchor;
    private final boolean atEntity;

    public ClientboundPlayerLookAtPacket(EntityAnchorArgument.Anchor param0, double param1, double param2, double param3) {
        this.fromAnchor = param0;
        this.x = param1;
        this.y = param2;
        this.z = param3;
        this.entity = 0;
        this.atEntity = false;
        this.toAnchor = null;
    }

    public ClientboundPlayerLookAtPacket(EntityAnchorArgument.Anchor param0, Entity param1, EntityAnchorArgument.Anchor param2) {
        this.fromAnchor = param0;
        this.entity = param1.getId();
        this.toAnchor = param2;
        Vec3 var0 = param2.apply(param1);
        this.x = var0.x;
        this.y = var0.y;
        this.z = var0.z;
        this.atEntity = true;
    }

    public ClientboundPlayerLookAtPacket(FriendlyByteBuf param0) {
        this.fromAnchor = param0.readEnum(EntityAnchorArgument.Anchor.class);
        this.x = param0.readDouble();
        this.y = param0.readDouble();
        this.z = param0.readDouble();
        this.atEntity = param0.readBoolean();
        if (this.atEntity) {
            this.entity = param0.readVarInt();
            this.toAnchor = param0.readEnum(EntityAnchorArgument.Anchor.class);
        } else {
            this.entity = 0;
            this.toAnchor = null;
        }

    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeEnum(this.fromAnchor);
        param0.writeDouble(this.x);
        param0.writeDouble(this.y);
        param0.writeDouble(this.z);
        param0.writeBoolean(this.atEntity);
        if (this.atEntity) {
            param0.writeVarInt(this.entity);
            param0.writeEnum(this.toAnchor);
        }

    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleLookAt(this);
    }

    public EntityAnchorArgument.Anchor getFromAnchor() {
        return this.fromAnchor;
    }

    @Nullable
    public Vec3 getPosition(Level param0) {
        if (this.atEntity) {
            Entity var0 = param0.getEntity(this.entity);
            return var0 == null ? new Vec3(this.x, this.y, this.z) : this.toAnchor.apply(var0);
        } else {
            return new Vec3(this.x, this.y, this.z);
        }
    }
}
