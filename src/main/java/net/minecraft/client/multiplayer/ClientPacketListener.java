package net.minecraft.client.multiplayer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.logging.LogUtils;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.DebugQueryHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.DemoIntroScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.client.gui.screens.achievement.StatsUpdateListener;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.inventory.CommandBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.debug.BeeDebugRenderer;
import net.minecraft.client.renderer.debug.BrainDebugRenderer;
import net.minecraft.client.renderer.debug.GoalSelectorDebugRenderer;
import net.minecraft.client.renderer.debug.NeighborsUpdateRenderer;
import net.minecraft.client.renderer.debug.WorldGenAttemptRenderer;
import net.minecraft.client.resources.sounds.BeeAggressiveSoundInstance;
import net.minecraft.client.resources.sounds.BeeFlyingSoundInstance;
import net.minecraft.client.resources.sounds.BeeSoundInstance;
import net.minecraft.client.resources.sounds.GuardianAttackSoundInstance;
import net.minecraft.client.resources.sounds.MinecartSoundInstance;
import net.minecraft.client.resources.sounds.SnifferSoundInstance;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.client.telemetry.WorldSessionTelemetryManager;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Position;
import net.minecraft.core.PositionImpl;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.LastSeenMessagesTracker;
import net.minecraft.network.chat.LocalChatSession;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MessageSignatureCache;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.chat.SignableCommand;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.chat.SignedMessageChain;
import net.minecraft.network.chat.SignedMessageLink;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundAddExperienceOrbPacket;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundAwardStatsPacket;
import net.minecraft.network.protocol.game.ClientboundBlockChangedAckPacket;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundChunksBiomesPacket;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundCooldownPacket;
import net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ClientboundDeleteChatPacket;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundHorseScreenOpenPacket;
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.network.protocol.game.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacketData;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.network.protocol.game.ClientboundPingPacket;
import net.minecraft.network.protocol.game.ClientboundPlaceGhostRecipePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatEndPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatEnterPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerLookAtPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRecipePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundResourcePackPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSelectAdvancementsTabPacket;
import net.minecraft.network.protocol.game.ClientboundServerDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderLerpSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDelayPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDistancePacket;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.network.protocol.game.ClientboundSetSimulationDistancePacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.network.protocol.game.ClientboundTagQueryPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateEnabledFeaturesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundChatAckPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundChatSessionUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPongPacket;
import net.minecraft.network.protocol.game.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.game.VecDeltaCodec;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.realms.DisconnectedRealmsScreen;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatsCounter;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraft.util.Crypt;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SignatureValidator;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.ProfileKeyPair;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ClientPacketListener implements TickablePacketListener, ClientGamePacketListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component GENERIC_DISCONNECT_MESSAGE = Component.translatable("disconnect.lost");
    private static final Component UNSECURE_SERVER_TOAST_TITLE = Component.translatable("multiplayer.unsecureserver.toast.title");
    private static final Component UNSERURE_SERVER_TOAST = Component.translatable("multiplayer.unsecureserver.toast");
    private static final Component INVALID_PACKET = Component.translatable("multiplayer.disconnect.invalid_packet");
    private static final Component CHAT_VALIDATION_FAILED_ERROR = Component.translatable("multiplayer.disconnect.chat_validation_failed");
    private static final int PENDING_OFFSET_THRESHOLD = 64;
    private final Connection connection;
    private final List<ClientPacketListener.DeferredPacket> deferredPackets = new ArrayList<>();
    @Nullable
    private final ServerData serverData;
    private final GameProfile localGameProfile;
    private final Screen callbackScreen;
    private final Minecraft minecraft;
    private ClientLevel level;
    private ClientLevel.ClientLevelData levelData;
    private final Map<UUID, PlayerInfo> playerInfoMap = Maps.newHashMap();
    private final Set<PlayerInfo> listedPlayers = new ReferenceOpenHashSet<>();
    private final ClientAdvancements advancements;
    private final ClientSuggestionProvider suggestionsProvider;
    private final DebugQueryHandler debugQueryHandler = new DebugQueryHandler(this);
    private int serverChunkRadius = 3;
    private int serverSimulationDistance = 3;
    private final RandomSource random = RandomSource.createThreadSafe();
    private CommandDispatcher<SharedSuggestionProvider> commands = new CommandDispatcher<>();
    private final RecipeManager recipeManager = new RecipeManager();
    private final UUID id = UUID.randomUUID();
    private Set<ResourceKey<Level>> levels;
    private LayeredRegistryAccess<ClientRegistryLayer> registryAccess = ClientRegistryLayer.createRegistryAccess();
    private FeatureFlagSet enabledFeatures = FeatureFlags.DEFAULT_FLAGS;
    private final WorldSessionTelemetryManager telemetryManager;
    @Nullable
    private LocalChatSession chatSession;
    private SignedMessageChain.Encoder signedMessageEncoder = SignedMessageChain.Encoder.UNSIGNED;
    private LastSeenMessagesTracker lastSeenMessages = new LastSeenMessagesTracker(20);
    private MessageSignatureCache messageSignatureCache = MessageSignatureCache.createDefault();

    public ClientPacketListener(
        Minecraft param0, Screen param1, Connection param2, @Nullable ServerData param3, GameProfile param4, WorldSessionTelemetryManager param5
    ) {
        this.minecraft = param0;
        this.callbackScreen = param1;
        this.connection = param2;
        this.serverData = param3;
        this.localGameProfile = param4;
        this.advancements = new ClientAdvancements(param0, param5);
        this.suggestionsProvider = new ClientSuggestionProvider(this, param0);
        this.telemetryManager = param5;
    }

    public ClientSuggestionProvider getSuggestionsProvider() {
        return this.suggestionsProvider;
    }

    public void close() {
        this.level = null;
        this.telemetryManager.onDisconnect();
    }

    public RecipeManager getRecipeManager() {
        return this.recipeManager;
    }

    @Override
    public void handleLogin(ClientboundLoginPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.minecraft.gameMode = new MultiPlayerGameMode(this.minecraft, this);
        this.registryAccess = this.registryAccess.replaceFrom(ClientRegistryLayer.REMOTE, param0.registryHolder());
        if (!this.connection.isMemoryConnection()) {
            this.registryAccess.compositeAccess().registries().forEach(param0x -> param0x.value().resetTags());
        }

        List<ResourceKey<Level>> var0 = Lists.newArrayList(param0.levels());
        Collections.shuffle(var0);
        this.levels = Sets.newLinkedHashSet(var0);
        ResourceKey<Level> var1 = param0.dimension();
        Holder<DimensionType> var2 = this.registryAccess
            .compositeAccess()
            .<DimensionType>registryOrThrow(Registries.DIMENSION_TYPE)
            .getHolderOrThrow(param0.dimensionType());
        this.serverChunkRadius = param0.chunkRadius();
        this.serverSimulationDistance = param0.simulationDistance();
        boolean var3 = param0.isDebug();
        boolean var4 = param0.isFlat();
        ClientLevel.ClientLevelData var5 = new ClientLevel.ClientLevelData(Difficulty.NORMAL, param0.hardcore(), var4);
        this.levelData = var5;
        this.level = new ClientLevel(
            this,
            var5,
            var1,
            var2,
            this.serverChunkRadius,
            this.serverSimulationDistance,
            this.minecraft::getProfiler,
            this.minecraft.levelRenderer,
            var3,
            param0.seed()
        );
        this.minecraft.setLevel(this.level);
        if (this.minecraft.player == null) {
            this.minecraft.player = this.minecraft.gameMode.createPlayer(this.level, new StatsCounter(), new ClientRecipeBook());
            this.minecraft.player.setYRot(-180.0F);
            if (this.minecraft.getSingleplayerServer() != null) {
                this.minecraft.getSingleplayerServer().setUUID(this.minecraft.player.getUUID());
            }
        }

        this.minecraft.debugRenderer.clear();
        this.minecraft.player.resetPos();
        int var6 = param0.playerId();
        this.minecraft.player.setId(var6);
        this.level.addPlayer(var6, this.minecraft.player);
        this.minecraft.player.input = new KeyboardInput(this.minecraft.options);
        this.minecraft.gameMode.adjustPlayer(this.minecraft.player);
        this.minecraft.cameraEntity = this.minecraft.player;
        this.minecraft.setScreen(new ReceivingLevelScreen());
        this.minecraft.player.setReducedDebugInfo(param0.reducedDebugInfo());
        this.minecraft.player.setShowDeathScreen(param0.showDeathScreen());
        this.minecraft.player.setLastDeathLocation(param0.lastDeathLocation());
        this.minecraft.player.setPortalCooldown(param0.portalCooldown());
        this.minecraft.gameMode.setLocalMode(param0.gameType(), param0.previousGameType());
        this.minecraft.options.setServerRenderDistance(param0.chunkRadius());
        this.minecraft.options.broadcastOptions();
        this.connection
            .send(
                new ServerboundCustomPayloadPacket(
                    ServerboundCustomPayloadPacket.BRAND, new FriendlyByteBuf(Unpooled.buffer()).writeUtf(ClientBrandRetriever.getClientModName())
                )
            );
        this.chatSession = null;
        this.lastSeenMessages = new LastSeenMessagesTracker(20);
        this.messageSignatureCache = MessageSignatureCache.createDefault();
        if (this.connection.isEncrypted()) {
            this.minecraft.getProfileKeyPairManager().prepareKeyPair().thenAcceptAsync(param0x -> param0x.ifPresent(this::setKeyPair), this.minecraft);
        }

        this.telemetryManager.onPlayerInfoReceived(param0.gameType(), param0.hardcore());
        this.minecraft.quickPlayLog().log(this.minecraft);
    }

    @Override
    public void handleAddEntity(ClientboundAddEntityPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        EntityType<?> var0 = param0.getType();
        Entity var1 = var0.create(this.level);
        if (var1 != null) {
            var1.recreateFromPacket(param0);
            int var2 = param0.getId();
            this.level.putNonPlayerEntity(var2, var1);
            this.postAddEntitySoundInstance(var1);
        } else {
            LOGGER.warn("Skipping Entity with id {}", var0);
        }

    }

    private void postAddEntitySoundInstance(Entity param0) {
        if (param0 instanceof AbstractMinecart) {
            this.minecraft.getSoundManager().play(new MinecartSoundInstance((AbstractMinecart)param0));
        } else if (param0 instanceof Bee) {
            boolean var0 = ((Bee)param0).isAngry();
            BeeSoundInstance var1;
            if (var0) {
                var1 = new BeeAggressiveSoundInstance((Bee)param0);
            } else {
                var1 = new BeeFlyingSoundInstance((Bee)param0);
            }

            this.minecraft.getSoundManager().queueTickingSound(var1);
        }

    }

    @Override
    public void handleAddExperienceOrb(ClientboundAddExperienceOrbPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        double var0 = param0.getX();
        double var1 = param0.getY();
        double var2 = param0.getZ();
        Entity var3 = new ExperienceOrb(this.level, var0, var1, var2, param0.getValue());
        var3.syncPacketPositionCodec(var0, var1, var2);
        var3.setYRot(0.0F);
        var3.setXRot(0.0F);
        var3.setId(param0.getId());
        this.level.putNonPlayerEntity(param0.getId(), var3);
    }

    @Override
    public void handleSetEntityMotion(ClientboundSetEntityMotionPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Entity var0 = this.level.getEntity(param0.getId());
        if (var0 != null) {
            var0.lerpMotion((double)param0.getXa() / 8000.0, (double)param0.getYa() / 8000.0, (double)param0.getZa() / 8000.0);
        }
    }

    @Override
    public void handleSetEntityData(ClientboundSetEntityDataPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Entity var0 = this.level.getEntity(param0.id());
        if (var0 != null) {
            var0.getEntityData().assignValues(param0.packedItems());
        }

    }

    @Override
    public void handleAddPlayer(ClientboundAddPlayerPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        PlayerInfo var0 = this.getPlayerInfo(param0.getPlayerId());
        if (var0 == null) {
            LOGGER.warn("Server attempted to add player prior to sending player info (Player id {})", param0.getPlayerId());
        } else {
            double var1 = param0.getX();
            double var2 = param0.getY();
            double var3 = param0.getZ();
            float var4 = (float)(param0.getyRot() * 360) / 256.0F;
            float var5 = (float)(param0.getxRot() * 360) / 256.0F;
            int var6 = param0.getEntityId();
            RemotePlayer var7 = new RemotePlayer(this.minecraft.level, var0.getProfile());
            var7.setId(var6);
            var7.syncPacketPositionCodec(var1, var2, var3);
            var7.absMoveTo(var1, var2, var3, var4, var5);
            var7.setOldPosAndRot();
            this.level.addPlayer(var6, var7);
        }
    }

    @Override
    public void handleTeleportEntity(ClientboundTeleportEntityPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Entity var0 = this.level.getEntity(param0.getId());
        if (var0 != null) {
            double var1 = param0.getX();
            double var2 = param0.getY();
            double var3 = param0.getZ();
            var0.syncPacketPositionCodec(var1, var2, var3);
            if (!var0.isControlledByLocalInstance()) {
                float var4 = (float)(param0.getyRot() * 360) / 256.0F;
                float var5 = (float)(param0.getxRot() * 360) / 256.0F;
                var0.lerpTo(var1, var2, var3, var4, var5, 3, true);
                var0.setOnGround(param0.isOnGround());
            }

        }
    }

    @Override
    public void handleSetCarriedItem(ClientboundSetCarriedItemPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        if (Inventory.isHotbarSlot(param0.getSlot())) {
            this.minecraft.player.getInventory().selected = param0.getSlot();
        }

    }

    @Override
    public void handleMoveEntity(ClientboundMoveEntityPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Entity var0 = param0.getEntity(this.level);
        if (var0 != null) {
            if (!var0.isControlledByLocalInstance()) {
                if (param0.hasPosition()) {
                    VecDeltaCodec var1 = var0.getPositionCodec();
                    Vec3 var2 = var1.decode((long)param0.getXa(), (long)param0.getYa(), (long)param0.getZa());
                    var1.setBase(var2);
                    float var3 = param0.hasRotation() ? (float)(param0.getyRot() * 360) / 256.0F : var0.getYRot();
                    float var4 = param0.hasRotation() ? (float)(param0.getxRot() * 360) / 256.0F : var0.getXRot();
                    var0.lerpTo(var2.x(), var2.y(), var2.z(), var3, var4, 3, false);
                } else if (param0.hasRotation()) {
                    float var5 = (float)(param0.getyRot() * 360) / 256.0F;
                    float var6 = (float)(param0.getxRot() * 360) / 256.0F;
                    var0.lerpTo(var0.getX(), var0.getY(), var0.getZ(), var5, var6, 3, false);
                }

                var0.setOnGround(param0.isOnGround());
            }

        }
    }

    @Override
    public void handleRotateMob(ClientboundRotateHeadPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Entity var0 = param0.getEntity(this.level);
        if (var0 != null) {
            float var1 = (float)(param0.getYHeadRot() * 360) / 256.0F;
            var0.lerpHeadTo(var1, 3);
        }
    }

    @Override
    public void handleRemoveEntities(ClientboundRemoveEntitiesPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        param0.getEntityIds().forEach(param0x -> this.level.removeEntity(param0x, Entity.RemovalReason.DISCARDED));
    }

    @Override
    public void handleMovePlayer(ClientboundPlayerPositionPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Player var0 = this.minecraft.player;
        Vec3 var1 = var0.getDeltaMovement();
        boolean var2 = param0.getRelativeArguments().contains(RelativeMovement.X);
        boolean var3 = param0.getRelativeArguments().contains(RelativeMovement.Y);
        boolean var4 = param0.getRelativeArguments().contains(RelativeMovement.Z);
        double var5;
        double var6;
        if (var2) {
            var5 = var1.x();
            var6 = var0.getX() + param0.getX();
            var0.xOld += param0.getX();
            var0.xo += param0.getX();
        } else {
            var5 = 0.0;
            var6 = param0.getX();
            var0.xOld = var6;
            var0.xo = var6;
        }

        double var9;
        double var10;
        if (var3) {
            var9 = var1.y();
            var10 = var0.getY() + param0.getY();
            var0.yOld += param0.getY();
            var0.yo += param0.getY();
        } else {
            var9 = 0.0;
            var10 = param0.getY();
            var0.yOld = var10;
            var0.yo = var10;
        }

        double var13;
        double var14;
        if (var4) {
            var13 = var1.z();
            var14 = var0.getZ() + param0.getZ();
            var0.zOld += param0.getZ();
            var0.zo += param0.getZ();
        } else {
            var13 = 0.0;
            var14 = param0.getZ();
            var0.zOld = var14;
            var0.zo = var14;
        }

        var0.setPos(var6, var10, var14);
        var0.setDeltaMovement(var5, var9, var13);
        float var17 = param0.getYRot();
        float var18 = param0.getXRot();
        if (param0.getRelativeArguments().contains(RelativeMovement.X_ROT)) {
            var0.setXRot(var0.getXRot() + var18);
            var0.xRotO += var18;
        } else {
            var0.setXRot(var18);
            var0.xRotO = var18;
        }

        if (param0.getRelativeArguments().contains(RelativeMovement.Y_ROT)) {
            var0.setYRot(var0.getYRot() + var17);
            var0.yRotO += var17;
        } else {
            var0.setYRot(var17);
            var0.yRotO = var17;
        }

        this.connection.send(new ServerboundAcceptTeleportationPacket(param0.getId()));
        this.connection.send(new ServerboundMovePlayerPacket.PosRot(var0.getX(), var0.getY(), var0.getZ(), var0.getYRot(), var0.getXRot(), false));
    }

    @Override
    public void handleChunkBlocksUpdate(ClientboundSectionBlocksUpdatePacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        param0.runUpdates((param0x, param1) -> this.level.setServerVerifiedBlockState(param0x, param1, 19));
    }

    @Override
    public void handleLevelChunkWithLight(ClientboundLevelChunkWithLightPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        int var0 = param0.getX();
        int var1 = param0.getZ();
        this.updateLevelChunk(var0, var1, param0.getChunkData());
        ClientboundLightUpdatePacketData var2 = param0.getLightData();
        this.level.queueLightUpdate(() -> {
            this.applyLightData(var0, var1, var2);
            LevelChunk var0x = this.level.getChunkSource().getChunk(var0, var1, false);
            if (var0x != null) {
                this.enableChunkLight(var0x, var0, var1);
            }

        });
    }

    @Override
    public void handleChunksBiomes(ClientboundChunksBiomesPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);

        for(ClientboundChunksBiomesPacket.ChunkBiomeData var0 : param0.chunkBiomeData()) {
            this.level.getChunkSource().replaceBiomes(var0.pos().x, var0.pos().z, var0.getReadBuffer());
        }

        for(ClientboundChunksBiomesPacket.ChunkBiomeData var1 : param0.chunkBiomeData()) {
            this.level.onChunkLoaded(new ChunkPos(var1.pos().x, var1.pos().z));
        }

        for(ClientboundChunksBiomesPacket.ChunkBiomeData var2 : param0.chunkBiomeData()) {
            for(int var3 = -1; var3 <= 1; ++var3) {
                for(int var4 = -1; var4 <= 1; ++var4) {
                    for(int var5 = this.level.getMinSection(); var5 < this.level.getMaxSection(); ++var5) {
                        this.minecraft.levelRenderer.setSectionDirty(var2.pos().x + var3, var5, var2.pos().z + var4);
                    }
                }
            }
        }

    }

    private void updateLevelChunk(int param0, int param1, ClientboundLevelChunkPacketData param2) {
        this.level
            .getChunkSource()
            .replaceWithPacketData(param0, param1, param2.getReadBuffer(), param2.getHeightmaps(), param2.getBlockEntitiesTagsConsumer(param0, param1));
    }

    private void enableChunkLight(LevelChunk param0, int param1, int param2) {
        LevelLightEngine var0 = this.level.getChunkSource().getLightEngine();
        LevelChunkSection[] var1 = param0.getSections();
        ChunkPos var2 = param0.getPos();

        for(int var3 = 0; var3 < var1.length; ++var3) {
            LevelChunkSection var4 = var1[var3];
            int var5 = this.level.getSectionYFromSectionIndex(var3);
            var0.updateSectionStatus(SectionPos.of(var2, var5), var4.hasOnlyAir());
            this.level.setSectionDirtyWithNeighbors(param1, var5, param2);
        }

    }

    @Override
    public void handleForgetLevelChunk(ClientboundForgetLevelChunkPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        int var0 = param0.getX();
        int var1 = param0.getZ();
        ClientChunkCache var2 = this.level.getChunkSource();
        var2.drop(var0, var1);
        this.queueLightRemoval(param0);
    }

    private void queueLightRemoval(ClientboundForgetLevelChunkPacket param0) {
        ChunkPos var0 = new ChunkPos(param0.getX(), param0.getZ());
        this.level.queueLightUpdate(() -> {
            LevelLightEngine var0x = this.level.getLightEngine();
            var0x.setLightEnabled(var0, false);

            for(int var1 = var0x.getMinLightSection(); var1 < var0x.getMaxLightSection(); ++var1) {
                SectionPos var2x = SectionPos.of(var0, var1);
                var0x.queueSectionData(LightLayer.BLOCK, var2x, null);
                var0x.queueSectionData(LightLayer.SKY, var2x, null);
            }

            for(int var3 = this.level.getMinSection(); var3 < this.level.getMaxSection(); ++var3) {
                var0x.updateSectionStatus(SectionPos.of(var0, var3), true);
            }

        });
    }

    @Override
    public void handleBlockUpdate(ClientboundBlockUpdatePacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.level.setServerVerifiedBlockState(param0.getPos(), param0.getBlockState(), 19);
    }

    @Override
    public void handleDisconnect(ClientboundDisconnectPacket param0) {
        this.connection.disconnect(param0.getReason());
    }

    @Override
    public void onDisconnect(Component param0) {
        this.minecraft.clearLevel();
        this.telemetryManager.onDisconnect();
        if (this.callbackScreen != null) {
            if (this.callbackScreen instanceof RealmsScreen) {
                this.minecraft.setScreen(new DisconnectedRealmsScreen(this.callbackScreen, GENERIC_DISCONNECT_MESSAGE, param0));
            } else {
                this.minecraft.setScreen(new DisconnectedScreen(this.callbackScreen, GENERIC_DISCONNECT_MESSAGE, param0));
            }
        } else {
            this.minecraft.setScreen(new DisconnectedScreen(new JoinMultiplayerScreen(new TitleScreen()), GENERIC_DISCONNECT_MESSAGE, param0));
        }

    }

    public void send(Packet<?> param0) {
        this.connection.send(param0);
    }

    @Override
    public void handleTakeItemEntity(ClientboundTakeItemEntityPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Entity var0 = this.level.getEntity(param0.getItemId());
        LivingEntity var1 = (LivingEntity)this.level.getEntity(param0.getPlayerId());
        if (var1 == null) {
            var1 = this.minecraft.player;
        }

        if (var0 != null) {
            if (var0 instanceof ExperienceOrb) {
                this.level
                    .playLocalSound(
                        var0.getX(),
                        var0.getY(),
                        var0.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP,
                        SoundSource.PLAYERS,
                        0.1F,
                        (this.random.nextFloat() - this.random.nextFloat()) * 0.35F + 0.9F,
                        false
                    );
            } else {
                this.level
                    .playLocalSound(
                        var0.getX(),
                        var0.getY(),
                        var0.getZ(),
                        SoundEvents.ITEM_PICKUP,
                        SoundSource.PLAYERS,
                        0.2F,
                        (this.random.nextFloat() - this.random.nextFloat()) * 1.4F + 2.0F,
                        false
                    );
            }

            this.minecraft
                .particleEngine
                .add(new ItemPickupParticle(this.minecraft.getEntityRenderDispatcher(), this.minecraft.renderBuffers(), this.level, var0, var1));
            if (var0 instanceof ItemEntity var2) {
                ItemStack var3 = var2.getItem();
                if (!var3.isEmpty()) {
                    var3.shrink(param0.getAmount());
                }

                if (var3.isEmpty()) {
                    this.level.removeEntity(param0.getItemId(), Entity.RemovalReason.DISCARDED);
                }
            } else if (!(var0 instanceof ExperienceOrb)) {
                this.level.removeEntity(param0.getItemId(), Entity.RemovalReason.DISCARDED);
            }
        }

    }

    @Override
    public void handleSystemChat(ClientboundSystemChatPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.minecraft.getChatListener().handleSystemMessage(param0.content(), param0.overlay());
    }

    @Override
    public void handlePlayerChat(ClientboundPlayerChatPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Optional<SignedMessageBody> var0 = param0.body().unpack(this.messageSignatureCache);
        Optional<ChatType.Bound> var1 = param0.chatType().resolve(this.registryAccess.compositeAccess());
        if (!var0.isEmpty() && !var1.isEmpty()) {
            UUID var2 = param0.sender();
            PlayerInfo var3 = this.getPlayerInfo(var2);
            if (var3 == null) {
                this.connection.disconnect(CHAT_VALIDATION_FAILED_ERROR);
            } else {
                RemoteChatSession var4 = var3.getChatSession();
                SignedMessageLink var5;
                if (var4 != null) {
                    var5 = new SignedMessageLink(param0.index(), var2, var4.sessionId());
                } else {
                    var5 = SignedMessageLink.unsigned(var2);
                }

                PlayerChatMessage var7 = new PlayerChatMessage(var5, param0.signature(), var0.get(), param0.unsignedContent(), param0.filterMask());
                if (!var3.getMessageValidator().updateAndValidate(var7)) {
                    this.connection.disconnect(CHAT_VALIDATION_FAILED_ERROR);
                } else {
                    this.minecraft.getChatListener().handlePlayerChatMessage(var7, var3.getProfile(), var1.get());
                    this.messageSignatureCache.push(var7);
                }
            }
        } else {
            this.connection.disconnect(INVALID_PACKET);
        }
    }

    @Override
    public void handleDisguisedChat(ClientboundDisguisedChatPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Optional<ChatType.Bound> var0 = param0.chatType().resolve(this.registryAccess.compositeAccess());
        if (var0.isEmpty()) {
            this.connection.disconnect(INVALID_PACKET);
        } else {
            this.minecraft.getChatListener().handleDisguisedChatMessage(param0.message(), var0.get());
        }
    }

    @Override
    public void handleDeleteChat(ClientboundDeleteChatPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Optional<MessageSignature> var0 = param0.messageSignature().unpack(this.messageSignatureCache);
        if (var0.isEmpty()) {
            this.connection.disconnect(INVALID_PACKET);
        } else {
            this.lastSeenMessages.ignorePending(var0.get());
            if (!this.minecraft.getChatListener().removeFromDelayedMessageQueue(var0.get())) {
                this.minecraft.gui.getChat().deleteMessage(var0.get());
            }

        }
    }

    @Override
    public void handleAnimate(ClientboundAnimatePacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Entity var0 = this.level.getEntity(param0.getId());
        if (var0 != null) {
            if (param0.getAction() == 0) {
                LivingEntity var1 = (LivingEntity)var0;
                var1.swing(InteractionHand.MAIN_HAND);
            } else if (param0.getAction() == 3) {
                LivingEntity var2 = (LivingEntity)var0;
                var2.swing(InteractionHand.OFF_HAND);
            } else if (param0.getAction() == 2) {
                Player var3 = (Player)var0;
                var3.stopSleepInBed(false, false);
            } else if (param0.getAction() == 4) {
                this.minecraft.particleEngine.createTrackingEmitter(var0, ParticleTypes.CRIT);
            } else if (param0.getAction() == 5) {
                this.minecraft.particleEngine.createTrackingEmitter(var0, ParticleTypes.ENCHANTED_HIT);
            }

        }
    }

    @Override
    public void handleHurtAnimation(ClientboundHurtAnimationPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Entity var0 = this.level.getEntity(param0.id());
        if (var0 != null) {
            var0.animateHurt(param0.yaw());
        }
    }

    @Override
    public void handleSetTime(ClientboundSetTimePacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.minecraft.level.setGameTime(param0.getGameTime());
        this.minecraft.level.setDayTime(param0.getDayTime());
        this.telemetryManager.setTime(param0.getGameTime());
    }

    @Override
    public void handleSetSpawn(ClientboundSetDefaultSpawnPositionPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.minecraft.level.setDefaultSpawnPos(param0.getPos(), param0.getAngle());
        Screen var3 = this.minecraft.screen;
        if (var3 instanceof ReceivingLevelScreen var0) {
            var0.loadingPacketsReceived();
        }

    }

    @Override
    public void handleSetEntityPassengersPacket(ClientboundSetPassengersPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Entity var0 = this.level.getEntity(param0.getVehicle());
        if (var0 == null) {
            LOGGER.warn("Received passengers for unknown entity");
        } else {
            boolean var1 = var0.hasIndirectPassenger(this.minecraft.player);
            var0.ejectPassengers();

            for(int var2 : param0.getPassengers()) {
                Entity var3 = this.level.getEntity(var2);
                if (var3 != null) {
                    var3.startRiding(var0, true);
                    if (var3 == this.minecraft.player && !var1) {
                        if (var0 instanceof Boat) {
                            this.minecraft.player.yRotO = var0.getYRot();
                            this.minecraft.player.setYRot(var0.getYRot());
                            this.minecraft.player.setYHeadRot(var0.getYRot());
                        }

                        Component var4 = Component.translatable("mount.onboard", this.minecraft.options.keyShift.getTranslatedKeyMessage());
                        this.minecraft.gui.setOverlayMessage(var4, false);
                        this.minecraft.getNarrator().sayNow(var4);
                    }
                }
            }

        }
    }

    @Override
    public void handleEntityLinkPacket(ClientboundSetEntityLinkPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Entity var0 = this.level.getEntity(param0.getSourceId());
        if (var0 instanceof Mob) {
            ((Mob)var0).setDelayedLeashHolderId(param0.getDestId());
        }

    }

    private static ItemStack findTotem(Player param0) {
        for(InteractionHand var0 : InteractionHand.values()) {
            ItemStack var1 = param0.getItemInHand(var0);
            if (var1.is(Items.TOTEM_OF_UNDYING)) {
                return var1;
            }
        }

        return new ItemStack(Items.TOTEM_OF_UNDYING);
    }

    @Override
    public void handleEntityEvent(ClientboundEntityEventPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Entity var0 = param0.getEntity(this.level);
        if (var0 != null) {
            switch(param0.getEventId()) {
                case 21:
                    this.minecraft.getSoundManager().play(new GuardianAttackSoundInstance((Guardian)var0));
                    break;
                case 35:
                    int var1 = 40;
                    this.minecraft.particleEngine.createTrackingEmitter(var0, ParticleTypes.TOTEM_OF_UNDYING, 30);
                    this.level.playLocalSound(var0.getX(), var0.getY(), var0.getZ(), SoundEvents.TOTEM_USE, var0.getSoundSource(), 1.0F, 1.0F, false);
                    if (var0 == this.minecraft.player) {
                        this.minecraft.gameRenderer.displayItemActivation(findTotem(this.minecraft.player));
                    }
                    break;
                case 63:
                    this.minecraft.getSoundManager().play(new SnifferSoundInstance((Sniffer)var0));
                    break;
                default:
                    var0.handleEntityEvent(param0.getEventId());
            }
        }

    }

    @Override
    public void handleDamageEvent(ClientboundDamageEventPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Entity var0 = this.level.getEntity(param0.entityId());
        if (var0 != null) {
            var0.handleDamageEvent(param0.getSource(this.level));
        }
    }

    @Override
    public void handleSetHealth(ClientboundSetHealthPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.minecraft.player.hurtTo(param0.getHealth());
        this.minecraft.player.getFoodData().setFoodLevel(param0.getFood());
        this.minecraft.player.getFoodData().setSaturation(param0.getSaturation());
    }

    @Override
    public void handleSetExperience(ClientboundSetExperiencePacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.minecraft.player.setExperienceValues(param0.getExperienceProgress(), param0.getTotalExperience(), param0.getExperienceLevel());
    }

    @Override
    public void handleRespawn(ClientboundRespawnPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        ResourceKey<Level> var0 = param0.getDimension();
        Holder<DimensionType> var1 = this.registryAccess
            .compositeAccess()
            .<DimensionType>registryOrThrow(Registries.DIMENSION_TYPE)
            .getHolderOrThrow(param0.getDimensionType());
        LocalPlayer var2 = this.minecraft.player;
        int var3 = var2.getId();
        if (var0 != var2.level().dimension()) {
            Scoreboard var4 = this.level.getScoreboard();
            Map<String, MapItemSavedData> var5 = this.level.getAllMapData();
            boolean var6 = param0.isDebug();
            boolean var7 = param0.isFlat();
            ClientLevel.ClientLevelData var8 = new ClientLevel.ClientLevelData(this.levelData.getDifficulty(), this.levelData.isHardcore(), var7);
            this.levelData = var8;
            this.level = new ClientLevel(
                this,
                var8,
                var0,
                var1,
                this.serverChunkRadius,
                this.serverSimulationDistance,
                this.minecraft::getProfiler,
                this.minecraft.levelRenderer,
                var6,
                param0.getSeed()
            );
            this.level.setScoreboard(var4);
            this.level.addMapData(var5);
            this.minecraft.setLevel(this.level);
            this.minecraft.setScreen(new ReceivingLevelScreen());
        }

        String var9 = var2.getServerBrand();
        this.minecraft.cameraEntity = null;
        if (var2.hasContainerOpen()) {
            var2.closeContainer();
        }

        LocalPlayer var10;
        if (param0.shouldKeep((byte)2)) {
            var10 = this.minecraft.gameMode.createPlayer(this.level, var2.getStats(), var2.getRecipeBook(), var2.isShiftKeyDown(), var2.isSprinting());
        } else {
            var10 = this.minecraft.gameMode.createPlayer(this.level, var2.getStats(), var2.getRecipeBook());
        }

        var10.setId(var3);
        this.minecraft.player = var10;
        if (var0 != var2.level().dimension()) {
            this.minecraft.getMusicManager().stopPlaying();
        }

        this.minecraft.cameraEntity = var10;
        if (param0.shouldKeep((byte)2)) {
            List<SynchedEntityData.DataValue<?>> var12 = var2.getEntityData().getNonDefaultValues();
            if (var12 != null) {
                var10.getEntityData().assignValues(var12);
            }
        }

        if (param0.shouldKeep((byte)1)) {
            var10.getAttributes().assignValues(var2.getAttributes());
        }

        var10.resetPos();
        var10.setServerBrand(var9);
        this.level.addPlayer(var3, var10);
        var10.setYRot(-180.0F);
        var10.input = new KeyboardInput(this.minecraft.options);
        this.minecraft.gameMode.adjustPlayer(var10);
        var10.setReducedDebugInfo(var2.isReducedDebugInfo());
        var10.setShowDeathScreen(var2.shouldShowDeathScreen());
        var10.setLastDeathLocation(param0.getLastDeathLocation());
        var10.setPortalCooldown(param0.getPortalCooldown());
        var10.spinningEffectIntensity = var2.spinningEffectIntensity;
        var10.oSpinningEffectIntensity = var2.oSpinningEffectIntensity;
        if (this.minecraft.screen instanceof DeathScreen || this.minecraft.screen instanceof DeathScreen.TitleConfirmScreen) {
            this.minecraft.setScreen(null);
        }

        this.minecraft.gameMode.setLocalMode(param0.getPlayerGameType(), param0.getPreviousPlayerGameType());
    }

    @Override
    public void handleExplosion(ClientboundExplodePacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Explosion var0 = new Explosion(this.minecraft.level, null, param0.getX(), param0.getY(), param0.getZ(), param0.getPower(), param0.getToBlow());
        var0.finalizeExplosion(true);
        this.minecraft
            .player
            .setDeltaMovement(
                this.minecraft.player.getDeltaMovement().add((double)param0.getKnockbackX(), (double)param0.getKnockbackY(), (double)param0.getKnockbackZ())
            );
    }

    @Override
    public void handleHorseScreenOpen(ClientboundHorseScreenOpenPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Entity var0 = this.level.getEntity(param0.getEntityId());
        if (var0 instanceof AbstractHorse) {
            LocalPlayer var1 = this.minecraft.player;
            AbstractHorse var2 = (AbstractHorse)var0;
            SimpleContainer var3 = new SimpleContainer(param0.getSize());
            HorseInventoryMenu var4 = new HorseInventoryMenu(param0.getContainerId(), var1.getInventory(), var3, var2);
            var1.containerMenu = var4;
            this.minecraft.setScreen(new HorseInventoryScreen(var4, var1.getInventory(), var2));
        }

    }

    @Override
    public void handleOpenScreen(ClientboundOpenScreenPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        MenuScreens.create(param0.getType(), this.minecraft, param0.getContainerId(), param0.getTitle());
    }

    @Override
    public void handleContainerSetSlot(ClientboundContainerSetSlotPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Player var0 = this.minecraft.player;
        ItemStack var1 = param0.getItem();
        int var2 = param0.getSlot();
        this.minecraft.getTutorial().onGetItem(var1);
        if (param0.getContainerId() == -1) {
            if (!(this.minecraft.screen instanceof CreativeModeInventoryScreen)) {
                var0.containerMenu.setCarried(var1);
            }
        } else if (param0.getContainerId() == -2) {
            var0.getInventory().setItem(var2, var1);
        } else {
            boolean var3 = false;
            Screen var7 = this.minecraft.screen;
            if (var7 instanceof CreativeModeInventoryScreen var4) {
                var3 = !var4.isInventoryOpen();
            }

            if (param0.getContainerId() == 0 && InventoryMenu.isHotbarSlot(var2)) {
                if (!var1.isEmpty()) {
                    ItemStack var5 = var0.inventoryMenu.getSlot(var2).getItem();
                    if (var5.isEmpty() || var5.getCount() < var1.getCount()) {
                        var1.setPopTime(5);
                    }
                }

                var0.inventoryMenu.setItem(var2, param0.getStateId(), var1);
            } else if (param0.getContainerId() == var0.containerMenu.containerId && (param0.getContainerId() != 0 || !var3)) {
                var0.containerMenu.setItem(var2, param0.getStateId(), var1);
            }
        }

    }

    @Override
    public void handleContainerContent(ClientboundContainerSetContentPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Player var0 = this.minecraft.player;
        if (param0.getContainerId() == 0) {
            var0.inventoryMenu.initializeContents(param0.getStateId(), param0.getItems(), param0.getCarriedItem());
        } else if (param0.getContainerId() == var0.containerMenu.containerId) {
            var0.containerMenu.initializeContents(param0.getStateId(), param0.getItems(), param0.getCarriedItem());
        }

    }

    @Override
    public void handleOpenSignEditor(ClientboundOpenSignEditorPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        BlockPos var0 = param0.getPos();
        BlockEntity var2 = this.level.getBlockEntity(var0);
        if (var2 instanceof SignBlockEntity var1) {
            this.minecraft.player.openTextEdit(var1, param0.isFrontText());
        } else {
            BlockState var2 = this.level.getBlockState(var0);
            SignBlockEntity var3 = new SignBlockEntity(var0, var2);
            var3.setLevel(this.level);
            this.minecraft.player.openTextEdit(var3, param0.isFrontText());
        }

    }

    @Override
    public void handleBlockEntityData(ClientboundBlockEntityDataPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        BlockPos var0 = param0.getPos();
        this.minecraft.level.getBlockEntity(var0, param0.getType()).ifPresent(param1 -> {
            CompoundTag var0x = param0.getTag();
            if (var0x != null) {
                param1.load(var0x);
            }

            if (param1 instanceof CommandBlockEntity && this.minecraft.screen instanceof CommandBlockEditScreen) {
                ((CommandBlockEditScreen)this.minecraft.screen).updateGui();
            }

        });
    }

    @Override
    public void handleContainerSetData(ClientboundContainerSetDataPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Player var0 = this.minecraft.player;
        if (var0.containerMenu != null && var0.containerMenu.containerId == param0.getContainerId()) {
            var0.containerMenu.setData(param0.getId(), param0.getValue());
        }

    }

    @Override
    public void handleSetEquipment(ClientboundSetEquipmentPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Entity var0 = this.level.getEntity(param0.getEntity());
        if (var0 != null) {
            param0.getSlots().forEach(param1 -> var0.setItemSlot(param1.getFirst(), param1.getSecond()));
        }

    }

    @Override
    public void handleContainerClose(ClientboundContainerClosePacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.minecraft.player.clientSideCloseContainer();
    }

    @Override
    public void handleBlockEvent(ClientboundBlockEventPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.minecraft.level.blockEvent(param0.getPos(), param0.getBlock(), param0.getB0(), param0.getB1());
    }

    @Override
    public void handleBlockDestruction(ClientboundBlockDestructionPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.minecraft.level.destroyBlockProgress(param0.getId(), param0.getPos(), param0.getProgress());
    }

    @Override
    public void handleGameEvent(ClientboundGameEventPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Player var0 = this.minecraft.player;
        ClientboundGameEventPacket.Type var1 = param0.getEvent();
        float var2 = param0.getParam();
        int var3 = Mth.floor(var2 + 0.5F);
        if (var1 == ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE) {
            var0.displayClientMessage(Component.translatable("block.minecraft.spawn.not_valid"), false);
        } else if (var1 == ClientboundGameEventPacket.START_RAINING) {
            this.level.getLevelData().setRaining(true);
            this.level.setRainLevel(0.0F);
        } else if (var1 == ClientboundGameEventPacket.STOP_RAINING) {
            this.level.getLevelData().setRaining(false);
            this.level.setRainLevel(1.0F);
        } else if (var1 == ClientboundGameEventPacket.CHANGE_GAME_MODE) {
            this.minecraft.gameMode.setLocalMode(GameType.byId(var3));
        } else if (var1 == ClientboundGameEventPacket.WIN_GAME) {
            if (var3 == 0) {
                this.minecraft.player.connection.send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN));
                this.minecraft.setScreen(new ReceivingLevelScreen());
            } else if (var3 == 1) {
                this.minecraft.setScreen(new WinScreen(true, () -> {
                    this.minecraft.player.connection.send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN));
                    this.minecraft.setScreen(null);
                }));
            }
        } else if (var1 == ClientboundGameEventPacket.DEMO_EVENT) {
            Options var4 = this.minecraft.options;
            if (var2 == 0.0F) {
                this.minecraft.setScreen(new DemoIntroScreen());
            } else if (var2 == 101.0F) {
                this.minecraft
                    .gui
                    .getChat()
                    .addMessage(
                        Component.translatable(
                            "demo.help.movement",
                            var4.keyUp.getTranslatedKeyMessage(),
                            var4.keyLeft.getTranslatedKeyMessage(),
                            var4.keyDown.getTranslatedKeyMessage(),
                            var4.keyRight.getTranslatedKeyMessage()
                        )
                    );
            } else if (var2 == 102.0F) {
                this.minecraft.gui.getChat().addMessage(Component.translatable("demo.help.jump", var4.keyJump.getTranslatedKeyMessage()));
            } else if (var2 == 103.0F) {
                this.minecraft.gui.getChat().addMessage(Component.translatable("demo.help.inventory", var4.keyInventory.getTranslatedKeyMessage()));
            } else if (var2 == 104.0F) {
                this.minecraft.gui.getChat().addMessage(Component.translatable("demo.day.6", var4.keyScreenshot.getTranslatedKeyMessage()));
            }
        } else if (var1 == ClientboundGameEventPacket.ARROW_HIT_PLAYER) {
            this.level.playSound(var0, var0.getX(), var0.getEyeY(), var0.getZ(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 0.18F, 0.45F);
        } else if (var1 == ClientboundGameEventPacket.RAIN_LEVEL_CHANGE) {
            this.level.setRainLevel(var2);
        } else if (var1 == ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE) {
            this.level.setThunderLevel(var2);
        } else if (var1 == ClientboundGameEventPacket.PUFFER_FISH_STING) {
            this.level.playSound(var0, var0.getX(), var0.getY(), var0.getZ(), SoundEvents.PUFFER_FISH_STING, SoundSource.NEUTRAL, 1.0F, 1.0F);
        } else if (var1 == ClientboundGameEventPacket.GUARDIAN_ELDER_EFFECT) {
            this.level.addParticle(ParticleTypes.ELDER_GUARDIAN, var0.getX(), var0.getY(), var0.getZ(), 0.0, 0.0, 0.0);
            if (var3 == 1) {
                this.level.playSound(var0, var0.getX(), var0.getY(), var0.getZ(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.HOSTILE, 1.0F, 1.0F);
            }
        } else if (var1 == ClientboundGameEventPacket.IMMEDIATE_RESPAWN) {
            this.minecraft.player.setShowDeathScreen(var2 == 0.0F);
        }

    }

    @Override
    public void handleMapItemData(ClientboundMapItemDataPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        MapRenderer var0 = this.minecraft.gameRenderer.getMapRenderer();
        int var1 = param0.getMapId();
        String var2 = MapItem.makeKey(var1);
        MapItemSavedData var3 = this.minecraft.level.getMapData(var2);
        if (var3 == null) {
            var3 = MapItemSavedData.createForClient(param0.getScale(), param0.isLocked(), this.minecraft.level.dimension());
            this.minecraft.level.overrideMapData(var2, var3);
        }

        param0.applyToMap(var3);
        var0.update(var1, var3);
    }

    @Override
    public void handleLevelEvent(ClientboundLevelEventPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        if (param0.isGlobalEvent()) {
            this.minecraft.level.globalLevelEvent(param0.getType(), param0.getPos(), param0.getData());
        } else {
            this.minecraft.level.levelEvent(param0.getType(), param0.getPos(), param0.getData());
        }

    }

    @Override
    public void handleUpdateAdvancementsPacket(ClientboundUpdateAdvancementsPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.advancements.update(param0);
    }

    @Override
    public void handleSelectAdvancementsTab(ClientboundSelectAdvancementsTabPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        ResourceLocation var0 = param0.getTab();
        if (var0 == null) {
            this.advancements.setSelectedTab(null, false);
        } else {
            Advancement var1 = this.advancements.getAdvancements().get(var0);
            this.advancements.setSelectedTab(var1, false);
        }

    }

    @Override
    public void handleCommands(ClientboundCommandsPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.commands = new CommandDispatcher<>(param0.getRoot(CommandBuildContext.simple(this.registryAccess.compositeAccess(), this.enabledFeatures)));
    }

    @Override
    public void handleStopSoundEvent(ClientboundStopSoundPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.minecraft.getSoundManager().stop(param0.getName(), param0.getSource());
    }

    @Override
    public void handleCommandSuggestions(ClientboundCommandSuggestionsPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.suggestionsProvider.completeCustomSuggestions(param0.getId(), param0.getSuggestions());
    }

    @Override
    public void handleUpdateRecipes(ClientboundUpdateRecipesPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.recipeManager.replaceRecipes(param0.getRecipes());
        ClientRecipeBook var0 = this.minecraft.player.getRecipeBook();
        var0.setupCollections(this.recipeManager.getRecipes(), this.minecraft.level.registryAccess());
        this.minecraft.populateSearchTree(SearchRegistry.RECIPE_COLLECTIONS, var0.getCollections());
    }

    @Override
    public void handleLookAt(ClientboundPlayerLookAtPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Vec3 var0 = param0.getPosition(this.level);
        if (var0 != null) {
            this.minecraft.player.lookAt(param0.getFromAnchor(), var0);
        }

    }

    @Override
    public void handleTagQueryPacket(ClientboundTagQueryPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        if (!this.debugQueryHandler.handleResponse(param0.getTransactionId(), param0.getTag())) {
            LOGGER.debug("Got unhandled response to tag query {}", param0.getTransactionId());
        }

    }

    @Override
    public void handleAwardStats(ClientboundAwardStatsPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);

        for(Entry<Stat<?>, Integer> var0 : param0.getStats().entrySet()) {
            Stat<?> var1 = var0.getKey();
            int var2 = var0.getValue();
            this.minecraft.player.getStats().setValue(this.minecraft.player, var1, var2);
        }

        if (this.minecraft.screen instanceof StatsUpdateListener) {
            ((StatsUpdateListener)this.minecraft.screen).onStatsUpdated();
        }

    }

    @Override
    public void handleAddOrRemoveRecipes(ClientboundRecipePacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        ClientRecipeBook var0 = this.minecraft.player.getRecipeBook();
        var0.setBookSettings(param0.getBookSettings());
        ClientboundRecipePacket.State var1 = param0.getState();
        switch(var1) {
            case REMOVE:
                for(ResourceLocation var2 : param0.getRecipes()) {
                    this.recipeManager.byKey(var2).ifPresent(var0::remove);
                }
                break;
            case INIT:
                for(ResourceLocation var3 : param0.getRecipes()) {
                    this.recipeManager.byKey(var3).ifPresent(var0::add);
                }

                for(ResourceLocation var4 : param0.getHighlights()) {
                    this.recipeManager.byKey(var4).ifPresent(var0::addHighlight);
                }
                break;
            case ADD:
                for(ResourceLocation var5 : param0.getRecipes()) {
                    this.recipeManager.byKey(var5).ifPresent(param1 -> {
                        var0.add(param1);
                        var0.addHighlight(param1);
                        if (param1.showNotification()) {
                            RecipeToast.addOrUpdate(this.minecraft.getToasts(), param1);
                        }

                    });
                }
        }

        var0.getCollections().forEach(param1 -> param1.updateKnownRecipes(var0));
        if (this.minecraft.screen instanceof RecipeUpdateListener) {
            ((RecipeUpdateListener)this.minecraft.screen).recipesUpdated();
        }

    }

    @Override
    public void handleUpdateMobEffect(ClientboundUpdateMobEffectPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Entity var0 = this.level.getEntity(param0.getEntityId());
        if (var0 instanceof LivingEntity) {
            MobEffect var1 = param0.getEffect();
            if (var1 != null) {
                MobEffectInstance var2 = new MobEffectInstance(
                    var1,
                    param0.getEffectDurationTicks(),
                    param0.getEffectAmplifier(),
                    param0.isEffectAmbient(),
                    param0.isEffectVisible(),
                    param0.effectShowsIcon(),
                    null,
                    Optional.ofNullable(param0.getFactorData())
                );
                ((LivingEntity)var0).forceAddEffect(var2, null);
            }
        }
    }

    @Override
    public void handleUpdateTags(ClientboundUpdateTagsPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        param0.getTags().forEach(this::updateTagsForRegistry);
        if (!this.connection.isMemoryConnection()) {
            Blocks.rebuildCache();
        }

        CreativeModeTabs.searchTab().rebuildSearchTree();
    }

    @Override
    public void handleEnabledFeatures(ClientboundUpdateEnabledFeaturesPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.enabledFeatures = FeatureFlags.REGISTRY.fromNames(param0.features());
    }

    private <T> void updateTagsForRegistry(ResourceKey<? extends Registry<? extends T>> param0x, TagNetworkSerialization.NetworkPayload param1) {
        if (!param1.isEmpty()) {
            Registry<T> var0 = this.registryAccess
                .compositeAccess()
                .<T>registry(param0x)
                .orElseThrow(() -> new IllegalStateException("Unknown registry " + param0x));
            Map<TagKey<T>, List<Holder<T>>> var2 = new HashMap<>();
            TagNetworkSerialization.deserializeTagsFromNetwork(param0x, var0, param1, var2::put);
            var0.bindTags(var2);
        }
    }

    @Override
    public void handlePlayerCombatEnd(ClientboundPlayerCombatEndPacket param0) {
    }

    @Override
    public void handlePlayerCombatEnter(ClientboundPlayerCombatEnterPacket param0) {
    }

    @Override
    public void handlePlayerCombatKill(ClientboundPlayerCombatKillPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Entity var0 = this.level.getEntity(param0.getPlayerId());
        if (var0 == this.minecraft.player) {
            if (this.minecraft.player.shouldShowDeathScreen()) {
                this.minecraft.setScreen(new DeathScreen(param0.getMessage(), this.level.getLevelData().isHardcore()));
            } else {
                this.minecraft.player.respawn();
            }
        }

    }

    @Override
    public void handleChangeDifficulty(ClientboundChangeDifficultyPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.levelData.setDifficulty(param0.getDifficulty());
        this.levelData.setDifficultyLocked(param0.isLocked());
    }

    @Override
    public void handleSetCamera(ClientboundSetCameraPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Entity var0 = param0.getEntity(this.level);
        if (var0 != null) {
            this.minecraft.setCameraEntity(var0);
        }

    }

    @Override
    public void handleInitializeBorder(ClientboundInitializeBorderPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        WorldBorder var0 = this.level.getWorldBorder();
        var0.setCenter(param0.getNewCenterX(), param0.getNewCenterZ());
        long var1 = param0.getLerpTime();
        if (var1 > 0L) {
            var0.lerpSizeBetween(param0.getOldSize(), param0.getNewSize(), var1);
        } else {
            var0.setSize(param0.getNewSize());
        }

        var0.setAbsoluteMaxSize(param0.getNewAbsoluteMaxSize());
        var0.setWarningBlocks(param0.getWarningBlocks());
        var0.setWarningTime(param0.getWarningTime());
    }

    @Override
    public void handleSetBorderCenter(ClientboundSetBorderCenterPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.level.getWorldBorder().setCenter(param0.getNewCenterX(), param0.getNewCenterZ());
    }

    @Override
    public void handleSetBorderLerpSize(ClientboundSetBorderLerpSizePacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.level.getWorldBorder().lerpSizeBetween(param0.getOldSize(), param0.getNewSize(), param0.getLerpTime());
    }

    @Override
    public void handleSetBorderSize(ClientboundSetBorderSizePacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.level.getWorldBorder().setSize(param0.getSize());
    }

    @Override
    public void handleSetBorderWarningDistance(ClientboundSetBorderWarningDistancePacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.level.getWorldBorder().setWarningBlocks(param0.getWarningBlocks());
    }

    @Override
    public void handleSetBorderWarningDelay(ClientboundSetBorderWarningDelayPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.level.getWorldBorder().setWarningTime(param0.getWarningDelay());
    }

    @Override
    public void handleTitlesClear(ClientboundClearTitlesPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.minecraft.gui.clear();
        if (param0.shouldResetTimes()) {
            this.minecraft.gui.resetTitleTimes();
        }

    }

    @Override
    public void handleServerData(ClientboundServerDataPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        if (this.serverData != null) {
            this.serverData.motd = param0.getMotd();
            param0.getIconBytes().ifPresent(this.serverData::setIconBytes);
            this.serverData.setEnforcesSecureChat(param0.enforcesSecureChat());
            ServerList.saveSingleServer(this.serverData);
            if (!param0.enforcesSecureChat()) {
                SystemToast var0 = SystemToast.multiline(
                    this.minecraft, SystemToast.SystemToastIds.UNSECURE_SERVER_WARNING, UNSECURE_SERVER_TOAST_TITLE, UNSERURE_SERVER_TOAST
                );
                this.minecraft.getToasts().addToast(var0);
            }

        }
    }

    @Override
    public void handleCustomChatCompletions(ClientboundCustomChatCompletionsPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.suggestionsProvider.modifyCustomCompletions(param0.action(), param0.entries());
    }

    @Override
    public void setActionBarText(ClientboundSetActionBarTextPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.minecraft.gui.setOverlayMessage(param0.getText(), false);
    }

    @Override
    public void setTitleText(ClientboundSetTitleTextPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.minecraft.gui.setTitle(param0.getText());
    }

    @Override
    public void setSubtitleText(ClientboundSetSubtitleTextPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.minecraft.gui.setSubtitle(param0.getText());
    }

    @Override
    public void setTitlesAnimation(ClientboundSetTitlesAnimationPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.minecraft.gui.setTimes(param0.getFadeIn(), param0.getStay(), param0.getFadeOut());
    }

    @Override
    public void handleTabListCustomisation(ClientboundTabListPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.minecraft.gui.getTabList().setHeader(param0.getHeader().getString().isEmpty() ? null : param0.getHeader());
        this.minecraft.gui.getTabList().setFooter(param0.getFooter().getString().isEmpty() ? null : param0.getFooter());
    }

    @Override
    public void handleRemoveMobEffect(ClientboundRemoveMobEffectPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Entity var0 = param0.getEntity(this.level);
        if (var0 instanceof LivingEntity) {
            ((LivingEntity)var0).removeEffectNoUpdate(param0.getEffect());
        }

    }

    @Override
    public void handlePlayerInfoRemove(ClientboundPlayerInfoRemovePacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);

        for(UUID var0 : param0.profileIds()) {
            this.minecraft.getPlayerSocialManager().removePlayer(var0);
            PlayerInfo var1 = this.playerInfoMap.remove(var0);
            if (var1 != null) {
                this.listedPlayers.remove(var1);
            }
        }

    }

    @Override
    public void handlePlayerInfoUpdate(ClientboundPlayerInfoUpdatePacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);

        for(ClientboundPlayerInfoUpdatePacket.Entry var0 : param0.newEntries()) {
            PlayerInfo var1 = new PlayerInfo(var0.profile(), this.enforcesSecureChat());
            if (this.playerInfoMap.putIfAbsent(var0.profileId(), var1) == null) {
                this.minecraft.getPlayerSocialManager().addPlayer(var1);
            }
        }

        for(ClientboundPlayerInfoUpdatePacket.Entry var2 : param0.entries()) {
            PlayerInfo var3 = this.playerInfoMap.get(var2.profileId());
            if (var3 == null) {
                LOGGER.warn("Ignoring player info update for unknown player {}", var2.profileId());
            } else {
                for(ClientboundPlayerInfoUpdatePacket.Action var4 : param0.actions()) {
                    this.applyPlayerInfoUpdate(var4, var2, var3);
                }
            }
        }

    }

    private void applyPlayerInfoUpdate(ClientboundPlayerInfoUpdatePacket.Action param0, ClientboundPlayerInfoUpdatePacket.Entry param1, PlayerInfo param2) {
        switch(param0) {
            case INITIALIZE_CHAT:
                this.initializeChatSession(param1, param2);
                break;
            case UPDATE_GAME_MODE:
                if (param2.getGameMode() != param1.gameMode() && this.minecraft.player != null && this.minecraft.player.getUUID().equals(param1.profileId())) {
                    this.minecraft.player.onGameModeChanged(param1.gameMode());
                }

                param2.setGameMode(param1.gameMode());
                break;
            case UPDATE_LISTED:
                if (param1.listed()) {
                    this.listedPlayers.add(param2);
                } else {
                    this.listedPlayers.remove(param2);
                }
                break;
            case UPDATE_LATENCY:
                param2.setLatency(param1.latency());
                break;
            case UPDATE_DISPLAY_NAME:
                param2.setTabListDisplayName(param1.displayName());
        }

    }

    private void initializeChatSession(ClientboundPlayerInfoUpdatePacket.Entry param0, PlayerInfo param1) {
        GameProfile var0 = param1.getProfile();
        SignatureValidator var1 = this.minecraft.getProfileKeySignatureValidator();
        if (var1 == null) {
            LOGGER.warn("Ignoring chat session from {} due to missing Services public key", var0.getName());
            param1.clearChatSession(this.enforcesSecureChat());
        } else {
            RemoteChatSession.Data var2 = param0.chatSession();
            if (var2 != null) {
                try {
                    RemoteChatSession var3 = var2.validate(var0, var1, ProfilePublicKey.EXPIRY_GRACE_PERIOD);
                    param1.setChatSession(var3);
                } catch (ProfilePublicKey.ValidationException var7) {
                    LOGGER.error("Failed to validate profile key for player: '{}'", var0.getName(), var7);
                    param1.clearChatSession(this.enforcesSecureChat());
                }
            } else {
                param1.clearChatSession(this.enforcesSecureChat());
            }

        }
    }

    private boolean enforcesSecureChat() {
        return this.serverData != null && this.serverData.enforcesSecureChat();
    }

    @Override
    public void handleKeepAlive(ClientboundKeepAlivePacket param0) {
        this.sendWhen(new ServerboundKeepAlivePacket(param0.getId()), () -> !RenderSystem.isFrozenAtPollEvents(), Duration.ofMinutes(1L));
    }

    private void sendWhen(Packet<ServerGamePacketListener> param0, BooleanSupplier param1, Duration param2) {
        if (param1.getAsBoolean()) {
            this.send(param0);
        } else {
            this.deferredPackets.add(new ClientPacketListener.DeferredPacket(param0, param1, Util.getMillis() + param2.toMillis()));
        }

    }

    private void sendDeferredPackets() {
        Iterator<ClientPacketListener.DeferredPacket> var0 = this.deferredPackets.iterator();

        while(var0.hasNext()) {
            ClientPacketListener.DeferredPacket var1 = var0.next();
            if (var1.sendCondition().getAsBoolean()) {
                this.send(var1.packet);
                var0.remove();
            } else if (var1.expirationTime() <= Util.getMillis()) {
                var0.remove();
            }
        }

    }

    @Override
    public void handlePlayerAbilities(ClientboundPlayerAbilitiesPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Player var0 = this.minecraft.player;
        var0.getAbilities().flying = param0.isFlying();
        var0.getAbilities().instabuild = param0.canInstabuild();
        var0.getAbilities().invulnerable = param0.isInvulnerable();
        var0.getAbilities().mayfly = param0.canFly();
        var0.getAbilities().setFlyingSpeed(param0.getFlyingSpeed());
        var0.getAbilities().setWalkingSpeed(param0.getWalkingSpeed());
    }

    @Override
    public void handleSoundEvent(ClientboundSoundPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.minecraft
            .level
            .playSeededSound(
                this.minecraft.player,
                param0.getX(),
                param0.getY(),
                param0.getZ(),
                param0.getSound(),
                param0.getSource(),
                param0.getVolume(),
                param0.getPitch(),
                param0.getSeed()
            );
    }

    @Override
    public void handleSoundEntityEvent(ClientboundSoundEntityPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Entity var0 = this.level.getEntity(param0.getId());
        if (var0 != null) {
            this.minecraft
                .level
                .playSeededSound(this.minecraft.player, var0, param0.getSound(), param0.getSource(), param0.getVolume(), param0.getPitch(), param0.getSeed());
        }
    }

    @Override
    public void handleResourcePack(ClientboundResourcePackPacket param0) {
        URL var0 = parseResourcePackUrl(param0.getUrl());
        if (var0 == null) {
            this.send(ServerboundResourcePackPacket.Action.FAILED_DOWNLOAD);
        } else {
            String var1 = param0.getHash();
            boolean var2 = param0.isRequired();
            if (this.serverData != null && this.serverData.getResourcePackStatus() == ServerData.ServerPackStatus.ENABLED) {
                this.send(ServerboundResourcePackPacket.Action.ACCEPTED);
                this.downloadCallback(this.minecraft.getDownloadedPackSource().downloadAndSelectResourcePack(var0, var1, true));
            } else if (this.serverData != null
                && this.serverData.getResourcePackStatus() != ServerData.ServerPackStatus.PROMPT
                && (!var2 || this.serverData.getResourcePackStatus() != ServerData.ServerPackStatus.DISABLED)) {
                this.send(ServerboundResourcePackPacket.Action.DECLINED);
                if (var2) {
                    this.connection.disconnect(Component.translatable("multiplayer.requiredTexturePrompt.disconnect"));
                }
            } else {
                this.minecraft
                    .execute(
                        () -> this.minecraft
                                .setScreen(
                                    new ConfirmScreen(
                                        param3x -> {
                                            this.minecraft.setScreen(null);
                                            if (param3x) {
                                                if (this.serverData != null) {
                                                    this.serverData.setResourcePackStatus(ServerData.ServerPackStatus.ENABLED);
                                                }
                    
                                                this.send(ServerboundResourcePackPacket.Action.ACCEPTED);
                                                this.downloadCallback(this.minecraft.getDownloadedPackSource().downloadAndSelectResourcePack(var0, var1, true));
                                            } else {
                                                this.send(ServerboundResourcePackPacket.Action.DECLINED);
                                                if (var2) {
                                                    this.connection.disconnect(Component.translatable("multiplayer.requiredTexturePrompt.disconnect"));
                                                } else if (this.serverData != null) {
                                                    this.serverData.setResourcePackStatus(ServerData.ServerPackStatus.DISABLED);
                                                }
                                            }
                    
                                            if (this.serverData != null) {
                                                ServerList.saveSingleServer(this.serverData);
                                            }
                    
                                        },
                                        var2
                                            ? Component.translatable("multiplayer.requiredTexturePrompt.line1")
                                            : Component.translatable("multiplayer.texturePrompt.line1"),
                                        preparePackPrompt(
                                            var2
                                                ? Component.translatable("multiplayer.requiredTexturePrompt.line2")
                                                    .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)
                                                : Component.translatable("multiplayer.texturePrompt.line2"),
                                            param0.getPrompt()
                                        ),
                                        var2 ? CommonComponents.GUI_PROCEED : CommonComponents.GUI_YES,
                                        (Component)(var2 ? Component.translatable("menu.disconnect") : CommonComponents.GUI_NO)
                                    )
                                )
                    );
            }

        }
    }

    private static Component preparePackPrompt(Component param0, @Nullable Component param1) {
        return (Component)(param1 == null ? param0 : Component.translatable("multiplayer.texturePrompt.serverPrompt", param0, param1));
    }

    @Nullable
    private static URL parseResourcePackUrl(String param0) {
        try {
            URL var0 = new URL(param0);
            String var1 = var0.getProtocol();
            return !"http".equals(var1) && !"https".equals(var1) ? null : var0;
        } catch (MalformedURLException var3) {
            return null;
        }
    }

    private void downloadCallback(CompletableFuture<?> param0) {
        param0.thenRun(() -> this.send(ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED)).exceptionally(param0x -> {
            this.send(ServerboundResourcePackPacket.Action.FAILED_DOWNLOAD);
            return null;
        });
    }

    private void send(ServerboundResourcePackPacket.Action param0) {
        this.connection.send(new ServerboundResourcePackPacket(param0));
    }

    @Override
    public void handleBossUpdate(ClientboundBossEventPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.minecraft.gui.getBossOverlay().update(param0);
    }

    @Override
    public void handleItemCooldown(ClientboundCooldownPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        if (param0.getDuration() == 0) {
            this.minecraft.player.getCooldowns().removeCooldown(param0.getItem());
        } else {
            this.minecraft.player.getCooldowns().addCooldown(param0.getItem(), param0.getDuration());
        }

    }

    @Override
    public void handleMoveVehicle(ClientboundMoveVehiclePacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Entity var0 = this.minecraft.player.getRootVehicle();
        if (var0 != this.minecraft.player && var0.isControlledByLocalInstance()) {
            var0.absMoveTo(param0.getX(), param0.getY(), param0.getZ(), param0.getYRot(), param0.getXRot());
            this.connection.send(new ServerboundMoveVehiclePacket(var0));
        }

    }

    @Override
    public void handleOpenBook(ClientboundOpenBookPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        ItemStack var0 = this.minecraft.player.getItemInHand(param0.getHand());
        if (var0.is(Items.WRITTEN_BOOK)) {
            this.minecraft.setScreen(new BookViewScreen(new BookViewScreen.WrittenBookAccess(var0)));
        }

    }

    @Override
    public void handleCustomPayload(ClientboundCustomPayloadPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        ResourceLocation var0 = param0.getIdentifier();
        FriendlyByteBuf var1 = null;

        try {
            var1 = param0.getData();
            if (ClientboundCustomPayloadPacket.BRAND.equals(var0)) {
                String var2 = var1.readUtf();
                this.minecraft.player.setServerBrand(var2);
                this.telemetryManager.onServerBrandReceived(var2);
            } else if (ClientboundCustomPayloadPacket.DEBUG_PATHFINDING_PACKET.equals(var0)) {
                int var3 = var1.readInt();
                float var4 = var1.readFloat();
                Path var5 = Path.createFromStream(var1);
                this.minecraft.debugRenderer.pathfindingRenderer.addPath(var3, var5, var4);
            } else if (ClientboundCustomPayloadPacket.DEBUG_NEIGHBORSUPDATE_PACKET.equals(var0)) {
                long var6 = var1.readVarLong();
                BlockPos var7 = var1.readBlockPos();
                ((NeighborsUpdateRenderer)this.minecraft.debugRenderer.neighborsUpdateRenderer).addUpdate(var6, var7);
            } else if (ClientboundCustomPayloadPacket.DEBUG_STRUCTURES_PACKET.equals(var0)) {
                DimensionType var8 = this.registryAccess
                    .compositeAccess()
                    .<DimensionType>registryOrThrow(Registries.DIMENSION_TYPE)
                    .get(var1.readResourceLocation());
                BoundingBox var9 = new BoundingBox(var1.readInt(), var1.readInt(), var1.readInt(), var1.readInt(), var1.readInt(), var1.readInt());
                int var10 = var1.readInt();
                List<BoundingBox> var11 = Lists.newArrayList();
                List<Boolean> var12 = Lists.newArrayList();

                for(int var13 = 0; var13 < var10; ++var13) {
                    var11.add(new BoundingBox(var1.readInt(), var1.readInt(), var1.readInt(), var1.readInt(), var1.readInt(), var1.readInt()));
                    var12.add(var1.readBoolean());
                }

                this.minecraft.debugRenderer.structureRenderer.addBoundingBox(var9, var11, var12, var8);
            } else if (ClientboundCustomPayloadPacket.DEBUG_WORLDGENATTEMPT_PACKET.equals(var0)) {
                ((WorldGenAttemptRenderer)this.minecraft.debugRenderer.worldGenAttemptRenderer)
                    .addPos(var1.readBlockPos(), var1.readFloat(), var1.readFloat(), var1.readFloat(), var1.readFloat(), var1.readFloat());
            } else if (ClientboundCustomPayloadPacket.DEBUG_VILLAGE_SECTIONS.equals(var0)) {
                int var14 = var1.readInt();

                for(int var15 = 0; var15 < var14; ++var15) {
                    this.minecraft.debugRenderer.villageSectionsDebugRenderer.setVillageSection(var1.readSectionPos());
                }

                int var16 = var1.readInt();

                for(int var17 = 0; var17 < var16; ++var17) {
                    this.minecraft.debugRenderer.villageSectionsDebugRenderer.setNotVillageSection(var1.readSectionPos());
                }
            } else if (ClientboundCustomPayloadPacket.DEBUG_POI_ADDED_PACKET.equals(var0)) {
                BlockPos var18 = var1.readBlockPos();
                String var19 = var1.readUtf();
                int var20 = var1.readInt();
                BrainDebugRenderer.PoiInfo var21 = new BrainDebugRenderer.PoiInfo(var18, var19, var20);
                this.minecraft.debugRenderer.brainDebugRenderer.addPoi(var21);
            } else if (ClientboundCustomPayloadPacket.DEBUG_POI_REMOVED_PACKET.equals(var0)) {
                BlockPos var22 = var1.readBlockPos();
                this.minecraft.debugRenderer.brainDebugRenderer.removePoi(var22);
            } else if (ClientboundCustomPayloadPacket.DEBUG_POI_TICKET_COUNT_PACKET.equals(var0)) {
                BlockPos var23 = var1.readBlockPos();
                int var24 = var1.readInt();
                this.minecraft.debugRenderer.brainDebugRenderer.setFreeTicketCount(var23, var24);
            } else if (ClientboundCustomPayloadPacket.DEBUG_GOAL_SELECTOR.equals(var0)) {
                BlockPos var25 = var1.readBlockPos();
                int var26 = var1.readInt();
                int var27 = var1.readInt();
                List<GoalSelectorDebugRenderer.DebugGoal> var28 = Lists.newArrayList();

                for(int var29 = 0; var29 < var27; ++var29) {
                    int var30 = var1.readInt();
                    boolean var31 = var1.readBoolean();
                    String var32 = var1.readUtf(255);
                    var28.add(new GoalSelectorDebugRenderer.DebugGoal(var25, var30, var32, var31));
                }

                this.minecraft.debugRenderer.goalSelectorRenderer.addGoalSelector(var26, var28);
            } else if (ClientboundCustomPayloadPacket.DEBUG_RAIDS.equals(var0)) {
                int var33 = var1.readInt();
                Collection<BlockPos> var34 = Lists.newArrayList();

                for(int var35 = 0; var35 < var33; ++var35) {
                    var34.add(var1.readBlockPos());
                }

                this.minecraft.debugRenderer.raidDebugRenderer.setRaidCenters(var34);
            } else if (ClientboundCustomPayloadPacket.DEBUG_BRAIN.equals(var0)) {
                double var36 = var1.readDouble();
                double var37 = var1.readDouble();
                double var38 = var1.readDouble();
                Position var39 = new PositionImpl(var36, var37, var38);
                UUID var40 = var1.readUUID();
                int var41 = var1.readInt();
                String var42 = var1.readUtf();
                String var43 = var1.readUtf();
                int var44 = var1.readInt();
                float var45 = var1.readFloat();
                float var46 = var1.readFloat();
                String var47 = var1.readUtf();
                Path var48 = var1.readNullable(Path::createFromStream);
                boolean var49 = var1.readBoolean();
                int var50 = var1.readInt();
                BrainDebugRenderer.BrainDump var51 = new BrainDebugRenderer.BrainDump(
                    var40, var41, var42, var43, var44, var45, var46, var39, var47, var48, var49, var50
                );
                int var52 = var1.readVarInt();

                for(int var53 = 0; var53 < var52; ++var53) {
                    String var54 = var1.readUtf();
                    var51.activities.add(var54);
                }

                int var55 = var1.readVarInt();

                for(int var56 = 0; var56 < var55; ++var56) {
                    String var57 = var1.readUtf();
                    var51.behaviors.add(var57);
                }

                int var58 = var1.readVarInt();

                for(int var59 = 0; var59 < var58; ++var59) {
                    String var60 = var1.readUtf();
                    var51.memories.add(var60);
                }

                int var61 = var1.readVarInt();

                for(int var62 = 0; var62 < var61; ++var62) {
                    BlockPos var63 = var1.readBlockPos();
                    var51.pois.add(var63);
                }

                int var64 = var1.readVarInt();

                for(int var65 = 0; var65 < var64; ++var65) {
                    BlockPos var66 = var1.readBlockPos();
                    var51.potentialPois.add(var66);
                }

                int var67 = var1.readVarInt();

                for(int var68 = 0; var68 < var67; ++var68) {
                    String var69 = var1.readUtf();
                    var51.gossips.add(var69);
                }

                this.minecraft.debugRenderer.brainDebugRenderer.addOrUpdateBrainDump(var51);
            } else if (ClientboundCustomPayloadPacket.DEBUG_BEE.equals(var0)) {
                double var70 = var1.readDouble();
                double var71 = var1.readDouble();
                double var72 = var1.readDouble();
                Position var73 = new PositionImpl(var70, var71, var72);
                UUID var74 = var1.readUUID();
                int var75 = var1.readInt();
                BlockPos var76 = var1.readNullable(FriendlyByteBuf::readBlockPos);
                BlockPos var77 = var1.readNullable(FriendlyByteBuf::readBlockPos);
                int var78 = var1.readInt();
                Path var79 = var1.readNullable(Path::createFromStream);
                BeeDebugRenderer.BeeInfo var80 = new BeeDebugRenderer.BeeInfo(var74, var75, var73, var79, var76, var77, var78);
                int var81 = var1.readVarInt();

                for(int var82 = 0; var82 < var81; ++var82) {
                    String var83 = var1.readUtf();
                    var80.goals.add(var83);
                }

                int var84 = var1.readVarInt();

                for(int var85 = 0; var85 < var84; ++var85) {
                    BlockPos var86 = var1.readBlockPos();
                    var80.blacklistedHives.add(var86);
                }

                this.minecraft.debugRenderer.beeDebugRenderer.addOrUpdateBeeInfo(var80);
            } else if (ClientboundCustomPayloadPacket.DEBUG_HIVE.equals(var0)) {
                BlockPos var87 = var1.readBlockPos();
                String var88 = var1.readUtf();
                int var89 = var1.readInt();
                int var90 = var1.readInt();
                boolean var91 = var1.readBoolean();
                BeeDebugRenderer.HiveInfo var92 = new BeeDebugRenderer.HiveInfo(var87, var88, var89, var90, var91, this.level.getGameTime());
                this.minecraft.debugRenderer.beeDebugRenderer.addOrUpdateHiveInfo(var92);
            } else if (ClientboundCustomPayloadPacket.DEBUG_GAME_TEST_CLEAR.equals(var0)) {
                this.minecraft.debugRenderer.gameTestDebugRenderer.clear();
            } else if (ClientboundCustomPayloadPacket.DEBUG_GAME_TEST_ADD_MARKER.equals(var0)) {
                BlockPos var93 = var1.readBlockPos();
                int var94 = var1.readInt();
                String var95 = var1.readUtf();
                int var96 = var1.readInt();
                this.minecraft.debugRenderer.gameTestDebugRenderer.addMarker(var93, var94, var95, var96);
            } else if (ClientboundCustomPayloadPacket.DEBUG_GAME_EVENT.equals(var0)) {
                GameEvent var97 = BuiltInRegistries.GAME_EVENT.get(new ResourceLocation(var1.readUtf()));
                Vec3 var98 = new Vec3(var1.readDouble(), var1.readDouble(), var1.readDouble());
                this.minecraft.debugRenderer.gameEventListenerRenderer.trackGameEvent(var97, var98);
            } else if (ClientboundCustomPayloadPacket.DEBUG_GAME_EVENT_LISTENER.equals(var0)) {
                ResourceLocation var99 = var1.readResourceLocation();
                PositionSource var100 = BuiltInRegistries.POSITION_SOURCE_TYPE
                    .getOptional(var99)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown position source type " + var99))
                    .read(var1);
                int var101 = var1.readVarInt();
                this.minecraft.debugRenderer.gameEventListenerRenderer.trackListener(var100, var101);
            } else {
                LOGGER.warn("Unknown custom packed identifier: {}", var0);
            }
        } finally {
            if (var1 != null) {
                var1.release();
            }

        }

    }

    @Override
    public void handleAddObjective(ClientboundSetObjectivePacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Scoreboard var0 = this.level.getScoreboard();
        String var1 = param0.getObjectiveName();
        if (param0.getMethod() == 0) {
            var0.addObjective(var1, ObjectiveCriteria.DUMMY, param0.getDisplayName(), param0.getRenderType());
        } else if (var0.hasObjective(var1)) {
            Objective var2 = var0.getObjective(var1);
            if (param0.getMethod() == 1) {
                var0.removeObjective(var2);
            } else if (param0.getMethod() == 2) {
                var2.setRenderType(param0.getRenderType());
                var2.setDisplayName(param0.getDisplayName());
            }
        }

    }

    @Override
    public void handleSetScore(ClientboundSetScorePacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Scoreboard var0 = this.level.getScoreboard();
        String var1 = param0.getObjectiveName();
        switch(param0.getMethod()) {
            case CHANGE:
                Objective var2 = var0.getOrCreateObjective(var1);
                Score var3 = var0.getOrCreatePlayerScore(param0.getOwner(), var2);
                var3.setScore(param0.getScore());
                break;
            case REMOVE:
                var0.resetPlayerScore(param0.getOwner(), var0.getObjective(var1));
        }

    }

    @Override
    public void handleSetDisplayObjective(ClientboundSetDisplayObjectivePacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Scoreboard var0 = this.level.getScoreboard();
        String var1 = param0.getObjectiveName();
        Objective var2 = var1 == null ? null : var0.getOrCreateObjective(var1);
        var0.setDisplayObjective(param0.getSlot(), var2);
    }

    @Override
    public void handleSetPlayerTeamPacket(ClientboundSetPlayerTeamPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Scoreboard var0 = this.level.getScoreboard();
        ClientboundSetPlayerTeamPacket.Action var1 = param0.getTeamAction();
        PlayerTeam var2;
        if (var1 == ClientboundSetPlayerTeamPacket.Action.ADD) {
            var2 = var0.addPlayerTeam(param0.getName());
        } else {
            var2 = var0.getPlayerTeam(param0.getName());
            if (var2 == null) {
                LOGGER.warn(
                    "Received packet for unknown team {}: team action: {}, player action: {}",
                    param0.getName(),
                    param0.getTeamAction(),
                    param0.getPlayerAction()
                );
                return;
            }
        }

        Optional<ClientboundSetPlayerTeamPacket.Parameters> var4 = param0.getParameters();
        var4.ifPresent(param1 -> {
            var2.setDisplayName(param1.getDisplayName());
            var2.setColor(param1.getColor());
            var2.unpackOptions(param1.getOptions());
            Team.Visibility var0x = Team.Visibility.byName(param1.getNametagVisibility());
            if (var0x != null) {
                var2.setNameTagVisibility(var0x);
            }

            Team.CollisionRule var1x = Team.CollisionRule.byName(param1.getCollisionRule());
            if (var1x != null) {
                var2.setCollisionRule(var1x);
            }

            var2.setPlayerPrefix(param1.getPlayerPrefix());
            var2.setPlayerSuffix(param1.getPlayerSuffix());
        });
        ClientboundSetPlayerTeamPacket.Action var5 = param0.getPlayerAction();
        if (var5 == ClientboundSetPlayerTeamPacket.Action.ADD) {
            for(String var6 : param0.getPlayers()) {
                var0.addPlayerToTeam(var6, var2);
            }
        } else if (var5 == ClientboundSetPlayerTeamPacket.Action.REMOVE) {
            for(String var7 : param0.getPlayers()) {
                var0.removePlayerFromTeam(var7, var2);
            }
        }

        if (var1 == ClientboundSetPlayerTeamPacket.Action.REMOVE) {
            var0.removePlayerTeam(var2);
        }

    }

    @Override
    public void handleParticleEvent(ClientboundLevelParticlesPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        if (param0.getCount() == 0) {
            double var0 = (double)(param0.getMaxSpeed() * param0.getXDist());
            double var1 = (double)(param0.getMaxSpeed() * param0.getYDist());
            double var2 = (double)(param0.getMaxSpeed() * param0.getZDist());

            try {
                this.level.addParticle(param0.getParticle(), param0.isOverrideLimiter(), param0.getX(), param0.getY(), param0.getZ(), var0, var1, var2);
            } catch (Throwable var17) {
                LOGGER.warn("Could not spawn particle effect {}", param0.getParticle());
            }
        } else {
            for(int var4 = 0; var4 < param0.getCount(); ++var4) {
                double var5 = this.random.nextGaussian() * (double)param0.getXDist();
                double var6 = this.random.nextGaussian() * (double)param0.getYDist();
                double var7 = this.random.nextGaussian() * (double)param0.getZDist();
                double var8 = this.random.nextGaussian() * (double)param0.getMaxSpeed();
                double var9 = this.random.nextGaussian() * (double)param0.getMaxSpeed();
                double var10 = this.random.nextGaussian() * (double)param0.getMaxSpeed();

                try {
                    this.level
                        .addParticle(
                            param0.getParticle(),
                            param0.isOverrideLimiter(),
                            param0.getX() + var5,
                            param0.getY() + var6,
                            param0.getZ() + var7,
                            var8,
                            var9,
                            var10
                        );
                } catch (Throwable var16) {
                    LOGGER.warn("Could not spawn particle effect {}", param0.getParticle());
                    return;
                }
            }
        }

    }

    @Override
    public void handlePing(ClientboundPingPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.send(new ServerboundPongPacket(param0.getId()));
    }

    @Override
    public void handleUpdateAttributes(ClientboundUpdateAttributesPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Entity var0 = this.level.getEntity(param0.getEntityId());
        if (var0 != null) {
            if (!(var0 instanceof LivingEntity)) {
                throw new IllegalStateException("Server tried to update attributes of a non-living entity (actually: " + var0 + ")");
            } else {
                AttributeMap var1 = ((LivingEntity)var0).getAttributes();

                for(ClientboundUpdateAttributesPacket.AttributeSnapshot var2 : param0.getValues()) {
                    AttributeInstance var3 = var1.getInstance(var2.getAttribute());
                    if (var3 == null) {
                        LOGGER.warn("Entity {} does not have attribute {}", var0, BuiltInRegistries.ATTRIBUTE.getKey(var2.getAttribute()));
                    } else {
                        var3.setBaseValue(var2.getBase());
                        var3.removeModifiers();

                        for(AttributeModifier var4 : var2.getModifiers()) {
                            var3.addTransientModifier(var4);
                        }
                    }
                }

            }
        }
    }

    @Override
    public void handlePlaceRecipe(ClientboundPlaceGhostRecipePacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        AbstractContainerMenu var0 = this.minecraft.player.containerMenu;
        if (var0.containerId == param0.getContainerId()) {
            this.recipeManager.byKey(param0.getRecipe()).ifPresent(param1 -> {
                if (this.minecraft.screen instanceof RecipeUpdateListener) {
                    RecipeBookComponent var0x = ((RecipeUpdateListener)this.minecraft.screen).getRecipeBookComponent();
                    var0x.setupGhostRecipe(param1, var0.slots);
                }

            });
        }
    }

    @Override
    public void handleLightUpdatePacket(ClientboundLightUpdatePacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        int var0 = param0.getX();
        int var1 = param0.getZ();
        ClientboundLightUpdatePacketData var2 = param0.getLightData();
        this.level.queueLightUpdate(() -> this.applyLightData(var0, var1, var2));
    }

    private void applyLightData(int param0, int param1, ClientboundLightUpdatePacketData param2) {
        LevelLightEngine var0 = this.level.getChunkSource().getLightEngine();
        BitSet var1 = param2.getSkyYMask();
        BitSet var2 = param2.getEmptySkyYMask();
        Iterator<byte[]> var3 = param2.getSkyUpdates().iterator();
        this.readSectionList(param0, param1, var0, LightLayer.SKY, var1, var2, var3);
        BitSet var4 = param2.getBlockYMask();
        BitSet var5 = param2.getEmptyBlockYMask();
        Iterator<byte[]> var6 = param2.getBlockUpdates().iterator();
        this.readSectionList(param0, param1, var0, LightLayer.BLOCK, var4, var5, var6);
        var0.setLightEnabled(new ChunkPos(param0, param1), true);
    }

    @Override
    public void handleMerchantOffers(ClientboundMerchantOffersPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        AbstractContainerMenu var0 = this.minecraft.player.containerMenu;
        if (param0.getContainerId() == var0.containerId && var0 instanceof MerchantMenu var1) {
            var1.setOffers(new MerchantOffers(param0.getOffers().createTag()));
            var1.setXp(param0.getVillagerXp());
            var1.setMerchantLevel(param0.getVillagerLevel());
            var1.setShowProgressBar(param0.showProgress());
            var1.setCanRestock(param0.canRestock());
        }

    }

    @Override
    public void handleSetChunkCacheRadius(ClientboundSetChunkCacheRadiusPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.serverChunkRadius = param0.getRadius();
        this.minecraft.options.setServerRenderDistance(this.serverChunkRadius);
        this.level.getChunkSource().updateViewRadius(param0.getRadius());
    }

    @Override
    public void handleSetSimulationDistance(ClientboundSetSimulationDistancePacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.serverSimulationDistance = param0.simulationDistance();
        this.level.setServerSimulationDistance(this.serverSimulationDistance);
    }

    @Override
    public void handleSetChunkCacheCenter(ClientboundSetChunkCacheCenterPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.level.getChunkSource().updateViewCenter(param0.getX(), param0.getZ());
    }

    @Override
    public void handleBlockChangedAck(ClientboundBlockChangedAckPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.level.handleBlockChangedAck(param0.sequence());
    }

    @Override
    public void handleBundlePacket(ClientboundBundlePacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);

        for(Packet<ClientGamePacketListener> var0 : param0.subPackets()) {
            var0.handle(this);
        }

    }

    private void readSectionList(int param0, int param1, LevelLightEngine param2, LightLayer param3, BitSet param4, BitSet param5, Iterator<byte[]> param6) {
        for(int var0 = 0; var0 < param2.getLightSectionCount(); ++var0) {
            int var1 = param2.getMinLightSection() + var0;
            boolean var2 = param4.get(var0);
            boolean var3 = param5.get(var0);
            if (var2 || var3) {
                param2.queueSectionData(param3, SectionPos.of(param0, var1, param1), var2 ? new DataLayer((byte[])param6.next().clone()) : new DataLayer());
                this.level.setSectionDirtyWithNeighbors(param0, var1, param1);
            }
        }

    }

    public Connection getConnection() {
        return this.connection;
    }

    @Override
    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }

    public Collection<PlayerInfo> getListedOnlinePlayers() {
        return this.listedPlayers;
    }

    public Collection<PlayerInfo> getOnlinePlayers() {
        return this.playerInfoMap.values();
    }

    public Collection<UUID> getOnlinePlayerIds() {
        return this.playerInfoMap.keySet();
    }

    @Nullable
    public PlayerInfo getPlayerInfo(UUID param0) {
        return this.playerInfoMap.get(param0);
    }

    @Nullable
    public PlayerInfo getPlayerInfo(String param0) {
        for(PlayerInfo var0 : this.playerInfoMap.values()) {
            if (var0.getProfile().getName().equals(param0)) {
                return var0;
            }
        }

        return null;
    }

    public GameProfile getLocalGameProfile() {
        return this.localGameProfile;
    }

    public ClientAdvancements getAdvancements() {
        return this.advancements;
    }

    public CommandDispatcher<SharedSuggestionProvider> getCommands() {
        return this.commands;
    }

    public ClientLevel getLevel() {
        return this.level;
    }

    public DebugQueryHandler getDebugQueryHandler() {
        return this.debugQueryHandler;
    }

    public UUID getId() {
        return this.id;
    }

    public Set<ResourceKey<Level>> levels() {
        return this.levels;
    }

    public RegistryAccess registryAccess() {
        return this.registryAccess.compositeAccess();
    }

    public void markMessageAsProcessed(PlayerChatMessage param0, boolean param1) {
        MessageSignature var0 = param0.signature();
        if (var0 != null && this.lastSeenMessages.addPending(var0, param1) && this.lastSeenMessages.offset() > 64) {
            this.sendChatAcknowledgement();
        }

    }

    private void sendChatAcknowledgement() {
        int var0 = this.lastSeenMessages.getAndClearOffset();
        if (var0 > 0) {
            this.send(new ServerboundChatAckPacket(var0));
        }

    }

    public void sendChat(String param0) {
        Instant var0 = Instant.now();
        long var1 = Crypt.SaltSupplier.getLong();
        LastSeenMessagesTracker.Update var2 = this.lastSeenMessages.generateAndApplyUpdate();
        MessageSignature var3 = this.signedMessageEncoder.pack(new SignedMessageBody(param0, var0, var1, var2.lastSeen()));
        this.send(new ServerboundChatPacket(param0, var0, var1, var3, var2.update()));
    }

    public void sendCommand(String param0) {
        Instant var0 = Instant.now();
        long var1 = Crypt.SaltSupplier.getLong();
        LastSeenMessagesTracker.Update var2 = this.lastSeenMessages.generateAndApplyUpdate();
        ArgumentSignatures var3 = ArgumentSignatures.signCommand(SignableCommand.of(this.parseCommand(param0)), param3 -> {
            SignedMessageBody var0x = new SignedMessageBody(param3, var0, var1, var2.lastSeen());
            return this.signedMessageEncoder.pack(var0x);
        });
        this.send(new ServerboundChatCommandPacket(param0, var0, var1, var3, var2.update()));
    }

    public boolean sendUnsignedCommand(String param0) {
        if (SignableCommand.of(this.parseCommand(param0)).arguments().isEmpty()) {
            LastSeenMessagesTracker.Update var0 = this.lastSeenMessages.generateAndApplyUpdate();
            this.send(new ServerboundChatCommandPacket(param0, Instant.now(), 0L, ArgumentSignatures.EMPTY, var0.update()));
            return true;
        } else {
            return false;
        }
    }

    private ParseResults<SharedSuggestionProvider> parseCommand(String param0) {
        return this.commands.parse(param0, this.suggestionsProvider);
    }

    @Override
    public void tick() {
        if (this.connection.isEncrypted()) {
            ProfileKeyPairManager var0 = this.minecraft.getProfileKeyPairManager();
            if (var0.shouldRefreshKeyPair()) {
                var0.prepareKeyPair().thenAcceptAsync(param0 -> param0.ifPresent(this::setKeyPair), this.minecraft);
            }
        }

        this.sendDeferredPackets();
        this.telemetryManager.tick();
    }

    public void setKeyPair(ProfileKeyPair param0) {
        if (this.localGameProfile.getId().equals(this.minecraft.getUser().getProfileId())) {
            if (this.chatSession == null || !this.chatSession.keyPair().equals(param0)) {
                this.chatSession = LocalChatSession.create(param0);
                this.signedMessageEncoder = this.chatSession.createMessageEncoder(this.localGameProfile.getId());
                this.send(new ServerboundChatSessionUpdatePacket(this.chatSession.asRemote().asData()));
            }
        }
    }

    @Nullable
    public ServerData getServerData() {
        return this.serverData;
    }

    public FeatureFlagSet enabledFeatures() {
        return this.enabledFeatures;
    }

    public boolean isFeatureEnabled(FeatureFlagSet param0) {
        return param0.isSubsetOf(this.enabledFeatures());
    }

    @OnlyIn(Dist.CLIENT)
    static record DeferredPacket(Packet<ServerGamePacketListener> packet, BooleanSupplier sendCondition, long expirationTime) {
    }
}
