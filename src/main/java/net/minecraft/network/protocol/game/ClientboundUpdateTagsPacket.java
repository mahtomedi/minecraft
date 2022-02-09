package net.minecraft.network.protocol.game;

import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagNetworkSerialization;

public class ClientboundUpdateTagsPacket implements Packet<ClientGamePacketListener> {
    private final Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> tags;

    public ClientboundUpdateTagsPacket(Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> param0) {
        this.tags = param0;
    }

    public ClientboundUpdateTagsPacket(FriendlyByteBuf param0) {
        this.tags = param0.readMap(param0x -> ResourceKey.createRegistryKey(param0x.readResourceLocation()), TagNetworkSerialization.NetworkPayload::read);
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeMap(this.tags, (param0x, param1) -> param0x.writeResourceLocation(param1.location()), (param0x, param1) -> param1.write(param0x));
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleUpdateTags(this);
    }

    public Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> getTags() {
        return this.tags;
    }
}
