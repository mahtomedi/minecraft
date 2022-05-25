package net.minecraft.server.network;

import com.google.common.collect.Lists;
import com.google.common.primitives.Floats;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.logging.LogUtils;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.PreviewedArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.ChatDecorator;
import net.minecraft.network.chat.ChatPreviewThrottler;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientboundBlockChangedAckPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundChatPreviewPacket;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundTagQueryPacket;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundBlockEntityTagQuery;
import net.minecraft.network.protocol.game.ServerboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundChatPreviewPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.network.protocol.game.ServerboundEntityTagQuery;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundJigsawGeneratePacket;
import net.minecraft.network.protocol.game.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ServerboundLockDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemPacket;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundPongPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookChangeSettingsPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookSeenRecipePacket;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.network.protocol.game.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.network.protocol.game.ServerboundSetBeaconPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandMinecartPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundSetJigsawBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetStructureBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

public class ServerGamePacketListenerImpl implements ServerGamePacketListener, ServerPlayerConnection {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int LATENCY_CHECK_INTERVAL = 15000;
    public static final double MAX_INTERACTION_DISTANCE = Mth.square(6.0);
    private static final int NO_BLOCK_UPDATES_TO_ACK = -1;
    public final Connection connection;
    private final MinecraftServer server;
    public ServerPlayer player;
    private int tickCount;
    private int ackBlockChangesUpTo = -1;
    private long keepAliveTime;
    private boolean keepAlivePending;
    private long keepAliveChallenge;
    private int chatSpamTickCount;
    private int dropSpamTickCount;
    private double firstGoodX;
    private double firstGoodY;
    private double firstGoodZ;
    private double lastGoodX;
    private double lastGoodY;
    private double lastGoodZ;
    @Nullable
    private Entity lastVehicle;
    private double vehicleFirstGoodX;
    private double vehicleFirstGoodY;
    private double vehicleFirstGoodZ;
    private double vehicleLastGoodX;
    private double vehicleLastGoodY;
    private double vehicleLastGoodZ;
    @Nullable
    private Vec3 awaitingPositionFromClient;
    private int awaitingTeleport;
    private int awaitingTeleportTime;
    private boolean clientIsFloating;
    private int aboveGroundTickCount;
    private boolean clientVehicleIsFloating;
    private int aboveGroundVehicleTickCount;
    private int receivedMovePacketCount;
    private int knownMovePacketCount;
    private final ChatPreviewThrottler chatPreviewThrottler = new ChatPreviewThrottler();
    private final AtomicReference<Instant> lastChatTimeStamp = new AtomicReference<>(Instant.EPOCH);

    public ServerGamePacketListenerImpl(MinecraftServer param0, Connection param1, ServerPlayer param2) {
        this.server = param0;
        this.connection = param1;
        param1.setListener(this);
        this.player = param2;
        param2.connection = this;
        this.keepAliveTime = Util.getMillis();
        param2.getTextFilter().join();
    }

    public void tick() {
        if (this.ackBlockChangesUpTo > -1) {
            this.send(new ClientboundBlockChangedAckPacket(this.ackBlockChangesUpTo));
            this.ackBlockChangesUpTo = -1;
        }

        this.resetPosition();
        this.player.xo = this.player.getX();
        this.player.yo = this.player.getY();
        this.player.zo = this.player.getZ();
        this.player.doTick();
        this.player.absMoveTo(this.firstGoodX, this.firstGoodY, this.firstGoodZ, this.player.getYRot(), this.player.getXRot());
        ++this.tickCount;
        this.knownMovePacketCount = this.receivedMovePacketCount;
        if (this.clientIsFloating && !this.player.isSleeping() && !this.player.isPassenger()) {
            if (++this.aboveGroundTickCount > 80) {
                LOGGER.warn("{} was kicked for floating too long!", this.player.getName().getString());
                this.disconnect(Component.translatable("multiplayer.disconnect.flying"));
                return;
            }
        } else {
            this.clientIsFloating = false;
            this.aboveGroundTickCount = 0;
        }

        this.lastVehicle = this.player.getRootVehicle();
        if (this.lastVehicle != this.player && this.lastVehicle.getControllingPassenger() == this.player) {
            this.vehicleFirstGoodX = this.lastVehicle.getX();
            this.vehicleFirstGoodY = this.lastVehicle.getY();
            this.vehicleFirstGoodZ = this.lastVehicle.getZ();
            this.vehicleLastGoodX = this.lastVehicle.getX();
            this.vehicleLastGoodY = this.lastVehicle.getY();
            this.vehicleLastGoodZ = this.lastVehicle.getZ();
            if (this.clientVehicleIsFloating && this.player.getRootVehicle().getControllingPassenger() == this.player) {
                if (++this.aboveGroundVehicleTickCount > 80) {
                    LOGGER.warn("{} was kicked for floating a vehicle too long!", this.player.getName().getString());
                    this.disconnect(Component.translatable("multiplayer.disconnect.flying"));
                    return;
                }
            } else {
                this.clientVehicleIsFloating = false;
                this.aboveGroundVehicleTickCount = 0;
            }
        } else {
            this.lastVehicle = null;
            this.clientVehicleIsFloating = false;
            this.aboveGroundVehicleTickCount = 0;
        }

        this.server.getProfiler().push("keepAlive");
        long var0 = Util.getMillis();
        if (var0 - this.keepAliveTime >= 15000L) {
            if (this.keepAlivePending) {
                this.disconnect(Component.translatable("disconnect.timeout"));
            } else {
                this.keepAlivePending = true;
                this.keepAliveTime = var0;
                this.keepAliveChallenge = var0;
                this.send(new ClientboundKeepAlivePacket(this.keepAliveChallenge));
            }
        }

        this.server.getProfiler().pop();
        if (this.chatSpamTickCount > 0) {
            --this.chatSpamTickCount;
        }

        if (this.dropSpamTickCount > 0) {
            --this.dropSpamTickCount;
        }

        if (this.player.getLastActionTime() > 0L
            && this.server.getPlayerIdleTimeout() > 0
            && Util.getMillis() - this.player.getLastActionTime() > (long)(this.server.getPlayerIdleTimeout() * 1000 * 60)) {
            this.disconnect(Component.translatable("multiplayer.disconnect.idling"));
        }

        this.chatPreviewThrottler.tick();
    }

    public void resetPosition() {
        this.firstGoodX = this.player.getX();
        this.firstGoodY = this.player.getY();
        this.firstGoodZ = this.player.getZ();
        this.lastGoodX = this.player.getX();
        this.lastGoodY = this.player.getY();
        this.lastGoodZ = this.player.getZ();
    }

    @Override
    public Connection getConnection() {
        return this.connection;
    }

    private boolean isSingleplayerOwner() {
        return this.server.isSingleplayerOwner(this.player.getGameProfile());
    }

    public void disconnect(Component param0) {
        this.connection.send(new ClientboundDisconnectPacket(param0), param1 -> this.connection.disconnect(param0));
        this.connection.setReadOnly();
        this.server.executeBlocking(this.connection::handleDisconnection);
    }

    private <T, R> void filterTextPacket(T param0, Consumer<R> param1, BiFunction<TextFilter, T, CompletableFuture<R>> param2) {
        BlockableEventLoop<?> var0 = this.player.getLevel().getServer();
        Consumer<R> var1 = param2x -> {
            if (this.getConnection().isConnected()) {
                try {
                    param1.accept(param2x);
                } catch (Exception var5x) {
                    LOGGER.error("Failed to handle chat packet {}, suppressing error", param0, var5x);
                }
            } else {
                LOGGER.debug("Ignoring packet due to disconnection");
            }

        };
        param2.apply(this.player.getTextFilter(), param0).thenAcceptAsync(var1, var0);
    }

