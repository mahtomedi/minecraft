package net.minecraft.world.entity.vehicle;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class MinecartCommandBlock extends AbstractMinecart {
    static final EntityDataAccessor<String> DATA_ID_COMMAND_NAME = SynchedEntityData.defineId(MinecartCommandBlock.class, EntityDataSerializers.STRING);
    static final EntityDataAccessor<Component> DATA_ID_LAST_OUTPUT = SynchedEntityData.defineId(MinecartCommandBlock.class, EntityDataSerializers.COMPONENT);
    private final BaseCommandBlock commandBlock = new MinecartCommandBlock.MinecartCommandBase();
    private static final int ACTIVATION_DELAY = 4;
    private int lastActivated;

    public MinecartCommandBlock(EntityType<? extends MinecartCommandBlock> param0, Level param1) {
        super(param0, param1);
    }

    public MinecartCommandBlock(Level param0, double param1, double param2, double param3) {
        super(EntityType.COMMAND_BLOCK_MINECART, param0, param1, param2, param3);
    }

    @Override
    protected Item getDropItem() {
        return Items.MINECART;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.getEntityData().define(DATA_ID_COMMAND_NAME, "");
        this.getEntityData().define(DATA_ID_LAST_OUTPUT, CommonComponents.EMPTY);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.commandBlock.load(param0);
        this.getEntityData().set(DATA_ID_COMMAND_NAME, this.getCommandBlock().getCommand());
        this.getEntityData().set(DATA_ID_LAST_OUTPUT, this.getCommandBlock().getLastOutput());
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        this.commandBlock.save(param0);
    }

    @Override
    public AbstractMinecart.Type getMinecartType() {
        return AbstractMinecart.Type.COMMAND_BLOCK;
    }

    @Override
    public BlockState getDefaultDisplayBlockState() {
        return Blocks.COMMAND_BLOCK.defaultBlockState();
    }

    public BaseCommandBlock getCommandBlock() {
        return this.commandBlock;
    }

    @Override
    public void activateMinecart(int param0, int param1, int param2, boolean param3) {
        if (param3 && this.tickCount - this.lastActivated >= 4) {
            this.getCommandBlock().performCommand(this.level());
            this.lastActivated = this.tickCount;
        }

    }

    @Override
    public InteractionResult interact(Player param0, InteractionHand param1) {
        return this.commandBlock.usedBy(param0);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> param0) {
        super.onSyncedDataUpdated(param0);
        if (DATA_ID_LAST_OUTPUT.equals(param0)) {
            try {
                this.commandBlock.setLastOutput(this.getEntityData().get(DATA_ID_LAST_OUTPUT));
            } catch (Throwable var3) {
            }
        } else if (DATA_ID_COMMAND_NAME.equals(param0)) {
            this.commandBlock.setCommand(this.getEntityData().get(DATA_ID_COMMAND_NAME));
        }

    }

    @Override
    public boolean onlyOpCanSetNbt() {
        return true;
    }

    public class MinecartCommandBase extends BaseCommandBlock {
        @Override
        public ServerLevel getLevel() {
            return (ServerLevel)MinecartCommandBlock.this.level();
        }

        @Override
        public void onUpdated() {
            MinecartCommandBlock.this.getEntityData().set(MinecartCommandBlock.DATA_ID_COMMAND_NAME, this.getCommand());
            MinecartCommandBlock.this.getEntityData().set(MinecartCommandBlock.DATA_ID_LAST_OUTPUT, this.getLastOutput());
        }

        @Override
        public Vec3 getPosition() {
            return MinecartCommandBlock.this.position();
        }

        public MinecartCommandBlock getMinecart() {
            return MinecartCommandBlock.this;
        }

        @Override
        public CommandSourceStack createCommandSourceStack() {
            return new CommandSourceStack(
                this,
                MinecartCommandBlock.this.position(),
                MinecartCommandBlock.this.getRotationVector(),
                this.getLevel(),
                2,
                this.getName().getString(),
                MinecartCommandBlock.this.getDisplayName(),
                this.getLevel().getServer(),
                MinecartCommandBlock.this
            );
        }
    }
}
