package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class CommandBlockEntity extends BlockEntity {
    private boolean powered;
    private boolean auto;
    private boolean conditionMet;
    private boolean sendToClient;
    private final BaseCommandBlock commandBlock = new BaseCommandBlock() {
        @Override
        public void setCommand(String param0) {
            super.setCommand(param0);
            CommandBlockEntity.this.setChanged();
        }

        @Override
        public ServerLevel getLevel() {
            return (ServerLevel)CommandBlockEntity.this.level;
        }

        @Override
        public void onUpdated() {
            BlockState var0 = CommandBlockEntity.this.level.getBlockState(CommandBlockEntity.this.worldPosition);
            this.getLevel().sendBlockUpdated(CommandBlockEntity.this.worldPosition, var0, var0, 3);
        }

        @Override
        public Vec3 getPosition() {
            return Vec3.atCenterOf(CommandBlockEntity.this.worldPosition);
        }

        @Override
        public CommandSourceStack createCommandSourceStack() {
            return new CommandSourceStack(
                this,
                Vec3.atCenterOf(CommandBlockEntity.this.worldPosition),
                Vec2.ZERO,
                this.getLevel(),
                2,
                this.getName().getString(),
                this.getName(),
                this.getLevel().getServer(),
                null
            );
        }
    };

    public CommandBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.COMMAND_BLOCK, param0, param1);
    }

    @Override
    protected void saveAdditional(CompoundTag param0) {
        super.saveAdditional(param0);
        this.commandBlock.save(param0);
        param0.putBoolean("powered", this.isPowered());
        param0.putBoolean("conditionMet", this.wasConditionMet());
        param0.putBoolean("auto", this.isAutomatic());
    }

    @Override
    public void load(CompoundTag param0) {
        super.load(param0);
        this.commandBlock.load(param0);
        this.powered = param0.getBoolean("powered");
        this.conditionMet = param0.getBoolean("conditionMet");
        this.setAutomatic(param0.getBoolean("auto"));
    }

    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        if (this.isSendToClient()) {
            this.setSendToClient(false);
            return ClientboundBlockEntityDataPacket.create(this);
        } else {
            return null;
        }
    }

    @Override
    public boolean onlyOpCanSetNbt() {
        return true;
    }

    public BaseCommandBlock getCommandBlock() {
        return this.commandBlock;
    }

    public void setPowered(boolean param0) {
        this.powered = param0;
    }

    public boolean isPowered() {
        return this.powered;
    }

    public boolean isAutomatic() {
        return this.auto;
    }

    public void setAutomatic(boolean param0) {
        boolean var0 = this.auto;
        this.auto = param0;
        if (!var0 && param0 && !this.powered && this.level != null && this.getMode() != CommandBlockEntity.Mode.SEQUENCE) {
            this.scheduleTick();
        }

    }

    public void onModeSwitch() {
        CommandBlockEntity.Mode var0 = this.getMode();
        if (var0 == CommandBlockEntity.Mode.AUTO && (this.powered || this.auto) && this.level != null) {
            this.scheduleTick();
        }

    }

    private void scheduleTick() {
        Block var0 = this.getBlockState().getBlock();
        if (var0 instanceof CommandBlock) {
            this.markConditionMet();
            this.level.getBlockTicks().scheduleTick(this.worldPosition, var0, 1);
        }

    }

    public boolean wasConditionMet() {
        return this.conditionMet;
    }

    public boolean markConditionMet() {
        this.conditionMet = true;
        if (this.isConditional()) {
            BlockPos var0 = this.worldPosition.relative(this.level.getBlockState(this.worldPosition).getValue(CommandBlock.FACING).getOpposite());
            if (this.level.getBlockState(var0).getBlock() instanceof CommandBlock) {
                BlockEntity var1 = this.level.getBlockEntity(var0);
                this.conditionMet = var1 instanceof CommandBlockEntity && ((CommandBlockEntity)var1).getCommandBlock().getSuccessCount() > 0;
            } else {
                this.conditionMet = false;
            }
        }

        return this.conditionMet;
    }

    public boolean isSendToClient() {
        return this.sendToClient;
    }

    public void setSendToClient(boolean param0) {
        this.sendToClient = param0;
    }

    public CommandBlockEntity.Mode getMode() {
        BlockState var0 = this.getBlockState();
        if (var0.is(Blocks.COMMAND_BLOCK)) {
            return CommandBlockEntity.Mode.REDSTONE;
        } else if (var0.is(Blocks.REPEATING_COMMAND_BLOCK)) {
            return CommandBlockEntity.Mode.AUTO;
        } else {
            return var0.is(Blocks.CHAIN_COMMAND_BLOCK) ? CommandBlockEntity.Mode.SEQUENCE : CommandBlockEntity.Mode.REDSTONE;
        }
    }

    public boolean isConditional() {
        BlockState var0 = this.level.getBlockState(this.getBlockPos());
        return var0.getBlock() instanceof CommandBlock ? var0.getValue(CommandBlock.CONDITIONAL) : false;
    }

    public static enum Mode {
        SEQUENCE,
        AUTO,
        REDSTONE;
    }
}
