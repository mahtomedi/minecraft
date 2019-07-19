package net.minecraft.network.protocol.game;

import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundPlayerLookAtPacket implements Packet<ClientGamePacketListener> {
    private double x;
    private double y;
    private double z;
    private int entity;
    private EntityAnchorArgument.Anchor fromAnchor;
    private EntityAnchorArgument.Anchor toAnchor;
    private boolean atEntity;

    public ClientboundPlayerLookAtPacket() {
    }

    public ClientboundPlayerLookAtPacket(EntityAnchorArgument.Anchor param0, double param1, double param2, double param3) {
        this.fromAnchor = param0;
        this.x = param1;
        this.y = param2;
        this.z = param3;
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

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.fromAnchor = param0.readEnum(EntityAnchorArgument.Anchor.class);
        this.x = param0.readDouble();
        this.y = param0.readDouble();
        this.z = param0.readDouble();
        if (param0.readBoolean()) {
            this.atEntity = true;
            this.entity = param0.readVarInt();
            this.toAnchor = param0.readEnum(EntityAnchorArgument.Anchor.class);
        }

    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
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

    @OnlyIn(Dist.CLIENT)
    public EntityAnchorArgument.Anchor getFromAnchor() {
        return this.fromAnchor;
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public Vec3 getPosition(Level param0) {
        if (this.atEntity) {
            Entity var0 = param0.getEntity(this.entity);
            return var0 == null ? new Vec3(this.x, this.y, this.z) : this.toAnchor.apply(var0);
        } else {
            return new Vec3(this.x, this.y, this.z);
        }
    }
}
