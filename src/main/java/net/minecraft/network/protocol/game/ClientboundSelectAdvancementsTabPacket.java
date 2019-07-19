package net.minecraft.network.protocol.game;

import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundSelectAdvancementsTabPacket implements Packet<ClientGamePacketListener> {
    @Nullable
    private ResourceLocation tab;

    public ClientboundSelectAdvancementsTabPacket() {
    }

    public ClientboundSelectAdvancementsTabPacket(@Nullable ResourceLocation param0) {
        this.tab = param0;
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleSelectAdvancementsTab(this);
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        if (param0.readBoolean()) {
            this.tab = param0.readResourceLocation();
        }

    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeBoolean(this.tab != null);
        if (this.tab != null) {
            param0.writeResourceLocation(this.tab);
        }

    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public ResourceLocation getTab() {
        return this.tab;
    }
}
