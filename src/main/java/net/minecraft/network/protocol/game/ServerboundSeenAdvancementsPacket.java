package net.minecraft.network.protocol.game;

import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerboundSeenAdvancementsPacket implements Packet<ServerGamePacketListener> {
    private ServerboundSeenAdvancementsPacket.Action action;
    private ResourceLocation tab;

    public ServerboundSeenAdvancementsPacket() {
    }

    @OnlyIn(Dist.CLIENT)
    public ServerboundSeenAdvancementsPacket(ServerboundSeenAdvancementsPacket.Action param0, @Nullable ResourceLocation param1) {
        this.action = param0;
        this.tab = param1;
    }

    @OnlyIn(Dist.CLIENT)
    public static ServerboundSeenAdvancementsPacket openedTab(Advancement param0) {
        return new ServerboundSeenAdvancementsPacket(ServerboundSeenAdvancementsPacket.Action.OPENED_TAB, param0.getId());
    }

    @OnlyIn(Dist.CLIENT)
    public static ServerboundSeenAdvancementsPacket closedScreen() {
        return new ServerboundSeenAdvancementsPacket(ServerboundSeenAdvancementsPacket.Action.CLOSED_SCREEN, null);
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.action = param0.readEnum(ServerboundSeenAdvancementsPacket.Action.class);
        if (this.action == ServerboundSeenAdvancementsPacket.Action.OPENED_TAB) {
            this.tab = param0.readResourceLocation();
        }

    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
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

    public ResourceLocation getTab() {
        return this.tab;
    }

    public static enum Action {
        OPENED_TAB,
        CLOSED_SCREEN;
    }
}
