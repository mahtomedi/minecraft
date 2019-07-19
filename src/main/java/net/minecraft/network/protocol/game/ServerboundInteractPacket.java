package net.minecraft.network.protocol.game;

import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerboundInteractPacket implements Packet<ServerGamePacketListener> {
    private int entityId;
    private ServerboundInteractPacket.Action action;
    private Vec3 location;
    private InteractionHand hand;

    public ServerboundInteractPacket() {
    }

    public ServerboundInteractPacket(Entity param0) {
        this.entityId = param0.getId();
        this.action = ServerboundInteractPacket.Action.ATTACK;
    }

    @OnlyIn(Dist.CLIENT)
    public ServerboundInteractPacket(Entity param0, InteractionHand param1) {
        this.entityId = param0.getId();
        this.action = ServerboundInteractPacket.Action.INTERACT;
        this.hand = param1;
    }

    @OnlyIn(Dist.CLIENT)
    public ServerboundInteractPacket(Entity param0, InteractionHand param1, Vec3 param2) {
        this.entityId = param0.getId();
        this.action = ServerboundInteractPacket.Action.INTERACT_AT;
        this.hand = param1;
        this.location = param2;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.entityId = param0.readVarInt();
        this.action = param0.readEnum(ServerboundInteractPacket.Action.class);
        if (this.action == ServerboundInteractPacket.Action.INTERACT_AT) {
            this.location = new Vec3((double)param0.readFloat(), (double)param0.readFloat(), (double)param0.readFloat());
        }

        if (this.action == ServerboundInteractPacket.Action.INTERACT || this.action == ServerboundInteractPacket.Action.INTERACT_AT) {
            this.hand = param0.readEnum(InteractionHand.class);
        }

    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeVarInt(this.entityId);
        param0.writeEnum(this.action);
        if (this.action == ServerboundInteractPacket.Action.INTERACT_AT) {
            param0.writeFloat((float)this.location.x);
            param0.writeFloat((float)this.location.y);
            param0.writeFloat((float)this.location.z);
        }

        if (this.action == ServerboundInteractPacket.Action.INTERACT || this.action == ServerboundInteractPacket.Action.INTERACT_AT) {
            param0.writeEnum(this.hand);
        }

    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleInteract(this);
    }

    @Nullable
    public Entity getTarget(Level param0) {
        return param0.getEntity(this.entityId);
    }

    public ServerboundInteractPacket.Action getAction() {
        return this.action;
    }

    public InteractionHand getHand() {
        return this.hand;
    }

    public Vec3 getLocation() {
        return this.location;
    }

    public static enum Action {
        INTERACT,
        ATTACK,
        INTERACT_AT;
    }
}
