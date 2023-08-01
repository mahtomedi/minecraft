package net.minecraft.network.protocol.common.custom;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record VillageSectionsDebugPayload(Set<SectionPos> villageChunks, Set<SectionPos> notVillageChunks) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation("debug/village_sections");

    public VillageSectionsDebugPayload(FriendlyByteBuf param0) {
        this(param0.readCollection(HashSet::new, FriendlyByteBuf::readSectionPos), param0.readCollection(HashSet::new, FriendlyByteBuf::readSectionPos));
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeCollection(this.villageChunks, FriendlyByteBuf::writeSectionPos);
        param0.writeCollection(this.notVillageChunks, FriendlyByteBuf::writeSectionPos);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
