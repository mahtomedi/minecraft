package net.minecraft.server.level;

import com.mojang.logging.LogUtils;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
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
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class ServerPlayerGameMode {
    private static final Logger LOGGER = LogUtils.getLogger();
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
            this.setGameModeForPlayer(param0, this.previousGameModeForPlayer);
            this.player.onUpdateAbilities();
            this.player
                .server
                .getPlayerList()
                .broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE, this.player));
            this.level.updateSleepingPlayerList();
            return true;
        }
    }

    protected void setGameModeForPlayer(GameType param0, @Nullable GameType param1) {
        this.previousGameModeForPlayer = param1;
        this.gameModeForPlayer = param0;
        param0.updatePlayerAbilities(this.player.getAbilities());
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

    private void debugLogging(BlockPos param0, boolean param1, int param2, String param3) {
    }

    public void handleBlockBreakAction(BlockPos param0, ServerboundPlayerActionPacket.Action param1, Direction param2, int param3, int param4) {
        if (this.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(param0)) > ServerGamePacketListenerImpl.MAX_INTERACTION_DISTANCE) {
            this.debugLogging(param0, false, param4, "too far");
        } else if (param0.getY() >= param3) {
            this.player.connection.send(new ClientboundBlockUpdatePacket(param0, this.level.getBlockState(param0)));
            this.debugLogging(param0, false, param4, "too high");
        } else {
            if (param1 == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
                if (!this.level.mayInteract(this.player, param0)) {
                    this.player.connection.send(new ClientboundBlockUpdatePacket(param0, this.level.getBlockState(param0)));
                    this.debugLogging(param0, false, param4, "may not interact");
                    return;
                }

                if (this.isCreative()) {
                    this.destroyAndAck(param0, param4, "creative destroy");
                    return;
                }

                if (this.player.blockActionRestricted(this.level, param0, this.gameModeForPlayer)) {
                    this.player.connection.send(new ClientboundBlockUpdatePacket(param0, this.level.getBlockState(param0)));
                    this.debugLogging(param0, false, param4, "block action restricted");
                    return;
                }

                this.destroyProgressStart = this.gameTicks;
                float var0 = 1.0F;
                BlockState var1 = this.level.getBlockState(param0);
                if (!var1.isAir()) {
                    var1.attack(this.level, param0, this.player);
                    var0 = var1.getDestroyProgress(this.player, this.player.level, param0);
                }

                if (!var1.isAir() && var0 >= 1.0F) {
                    this.destroyAndAck(param0, param4, "insta mine");
                } else {
                    if (this.isDestroyingBlock) {
                        this.player.connection.send(new ClientboundBlockUpdatePacket(this.destroyPos, this.level.getBlockState(this.destroyPos)));
                        this.debugLogging(param0, false, param4, "abort destroying since another started (client insta mine, server disagreed)");
                    }

                    this.isDestroyingBlock = true;
                    this.destroyPos = param0.immutable();
                    int var2 = (int)(var0 * 10.0F);
                    this.level.destroyBlockProgress(this.player.getId(), param0, var2);
                    this.debugLogging(param0, true, param4, "actual start of destroying");
                    this.lastSentState = var2;
                }
            } else if (param1 == ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK) {
                if (param0.equals(this.destroyPos)) {
                    int var3 = this.gameTicks - this.destroyProgressStart;
                    BlockState var4 = this.level.getBlockState(param0);
                    if (!var4.isAir()) {
                        float var5 = var4.getDestroyProgress(this.player, this.player.level, param0) * (float)(var3 + 1);
                        if (var5 >= 0.7F) {
                            this.isDestroyingBlock = false;
                            this.level.destroyBlockProgress(this.player.getId(), param0, -1);
                            this.destroyAndAck(param0, param4, "destroyed");
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

                this.debugLogging(param0, true, param4, "stopped destroying");
            } else if (param1 == ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK) {
                this.isDestroyingBlock = false;
                if (!Objects.equals(this.destroyPos, param0)) {
                    LOGGER.warn("Mismatch in destroy block pos: {} {}", this.destroyPos, param0);
                    this.level.destroyBlockProgress(this.player.getId(), this.destroyPos, -1);
                    this.debugLogging(param0, true, param4, "aborted mismatched destroying");
                }

                this.level.destroyBlockProgress(this.player.getId(), param0, -1);
                this.debugLogging(param0, true, param4, "aborted destroying");
            }

        }
    }

    public void destroyAndAck(BlockPos param0, int param1, String param2) {
        if (this.destroyBlock(param0)) {
            this.debugLogging(param0, true, param1, param2);
        } else {
            this.player.connection.send(new ClientboundBlockUpdatePacket(param0, this.level.getBlockState(param0)));
            this.debugLogging(param0, false, param1, param2);
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
                if (param2 != var3) {
                    param0.setItemInHand(param3, var3);
                }

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
        if (!var1.getBlock().isEnabled(param1.enabledFeatures())) {
            return InteractionResult.FAIL;
        } else if (this.gameModeForPlayer == GameType.SPECTATOR) {
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
