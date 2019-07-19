package net.minecraft.network.protocol.game;

import java.io.IOException;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.BossEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundBossEventPacket implements Packet<ClientGamePacketListener> {
    private UUID id;
    private ClientboundBossEventPacket.Operation operation;
    private Component name;
    private float pct;
    private BossEvent.BossBarColor color;
    private BossEvent.BossBarOverlay overlay;
    private boolean darkenScreen;
    private boolean playMusic;
    private boolean createWorldFog;

    public ClientboundBossEventPacket() {
    }

    public ClientboundBossEventPacket(ClientboundBossEventPacket.Operation param0, BossEvent param1) {
        this.operation = param0;
        this.id = param1.getId();
        this.name = param1.getName();
        this.pct = param1.getPercent();
        this.color = param1.getColor();
        this.overlay = param1.getOverlay();
        this.darkenScreen = param1.shouldDarkenScreen();
        this.playMusic = param1.shouldPlayBossMusic();
        this.createWorldFog = param1.shouldCreateWorldFog();
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.id = param0.readUUID();
        this.operation = param0.readEnum(ClientboundBossEventPacket.Operation.class);
        switch(this.operation) {
            case ADD:
                this.name = param0.readComponent();
                this.pct = param0.readFloat();
                this.color = param0.readEnum(BossEvent.BossBarColor.class);
                this.overlay = param0.readEnum(BossEvent.BossBarOverlay.class);
                this.decodeProperties(param0.readUnsignedByte());
            case REMOVE:
            default:
                break;
            case UPDATE_PCT:
                this.pct = param0.readFloat();
                break;
            case UPDATE_NAME:
                this.name = param0.readComponent();
                break;
            case UPDATE_STYLE:
                this.color = param0.readEnum(BossEvent.BossBarColor.class);
                this.overlay = param0.readEnum(BossEvent.BossBarOverlay.class);
                break;
            case UPDATE_PROPERTIES:
                this.decodeProperties(param0.readUnsignedByte());
        }

    }

    private void decodeProperties(int param0) {
        this.darkenScreen = (param0 & 1) > 0;
        this.playMusic = (param0 & 2) > 0;
        this.createWorldFog = (param0 & 4) > 0;
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeUUID(this.id);
        param0.writeEnum(this.operation);
        switch(this.operation) {
            case ADD:
                param0.writeComponent(this.name);
                param0.writeFloat(this.pct);
                param0.writeEnum(this.color);
                param0.writeEnum(this.overlay);
                param0.writeByte(this.encodeProperties());
            case REMOVE:
            default:
                break;
            case UPDATE_PCT:
                param0.writeFloat(this.pct);
                break;
            case UPDATE_NAME:
                param0.writeComponent(this.name);
                break;
            case UPDATE_STYLE:
                param0.writeEnum(this.color);
                param0.writeEnum(this.overlay);
                break;
            case UPDATE_PROPERTIES:
                param0.writeByte(this.encodeProperties());
        }

    }

    private int encodeProperties() {
        int var0 = 0;
        if (this.darkenScreen) {
            var0 |= 1;
        }

        if (this.playMusic) {
            var0 |= 2;
        }

        if (this.createWorldFog) {
            var0 |= 4;
        }

        return var0;
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleBossUpdate(this);
    }

    @OnlyIn(Dist.CLIENT)
    public UUID getId() {
        return this.id;
    }

    @OnlyIn(Dist.CLIENT)
    public ClientboundBossEventPacket.Operation getOperation() {
        return this.operation;
    }

    @OnlyIn(Dist.CLIENT)
    public Component getName() {
        return this.name;
    }

    @OnlyIn(Dist.CLIENT)
    public float getPercent() {
        return this.pct;
    }

    @OnlyIn(Dist.CLIENT)
    public BossEvent.BossBarColor getColor() {
        return this.color;
    }

    @OnlyIn(Dist.CLIENT)
    public BossEvent.BossBarOverlay getOverlay() {
        return this.overlay;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean shouldDarkenScreen() {
        return this.darkenScreen;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean shouldPlayMusic() {
        return this.playMusic;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean shouldCreateWorldFog() {
        return this.createWorldFog;
    }

    public static enum Operation {
        ADD,
        REMOVE,
        UPDATE_PCT,
        UPDATE_NAME,
        UPDATE_STYLE,
        UPDATE_PROPERTIES;
    }
}
