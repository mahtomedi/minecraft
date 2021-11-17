package net.minecraft.server.level;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockBreakAckPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerPlayerGameMode {
    private static final Logger LOGGER = LogManager.getLogger();
    protected ServerLevel level;
    protected final ServerPlayer player;
    private GameType gameModeForPlayer = GameType.DEFAULT_MODE;
    @Nullable
    private GameType previousGameModeForPlayer;
    private boolean isDestroyingBlock;
    private int destroyProgressStart;
    private BlockPos destroyPos = BlockPos.ZERO;
    private int gameTicks;
    private boolean hasDelayedDestroy;
    private BlockPos delayedDestroyPos = BlockPos.ZERO;
    private int delayedTickStart;
    private int lastSentState = -1;

    public ServerPlayerGameMode(ServerPlayer param0) {
        this.player = param0;
        this.level = param0.getLevel();
    }

    public boolean changeGameModeForPlayer(GameType param0) {
        if (param0 == this.gameModeForPlayer) {
            return false;
        } else {
            this.setGameModeForPlayer(param0, this.gameModeForPlayer);
            return true;
        }
    }

    protected void setGameModeForPlayer(GameType param0, @Nullable GameType param1) {
        this.previousGameModeForPlayer = param1;
        this.gameModeForPlayer = param0;
        param0.updatePlayerAbilities(this.player.getAbilities());
        this.player.onUpdateAbilities();
        this.player.server.getPlayerList().broadcastAll(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.UPDATE_GAME_MODE, this.player));
        this.level.updateSleepingPlayerList();
    }

    public GameType getGameModeForPlayer() {
        return this.gameModeForPlayer;
    }

    @Nullable
    public GameType getPreviousGameModeForPlayer() {
        return this.previousGameModeForPlayer;
    }

    public boolean isSurvival() {
        return this.gameModeForPlayer.isSurvival();
    }

    public boolean isCreative() {
        return this.gameModeForPlayer.isCreative();
    }

    public void tick() {
        ++this.gameTicks;
        if (this.hasDelayedDestroy) {
            BlockState var0 = this.level.getBlockState(this.delayedDestroyPos);
            if (var0.isAir()) {
                this.hasDelayedDestroy = false;
            } else {
                float var1 = this.incrementDestroyProgress(var0, this.delayedDestroyPos, this.delayedTickStart);
                if (var1 >= 1.0F) {
                    this.hasDelayedDestroy = false;
                    this.destroyBlock(this.delayedDestroyPos);
                }
            }
        } else if (this.isDestroyingBlock) {
            BlockState var2 = this.level.getBlockState(this.destroyPos);
            if (var2.isAir()) {
                this.level.destroyBlockProgress(this.player.getId(), this.destroyPos, -1);
                this.lastSentState = -1;
                this.isDestroyingBlock = false;
            } else {
                this.incrementDestroyProgress(var2, this.destroyPos, this.destroyProgressStart);
            }
        }

    }

    private float incrementDestroyProgress(BlockState param0, BlockPos param1, int param2) {
        int var0 = this.gameTicks - param2;
        float var1 = param0.getDestroyProgress(this.player, this.player.level, param1) * (float)(var0 + 1);
        int var2 = (int)(var1 * 10.0F);
        if (var2 != this.lastSentState) {
            this.level.destroyBlockProgress(this.player.getId(), param1, var2);
            this.lastSentState = var2;
        }

        return var1;
    }

    public void handleBlockBreakAction(BlockPos param0, ServerboundPlayerActionPacket.Action param1, Direction param2, int param3) {
        double var0 = this.player.getX() - ((double)param0.getX() + 0.5);
        double var1 = this.player.getY() - ((double)param0.getY() + 0.5) + 1.5;
        double var2 = this.player.getZ() - ((double)param0.getZ() + 0.5);
        double var3 = var0 * var0 + var1 * var1 + var2 * var2;
        if (var3 > 36.0) {
            this.player.connection.send(new ClientboundBlockBreakAckPacket(param0, this.level.getBlockState(param0), param1, false, "too far"));
        } else if (param0.getY() >= param3) {
            this.player.connection.send(new ClientboundBlockBreakAckPacket(param0, this.level.getBlockState(param0), param1, false, "too high"));
        } else {
            if (param1 == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
                if (!this.level.mayInteract(this.player, param0)) {
                    this.player
                        .connection
                        .send(new ClientboundBlockBreakAckPacket(param0, this.level.getBlockState(param0), param1, false, "may not interact"));
                    return;
                }

                if (this.isCreative()) {
                    this.destroyAndAck(param0, param1, "creative destroy");
                    return;
                }

                if (this.player.blockActionRestricted(this.level, param0, this.gameModeForPlayer)) {
                    this.player
                        .connection
                        .send(new ClientboundBlockBreakAckPacket(param0, this.level.getBlockState(param0), param1, false, "block action restricted"));
                    return;
                }

                this.destroyProgressStart = this.gameTicks;
                float var4 = 1.0F;
                BlockState var5 = this.level.getBlockState(param0);
                if (!var5.isAir()) {
                    var5.attack(this.level, param0, this.player);
                    var4 = var5.getDestroyProgress(this.player, this.player.level, param0);
                }

                if (!var5.isAir() && var4 >= 1.0F) {
                    this.destroyAndAck(param0, param1, "insta mine");
                } else {
                    if (this.isDestroyingBlock) {
                        this.player
                            .connection
                            .send(
                                new ClientboundBlockBreakAckPacket(
                                    this.destroyPos,
                                    this.level.getBlockState(this.destroyPos),
                                    ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK,
                                    false,
                                    "abort destroying since another started (client insta mine, server disagreed)"
                                )
                            );
                    }

                    this.isDestroyingBlock = true;
                    this.destroyPos = param0.immutable();
                    int var6 = (int)(var4 * 10.0F);
                    this.level.destroyBlockProgress(this.player.getId(), param0, var6);
                    this.player
                        .connection
                        .send(new ClientboundBlockBreakAckPacket(param0, this.level.getBlockState(param0), param1, true, "actual start of destroying"));
                    this.lastSentState = var6;
                }
            } else if (param1 == ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK) {
                if (param0.equals(this.destroyPos)) {
                    int var7 = this.gameTicks - this.destroyProgressStart;
                    BlockState var8 = this.level.getBlockState(param0);
                    if (!var8.isAir()) {
                        float var9 = var8.getDestroyProgress(this.player, this.player.level, param0) * (float)(var7 + 1);
                        if (var9 >= 0.7F) {
                            this.isDestroyingBlock = false;
                            this.level.destroyBlockProgress(this.player.getId(), param0, -1);
                            this.destroyAndAck(param0, param1, "destroyed");
                            return;
                        }

                        if (!this.hasDelayedDestroy) {
                            this.isDestroyingBlock = false;
                            this.hasDelayedDestroy = true;
                            this.delayedDestroyPos = param0;
                            this.delayedTickStart = this.destroyProgressStart;
                        }
                    }
                }

                this.player.connection.send(new ClientboundBlockBreakAckPacket(param0, this.level.getBlockState(param0), param1, true, "stopped destroying"));
            } else if (param1 == ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK) {
                this.isDestroyingBlock = false;
                if (!Objects.equals(this.destroyPos, param0)) {
                    LOGGER.warn("Mismatch in destroy block pos: {} {}", this.destroyPos, param0);
                    this.level.destroyBlockProgress(this.player.getId(), this.destroyPos, -1);
                    this.player
                        .connection
                        .send(
                            new ClientboundBlockBreakAckPacket(
                                this.destroyPos, this.level.getBlockState(this.destroyPos), param1, true, "aborted mismatched destroying"
                            )
                        );
                }

                this.level.destroyBlockProgress(this.player.getId(), param0, -1);
                this.player.connection.send(new ClientboundBlockBreakAckPacket(param0, this.level.getBlockState(param0), param1, true, "aborted destroying"));
            }

        }
    }

    public void destroyAndAck(BlockPos param0, ServerboundPlayerActionPacket.Action param1, String param2) {
        if (this.destroyBlock(param0)) {
            this.player.connection.send(new ClientboundBlockBreakAckPacket(param0, this.level.getBlockState(param0), param1, true, param2));
        } else {
            this.player.connection.send(new ClientboundBlockBreakAckPacket(param0, this.level.getBlockState(param0), param1, false, param2));
        }

    }

    public boolean destroyBlock(BlockPos param0) {
        BlockState var0 = this.level.getBlockState(param0);
        if (!this.player.getMainHandItem().getItem().canAttackBlock(var0, this.level, param0, this.player)) {
            return false;
        } else {
            BlockEntity var1 = this.level.getBlockEntity(param0);
            Block var2 = var0.getBlock();
            if (var2 instanceof GameMasterBlock && !this.player.canUseGameMasterBlocks()) {
                this.level.sendBlockUpdated(param0, var0, var0, 3);
                return false;
            } else if (this.player.blockActionRestricted(this.level, param0, this.gameModeForPlayer)) {
                return false;
            } else {
                var2.playerWillDestroy(this.level, param0, var0, this.player);
                boolean var3 = this.level.removeBlock(param0, false);
                if (var3) {
                    var2.destroy(this.level, param0, var0);
                }

                if (this.isCreative()) {
                    return true;
                } else {
                    ItemStack var4 = this.player.getMainHandItem();
                    ItemStack var5 = var4.copy();
                    boolean var6 = this.player.hasCorrectToolForDrops(var0);
                    var4.mineBlock(this.level, var0, param0, this.player);
                    if (var3 && var6) {
                        var2.playerDestroy(this.level, this.player, param0, var0, var1, var5);
                    }

                    return true;
                }
            }
        }
    }

    public InteractionResult useItem(ServerPlayer param0, Level param1, ItemStack param2, InteractionHand param3) {
        if (this.gameModeForPlayer == GameType.SPECTATOR) {
            return InteractionResult.PASS;
        } else if (param0.getCooldowns().isOnCooldown(param2.getItem())) {
            return InteractionResult.PASS;
        } else {
            int var0 = param2.getCount();
            int var1 = param2.getDamageValue();
            InteractionResultHolder<ItemStack> var2 = param2.use(param1, param0, param3);
            ItemStack var3 = var2.getObject();
            if (var3 == param2 && var3.getCount() == var0 && var3.getUseDuration() <= 0 && var3.getDamageValue() == var1) {
                return var2.getResult();
            } else if (var2.getResult() == InteractionResult.FAIL && var3.getUseDuration() > 0 && !param0.isUsingItem()) {
                return var2.getResult();
            } else {
                param0.setItemInHand(param3, var3);
                if (this.isCreative()) {
                    var3.setCount(var0);
                    if (var3.isDamageableItem() && var3.getDamageValue() != var1) {
                        var3.setDamageValue(var1);
                    }
                }

                if (var3.isEmpty()) {
                    param0.setItemInHand(param3, ItemStack.EMPTY);
                }

                if (!param0.isUsingItem()) {
                    param0.inventoryMenu.sendAllDataToRemote();
                }

                return var2.getResult();
            }
        }
    }

    public InteractionResult useItemOn(ServerPlayer param0, Level param1, ItemStack param2, InteractionHand param3, BlockHitResult param4) {
        BlockPos var0 = param4.getBlockPos();
        BlockState var1 = param1.getBlockState(var0);
        if (this.gameModeForPlayer == GameType.SPECTATOR) {
            MenuProvider var2 = var1.getMenuProvider(param1, var0);
            if (var2 != null) {
                param0.openMenu(var2);
                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.PASS;
            }
        } else {
            boolean var3 = !param0.getMainHandItem().isEmpty() || !param0.getOffhandItem().isEmpty();
            boolean var4 = param0.isSecondaryUseActive() && var3;
            ItemStack var5 = param2.copy();
            if (!var4) {
                InteractionResult var6 = var1.use(param1, param0, param3, param4);
                if (var6.consumesAction()) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(param0, var0, var5);
                    return var6;
                }
            }

            if (!param2.isEmpty() && !param0.getCooldowns().isOnCooldown(param2.getItem())) {
                UseOnContext var7 = new UseOnContext(param0, param3, param4);
                InteractionResult var9;
                if (this.isCreative()) {
                    int var8 = param2.getCount();
                    var9 = param2.useOn(var7);
                    param2.setCount(var8);
                } else {
                    var9 = param2.useOn(var7);
                }

                if (var9.consumesAction()) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(param0, var0, var5);
                }

                return var9;
            } else {
                return InteractionResult.PASS;
            }
        }
    }

    public void setLevel(ServerLevel param0) {
        this.level = param0;
    }
}
