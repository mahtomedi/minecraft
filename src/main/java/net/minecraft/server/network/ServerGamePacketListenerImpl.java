package net.minecraft.server.network;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import it.unimi.dsi.fastutil.ints.Int2ShortMap;
import it.unimi.dsi.fastutil.ints.Int2ShortOpenHashMap;
import java.util.Collections;
import java.util.Set;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ClientboundContainerAckPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ClientboundTagQueryPacket;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundBlockEntityTagQuery;
import net.minecraft.network.protocol.game.ServerboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.minecraft.network.protocol.game.ServerboundContainerAckPacket;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.network.protocol.game.ServerboundEntityTagQuery;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
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
import net.minecraft.network.protocol.game.ServerboundRecipeBookUpdatePacket;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WritableBookItem;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerGamePacketListenerImpl implements ServerGamePacketListener {
    private static final Logger LOGGER = LogManager.getLogger();
    public final Connection connection;
    private final MinecraftServer server;
    public ServerPlayer player;
    private int tickCount;
    private long keepAliveTime;
    private boolean keepAlivePending;
    private long keepAliveChallenge;
    private int chatSpamTickCount;
    private int dropSpamTickCount;
    private final Int2ShortMap expectedAcks = new Int2ShortOpenHashMap();
    private double firstGoodX;
    private double firstGoodY;
    private double firstGoodZ;
    private double lastGoodX;
    private double lastGoodY;
    private double lastGoodZ;
    private Entity lastVehicle;
    private double vehicleFirstGoodX;
    private double vehicleFirstGoodY;
    private double vehicleFirstGoodZ;
    private double vehicleLastGoodX;
    private double vehicleLastGoodY;
    private double vehicleLastGoodZ;
    private Vec3 awaitingPositionFromClient;
    private int awaitingTeleport;
    private int awaitingTeleportTime;
    private boolean clientIsFloating;
    private int aboveGroundTickCount;
    private boolean clientVehicleIsFloating;
    private int aboveGroundVehicleTickCount;
    private int receivedMovePacketCount;
    private int knownMovePacketCount;

    public ServerGamePacketListenerImpl(MinecraftServer param0, Connection param1, ServerPlayer param2) {
        this.server = param0;
        this.connection = param1;
        param1.setListener(this);
        this.player = param2;
        param2.connection = this;
    }

    public void tick() {
        this.resetPosition();
        this.player.xo = this.player.getX();
        this.player.yo = this.player.getY();
        this.player.zo = this.player.getZ();
        this.player.doTick();
        this.player.absMoveTo(this.firstGoodX, this.firstGoodY, this.firstGoodZ, this.player.yRot, this.player.xRot);
        ++this.tickCount;
        this.knownMovePacketCount = this.receivedMovePacketCount;
        if (this.clientIsFloating) {
            if (++this.aboveGroundTickCount > 80) {
                LOGGER.warn("{} was kicked for floating too long!", this.player.getName().getString());
                this.disconnect(new TranslatableComponent("multiplayer.disconnect.flying"));
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
                    this.disconnect(new TranslatableComponent("multiplayer.disconnect.flying"));
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
                this.disconnect(new TranslatableComponent("disconnect.timeout"));
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
            this.disconnect(new TranslatableComponent("multiplayer.disconnect.idling"));
        }

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

    @Override
    public void handlePlayerInput(ServerboundPlayerInputPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        this.player.setPlayerInput(param0.getXxa(), param0.getZza(), param0.isJumping(), param0.isShiftKeyDown());
    }

    private static boolean containsInvalidValues(ServerboundMovePlayerPacket param0) {
        if (Doubles.isFinite(param0.getX(0.0))
            && Doubles.isFinite(param0.getY(0.0))
            && Doubles.isFinite(param0.getZ(0.0))
            && Floats.isFinite(param0.getXRot(0.0F))
            && Floats.isFinite(param0.getYRot(0.0F))) {
            return Math.abs(param0.getX(0.0)) > 3.0E7 || Math.abs(param0.getY(0.0)) > 3.0E7 || Math.abs(param0.getZ(0.0)) > 3.0E7;
        } else {
            return true;
        }
    }

    private static boolean containsInvalidValues(ServerboundMoveVehiclePacket param0) {
        return !Doubles.isFinite(param0.getX())
            || !Doubles.isFinite(param0.getY())
            || !Doubles.isFinite(param0.getZ())
            || !Floats.isFinite(param0.getXRot())
            || !Floats.isFinite(param0.getYRot());
    }

    @Override
    public void handleMoveVehicle(ServerboundMoveVehiclePacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        if (containsInvalidValues(param0)) {
            this.disconnect(new TranslatableComponent("multiplayer.disconnect.invalid_vehicle_movement"));
        } else {
            Entity var0 = this.player.getRootVehicle();
            if (var0 != this.player && var0.getControllingPassenger() == this.player && var0 == this.lastVehicle) {
                ServerLevel var1 = this.player.getLevel();
                double var2 = var0.getX();
                double var3 = var0.getY();
                double var4 = var0.getZ();
                double var5 = param0.getX();
                double var6 = param0.getY();
                double var7 = param0.getZ();
                float var8 = param0.getYRot();
                float var9 = param0.getXRot();
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
                var0.move(MoverType.PLAYER, new Vec3(var10, var11, var12));
                var10 = var5 - var0.getX();
                var11 = var6 - var0.getY();
                if (var11 > -0.5 || var11 < 0.5) {
                    var11 = 0.0;
                }

                var12 = var7 - var0.getZ();
                var14 = var10 * var10 + var11 * var11 + var12 * var12;
                boolean var17 = false;
                if (var14 > 0.0625) {
                    var17 = true;
                    LOGGER.warn("{} (vehicle of {}) moved wrongly! {}", var0.getName().getString(), this.player.getName().getString(), Math.sqrt(var14));
                }

                var0.absMoveTo(var5, var6, var7, var8, var9);
                boolean var18 = var1.noCollision(var0, var0.getBoundingBox().deflate(0.0625));
                if (var15 && (var17 || !var18)) {
                    var0.absMoveTo(var2, var3, var4, var8, var9);
                    this.connection.send(new ClientboundMoveVehiclePacket(var0));
                    return;
                }

                this.player.getLevel().getChunkSource().move(this.player);
                this.player.checkMovementStatistics(this.player.getX() - var2, this.player.getY() - var3, this.player.getZ() - var4);
                this.clientVehicleIsFloating = var11 >= -0.03125
                    && !this.server.isFlightAllowed()
                    && !var1.containsAnyBlocks(var0.getBoundingBox().inflate(0.0625).expandTowards(0.0, -0.55, 0.0));
                this.vehicleLastGoodX = var0.getX();
                this.vehicleLastGoodY = var0.getY();
                this.vehicleLastGoodZ = var0.getZ();
            }

        }
    }

    @Override
    public void handleAcceptTeleportPacket(ServerboundAcceptTeleportationPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        if (param0.getId() == this.awaitingTeleport) {
            this.player
                .absMoveTo(
                    this.awaitingPositionFromClient.x, this.awaitingPositionFromClient.y, this.awaitingPositionFromClient.z, this.player.yRot, this.player.xRot
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
    public void handleRecipeBookUpdatePacket(ServerboundRecipeBookUpdatePacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        if (param0.getPurpose() == ServerboundRecipeBookUpdatePacket.Purpose.SHOWN) {
            this.server.getRecipeManager().byKey(param0.getRecipe()).ifPresent(this.player.getRecipeBook()::removeHighlight);
        } else if (param0.getPurpose() == ServerboundRecipeBookUpdatePacket.Purpose.SETTINGS) {
            this.player.getRecipeBook().setGuiOpen(param0.isGuiOpen());
            this.player.getRecipeBook().setFilteringCraftable(param0.isFilteringCraftable());
            this.player.getRecipeBook().setFurnaceGuiOpen(param0.isFurnaceGuiOpen());
            this.player.getRecipeBook().setFurnaceFilteringCraftable(param0.isFurnaceFilteringCraftable());
            this.player.getRecipeBook().setBlastingFurnaceGuiOpen(param0.isBlastFurnaceGuiOpen());
            this.player.getRecipeBook().setBlastingFurnaceFilteringCraftable(param0.isBlastFurnaceFilteringCraftable());
            this.player.getRecipeBook().setSmokerGuiOpen(param0.isSmokerGuiOpen());
            this.player.getRecipeBook().setSmokerFilteringCraftable(param0.isSmokerFilteringCraftable());
        }

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
            this.player.sendMessage(new TranslatableComponent("advMode.notEnabled"));
        } else if (!this.player.canUseGameMasterBlocks()) {
            this.player.sendMessage(new TranslatableComponent("advMode.notAllowed"));
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
                Direction var7 = this.player.level.getBlockState(var2).getValue(CommandBlock.FACING);
                switch(param0.getMode()) {
                    case SEQUENCE:
                        BlockState var8 = Blocks.CHAIN_COMMAND_BLOCK.defaultBlockState();
                        this.player
                            .level
                            .setBlock(
                                var2, var8.setValue(CommandBlock.FACING, var7).setValue(CommandBlock.CONDITIONAL, Boolean.valueOf(param0.isConditional())), 2
                            );
                        break;
                    case AUTO:
                        BlockState var9 = Blocks.REPEATING_COMMAND_BLOCK.defaultBlockState();
                        this.player
                            .level
                            .setBlock(
                                var2, var9.setValue(CommandBlock.FACING, var7).setValue(CommandBlock.CONDITIONAL, Boolean.valueOf(param0.isConditional())), 2
                            );
                        break;
                    case REDSTONE:
                    default:
                        BlockState var10 = Blocks.COMMAND_BLOCK.defaultBlockState();
                        this.player
                            .level
                            .setBlock(
                                var2, var10.setValue(CommandBlock.FACING, var7).setValue(CommandBlock.CONDITIONAL, Boolean.valueOf(param0.isConditional())), 2
                            );
                }

                var3.clearRemoved();
                this.player.level.setBlockEntity(var2, var3);
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
                    this.player.sendMessage(new TranslatableComponent("advMode.setCommand.success", var4));
                }
            }

        }
    }

    @Override
    public void handleSetCommandMinecart(ServerboundSetCommandMinecartPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        if (!this.server.isCommandBlockEnabled()) {
            this.player.sendMessage(new TranslatableComponent("advMode.notEnabled"));
        } else if (!this.player.canUseGameMasterBlocks()) {
            this.player.sendMessage(new TranslatableComponent("advMode.notAllowed"));
        } else {
            BaseCommandBlock var0 = param0.getCommandBlock(this.player.level);
            if (var0 != null) {
                var0.setCommand(param0.getCommand());
                var0.setTrackOutput(param0.isTrackOutput());
                if (!param0.isTrackOutput()) {
                    var0.setLastOutput(null);
                }

                var0.onUpdated();
                this.player.sendMessage(new TranslatableComponent("advMode.setCommand.success", param0.getCommand()));
            }

        }
    }

    @Override
    public void handlePickItem(ServerboundPickItemPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        this.player.inventory.pickSlot(param0.getSlot());
        this.player
            .connection
            .send(new ClientboundContainerSetSlotPacket(-2, this.player.inventory.selected, this.player.inventory.getItem(this.player.inventory.selected)));
        this.player.connection.send(new ClientboundContainerSetSlotPacket(-2, param0.getSlot(), this.player.inventory.getItem(param0.getSlot())));
        this.player.connection.send(new ClientboundSetCarriedItemPacket(this.player.inventory.selected));
    }

    @Override
    public void handleRenameItem(ServerboundRenameItemPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        if (this.player.containerMenu instanceof AnvilMenu) {
            AnvilMenu var0 = (AnvilMenu)this.player.containerMenu;
            String var1 = SharedConstants.filterText(param0.getName());
            if (var1.length() <= 35) {
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
            if (var2 instanceof StructureBlockEntity) {
                StructureBlockEntity var3 = (StructureBlockEntity)var2;
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
                            this.player.displayClientMessage(new TranslatableComponent("structure_block.save_success", var4), false);
                        } else {
                            this.player.displayClientMessage(new TranslatableComponent("structure_block.save_failure", var4), false);
                        }
                    } else if (param0.getUpdateType() == StructureBlockEntity.UpdateType.LOAD_AREA) {
                        if (!var3.isStructureLoadable()) {
                            this.player.displayClientMessage(new TranslatableComponent("structure_block.load_not_found", var4), false);
                        } else if (var3.loadStructure()) {
                            this.player.displayClientMessage(new TranslatableComponent("structure_block.load_success", var4), false);
                        } else {
                            this.player.displayClientMessage(new TranslatableComponent("structure_block.load_prepare", var4), false);
                        }
                    } else if (param0.getUpdateType() == StructureBlockEntity.UpdateType.SCAN_AREA) {
                        if (var3.detectSize()) {
                            this.player.displayClientMessage(new TranslatableComponent("structure_block.size_success", var4), false);
                        } else {
                            this.player.displayClientMessage(new TranslatableComponent("structure_block.size_failure"), false);
                        }
                    }
                } else {
                    this.player.displayClientMessage(new TranslatableComponent("structure_block.invalid_structure_name", param0.getName()), false);
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
            if (var2 instanceof JigsawBlockEntity) {
                JigsawBlockEntity var3 = (JigsawBlockEntity)var2;
                var3.setAttachementType(param0.getAttachementType());
                var3.setTargetPool(param0.getTargetPool());
                var3.setFinalState(param0.getFinalState());
                var3.setChanged();
                this.player.level.sendBlockUpdated(var0, var1, var1, 3);
            }

        }
    }

    @Override
    public void handleSelectTrade(ServerboundSelectTradePacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        int var0 = param0.getItem();
        AbstractContainerMenu var1 = this.player.containerMenu;
        if (var1 instanceof MerchantMenu) {
            MerchantMenu var2 = (MerchantMenu)var1;
            var2.setSelectionHint(var0);
            var2.tryMoveItems(var0);
        }

    }

    @Override
    public void handleEditBook(ServerboundEditBookPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        ItemStack var0 = param0.getBook();
        if (!var0.isEmpty()) {
            if (WritableBookItem.makeSureTagIsValid(var0.getTag())) {
                ItemStack var1 = this.player.getItemInHand(param0.getHand());
                if (var0.getItem() == Items.WRITABLE_BOOK && var1.getItem() == Items.WRITABLE_BOOK) {
                    if (param0.isSigning()) {
                        ItemStack var2 = new ItemStack(Items.WRITTEN_BOOK);
                        CompoundTag var3 = var1.getTag();
                        if (var3 != null) {
                            var2.setTag(var3.copy());
                        }

                        var2.addTagElement("author", StringTag.valueOf(this.player.getName().getString()));
                        var2.addTagElement("title", StringTag.valueOf(var0.getTag().getString("title")));
                        ListTag var4 = var0.getTag().getList("pages", 8);

                        for(int var5 = 0; var5 < var4.size(); ++var5) {
                            String var6 = var4.getString(var5);
                            Component var7 = new TextComponent(var6);
                            var6 = Component.Serializer.toJson(var7);
                            var4.set(var5, (Tag)StringTag.valueOf(var6));
                        }

                        var2.addTagElement("pages", var4);
                        this.player.setItemInHand(param0.getHand(), var2);
                    } else {
                        var1.addTagElement("pages", var0.getTag().getList("pages", 8));
                    }
                }

            }
        }
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
            CompoundTag var1 = var0 != null ? var0.save(new CompoundTag()) : null;
            this.player.connection.send(new ClientboundTagQueryPacket(param0.getTransactionId(), var1));
        }
    }

    @Override
    public void handleMovePlayer(ServerboundMovePlayerPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        if (containsInvalidValues(param0)) {
            this.disconnect(new TranslatableComponent("multiplayer.disconnect.invalid_player_movement"));
        } else {
            ServerLevel var0 = this.server.getLevel(this.player.dimension);
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
                            this.player.yRot,
                            this.player.xRot
                        );
                    }

                } else {
                    this.awaitingTeleportTime = this.tickCount;
                    if (this.player.isPassenger()) {
                        this.player
                            .absMoveTo(
                                this.player.getX(), this.player.getY(), this.player.getZ(), param0.getYRot(this.player.yRot), param0.getXRot(this.player.xRot)
                            );
                        this.player.getLevel().getChunkSource().move(this.player);
                    } else {
                        double var1 = this.player.getX();
                        double var2 = this.player.getY();
                        double var3 = this.player.getZ();
                        double var4 = this.player.getY();
                        double var5 = param0.getX(this.player.getX());
                        double var6 = param0.getY(this.player.getY());
                        double var7 = param0.getZ(this.player.getZ());
                        float var8 = param0.getYRot(this.player.yRot);
                        float var9 = param0.getXRot(this.player.xRot);
                        double var10 = var5 - this.firstGoodX;
                        double var11 = var6 - this.firstGoodY;
                        double var12 = var7 - this.firstGoodZ;
                        double var13 = this.player.getDeltaMovement().lengthSqr();
                        double var14 = var10 * var10 + var11 * var11 + var12 * var12;
                        if (this.player.isSleeping()) {
                            if (var14 > 1.0) {
                                this.teleport(
                                    this.player.getX(),
                                    this.player.getY(),
                                    this.player.getZ(),
                                    param0.getYRot(this.player.yRot),
                                    param0.getXRot(this.player.xRot)
                                );
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
                                    this.teleport(this.player.getX(), this.player.getY(), this.player.getZ(), this.player.yRot, this.player.xRot);
                                    return;
                                }
                            }

                            boolean var17 = this.isPlayerCollidingWithAnything(var0);
                            var10 = var5 - this.lastGoodX;
                            var11 = var6 - this.lastGoodY;
                            var12 = var7 - this.lastGoodZ;
                            if (var11 > 0.0) {
                                this.player.fallDistance = 0.0F;
                            }

                            if (this.player.isOnGround() && !param0.isOnGround() && var11 > 0.0) {
                                this.player.jumpFromGround();
                            }

                            this.player.move(MoverType.PLAYER, new Vec3(var10, var11, var12));
                            var10 = var5 - this.player.getX();
                            var11 = var6 - this.player.getY();
                            if (var11 > -0.5 || var11 < 0.5) {
                                var11 = 0.0;
                            }

                            var12 = var7 - this.player.getZ();
                            var14 = var10 * var10 + var11 * var11 + var12 * var12;
                            boolean var19 = false;
                            if (!this.player.isChangingDimension()
                                && var14 > 0.0625
                                && !this.player.isSleeping()
                                && !this.player.gameMode.isCreative()
                                && this.player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) {
                                var19 = true;
                                LOGGER.warn("{} moved wrongly!", this.player.getName().getString());
                            }

                            this.player.absMoveTo(var5, var6, var7, var8, var9);
                            this.player.checkMovementStatistics(this.player.getX() - var1, this.player.getY() - var2, this.player.getZ() - var3);
                            if (!this.player.noPhysics && !this.player.isSleeping()) {
                                boolean var20 = this.isPlayerCollidingWithAnything(var0);
                                if (var17 && (var19 || !var20)) {
                                    this.teleport(var1, var2, var3, var8, var9);
                                    return;
                                }
                            }

                            this.clientIsFloating = var11 >= -0.03125
                                && this.player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR
                                && !this.server.isFlightAllowed()
                                && !this.player.abilities.mayfly
                                && !this.player.hasEffect(MobEffects.LEVITATION)
                                && !this.player.isFallFlying()
                                && !var0.containsAnyBlocks(this.player.getBoundingBox().inflate(0.0625).expandTowards(0.0, -0.55, 0.0));
                            this.player.getLevel().getChunkSource().move(this.player);
                            this.player.doCheckFallDamage(this.player.getY() - var4, param0.isOnGround());
                            this.player.setOnGround(param0.isOnGround());
                            this.lastGoodX = this.player.getX();
                            this.lastGoodY = this.player.getY();
                            this.lastGoodZ = this.player.getZ();
                        }
                    }
                }
            }
        }
    }

    private boolean isPlayerCollidingWithAnything(LevelReader param0) {
        return param0.noCollision(this.player, this.player.getBoundingBox().deflate(1.0E-5F));
    }

    public void teleport(double param0, double param1, double param2, float param3, float param4) {
        this.teleport(param0, param1, param2, param3, param4, Collections.emptySet());
    }

    public void teleport(double param0, double param1, double param2, float param3, float param4, Set<ClientboundPlayerPositionPacket.RelativeArgument> param5) {
        double var0 = param5.contains(ClientboundPlayerPositionPacket.RelativeArgument.X) ? this.player.getX() : 0.0;
        double var1 = param5.contains(ClientboundPlayerPositionPacket.RelativeArgument.Y) ? this.player.getY() : 0.0;
        double var2 = param5.contains(ClientboundPlayerPositionPacket.RelativeArgument.Z) ? this.player.getZ() : 0.0;
        float var3 = param5.contains(ClientboundPlayerPositionPacket.RelativeArgument.Y_ROT) ? this.player.yRot : 0.0F;
        float var4 = param5.contains(ClientboundPlayerPositionPacket.RelativeArgument.X_ROT) ? this.player.xRot : 0.0F;
        this.awaitingPositionFromClient = new Vec3(param0, param1, param2);
        if (++this.awaitingTeleport == Integer.MAX_VALUE) {
            this.awaitingTeleport = 0;
        }

        this.awaitingTeleportTime = this.tickCount;
        this.player.absMoveTo(param0, param1, param2, param3, param4);
        this.player
            .connection
            .send(new ClientboundPlayerPositionPacket(param0 - var0, param1 - var1, param2 - var2, param3 - var3, param4 - var4, param5, this.awaitingTeleport));
    }

    @Override
    public void handlePlayerAction(ServerboundPlayerActionPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        BlockPos var0 = param0.getPos();
        this.player.resetLastActionTime();
        ServerboundPlayerActionPacket.Action var1 = param0.getAction();
        switch(var1) {
            case SWAP_HELD_ITEMS:
                if (!this.player.isSpectator()) {
                    ItemStack var2 = this.player.getItemInHand(InteractionHand.OFF_HAND);
                    this.player.setItemInHand(InteractionHand.OFF_HAND, this.player.getItemInHand(InteractionHand.MAIN_HAND));
                    this.player.setItemInHand(InteractionHand.MAIN_HAND, var2);
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
                this.player.gameMode.handleBlockBreakAction(var0, var1, param0.getDirection(), this.server.getMaxBuildHeight());
                return;
            default:
                throw new IllegalArgumentException("Invalid player action");
        }
    }

    @Override
    public void handleUseItemOn(ServerboundUseItemOnPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        ServerLevel var0 = this.server.getLevel(this.player.dimension);
        InteractionHand var1 = param0.getHand();
        ItemStack var2 = this.player.getItemInHand(var1);
        BlockHitResult var3 = param0.getHitResult();
        BlockPos var4 = var3.getBlockPos();
        Direction var5 = var3.getDirection();
        this.player.resetLastActionTime();
        if (var4.getY() < this.server.getMaxBuildHeight() - 1 || var5 != Direction.UP && var4.getY() < this.server.getMaxBuildHeight()) {
            if (this.awaitingPositionFromClient == null
                && this.player.distanceToSqr((double)var4.getX() + 0.5, (double)var4.getY() + 0.5, (double)var4.getZ() + 0.5) < 64.0
                && var0.mayInteract(this.player, var4)) {
                InteractionResult var6 = this.player.gameMode.useItemOn(this.player, var0, var2, var1, var3);
                if (var6.shouldSwing()) {
                    this.player.swing(var1, true);
                }
            }
        } else {
            Component var7 = new TranslatableComponent("build.tooHigh", this.server.getMaxBuildHeight()).withStyle(ChatFormatting.RED);
            this.player.connection.send(new ClientboundChatPacket(var7, ChatType.GAME_INFO));
        }

        this.player.connection.send(new ClientboundBlockUpdatePacket(var0, var4));
        this.player.connection.send(new ClientboundBlockUpdatePacket(var0, var4.relative(var5)));
    }

    @Override
    public void handleUseItem(ServerboundUseItemPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        ServerLevel var0 = this.server.getLevel(this.player.dimension);
        InteractionHand var1 = param0.getHand();
        ItemStack var2 = this.player.getItemInHand(var1);
        this.player.resetLastActionTime();
        if (!var2.isEmpty()) {
            this.player.gameMode.useItem(this.player, var0, var2, var1);
        }
    }

    @Override
    public void handleTeleportToEntityPacket(ServerboundTeleportToEntityPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        if (this.player.isSpectator()) {
            for(ServerLevel var0 : this.server.getAllLevels()) {
                Entity var1 = param0.getEntity(var0);
                if (var1 != null) {
                    this.player.teleportTo(var0, var1.getX(), var1.getY(), var1.getZ(), var1.yRot, var1.xRot);
                    return;
                }
            }
        }

    }

    @Override
    public void handleResourcePackResponse(ServerboundResourcePackPacket param0) {
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
    public void onDisconnect(Component param0) {
        LOGGER.info("{} lost connection: {}", this.player.getName().getString(), param0.getString());
        this.server.invalidateStatus();
        this.server
            .getPlayerList()
            .broadcastMessage(new TranslatableComponent("multiplayer.player.left", this.player.getDisplayName()).withStyle(ChatFormatting.YELLOW));
        this.player.disconnect();
        this.server.getPlayerList().remove(this.player);
        if (this.isSingleplayerOwner()) {
            LOGGER.info("Stopping singleplayer server as player logged out");
            this.server.halt(false);
        }

    }

    public void send(Packet<?> param0) {
        this.send(param0, null);
    }

    public void send(Packet<?> param0, @Nullable GenericFutureListener<? extends Future<? super Void>> param1) {
        if (param0 instanceof ClientboundChatPacket) {
            ClientboundChatPacket var0 = (ClientboundChatPacket)param0;
            ChatVisiblity var1 = this.player.getChatVisibility();
            if (var1 == ChatVisiblity.HIDDEN && var0.getType() != ChatType.GAME_INFO) {
                return;
            }

            if (var1 == ChatVisiblity.SYSTEM && !var0.isSystem()) {
                return;
            }
        }

        try {
            this.connection.send(param0, param1);
        } catch (Throwable var6) {
            CrashReport var3 = CrashReport.forThrowable(var6, "Sending packet");
            CrashReportCategory var4 = var3.addCategory("Packet being sent");
            var4.setDetail("Packet class", () -> param0.getClass().getCanonicalName());
            throw new ReportedException(var3);
        }
    }

    @Override
    public void handleSetCarriedItem(ServerboundSetCarriedItemPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        if (param0.getSlot() >= 0 && param0.getSlot() < Inventory.getSelectionSize()) {
            this.player.inventory.selected = param0.getSlot();
            this.player.resetLastActionTime();
        } else {
            LOGGER.warn("{} tried to set an invalid carried item", this.player.getName().getString());
        }
    }

    @Override
    public void handleChat(ServerboundChatPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        if (this.player.getChatVisibility() == ChatVisiblity.HIDDEN) {
            this.send(new ClientboundChatPacket(new TranslatableComponent("chat.cannotSend").withStyle(ChatFormatting.RED)));
        } else {
            this.player.resetLastActionTime();
            String var0 = param0.getMessage();
            var0 = StringUtils.normalizeSpace(var0);

            for(int var1 = 0; var1 < var0.length(); ++var1) {
                if (!SharedConstants.isAllowedChatCharacter(var0.charAt(var1))) {
                    this.disconnect(new TranslatableComponent("multiplayer.disconnect.illegal_characters"));
                    return;
                }
            }

            if (var0.startsWith("/")) {
                this.handleCommand(var0);
            } else {
                Component var2 = new TranslatableComponent("chat.type.text", this.player.getDisplayName(), var0);
                this.server.getPlayerList().broadcastMessage(var2, false);
            }

            this.chatSpamTickCount += 20;
            if (this.chatSpamTickCount > 200 && !this.server.getPlayerList().isOp(this.player.getGameProfile())) {
                this.disconnect(new TranslatableComponent("disconnect.spam"));
            }

        }
    }

    private void handleCommand(String param0) {
        this.server.getCommands().performCommand(this.player.createCommandSourceStack(), param0);
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
                if (this.player.getVehicle() instanceof PlayerRideableJumping) {
                    PlayerRideableJumping var0 = (PlayerRideableJumping)this.player.getVehicle();
                    int var1 = param0.getData();
                    if (var0.canJump() && var1 > 0) {
                        var0.handleStartJump(var1);
                    }
                }
                break;
            case STOP_RIDING_JUMP:
                if (this.player.getVehicle() instanceof PlayerRideableJumping) {
                    PlayerRideableJumping var2 = (PlayerRideableJumping)this.player.getVehicle();
                    var2.handleStopJump();
                }
                break;
            case OPEN_INVENTORY:
                if (this.player.getVehicle() instanceof AbstractHorse) {
                    ((AbstractHorse)this.player.getVehicle()).openInventory(this.player);
                }
                break;
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
        ServerLevel var0 = this.server.getLevel(this.player.dimension);
        Entity var1 = param0.getTarget(var0);
        this.player.resetLastActionTime();
        if (var1 != null) {
            boolean var2 = this.player.canSee(var1);
            double var3 = 36.0;
            if (!var2) {
                var3 = 9.0;
            }

            if (this.player.distanceToSqr(var1) < var3) {
                if (param0.getAction() == ServerboundInteractPacket.Action.INTERACT) {
                    InteractionHand var4 = param0.getHand();
                    this.player.interactOn(var1, var4);
                } else if (param0.getAction() == ServerboundInteractPacket.Action.INTERACT_AT) {
                    InteractionHand var5 = param0.getHand();
                    InteractionResult var6 = var1.interactAt(this.player, param0.getLocation(), var5);
                    if (var6.shouldSwing()) {
                        this.player.swing(var5, true);
                    }
                } else if (param0.getAction() == ServerboundInteractPacket.Action.ATTACK) {
                    if (var1 instanceof ItemEntity || var1 instanceof ExperienceOrb || var1 instanceof AbstractArrow || var1 == this.player) {
                        this.disconnect(new TranslatableComponent("multiplayer.disconnect.invalid_entity_attacked"));
                        this.server.warn("Player " + this.player.getName().getString() + " tried to attack an invalid entity");
                        return;
                    }

                    this.player.attack(var1);
                }
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
                    CriteriaTriggers.CHANGED_DIMENSION.trigger(this.player, DimensionType.THE_END, DimensionType.OVERWORLD);
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
        if (this.player.containerMenu.containerId == param0.getContainerId() && this.player.containerMenu.isSynched(this.player)) {
            if (this.player.isSpectator()) {
                NonNullList<ItemStack> var0 = NonNullList.create();

                for(int var1 = 0; var1 < this.player.containerMenu.slots.size(); ++var1) {
                    var0.add(this.player.containerMenu.slots.get(var1).getItem());
                }

                this.player.refreshContainer(this.player.containerMenu, var0);
            } else {
                ItemStack var2 = this.player.containerMenu.clicked(param0.getSlotNum(), param0.getButtonNum(), param0.getClickType(), this.player);
                if (ItemStack.matches(param0.getItem(), var2)) {
                    this.player.connection.send(new ClientboundContainerAckPacket(param0.getContainerId(), param0.getUid(), true));
                    this.player.ignoreSlotUpdateHack = true;
                    this.player.containerMenu.broadcastChanges();
                    this.player.broadcastCarriedItem();
                    this.player.ignoreSlotUpdateHack = false;
                } else {
                    this.expectedAcks.put(this.player.containerMenu.containerId, param0.getUid());
                    this.player.connection.send(new ClientboundContainerAckPacket(param0.getContainerId(), param0.getUid(), false));
                    this.player.containerMenu.setSynched(this.player, false);
                    NonNullList<ItemStack> var3 = NonNullList.create();

                    for(int var4 = 0; var4 < this.player.containerMenu.slots.size(); ++var4) {
                        ItemStack var5 = this.player.containerMenu.slots.get(var4).getItem();
                        var3.add(var5.isEmpty() ? ItemStack.EMPTY : var5);
                    }

                    this.player.refreshContainer(this.player.containerMenu, var3);
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
            && this.player.containerMenu.isSynched(this.player)
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
        if (this.player.containerMenu.containerId == param0.getContainerId() && this.player.containerMenu.isSynched(this.player) && !this.player.isSpectator()) {
            this.player.containerMenu.clickMenuButton(this.player, param0.getButtonId());
            this.player.containerMenu.broadcastChanges();
        }

    }

    @Override
    public void handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        if (this.player.gameMode.isCreative()) {
            boolean var0 = param0.getSlotNum() < 0;
            ItemStack var1 = param0.getItem();
            CompoundTag var2 = var1.getTagElement("BlockEntityTag");
            if (!var1.isEmpty() && var2 != null && var2.contains("x") && var2.contains("y") && var2.contains("z")) {
                BlockPos var3 = new BlockPos(var2.getInt("x"), var2.getInt("y"), var2.getInt("z"));
                BlockEntity var4 = this.player.level.getBlockEntity(var3);
                if (var4 != null) {
                    CompoundTag var5 = var4.save(new CompoundTag());
                    var5.remove("x");
                    var5.remove("y");
                    var5.remove("z");
                    var1.addTagElement("BlockEntityTag", var5);
                }
            }

            boolean var6 = param0.getSlotNum() >= 1 && param0.getSlotNum() <= 45;
            boolean var7 = var1.isEmpty() || var1.getDamageValue() >= 0 && var1.getCount() <= 64 && !var1.isEmpty();
            if (var6 && var7) {
                if (var1.isEmpty()) {
                    this.player.inventoryMenu.setItem(param0.getSlotNum(), ItemStack.EMPTY);
                } else {
                    this.player.inventoryMenu.setItem(param0.getSlotNum(), var1);
                }

                this.player.inventoryMenu.setSynched(this.player, true);
                this.player.inventoryMenu.broadcastChanges();
            } else if (var0 && var7 && this.dropSpamTickCount < 200) {
                this.dropSpamTickCount += 20;
                this.player.drop(var1, true);
            }
        }

    }

    @Override
    public void handleContainerAck(ServerboundContainerAckPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        int var0 = this.player.containerMenu.containerId;
        if (var0 == param0.getContainerId()
            && this.expectedAcks.getOrDefault(var0, (short)(param0.getUid() + 1)) == param0.getUid()
            && !this.player.containerMenu.isSynched(this.player)
            && !this.player.isSpectator()) {
            this.player.containerMenu.setSynched(this.player, true);
        }

    }

    @Override
    public void handleSignUpdate(ServerboundSignUpdatePacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        this.player.resetLastActionTime();
        ServerLevel var0 = this.server.getLevel(this.player.dimension);
        BlockPos var1 = param0.getPos();
        if (var0.hasChunkAt(var1)) {
            BlockState var2 = var0.getBlockState(var1);
            BlockEntity var3 = var0.getBlockEntity(var1);
            if (!(var3 instanceof SignBlockEntity)) {
                return;
            }

            SignBlockEntity var4 = (SignBlockEntity)var3;
            if (!var4.isEditable() || var4.getPlayerWhoMayEdit() != this.player) {
                this.server.warn("Player " + this.player.getName().getString() + " just tried to change non-editable sign");
                return;
            }

            String[] var5 = param0.getLines();

            for(int var6 = 0; var6 < var5.length; ++var6) {
                var4.setMessage(var6, new TextComponent(ChatFormatting.stripFormatting(var5[var6])));
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
            this.disconnect(new TranslatableComponent("disconnect.timeout"));
        }

    }

    @Override
    public void handlePlayerAbilities(ServerboundPlayerAbilitiesPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.player.getLevel());
        this.player.abilities.flying = param0.isFlying() && this.player.abilities.mayfly;
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
}
