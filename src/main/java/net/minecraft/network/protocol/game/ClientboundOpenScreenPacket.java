package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.inventory.MenuType;

public class ClientboundOpenScreenPacket implements Packet<ClientGamePacketListener> {
    private final int containerId;
    private final MenuType<?> type;
    private final Component title;

    public ClientboundOpenScreenPacket(int param0, MenuType<?> param1, Component param2) {
        this.containerId = param0;
        this.type = param1;
        this.title = param2;
    }

    public ClientboundOpenScreenPacket(FriendlyByteBuf param0) {
        this.containerId = param0.readVarInt();
        this.type = param0.readById(BuiltInRegistries.MENU);
        this.title = param0.readComponentTrusted();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.containerId);
        param0.writeId(BuiltInRegistries.MENU, this.type);
        param0.writeComponent(this.title);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleOpenScreen(this);
    }

    public int getContainerId() {
        return this.containerId;
    }

    @Nullable
    public MenuType<?> getType() {
        return this.type;
    }

    public Component getTitle() {
        return this.title;
    }
}