    private void filterTextPacket(String param0, Consumer<FilteredText<String>> param1) {
        this.filterTextPacket(param0, param1, TextFilter::processStreamMessage);
    }

    private void filterTextPacket(List<String> param0, Consumer<List<FilteredText<String>>> param1) {
        this.filterTextPacket(param0, param1, TextFilter::processMessageBundle);
    }

    @Override
    public void handlePlayerInput(ServerboundPlayerInputPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        this.player.setPlayerInput(param0.getXxa(), param0.getZza(), param0.isJumping(), param0.isShiftKeyDown());
    }

    private static boolean containsInvalidValues(double param0, double param1, double param2, float param3, float param4) {
        return Double.isNaN(param0) || Double.isNaN(param1) || Double.isNaN(param2) || !Floats.isFinite(param4) || !Floats.isFinite(param3);
    }

    private static double clampHorizontal(double param0) {
        return Mth.clamp(param0, -3.0E7, 3.0E7);
    }

    private static double clampVertical(double param0) {
        return Mth.clamp(param0, -2.0E7, 2.0E7);
    }

    @Override
    public void handleMoveVehicle(ServerboundMoveVehiclePacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        if (containsInvalidValues(param0.getX(), param0.getY(), param0.getZ(), param0.getYRot(), param0.getXRot())) {
            this.disconnect(Component.translatable("multiplayer.disconnect.invalid_vehicle_movement"));
        } else {
            Entity var0 = this.player.getRootVehicle();
            if (var0 != this.player && var0.getControllingPassenger() == this.player && var0 == this.lastVehicle) {
                ServerLevel var1 = this.player.getLevel();
                double var2 = var0.getX();
                double var3 = var0.getY();
                double var4 = var0.getZ();
                double var5 = clampHorizontal(param0.getX());
                double var6 = clampVertical(param0.getY());
                double var7 = clampHorizontal(param0.getZ());
                float var8 = Mth.wrapDegrees(param0.getYRot());
                float var9 = Mth.wrapDegrees(param0.getXRot());
                double var10 = var5 - this.vehicleFirstGoodX;
                double var11 = var6 - this.vehicleFirstGoodY;
                double var12 = var7 - this.vehicleFirstGoodZ;
                double var13 = var0.getDeltaMovement().lengthSqr();
                double var14 = var10 * var10 + var11 * var11 + var12 * var12;
                if (var14 - var13 > 100.0 && !this.isSingleplayerOwner()) {
                    LOGGER.warn(
                        "{} (vehicle of {}) moved too quickly! {},{},{}", var0.getName().getString(), this.player.getName().getString(), var10, var11, var12
                    );
                    this.connection.send(new ClientboundMoveVehiclePacket(var0));
                    return;
                }

                boolean var15 = var1.noCollision(var0, var0.getBoundingBox().deflate(0.0625));
                var10 = var5 - this.vehicleLastGoodX;
                var11 = var6 - this.vehicleLastGoodY - 1.0E-6;
                var12 = var7 - this.vehicleLastGoodZ;
                boolean var16 = var0.verticalCollisionBelow;
                var0.move(MoverType.PLAYER, new Vec3(var10, var11, var12));
                var10 = var5 - var0.getX();
                var11 = var6 - var0.getY();
                if (var11 > -0.5 || var11 < 0.5) {
                    var11 = 0.0;
                }

                var12 = var7 - var0.getZ();
                var14 = var10 * var10 + var11 * var11 + var12 * var12;
                boolean var18 = false;
                if (var14 > 0.0625) {
                    var18 = true;
                    LOGGER.warn("{} (vehicle of {}) moved wrongly! {}", var0.getName().getString(), this.player.getName().getString(), Math.sqrt(var14));
                }

                var0.absMoveTo(var5, var6, var7, var8, var9);
                boolean var19 = var1.noCollision(var0, var0.getBoundingBox().deflate(0.0625));
                if (var15 && (var18 || !var19)) {
                    var0.absMoveTo(var2, var3, var4, var8, var9);
                    this.connection.send(new ClientboundMoveVehiclePacket(var0));
                    return;
                }

                this.player.getLevel().getChunkSource().move(this.player);
                this.player.checkMovementStatistics(this.player.getX() - var2, this.player.getY() - var3, this.player.getZ() - var4);
                this.clientVehicleIsFloating = var11 >= -0.03125
                    && !var16
                    && !this.server.isFlightAllowed()
                    && !var0.isNoGravity()
                    && this.noBlocksAround(var0);
                this.vehicleLastGoodX = var0.getX();
                this.vehicleLastGoodY = var0.getY();
                this.vehicleLastGoodZ = var0.getZ();
            }

        }
    }

    private boolean noBlocksAround(Entity param0) {
        return param0.level
            .getBlockStates(param0.getBoundingBox().inflate(0.0625).expandTowards(0.0, -0.55, 0.0))
            .allMatch(BlockBehaviour.BlockStateBase::isAir);
    }

    @Override
    public void handleAcceptTeleportPacket(ServerboundAcceptTeleportationPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        if (param0.getId() == this.awaitingTeleport) {
            if (this.awaitingPositionFromClient == null) {
                this.disconnect(Component.translatable("multiplayer.disconnect.invalid_player_movement"));
                return;
            }

            this.player
                .absMoveTo(
                    this.awaitingPositionFromClient.x,
                    this.awaitingPositionFromClient.y,
                    this.awaitingPositionFromClient.z,
                    this.player.getYRot(),
                    this.player.getXRot()
                );
            this.lastGoodX = this.awaitingPositionFromClient.x;
            this.lastGoodY = this.awaitingPositionFromClient.y;
            this.lastGoodZ = this.awaitingPositionFromClient.z;
            if (this.player.isChangingDimension()) {
                this.player.hasChangedDimension();
            }

            this.awaitingPositionFromClient = null;
        }

    }

    @Override
    public void handleRecipeBookSeenRecipePacket(ServerboundRecipeBookSeenRecipePacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        this.server.getRecipeManager().byKey(param0.getRecipe()).ifPresent(this.player.getRecipeBook()::removeHighlight);
    }

    @Override
    public void handleRecipeBookChangeSettingsPacket(ServerboundRecipeBookChangeSettingsPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        this.player.getRecipeBook().setBookSetting(param0.getBookType(), param0.isOpen(), param0.isFiltering());
    }

    @Override
    public void handleSeenAdvancements(ServerboundSeenAdvancementsPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        if (param0.getAction() == ServerboundSeenAdvancementsPacket.Action.OPENED_TAB) {
            ResourceLocation var0 = param0.getTab();
            Advancement var1 = this.server.getAdvancements().getAdvancement(var0);
            if (var1 != null) {
                this.player.getAdvancements().setSelectedTab(var1);
            }
        }

    }

    @Override
    public void handleCustomCommandSuggestions(ServerboundCommandSuggestionPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        StringReader var0 = new StringReader(param0.getCommand());
        if (var0.canRead() && var0.peek() == '/') {
            var0.skip();
        }

        ParseResults<CommandSourceStack> var1 = this.server.getCommands().getDispatcher().parse(var0, this.player.createCommandSourceStack());
        this.server
            .getCommands()
            .getDispatcher()
            .getCompletionSuggestions(var1)
            .thenAccept(param1 -> this.connection.send(new ClientboundCommandSuggestionsPacket(param0.getId(), param1)));
    }

