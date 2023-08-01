package net.minecraft.network.protocol.common.custom;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public record BrainDebugPayload(BrainDebugPayload.BrainDump brainDump) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation("debug/brain");

    public BrainDebugPayload(FriendlyByteBuf param0) {
        this(new BrainDebugPayload.BrainDump(param0));
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        this.brainDump.write(param0);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static record BrainDump(
        UUID uuid,
        int id,
        String name,
        String profession,
        int xp,
        float health,
        float maxHealth,
        Vec3 pos,
        String inventory,
        @Nullable Path path,
        boolean wantsGolem,
        int angerLevel,
        List<String> activities,
        List<String> behaviors,
        List<String> memories,
        List<String> gossips,
        Set<BlockPos> pois,
        Set<BlockPos> potentialPois
    ) {
        public BrainDump(FriendlyByteBuf param0) {
            this(
                param0.readUUID(),
                param0.readInt(),
                param0.readUtf(),
                param0.readUtf(),
                param0.readInt(),
                param0.readFloat(),
                param0.readFloat(),
                param0.readVec3(),
                param0.readUtf(),
                param0.readNullable(Path::createFromStream),
                param0.readBoolean(),
                param0.readInt(),
                param0.readList(FriendlyByteBuf::readUtf),
                param0.readList(FriendlyByteBuf::readUtf),
                param0.readList(FriendlyByteBuf::readUtf),
                param0.readList(FriendlyByteBuf::readUtf),
                param0.readCollection(HashSet::new, FriendlyByteBuf::readBlockPos),
                param0.readCollection(HashSet::new, FriendlyByteBuf::readBlockPos)
            );
        }

        public void write(FriendlyByteBuf param0) {
            param0.writeUUID(this.uuid);
            param0.writeInt(this.id);
            param0.writeUtf(this.name);
            param0.writeUtf(this.profession);
            param0.writeInt(this.xp);
            param0.writeFloat(this.health);
            param0.writeFloat(this.maxHealth);
            param0.writeVec3(this.pos);
            param0.writeUtf(this.inventory);
            param0.writeNullable(this.path, (param0x, param1) -> param1.writeToStream(param0x));
            param0.writeBoolean(this.wantsGolem);
            param0.writeInt(this.angerLevel);
            param0.writeCollection(this.activities, FriendlyByteBuf::writeUtf);
            param0.writeCollection(this.behaviors, FriendlyByteBuf::writeUtf);
            param0.writeCollection(this.memories, FriendlyByteBuf::writeUtf);
            param0.writeCollection(this.gossips, FriendlyByteBuf::writeUtf);
            param0.writeCollection(this.pois, FriendlyByteBuf::writeBlockPos);
            param0.writeCollection(this.potentialPois, FriendlyByteBuf::writeBlockPos);
        }

        public boolean hasPoi(BlockPos param0) {
            return this.pois.contains(param0);
        }

        public boolean hasPotentialPoi(BlockPos param0) {
            return this.potentialPois.contains(param0);
        }
    }
}
