package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.entity.LivingEntity;

public class ClientboundPlayerCombatPacket implements Packet<ClientGamePacketListener> {
    public ClientboundPlayerCombatPacket.Event event;
    public int playerId;
    public int killerId;
    public int duration;
    public Component message;

    public ClientboundPlayerCombatPacket() {
    }

    public ClientboundPlayerCombatPacket(CombatTracker param0, ClientboundPlayerCombatPacket.Event param1) {
        this(param0, param1, TextComponent.EMPTY);
    }

    public ClientboundPlayerCombatPacket(CombatTracker param0, ClientboundPlayerCombatPacket.Event param1, Component param2) {
        this.event = param1;
        LivingEntity var0 = param0.getKiller();
        switch(param1) {
            case END_COMBAT:
                this.duration = param0.getCombatDuration();
                this.killerId = var0 == null ? -1 : var0.getId();
                break;
            case ENTITY_DIED:
                this.playerId = param0.getMob().getId();
                this.killerId = var0 == null ? -1 : var0.getId();
                this.message = param2;
        }

    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.event = param0.readEnum(ClientboundPlayerCombatPacket.Event.class);
        if (this.event == ClientboundPlayerCombatPacket.Event.END_COMBAT) {
            this.duration = param0.readVarInt();
            this.killerId = param0.readInt();
        } else if (this.event == ClientboundPlayerCombatPacket.Event.ENTITY_DIED) {
            this.playerId = param0.readVarInt();
            this.killerId = param0.readInt();
            this.message = param0.readComponent();
        }

    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeEnum(this.event);
        if (this.event == ClientboundPlayerCombatPacket.Event.END_COMBAT) {
            param0.writeVarInt(this.duration);
            param0.writeInt(this.killerId);
        } else if (this.event == ClientboundPlayerCombatPacket.Event.ENTITY_DIED) {
            param0.writeVarInt(this.playerId);
            param0.writeInt(this.killerId);
            param0.writeComponent(this.message);
        }

    }

    public void handle(ClientGamePacketListener param0) {
        param0.handlePlayerCombat(this);
    }

    @Override
    public boolean isSkippable() {
        return this.event == ClientboundPlayerCombatPacket.Event.ENTITY_DIED;
    }

    public static enum Event {
        ENTER_COMBAT,
        END_COMBAT,
        ENTITY_DIED;
    }
}
