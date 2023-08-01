package net.minecraft.network.protocol.common.custom;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record GoalDebugPayload(int entityId, BlockPos pos, List<GoalDebugPayload.DebugGoal> goals) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation("debug/goal_selector");

    public GoalDebugPayload(FriendlyByteBuf param0) {
        this(param0.readInt(), param0.readBlockPos(), param0.readList(GoalDebugPayload.DebugGoal::new));
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeInt(this.entityId);
        param0.writeBlockPos(this.pos);
        param0.writeCollection(this.goals, (param0x, param1) -> param1.write(param0x));
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static record DebugGoal(int priority, boolean isRunning, String name) {
        public DebugGoal(FriendlyByteBuf param0) {
            this(param0.readInt(), param0.readBoolean(), param0.readUtf(255));
        }

        public void write(FriendlyByteBuf param0) {
            param0.writeInt(this.priority);
            param0.writeBoolean(this.isRunning);
            param0.writeUtf(this.name);
        }
    }
}
