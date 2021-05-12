package net.minecraft.network.protocol.game;

import java.util.UUID;
import java.util.function.Function;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.BossEvent;

public class ClientboundBossEventPacket implements Packet<ClientGamePacketListener> {
    private static final int FLAG_DARKEN = 1;
    private static final int FLAG_MUSIC = 2;
    private static final int FLAG_FOG = 4;
    private final UUID id;
    private final ClientboundBossEventPacket.Operation operation;
    static final ClientboundBossEventPacket.Operation REMOVE_OPERATION = new ClientboundBossEventPacket.Operation() {
        @Override
        public ClientboundBossEventPacket.OperationType getType() {
            return ClientboundBossEventPacket.OperationType.REMOVE;
        }

        @Override
        public void dispatch(UUID param0, ClientboundBossEventPacket.Handler param1) {
            param1.remove(param0);
        }

        @Override
        public void write(FriendlyByteBuf param0) {
        }
    };

    private ClientboundBossEventPacket(UUID param0, ClientboundBossEventPacket.Operation param1) {
        this.id = param0;
        this.operation = param1;
    }

    public ClientboundBossEventPacket(FriendlyByteBuf param0) {
        this.id = param0.readUUID();
        ClientboundBossEventPacket.OperationType var0 = param0.readEnum(ClientboundBossEventPacket.OperationType.class);
        this.operation = var0.reader.apply(param0);
    }

    public static ClientboundBossEventPacket createAddPacket(BossEvent param0) {
        return new ClientboundBossEventPacket(param0.getId(), new ClientboundBossEventPacket.AddOperation(param0));
    }

    public static ClientboundBossEventPacket createRemovePacket(UUID param0) {
        return new ClientboundBossEventPacket(param0, REMOVE_OPERATION);
    }

    public static ClientboundBossEventPacket createUpdateProgressPacket(BossEvent param0) {
        return new ClientboundBossEventPacket(param0.getId(), new ClientboundBossEventPacket.UpdateProgressOperation(param0.getProgress()));
    }

    public static ClientboundBossEventPacket createUpdateNamePacket(BossEvent param0) {
        return new ClientboundBossEventPacket(param0.getId(), new ClientboundBossEventPacket.UpdateNameOperation(param0.getName()));
    }

    public static ClientboundBossEventPacket createUpdateStylePacket(BossEvent param0) {
        return new ClientboundBossEventPacket(param0.getId(), new ClientboundBossEventPacket.UpdateStyleOperation(param0.getColor(), param0.getOverlay()));
    }

    public static ClientboundBossEventPacket createUpdatePropertiesPacket(BossEvent param0) {
        return new ClientboundBossEventPacket(
            param0.getId(),
            new ClientboundBossEventPacket.UpdatePropertiesOperation(param0.shouldDarkenScreen(), param0.shouldPlayBossMusic(), param0.shouldCreateWorldFog())
        );
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeUUID(this.id);
        param0.writeEnum(this.operation.getType());
        this.operation.write(param0);
    }

    static int encodeProperties(boolean param0, boolean param1, boolean param2) {
        int var0 = 0;
        if (param0) {
            var0 |= 1;
        }

        if (param1) {
            var0 |= 2;
        }

        if (param2) {
            var0 |= 4;
        }

        return var0;
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleBossUpdate(this);
    }

    public void dispatch(ClientboundBossEventPacket.Handler param0) {
        this.operation.dispatch(this.id, param0);
    }

    static class AddOperation implements ClientboundBossEventPacket.Operation {
        private final Component name;
        private final float progress;
        private final BossEvent.BossBarColor color;
        private final BossEvent.BossBarOverlay overlay;
        private final boolean darkenScreen;
        private final boolean playMusic;
        private final boolean createWorldFog;

        AddOperation(BossEvent param0) {
            this.name = param0.getName();
            this.progress = param0.getProgress();
            this.color = param0.getColor();
            this.overlay = param0.getOverlay();
            this.darkenScreen = param0.shouldDarkenScreen();
            this.playMusic = param0.shouldPlayBossMusic();
            this.createWorldFog = param0.shouldCreateWorldFog();
        }

        private AddOperation(FriendlyByteBuf param0) {
            this.name = param0.readComponent();
            this.progress = param0.readFloat();
            this.color = param0.readEnum(BossEvent.BossBarColor.class);
            this.overlay = param0.readEnum(BossEvent.BossBarOverlay.class);
            int var0 = param0.readUnsignedByte();
            this.darkenScreen = (var0 & 1) > 0;
            this.playMusic = (var0 & 2) > 0;
            this.createWorldFog = (var0 & 4) > 0;
        }

        @Override
        public ClientboundBossEventPacket.OperationType getType() {
            return ClientboundBossEventPacket.OperationType.ADD;
        }

        @Override
        public void dispatch(UUID param0, ClientboundBossEventPacket.Handler param1) {
            param1.add(param0, this.name, this.progress, this.color, this.overlay, this.darkenScreen, this.playMusic, this.createWorldFog);
        }

        @Override
        public void write(FriendlyByteBuf param0) {
            param0.writeComponent(this.name);
            param0.writeFloat(this.progress);
            param0.writeEnum(this.color);
            param0.writeEnum(this.overlay);
            param0.writeByte(ClientboundBossEventPacket.encodeProperties(this.darkenScreen, this.playMusic, this.createWorldFog));
        }
    }

