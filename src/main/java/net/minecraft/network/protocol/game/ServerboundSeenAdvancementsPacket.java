package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public class ServerboundSeenAdvancementsPacket implements Packet<ServerGamePacketListener> {
    private final ServerboundSeenAdvancementsPacket.Action action;
    @Nullable
    private final ResourceLocation tab;

    public ServerboundSeenAdvancementsPacket(ServerboundSeenAdvancementsPacket.Action param0, @Nullable ResourceLocation param1) {
        this.action = param0;
        this.tab = param1;
    }

    public static ServerboundSeenAdvancementsPacket openedTab(AdvancementHolder param0) {
        return new ServerboundSeenAdvancementsPacket(ServerboundSeenAdvancementsPacket.Action.OPENED_TAB, param0.id());
    }

    public static ServerboundSeenAdvancementsPacket closedScreen() {
        return new ServerboundSeenAdvancementsPacket(ServerboundSeenAdvancementsPacket.Action.CLOSED_SCREEN, null);
    }

    public ServerboundSeenAdvancementsPacket(FriendlyByteBuf param0) {
        this.action = param0.readEnum(ServerboundSeenAdvancementsPacket.Action.class);
        if (this.action == ServerboundSeenAdvancementsPacket.Action.OPENED_TAB) {
            this.tab = param0.readResourceLocation();
        } else {
            this.tab = null;
        }

    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeEnum(this.action);
        if (this.action == ServerboundSeenAdvancementsPacket.Action.OPENED_TAB) {
            param0.writeResourceLocation(this.tab);
        }

    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleSeenAdvancements(this);
    }

    public ServerboundSeenAdvancementsPacket.Action getAction() {
        return this.action;
    }

    @Nullable
    public ResourceLocation getTab() {
        return this.tab;
    }

    public static enum Action {
        OPENED_TAB,
        CLOSED_SCREEN;
    }
}
