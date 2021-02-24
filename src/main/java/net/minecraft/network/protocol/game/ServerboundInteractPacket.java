package net.minecraft.network.protocol.game;

import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerboundInteractPacket implements Packet<ServerGamePacketListener> {
    private final int entityId;
    private final ServerboundInteractPacket.Action action;
    private final boolean usingSecondaryAction;
    private static final ServerboundInteractPacket.Action ATTACK_ACTION = new ServerboundInteractPacket.Action() {
        @Override
        public ServerboundInteractPacket.ActionType getType() {
            return ServerboundInteractPacket.ActionType.ATTACK;
        }

        @Override
        public void dispatch(ServerboundInteractPacket.Handler param0) {
            param0.onAttack();
        }

        @Override
        public void write(FriendlyByteBuf param0) {
        }
    };

    @OnlyIn(Dist.CLIENT)
    private ServerboundInteractPacket(int param0, boolean param1, ServerboundInteractPacket.Action param2) {
        this.entityId = param0;
        this.action = param2;
        this.usingSecondaryAction = param1;
    }

    @OnlyIn(Dist.CLIENT)
    public static ServerboundInteractPacket createAttackPacket(Entity param0, boolean param1) {
        return new ServerboundInteractPacket(param0.getId(), param1, ATTACK_ACTION);
    }

    @OnlyIn(Dist.CLIENT)
    public static ServerboundInteractPacket createInteractionPacket(Entity param0, boolean param1, InteractionHand param2) {
        return new ServerboundInteractPacket(param0.getId(), param1, new ServerboundInteractPacket.InteractionAction(param2));
    }

    @OnlyIn(Dist.CLIENT)
    public static ServerboundInteractPacket createInteractionPacket(Entity param0, boolean param1, InteractionHand param2, Vec3 param3) {
        return new ServerboundInteractPacket(param0.getId(), param1, new ServerboundInteractPacket.InteractionAtLocationAction(param2, param3));
    }

    public ServerboundInteractPacket(FriendlyByteBuf param0) {
        this.entityId = param0.readVarInt();
        ServerboundInteractPacket.ActionType var0 = param0.readEnum(ServerboundInteractPacket.ActionType.class);
        this.action = var0.reader.apply(param0);
        this.usingSecondaryAction = param0.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.entityId);
        param0.writeEnum(this.action.getType());
        this.action.write(param0);
        param0.writeBoolean(this.usingSecondaryAction);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleInteract(this);
    }

    @Nullable
    public Entity getTarget(ServerLevel param0) {
        return param0.getEntityOrPart(this.entityId);
    }

    public boolean isUsingSecondaryAction() {
        return this.usingSecondaryAction;
    }

    public void dispatch(ServerboundInteractPacket.Handler param0) {
        this.action.dispatch(param0);
    }

    interface Action {
        ServerboundInteractPacket.ActionType getType();

        void dispatch(ServerboundInteractPacket.Handler var1);

        void write(FriendlyByteBuf var1);
    }

    static enum ActionType {
        INTERACT(param0 -> new ServerboundInteractPacket.InteractionAction(param0)),
        ATTACK(param0 -> ServerboundInteractPacket.ATTACK_ACTION),
        INTERACT_AT(param0 -> new ServerboundInteractPacket.InteractionAtLocationAction(param0));

        private final Function<FriendlyByteBuf, ServerboundInteractPacket.Action> reader;

        private ActionType(Function<FriendlyByteBuf, ServerboundInteractPacket.Action> param0) {
            this.reader = param0;
        }
    }

    public interface Handler {
        void onInteraction(InteractionHand var1);

        void onInteraction(InteractionHand var1, Vec3 var2);

        void onAttack();
    }

    static class InteractionAction implements ServerboundInteractPacket.Action {
        private final InteractionHand hand;

        @OnlyIn(Dist.CLIENT)
        private InteractionAction(InteractionHand param0) {
            this.hand = param0;
        }

        private InteractionAction(FriendlyByteBuf param0) {
            this.hand = param0.readEnum(InteractionHand.class);
        }

        @Override
        public ServerboundInteractPacket.ActionType getType() {
            return ServerboundInteractPacket.ActionType.INTERACT;
        }

        @Override
        public void dispatch(ServerboundInteractPacket.Handler param0) {
            param0.onInteraction(this.hand);
        }

        @Override
        public void write(FriendlyByteBuf param0) {
            param0.writeEnum(this.hand);
        }
    }

    static class InteractionAtLocationAction implements ServerboundInteractPacket.Action {
        private final InteractionHand hand;
        private final Vec3 location;

        @OnlyIn(Dist.CLIENT)
        private InteractionAtLocationAction(InteractionHand param0, Vec3 param1) {
            this.hand = param0;
            this.location = param1;
        }

        private InteractionAtLocationAction(FriendlyByteBuf param0) {
            this.location = new Vec3((double)param0.readFloat(), (double)param0.readFloat(), (double)param0.readFloat());
            this.hand = param0.readEnum(InteractionHand.class);
        }

        @Override
        public ServerboundInteractPacket.ActionType getType() {
            return ServerboundInteractPacket.ActionType.INTERACT_AT;
        }

        @Override
        public void dispatch(ServerboundInteractPacket.Handler param0) {
            param0.onInteraction(this.hand, this.location);
        }

        @Override
        public void write(FriendlyByteBuf param0) {
            param0.writeFloat((float)this.location.x);
            param0.writeFloat((float)this.location.y);
            param0.writeFloat((float)this.location.z);
            param0.writeEnum(this.hand);
        }
    }
}
