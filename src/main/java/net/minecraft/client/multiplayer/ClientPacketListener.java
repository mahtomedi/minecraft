package net.minecraft.client.multiplayer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import io.netty.buffer.Unpooled;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.DebugQueryHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.gui.components.toasts.RecipeToast;
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
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.debug.BeeDebugRenderer;
import net.minecraft.client.renderer.debug.GoalSelectorDebugRenderer;
import net.minecraft.client.renderer.debug.NeighborsUpdateRenderer;
import net.minecraft.client.renderer.debug.VillageDebugRenderer;
import net.minecraft.client.renderer.debug.WorldGenAttemptRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.BeeAggressiveSoundInstance;
import net.minecraft.client.resources.sounds.BeeFlyingSoundInstance;
import net.minecraft.client.resources.sounds.BeeSoundInstance;
import net.minecraft.client.resources.sounds.GuardianAttackSoundInstance;
import net.minecraft.client.resources.sounds.MinecartSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.searchtree.MutableSearchTree;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.PositionImpl;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundAddExperienceOrbPacket;
import net.minecraft.network.protocol.game.ClientboundAddGlobalEntityPacket;
import net.minecraft.network.protocol.game.ClientboundAddMobPacket;
import net.minecraft.network.protocol.game.ClientboundAddPaintingPacket;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundAwardStatsPacket;
import net.minecraft.network.protocol.game.ClientboundBlockBreakAckPacket;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import net.minecraft.network.protocol.game.ClientboundChunkBlocksUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.network.protocol.game.ClientboundContainerAckPacket;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundCooldownPacket;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundCustomSoundPacket;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundHorseScreenOpenPacket;
import net.minecraft.network.protocol.game.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.network.protocol.game.ClientboundPlaceGhostRecipePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerLookAtPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRecipePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundResourcePackPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSelectAdvancementsTabPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderPacket;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquippedItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.network.protocol.game.ClientboundSetSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.network.protocol.game.ClientboundTagQueryPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundContainerAckPacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundResourcePackPacket;
import net.minecraft.realms.DisconnectedRealmsScreen;
import net.minecraft.realms.RealmsScreenProxy;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatsCounter;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagManager;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.BaseAttributeMap;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.fishing.FishingHook;
import net.minecraft.world.entity.global.LightningBolt;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.entity.vehicle.MinecartCommandBlock;
import net.minecraft.world.entity.vehicle.MinecartFurnace;
import net.minecraft.world.entity.vehicle.MinecartHopper;
import net.minecraft.world.entity.vehicle.MinecartSpawner;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ClientPacketListener implements ClientGamePacketListener {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Connection connection;
    private final GameProfile localGameProfile;
    private final Screen callbackScreen;
    private Minecraft minecraft;
    private ClientLevel level;
    private boolean started;
    private final Map<UUID, PlayerInfo> playerInfoMap = Maps.newHashMap();
    private final ClientAdvancements advancements;
    private final ClientSuggestionProvider suggestionsProvider;
    private TagManager tags = new TagManager();
    private final DebugQueryHandler debugQueryHandler = new DebugQueryHandler(this);
    private int serverChunkRadius = 3;
    private final Random random = new Random();
    private CommandDispatcher<SharedSuggestionProvider> commands = new CommandDispatcher<>();
    private final RecipeManager recipeManager = new RecipeManager();
    private final UUID id = UUID.randomUUID();

    public ClientPacketListener(Minecraft param0, Screen param1, Connection param2, GameProfile param3) {
        this.minecraft = param0;
        this.callbackScreen = param1;
        this.connection = param2;
        this.localGameProfile = param3;
        this.advancements = new ClientAdvancements(param0);
        this.suggestionsProvider = new ClientSuggestionProvider(this, param0);
    }

    public ClientSuggestionProvider getSuggestionsProvider() {
        return this.suggestionsProvider;
    }

    public void cleanup() {
        this.level = null;
    }

    public RecipeManager getRecipeManager() {
        return this.recipeManager;
    }

    @Override
    public void handleLogin(ClientboundLoginPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.minecraft.gameMode = new MultiPlayerGameMode(this.minecraft, this);
        this.serverChunkRadius = param0.getChunkRadius();
        this.level = new ClientLevel(
            this,
            new LevelSettings(param0.getSeed(), param0.getGameType(), false, param0.isHardcore(), param0.getLevelType()),
            param0.getDimension(),
            this.serverChunkRadius,
            this.minecraft.getProfiler(),
            this.minecraft.levelRenderer
        );
        this.minecraft.setLevel(this.level);
        if (this.minecraft.player == null) {
            this.minecraft.player = this.minecraft.gameMode.createPlayer(this.level, new StatsCounter(), new ClientRecipeBook(this.level.getRecipeManager()));
            this.minecraft.player.yRot = -180.0F;
            if (this.minecraft.getSingleplayerServer() != null) {
                this.minecraft.getSingleplayerServer().setUUID(this.minecraft.player.getUUID());
            }
        }

        this.minecraft.debugRenderer.clear();
        this.minecraft.player.resetPos();
        int var0 = param0.getPlayerId();
        this.level.addPlayer(var0, this.minecraft.player);
        this.minecraft.player.input = new KeyboardInput(this.minecraft.options);
        this.minecraft.gameMode.adjustPlayer(this.minecraft.player);
        this.minecraft.cameraEntity = this.minecraft.player;
        this.minecraft.player.dimension = param0.getDimension();
        this.minecraft.setScreen(new ReceivingLevelScreen());
        this.minecraft.player.setId(var0);
        this.minecraft.player.setReducedDebugInfo(param0.isReducedDebugInfo());
        this.minecraft.player.setShowDeathScreen(param0.shouldShowDeathScreen());
        this.minecraft.gameMode.setLocalMode(param0.getGameType());
        this.minecraft.options.broadcastOptions();
        this.connection
            .send(
                new ServerboundCustomPayloadPacket(
                    ServerboundCustomPayloadPacket.BRAND, new FriendlyByteBuf(Unpooled.buffer()).writeUtf(ClientBrandRetriever.getClientModName())
                )
            );
        this.minecraft.getGame().onStartGameSession();
    }

    @Override
    public void handleAddEntity(ClientboundAddEntityPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        double var0 = param0.getX();
        double var1 = param0.getY();
        double var2 = param0.getZ();
        EntityType<?> var3 = param0.getType();
        Entity var4;
        if (var3 == EntityType.CHEST_MINECART) {
            var4 = new MinecartChest(this.level, var0, var1, var2);
        } else if (var3 == EntityType.FURNACE_MINECART) {
            var4 = new MinecartFurnace(this.level, var0, var1, var2);
        } else if (var3 == EntityType.TNT_MINECART) {
            var4 = new MinecartTNT(this.level, var0, var1, var2);
        } else if (var3 == EntityType.SPAWNER_MINECART) {
            var4 = new MinecartSpawner(this.level, var0, var1, var2);
        } else if (var3 == EntityType.HOPPER_MINECART) {
            var4 = new MinecartHopper(this.level, var0, var1, var2);
        } else if (var3 == EntityType.COMMAND_BLOCK_MINECART) {
            var4 = new MinecartCommandBlock(this.level, var0, var1, var2);
        } else if (var3 == EntityType.MINECART) {
            var4 = new Minecart(this.level, var0, var1, var2);
        } else if (var3 == EntityType.FISHING_BOBBER) {
            Entity var11 = this.level.getEntity(param0.getData());
            if (var11 instanceof Player) {
                var4 = new FishingHook(this.level, (Player)var11, var0, var1, var2);
            } else {
                var4 = null;
            }
        } else if (var3 == EntityType.ARROW) {
            var4 = new Arrow(this.level, var0, var1, var2);
            Entity var15 = this.level.getEntity(param0.getData());
            if (var15 != null) {
                ((AbstractArrow)var4).setOwner(var15);
            }
        } else if (var3 == EntityType.SPECTRAL_ARROW) {
            var4 = new SpectralArrow(this.level, var0, var1, var2);
            Entity var17 = this.level.getEntity(param0.getData());
            if (var17 != null) {
                ((AbstractArrow)var4).setOwner(var17);
            }
        } else if (var3 == EntityType.TRIDENT) {
            var4 = new ThrownTrident(this.level, var0, var1, var2);
            Entity var19 = this.level.getEntity(param0.getData());
            if (var19 != null) {
                ((AbstractArrow)var4).setOwner(var19);
            }
        } else if (var3 == EntityType.SNOWBALL) {
            var4 = new Snowball(this.level, var0, var1, var2);
        } else if (var3 == EntityType.LLAMA_SPIT) {
            var4 = new LlamaSpit(this.level, var0, var1, var2, param0.getXa(), param0.getYa(), param0.getZa());
        } else if (var3 == EntityType.ITEM_FRAME) {
            var4 = new ItemFrame(this.level, new BlockPos(var0, var1, var2), Direction.from3DDataValue(param0.getData()));
        } else if (var3 == EntityType.LEASH_KNOT) {
            var4 = new LeashFenceKnotEntity(this.level, new BlockPos(var0, var1, var2));
        } else if (var3 == EntityType.ENDER_PEARL) {
            var4 = new ThrownEnderpearl(this.level, var0, var1, var2);
        } else if (var3 == EntityType.EYE_OF_ENDER) {
            var4 = new EyeOfEnder(this.level, var0, var1, var2);
        } else if (var3 == EntityType.FIREWORK_ROCKET) {
            var4 = new FireworkRocketEntity(this.level, var0, var1, var2, ItemStack.EMPTY);
        } else if (var3 == EntityType.FIREBALL) {
            var4 = new LargeFireball(this.level, var0, var1, var2, param0.getXa(), param0.getYa(), param0.getZa());
        } else if (var3 == EntityType.DRAGON_FIREBALL) {
            var4 = new DragonFireball(this.level, var0, var1, var2, param0.getXa(), param0.getYa(), param0.getZa());
        } else if (var3 == EntityType.SMALL_FIREBALL) {
            var4 = new SmallFireball(this.level, var0, var1, var2, param0.getXa(), param0.getYa(), param0.getZa());
        } else if (var3 == EntityType.WITHER_SKULL) {
            var4 = new WitherSkull(this.level, var0, var1, var2, param0.getXa(), param0.getYa(), param0.getZa());
        } else if (var3 == EntityType.SHULKER_BULLET) {
            var4 = new ShulkerBullet(this.level, var0, var1, var2, param0.getXa(), param0.getYa(), param0.getZa());
        } else if (var3 == EntityType.EGG) {
            var4 = new ThrownEgg(this.level, var0, var1, var2);
        } else if (var3 == EntityType.EVOKER_FANGS) {
            var4 = new EvokerFangs(this.level, var0, var1, var2, 0.0F, 0, null);
        } else if (var3 == EntityType.POTION) {
            var4 = new ThrownPotion(this.level, var0, var1, var2);
        } else if (var3 == EntityType.EXPERIENCE_BOTTLE) {
            var4 = new ThrownExperienceBottle(this.level, var0, var1, var2);
        } else if (var3 == EntityType.BOAT) {
            var4 = new Boat(this.level, var0, var1, var2);
        } else if (var3 == EntityType.TNT) {
            var4 = new PrimedTnt(this.level, var0, var1, var2, null);
        } else if (var3 == EntityType.ARMOR_STAND) {
            var4 = new ArmorStand(this.level, var0, var1, var2);
        } else if (var3 == EntityType.END_CRYSTAL) {
            var4 = new EndCrystal(this.level, var0, var1, var2);
        } else if (var3 == EntityType.ITEM) {
            var4 = new ItemEntity(this.level, var0, var1, var2);
        } else if (var3 == EntityType.FALLING_BLOCK) {
            var4 = new FallingBlockEntity(this.level, var0, var1, var2, Block.stateById(param0.getData()));
        } else if (var3 == EntityType.AREA_EFFECT_CLOUD) {
            var4 = new AreaEffectCloud(this.level, var0, var1, var2);
        } else {
            var4 = null;
        }

        if (var4 != null) {
            int var44 = param0.getId();
            var4.setPacketCoordinates(var0, var1, var2);
            var4.xRot = (float)(param0.getxRot() * 360) / 256.0F;
            var4.yRot = (float)(param0.getyRot() * 360) / 256.0F;
            var4.setId(var44);
            var4.setUUID(param0.getUUID());
            this.level.putNonPlayerEntity(var44, var4);
            if (var4 instanceof AbstractMinecart) {
                this.minecraft.getSoundManager().play(new MinecartSoundInstance((AbstractMinecart)var4));
            }
        }

    }

    @Override
    public void handleAddExperienceOrb(ClientboundAddExperienceOrbPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        double var0 = param0.getX();
        double var1 = param0.getY();
        double var2 = param0.getZ();
        Entity var3 = new ExperienceOrb(this.level, var0, var1, var2, param0.getValue());
        var3.setPacketCoordinates(var0, var1, var2);
        var3.yRot = 0.0F;
        var3.xRot = 0.0F;
        var3.setId(param0.getId());
        this.level.putNonPlayerEntity(param0.getId(), var3);
    }

    @Override
    public void handleAddGlobalEntity(ClientboundAddGlobalEntityPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        double var0 = param0.getX();
        double var1 = param0.getY();
        double var2 = param0.getZ();
        if (param0.getType() == 1) {
            LightningBolt var3 = new LightningBolt(this.level, var0, var1, var2, false);
            var3.setPacketCoordinates(var0, var1, var2);
            var3.yRot = 0.0F;
            var3.xRot = 0.0F;
            var3.setId(param0.getId());
            this.level.addLightning(var3);
        }

    }

    @Override
    public void handleAddPainting(ClientboundAddPaintingPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Painting var0 = new Painting(this.level, param0.getPos(), param0.getDirection(), param0.getMotive());
        var0.setId(param0.getId());
        var0.setUUID(param0.getUUID());
        this.level.putNonPlayerEntity(param0.getId(), var0);
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
        Entity var0 = this.level.getEntity(param0.getId());
        if (var0 != null && param0.getUnpackedData() != null) {
            var0.getEntityData().assignValues(param0.getUnpackedData());
        }

    }

    @Override
    public void handleAddPlayer(ClientboundAddPlayerPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        double var0 = param0.getX();
        double var1 = param0.getY();
        double var2 = param0.getZ();
        float var3 = (float)(param0.getyRot() * 360) / 256.0F;
        float var4 = (float)(param0.getxRot() * 360) / 256.0F;
        int var5 = param0.getEntityId();
        RemotePlayer var6 = new RemotePlayer(this.minecraft.level, this.getPlayerInfo(param0.getPlayerId()).getProfile());
        var6.setId(var5);
        var6.setPosAndOldPos(var0, var1, var2);
        var6.setPacketCoordinates(var0, var1, var2);
        var6.absMoveTo(var0, var1, var2, var3, var4);
        this.level.addPlayer(var5, var6);
    }

    @Override
    public void handleTeleportEntity(ClientboundTeleportEntityPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Entity var0 = this.level.getEntity(param0.getId());
        if (var0 != null) {
            double var1 = param0.getX();
            double var2 = param0.getY();
            double var3 = param0.getZ();
            var0.setPacketCoordinates(var1, var2, var3);
            if (!var0.isControlledByLocalInstance()) {
                float var4 = (float)(param0.getyRot() * 360) / 256.0F;
                float var5 = (float)(param0.getxRot() * 360) / 256.0F;
                if (!(Math.abs(var0.getX() - var1) >= 0.03125) && !(Math.abs(var0.getY() - var2) >= 0.015625) && !(Math.abs(var0.getZ() - var3) >= 0.03125)) {
                    var0.lerpTo(var0.getX(), var0.getY(), var0.getZ(), var4, var5, 0, true);
                } else {
                    var0.lerpTo(var1, var2, var3, var4, var5, 3, true);
                }

                var0.onGround = param0.isOnGround();
            }

        }
    }

    @Override
    public void handleSetCarriedItem(ClientboundSetCarriedItemPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        if (Inventory.isHotbarSlot(param0.getSlot())) {
            this.minecraft.player.inventory.selected = param0.getSlot();
        }

    }

    @Override
    public void handleMoveEntity(ClientboundMoveEntityPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Entity var0 = param0.getEntity(this.level);
        if (var0 != null) {
            if (!var0.isControlledByLocalInstance()) {
                if (param0.hasPosition()) {
                    var0.xp += (long)param0.getXa();
                    var0.yp += (long)param0.getYa();
                    var0.zp += (long)param0.getZa();
                    Vec3 var1 = ClientboundMoveEntityPacket.packetToEntity(var0.xp, var0.yp, var0.zp);
                    float var2 = param0.hasRotation() ? (float)(param0.getyRot() * 360) / 256.0F : var0.yRot;
                    float var3 = param0.hasRotation() ? (float)(param0.getxRot() * 360) / 256.0F : var0.xRot;
                    var0.lerpTo(var1.x, var1.y, var1.z, var2, var3, 3, false);
                } else if (param0.hasRotation()) {
                    float var4 = (float)param0.getyRot();
                    float var5 = (float)param0.getxRot();
                    var0.lerpTo(var0.getX(), var0.getY(), var0.getZ(), var4, var5, 3, false);
                }

                var0.onGround = param0.isOnGround();
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
    public void handleRemoveEntity(ClientboundRemoveEntitiesPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);

        for(int var0 = 0; var0 < param0.getEntityIds().length; ++var0) {
            int var1 = param0.getEntityIds()[var0];
            this.level.removeEntity(var1);
        }

    }

    @Override
    public void handleMovePlayer(ClientboundPlayerPositionPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Player var0 = this.minecraft.player;
        Vec3 var1 = var0.getDeltaMovement();
        boolean var2 = param0.getRelativeArguments().contains(ClientboundPlayerPositionPacket.RelativeArgument.X);
        boolean var3 = param0.getRelativeArguments().contains(ClientboundPlayerPositionPacket.RelativeArgument.Y);
        boolean var4 = param0.getRelativeArguments().contains(ClientboundPlayerPositionPacket.RelativeArgument.Z);
        double var5;
        double var6;
        if (var2) {
            var5 = var1.x();
            var6 = var0.getX() + param0.getX();
            var0.xOld += param0.getX();
        } else {
            var5 = 0.0;
            var6 = param0.getX();
            var0.xOld = var6;
        }

        double var9;
        double var10;
        if (var3) {
            var9 = var1.y();
            var10 = var0.getY() + param0.getY();
            var0.yOld += param0.getY();
        } else {
            var9 = 0.0;
            var10 = param0.getY();
            var0.yOld = var10;
        }

        double var13;
        double var14;
        if (var4) {
            var13 = var1.z();
            var14 = var0.getZ() + param0.getZ();
            var0.zOld += param0.getZ();
        } else {
            var13 = 0.0;
            var14 = param0.getZ();
            var0.zOld = var14;
        }

        var0.setPosRaw(var6, var10, var14);
        var0.xo = var6;
        var0.yo = var10;
        var0.zo = var14;
        var0.setDeltaMovement(var5, var9, var13);
        float var17 = param0.getYRot();
        float var18 = param0.getXRot();
        if (param0.getRelativeArguments().contains(ClientboundPlayerPositionPacket.RelativeArgument.X_ROT)) {
            var18 += var0.xRot;
        }

        if (param0.getRelativeArguments().contains(ClientboundPlayerPositionPacket.RelativeArgument.Y_ROT)) {
            var17 += var0.yRot;
        }

        var0.absMoveTo(var6, var10, var14, var17, var18);
        this.connection.send(new ServerboundAcceptTeleportationPacket(param0.getId()));
        this.connection.send(new ServerboundMovePlayerPacket.PosRot(var0.getX(), var0.getY(), var0.getZ(), var0.yRot, var0.xRot, false));
        if (!this.started) {
            this.started = true;
            this.minecraft.setScreen(null);
        }

    }

    @Override
    public void handleChunkBlocksUpdate(ClientboundChunkBlocksUpdatePacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);

        for(ClientboundChunkBlocksUpdatePacket.BlockUpdate var0 : param0.getUpdates()) {
            this.level.setKnownState(var0.getPos(), var0.getBlock());
        }

    }

    @Override
    public void handleLevelChunk(ClientboundLevelChunkPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        int var0 = param0.getX();
        int var1 = param0.getZ();
        LevelChunk var2 = this.level
            .getChunkSource()
            .replaceWithPacketData(var0, var1, param0.getBiomes(), param0.getReadBuffer(), param0.getHeightmaps(), param0.getAvailableSections());
        if (var2 != null && param0.isFullChunk()) {
            this.level.reAddEntitiesToChunk(var2);
        }

        for(int var3 = 0; var3 < 16; ++var3) {
            this.level.setSectionDirtyWithNeighbors(var0, var3, var1);
        }

        for(CompoundTag var4 : param0.getBlockEntitiesTags()) {
            BlockPos var5 = new BlockPos(var4.getInt("x"), var4.getInt("y"), var4.getInt("z"));
            BlockEntity var6 = this.level.getBlockEntity(var5);
            if (var6 != null) {
                var6.load(var4);
            }
        }

    }

    @Override
    public void handleForgetLevelChunk(ClientboundForgetLevelChunkPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        int var0 = param0.getX();
        int var1 = param0.getZ();
        ClientChunkCache var2 = this.level.getChunkSource();
        var2.drop(var0, var1);
        LevelLightEngine var3 = var2.getLightEngine();

        for(int var4 = 0; var4 < 16; ++var4) {
            this.level.setSectionDirtyWithNeighbors(var0, var4, var1);
            var3.updateSectionStatus(SectionPos.of(var0, var4, var1), true);
        }

        var3.enableLightSources(new ChunkPos(var0, var1), false);
    }

    @Override
    public void handleBlockUpdate(ClientboundBlockUpdatePacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.level.setKnownState(param0.getPos(), param0.getBlockState());
    }

    @Override
    public void handleDisconnect(ClientboundDisconnectPacket param0) {
        this.connection.disconnect(param0.getReason());
    }

    @Override
    public void onDisconnect(Component param0) {
        this.minecraft.clearLevel();
        if (this.callbackScreen != null) {
            if (this.callbackScreen instanceof RealmsScreenProxy) {
                this.minecraft
                    .setScreen(new DisconnectedRealmsScreen(((RealmsScreenProxy)this.callbackScreen).getScreen(), "disconnect.lost", param0).getProxy());
            } else {
                this.minecraft.setScreen(new DisconnectedScreen(this.callbackScreen, "disconnect.lost", param0));
            }
        } else {
            this.minecraft.setScreen(new DisconnectedScreen(new JoinMultiplayerScreen(new TitleScreen()), "disconnect.lost", param0));
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

            if (var0 instanceof ItemEntity) {
                ((ItemEntity)var0).getItem().setCount(param0.getAmount());
            }

            this.minecraft
                .particleEngine
                .add(new ItemPickupParticle(this.minecraft.getEntityRenderDispatcher(), this.minecraft.renderBuffers(), this.level, var0, var1));
            this.level.removeEntity(param0.getItemId());
        }

    }

    @Override
    public void handleChat(ClientboundChatPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.minecraft.gui.handleChat(param0.getType(), param0.getMessage());
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
            } else if (param0.getAction() == 1) {
                var0.animateHurt();
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
    public void handleAddMob(ClientboundAddMobPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        double var0 = param0.getX();
        double var1 = param0.getY();
        double var2 = param0.getZ();
        float var3 = (float)(param0.getyRot() * 360) / 256.0F;
        float var4 = (float)(param0.getxRot() * 360) / 256.0F;
        LivingEntity var5 = (LivingEntity)EntityType.create(param0.getType(), this.minecraft.level);
        if (var5 != null) {
            var5.setPacketCoordinates(var0, var1, var2);
            var5.yBodyRot = (float)(param0.getyHeadRot() * 360) / 256.0F;
            var5.yHeadRot = (float)(param0.getyHeadRot() * 360) / 256.0F;
            if (var5 instanceof EnderDragon) {
                EnderDragonPart[] var6 = ((EnderDragon)var5).getSubEntities();

                for(int var7 = 0; var7 < var6.length; ++var7) {
                    var6[var7].setId(var7 + param0.getId());
                }
            }

            var5.setId(param0.getId());
            var5.setUUID(param0.getUUID());
            var5.absMoveTo(var0, var1, var2, var3, var4);
            var5.setDeltaMovement(
                (double)((float)param0.getXd() / 8000.0F), (double)((float)param0.getYd() / 8000.0F), (double)((float)param0.getZd() / 8000.0F)
            );
            this.level.putNonPlayerEntity(param0.getId(), var5);
            if (var5 instanceof Bee) {
                boolean var8 = ((Bee)var5).isAngry();
                BeeSoundInstance var9;
                if (var8) {
                    var9 = new BeeAggressiveSoundInstance((Bee)var5);
                } else {
                    var9 = new BeeFlyingSoundInstance((Bee)var5);
                }

                this.minecraft.getSoundManager().play(var9);
            }
        } else {
            LOGGER.warn("Skipping Entity with id {}", param0.getType());
        }

    }

    @Override
    public void handleSetTime(ClientboundSetTimePacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.minecraft.level.setGameTime(param0.getGameTime());
        this.minecraft.level.setDayTime(param0.getDayTime());
    }

    @Override
    public void handleSetSpawn(ClientboundSetSpawnPositionPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.minecraft.player.setRespawnPosition(param0.getPos(), true);
        this.minecraft.level.getLevelData().setSpawn(param0.getPos());
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
                        this.minecraft.gui.setOverlayMessage(I18n.get("mount.onboard", this.minecraft.options.keyShift.getTranslatedKeyMessage()), false);
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
            if (var1.getItem() == Items.TOTEM_OF_UNDYING) {
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
            if (param0.getEventId() == 21) {
                this.minecraft.getSoundManager().play(new GuardianAttackSoundInstance((Guardian)var0));
            } else if (param0.getEventId() == 35) {
                int var1 = 40;
                this.minecraft.particleEngine.createTrackingEmitter(var0, ParticleTypes.TOTEM_OF_UNDYING, 30);
                this.level.playLocalSound(var0.getX(), var0.getY(), var0.getZ(), SoundEvents.TOTEM_USE, var0.getSoundSource(), 1.0F, 1.0F, false);
                if (var0 == this.minecraft.player) {
                    this.minecraft.gameRenderer.displayItemActivation(findTotem(this.minecraft.player));
                }
            } else {
                var0.handleEntityEvent(param0.getEventId());
            }
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
        DimensionType var0 = param0.getDimension();
        LocalPlayer var1 = this.minecraft.player;
        int var2 = var1.getId();
        if (var0 != var1.dimension) {
            this.started = false;
            Scoreboard var3 = this.level.getScoreboard();
            this.level = new ClientLevel(
                this,
                new LevelSettings(param0.getSeed(), param0.getPlayerGameType(), false, this.minecraft.level.getLevelData().isHardcore(), param0.getLevelType()),
                param0.getDimension(),
                this.serverChunkRadius,
                this.minecraft.getProfiler(),
                this.minecraft.levelRenderer
            );
            this.level.setScoreboard(var3);
            this.minecraft.setLevel(this.level);
            this.minecraft.setScreen(new ReceivingLevelScreen());
        }

        this.level.validateSpawn();
        this.level.removeAllPendingEntityRemovals();
        String var4 = var1.getServerBrand();
        this.minecraft.cameraEntity = null;
        LocalPlayer var5 = this.minecraft.gameMode.createPlayer(this.level, var1.getStats(), var1.getRecipeBook());
        var5.setId(var2);
        var5.dimension = var0;
        this.minecraft.player = var5;
        this.minecraft.cameraEntity = var5;
        var5.getEntityData().assignValues(var1.getEntityData().getAll());
        var5.getAttributes().assignValues(var1.getAttributes());
        var5.resetPos();
        var5.setServerBrand(var4);
        this.level.addPlayer(var2, var5);
        var5.yRot = -180.0F;
        var5.input = new KeyboardInput(this.minecraft.options);
        this.minecraft.gameMode.adjustPlayer(var5);
        var5.setReducedDebugInfo(var1.isReducedDebugInfo());
        var5.setShowDeathScreen(var1.shouldShowDeathScreen());
        if (this.minecraft.screen instanceof DeathScreen) {
            this.minecraft.setScreen(null);
        }

        this.minecraft.gameMode.setLocalMode(param0.getPlayerGameType());
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
            HorseInventoryMenu var4 = new HorseInventoryMenu(param0.getContainerId(), var1.inventory, var3, var2);
            var1.containerMenu = var4;
            this.minecraft.setScreen(new HorseInventoryScreen(var4, var1.inventory, var2));
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
                var0.inventory.setCarried(var1);
            }
        } else if (param0.getContainerId() == -2) {
            var0.inventory.setItem(var2, var1);
        } else {
            boolean var3 = false;
            if (this.minecraft.screen instanceof CreativeModeInventoryScreen) {
                CreativeModeInventoryScreen var4 = (CreativeModeInventoryScreen)this.minecraft.screen;
                var3 = var4.getSelectedTab() != CreativeModeTab.TAB_INVENTORY.getId();
            }

            if (param0.getContainerId() == 0 && param0.getSlot() >= 36 && var2 < 45) {
                if (!var1.isEmpty()) {
                    ItemStack var5 = var0.inventoryMenu.getSlot(var2).getItem();
                    if (var5.isEmpty() || var5.getCount() < var1.getCount()) {
                        var1.setPopTime(5);
                    }
                }

                var0.inventoryMenu.setItem(var2, var1);
            } else if (param0.getContainerId() == var0.containerMenu.containerId && (param0.getContainerId() != 0 || !var3)) {
                var0.containerMenu.setItem(var2, var1);
            }
        }

    }

    @Override
    public void handleContainerAck(ClientboundContainerAckPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        AbstractContainerMenu var0 = null;
        Player var1 = this.minecraft.player;
        if (param0.getContainerId() == 0) {
            var0 = var1.inventoryMenu;
        } else if (param0.getContainerId() == var1.containerMenu.containerId) {
            var0 = var1.containerMenu;
        }

        if (var0 != null && !param0.isAccepted()) {
            this.send(new ServerboundContainerAckPacket(param0.getContainerId(), param0.getUid(), true));
        }

    }

    @Override
    public void handleContainerContent(ClientboundContainerSetContentPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Player var0 = this.minecraft.player;
        if (param0.getContainerId() == 0) {
            var0.inventoryMenu.setAll(param0.getItems());
        } else if (param0.getContainerId() == var0.containerMenu.containerId) {
            var0.containerMenu.setAll(param0.getItems());
        }

    }

    @Override
    public void handleOpenSignEditor(ClientboundOpenSignEditorPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        BlockEntity var0 = this.level.getBlockEntity(param0.getPos());
        if (!(var0 instanceof SignBlockEntity)) {
            var0 = new SignBlockEntity();
            var0.setLevelAndPosition(this.level, param0.getPos());
        }

        this.minecraft.player.openTextEdit((SignBlockEntity)var0);
    }

    @Override
    public void handleBlockEntityData(ClientboundBlockEntityDataPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        if (this.minecraft.level.hasChunkAt(param0.getPos())) {
            BlockEntity var0 = this.minecraft.level.getBlockEntity(param0.getPos());
            int var1 = param0.getType();
            boolean var2 = var1 == 2 && var0 instanceof CommandBlockEntity;
            if (var1 == 1 && var0 instanceof SpawnerBlockEntity
                || var2
                || var1 == 3 && var0 instanceof BeaconBlockEntity
                || var1 == 4 && var0 instanceof SkullBlockEntity
                || var1 == 6 && var0 instanceof BannerBlockEntity
                || var1 == 7 && var0 instanceof StructureBlockEntity
                || var1 == 8 && var0 instanceof TheEndGatewayBlockEntity
                || var1 == 9 && var0 instanceof SignBlockEntity
                || var1 == 11 && var0 instanceof BedBlockEntity
                || var1 == 5 && var0 instanceof ConduitBlockEntity
                || var1 == 12 && var0 instanceof JigsawBlockEntity
                || var1 == 13 && var0 instanceof CampfireBlockEntity
                || var1 == 14 && var0 instanceof BeehiveBlockEntity) {
                var0.load(param0.getTag());
            }

            if (var2 && this.minecraft.screen instanceof CommandBlockEditScreen) {
                ((CommandBlockEditScreen)this.minecraft.screen).updateGui();
            }
        }

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
    public void handleSetEquippedItem(ClientboundSetEquippedItemPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Entity var0 = this.level.getEntity(param0.getEntity());
        if (var0 != null) {
            var0.setItemSlot(param0.getSlot(), param0.getItem());
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
        int var1 = param0.getEvent();
        float var2 = param0.getParam();
        int var3 = Mth.floor(var2 + 0.5F);
        if (var1 >= 0 && var1 < ClientboundGameEventPacket.EVENT_LANGUAGE_ID.length && ClientboundGameEventPacket.EVENT_LANGUAGE_ID[var1] != null) {
            var0.displayClientMessage(new TranslatableComponent(ClientboundGameEventPacket.EVENT_LANGUAGE_ID[var1]), false);
        }

        if (var1 == 1) {
            this.level.getLevelData().setRaining(true);
            this.level.setRainLevel(0.0F);
        } else if (var1 == 2) {
            this.level.getLevelData().setRaining(false);
            this.level.setRainLevel(1.0F);
        } else if (var1 == 3) {
            this.minecraft.gameMode.setLocalMode(GameType.byId(var3));
        } else if (var1 == 4) {
            if (var3 == 0) {
                this.minecraft.player.connection.send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN));
                this.minecraft.setScreen(new ReceivingLevelScreen());
            } else if (var3 == 1) {
                this.minecraft
                    .setScreen(
                        new WinScreen(
                            true,
                            () -> this.minecraft
                                    .player
                                    .connection
                                    .send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN))
                        )
                    );
            }
        } else if (var1 == 5) {
            Options var4 = this.minecraft.options;
            if (var2 == 0.0F) {
                this.minecraft.setScreen(new DemoIntroScreen());
            } else if (var2 == 101.0F) {
                this.minecraft
                    .gui
                    .getChat()
                    .addMessage(
                        new TranslatableComponent(
                            "demo.help.movement",
                            var4.keyUp.getTranslatedKeyMessage(),
                            var4.keyLeft.getTranslatedKeyMessage(),
                            var4.keyDown.getTranslatedKeyMessage(),
                            var4.keyRight.getTranslatedKeyMessage()
                        )
                    );
            } else if (var2 == 102.0F) {
                this.minecraft.gui.getChat().addMessage(new TranslatableComponent("demo.help.jump", var4.keyJump.getTranslatedKeyMessage()));
            } else if (var2 == 103.0F) {
                this.minecraft.gui.getChat().addMessage(new TranslatableComponent("demo.help.inventory", var4.keyInventory.getTranslatedKeyMessage()));
            } else if (var2 == 104.0F) {
                this.minecraft.gui.getChat().addMessage(new TranslatableComponent("demo.day.6", var4.keyScreenshot.getTranslatedKeyMessage()));
            }
        } else if (var1 == 6) {
            this.level.playSound(var0, var0.getX(), var0.getEyeY(), var0.getZ(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 0.18F, 0.45F);
        } else if (var1 == 7) {
            this.level.setRainLevel(var2);
        } else if (var1 == 8) {
            this.level.setThunderLevel(var2);
        } else if (var1 == 9) {
            this.level.playSound(var0, var0.getX(), var0.getY(), var0.getZ(), SoundEvents.PUFFER_FISH_STING, SoundSource.NEUTRAL, 1.0F, 1.0F);
        } else if (var1 == 10) {
            this.level.addParticle(ParticleTypes.ELDER_GUARDIAN, var0.getX(), var0.getY(), var0.getZ(), 0.0, 0.0, 0.0);
            this.level.playSound(var0, var0.getX(), var0.getY(), var0.getZ(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.HOSTILE, 1.0F, 1.0F);
        } else if (var1 == 11) {
            this.minecraft.player.setShowDeathScreen(var2 == 0.0F);
        }

    }

    @Override
    public void handleMapItemData(ClientboundMapItemDataPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        MapRenderer var0 = this.minecraft.gameRenderer.getMapRenderer();
        String var1 = MapItem.makeKey(param0.getMapId());
        MapItemSavedData var2 = this.minecraft.level.getMapData(var1);
        if (var2 == null) {
            var2 = new MapItemSavedData(var1);
            if (var0.getMapInstanceIfExists(var1) != null) {
                MapItemSavedData var3 = var0.getData(var0.getMapInstanceIfExists(var1));
                if (var3 != null) {
                    var2 = var3;
                }
            }

            this.minecraft.level.setMapData(var2);
        }

        param0.applyToMap(var2);
        var0.update(var2);
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
        this.commands = new CommandDispatcher<>(param0.getRoot());
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
        MutableSearchTree<RecipeCollection> var0 = this.minecraft.getSearchTree(SearchRegistry.RECIPE_COLLECTIONS);
        var0.clear();
        ClientRecipeBook var1 = this.minecraft.player.getRecipeBook();
        var1.setupCollections();
        var1.getCollections().forEach(var0::add);
        var0.refresh();
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
        var0.setGuiOpen(param0.isGuiOpen());
        var0.setFilteringCraftable(param0.isFilteringCraftable());
        var0.setFurnaceGuiOpen(param0.isFurnaceGuiOpen());
        var0.setFurnaceFilteringCraftable(param0.isFurnaceFilteringCraftable());
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
                        RecipeToast.addOrUpdate(this.minecraft.getToasts(), param1);
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
            MobEffect var1 = MobEffect.byId(param0.getEffectId());
            if (var1 != null) {
                MobEffectInstance var2 = new MobEffectInstance(
                    var1,
                    param0.getEffectDurationTicks(),
                    param0.getEffectAmplifier(),
                    param0.isEffectAmbient(),
                    param0.isEffectVisible(),
                    param0.effectShowsIcon()
                );
                var2.setNoCounter(param0.isSuperLongDuration());
                ((LivingEntity)var0).addEffect(var2);
            }
        }
    }

    @Override
    public void handleUpdateTags(ClientboundUpdateTagsPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.tags = param0.getTags();
        if (!this.connection.isMemoryConnection()) {
            BlockTags.reset(this.tags.getBlocks());
            ItemTags.reset(this.tags.getItems());
            FluidTags.reset(this.tags.getFluids());
            EntityTypeTags.reset(this.tags.getEntityTypes());
        }

        this.minecraft.getSearchTree(SearchRegistry.CREATIVE_TAGS).refresh();
    }

    @Override
    public void handlePlayerCombat(ClientboundPlayerCombatPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        if (param0.event == ClientboundPlayerCombatPacket.Event.ENTITY_DIED) {
            Entity var0 = this.level.getEntity(param0.playerId);
            if (var0 == this.minecraft.player) {
                if (this.minecraft.player.shouldShowDeathScreen()) {
                    this.minecraft.setScreen(new DeathScreen(param0.message, this.level.getLevelData().isHardcore()));
                } else {
                    this.minecraft.player.respawn();
                }
            }
        }

    }

    @Override
    public void handleChangeDifficulty(ClientboundChangeDifficultyPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.minecraft.level.getLevelData().setDifficulty(param0.getDifficulty());
        this.minecraft.level.getLevelData().setDifficultyLocked(param0.isLocked());
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
    public void handleSetBorder(ClientboundSetBorderPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        param0.applyChanges(this.level.getWorldBorder());
    }

    @Override
    public void handleSetTitles(ClientboundSetTitlesPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        ClientboundSetTitlesPacket.Type var0 = param0.getType();
        String var1 = null;
        String var2 = null;
        String var3 = param0.getText() != null ? param0.getText().getColoredString() : "";
        switch(var0) {
            case TITLE:
                var1 = var3;
                break;
            case SUBTITLE:
                var2 = var3;
                break;
            case ACTIONBAR:
                this.minecraft.gui.setOverlayMessage(var3, false);
                return;
            case RESET:
                this.minecraft.gui.setTitles("", "", -1, -1, -1);
                this.minecraft.gui.resetTitleTimes();
                return;
        }

        this.minecraft.gui.setTitles(var1, var2, param0.getFadeInTime(), param0.getStayTime(), param0.getFadeOutTime());
    }

    @Override
    public void handleTabListCustomisation(ClientboundTabListPacket param0) {
        this.minecraft.gui.getTabList().setHeader(param0.getHeader().getColoredString().isEmpty() ? null : param0.getHeader());
        this.minecraft.gui.getTabList().setFooter(param0.getFooter().getColoredString().isEmpty() ? null : param0.getFooter());
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
    public void handlePlayerInfo(ClientboundPlayerInfoPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);

        for(ClientboundPlayerInfoPacket.PlayerUpdate var0 : param0.getEntries()) {
            if (param0.getAction() == ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER) {
                this.playerInfoMap.remove(var0.getProfile().getId());
            } else {
                PlayerInfo var1 = this.playerInfoMap.get(var0.getProfile().getId());
                if (param0.getAction() == ClientboundPlayerInfoPacket.Action.ADD_PLAYER) {
                    var1 = new PlayerInfo(var0);
                    this.playerInfoMap.put(var1.getProfile().getId(), var1);
                }

                if (var1 != null) {
                    switch(param0.getAction()) {
                        case ADD_PLAYER:
                            var1.setGameMode(var0.getGameMode());
                            var1.setLatency(var0.getLatency());
                            var1.setTabListDisplayName(var0.getDisplayName());
                            break;
                        case UPDATE_GAME_MODE:
                            var1.setGameMode(var0.getGameMode());
                            break;
                        case UPDATE_LATENCY:
                            var1.setLatency(var0.getLatency());
                            break;
                        case UPDATE_DISPLAY_NAME:
                            var1.setTabListDisplayName(var0.getDisplayName());
                    }
                }
            }
        }

    }

    @Override
    public void handleKeepAlive(ClientboundKeepAlivePacket param0) {
        this.send(new ServerboundKeepAlivePacket(param0.getId()));
    }

    @Override
    public void handlePlayerAbilities(ClientboundPlayerAbilitiesPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Player var0 = this.minecraft.player;
        var0.abilities.flying = param0.isFlying();
        var0.abilities.instabuild = param0.canInstabuild();
        var0.abilities.invulnerable = param0.isInvulnerable();
        var0.abilities.mayfly = param0.canFly();
        var0.abilities.setFlyingSpeed(param0.getFlyingSpeed());
        var0.abilities.setWalkingSpeed(param0.getWalkingSpeed());
    }

    @Override
    public void handleSoundEvent(ClientboundSoundPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.minecraft
            .level
            .playSound(
                this.minecraft.player,
                param0.getX(),
                param0.getY(),
                param0.getZ(),
                param0.getSound(),
                param0.getSource(),
                param0.getVolume(),
                param0.getPitch()
            );
    }

    @Override
    public void handleSoundEntityEvent(ClientboundSoundEntityPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Entity var0 = this.level.getEntity(param0.getId());
        if (var0 != null) {
            this.minecraft.level.playSound(this.minecraft.player, var0, param0.getSound(), param0.getSource(), param0.getVolume(), param0.getPitch());
        }
    }

    @Override
    public void handleCustomSoundEvent(ClientboundCustomSoundPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.minecraft
            .getSoundManager()
            .play(
                new SimpleSoundInstance(
                    param0.getName(),
                    param0.getSource(),
                    param0.getVolume(),
                    param0.getPitch(),
                    false,
                    0,
                    SoundInstance.Attenuation.LINEAR,
                    (float)param0.getX(),
                    (float)param0.getY(),
                    (float)param0.getZ(),
                    false
                )
            );
    }

    @Override
    public void handleResourcePack(ClientboundResourcePackPacket param0) {
        String var0 = param0.getUrl();
        String var1 = param0.getHash();
        if (this.validateResourcePackUrl(var0)) {
            if (var0.startsWith("level://")) {
                try {
                    String var2 = URLDecoder.decode(var0.substring("level://".length()), StandardCharsets.UTF_8.toString());
                    File var3 = new File(this.minecraft.gameDirectory, "saves");
                    File var4 = new File(var3, var2);
                    if (var4.isFile()) {
                        this.send(ServerboundResourcePackPacket.Action.ACCEPTED);
                        CompletableFuture<?> var5 = this.minecraft.getClientPackSource().setServerPack(var4);
                        this.downloadCallback(var5);
                        return;
                    }
                } catch (UnsupportedEncodingException var8) {
                }

                this.send(ServerboundResourcePackPacket.Action.FAILED_DOWNLOAD);
            } else {
                ServerData var6 = this.minecraft.getCurrentServer();
                if (var6 != null && var6.getResourcePackStatus() == ServerData.ServerPackStatus.ENABLED) {
                    this.send(ServerboundResourcePackPacket.Action.ACCEPTED);
                    this.downloadCallback(this.minecraft.getClientPackSource().downloadAndSelectResourcePack(var0, var1));
                } else if (var6 != null && var6.getResourcePackStatus() != ServerData.ServerPackStatus.PROMPT) {
                    this.send(ServerboundResourcePackPacket.Action.DECLINED);
                } else {
                    this.minecraft.execute(() -> this.minecraft.setScreen(new ConfirmScreen(param2 -> {
                            this.minecraft = Minecraft.getInstance();
                            ServerData var0x = this.minecraft.getCurrentServer();
                            if (param2) {
                                if (var0x != null) {
                                    var0x.setResourcePackStatus(ServerData.ServerPackStatus.ENABLED);
                                }

                                this.send(ServerboundResourcePackPacket.Action.ACCEPTED);
                                this.downloadCallback(this.minecraft.getClientPackSource().downloadAndSelectResourcePack(var0, var1));
                            } else {
                                if (var0x != null) {
                                    var0x.setResourcePackStatus(ServerData.ServerPackStatus.DISABLED);
                                }

                                this.send(ServerboundResourcePackPacket.Action.DECLINED);
                            }

                            ServerList.saveSingleServer(var0x);
                            this.minecraft.setScreen(null);
                        }, new TranslatableComponent("multiplayer.texturePrompt.line1"), new TranslatableComponent("multiplayer.texturePrompt.line2"))));
                }

            }
        }
    }

    private boolean validateResourcePackUrl(String param0) {
        try {
            URI var0 = new URI(param0);
            String var1 = var0.getScheme();
            boolean var2 = "level".equals(var1);
            if (!"http".equals(var1) && !"https".equals(var1) && !var2) {
                throw new URISyntaxException(param0, "Wrong protocol");
            } else if (!var2 || !param0.contains("..") && param0.endsWith("/resources.zip")) {
                return true;
            } else {
                throw new URISyntaxException(param0, "Invalid levelstorage resourcepack path");
            }
        } catch (URISyntaxException var5) {
            this.send(ServerboundResourcePackPacket.Action.FAILED_DOWNLOAD);
            return false;
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
        if (var0.getItem() == Items.WRITTEN_BOOK) {
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
                this.minecraft.player.setServerBrand(var1.readUtf(32767));
            } else if (ClientboundCustomPayloadPacket.DEBUG_PATHFINDING_PACKET.equals(var0)) {
                int var2 = var1.readInt();
                float var3 = var1.readFloat();
                Path var4 = Path.createFromStream(var1);
                this.minecraft.debugRenderer.pathfindingRenderer.addPath(var2, var4, var3);
            } else if (ClientboundCustomPayloadPacket.DEBUG_NEIGHBORSUPDATE_PACKET.equals(var0)) {
                long var5 = var1.readVarLong();
                BlockPos var6 = var1.readBlockPos();
                ((NeighborsUpdateRenderer)this.minecraft.debugRenderer.neighborsUpdateRenderer).addUpdate(var5, var6);
            } else if (ClientboundCustomPayloadPacket.DEBUG_CAVES_PACKET.equals(var0)) {
                BlockPos var7 = var1.readBlockPos();
                int var8 = var1.readInt();
                List<BlockPos> var9 = Lists.newArrayList();
                List<Float> var10 = Lists.newArrayList();

                for(int var11 = 0; var11 < var8; ++var11) {
                    var9.add(var1.readBlockPos());
                    var10.add(var1.readFloat());
                }

                this.minecraft.debugRenderer.caveRenderer.addTunnel(var7, var9, var10);
            } else if (ClientboundCustomPayloadPacket.DEBUG_STRUCTURES_PACKET.equals(var0)) {
                DimensionType var12 = DimensionType.getById(var1.readInt());
                BoundingBox var13 = new BoundingBox(var1.readInt(), var1.readInt(), var1.readInt(), var1.readInt(), var1.readInt(), var1.readInt());
                int var14 = var1.readInt();
                List<BoundingBox> var15 = Lists.newArrayList();
                List<Boolean> var16 = Lists.newArrayList();

                for(int var17 = 0; var17 < var14; ++var17) {
                    var15.add(new BoundingBox(var1.readInt(), var1.readInt(), var1.readInt(), var1.readInt(), var1.readInt(), var1.readInt()));
                    var16.add(var1.readBoolean());
                }

                this.minecraft.debugRenderer.structureRenderer.addBoundingBox(var13, var15, var16, var12);
            } else if (ClientboundCustomPayloadPacket.DEBUG_WORLDGENATTEMPT_PACKET.equals(var0)) {
                ((WorldGenAttemptRenderer)this.minecraft.debugRenderer.worldGenAttemptRenderer)
                    .addPos(var1.readBlockPos(), var1.readFloat(), var1.readFloat(), var1.readFloat(), var1.readFloat(), var1.readFloat());
            } else if (ClientboundCustomPayloadPacket.DEBUG_VILLAGE_SECTIONS.equals(var0)) {
                int var18 = var1.readInt();

                for(int var19 = 0; var19 < var18; ++var19) {
                    this.minecraft.debugRenderer.villageDebugRenderer.setVillageSection(var1.readSectionPos());
                }

                int var20 = var1.readInt();

                for(int var21 = 0; var21 < var20; ++var21) {
                    this.minecraft.debugRenderer.villageDebugRenderer.setNotVillageSection(var1.readSectionPos());
                }
            } else if (ClientboundCustomPayloadPacket.DEBUG_POI_ADDED_PACKET.equals(var0)) {
                BlockPos var22 = var1.readBlockPos();
                String var23 = var1.readUtf();
                int var24 = var1.readInt();
                VillageDebugRenderer.PoiInfo var25 = new VillageDebugRenderer.PoiInfo(var22, var23, var24);
                this.minecraft.debugRenderer.villageDebugRenderer.addPoi(var25);
            } else if (ClientboundCustomPayloadPacket.DEBUG_POI_REMOVED_PACKET.equals(var0)) {
                BlockPos var26 = var1.readBlockPos();
                this.minecraft.debugRenderer.villageDebugRenderer.removePoi(var26);
            } else if (ClientboundCustomPayloadPacket.DEBUG_POI_TICKET_COUNT_PACKET.equals(var0)) {
                BlockPos var27 = var1.readBlockPos();
                int var28 = var1.readInt();
                this.minecraft.debugRenderer.villageDebugRenderer.setFreeTicketCount(var27, var28);
            } else if (ClientboundCustomPayloadPacket.DEBUG_GOAL_SELECTOR.equals(var0)) {
                BlockPos var29 = var1.readBlockPos();
                int var30 = var1.readInt();
                int var31 = var1.readInt();
                List<GoalSelectorDebugRenderer.DebugGoal> var32 = Lists.newArrayList();

                for(int var33 = 0; var33 < var31; ++var33) {
                    int var34 = var1.readInt();
                    boolean var35 = var1.readBoolean();
                    String var36 = var1.readUtf(255);
                    var32.add(new GoalSelectorDebugRenderer.DebugGoal(var29, var34, var36, var35));
                }

                this.minecraft.debugRenderer.goalSelectorRenderer.addGoalSelector(var30, var32);
            } else if (ClientboundCustomPayloadPacket.DEBUG_RAIDS.equals(var0)) {
                int var37 = var1.readInt();
                Collection<BlockPos> var38 = Lists.newArrayList();

                for(int var39 = 0; var39 < var37; ++var39) {
                    var38.add(var1.readBlockPos());
                }

                this.minecraft.debugRenderer.raidDebugRenderer.setRaidCenters(var38);
            } else if (ClientboundCustomPayloadPacket.DEBUG_BRAIN.equals(var0)) {
                double var40 = var1.readDouble();
                double var41 = var1.readDouble();
                double var42 = var1.readDouble();
                Position var43 = new PositionImpl(var40, var41, var42);
                UUID var44 = var1.readUUID();
                int var45 = var1.readInt();
                String var46 = var1.readUtf();
                String var47 = var1.readUtf();
                int var48 = var1.readInt();
                String var49 = var1.readUtf();
                boolean var50 = var1.readBoolean();
                Path var51;
                if (var50) {
                    var51 = Path.createFromStream(var1);
                } else {
                    var51 = null;
                }

                boolean var53 = var1.readBoolean();
                VillageDebugRenderer.BrainDump var54 = new VillageDebugRenderer.BrainDump(var44, var45, var46, var47, var48, var43, var49, var51, var53);
                int var55 = var1.readInt();

                for(int var56 = 0; var56 < var55; ++var56) {
                    String var57 = var1.readUtf();
                    var54.activities.add(var57);
                }

                int var58 = var1.readInt();

                for(int var59 = 0; var59 < var58; ++var59) {
                    String var60 = var1.readUtf();
                    var54.behaviors.add(var60);
                }

                int var61 = var1.readInt();

                for(int var62 = 0; var62 < var61; ++var62) {
                    String var63 = var1.readUtf();
                    var54.memories.add(var63);
                }

                int var64 = var1.readInt();

                for(int var65 = 0; var65 < var64; ++var65) {
                    BlockPos var66 = var1.readBlockPos();
                    var54.pois.add(var66);
                }

                int var67 = var1.readInt();

                for(int var68 = 0; var68 < var67; ++var68) {
                    String var69 = var1.readUtf();
                    var54.gossips.add(var69);
                }

                this.minecraft.debugRenderer.villageDebugRenderer.addOrUpdateBrainDump(var54);
            } else if (ClientboundCustomPayloadPacket.DEBUG_BEE.equals(var0)) {
                double var70 = var1.readDouble();
                double var71 = var1.readDouble();
                double var72 = var1.readDouble();
                Position var73 = new PositionImpl(var70, var71, var72);
                UUID var74 = var1.readUUID();
                int var75 = var1.readInt();
                BlockPos var76 = var1.readBlockPos();
                BlockPos var77 = var1.readBlockPos();
                boolean var78 = var1.readBoolean();
                Path var79;
                if (var78) {
                    var79 = Path.createFromStream(var1);
                } else {
                    var79 = null;
                }

                BeeDebugRenderer.BeeInfo var81 = new BeeDebugRenderer.BeeInfo(var74, var75, var73, var79, var76, var77);
                int var82 = var1.readInt();

                for(int var83 = 0; var83 < var82; ++var83) {
                    String var84 = var1.readUtf();
                    var81.goals.add(var84);
                }

                this.minecraft.debugRenderer.beeDebugRenderer.addOrUpdateBeeInfo(var81);
            } else if (ClientboundCustomPayloadPacket.DEBUG_HIVE.equals(var0)) {
                BlockPos var85 = var1.readBlockPos();
                String var86 = var1.readUtf();
                int var87 = var1.readInt();
                BeeDebugRenderer.HiveInfo var88 = new BeeDebugRenderer.HiveInfo(var85, var86, var87, this.level.getGameTime());
                this.minecraft.debugRenderer.beeDebugRenderer.addOrUpdateHiveInfo(var88);
            } else if (ClientboundCustomPayloadPacket.DEBUG_GAME_TEST_CLEAR.equals(var0)) {
                this.minecraft.debugRenderer.gameTestDebugRenderer.clear();
            } else if (ClientboundCustomPayloadPacket.DEBUG_GAME_TEST_ADD_MARKER.equals(var0)) {
                BlockPos var89 = var1.readBlockPos();
                int var90 = var1.readInt();
                String var91 = var1.readUtf();
                int var92 = var1.readInt();
                this.minecraft.debugRenderer.gameTestDebugRenderer.addMarker(var89, var90, var91, var92);
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
        PlayerTeam var1;
        if (param0.getMethod() == 0) {
            var1 = var0.addPlayerTeam(param0.getName());
        } else {
            var1 = var0.getPlayerTeam(param0.getName());
        }

        if (param0.getMethod() == 0 || param0.getMethod() == 2) {
            var1.setDisplayName(param0.getDisplayName());
            var1.setColor(param0.getColor());
            var1.unpackOptions(param0.getOptions());
            Team.Visibility var3 = Team.Visibility.byName(param0.getNametagVisibility());
            if (var3 != null) {
                var1.setNameTagVisibility(var3);
            }

            Team.CollisionRule var4 = Team.CollisionRule.byName(param0.getCollisionRule());
            if (var4 != null) {
                var1.setCollisionRule(var4);
            }

            var1.setPlayerPrefix(param0.getPlayerPrefix());
            var1.setPlayerSuffix(param0.getPlayerSuffix());
        }

        if (param0.getMethod() == 0 || param0.getMethod() == 3) {
            for(String var5 : param0.getPlayers()) {
                var0.addPlayerToTeam(var5, var1);
            }
        }

        if (param0.getMethod() == 4) {
            for(String var6 : param0.getPlayers()) {
                var0.removePlayerFromTeam(var6, var1);
            }
        }

        if (param0.getMethod() == 1) {
            var0.removePlayerTeam(var1);
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
    public void handleUpdateAttributes(ClientboundUpdateAttributesPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        Entity var0 = this.level.getEntity(param0.getEntityId());
        if (var0 != null) {
            if (!(var0 instanceof LivingEntity)) {
                throw new IllegalStateException("Server tried to update attributes of a non-living entity (actually: " + var0 + ")");
            } else {
                BaseAttributeMap var1 = ((LivingEntity)var0).getAttributes();

                for(ClientboundUpdateAttributesPacket.AttributeSnapshot var2 : param0.getValues()) {
                    AttributeInstance var3 = var1.getInstance(var2.getName());
                    if (var3 == null) {
                        var3 = var1.registerAttribute(new RangedAttribute(null, var2.getName(), 0.0, Double.MIN_NORMAL, Double.MAX_VALUE));
                    }

                    var3.setBaseValue(var2.getBase());
                    var3.removeModifiers();

                    for(AttributeModifier var4 : var2.getModifiers()) {
                        var3.addModifier(var4);
                    }
                }

            }
        }
    }

    @Override
    public void handlePlaceRecipe(ClientboundPlaceGhostRecipePacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        AbstractContainerMenu var0 = this.minecraft.player.containerMenu;
        if (var0.containerId == param0.getContainerId() && var0.isSynched(this.minecraft.player)) {
            this.recipeManager.byKey(param0.getRecipe()).ifPresent(param1 -> {
                if (this.minecraft.screen instanceof RecipeUpdateListener) {
                    RecipeBookComponent var0x = ((RecipeUpdateListener)this.minecraft.screen).getRecipeBookComponent();
                    var0x.setupGhostRecipe(param1, var0.slots);
                }

            });
        }
    }

    @Override
    public void handleLightUpdatePacked(ClientboundLightUpdatePacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        int var0 = param0.getX();
        int var1 = param0.getZ();
        LevelLightEngine var2 = this.level.getChunkSource().getLightEngine();
        int var3 = param0.getSkyYMask();
        int var4 = param0.getEmptySkyYMask();
        Iterator<byte[]> var5 = param0.getSkyUpdates().iterator();
        this.readSectionList(var0, var1, var2, LightLayer.SKY, var3, var4, var5);
        int var6 = param0.getBlockYMask();
        int var7 = param0.getEmptyBlockYMask();
        Iterator<byte[]> var8 = param0.getBlockUpdates().iterator();
        this.readSectionList(var0, var1, var2, LightLayer.BLOCK, var6, var7, var8);
    }

    @Override
    public void handleMerchantOffers(ClientboundMerchantOffersPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        AbstractContainerMenu var0 = this.minecraft.player.containerMenu;
        if (param0.getContainerId() == var0.containerId && var0 instanceof MerchantMenu) {
            ((MerchantMenu)var0).setOffers(new MerchantOffers(param0.getOffers().createTag()));
            ((MerchantMenu)var0).setXp(param0.getVillagerXp());
            ((MerchantMenu)var0).setMerchantLevel(param0.getVillagerLevel());
            ((MerchantMenu)var0).setShowProgressBar(param0.showProgress());
            ((MerchantMenu)var0).setCanRestock(param0.canRestock());
        }

    }

    @Override
    public void handleSetChunkCacheRadius(ClientboundSetChunkCacheRadiusPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.serverChunkRadius = param0.getRadius();
        this.level.getChunkSource().updateViewRadius(param0.getRadius());
    }

    @Override
    public void handleSetChunkCacheCenter(ClientboundSetChunkCacheCenterPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.level.getChunkSource().updateViewCenter(param0.getX(), param0.getZ());
    }

    @Override
    public void handleBlockBreakAck(ClientboundBlockBreakAckPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.minecraft.gameMode.handleBlockBreakAck(this.level, param0.getPos(), param0.getState(), param0.action(), param0.allGood());
    }

    private void readSectionList(int param0, int param1, LevelLightEngine param2, LightLayer param3, int param4, int param5, Iterator<byte[]> param6) {
        for(int var0 = 0; var0 < 18; ++var0) {
            int var1 = -1 + var0;
            boolean var2 = (param4 & 1 << var0) != 0;
            boolean var3 = (param5 & 1 << var0) != 0;
            if (var2 || var3) {
                param2.queueSectionData(param3, SectionPos.of(param0, var1, param1), var2 ? new DataLayer((byte[])param6.next().clone()) : new DataLayer());
                this.level.setSectionDirtyWithNeighbors(param0, var1, param1);
            }
        }

    }

    @Override
    public Connection getConnection() {
        return this.connection;
    }

    public Collection<PlayerInfo> getOnlinePlayers() {
        return this.playerInfoMap.values();
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

    public TagManager getTags() {
        return this.tags;
    }

    public DebugQueryHandler getDebugQueryHandler() {
        return this.debugQueryHandler;
    }

    public UUID getId() {
        return this.id;
    }
}
