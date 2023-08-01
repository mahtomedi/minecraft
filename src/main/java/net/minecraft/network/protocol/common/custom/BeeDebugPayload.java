package net.minecraft.network.protocol.common.custom;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.DebugEntityNameGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public record BeeDebugPayload(BeeDebugPayload.BeeInfo beeInfo) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation("debug/bee");

    public BeeDebugPayload(FriendlyByteBuf param0) {
        this(new BeeDebugPayload.BeeInfo(param0));
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        this.beeInfo.write(param0);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static record BeeInfo(
        UUID uuid,
        int id,
        Vec3 pos,
        @Nullable Path path,
        @Nullable BlockPos hivePos,
        @Nullable BlockPos flowerPos,
        int travelTicks,
        Set<String> goals,
        List<BlockPos> blacklistedHives
    ) {
        public BeeInfo(FriendlyByteBuf param0) {
            this(
                param0.readUUID(),
                param0.readInt(),
                param0.readVec3(),
                param0.readNullable(Path::createFromStream),
                param0.readNullable(FriendlyByteBuf::readBlockPos),
                param0.readNullable(FriendlyByteBuf::readBlockPos),
                param0.readInt(),
                param0.readCollection(HashSet::new, FriendlyByteBuf::readUtf),
                param0.readList(FriendlyByteBuf::readBlockPos)
            );
        }

        public void write(FriendlyByteBuf param0) {
            param0.writeUUID(this.uuid);
            param0.writeInt(this.id);
            param0.writeVec3(this.pos);
            param0.writeNullable(this.path, (param0x, param1) -> param1.writeToStream(param0x));
            param0.writeNullable(this.hivePos, FriendlyByteBuf::writeBlockPos);
            param0.writeNullable(this.flowerPos, FriendlyByteBuf::writeBlockPos);
            param0.writeInt(this.travelTicks);
            param0.writeCollection(this.goals, FriendlyByteBuf::writeUtf);
            param0.writeCollection(this.blacklistedHives, FriendlyByteBuf::writeBlockPos);
        }

        public boolean hasHive(BlockPos param0) {
            return Objects.equals(param0, this.hivePos);
        }

        public String generateName() {
            return DebugEntityNameGenerator.getEntityName(this.uuid);
        }

        @Override
        public String toString() {
            return this.generateName();
        }
    }
}
