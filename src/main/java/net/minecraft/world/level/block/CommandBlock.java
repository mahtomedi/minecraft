package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandBlock extends BaseEntityBlock implements GameMasterBlock {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final DirectionProperty FACING = DirectionalBlock.FACING;
    public static final BooleanProperty CONDITIONAL = BlockStateProperties.CONDITIONAL;
    private final boolean automatic;

    public CommandBlock(BlockBehaviour.Properties param0, boolean param1) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(CONDITIONAL, Boolean.valueOf(false)));
        this.automatic = param1;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos param0, BlockState param1) {
        CommandBlockEntity var0 = new CommandBlockEntity(param0, param1);
        var0.setAutomatic(this.automatic);
        return var0;
    }

    @Override
    public void neighborChanged(BlockState param0, Level param1, BlockPos param2, Block param3, BlockPos param4, boolean param5) {
        if (!param1.isClientSide) {
            BlockEntity var0 = param1.getBlockEntity(param2);
            if (var0 instanceof CommandBlockEntity) {
                CommandBlockEntity var1 = (CommandBlockEntity)var0;
                boolean var2 = param1.hasNeighborSignal(param2);
                boolean var3 = var1.isPowered();
                var1.setPowered(var2);
                if (!var3 && !var1.isAutomatic() && var1.getMode() != CommandBlockEntity.Mode.SEQUENCE) {
                    if (var2) {
                        var1.markConditionMet();
                        param1.scheduleTick(param2, this, 1);
                    }

                }
            }
        }
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        BlockEntity var0 = param1.getBlockEntity(param2);
        if (var0 instanceof CommandBlockEntity var1) {
            BaseCommandBlock var2 = var1.getCommandBlock();
            boolean var3 = !StringUtil.isNullOrEmpty(var2.getCommand());
            CommandBlockEntity.Mode var4 = var1.getMode();
            boolean var5 = var1.wasConditionMet();
            if (var4 == CommandBlockEntity.Mode.AUTO) {
                var1.markConditionMet();
                if (var5) {
                    this.execute(param0, param1, param2, var2, var3);
                } else if (var1.isConditional()) {
                    var2.setSuccessCount(0);
                }

                if (var1.isPowered() || var1.isAutomatic()) {
                    param1.scheduleTick(param2, this, 1);
                }
            } else if (var4 == CommandBlockEntity.Mode.REDSTONE) {
                if (var5) {
                    this.execute(param0, param1, param2, var2, var3);
                } else if (var1.isConditional()) {
                    var2.setSuccessCount(0);
                }
            }

            param1.updateNeighbourForOutputSignal(param2, this);
        }

    }

    private void execute(BlockState param0, Level param1, BlockPos param2, BaseCommandBlock param3, boolean param4) {
        if (param4) {
            param3.performCommand(param1);
        } else {
            param3.setSuccessCount(0);
        }

        executeChain(param1, param2, param0.getValue(FACING));
    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        BlockEntity var0 = param1.getBlockEntity(param2);
        if (var0 instanceof CommandBlockEntity && param3.canUseGameMasterBlocks()) {
            param3.openCommandBlock((CommandBlockEntity)var0);
            return InteractionResult.sidedSuccess(param1.isClientSide);
        } else {
            return InteractionResult.PASS;
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState param0) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState param0, Level param1, BlockPos param2) {
        BlockEntity var0 = param1.getBlockEntity(param2);
        return var0 instanceof CommandBlockEntity ? ((CommandBlockEntity)var0).getCommandBlock().getSuccessCount() : 0;
    }

    @Override
    public void setPlacedBy(Level param0, BlockPos param1, BlockState param2, LivingEntity param3, ItemStack param4) {
        BlockEntity var0 = param0.getBlockEntity(param1);
        if (var0 instanceof CommandBlockEntity) {
            CommandBlockEntity var1 = (CommandBlockEntity)var0;
            BaseCommandBlock var2 = var1.getCommandBlock();
            if (param4.hasCustomHoverName()) {
                var2.setName(param4.getHoverName());
            }

            if (!param0.isClientSide) {
                if (BlockItem.getBlockEntityData(param4) == null) {
                    var2.setTrackOutput(param0.getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK));
                    var1.setAutomatic(this.automatic);
                }

                if (var1.getMode() == CommandBlockEntity.Mode.SEQUENCE) {
                    boolean var3 = param0.hasNeighborSignal(param1);
                    var1.setPowered(var3);
                }
            }

        }
    }

    @Override
    public RenderShape getRenderShape(BlockState param0) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState rotate(BlockState param0, Rotation param1) {
        return param0.setValue(FACING, param1.rotate(param0.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState param0, Mirror param1) {
        return param0.rotate(param1.getRotation(param0.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(FACING, CONDITIONAL);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        return this.defaultBlockState().setValue(FACING, param0.getNearestLookingDirection().getOpposite());
    }

    private static void executeChain(Level param0, BlockPos param1, Direction param2) {
        BlockPos.MutableBlockPos var0 = param1.mutable();
        GameRules var1 = param0.getGameRules();

        int var2;
        BlockState var3;
        for(var2 = var1.getInt(GameRules.RULE_MAX_COMMAND_CHAIN_LENGTH); var2-- > 0; param2 = var3.getValue(FACING)) {
            var0.move(param2);
            var3 = param0.getBlockState(var0);
            Block var4 = var3.getBlock();
            if (!var3.is(Blocks.CHAIN_COMMAND_BLOCK)) {
                break;
            }

            BlockEntity var5 = param0.getBlockEntity(var0);
            if (!(var5 instanceof CommandBlockEntity)) {
                break;
            }

            CommandBlockEntity var6 = (CommandBlockEntity)var5;
            if (var6.getMode() != CommandBlockEntity.Mode.SEQUENCE) {
                break;
            }

            if (var6.isPowered() || var6.isAutomatic()) {
                BaseCommandBlock var7 = var6.getCommandBlock();
                if (var6.markConditionMet()) {
                    if (!var7.performCommand(param0)) {
                        break;
                    }

                    param0.updateNeighbourForOutputSignal(var0, var4);
                } else if (var6.isConditional()) {
                    var7.setSuccessCount(0);
                }
            }
        }

        if (var2 <= 0) {
            int var8 = Math.max(var1.getInt(GameRules.RULE_MAX_COMMAND_CHAIN_LENGTH), 0);
            LOGGER.warn("Command Block chain tried to execute more than {} steps!", var8);
        }

    }
}
