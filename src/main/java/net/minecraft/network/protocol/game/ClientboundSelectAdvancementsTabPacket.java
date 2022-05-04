package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public class ClientboundSelectAdvancementsTabPacket implements Packet<ClientGamePacketListener> {
    @Nullable
    private final ResourceLocation tab;

    public ClientboundSelectAdvancementsTabPacket(@Nullable ResourceLocation param0) {
        this.tab = param0;
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleSelectAdvancementsTab(this);
    }

    public ClientboundSelectAdvancementsTabPacket(FriendlyByteBuf param0) {
        this.tab = param0.readNullable(FriendlyByteBuf::readResourceLocation);
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeNullable(this.tab, FriendlyByteBuf::writeResourceLocation);
    }

    @Nullable
    public ResourceLocation getTab() {
        return this.tab;
    }
}