    @Override
    public void handleSetCommandBlock(ServerboundSetCommandBlockPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        if (!this.server.isCommandBlockEnabled()) {
            this.player.sendSystemMessage(Component.translatable("advMode.notEnabled"));
        } else if (!this.player.canUseGameMasterBlocks()) {
            this.player.sendSystemMessage(Component.translatable("advMode.notAllowed"));
        } else {
            BaseCommandBlock var0 = null;
            CommandBlockEntity var1 = null;
            BlockPos var2 = param0.getPos();
            BlockEntity var3 = this.player.level.getBlockEntity(var2);
            if (var3 instanceof CommandBlockEntity) {
                var1 = (CommandBlockEntity)var3;
                var0 = var1.getCommandBlock();
            }

            String var4 = param0.getCommand();
            boolean var5 = param0.isTrackOutput();
            if (var0 != null) {
                CommandBlockEntity.Mode var6 = var1.getMode();
                BlockState var7 = this.player.level.getBlockState(var2);
                Direction var8 = var7.getValue(CommandBlock.FACING);

                BlockState var12 = (switch(param0.getMode()) {
                    case SEQUENCE -> Blocks.CHAIN_COMMAND_BLOCK.defaultBlockState();
                    case AUTO -> Blocks.REPEATING_COMMAND_BLOCK.defaultBlockState();
                    default -> Blocks.COMMAND_BLOCK.defaultBlockState();
                }).setValue(CommandBlock.FACING, var8).setValue(CommandBlock.CONDITIONAL, Boolean.valueOf(param0.isConditional()));
                if (var12 != var7) {
                    this.player.level.setBlock(var2, var12, 2);
                    var3.setBlockState(var12);
                    this.player.level.getChunkAt(var2).setBlockEntity(var3);
                }

                var0.setCommand(var4);
                var0.setTrackOutput(var5);
                if (!var5) {
                    var0.setLastOutput(null);
                }

                var1.setAutomatic(param0.isAutomatic());
                if (var6 != param0.getMode()) {
                    var1.onModeSwitch();
                }

                var0.onUpdated();
                if (!StringUtil.isNullOrEmpty(var4)) {
                    this.player.sendSystemMessage(Component.translatable("advMode.setCommand.success", var4));
                }
            }

        }
    }

    @Override
    public void handleSetCommandMinecart(ServerboundSetCommandMinecartPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        if (!this.server.isCommandBlockEnabled()) {
            this.player.sendSystemMessage(Component.translatable("advMode.notEnabled"));
        } else if (!this.player.canUseGameMasterBlocks()) {
            this.player.sendSystemMessage(Component.translatable("advMode.notAllowed"));
        } else {
            BaseCommandBlock var0 = param0.getCommandBlock(this.player.level);
            if (var0 != null) {
                var0.setCommand(param0.getCommand());
                var0.setTrackOutput(param0.isTrackOutput());
                if (!param0.isTrackOutput()) {
                    var0.setLastOutput(null);
                }

                var0.onUpdated();
                this.player.sendSystemMessage(Component.translatable("advMode.setCommand.success", param0.getCommand()));
            }

        }
    }

    @Override
    public void handlePickItem(ServerboundPickItemPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        this.player.getInventory().pickSlot(param0.getSlot());
        this.player
            .connection
            .send(
                new ClientboundContainerSetSlotPacket(
                    -2, 0, this.player.getInventory().selected, this.player.getInventory().getItem(this.player.getInventory().selected)
                )
            );
        this.player.connection.send(new ClientboundContainerSetSlotPacket(-2, 0, param0.getSlot(), this.player.getInventory().getItem(param0.getSlot())));
        this.player.connection.send(new ClientboundSetCarriedItemPacket(this.player.getInventory().selected));
    }

    @Override
    public void handleRenameItem(ServerboundRenameItemPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        if (this.player.containerMenu instanceof AnvilMenu var0) {
            String var1 = SharedConstants.filterText(param0.getName());
            if (var1.length() <= 50) {
                var0.setItemName(var1);
            }
        }

    }

    @Override
    public void handleSetBeaconPacket(ServerboundSetBeaconPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        if (this.player.containerMenu instanceof BeaconMenu) {
            ((BeaconMenu)this.player.containerMenu).updateEffects(param0.getPrimary(), param0.getSecondary());
        }

    }

    @Override
    public void handleSetStructureBlock(ServerboundSetStructureBlockPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        if (this.player.canUseGameMasterBlocks()) {
            BlockPos var0 = param0.getPos();
            BlockState var1 = this.player.level.getBlockState(var0);
            BlockEntity var2 = this.player.level.getBlockEntity(var0);
            if (var2 instanceof StructureBlockEntity var3) {
                var3.setMode(param0.getMode());
                var3.setStructureName(param0.getName());
                var3.setStructurePos(param0.getOffset());
                var3.setStructureSize(param0.getSize());
                var3.setMirror(param0.getMirror());
                var3.setRotation(param0.getRotation());
                var3.setMetaData(param0.getData());
                var3.setIgnoreEntities(param0.isIgnoreEntities());
                var3.setShowAir(param0.isShowAir());
                var3.setShowBoundingBox(param0.isShowBoundingBox());
                var3.setIntegrity(param0.getIntegrity());
                var3.setSeed(param0.getSeed());
                if (var3.hasStructureName()) {
                    String var4 = var3.getStructureName();
                    if (param0.getUpdateType() == StructureBlockEntity.UpdateType.SAVE_AREA) {
                        if (var3.saveStructure()) {
                            this.player.displayClientMessage(Component.translatable("structure_block.save_success", var4), false);
                        } else {
                            this.player.displayClientMessage(Component.translatable("structure_block.save_failure", var4), false);
                        }
                    } else if (param0.getUpdateType() == StructureBlockEntity.UpdateType.LOAD_AREA) {
                        if (!var3.isStructureLoadable()) {
                            this.player.displayClientMessage(Component.translatable("structure_block.load_not_found", var4), false);
                        } else if (var3.loadStructure(this.player.getLevel())) {
                            this.player.displayClientMessage(Component.translatable("structure_block.load_success", var4), false);
                        } else {
                            this.player.displayClientMessage(Component.translatable("structure_block.load_prepare", var4), false);
                        }
                    } else if (param0.getUpdateType() == StructureBlockEntity.UpdateType.SCAN_AREA) {
                        if (var3.detectSize()) {
                            this.player.displayClientMessage(Component.translatable("structure_block.size_success", var4), false);
                        } else {
                            this.player.displayClientMessage(Component.translatable("structure_block.size_failure"), false);
                        }
                    }
                } else {
                    this.player.displayClientMessage(Component.translatable("structure_block.invalid_structure_name", param0.getName()), false);
                }

                var3.setChanged();
                this.player.level.sendBlockUpdated(var0, var1, var1, 3);
            }

        }
    }

    @Override
    public void handleSetJigsawBlock(ServerboundSetJigsawBlockPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        if (this.player.canUseGameMasterBlocks()) {
            BlockPos var0 = param0.getPos();
            BlockState var1 = this.player.level.getBlockState(var0);
            BlockEntity var2 = this.player.level.getBlockEntity(var0);
            if (var2 instanceof JigsawBlockEntity var3) {
                var3.setName(param0.getName());
                var3.setTarget(param0.getTarget());
                var3.setPool(ResourceKey.create(Registry.TEMPLATE_POOL_REGISTRY, param0.getPool()));
                var3.setFinalState(param0.getFinalState());
                var3.setJoint(param0.getJoint());
                var3.setChanged();
                this.player.level.sendBlockUpdated(var0, var1, var1, 3);
            }

        }
    }

    @Override
    public void handleJigsawGenerate(ServerboundJigsawGeneratePacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        if (this.player.canUseGameMasterBlocks()) {
            BlockPos var0 = param0.getPos();
            BlockEntity var1 = this.player.level.getBlockEntity(var0);
            if (var1 instanceof JigsawBlockEntity var2) {
                var2.generate(this.player.getLevel(), param0.levels(), param0.keepJigsaws());
            }

        }
    }

    @Override
    public void handleSelectTrade(ServerboundSelectTradePacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        int var0 = param0.getItem();
        AbstractContainerMenu var1 = this.player.containerMenu;
        if (var1 instanceof MerchantMenu var2) {
            var2.setSelectionHint(var0);
            var2.tryMoveItems(var0);
        }

    }

