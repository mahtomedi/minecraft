package net.minecraft.client.multiplayer;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemPacket;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.StatsCounter;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseOnContext;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StructureBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.PosAndRot;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class MultiPlayerGameMode {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Minecraft minecraft;
    private final ClientPacketListener connection;
    private BlockPos destroyBlockPos = new BlockPos(-1, -1, -1);
    private ItemStack destroyingItem = ItemStack.EMPTY;
    private float destroyProgress;
    private float destroyTicks;
    private int destroyDelay;
    private boolean isDestroying;
    private GameType localPlayerMode = GameType.SURVIVAL;
    private GameType prevLocalPlayerMode = GameType.SURVIVAL;
    private final Object2ObjectLinkedOpenHashMap<Pair<BlockPos, ServerboundPlayerActionPacket.Action>, PosAndRot> unAckedActions = new Object2ObjectLinkedOpenHashMap<>(
        
    );
    private int carriedIndex;

    public MultiPlayerGameMode(Minecraft param0, ClientPacketListener param1) {
        this.minecraft = param0;
        this.connection = param1;
    }

    public void adjustPlayer(Player param0) {
        this.localPlayerMode.updatePlayerAbilities(param0.abilities);
    }

    public void setLocalMode(GameType param0) {
        if (param0 != this.localPlayerMode) {
            this.prevLocalPlayerMode = this.localPlayerMode;
        }

        this.localPlayerMode = param0;
        this.localPlayerMode.updatePlayerAbilities(this.minecraft.player.abilities);
    }

    public boolean canHurtPlayer() {
        return this.localPlayerMode.isSurvival();
    }

    public boolean destroyBlock(BlockPos param0) {
        if (this.minecraft.player.blockActionRestricted(this.minecraft.level, param0, this.localPlayerMode)) {
            return false;
        } else {
            Level var0 = this.minecraft.level;
            BlockState var1 = var0.getBlockState(param0);
            if (!this.minecraft.player.getMainHandItem().getItem().canAttackBlock(var1, var0, param0, this.minecraft.player)) {
                return false;
            } else {
                Block var2 = var1.getBlock();
                if ((var2 instanceof CommandBlock || var2 instanceof StructureBlock || var2 instanceof JigsawBlock)
                    && !this.minecraft.player.canUseGameMasterBlocks()) {
                    return false;
                } else if (var1.isAir()) {
                    return false;
                } else {
                    var2.playerWillDestroy(var0, param0, var1, this.minecraft.player);
                    FluidState var3 = var0.getFluidState(param0);
                    boolean var4 = var0.setBlock(param0, var3.createLegacyBlock(), 11);
                    if (var4) {
                        var2.destroy(var0, param0, var1);
                    }

                    return var4;
                }
            }
        }
    }

    public boolean startDestroyBlock(BlockPos param0, Direction param1) {
        if (this.minecraft.player.blockActionRestricted(this.minecraft.level, param0, this.localPlayerMode)) {
            return false;
        } else if (!this.minecraft.level.getWorldBorder().isWithinBounds(param0)) {
            return false;
        } else {
            if (this.localPlayerMode.isCreative()) {
                BlockState var0 = this.minecraft.level.getBlockState(param0);
                this.minecraft.getTutorial().onDestroyBlock(this.minecraft.level, param0, var0, 1.0F);
                this.sendBlockAction(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, param0, param1);
                this.destroyBlock(param0);
                this.destroyDelay = 5;
            } else if (!this.isDestroying || !this.sameDestroyTarget(param0)) {
                if (this.isDestroying) {
                    this.sendBlockAction(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.destroyBlockPos, param1);
                }

                BlockState var1 = this.minecraft.level.getBlockState(param0);
                this.minecraft.getTutorial().onDestroyBlock(this.minecraft.level, param0, var1, 0.0F);
                this.sendBlockAction(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, param0, param1);
                boolean var2 = !var1.isAir();
                if (var2 && this.destroyProgress == 0.0F) {
                    var1.attack(this.minecraft.level, param0, this.minecraft.player);
                }

                if (var2 && var1.getDestroyProgress(this.minecraft.player, this.minecraft.player.level, param0) >= 1.0F) {
                    this.destroyBlock(param0);
                } else {
                    this.isDestroying = true;
                    this.destroyBlockPos = param0;
                    this.destroyingItem = this.minecraft.player.getMainHandItem();
                    this.destroyProgress = 0.0F;
                    this.destroyTicks = 0.0F;
                    this.minecraft.level.destroyBlockProgress(this.minecraft.player.getId(), this.destroyBlockPos, (int)(this.destroyProgress * 10.0F) - 1);
                }
            }

            return true;
        }
    }

    public void stopDestroyBlock() {
        if (this.isDestroying) {
            BlockState var0 = this.minecraft.level.getBlockState(this.destroyBlockPos);
            this.minecraft.getTutorial().onDestroyBlock(this.minecraft.level, this.destroyBlockPos, var0, -1.0F);
            this.sendBlockAction(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.destroyBlockPos, Direction.DOWN);
            this.isDestroying = false;
            this.destroyProgress = 0.0F;
            this.minecraft.level.destroyBlockProgress(this.minecraft.player.getId(), this.destroyBlockPos, -1);
            this.minecraft.player.resetAttackStrengthTicker();
        }

    }

    public boolean continueDestroyBlock(BlockPos param0, Direction param1) {
        this.ensureHasSentCarriedItem();
        if (this.destroyDelay > 0) {
            --this.destroyDelay;
            return true;
        } else if (this.localPlayerMode.isCreative() && this.minecraft.level.getWorldBorder().isWithinBounds(param0)) {
            this.destroyDelay = 5;
            BlockState var0 = this.minecraft.level.getBlockState(param0);
            this.minecraft.getTutorial().onDestroyBlock(this.minecraft.level, param0, var0, 1.0F);
            this.sendBlockAction(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, param0, param1);
            this.destroyBlock(param0);
            return true;
        } else if (this.sameDestroyTarget(param0)) {
            BlockState var1 = this.minecraft.level.getBlockState(param0);
            if (var1.isAir()) {
                this.isDestroying = false;
                return false;
            } else {
                this.destroyProgress += var1.getDestroyProgress(this.minecraft.player, this.minecraft.player.level, param0);
                if (this.destroyTicks % 4.0F == 0.0F) {
                    SoundType var2 = var1.getSoundType();
                    this.minecraft
                        .getSoundManager()
                        .play(new SimpleSoundInstance(var2.getHitSound(), SoundSource.BLOCKS, (var2.getVolume() + 1.0F) / 8.0F, var2.getPitch() * 0.5F, param0));
                }

                ++this.destroyTicks;
                this.minecraft.getTutorial().onDestroyBlock(this.minecraft.level, param0, var1, Mth.clamp(this.destroyProgress, 0.0F, 1.0F));
                if (this.destroyProgress >= 1.0F) {
                    this.isDestroying = false;
                    this.sendBlockAction(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, param0, param1);
                    this.destroyBlock(param0);
                    this.destroyProgress = 0.0F;
                    this.destroyTicks = 0.0F;
                    this.destroyDelay = 5;
                }

                this.minecraft.level.destroyBlockProgress(this.minecraft.player.getId(), this.destroyBlockPos, (int)(this.destroyProgress * 10.0F) - 1);
                return true;
            }
        } else {
            return this.startDestroyBlock(param0, param1);
        }
    }

    public float getPickRange() {
        return this.localPlayerMode.isCreative() ? 5.0F : 4.5F;
    }

    public void tick() {
        this.ensureHasSentCarriedItem();
        if (this.connection.getConnection().isConnected()) {
            this.connection.getConnection().tick();
        } else {
            this.connection.getConnection().handleDisconnection();
        }

    }

    private boolean sameDestroyTarget(BlockPos param0) {
        ItemStack var0 = this.minecraft.player.getMainHandItem();
        boolean var1 = this.destroyingItem.isEmpty() && var0.isEmpty();
        if (!this.destroyingItem.isEmpty() && !var0.isEmpty()) {
            var1 = var0.getItem() == this.destroyingItem.getItem()
                && ItemStack.tagMatches(var0, this.destroyingItem)
                && (var0.isDamageableItem() || var0.getDamageValue() == this.destroyingItem.getDamageValue());
        }

        return param0.equals(this.destroyBlockPos) && var1;
    }

    private void ensureHasSentCarriedItem() {
        int var0 = this.minecraft.player.inventory.selected;
        if (var0 != this.carriedIndex) {
            this.carriedIndex = var0;
            this.connection.send(new ServerboundSetCarriedItemPacket(this.carriedIndex));
        }

    }

    public InteractionResult useItemOn(LocalPlayer param0, ClientLevel param1, InteractionHand param2, BlockHitResult param3) {
        this.ensureHasSentCarriedItem();
        BlockPos var0 = param3.getBlockPos();
        if (!this.minecraft.level.getWorldBorder().isWithinBounds(var0)) {
            return InteractionResult.FAIL;
        } else {
            ItemStack var1 = param0.getItemInHand(param2);
            if (this.localPlayerMode == GameType.SPECTATOR) {
                this.connection.send(new ServerboundUseItemOnPacket(param2, param3));
                return InteractionResult.SUCCESS;
            } else {
                boolean var2 = !param0.getMainHandItem().isEmpty() || !param0.getOffhandItem().isEmpty();
                boolean var3 = param0.isSecondaryUseActive() && var2;
                if (!var3) {
                    InteractionResult var4 = param1.getBlockState(var0).use(param1, param0, param2, param3);
                    if (var4.consumesAction()) {
                        this.connection.send(new ServerboundUseItemOnPacket(param2, param3));
                        return var4;
                    }
                }

                this.connection.send(new ServerboundUseItemOnPacket(param2, param3));
                if (!var1.isEmpty() && !param0.getCooldowns().isOnCooldown(var1.getItem())) {
                    UseOnContext var5 = new UseOnContext(param0, param2, param3);
                    InteractionResult var7;
                    if (this.localPlayerMode.isCreative()) {
                        int var6 = var1.getCount();
                        var7 = var1.useOn(var5);
                        var1.setCount(var6);
                    } else {
                        var7 = var1.useOn(var5);
                    }

                    return var7;
                } else {
                    return InteractionResult.PASS;
                }
            }
        }
    }

    public InteractionResult useItem(Player param0, Level param1, InteractionHand param2) {
        if (this.localPlayerMode == GameType.SPECTATOR) {
            return InteractionResult.PASS;
        } else {
            this.ensureHasSentCarriedItem();
            this.connection.send(new ServerboundUseItemPacket(param2));
            ItemStack var0 = param0.getItemInHand(param2);
            if (param0.getCooldowns().isOnCooldown(var0.getItem())) {
                return InteractionResult.PASS;
            } else {
                int var1 = var0.getCount();
                InteractionResultHolder<ItemStack> var2 = var0.use(param1, param0, param2);
                ItemStack var3 = var2.getObject();
                if (var3 != var0) {
                    param0.setItemInHand(param2, var3);
                }

                return var2.getResult();
            }
        }
    }

    public LocalPlayer createPlayer(ClientLevel param0, StatsCounter param1, ClientRecipeBook param2) {
        return new LocalPlayer(this.minecraft, param0, this.connection, param1, param2);
    }

    public void attack(Player param0, Entity param1) {
        this.ensureHasSentCarriedItem();
        this.connection.send(new ServerboundInteractPacket(param1));
        if (this.localPlayerMode != GameType.SPECTATOR) {
            param0.attack(param1);
            param0.resetAttackStrengthTicker();
        }

    }

    public InteractionResult interact(Player param0, Entity param1, InteractionHand param2) {
        this.ensureHasSentCarriedItem();
        this.connection.send(new ServerboundInteractPacket(param1, param2));
        return this.localPlayerMode == GameType.SPECTATOR ? InteractionResult.PASS : param0.interactOn(param1, param2);
    }

    public InteractionResult interactAt(Player param0, Entity param1, EntityHitResult param2, InteractionHand param3) {
        this.ensureHasSentCarriedItem();
        Vec3 var0 = param2.getLocation().subtract(param1.getX(), param1.getY(), param1.getZ());
        this.connection.send(new ServerboundInteractPacket(param1, param3, var0));
        return this.localPlayerMode == GameType.SPECTATOR ? InteractionResult.PASS : param1.interactAt(param0, var0, param3);
    }

    public ItemStack handleInventoryMouseClick(int param0, int param1, int param2, ClickType param3, Player param4) {
        short var0 = param4.containerMenu.backup(param4.inventory);
        ItemStack var1 = param4.containerMenu.clicked(param1, param2, param3, param4);
        this.connection.send(new ServerboundContainerClickPacket(param0, param1, param2, param3, var1, var0));
        return var1;
    }

    public void handlePlaceRecipe(int param0, Recipe<?> param1, boolean param2) {
        this.connection.send(new ServerboundPlaceRecipePacket(param0, param1, param2));
    }

    public void handleInventoryButtonClick(int param0, int param1) {
        this.connection.send(new ServerboundContainerButtonClickPacket(param0, param1));
    }

    public void handleCreativeModeItemAdd(ItemStack param0, int param1) {
        if (this.localPlayerMode.isCreative()) {
            this.connection.send(new ServerboundSetCreativeModeSlotPacket(param1, param0));
        }

    }

    public void handleCreativeModeItemDrop(ItemStack param0) {
        if (this.localPlayerMode.isCreative() && !param0.isEmpty()) {
            this.connection.send(new ServerboundSetCreativeModeSlotPacket(-1, param0));
        }

    }

    public void releaseUsingItem(Player param0) {
        this.ensureHasSentCarriedItem();
        this.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN));
        param0.releaseUsingItem();
    }

    public boolean hasExperience() {
        return this.localPlayerMode.isSurvival();
    }

    public boolean hasMissTime() {
        return !this.localPlayerMode.isCreative();
    }

    public boolean hasInfiniteItems() {
        return this.localPlayerMode.isCreative();
    }

    public boolean hasFarPickRange() {
        return this.localPlayerMode.isCreative();
    }

    public boolean isServerControlledInventory() {
        return this.minecraft.player.isPassenger() && this.minecraft.player.getVehicle() instanceof AbstractHorse;
    }

    public boolean isAlwaysFlying() {
        return this.localPlayerMode == GameType.SPECTATOR;
    }

    public GameType getPrevPlayerMode() {
        return this.prevLocalPlayerMode;
    }

    public GameType getPlayerMode() {
        return this.localPlayerMode;
    }

    public boolean isDestroying() {
        return this.isDestroying;
    }

    public void handlePickItem(int param0) {
        this.connection.send(new ServerboundPickItemPacket(param0));
    }

    private void sendBlockAction(ServerboundPlayerActionPacket.Action param0, BlockPos param1, Direction param2) {
        LocalPlayer var0 = this.minecraft.player;
        this.unAckedActions.put(Pair.of(param1, param0), new PosAndRot(var0.position(), var0.xRot, var0.yRot));
        this.connection.send(new ServerboundPlayerActionPacket(param0, param1, param2));
    }

    public void handleBlockBreakAck(ClientLevel param0, BlockPos param1, BlockState param2, ServerboundPlayerActionPacket.Action param3, boolean param4) {
        PosAndRot var0 = this.unAckedActions.remove(Pair.of(param1, param3));
        if (var0 == null || !param4 || param3 != ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK && param0.getBlockState(param1) != param2) {
            param0.setKnownState(param1, param2);
            if (var0 != null) {
                Vec3 var1 = var0.pos();
                this.minecraft.player.absMoveTo(var1.x, var1.y, var1.z, var0.yRot(), var0.xRot());
            }
        }

        while(this.unAckedActions.size() >= 50) {
            Pair<BlockPos, ServerboundPlayerActionPacket.Action> var2 = this.unAckedActions.firstKey();
            this.unAckedActions.removeFirst();
            LOGGER.error("Too many unacked block actions, dropping " + var2);
        }

    }
}
