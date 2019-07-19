package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.tags.TagManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundUpdateTagsPacket implements Packet<ClientGamePacketListener> {
    private TagManager tags;

    public ClientboundUpdateTagsPacket() {
    }

    public ClientboundUpdateTagsPacket(TagManager param0) {
        this.tags = param0;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.tags = TagManager.deserializeFromNetwork(param0);
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        this.tags.serializeToNetwork(param0);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleUpdateTags(this);
    }

    @OnlyIn(Dist.CLIENT)
    public TagManager getTags() {
        return this.tags;
    }
}
