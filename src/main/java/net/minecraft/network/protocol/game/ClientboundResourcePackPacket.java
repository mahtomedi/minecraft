package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundResourcePackPacket implements Packet<ClientGamePacketListener> {
    private final String url;
    private final String hash;
    private final boolean required;

    public ClientboundResourcePackPacket(String param0, String param1, boolean param2) {
        if (param1.length() > 40) {
            throw new IllegalArgumentException("Hash is too long (max 40, was " + param1.length() + ")");
        } else {
            this.url = param0;
            this.hash = param1;
            this.required = param2;
        }
    }

    public ClientboundResourcePackPacket(FriendlyByteBuf param0) {
        this.url = param0.readUtf();
        this.hash = param0.readUtf(40);
        this.required = param0.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeUtf(this.url);
        param0.writeUtf(this.hash);
        param0.writeBoolean(this.required);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleResourcePack(this);
    }

    @OnlyIn(Dist.CLIENT)
    public String getUrl() {
        return this.url;
    }

    @OnlyIn(Dist.CLIENT)
    public String getHash() {
        return this.hash;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isRequired() {
        return this.required;
    }
}