    public interface Handler {
        default void add(
            UUID param0,
            Component param1,
            float param2,
            BossEvent.BossBarColor param3,
            BossEvent.BossBarOverlay param4,
            boolean param5,
            boolean param6,
            boolean param7
        ) {
        }

        default void remove(UUID param0) {
        }

        default void updateProgress(UUID param0, float param1) {
        }

        default void updateName(UUID param0, Component param1) {
        }

        default void updateStyle(UUID param0, BossEvent.BossBarColor param1, BossEvent.BossBarOverlay param2) {
        }

        default void updateProperties(UUID param0, boolean param1, boolean param2, boolean param3) {
        }
    }

    interface Operation {
        ClientboundBossEventPacket.OperationType getType();

        void dispatch(UUID var1, ClientboundBossEventPacket.Handler var2);

        void write(FriendlyByteBuf var1);
    }

    static enum OperationType {
        ADD(ClientboundBossEventPacket.AddOperation::new),
        REMOVE(param0 -> ClientboundBossEventPacket.REMOVE_OPERATION),
        UPDATE_PROGRESS(ClientboundBossEventPacket.UpdateProgressOperation::new),
        UPDATE_NAME(ClientboundBossEventPacket.UpdateNameOperation::new),
        UPDATE_STYLE(ClientboundBossEventPacket.UpdateStyleOperation::new),
        UPDATE_PROPERTIES(ClientboundBossEventPacket.UpdatePropertiesOperation::new);

        final Function<FriendlyByteBuf, ClientboundBossEventPacket.Operation> reader;

        private OperationType(Function<FriendlyByteBuf, ClientboundBossEventPacket.Operation> param0) {
            this.reader = param0;
        }
    }

    static class UpdateNameOperation implements ClientboundBossEventPacket.Operation {
        private final Component name;

        UpdateNameOperation(Component param0) {
            this.name = param0;
        }

        private UpdateNameOperation(FriendlyByteBuf param0) {
            this.name = param0.readComponent();
        }

        @Override
        public ClientboundBossEventPacket.OperationType getType() {
            return ClientboundBossEventPacket.OperationType.UPDATE_NAME;
        }

        @Override
        public void dispatch(UUID param0, ClientboundBossEventPacket.Handler param1) {
            param1.updateName(param0, this.name);
        }

        @Override
        public void write(FriendlyByteBuf param0) {
            param0.writeComponent(this.name);
        }
    }

    static class UpdateProgressOperation implements ClientboundBossEventPacket.Operation {
        private final float progress;

        UpdateProgressOperation(float param0) {
            this.progress = param0;
        }

        private UpdateProgressOperation(FriendlyByteBuf param0) {
            this.progress = param0.readFloat();
        }

        @Override
        public ClientboundBossEventPacket.OperationType getType() {
            return ClientboundBossEventPacket.OperationType.UPDATE_PROGRESS;
        }

        @Override
        public void dispatch(UUID param0, ClientboundBossEventPacket.Handler param1) {
            param1.updateProgress(param0, this.progress);
        }

        @Override
        public void write(FriendlyByteBuf param0) {
            param0.writeFloat(this.progress);
        }
    }

    static class UpdatePropertiesOperation implements ClientboundBossEventPacket.Operation {
        private final boolean darkenScreen;
        private final boolean playMusic;
        private final boolean createWorldFog;

        UpdatePropertiesOperation(boolean param0, boolean param1, boolean param2) {
            this.darkenScreen = param0;
            this.playMusic = param1;
            this.createWorldFog = param2;
        }

        private UpdatePropertiesOperation(FriendlyByteBuf param0) {
            int var0 = param0.readUnsignedByte();
            this.darkenScreen = (var0 & 1) > 0;
            this.playMusic = (var0 & 2) > 0;
            this.createWorldFog = (var0 & 4) > 0;
        }

        @Override
        public ClientboundBossEventPacket.OperationType getType() {
            return ClientboundBossEventPacket.OperationType.UPDATE_PROPERTIES;
        }

        @Override
        public void dispatch(UUID param0, ClientboundBossEventPacket.Handler param1) {
            param1.updateProperties(param0, this.darkenScreen, this.playMusic, this.createWorldFog);
        }

        @Override
        public void write(FriendlyByteBuf param0) {
            param0.writeByte(ClientboundBossEventPacket.encodeProperties(this.darkenScreen, this.playMusic, this.createWorldFog));
        }
    }

    static class UpdateStyleOperation implements ClientboundBossEventPacket.Operation {
        private final BossEvent.BossBarColor color;
        private final BossEvent.BossBarOverlay overlay;

        UpdateStyleOperation(BossEvent.BossBarColor param0, BossEvent.BossBarOverlay param1) {
            this.color = param0;
            this.overlay = param1;
        }

        private UpdateStyleOperation(FriendlyByteBuf param0) {
            this.color = param0.readEnum(BossEvent.BossBarColor.class);
            this.overlay = param0.readEnum(BossEvent.BossBarOverlay.class);
        }

        @Override
        public ClientboundBossEventPacket.OperationType getType() {
            return ClientboundBossEventPacket.OperationType.UPDATE_STYLE;
        }

        @Override
        public void dispatch(UUID param0, ClientboundBossEventPacket.Handler param1) {
            param1.updateStyle(param0, this.color, this.overlay);
        }

        @Override
        public void write(FriendlyByteBuf param0) {
            param0.writeEnum(this.color);
            param0.writeEnum(this.overlay);
        }
    }
}
