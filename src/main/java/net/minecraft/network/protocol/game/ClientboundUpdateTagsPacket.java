package net.minecraft.network.protocol.game;

import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagCollection;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundUpdateTagsPacket implements Packet<ClientGamePacketListener> {
    private Map<ResourceKey<? extends Registry<?>>, TagCollection.NetworkPayload> tags;

    public ClientboundUpdateTagsPacket() {
    }

    public ClientboundUpdateTagsPacket(Map<ResourceKey<? extends Registry<?>>, TagCollection.NetworkPayload> param0) {
        this.tags = param0;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        int var0 = param0.readVarInt();
        this.tags = Maps.newHashMapWithExpectedSize(var0);

        for(int var1 = 0; var1 < var0; ++var1) {
            ResourceLocation var2 = param0.readResourceLocation();
            ResourceKey<? extends Registry<?>> var3 = ResourceKey.createRegistryKey(var2);
            TagCollection.NetworkPayload var4 = TagCollection.NetworkPayload.read(param0);
            this.tags.put(var3, var4);
        }

    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeVarInt(this.tags.size());
        this.tags.forEach((param1, param2) -> {
            param0.writeResourceLocation(param1.location());
            param2.write(param0);
        });
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleUpdateTags(this);
    }

    @OnlyIn(Dist.CLIENT)
    public Map<ResourceKey<? extends Registry<?>>, TagCollection.NetworkPayload> getTags() {
        return this.tags;
    }
}
