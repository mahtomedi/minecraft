package net.minecraft.network.protocol.game;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public class ClientboundUpdateAdvancementsPacket implements Packet<ClientGamePacketListener> {
    private final boolean reset;
    private final List<AdvancementHolder> added;
    private final Set<ResourceLocation> removed;
    private final Map<ResourceLocation, AdvancementProgress> progress;

    public ClientboundUpdateAdvancementsPacket(
        boolean param0, Collection<AdvancementHolder> param1, Set<ResourceLocation> param2, Map<ResourceLocation, AdvancementProgress> param3
    ) {
        this.reset = param0;
        this.added = List.copyOf(param1);
        this.removed = Set.copyOf(param2);
        this.progress = Map.copyOf(param3);
    }

    public ClientboundUpdateAdvancementsPacket(FriendlyByteBuf param0) {
        this.reset = param0.readBoolean();
        this.added = param0.readList(AdvancementHolder::read);
        this.removed = param0.readCollection(Sets::newLinkedHashSetWithExpectedSize, FriendlyByteBuf::readResourceLocation);
        this.progress = param0.readMap(FriendlyByteBuf::readResourceLocation, AdvancementProgress::fromNetwork);
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeBoolean(this.reset);
        param0.writeCollection(this.added, (param0x, param1) -> param1.write(param0x));
        param0.writeCollection(this.removed, FriendlyByteBuf::writeResourceLocation);
        param0.writeMap(this.progress, FriendlyByteBuf::writeResourceLocation, (param0x, param1) -> param1.serializeToNetwork(param0x));
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleUpdateAdvancementsPacket(this);
    }

    public List<AdvancementHolder> getAdded() {
        return this.added;
    }

    public Set<ResourceLocation> getRemoved() {
        return this.removed;
    }

    public Map<ResourceLocation, AdvancementProgress> getProgress() {
        return this.progress;
    }

    public boolean shouldReset() {
        return this.reset;
    }
}