    @Override
    public void handleEditBook(ServerboundEditBookPacket param0) {
        int var0 = param0.getSlot();
        if (Inventory.isHotbarSlot(var0) || var0 == 40) {
            List<String> var1 = Lists.newArrayList();
            Optional<String> var2 = param0.getTitle();
            var2.ifPresent(var1::add);
            param0.getPages().stream().limit(100L).forEach(var1::add);
            this.filterTextPacket(
                var1,
                var2.isPresent()
                    ? param1 -> this.signBook(param1.get(0), param1.subList(1, param1.size()), var0)
                    : param1 -> this.updateBookContents(param1, var0)
            );
        }
    }

    private void updateBookContents(List<FilteredText<String>> param0, int param1) {
        ItemStack var0 = this.player.getInventory().getItem(param1);
        if (var0.is(Items.WRITABLE_BOOK)) {
            this.updateBookPages(param0, UnaryOperator.identity(), var0);
        }
    }

    private void signBook(FilteredText<String> param0, List<FilteredText<String>> param1, int param2) {
        ItemStack var0 = this.player.getInventory().getItem(param2);
        if (var0.is(Items.WRITABLE_BOOK)) {
            ItemStack var1 = new ItemStack(Items.WRITTEN_BOOK);
            CompoundTag var2 = var0.getTag();
            if (var2 != null) {
                var1.setTag(var2.copy());
            }

            var1.addTagElement("author", StringTag.valueOf(this.player.getName().getString()));
            if (this.player.isTextFilteringEnabled()) {
                var1.addTagElement("title", StringTag.valueOf(param0.filteredOrElse("")));
            } else {
                var1.addTagElement("filtered_title", StringTag.valueOf(param0.filteredOrElse("")));
                var1.addTagElement("title", StringTag.valueOf(param0.raw()));
            }

            this.updateBookPages(param1, param0x -> Component.Serializer.toJson(Component.literal(param0x)), var1);
            this.player.getInventory().setItem(param2, var1);
        }
    }

    private void updateBookPages(List<FilteredText<String>> param0, UnaryOperator<String> param1, ItemStack param2) {
        ListTag var0 = new ListTag();
        if (this.player.isTextFilteringEnabled()) {
            param0.stream().map(param1x -> StringTag.valueOf(param1.apply(param1x.filteredOrElse("")))).forEach(var0::add);
        } else {
            CompoundTag var1 = new CompoundTag();
            int var2 = 0;

            for(int var3 = param0.size(); var2 < var3; ++var2) {
                FilteredText<String> var4 = param0.get(var2);
                String var5 = var4.raw();
                var0.add(StringTag.valueOf(param1.apply(var5)));
                if (var4.isFiltered()) {
                    var1.putString(String.valueOf(var2), param1.apply(var4.filteredOrElse("")));
                }
            }

            if (!var1.isEmpty()) {
                param2.addTagElement("filtered_pages", var1);
            }
        }

        param2.addTagElement("pages", var0);
    }

    @Override
    public void handleEntityTagQuery(ServerboundEntityTagQuery param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        if (this.player.hasPermissions(2)) {
            Entity var0 = this.player.getLevel().getEntity(param0.getEntityId());
            if (var0 != null) {
                CompoundTag var1 = var0.saveWithoutId(new CompoundTag());
                this.player.connection.send(new ClientboundTagQueryPacket(param0.getTransactionId(), var1));
            }

        }
    }

    @Override
    public void handleBlockEntityTagQuery(ServerboundBlockEntityTagQuery param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        if (this.player.hasPermissions(2)) {
            BlockEntity var0 = this.player.getLevel().getBlockEntity(param0.getPos());
            CompoundTag var1 = var0 != null ? var0.saveWithoutMetadata() : null;
            this.player.connection.send(new ClientboundTagQueryPacket(param0.getTransactionId(), var1));
        }
    }

