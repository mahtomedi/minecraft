package net.minecraft.network.protocol.configuration;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public record ClientboundUpdateEnabledFeaturesPacket(Set<ResourceLocation> features) implements Packet<ClientConfigurationPacketListener> {
    public ClientboundUpdateEnabledFeaturesPacket(FriendlyByteBuf param0) {
        this(param0.readCollection(HashSet::new, FriendlyByteBuf::readResourceLocation));
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeCollection(this.features, FriendlyByteBuf::writeResourceLocation);
    }

    public void handle(ClientConfigurationPacketListener param0) {
        param0.handleEnabledFeatures(this);
    }
}
