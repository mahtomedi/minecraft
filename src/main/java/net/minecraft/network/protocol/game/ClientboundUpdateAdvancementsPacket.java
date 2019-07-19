package net.minecraft.network.protocol.game;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundUpdateAdvancementsPacket implements Packet<ClientGamePacketListener> {
    private boolean reset;
    private Map<ResourceLocation, Advancement.Builder> added;
    private Set<ResourceLocation> removed;
    private Map<ResourceLocation, AdvancementProgress> progress;

    public ClientboundUpdateAdvancementsPacket() {
    }

    public ClientboundUpdateAdvancementsPacket(
        boolean param0, Collection<Advancement> param1, Set<ResourceLocation> param2, Map<ResourceLocation, AdvancementProgress> param3
    ) {
        this.reset = param0;
        this.added = Maps.newHashMap();

        for(Advancement var0 : param1) {
            this.added.put(var0.getId(), var0.deconstruct());
        }

        this.removed = param2;
        this.progress = Maps.newHashMap(param3);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleUpdateAdvancementsPacket(this);
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.reset = param0.readBoolean();
        this.added = Maps.newHashMap();
        this.removed = Sets.newLinkedHashSet();
        this.progress = Maps.newHashMap();
        int var0 = param0.readVarInt();

        for(int var1 = 0; var1 < var0; ++var1) {
            ResourceLocation var2 = param0.readResourceLocation();
            Advancement.Builder var3 = Advancement.Builder.fromNetwork(param0);
            this.added.put(var2, var3);
        }

        var0 = param0.readVarInt();

        for(int var4 = 0; var4 < var0; ++var4) {
            ResourceLocation var5 = param0.readResourceLocation();
            this.removed.add(var5);
        }

        var0 = param0.readVarInt();

        for(int var6 = 0; var6 < var0; ++var6) {
            ResourceLocation var7 = param0.readResourceLocation();
            this.progress.put(var7, AdvancementProgress.fromNetwork(param0));
        }

    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeBoolean(this.reset);
        param0.writeVarInt(this.added.size());

        for(Entry<ResourceLocation, Advancement.Builder> var0 : this.added.entrySet()) {
            ResourceLocation var1 = var0.getKey();
            Advancement.Builder var2 = var0.getValue();
            param0.writeResourceLocation(var1);
            var2.serializeToNetwork(param0);
        }

        param0.writeVarInt(this.removed.size());

        for(ResourceLocation var3 : this.removed) {
            param0.writeResourceLocation(var3);
        }

        param0.writeVarInt(this.progress.size());

        for(Entry<ResourceLocation, AdvancementProgress> var4 : this.progress.entrySet()) {
            param0.writeResourceLocation(var4.getKey());
            var4.getValue().serializeToNetwork(param0);
        }

    }

    @OnlyIn(Dist.CLIENT)
    public Map<ResourceLocation, Advancement.Builder> getAdded() {
        return this.added;
    }

    @OnlyIn(Dist.CLIENT)
    public Set<ResourceLocation> getRemoved() {
        return this.removed;
    }

    @OnlyIn(Dist.CLIENT)
    public Map<ResourceLocation, AdvancementProgress> getProgress() {
        return this.progress;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean shouldReset() {
        return this.reset;
    }
}
