package net.minecraft.network.protocol.common.custom;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.DebugEntityNameGenerator;
import net.minecraft.resources.ResourceLocation;

public record BreezeDebugPayload(BreezeDebugPayload.BreezeInfo breezeInfo) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation("debug/breeze");

    public BreezeDebugPayload(FriendlyByteBuf param0) {
        this(new BreezeDebugPayload.BreezeInfo(param0));
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        this.breezeInfo.write(param0);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static record BreezeInfo(UUID uuid, int id, Integer attackTarget, BlockPos jumpTarget) {
        public BreezeInfo(FriendlyByteBuf param0) {
            this(param0.readUUID(), param0.readInt(), param0.readNullable(FriendlyByteBuf::readInt), param0.readNullable(FriendlyByteBuf::readBlockPos));
        }

        public void write(FriendlyByteBuf param0) {
            param0.writeUUID(this.uuid);
            param0.writeInt(this.id);
            param0.writeNullable(this.attackTarget, FriendlyByteBuf::writeInt);
            param0.writeNullable(this.jumpTarget, FriendlyByteBuf::writeBlockPos);
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
