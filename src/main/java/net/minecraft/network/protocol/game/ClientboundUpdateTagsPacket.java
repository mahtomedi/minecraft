package net.minecraft.network.protocol.game;

import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagCollection;

public class ClientboundUpdateTagsPacket implements Packet<ClientGamePacketListener> {
    private final Map<ResourceKey<? extends Registry<?>>, TagCollection.NetworkPayload> tags;

    public ClientboundUpdateTagsPacket(Map<ResourceKey<? extends Registry<?>>, TagCollection.NetworkPayload> param0) {
        this.tags = param0;
    }

    public ClientboundUpdateTagsPacket(FriendlyByteBuf param0) {
        this.tags = param0.readMap(param0x -> ResourceKey.createRegistryKey(param0x.readResourceLocation()), TagCollection.NetworkPayload::read);
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeMap(this.tags, (param0x, param1) -> param0x.writeResourceLocation(param1.location()), (param0x, param1) -> param1.write(param0x));
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleUpdateTags(this);
    }

    public Map<ResourceKey<? extends Registry<?>>, TagCollection.NetworkPayload> getTags() {
        return this.tags;
    }
}
