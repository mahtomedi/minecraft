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
        if (param0.readBoolean()) {
            this.tab = param0.readResourceLocation();
        } else {
            this.tab = null;
        }

    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeBoolean(this.tab != null);
        if (this.tab != null) {
            param0.writeResourceLocation(this.tab);
        }

    }

    @Nullable
    public ResourceLocation getTab() {
        return this.tab;
    }
}