    @Override
    public void handleMovePlayer(ServerboundMovePlayerPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        if (containsInvalidValues(param0.getX(0.0), param0.getY(0.0), param0.getZ(0.0), param0.getYRot(0.0F), param0.getXRot(0.0F))) {
            this.disconnect(Component.translatable("multiplayer.disconnect.invalid_player_movement"));
        } else {
            ServerLevel var0 = this.player.getLevel();
            if (!this.player.wonGame) {
                if (this.tickCount == 0) {
                    this.resetPosition();
                }

                if (this.awaitingPositionFromClient != null) {
                    if (this.tickCount - this.awaitingTeleportTime > 20) {
                        this.awaitingTeleportTime = this.tickCount;
                        this.teleport(
                            this.awaitingPositionFromClient.x,
                            this.awaitingPositionFromClient.y,
                            this.awaitingPositionFromClient.z,
                            this.player.getYRot(),
                            this.player.getXRot()
                        );
                    }

                } else {
                    this.awaitingTeleportTime = this.tickCount;
                    double var1 = clampHorizontal(param0.getX(this.player.getX()));
                    double var2 = clampVertical(param0.getY(this.player.getY()));
                    double var3 = clampHorizontal(param0.getZ(this.player.getZ()));
                    float var4 = Mth.wrapDegrees(param0.getYRot(this.player.getYRot()));
                    float var5 = Mth.wrapDegrees(param0.getXRot(this.player.getXRot()));
                    if (this.player.isPassenger()) {
                        this.player.absMoveTo(this.player.getX(), this.player.getY(), this.player.getZ(), var4, var5);
                        this.player.getLevel().getChunkSource().move(this.player);
                    } else {
                        double var6 = this.player.getX();
                        double var7 = this.player.getY();
                        double var8 = this.player.getZ();
                        double var9 = this.player.getY();
                        double var10 = var1 - this.firstGoodX;
                        double var11 = var2 - this.firstGoodY;
                        double var12 = var3 - this.firstGoodZ;
                        double var13 = this.player.getDeltaMovement().lengthSqr();
                        double var14 = var10 * var10 + var11 * var11 + var12 * var12;
                        if (this.player.isSleeping()) {
                            if (var14 > 1.0) {
                                this.teleport(this.player.getX(), this.player.getY(), this.player.getZ(), var4, var5);
                            }

                        } else {
                            ++this.receivedMovePacketCount;
                            int var15 = this.receivedMovePacketCount - this.knownMovePacketCount;
                            if (var15 > 5) {
                                LOGGER.debug("{} is sending move packets too frequently ({} packets since last tick)", this.player.getName().getString(), var15);
                                var15 = 1;
                            }

                            if (!this.player.isChangingDimension()
                                && (
                                    !this.player.getLevel().getGameRules().getBoolean(GameRules.RULE_DISABLE_ELYTRA_MOVEMENT_CHECK)
                                        || !this.player.isFallFlying()
                                )) {
                                float var16 = this.player.isFallFlying() ? 300.0F : 100.0F;
                                if (var14 - var13 > (double)(var16 * (float)var15) && !this.isSingleplayerOwner()) {
                                    LOGGER.warn("{} moved too quickly! {},{},{}", this.player.getName().getString(), var10, var11, var12);
                                    this.teleport(this.player.getX(), this.player.getY(), this.player.getZ(), this.player.getYRot(), this.player.getXRot());
                                    return;
                                }
                            }

                            AABB var17 = this.player.getBoundingBox();
                            var10 = var1 - this.lastGoodX;
                            var11 = var2 - this.lastGoodY;
                            var12 = var3 - this.lastGoodZ;
                            boolean var18 = var11 > 0.0;
                            if (this.player.isOnGround() && !param0.isOnGround() && var18) {
                                this.player.jumpFromGround();
                            }

                            boolean var19 = this.player.verticalCollisionBelow;
                            this.player.move(MoverType.PLAYER, new Vec3(var10, var11, var12));
                            var10 = var1 - this.player.getX();
                            var11 = var2 - this.player.getY();
                            if (var11 > -0.5 || var11 < 0.5) {
                                var11 = 0.0;
                            }

                            var12 = var3 - this.player.getZ();
                            var14 = var10 * var10 + var11 * var11 + var12 * var12;
                            boolean var21 = false;
                            if (!this.player.isChangingDimension()
                                && var14 > 0.0625
                                && !this.player.isSleeping()
                                && !this.player.gameMode.isCreative()
                                && this.player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) {
                                var21 = true;
                                LOGGER.warn("{} moved wrongly!", this.player.getName().getString());
                            }

                            this.player.absMoveTo(var1, var2, var3, var4, var5);
                            if (this.player.noPhysics
                                || this.player.isSleeping()
                                || (!var21 || !var0.noCollision(this.player, var17)) && !this.isPlayerCollidingWithAnythingNew(var0, var17)) {
                                this.clientIsFloating = var11 >= -0.03125
                                    && !var19
                                    && this.player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR
                                    && !this.server.isFlightAllowed()
                                    && !this.player.getAbilities().mayfly
                                    && !this.player.hasEffect(MobEffects.LEVITATION)
                                    && !this.player.isFallFlying()
                                    && !this.player.isAutoSpinAttack()
                                    && this.noBlocksAround(this.player);
                                this.player.getLevel().getChunkSource().move(this.player);
                                this.player.doCheckFallDamage(this.player.getY() - var9, param0.isOnGround());
                                this.player.setOnGround(param0.isOnGround());
                                if (var18) {
                                    this.player.resetFallDistance();
                                }

                                this.player.checkMovementStatistics(this.player.getX() - var6, this.player.getY() - var7, this.player.getZ() - var8);
                                this.lastGoodX = this.player.getX();
                                this.lastGoodY = this.player.getY();
                                this.lastGoodZ = this.player.getZ();
                            } else {
                                this.teleport(var6, var7, var8, var4, var5);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isPlayerCollidingWithAnythingNew(LevelReader param0, AABB param1) {
        Iterable<VoxelShape> var0 = param0.getCollisions(this.player, this.player.getBoundingBox().deflate(1.0E-5F));
        VoxelShape var1 = Shapes.create(param1.deflate(1.0E-5F));

        for(VoxelShape var2 : var0) {
            if (!Shapes.joinIsNotEmpty(var2, var1, BooleanOp.AND)) {
                return true;
            }
        }

        return false;
    }

    public void dismount(double param0, double param1, double param2, float param3, float param4) {
        this.teleport(param0, param1, param2, param3, param4, Collections.emptySet(), true);
    }

    public void teleport(double param0, double param1, double param2, float param3, float param4) {
        this.teleport(param0, param1, param2, param3, param4, Collections.emptySet(), false);
    }

    public void teleport(double param0, double param1, double param2, float param3, float param4, Set<ClientboundPlayerPositionPacket.RelativeArgument> param5) {
        this.teleport(param0, param1, param2, param3, param4, param5, false);
    }

    public void teleport(
        double param0, double param1, double param2, float param3, float param4, Set<ClientboundPlayerPositionPacket.RelativeArgument> param5, boolean param6
    ) {
        double var0 = param5.contains(ClientboundPlayerPositionPacket.RelativeArgument.X) ? this.player.getX() : 0.0;
        double var1 = param5.contains(ClientboundPlayerPositionPacket.RelativeArgument.Y) ? this.player.getY() : 0.0;
        double var2 = param5.contains(ClientboundPlayerPositionPacket.RelativeArgument.Z) ? this.player.getZ() : 0.0;
        float var3 = param5.contains(ClientboundPlayerPositionPacket.RelativeArgument.Y_ROT) ? this.player.getYRot() : 0.0F;
        float var4 = param5.contains(ClientboundPlayerPositionPacket.RelativeArgument.X_ROT) ? this.player.getXRot() : 0.0F;
        this.awaitingPositionFromClient = new Vec3(param0, param1, param2);
        if (++this.awaitingTeleport == Integer.MAX_VALUE) {
            this.awaitingTeleport = 0;
        }

        this.awaitingTeleportTime = this.tickCount;
        this.player.absMoveTo(param0, param1, param2, param3, param4);
        this.player
            .connection
            .send(
                new ClientboundPlayerPositionPacket(
                    param0 - var0, param1 - var1, param2 - var2, param3 - var3, param4 - var4, param5, this.awaitingTeleport, param6
                )
            );
    }

    @Override
    public void handlePlayerAction(ServerboundPlayerActionPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        BlockPos var0 = param0.getPos();
        this.player.resetLastActionTime();
        ServerboundPlayerActionPacket.Action var1 = param0.getAction();
        switch(var1) {
            case SWAP_ITEM_WITH_OFFHAND:
                if (!this.player.isSpectator()) {
                    ItemStack var2 = this.player.getItemInHand(InteractionHand.OFF_HAND);
                    this.player.setItemInHand(InteractionHand.OFF_HAND, this.player.getItemInHand(InteractionHand.MAIN_HAND));
                    this.player.setItemInHand(InteractionHand.MAIN_HAND, var2);
                    this.player.stopUsingItem();
                }

                return;
            case DROP_ITEM:
                if (!this.player.isSpectator()) {
                    this.player.drop(false);
                }

                return;
            case DROP_ALL_ITEMS:
                if (!this.player.isSpectator()) {
                    this.player.drop(true);
                }

                return;
            case RELEASE_USE_ITEM:
                this.player.releaseUsingItem();
                return;
            case START_DESTROY_BLOCK:
            case ABORT_DESTROY_BLOCK:
            case STOP_DESTROY_BLOCK:
                this.player.gameMode.handleBlockBreakAction(var0, var1, param0.getDirection(), this.player.level.getMaxBuildHeight(), param0.getSequence());
                this.player.connection.ackBlockChangesUpTo(param0.getSequence());
                return;
            default:
                throw new IllegalArgumentException("Invalid player action");
        }
    }

    private static boolean wasBlockPlacementAttempt(ServerPlayer param0, ItemStack param1) {
        if (param1.isEmpty()) {
            return false;
        } else {
            Item var0 = param1.getItem();
            return (var0 instanceof BlockItem || var0 instanceof BucketItem) && !param0.getCooldowns().isOnCooldown(var0);
        }
    }

    @Override
    public void handleUseItemOn(ServerboundUseItemOnPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        this.player.connection.ackBlockChangesUpTo(param0.getSequence());
        ServerLevel var0 = this.player.getLevel();
        InteractionHand var1 = param0.getHand();
        ItemStack var2 = this.player.getItemInHand(var1);
        BlockHitResult var3 = param0.getHitResult();
        Vec3 var4 = var3.getLocation();
        BlockPos var5 = var3.getBlockPos();
        Vec3 var6 = Vec3.atCenterOf(var5);
        if (!(this.player.getEyePosition().distanceToSqr(var6) > MAX_INTERACTION_DISTANCE)) {
            Vec3 var7 = var4.subtract(var6);
            double var8 = 1.0000001;
            if (Math.abs(var7.x()) < 1.0000001 && Math.abs(var7.y()) < 1.0000001 && Math.abs(var7.z()) < 1.0000001) {
                Direction var9 = var3.getDirection();
                this.player.resetLastActionTime();
                int var10 = this.player.level.getMaxBuildHeight();
                if (var5.getY() < var10) {
                    if (this.awaitingPositionFromClient == null
                        && this.player.distanceToSqr((double)var5.getX() + 0.5, (double)var5.getY() + 0.5, (double)var5.getZ() + 0.5) < 64.0
                        && var0.mayInteract(this.player, var5)) {
                        InteractionResult var11 = this.player.gameMode.useItemOn(this.player, var0, var2, var1, var3);
                        if (var9 == Direction.UP && !var11.consumesAction() && var5.getY() >= var10 - 1 && wasBlockPlacementAttempt(this.player, var2)) {
                            Component var12 = Component.translatable("build.tooHigh", var10 - 1).withStyle(ChatFormatting.RED);
                            this.player.sendSystemMessage(var12, ChatType.GAME_INFO);
                        } else if (var11.shouldSwing()) {
                            this.player.swing(var1, true);
                        }
                    }
                } else {
                    Component var13 = Component.translatable("build.tooHigh", var10 - 1).withStyle(ChatFormatting.RED);
                    this.player.sendSystemMessage(var13, ChatType.GAME_INFO);
                }

                this.player.connection.send(new ClientboundBlockUpdatePacket(var0, var5));
                this.player.connection.send(new ClientboundBlockUpdatePacket(var0, var5.relative(var9)));
            } else {
                LOGGER.warn(
                    "Rejecting UseItemOnPacket from {}: Location {} too far away from hit block {}.", this.player.getGameProfile().getName(), var4, var5
                );
            }
        }
    }

    @Override
    public void handleUseItem(ServerboundUseItemPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        this.ackBlockChangesUpTo(param0.getSequence());
        ServerLevel var0 = this.player.getLevel();
        InteractionHand var1 = param0.getHand();
        ItemStack var2 = this.player.getItemInHand(var1);
        this.player.resetLastActionTime();
        if (!var2.isEmpty()) {
            InteractionResult var3 = this.player.gameMode.useItem(this.player, var0, var2, var1);
            if (var3.shouldSwing()) {
                this.player.swing(var1, true);
            }

        }
    }

    @Override
    public void handleTeleportToEntityPacket(ServerboundTeleportToEntityPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        if (this.player.isSpectator()) {
            for(ServerLevel var0 : this.server.getAllLevels()) {
                Entity var1 = param0.getEntity(var0);
                if (var1 != null) {
                    this.player.teleportTo(var0, var1.getX(), var1.getY(), var1.getZ(), var1.getYRot(), var1.getXRot());
                    return;
                }
            }
        }

    }

    @Override
    public void handleResourcePackResponse(ServerboundResourcePackPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        if (param0.getAction() == ServerboundResourcePackPacket.Action.DECLINED && this.server.isResourcePackRequired()) {
            LOGGER.info("Disconnecting {} due to resource pack rejection", this.player.getName());
            this.disconnect(Component.translatable("multiplayer.requiredTexturePrompt.disconnect"));
        }

    }

    @Override
    public void handlePaddleBoat(ServerboundPaddleBoatPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        Entity var0 = this.player.getVehicle();
        if (var0 instanceof Boat) {
            ((Boat)var0).setPaddleState(param0.getLeft(), param0.getRight());
        }

    }

    @Override
    public void handlePong(ServerboundPongPacket param0) {
    }

    @Override
    public void onDisconnect(Component param0) {
        LOGGER.info("{} lost connection: {}", this.player.getName().getString(), param0.getString());
        this.server.invalidateStatus();
        this.server
            .getPlayerList()
            .broadcastSystemMessage(
                Component.translatable("multiplayer.player.left", this.player.getDisplayName()).withStyle(ChatFormatting.YELLOW), ChatType.SYSTEM
            );
        this.player.disconnect();
        this.server.getPlayerList().remove(this.player);
        this.player.getTextFilter().leave();
        if (this.isSingleplayerOwner()) {
            LOGGER.info("Stopping singleplayer server as player logged out");
            this.server.halt(false);
        }

    }

    public void ackBlockChangesUpTo(int param0) {
        if (param0 < 0) {
            throw new IllegalArgumentException("Expected packet sequence nr >= 0");
        } else {
            this.ackBlockChangesUpTo = Math.max(param0, this.ackBlockChangesUpTo);
        }
    }

    @Override
    public void send(Packet<?> param0) {
        this.send(param0, null);
    }

    public void send(Packet<?> param0, @Nullable GenericFutureListener<? extends Future<? super Void>> param1) {
        try {
            this.connection.send(param0, param1);
        } catch (Throwable var6) {
            CrashReport var1 = CrashReport.forThrowable(var6, "Sending packet");
            CrashReportCategory var2 = var1.addCategory("Packet being sent");
            var2.setDetail("Packet class", () -> param0.getClass().getCanonicalName());
            throw new ReportedException(var1);
        }
    }

    @Override
    public void handleSetCarriedItem(ServerboundSetCarriedItemPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        if (param0.getSlot() >= 0 && param0.getSlot() < Inventory.getSelectionSize()) {
            if (this.player.getInventory().selected != param0.getSlot() && this.player.getUsedItemHand() == InteractionHand.MAIN_HAND) {
                this.player.stopUsingItem();
            }

            this.player.getInventory().selected = param0.getSlot();
            this.player.resetLastActionTime();
        } else {
            LOGGER.warn("{} tried to set an invalid carried item", this.player.getName().getString());
        }
    }

    @Override
    public void handleChat(ServerboundChatPacket param0) {
        if (isChatMessageIllegal(param0.getMessage())) {
            this.disconnect(Component.translatable("multiplayer.disconnect.illegal_characters"));
        } else {
            Instant var0 = param0.getTimeStamp();
            if (!this.isChatExpired(var0) && !this.isChatOutOfOrder(var0)) {
                this.filterTextPacket(param0.getMessage(), param1 -> this.handleChat(param0, param1));
            } else {
                LOGGER.warn("{} tried to send expired or out-of-order message: '{}'", this.player.getName().getString(), param0.getMessage());
            }
        }
    }

    @Override
    public void handleChatCommand(ServerboundChatCommandPacket param0) {
        if (isChatMessageIllegal(param0.command())) {
            this.disconnect(Component.translatable("multiplayer.disconnect.illegal_characters"));
        } else {
            Instant var0 = param0.timeStamp();
            if (!this.isChatExpired(var0) && !this.isChatOutOfOrder(var0)) {
                PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
                if (this.resetLastActionTime()) {
                    CommandSourceStack var1 = this.player.createCommandSourceStack().withSigningContext(param0.signingContext(this.player.getUUID()));
                    this.server.getCommands().performCommand(var1, param0.command());
                    this.detectRateSpam();
                }

            } else {
                LOGGER.warn("{} tried to send expired or out-of-order command: '{}'", this.player.getName().getString(), param0.command());
            }
        }
    }

    private boolean isChatExpired(Instant param0) {
        Instant var0 = param0.plus(ServerboundChatPacket.MESSAGE_EXPIRES_AFTER);
        return Instant.now().isAfter(var0);
    }

    private boolean isChatOutOfOrder(Instant param0) {
        Instant var0;
        do {
            var0 = this.lastChatTimeStamp.get();
            if (param0.isBefore(var0)) {
                return true;
            }
        } while(!this.lastChatTimeStamp.compareAndSet(var0, param0));

        return false;
    }

    private static boolean isChatMessageIllegal(String param0) {
        for(int var0 = 0; var0 < param0.length(); ++var0) {
            if (!SharedConstants.isAllowedChatCharacter(param0.charAt(var0))) {
                return true;
            }
        }

        return false;
    }

    private boolean resetLastActionTime() {
        if (this.player.getChatVisibility() == ChatVisiblity.HIDDEN) {
            Registry<ChatType> var0 = this.player.level.registryAccess().registryOrThrow(Registry.CHAT_TYPE_REGISTRY);
            int var1 = var0.getId(var0.get(ChatType.SYSTEM));
            this.send(new ClientboundSystemChatPacket(Component.translatable("chat.disabled.options").withStyle(ChatFormatting.RED), var1));
            return false;
        } else {
            this.player.resetLastActionTime();
            return true;
        }
    }

    private void handleChat(ServerboundChatPacket param0, FilteredText<String> param1) {
        if (this.resetLastActionTime()) {
            MessageSignature var0 = param0.getSignature(this.player.getUUID());
            boolean var1 = param0.signedPreview();
            ChatDecorator var2 = this.server.getChatDecorator();
            var2.decorateChat(this.player, param1.map(Component::literal), var0, var1).thenAcceptAsync(this::broadcastChatMessage, this.server);
        }

    }

    private void broadcastChatMessage(FilteredText<PlayerChatMessage> param0x) {
        if (!param0x.raw().verify(this.player)) {
            LOGGER.warn("{} sent message with invalid signature: '{}'", this.player.getName().getString(), param0x.raw().signedContent().getString());
        } else {
            this.server.getPlayerList().broadcastChatMessage(param0x, this.player, ChatType.CHAT);
            this.detectRateSpam();
        }
    }

    private void detectRateSpam() {
        this.chatSpamTickCount += 20;
        if (this.chatSpamTickCount > 200 && !this.server.getPlayerList().isOp(this.player.getGameProfile())) {
            this.disconnect(Component.translatable("disconnect.spam"));
        }

    }

    @Override
    public void handleChatPreview(ServerboundChatPreviewPacket param0) {
        if (this.server.previewsChat()) {
            this.chatPreviewThrottler.schedule(() -> {
                int var0 = param0.queryId();
                String var1x = param0.query();
                return this.queryPreview(var1x).thenAccept(param1 -> this.sendPreviewResponse(var0, param1));
            });
        }

    }

    private void sendPreviewResponse(int param0, Component param1) {
        this.send(new ClientboundChatPreviewPacket(param0, param1), param1x -> {
            if (!param1x.isSuccess()) {
                this.send(new ClientboundChatPreviewPacket(param0, null));
            }

        });
    }

    private CompletableFuture<Component> queryPreview(String param0) {
        String var0 = StringUtils.normalizeSpace(param0);
        return var0.startsWith("/") ? this.queryCommandPreview(var0.substring(1)) : this.queryChatPreview(param0);
    }

    private CompletableFuture<Component> queryChatPreview(String param0) {
        Component var0 = Component.literal(param0);
        return this.server.getChatDecorator().decorate(this.player, var0).thenApply(param1 -> !var0.equals(param1) ? param1 : null);
    }

    private CompletableFuture<Component> queryCommandPreview(String param0) {
        CommandSourceStack var0 = this.player.createCommandSourceStack();
        ParseResults<CommandSourceStack> var1 = this.server.getCommands().getDispatcher().parse(param0, var0);
        return this.getPreviewedArgument(var1.getContext());
    }

    private CompletableFuture<Component> getPreviewedArgument(CommandContextBuilder<CommandSourceStack> param0) {
        CommandContextBuilder<CommandSourceStack> var0 = param0.getLastChild();
        if (var0.getArguments().isEmpty()) {
            return CompletableFuture.completedFuture(null);
        } else {
            List<? extends ParsedCommandNode<?>> var1 = var0.getNodes();

            for(int var2 = var1.size() - 1; var2 >= 0; --var2) {
                CommandNode<?> var3 = var1.get(var2).getNode();
                if (var3 instanceof ArgumentCommandNode var4) {
                    try {
                        CompletableFuture<Component> var5 = PreviewedArgument.resolvePreviewed(var4, var0);
                        if (var5 != null) {
                            return var5;
                        }
                    } catch (CommandSyntaxException var8) {
                        return CompletableFuture.completedFuture(null);
                    }
                }
            }

            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public void handleAnimate(ServerboundSwingPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        this.player.resetLastActionTime();
        this.player.swing(param0.getHand());
    }

    @Override
    public void handlePlayerCommand(ServerboundPlayerCommandPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        this.player.resetLastActionTime();
        switch(param0.getAction()) {
            case PRESS_SHIFT_KEY:
                this.player.setShiftKeyDown(true);
                break;
            case RELEASE_SHIFT_KEY:
                this.player.setShiftKeyDown(false);
                break;
            case START_SPRINTING:
                this.player.setSprinting(true);
                break;
            case STOP_SPRINTING:
                this.player.setSprinting(false);
                break;
            case STOP_SLEEPING:
                if (this.player.isSleeping()) {
                    this.player.stopSleepInBed(false, true);
                    this.awaitingPositionFromClient = this.player.position();
                }
                break;
            case START_RIDING_JUMP:
                if (this.player.getVehicle() instanceof PlayerRideableJumping var0) {
                    int var1 = param0.getData();
                    if (var0.canJump() && var1 > 0) {
                        var0.handleStartJump(var1);
                    }
                }
                break;
            case STOP_RIDING_JUMP:
                if (this.player.getVehicle() instanceof PlayerRideableJumping var2) {
                    var2.handleStopJump();
                }
                break;
            case OPEN_INVENTORY: {
                Entity var1 = this.player.getVehicle();
                if (var1 instanceof HasCustomInventoryScreen var3) {
                    var3.openCustomInventoryScreen(this.player);
                }
                break;
            }
            case START_FALL_FLYING:
                if (!this.player.tryToStartFallFlying()) {
                    this.player.stopFallFlying();
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid client command!");
        }

    }

    @Override
    public void handleInteract(ServerboundInteractPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        ServerLevel var0 = this.player.getLevel();
        final Entity var1 = param0.getTarget(var0);
        this.player.resetLastActionTime();
        this.player.setShiftKeyDown(param0.isUsingSecondaryAction());
        if (var1 != null) {
            if (!var0.getWorldBorder().isWithinBounds(var1.blockPosition())) {
                return;
            }

            if (var1.distanceToSqr(this.player.getEyePosition()) < MAX_INTERACTION_DISTANCE) {
                param0.dispatch(
                    new ServerboundInteractPacket.Handler() {
                        private void performInteraction(InteractionHand param0, ServerGamePacketListenerImpl.EntityInteraction param1) {
                            ItemStack var0 = ServerGamePacketListenerImpl.this.player.getItemInHand(param0).copy();
                            InteractionResult var1 = param1.run(ServerGamePacketListenerImpl.this.player, var1, param0);
                            if (var1.consumesAction()) {
                                CriteriaTriggers.PLAYER_INTERACTED_WITH_ENTITY.trigger(ServerGamePacketListenerImpl.this.player, var0, var1);
                                if (var1.shouldSwing()) {
                                    ServerGamePacketListenerImpl.this.player.swing(param0, true);
                                }
                            }
    
                        }
    
                        @Override
                        public void onInteraction(InteractionHand param0) {
                            this.performInteraction(param0, Player::interactOn);
                        }
    
                        @Override
                        public void onInteraction(InteractionHand param0, Vec3 param1) {
                            this.performInteraction(param0, (param1x, param2, param3) -> param2.interactAt(param1x, param1, param3));
                        }
    
                        @Override
                        public void onAttack() {
                            if (!(var1 instanceof ItemEntity)
                                && !(var1 instanceof ExperienceOrb)
                                && !(var1 instanceof AbstractArrow)
                                && var1 != ServerGamePacketListenerImpl.this.player) {
                                ServerGamePacketListenerImpl.this.player.attack(var1);
                            } else {
                                ServerGamePacketListenerImpl.this.disconnect(Component.translatable("multiplayer.disconnect.invalid_entity_attacked"));
                                ServerGamePacketListenerImpl.LOGGER
                                    .warn("Player {} tried to attack an invalid entity", ServerGamePacketListenerImpl.this.player.getName().getString());
                            }
                        }
                    }
                );
            }
        }

    }

    @Override
    public void handleClientCommand(ServerboundClientCommandPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        this.player.resetLastActionTime();
        ServerboundClientCommandPacket.Action var0 = param0.getAction();
        switch(var0) {
            case PERFORM_RESPAWN:
                if (this.player.wonGame) {
                    this.player.wonGame = false;
                    this.player = this.server.getPlayerList().respawn(this.player, true);
                    CriteriaTriggers.CHANGED_DIMENSION.trigger(this.player, Level.END, Level.OVERWORLD);
                } else {
                    if (this.player.getHealth() > 0.0F) {
                        return;
                    }

                    this.player = this.server.getPlayerList().respawn(this.player, false);
                    if (this.server.isHardcore()) {
                        this.player.setGameMode(GameType.SPECTATOR);
                        this.player.getLevel().getGameRules().getRule(GameRules.RULE_SPECTATORSGENERATECHUNKS).set(false, this.server);
                    }
                }
                break;
            case REQUEST_STATS:
                this.player.getStats().sendStats(this.player);
        }

    }

    @Override
    public void handleContainerClose(ServerboundContainerClosePacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        this.player.doCloseContainer();
    }

    @Override
    public void handleContainerClick(ServerboundContainerClickPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        this.player.resetLastActionTime();
        if (this.player.containerMenu.containerId == param0.getContainerId()) {
            if (this.player.isSpectator()) {
                this.player.containerMenu.sendAllDataToRemote();
            } else {
                int var0 = param0.getSlotNum();
                if (!this.player.containerMenu.isValidSlotIndex(var0)) {
                    LOGGER.debug(
                        "Player {} clicked invalid slot index: {}, available slots: {}", this.player.getName(), var0, this.player.containerMenu.slots.size()
                    );
                } else {
                    boolean var1 = param0.getStateId() != this.player.containerMenu.getStateId();
                    this.player.containerMenu.suppressRemoteUpdates();
                    this.player.containerMenu.clicked(var0, param0.getButtonNum(), param0.getClickType(), this.player);

                    for(Entry<ItemStack> var2 : Int2ObjectMaps.fastIterable(param0.getChangedSlots())) {
                        this.player.containerMenu.setRemoteSlotNoCopy(var2.getIntKey(), var2.getValue());
                    }

                    this.player.containerMenu.setRemoteCarried(param0.getCarriedItem());
                    this.player.containerMenu.resumeRemoteUpdates();
                    if (var1) {
                        this.player.containerMenu.broadcastFullState();
                    } else {
                        this.player.containerMenu.broadcastChanges();
                    }

                }
            }
        }
    }

    @Override
    public void handlePlaceRecipe(ServerboundPlaceRecipePacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        this.player.resetLastActionTime();
        if (!this.player.isSpectator()
            && this.player.containerMenu.containerId == param0.getContainerId()
            && this.player.containerMenu instanceof RecipeBookMenu) {
            this.server
                .getRecipeManager()
                .byKey(param0.getRecipe())
                .ifPresent(param1 -> ((RecipeBookMenu)this.player.containerMenu).handlePlacement(param0.isShiftDown(), param1, this.player));
        }
    }

    @Override
    public void handleContainerButtonClick(ServerboundContainerButtonClickPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        this.player.resetLastActionTime();
        if (this.player.containerMenu.containerId == param0.getContainerId() && !this.player.isSpectator()) {
            boolean var0 = this.player.containerMenu.clickMenuButton(this.player, param0.getButtonId());
            if (var0) {
                this.player.containerMenu.broadcastChanges();
            }
        }

    }

    @Override
    public void handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        if (this.player.gameMode.isCreative()) {
            boolean var0 = param0.getSlotNum() < 0;
            ItemStack var1 = param0.getItem();
            CompoundTag var2 = BlockItem.getBlockEntityData(var1);
            if (!var1.isEmpty() && var2 != null && var2.contains("x") && var2.contains("y") && var2.contains("z")) {
                BlockPos var3 = BlockEntity.getPosFromTag(var2);
                BlockEntity var4 = this.player.level.getBlockEntity(var3);
                if (var4 != null) {
                    var4.saveToItem(var1);
                }
            }

            boolean var5 = param0.getSlotNum() >= 1 && param0.getSlotNum() <= 45;
            boolean var6 = var1.isEmpty() || var1.getDamageValue() >= 0 && var1.getCount() <= 64 && !var1.isEmpty();
            if (var5 && var6) {
                this.player.inventoryMenu.getSlot(param0.getSlotNum()).set(var1);
                this.player.inventoryMenu.broadcastChanges();
            } else if (var0 && var6 && this.dropSpamTickCount < 200) {
                this.dropSpamTickCount += 20;
                this.player.drop(var1, true);
            }
        }

    }

    @Override
    public void handleSignUpdate(ServerboundSignUpdatePacket param0) {
        List<String> var0 = Stream.of(param0.getLines()).map(ChatFormatting::stripFormatting).collect(Collectors.toList());
        this.filterTextPacket(var0, param1 -> this.updateSignText(param0, param1));
    }

    private void updateSignText(ServerboundSignUpdatePacket param0, List<FilteredText<String>> param1) {
        this.player.resetLastActionTime();
        ServerLevel var0 = this.player.getLevel();
        BlockPos var1 = param0.getPos();
        if (var0.hasChunkAt(var1)) {
            BlockState var2 = var0.getBlockState(var1);
            BlockEntity var3 = var0.getBlockEntity(var1);
            if (!(var3 instanceof SignBlockEntity)) {
                return;
            }

            SignBlockEntity var4 = (SignBlockEntity)var3;
            if (!var4.isEditable() || !this.player.getUUID().equals(var4.getPlayerWhoMayEdit())) {
                LOGGER.warn("Player {} just tried to change non-editable sign", this.player.getName().getString());
                return;
            }

            for(int var5 = 0; var5 < param1.size(); ++var5) {
                FilteredText<Component> var6 = param1.get(var5).map(Component::literal);
                if (this.player.isTextFilteringEnabled()) {
                    var4.setMessage(var5, var6.filteredOrElse(CommonComponents.EMPTY));
                } else {
                    var4.setMessage(var5, var6.raw(), var6.filteredOrElse(CommonComponents.EMPTY));
                }
            }

            var4.setChanged();
            var0.sendBlockUpdated(var1, var2, var2, 3);
        }

    }

    @Override
    public void handleKeepAlive(ServerboundKeepAlivePacket param0) {
        if (this.keepAlivePending && param0.getId() == this.keepAliveChallenge) {
            int var0 = (int)(Util.getMillis() - this.keepAliveTime);
            this.player.latency = (this.player.latency * 3 + var0) / 4;
            this.keepAlivePending = false;
        } else if (!this.isSingleplayerOwner()) {
            this.disconnect(Component.translatable("disconnect.timeout"));
        }

    }

    @Override
    public void handlePlayerAbilities(ServerboundPlayerAbilitiesPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        this.player.getAbilities().flying = param0.isFlying() && this.player.getAbilities().mayfly;
    }

    @Override
    public void handleClientInformation(ServerboundClientInformationPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        this.player.updateOptions(param0);
    }

    @Override
    public void handleCustomPayload(ServerboundCustomPayloadPacket param0) {
    }

    @Override
    public void handleChangeDifficulty(ServerboundChangeDifficultyPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        if (this.player.hasPermissions(2) || this.isSingleplayerOwner()) {
            this.server.setDifficulty(param0.getDifficulty(), false);
        }
    }

    @Override
    public void handleLockDifficulty(ServerboundLockDifficultyPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        if (this.player.hasPermissions(2) || this.isSingleplayerOwner()) {
            this.server.setDifficultyLocked(param0.isLocked());
        }
    }

    @Override
    public ServerPlayer getPlayer() {
        return this.player;
    }

    @FunctionalInterface
    interface EntityInteraction {
        InteractionResult run(ServerPlayer var1, Entity var2, InteractionHand var3);
    }
}
