package net.minecraft.server.level;

import com.google.common.net.InetAddresses;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.BlockUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundHorseScreenOpenPacket;
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatEndPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatEnterPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerLookAtPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundServerDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.TextFilter;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.warden.WardenSpawnTracker;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ComplexItem;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ServerItemCooldowns;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.slf4j.Logger;

public class ServerPlayer extends Player {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int NEUTRAL_MOB_DEATH_NOTIFICATION_RADII_XZ = 32;
    private static final int NEUTRAL_MOB_DEATH_NOTIFICATION_RADII_Y = 10;
    private static final int FLY_STAT_RECORDING_SPEED = 25;
    public ServerGamePacketListenerImpl connection;
    public final MinecraftServer server;
    public final ServerPlayerGameMode gameMode;
    private final PlayerAdvancements advancements;
    private final ServerStatsCounter stats;
    private float lastRecordedHealthAndAbsorption = Float.MIN_VALUE;
    private int lastRecordedFoodLevel = Integer.MIN_VALUE;
    private int lastRecordedAirLevel = Integer.MIN_VALUE;
    private int lastRecordedArmor = Integer.MIN_VALUE;
    private int lastRecordedLevel = Integer.MIN_VALUE;
    private int lastRecordedExperience = Integer.MIN_VALUE;
    private float lastSentHealth = -1.0E8F;
    private int lastSentFood = -99999999;
    private boolean lastFoodSaturationZero = true;
    private int lastSentExp = -99999999;
    private int spawnInvulnerableTime = 60;
    private ChatVisiblity chatVisibility = ChatVisiblity.FULL;
    private boolean canChatColor = true;
    private long lastActionTime = Util.getMillis();
    @Nullable
    private Entity camera;
    private boolean isChangingDimension;
    private boolean seenCredits;
    private final ServerRecipeBook recipeBook = new ServerRecipeBook();
    @Nullable
    private Vec3 levitationStartPos;
    private int levitationStartTime;
    private boolean disconnected;
    private int requestedViewDistance = 2;
    private String language = "en_us";
    @Nullable
    private Vec3 startingToFallPosition;
    @Nullable
    private Vec3 enteredNetherPosition;
    @Nullable
    private Vec3 enteredLavaOnVehiclePosition;
    private SectionPos lastSectionPos = SectionPos.of(0, 0, 0);
    private ChunkTrackingView chunkTrackingView = ChunkTrackingView.EMPTY;
    private ResourceKey<Level> respawnDimension = Level.OVERWORLD;
    @Nullable
    private BlockPos respawnPosition;
    private boolean respawnForced;
    private float respawnAngle;
    private final TextFilter textFilter;
    private boolean textFilteringEnabled;
    private boolean allowsListing;
    private WardenSpawnTracker wardenSpawnTracker = new WardenSpawnTracker(0, 0, 0);
    private final ContainerSynchronizer containerSynchronizer = new ContainerSynchronizer() {
        @Override
        public void sendInitialData(AbstractContainerMenu param0, NonNullList<ItemStack> param1, ItemStack param2, int[] param3) {
            ServerPlayer.this.connection.send(new ClientboundContainerSetContentPacket(param0.containerId, param0.incrementStateId(), param1, param2));

            for(int var0 = 0; var0 < param3.length; ++var0) {
                this.broadcastDataValue(param0, var0, param3[var0]);
            }

        }

        @Override
        public void sendSlotChange(AbstractContainerMenu param0, int param1, ItemStack param2) {
            ServerPlayer.this.connection.send(new ClientboundContainerSetSlotPacket(param0.containerId, param0.incrementStateId(), param1, param2));
        }

        @Override
        public void sendCarriedChange(AbstractContainerMenu param0, ItemStack param1) {
            ServerPlayer.this.connection.send(new ClientboundContainerSetSlotPacket(-1, param0.incrementStateId(), -1, param1));
        }

        @Override
        public void sendDataChange(AbstractContainerMenu param0, int param1, int param2) {
            this.broadcastDataValue(param0, param1, param2);
        }

        private void broadcastDataValue(AbstractContainerMenu param0, int param1, int param2) {
            ServerPlayer.this.connection.send(new ClientboundContainerSetDataPacket(param0.containerId, param1, param2));
        }
    };
    private final ContainerListener containerListener = new ContainerListener() {
        @Override
        public void slotChanged(AbstractContainerMenu param0, int param1, ItemStack param2) {
            Slot var0 = param0.getSlot(param1);
            if (!(var0 instanceof ResultSlot)) {
                if (var0.container == ServerPlayer.this.getInventory()) {
                    CriteriaTriggers.INVENTORY_CHANGED.trigger(ServerPlayer.this, ServerPlayer.this.getInventory(), param2);
                }

            }
        }

        @Override
        public void dataChanged(AbstractContainerMenu param0, int param1, int param2) {
        }
    };
    @Nullable
    private RemoteChatSession chatSession;
    private int containerCounter;
    public boolean wonGame;

    public ServerPlayer(MinecraftServer param0, ServerLevel param1, GameProfile param2, ClientInformation param3) {
        super(param1, param1.getSharedSpawnPos(), param1.getSharedSpawnAngle(), param2);
        this.textFilter = param0.createTextFilterForPlayer(this);
        this.gameMode = param0.createGameModeForPlayer(this);
        this.server = param0;
        this.stats = param0.getPlayerList().getPlayerStats(this);
        this.advancements = param0.getPlayerList().getPlayerAdvancements(this);
        this.setMaxUpStep(1.0F);
        this.fudgeSpawnLocation(param1);
        this.updateOptions(param3);
    }

    private void fudgeSpawnLocation(ServerLevel param0) {
        BlockPos var0 = param0.getSharedSpawnPos();
        if (param0.dimensionType().hasSkyLight() && param0.getServer().getWorldData().getGameType() != GameType.ADVENTURE) {
            int var1 = Math.max(0, this.server.getSpawnRadius(param0));
            int var2 = Mth.floor(param0.getWorldBorder().getDistanceToBorder((double)var0.getX(), (double)var0.getZ()));
            if (var2 < var1) {
                var1 = var2;
            }

            if (var2 <= 1) {
                var1 = 1;
            }

            long var3 = (long)(var1 * 2 + 1);
            long var4 = var3 * var3;
            int var5 = var4 > 2147483647L ? Integer.MAX_VALUE : (int)var4;
            int var6 = this.getCoprime(var5);
            int var7 = RandomSource.create().nextInt(var5);

            for(int var8 = 0; var8 < var5; ++var8) {
                int var9 = (var7 + var6 * var8) % var5;
                int var10 = var9 % (var1 * 2 + 1);
                int var11 = var9 / (var1 * 2 + 1);
                BlockPos var12 = PlayerRespawnLogic.getOverworldRespawnPos(param0, var0.getX() + var10 - var1, var0.getZ() + var11 - var1);
                if (var12 != null) {
                    this.moveTo(var12, 0.0F, 0.0F);
                    if (param0.noCollision(this)) {
                        break;
                    }
                }
            }
        } else {
            this.moveTo(var0, 0.0F, 0.0F);

            while(!param0.noCollision(this) && this.getY() < (double)(param0.getMaxBuildHeight() - 1)) {
                this.setPos(this.getX(), this.getY() + 1.0, this.getZ());
            }
        }

    }

    private int getCoprime(int param0) {
        return param0 <= 16 ? param0 - 1 : 17;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        if (param0.contains("warden_spawn_tracker", 10)) {
            WardenSpawnTracker.CODEC
                .parse(new Dynamic<>(NbtOps.INSTANCE, param0.get("warden_spawn_tracker")))
                .resultOrPartial(LOGGER::error)
                .ifPresent(param0x -> this.wardenSpawnTracker = param0x);
        }

        if (param0.contains("enteredNetherPosition", 10)) {
            CompoundTag var0 = param0.getCompound("enteredNetherPosition");
            this.enteredNetherPosition = new Vec3(var0.getDouble("x"), var0.getDouble("y"), var0.getDouble("z"));
        }

        this.seenCredits = param0.getBoolean("seenCredits");
        if (param0.contains("recipeBook", 10)) {
            this.recipeBook.fromNbt(param0.getCompound("recipeBook"), this.server.getRecipeManager());
        }

        if (this.isSleeping()) {
            this.stopSleeping();
        }

        if (param0.contains("SpawnX", 99) && param0.contains("SpawnY", 99) && param0.contains("SpawnZ", 99)) {
            this.respawnPosition = new BlockPos(param0.getInt("SpawnX"), param0.getInt("SpawnY"), param0.getInt("SpawnZ"));
            this.respawnForced = param0.getBoolean("SpawnForced");
            this.respawnAngle = param0.getFloat("SpawnAngle");
            if (param0.contains("SpawnDimension")) {
                this.respawnDimension = Level.RESOURCE_KEY_CODEC
                    .parse(NbtOps.INSTANCE, param0.get("SpawnDimension"))
                    .resultOrPartial(LOGGER::error)
                    .orElse(Level.OVERWORLD);
            }
        }

    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        WardenSpawnTracker.CODEC
            .encodeStart(NbtOps.INSTANCE, this.wardenSpawnTracker)
            .resultOrPartial(LOGGER::error)
            .ifPresent(param1 -> param0.put("warden_spawn_tracker", param1));
        this.storeGameTypes(param0);
        param0.putBoolean("seenCredits", this.seenCredits);
        if (this.enteredNetherPosition != null) {
            CompoundTag var0 = new CompoundTag();
            var0.putDouble("x", this.enteredNetherPosition.x);
            var0.putDouble("y", this.enteredNetherPosition.y);
            var0.putDouble("z", this.enteredNetherPosition.z);
            param0.put("enteredNetherPosition", var0);
        }

        Entity var1 = this.getRootVehicle();
        Entity var2 = this.getVehicle();
        if (var2 != null && var1 != this && var1.hasExactlyOnePlayerPassenger()) {
            CompoundTag var3 = new CompoundTag();
            CompoundTag var4 = new CompoundTag();
            var1.save(var4);
            var3.putUUID("Attach", var2.getUUID());
            var3.put("Entity", var4);
            param0.put("RootVehicle", var3);
        }

        param0.put("recipeBook", this.recipeBook.toNbt());
        param0.putString("Dimension", this.level().dimension().location().toString());
        if (this.respawnPosition != null) {
            param0.putInt("SpawnX", this.respawnPosition.getX());
            param0.putInt("SpawnY", this.respawnPosition.getY());
            param0.putInt("SpawnZ", this.respawnPosition.getZ());
            param0.putBoolean("SpawnForced", this.respawnForced);
            param0.putFloat("SpawnAngle", this.respawnAngle);
            ResourceLocation.CODEC
                .encodeStart(NbtOps.INSTANCE, this.respawnDimension.location())
                .resultOrPartial(LOGGER::error)
                .ifPresent(param1 -> param0.put("SpawnDimension", param1));
        }

    }

    public void setExperiencePoints(int param0) {
        float var0 = (float)this.getXpNeededForNextLevel();
        float var1 = (var0 - 1.0F) / var0;
        this.experienceProgress = Mth.clamp((float)param0 / var0, 0.0F, var1);
        this.lastSentExp = -1;
    }

    public void setExperienceLevels(int param0) {
        this.experienceLevel = param0;
        this.lastSentExp = -1;
    }

    @Override
    public void giveExperienceLevels(int param0) {
        super.giveExperienceLevels(param0);
        this.lastSentExp = -1;
    }

    @Override
    public void onEnchantmentPerformed(ItemStack param0, int param1) {
        super.onEnchantmentPerformed(param0, param1);
        this.lastSentExp = -1;
    }

    private void initMenu(AbstractContainerMenu param0) {
        param0.addSlotListener(this.containerListener);
        param0.setSynchronizer(this.containerSynchronizer);
    }

    public void initInventoryMenu() {
        this.initMenu(this.inventoryMenu);
    }

    @Override
    public void onEnterCombat() {
        super.onEnterCombat();
        this.connection.send(new ClientboundPlayerCombatEnterPacket());
    }

    @Override
    public void onLeaveCombat() {
        super.onLeaveCombat();
        this.connection.send(new ClientboundPlayerCombatEndPacket(this.getCombatTracker()));
    }

    @Override
    protected void onInsideBlock(BlockState param0) {
        CriteriaTriggers.ENTER_BLOCK.trigger(this, param0);
    }

    @Override
    protected ItemCooldowns createItemCooldowns() {
        return new ServerItemCooldowns(this);
    }

    @Override
    public void tick() {
        this.gameMode.tick();
        this.wardenSpawnTracker.tick();
        --this.spawnInvulnerableTime;
        if (this.invulnerableTime > 0) {
            --this.invulnerableTime;
        }

        this.containerMenu.broadcastChanges();
        if (!this.level().isClientSide && !this.containerMenu.stillValid(this)) {
            this.closeContainer();
            this.containerMenu = this.inventoryMenu;
        }

        Entity var0 = this.getCamera();
        if (var0 != this) {
            if (var0.isAlive()) {
                this.absMoveTo(var0.getX(), var0.getY(), var0.getZ(), var0.getYRot(), var0.getXRot());
                this.serverLevel().getChunkSource().move(this);
                if (this.wantsToStopRiding()) {
                    this.setCamera(this);
                }
            } else {
                this.setCamera(this);
            }
        }

        CriteriaTriggers.TICK.trigger(this);
        if (this.levitationStartPos != null) {
            CriteriaTriggers.LEVITATION.trigger(this, this.levitationStartPos, this.tickCount - this.levitationStartTime);
        }

        this.trackStartFallingPosition();
        this.trackEnteredOrExitedLavaOnVehicle();
        this.advancements.flushDirty(this);
    }

    public void doTick() {
        try {
            if (!this.isSpectator() || !this.touchingUnloadedChunk()) {
                super.tick();
            }

            for(int var0 = 0; var0 < this.getInventory().getContainerSize(); ++var0) {
                ItemStack var1 = this.getInventory().getItem(var0);
                if (var1.getItem().isComplex()) {
                    Packet<?> var2 = ((ComplexItem)var1.getItem()).getUpdatePacket(var1, this.level(), this);
                    if (var2 != null) {
                        this.connection.send(var2);
                    }
                }
            }

            if (this.getHealth() != this.lastSentHealth
                || this.lastSentFood != this.foodData.getFoodLevel()
                || this.foodData.getSaturationLevel() == 0.0F != this.lastFoodSaturationZero) {
                this.connection.send(new ClientboundSetHealthPacket(this.getHealth(), this.foodData.getFoodLevel(), this.foodData.getSaturationLevel()));
                this.lastSentHealth = this.getHealth();
                this.lastSentFood = this.foodData.getFoodLevel();
                this.lastFoodSaturationZero = this.foodData.getSaturationLevel() == 0.0F;
            }

            if (this.getHealth() + this.getAbsorptionAmount() != this.lastRecordedHealthAndAbsorption) {
                this.lastRecordedHealthAndAbsorption = this.getHealth() + this.getAbsorptionAmount();
                this.updateScoreForCriteria(ObjectiveCriteria.HEALTH, Mth.ceil(this.lastRecordedHealthAndAbsorption));
            }

            if (this.foodData.getFoodLevel() != this.lastRecordedFoodLevel) {
                this.lastRecordedFoodLevel = this.foodData.getFoodLevel();
                this.updateScoreForCriteria(ObjectiveCriteria.FOOD, Mth.ceil((float)this.lastRecordedFoodLevel));
            }

            if (this.getAirSupply() != this.lastRecordedAirLevel) {
                this.lastRecordedAirLevel = this.getAirSupply();
                this.updateScoreForCriteria(ObjectiveCriteria.AIR, Mth.ceil((float)this.lastRecordedAirLevel));
            }

            if (this.getArmorValue() != this.lastRecordedArmor) {
                this.lastRecordedArmor = this.getArmorValue();
                this.updateScoreForCriteria(ObjectiveCriteria.ARMOR, Mth.ceil((float)this.lastRecordedArmor));
            }

            if (this.totalExperience != this.lastRecordedExperience) {
                this.lastRecordedExperience = this.totalExperience;
                this.updateScoreForCriteria(ObjectiveCriteria.EXPERIENCE, Mth.ceil((float)this.lastRecordedExperience));
            }

            if (this.experienceLevel != this.lastRecordedLevel) {
                this.lastRecordedLevel = this.experienceLevel;
                this.updateScoreForCriteria(ObjectiveCriteria.LEVEL, Mth.ceil((float)this.lastRecordedLevel));
            }

            if (this.totalExperience != this.lastSentExp) {
                this.lastSentExp = this.totalExperience;
                this.connection.send(new ClientboundSetExperiencePacket(this.experienceProgress, this.totalExperience, this.experienceLevel));
            }

            if (this.tickCount % 20 == 0) {
                CriteriaTriggers.LOCATION.trigger(this);
            }

        } catch (Throwable var41) {
            CrashReport var4 = CrashReport.forThrowable(var41, "Ticking player");
            CrashReportCategory var5 = var4.addCategory("Player being ticked");
            this.fillCrashReportCategory(var5);
            throw new ReportedException(var4);
        }
    }

    @Override
    public void resetFallDistance() {
        if (this.getHealth() > 0.0F && this.startingToFallPosition != null) {
            CriteriaTriggers.FALL_FROM_HEIGHT.trigger(this, this.startingToFallPosition);
        }

        this.startingToFallPosition = null;
        super.resetFallDistance();
    }

    public void trackStartFallingPosition() {
        if (this.fallDistance > 0.0F && this.startingToFallPosition == null) {
            this.startingToFallPosition = this.position();
        }

    }

    public void trackEnteredOrExitedLavaOnVehicle() {
        if (this.getVehicle() != null && this.getVehicle().isInLava()) {
            if (this.enteredLavaOnVehiclePosition == null) {
                this.enteredLavaOnVehiclePosition = this.position();
            } else {
                CriteriaTriggers.RIDE_ENTITY_IN_LAVA_TRIGGER.trigger(this, this.enteredLavaOnVehiclePosition);
            }
        }

        if (this.enteredLavaOnVehiclePosition != null && (this.getVehicle() == null || !this.getVehicle().isInLava())) {
            this.enteredLavaOnVehiclePosition = null;
        }

    }

    private void updateScoreForCriteria(ObjectiveCriteria param0, int param1) {
        this.getScoreboard().forAllObjectives(param0, this, param1x -> param1x.set(param1));
    }

    @Override
    public void die(DamageSource param0) {
        this.gameEvent(GameEvent.ENTITY_DIE);
        boolean var0 = this.level().getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES);
        if (var0) {
            Component var1 = this.getCombatTracker().getDeathMessage();
            this.connection
                .send(
                    new ClientboundPlayerCombatKillPacket(this.getId(), var1),
                    PacketSendListener.exceptionallySend(
                        () -> {
                            int var0x = 256;
                            String var1x = var1.getString(256);
                            Component var2x = Component.translatable("death.attack.message_too_long", Component.literal(var1x).withStyle(ChatFormatting.YELLOW));
                            Component var3x = Component.translatable("death.attack.even_more_magic", this.getDisplayName())
                                .withStyle(param1 -> param1.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, var2x)));
                            return new ClientboundPlayerCombatKillPacket(this.getId(), var3x);
                        }
                    )
                );
            Team var2 = this.getTeam();
            if (var2 == null || var2.getDeathMessageVisibility() == Team.Visibility.ALWAYS) {
                this.server.getPlayerList().broadcastSystemMessage(var1, false);
            } else if (var2.getDeathMessageVisibility() == Team.Visibility.HIDE_FOR_OTHER_TEAMS) {
                this.server.getPlayerList().broadcastSystemToTeam(this, var1);
            } else if (var2.getDeathMessageVisibility() == Team.Visibility.HIDE_FOR_OWN_TEAM) {
                this.server.getPlayerList().broadcastSystemToAllExceptTeam(this, var1);
            }
        } else {
            this.connection.send(new ClientboundPlayerCombatKillPacket(this.getId(), CommonComponents.EMPTY));
        }

        this.removeEntitiesOnShoulder();
        if (this.level().getGameRules().getBoolean(GameRules.RULE_FORGIVE_DEAD_PLAYERS)) {
            this.tellNeutralMobsThatIDied();
        }

        if (!this.isSpectator()) {
            this.dropAllDeathLoot(param0);
        }

        this.getScoreboard().forAllObjectives(ObjectiveCriteria.DEATH_COUNT, this, ScoreAccess::increment);
        LivingEntity var3 = this.getKillCredit();
        if (var3 != null) {
            this.awardStat(Stats.ENTITY_KILLED_BY.get(var3.getType()));
            var3.awardKillScore(this, this.deathScore, param0);
            this.createWitherRose(var3);
        }

        this.level().broadcastEntityEvent(this, (byte)3);
        this.awardStat(Stats.DEATHS);
        this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_DEATH));
        this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
        this.clearFire();
        this.setTicksFrozen(0);
        this.setSharedFlagOnFire(false);
        this.getCombatTracker().recheckStatus();
        this.setLastDeathLocation(Optional.of(GlobalPos.of(this.level().dimension(), this.blockPosition())));
    }

    private void tellNeutralMobsThatIDied() {
        AABB var0 = new AABB(this.blockPosition()).inflate(32.0, 10.0, 32.0);
        this.level()
            .getEntitiesOfClass(Mob.class, var0, EntitySelector.NO_SPECTATORS)
            .stream()
            .filter(param0 -> param0 instanceof NeutralMob)
            .forEach(param0 -> ((NeutralMob)param0).playerDied(this));
    }

    @Override
    public void awardKillScore(Entity param0, int param1, DamageSource param2) {
        if (param0 != this) {
            super.awardKillScore(param0, param1, param2);
            this.increaseScore(param1);
            this.getScoreboard().forAllObjectives(ObjectiveCriteria.KILL_COUNT_ALL, this, ScoreAccess::increment);
            if (param0 instanceof Player) {
                this.awardStat(Stats.PLAYER_KILLS);
                this.getScoreboard().forAllObjectives(ObjectiveCriteria.KILL_COUNT_PLAYERS, this, ScoreAccess::increment);
            } else {
                this.awardStat(Stats.MOB_KILLS);
            }

            this.handleTeamKill(this, param0, ObjectiveCriteria.TEAM_KILL);
            this.handleTeamKill(param0, this, ObjectiveCriteria.KILLED_BY_TEAM);
            CriteriaTriggers.PLAYER_KILLED_ENTITY.trigger(this, param0, param2);
        }
    }

    private void handleTeamKill(ScoreHolder param0, ScoreHolder param1, ObjectiveCriteria[] param2) {
        PlayerTeam var0 = this.getScoreboard().getPlayersTeam(param1.getScoreboardName());
        if (var0 != null) {
            int var1 = var0.getColor().getId();
            if (var1 >= 0 && var1 < param2.length) {
                this.getScoreboard().forAllObjectives(param2[var1], param0, ScoreAccess::increment);
            }
        }

    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        if (this.isInvulnerableTo(param0)) {
            return false;
        } else {
            boolean var0 = this.server.isDedicatedServer() && this.isPvpAllowed() && param0.is(DamageTypeTags.IS_FALL);
            if (!var0 && this.spawnInvulnerableTime > 0 && !param0.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
                return false;
            } else {
                Entity var1 = param0.getEntity();
                if (var1 instanceof Player var2 && !this.canHarmPlayer(var2)) {
                    return false;
                }

                if (var1 instanceof AbstractArrow var3) {
                    Entity var4 = var3.getOwner();
                    if (var4 instanceof Player var5 && !this.canHarmPlayer(var5)) {
                        return false;
                    }
                }

                return super.hurt(param0, param1);
            }
        }
    }

    @Override
    public boolean canHarmPlayer(Player param0) {
        return !this.isPvpAllowed() ? false : super.canHarmPlayer(param0);
    }

    private boolean isPvpAllowed() {
        return this.server.isPvpAllowed();
    }

    @Nullable
    @Override
    protected PortalInfo findDimensionEntryPoint(ServerLevel param0) {
        PortalInfo var0 = super.findDimensionEntryPoint(param0);
        if (var0 != null && this.level().dimension() == Level.OVERWORLD && param0.dimension() == Level.END) {
            Vec3 var1 = var0.pos.add(0.0, -1.0, 0.0);
            return new PortalInfo(var1, Vec3.ZERO, 90.0F, 0.0F);
        } else {
            return var0;
        }
    }

    @Nullable
    @Override
    public Entity changeDimension(ServerLevel param0) {
        this.isChangingDimension = true;
        ServerLevel var0 = this.serverLevel();
        ResourceKey<Level> var1 = var0.dimension();
        if (var1 == Level.END && param0.dimension() == Level.OVERWORLD) {
            this.unRide();
            this.serverLevel().removePlayerImmediately(this, Entity.RemovalReason.CHANGED_DIMENSION);
            if (!this.wonGame) {
                this.wonGame = true;
                this.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.WIN_GAME, this.seenCredits ? 0.0F : 1.0F));
                this.seenCredits = true;
            }

            return this;
        } else {
            LevelData var2 = param0.getLevelData();
            this.connection.send(new ClientboundRespawnPacket(this.createCommonSpawnInfo(param0), (byte)3));
            this.connection.send(new ClientboundChangeDifficultyPacket(var2.getDifficulty(), var2.isDifficultyLocked()));
            PlayerList var3 = this.server.getPlayerList();
            var3.sendPlayerPermissionLevel(this);
            var0.removePlayerImmediately(this, Entity.RemovalReason.CHANGED_DIMENSION);
            this.unsetRemoved();
            PortalInfo var4 = this.findDimensionEntryPoint(param0);
            if (var4 != null) {
                var0.getProfiler().push("moving");
                if (var1 == Level.OVERWORLD && param0.dimension() == Level.NETHER) {
                    this.enteredNetherPosition = this.position();
                } else if (param0.dimension() == Level.END) {
                    this.createEndPlatform(param0, BlockPos.containing(var4.pos));
                }

                var0.getProfiler().pop();
                var0.getProfiler().push("placing");
                this.setServerLevel(param0);
                this.connection.teleport(var4.pos.x, var4.pos.y, var4.pos.z, var4.yRot, var4.xRot);
                this.connection.resetPosition();
                param0.addDuringPortalTeleport(this);
                var0.getProfiler().pop();
                this.triggerDimensionChangeTriggers(var0);
                this.connection.send(new ClientboundPlayerAbilitiesPacket(this.getAbilities()));
                var3.sendLevelInfo(this, param0);
                var3.sendAllPlayerInfo(this);

                for(MobEffectInstance var5 : this.getActiveEffects()) {
                    this.connection.send(new ClientboundUpdateMobEffectPacket(this.getId(), var5));
                }

                this.connection.send(new ClientboundLevelEventPacket(1032, BlockPos.ZERO, 0, false));
                this.lastSentExp = -1;
                this.lastSentHealth = -1.0F;
                this.lastSentFood = -1;
            }

            return this;
        }
    }

    private void createEndPlatform(ServerLevel param0, BlockPos param1) {
        BlockPos.MutableBlockPos var0 = param1.mutable();

        for(int var1 = -2; var1 <= 2; ++var1) {
            for(int var2 = -2; var2 <= 2; ++var2) {
                for(int var3 = -1; var3 < 3; ++var3) {
                    BlockState var4 = var3 == -1 ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.AIR.defaultBlockState();
                    param0.setBlockAndUpdate(var0.set(param1).move(var2, var3, var1), var4);
                }
            }
        }

    }

    @Override
    protected Optional<BlockUtil.FoundRectangle> getExitPortal(ServerLevel param0, BlockPos param1, boolean param2, WorldBorder param3) {
        Optional<BlockUtil.FoundRectangle> var0 = super.getExitPortal(param0, param1, param2, param3);
        if (var0.isPresent()) {
            return var0;
        } else {
            Direction.Axis var1 = this.level().getBlockState(this.portalEntrancePos).getOptionalValue(NetherPortalBlock.AXIS).orElse(Direction.Axis.X);
            Optional<BlockUtil.FoundRectangle> var2 = param0.getPortalForcer().createPortal(param1, var1);
            if (var2.isEmpty()) {
                LOGGER.error("Unable to create a portal, likely target out of worldborder");
            }

            return var2;
        }
    }

    private void triggerDimensionChangeTriggers(ServerLevel param0) {
        ResourceKey<Level> var0 = param0.dimension();
        ResourceKey<Level> var1 = this.level().dimension();
        CriteriaTriggers.CHANGED_DIMENSION.trigger(this, var0, var1);
        if (var0 == Level.NETHER && var1 == Level.OVERWORLD && this.enteredNetherPosition != null) {
            CriteriaTriggers.NETHER_TRAVEL.trigger(this, this.enteredNetherPosition);
        }

        if (var1 != Level.NETHER) {
            this.enteredNetherPosition = null;
        }

    }

    @Override
    public boolean broadcastToPlayer(ServerPlayer param0) {
        if (param0.isSpectator()) {
            return this.getCamera() == this;
        } else {
            return this.isSpectator() ? false : super.broadcastToPlayer(param0);
        }
    }

    @Override
    public void take(Entity param0, int param1) {
        super.take(param0, param1);
        this.containerMenu.broadcastChanges();
    }

    @Override
    public Either<Player.BedSleepingProblem, Unit> startSleepInBed(BlockPos param0) {
        Direction var0 = this.level().getBlockState(param0).getValue(HorizontalDirectionalBlock.FACING);
        if (this.isSleeping() || !this.isAlive()) {
            return Either.left(Player.BedSleepingProblem.OTHER_PROBLEM);
        } else if (!this.level().dimensionType().natural()) {
            return Either.left(Player.BedSleepingProblem.NOT_POSSIBLE_HERE);
        } else if (!this.bedInRange(param0, var0)) {
            return Either.left(Player.BedSleepingProblem.TOO_FAR_AWAY);
        } else if (this.bedBlocked(param0, var0)) {
            return Either.left(Player.BedSleepingProblem.OBSTRUCTED);
        } else {
            this.setRespawnPosition(this.level().dimension(), param0, this.getYRot(), false, true);
            if (this.level().isDay()) {
                return Either.left(Player.BedSleepingProblem.NOT_POSSIBLE_NOW);
            } else {
                if (!this.isCreative()) {
                    double var1 = 8.0;
                    double var2 = 5.0;
                    Vec3 var3 = Vec3.atBottomCenterOf(param0);
                    List<Monster> var4 = this.level()
                        .getEntitiesOfClass(
                            Monster.class,
                            new AABB(var3.x() - 8.0, var3.y() - 5.0, var3.z() - 8.0, var3.x() + 8.0, var3.y() + 5.0, var3.z() + 8.0),
                            param0x -> param0x.isPreventingPlayerRest(this)
                        );
                    if (!var4.isEmpty()) {
                        return Either.left(Player.BedSleepingProblem.NOT_SAFE);
                    }
                }

                Either<Player.BedSleepingProblem, Unit> var5 = super.startSleepInBed(param0).ifRight(param0x -> {
                    this.awardStat(Stats.SLEEP_IN_BED);
                    CriteriaTriggers.SLEPT_IN_BED.trigger(this);
                });
                if (!this.serverLevel().canSleepThroughNights()) {
                    this.displayClientMessage(Component.translatable("sleep.not_possible"), true);
                }

                ((ServerLevel)this.level()).updateSleepingPlayerList();
                return var5;
            }
        }
    }

    @Override
    public void startSleeping(BlockPos param0) {
        this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
        super.startSleeping(param0);
    }

    private boolean bedInRange(BlockPos param0, Direction param1) {
        return this.isReachableBedBlock(param0) || this.isReachableBedBlock(param0.relative(param1.getOpposite()));
    }

    private boolean isReachableBedBlock(BlockPos param0) {
        Vec3 var0 = Vec3.atBottomCenterOf(param0);
        return Math.abs(this.getX() - var0.x()) <= 3.0 && Math.abs(this.getY() - var0.y()) <= 2.0 && Math.abs(this.getZ() - var0.z()) <= 3.0;
    }

    private boolean bedBlocked(BlockPos param0, Direction param1) {
        BlockPos var0 = param0.above();
        return !this.freeAt(var0) || !this.freeAt(var0.relative(param1.getOpposite()));
    }

    @Override
    public void stopSleepInBed(boolean param0, boolean param1) {
        if (this.isSleeping()) {
            this.serverLevel().getChunkSource().broadcastAndSend(this, new ClientboundAnimatePacket(this, 2));
        }

        super.stopSleepInBed(param0, param1);
        if (this.connection != null) {
            this.connection.teleport(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
        }

    }

    @Override
    public void dismountTo(double param0, double param1, double param2) {
        this.removeVehicle();
        this.setPos(param0, param1, param2);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource param0) {
        return super.isInvulnerableTo(param0) || this.isChangingDimension();
    }

    @Override
    protected void checkFallDamage(double param0, boolean param1, BlockState param2, BlockPos param3) {
    }

    @Override
    protected void onChangedBlock(BlockPos param0) {
        if (!this.isSpectator()) {
            super.onChangedBlock(param0);
        }

    }

    public void doCheckFallDamage(double param0, double param1, double param2, boolean param3) {
        if (!this.touchingUnloadedChunk()) {
            this.checkSupportingBlock(param3, new Vec3(param0, param1, param2));
            BlockPos var0 = this.getOnPosLegacy();
            super.checkFallDamage(param1, param3, this.level().getBlockState(var0), var0);
        }
    }

    @Override
    protected void pushEntities() {
        if (this.level().tickRateManager().runsNormally()) {
            super.pushEntities();
        }

    }

    @Override
    public void openTextEdit(SignBlockEntity param0, boolean param1) {
        this.connection.send(new ClientboundBlockUpdatePacket(this.level(), param0.getBlockPos()));
        this.connection.send(new ClientboundOpenSignEditorPacket(param0.getBlockPos(), param1));
    }

    private void nextContainerCounter() {
        this.containerCounter = this.containerCounter % 100 + 1;
    }

    @Override
    public OptionalInt openMenu(@Nullable MenuProvider param0) {
        if (param0 == null) {
            return OptionalInt.empty();
        } else {
            if (this.containerMenu != this.inventoryMenu) {
                this.closeContainer();
            }

            this.nextContainerCounter();
            AbstractContainerMenu var0 = param0.createMenu(this.containerCounter, this.getInventory(), this);
            if (var0 == null) {
                if (this.isSpectator()) {
                    this.displayClientMessage(Component.translatable("container.spectatorCantOpen").withStyle(ChatFormatting.RED), true);
                }

                return OptionalInt.empty();
            } else {
                this.connection.send(new ClientboundOpenScreenPacket(var0.containerId, var0.getType(), param0.getDisplayName()));
                this.initMenu(var0);
                this.containerMenu = var0;
                return OptionalInt.of(this.containerCounter);
            }
        }
    }

    @Override
    public void sendMerchantOffers(int param0, MerchantOffers param1, int param2, int param3, boolean param4, boolean param5) {
        this.connection.send(new ClientboundMerchantOffersPacket(param0, param1, param2, param3, param4, param5));
    }

    @Override
    public void openHorseInventory(AbstractHorse param0, Container param1) {
        if (this.containerMenu != this.inventoryMenu) {
            this.closeContainer();
        }

        this.nextContainerCounter();
        this.connection.send(new ClientboundHorseScreenOpenPacket(this.containerCounter, param1.getContainerSize(), param0.getId()));
        this.containerMenu = new HorseInventoryMenu(this.containerCounter, this.getInventory(), param1, param0);
        this.initMenu(this.containerMenu);
    }

    @Override
    public void openItemGui(ItemStack param0, InteractionHand param1) {
        if (param0.is(Items.WRITTEN_BOOK)) {
            if (WrittenBookItem.resolveBookComponents(param0, this.createCommandSourceStack(), this)) {
                this.containerMenu.broadcastChanges();
            }

            this.connection.send(new ClientboundOpenBookPacket(param1));
        }

    }

    @Override
    public void openCommandBlock(CommandBlockEntity param0) {
        this.connection.send(ClientboundBlockEntityDataPacket.create(param0, BlockEntity::saveWithoutMetadata));
    }

    @Override
    public void closeContainer() {
        this.connection.send(new ClientboundContainerClosePacket(this.containerMenu.containerId));
        this.doCloseContainer();
    }

    @Override
    public void doCloseContainer() {
        this.containerMenu.removed(this);
        this.inventoryMenu.transferState(this.containerMenu);
        this.containerMenu = this.inventoryMenu;
    }

    public void setPlayerInput(float param0, float param1, boolean param2, boolean param3) {
        if (this.isPassenger()) {
            if (param0 >= -1.0F && param0 <= 1.0F) {
                this.xxa = param0;
            }

            if (param1 >= -1.0F && param1 <= 1.0F) {
                this.zza = param1;
            }

            this.jumping = param2;
            this.setShiftKeyDown(param3);
        }

    }

    @Override
    public void travel(Vec3 param0) {
        double var0 = this.getX();
        double var1 = this.getY();
        double var2 = this.getZ();
        super.travel(param0);
        this.checkMovementStatistics(this.getX() - var0, this.getY() - var1, this.getZ() - var2);
    }

    @Override
    public void rideTick() {
        double var0 = this.getX();
        double var1 = this.getY();
        double var2 = this.getZ();
        super.rideTick();
        this.checkRidingStatistics(this.getX() - var0, this.getY() - var1, this.getZ() - var2);
    }

    public void checkMovementStatistics(double param0, double param1, double param2) {
        if (!this.isPassenger() && !didNotMove(param0, param1, param2)) {
            if (this.isSwimming()) {
                int var0 = Math.round((float)Math.sqrt(param0 * param0 + param1 * param1 + param2 * param2) * 100.0F);
                if (var0 > 0) {
                    this.awardStat(Stats.SWIM_ONE_CM, var0);
                    this.causeFoodExhaustion(0.01F * (float)var0 * 0.01F);
                }
            } else if (this.isEyeInFluid(FluidTags.WATER)) {
                int var1 = Math.round((float)Math.sqrt(param0 * param0 + param1 * param1 + param2 * param2) * 100.0F);
                if (var1 > 0) {
                    this.awardStat(Stats.WALK_UNDER_WATER_ONE_CM, var1);
                    this.causeFoodExhaustion(0.01F * (float)var1 * 0.01F);
                }
            } else if (this.isInWater()) {
                int var2 = Math.round((float)Math.sqrt(param0 * param0 + param2 * param2) * 100.0F);
                if (var2 > 0) {
                    this.awardStat(Stats.WALK_ON_WATER_ONE_CM, var2);
                    this.causeFoodExhaustion(0.01F * (float)var2 * 0.01F);
                }
            } else if (this.onClimbable()) {
                if (param1 > 0.0) {
                    this.awardStat(Stats.CLIMB_ONE_CM, (int)Math.round(param1 * 100.0));
                }
            } else if (this.onGround()) {
                int var3 = Math.round((float)Math.sqrt(param0 * param0 + param2 * param2) * 100.0F);
                if (var3 > 0) {
                    if (this.isSprinting()) {
                        this.awardStat(Stats.SPRINT_ONE_CM, var3);
                        this.causeFoodExhaustion(0.1F * (float)var3 * 0.01F);
                    } else if (this.isCrouching()) {
                        this.awardStat(Stats.CROUCH_ONE_CM, var3);
                        this.causeFoodExhaustion(0.0F * (float)var3 * 0.01F);
                    } else {
                        this.awardStat(Stats.WALK_ONE_CM, var3);
                        this.causeFoodExhaustion(0.0F * (float)var3 * 0.01F);
                    }
                }
            } else if (this.isFallFlying()) {
                int var4 = Math.round((float)Math.sqrt(param0 * param0 + param1 * param1 + param2 * param2) * 100.0F);
                this.awardStat(Stats.AVIATE_ONE_CM, var4);
            } else {
                int var5 = Math.round((float)Math.sqrt(param0 * param0 + param2 * param2) * 100.0F);
                if (var5 > 25) {
                    this.awardStat(Stats.FLY_ONE_CM, var5);
                }
            }

        }
    }

    private void checkRidingStatistics(double param0, double param1, double param2) {
        if (this.isPassenger() && !didNotMove(param0, param1, param2)) {
            int var0 = Math.round((float)Math.sqrt(param0 * param0 + param1 * param1 + param2 * param2) * 100.0F);
            Entity var1 = this.getVehicle();
            if (var1 instanceof AbstractMinecart) {
                this.awardStat(Stats.MINECART_ONE_CM, var0);
            } else if (var1 instanceof Boat) {
                this.awardStat(Stats.BOAT_ONE_CM, var0);
            } else if (var1 instanceof Pig) {
                this.awardStat(Stats.PIG_ONE_CM, var0);
            } else if (var1 instanceof AbstractHorse) {
                this.awardStat(Stats.HORSE_ONE_CM, var0);
            } else if (var1 instanceof Strider) {
                this.awardStat(Stats.STRIDER_ONE_CM, var0);
            }

        }
    }

    private static boolean didNotMove(double param0, double param1, double param2) {
        return param0 == 0.0 && param1 == 0.0 && param2 == 0.0;
    }

    @Override
    public void awardStat(Stat<?> param0, int param1) {
        this.stats.increment(this, param0, param1);
        this.getScoreboard().forAllObjectives(param0, this, param1x -> param1x.add(param1));
    }

    @Override
    public void resetStat(Stat<?> param0) {
        this.stats.setValue(this, param0, 0);
        this.getScoreboard().forAllObjectives(param0, this, ScoreAccess::reset);
    }

    @Override
    public int awardRecipes(Collection<RecipeHolder<?>> param0) {
        return this.recipeBook.addRecipes(param0, this);
    }

    @Override
    public void triggerRecipeCrafted(RecipeHolder<?> param0, List<ItemStack> param1) {
        CriteriaTriggers.RECIPE_CRAFTED.trigger(this, param0.id(), param1);
    }

    @Override
    public void awardRecipesByKey(List<ResourceLocation> param0) {
        List<RecipeHolder<?>> var0 = param0.stream().flatMap(param0x -> this.server.getRecipeManager().byKey(param0x).stream()).collect(Collectors.toList());
        this.awardRecipes(var0);
    }

    @Override
    public int resetRecipes(Collection<RecipeHolder<?>> param0) {
        return this.recipeBook.removeRecipes(param0, this);
    }

    @Override
    public void giveExperiencePoints(int param0) {
        super.giveExperiencePoints(param0);
        this.lastSentExp = -1;
    }

    public void disconnect() {
        this.disconnected = true;
        this.ejectPassengers();
        if (this.isSleeping()) {
            this.stopSleepInBed(true, false);
        }

    }

    public boolean hasDisconnected() {
        return this.disconnected;
    }

    public void resetSentInfo() {
        this.lastSentHealth = -1.0E8F;
    }

    @Override
    public void displayClientMessage(Component param0, boolean param1) {
        this.sendSystemMessage(param0, param1);
    }

    @Override
    protected void completeUsingItem() {
        if (!this.useItem.isEmpty() && this.isUsingItem()) {
            this.connection.send(new ClientboundEntityEventPacket(this, (byte)9));
            super.completeUsingItem();
        }

    }

    @Override
    public void lookAt(EntityAnchorArgument.Anchor param0, Vec3 param1) {
        super.lookAt(param0, param1);
        this.connection.send(new ClientboundPlayerLookAtPacket(param0, param1.x, param1.y, param1.z));
    }

    public void lookAt(EntityAnchorArgument.Anchor param0, Entity param1, EntityAnchorArgument.Anchor param2) {
        Vec3 var0 = param2.apply(param1);
        super.lookAt(param0, var0);
        this.connection.send(new ClientboundPlayerLookAtPacket(param0, param1, param2));
    }

    public void restoreFrom(ServerPlayer param0, boolean param1) {
        this.wardenSpawnTracker = param0.wardenSpawnTracker;
        this.chatSession = param0.chatSession;
        this.gameMode.setGameModeForPlayer(param0.gameMode.getGameModeForPlayer(), param0.gameMode.getPreviousGameModeForPlayer());
        this.onUpdateAbilities();
        if (param1) {
            this.getInventory().replaceWith(param0.getInventory());
            this.setHealth(param0.getHealth());
            this.foodData = param0.foodData;
            this.experienceLevel = param0.experienceLevel;
            this.totalExperience = param0.totalExperience;
            this.experienceProgress = param0.experienceProgress;
            this.setScore(param0.getScore());
            this.portalEntrancePos = param0.portalEntrancePos;
        } else if (this.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) || param0.isSpectator()) {
            this.getInventory().replaceWith(param0.getInventory());
            this.experienceLevel = param0.experienceLevel;
            this.totalExperience = param0.totalExperience;
            this.experienceProgress = param0.experienceProgress;
            this.setScore(param0.getScore());
        }

        this.enchantmentSeed = param0.enchantmentSeed;
        this.enderChestInventory = param0.enderChestInventory;
        this.getEntityData().set(DATA_PLAYER_MODE_CUSTOMISATION, param0.getEntityData().get(DATA_PLAYER_MODE_CUSTOMISATION));
        this.lastSentExp = -1;
        this.lastSentHealth = -1.0F;
        this.lastSentFood = -1;
        this.recipeBook.copyOverData(param0.recipeBook);
        this.seenCredits = param0.seenCredits;
        this.enteredNetherPosition = param0.enteredNetherPosition;
        this.chunkTrackingView = param0.chunkTrackingView;
        this.setShoulderEntityLeft(param0.getShoulderEntityLeft());
        this.setShoulderEntityRight(param0.getShoulderEntityRight());
        this.setLastDeathLocation(param0.getLastDeathLocation());
    }

    @Override
    protected void onEffectAdded(MobEffectInstance param0, @Nullable Entity param1) {
        super.onEffectAdded(param0, param1);
        this.connection.send(new ClientboundUpdateMobEffectPacket(this.getId(), param0));
        if (param0.getEffect() == MobEffects.LEVITATION) {
            this.levitationStartTime = this.tickCount;
            this.levitationStartPos = this.position();
        }

        CriteriaTriggers.EFFECTS_CHANGED.trigger(this, param1);
    }

    @Override
    protected void onEffectUpdated(MobEffectInstance param0, boolean param1, @Nullable Entity param2) {
        super.onEffectUpdated(param0, param1, param2);
        this.connection.send(new ClientboundUpdateMobEffectPacket(this.getId(), param0));
        CriteriaTriggers.EFFECTS_CHANGED.trigger(this, param2);
    }

    @Override
    protected void onEffectRemoved(MobEffectInstance param0) {
        super.onEffectRemoved(param0);
        this.connection.send(new ClientboundRemoveMobEffectPacket(this.getId(), param0.getEffect()));
        if (param0.getEffect() == MobEffects.LEVITATION) {
            this.levitationStartPos = null;
        }

        CriteriaTriggers.EFFECTS_CHANGED.trigger(this, null);
    }

    @Override
    public void teleportTo(double param0, double param1, double param2) {
        this.connection.teleport(param0, param1, param2, this.getYRot(), this.getXRot(), RelativeMovement.ROTATION);
    }

    @Override
    public void teleportRelative(double param0, double param1, double param2) {
        this.connection.teleport(this.getX() + param0, this.getY() + param1, this.getZ() + param2, this.getYRot(), this.getXRot(), RelativeMovement.ALL);
    }

    @Override
    public boolean teleportTo(ServerLevel param0, double param1, double param2, double param3, Set<RelativeMovement> param4, float param5, float param6) {
        ChunkPos var0 = new ChunkPos(BlockPos.containing(param1, param2, param3));
        param0.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, var0, 1, this.getId());
        this.stopRiding();
        if (this.isSleeping()) {
            this.stopSleepInBed(true, true);
        }

        if (param0 == this.level()) {
            this.connection.teleport(param1, param2, param3, param5, param6, param4);
        } else {
            this.teleportTo(param0, param1, param2, param3, param5, param6);
        }

        this.setYHeadRot(param5);
        return true;
    }

    @Override
    public void moveTo(double param0, double param1, double param2) {
        super.moveTo(param0, param1, param2);
        this.connection.resetPosition();
    }

    @Override
    public void crit(Entity param0) {
        this.serverLevel().getChunkSource().broadcastAndSend(this, new ClientboundAnimatePacket(param0, 4));
    }

    @Override
    public void magicCrit(Entity param0) {
        this.serverLevel().getChunkSource().broadcastAndSend(this, new ClientboundAnimatePacket(param0, 5));
    }

    @Override
    public void onUpdateAbilities() {
        if (this.connection != null) {
            this.connection.send(new ClientboundPlayerAbilitiesPacket(this.getAbilities()));
            this.updateInvisibilityStatus();
        }
    }

    public ServerLevel serverLevel() {
        return (ServerLevel)this.level();
    }

    public boolean setGameMode(GameType param0) {
        if (!this.gameMode.changeGameModeForPlayer(param0)) {
            return false;
        } else {
            this.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, (float)param0.getId()));
            if (param0 == GameType.SPECTATOR) {
                this.removeEntitiesOnShoulder();
                this.stopRiding();
            } else {
                this.setCamera(this);
            }

            this.onUpdateAbilities();
            this.updateEffectVisibility();
            return true;
        }
    }

    @Override
    public boolean isSpectator() {
        return this.gameMode.getGameModeForPlayer() == GameType.SPECTATOR;
    }

    @Override
    public boolean isCreative() {
        return this.gameMode.getGameModeForPlayer() == GameType.CREATIVE;
    }

    @Override
    public void sendSystemMessage(Component param0) {
        this.sendSystemMessage(param0, false);
    }

    public void sendSystemMessage(Component param0, boolean param1) {
        if (this.acceptsSystemMessages(param1)) {
            this.connection
                .send(
                    new ClientboundSystemChatPacket(param0, param1),
                    PacketSendListener.exceptionallySend(
                        () -> {
                            if (this.acceptsSystemMessages(false)) {
                                int var0 = 256;
                                String var1x = param0.getString(256);
                                Component var2x = Component.literal(var1x).withStyle(ChatFormatting.YELLOW);
                                return new ClientboundSystemChatPacket(
                                    Component.translatable("multiplayer.message_not_delivered", var2x).withStyle(ChatFormatting.RED), false
                                );
                            } else {
                                return null;
                            }
                        }
                    )
                );
        }
    }

    public void sendChatMessage(OutgoingChatMessage param0, boolean param1, ChatType.Bound param2) {
        if (this.acceptsChatMessages()) {
            param0.sendToPlayer(this, param1, param2);
        }

    }

    public String getIpAddress() {
        SocketAddress var0 = this.connection.getRemoteAddress();
        return var0 instanceof InetSocketAddress var1 ? InetAddresses.toAddrString(var1.getAddress()) : "<unknown>";
    }

    public void updateOptions(ClientInformation param0) {
        this.language = param0.language();
        this.requestedViewDistance = param0.viewDistance();
        this.chatVisibility = param0.chatVisibility();
        this.canChatColor = param0.chatColors();
        this.textFilteringEnabled = param0.textFilteringEnabled();
        this.allowsListing = param0.allowsListing();
        this.getEntityData().set(DATA_PLAYER_MODE_CUSTOMISATION, (byte)param0.modelCustomisation());
        this.getEntityData().set(DATA_PLAYER_MAIN_HAND, (byte)param0.mainHand().getId());
    }

    public ClientInformation clientInformation() {
        int var0 = this.getEntityData().get(DATA_PLAYER_MODE_CUSTOMISATION);
        HumanoidArm var1 = HumanoidArm.BY_ID.apply(this.getEntityData().get(DATA_PLAYER_MAIN_HAND));
        return new ClientInformation(
            this.language, this.requestedViewDistance, this.chatVisibility, this.canChatColor, var0, var1, this.textFilteringEnabled, this.allowsListing
        );
    }

    public boolean canChatInColor() {
        return this.canChatColor;
    }

    public ChatVisiblity getChatVisibility() {
        return this.chatVisibility;
    }

    private boolean acceptsSystemMessages(boolean param0) {
        return this.chatVisibility == ChatVisiblity.HIDDEN ? param0 : true;
    }

    private boolean acceptsChatMessages() {
        return this.chatVisibility == ChatVisiblity.FULL;
    }

    public int requestedViewDistance() {
        return this.requestedViewDistance;
    }

    public void sendServerStatus(ServerStatus param0) {
        this.connection
            .send(new ClientboundServerDataPacket(param0.description(), param0.favicon().map(ServerStatus.Favicon::iconBytes), param0.enforcesSecureChat()));
    }

    @Override
    protected int getPermissionLevel() {
        return this.server.getProfilePermissions(this.getGameProfile());
    }

    public void resetLastActionTime() {
        this.lastActionTime = Util.getMillis();
    }

    public ServerStatsCounter getStats() {
        return this.stats;
    }

    public ServerRecipeBook getRecipeBook() {
        return this.recipeBook;
    }

    @Override
    protected void updateInvisibilityStatus() {
        if (this.isSpectator()) {
            this.removeEffectParticles();
            this.setInvisible(true);
        } else {
            super.updateInvisibilityStatus();
        }

    }

    public Entity getCamera() {
        return (Entity)(this.camera == null ? this : this.camera);
    }

    public void setCamera(@Nullable Entity param0) {
        Entity var0 = this.getCamera();
        this.camera = (Entity)(param0 == null ? this : param0);
        if (var0 != this.camera) {
            Level var4 = this.camera.level();
            if (var4 instanceof ServerLevel var1) {
                this.teleportTo(var1, this.camera.getX(), this.camera.getY(), this.camera.getZ(), Set.of(), this.getYRot(), this.getXRot());
            }

            if (param0 != null) {
                this.serverLevel().getChunkSource().move(this);
            }

            this.connection.send(new ClientboundSetCameraPacket(this.camera));
            this.connection.resetPosition();
        }

    }

    @Override
    protected void processPortalCooldown() {
        if (!this.isChangingDimension) {
            super.processPortalCooldown();
        }

    }

    @Override
    public void attack(Entity param0) {
        if (this.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
            this.setCamera(param0);
        } else {
            super.attack(param0);
        }

    }

    public long getLastActionTime() {
        return this.lastActionTime;
    }

    @Nullable
    public Component getTabListDisplayName() {
        return null;
    }

    @Override
    public void swing(InteractionHand param0) {
        super.swing(param0);
        this.resetAttackStrengthTicker();
    }

    public boolean isChangingDimension() {
        return this.isChangingDimension;
    }

    public void hasChangedDimension() {
        this.isChangingDimension = false;
    }

    public PlayerAdvancements getAdvancements() {
        return this.advancements;
    }

    public void teleportTo(ServerLevel param0, double param1, double param2, double param3, float param4, float param5) {
        this.setCamera(this);
        this.stopRiding();
        if (param0 == this.level()) {
            this.connection.teleport(param1, param2, param3, param4, param5);
        } else {
            ServerLevel var0 = this.serverLevel();
            LevelData var1 = param0.getLevelData();
            this.connection.send(new ClientboundRespawnPacket(this.createCommonSpawnInfo(param0), (byte)3));
            this.connection.send(new ClientboundChangeDifficultyPacket(var1.getDifficulty(), var1.isDifficultyLocked()));
            this.server.getPlayerList().sendPlayerPermissionLevel(this);
            var0.removePlayerImmediately(this, Entity.RemovalReason.CHANGED_DIMENSION);
            this.unsetRemoved();
            this.moveTo(param1, param2, param3, param4, param5);
            this.setServerLevel(param0);
            param0.addDuringCommandTeleport(this);
            this.triggerDimensionChangeTriggers(var0);
            this.connection.teleport(param1, param2, param3, param4, param5);
            this.server.getPlayerList().sendLevelInfo(this, param0);
            this.server.getPlayerList().sendAllPlayerInfo(this);
        }

    }

    @Nullable
    public BlockPos getRespawnPosition() {
        return this.respawnPosition;
    }

    public float getRespawnAngle() {
        return this.respawnAngle;
    }

    public ResourceKey<Level> getRespawnDimension() {
        return this.respawnDimension;
    }

    public boolean isRespawnForced() {
        return this.respawnForced;
    }

    public void setRespawnPosition(ResourceKey<Level> param0, @Nullable BlockPos param1, float param2, boolean param3, boolean param4) {
        if (param1 != null) {
            boolean var0 = param1.equals(this.respawnPosition) && param0.equals(this.respawnDimension);
            if (param4 && !var0) {
                this.sendSystemMessage(Component.translatable("block.minecraft.set_spawn"));
            }

            this.respawnPosition = param1;
            this.respawnDimension = param0;
            this.respawnAngle = param2;
            this.respawnForced = param3;
        } else {
            this.respawnPosition = null;
            this.respawnDimension = Level.OVERWORLD;
            this.respawnAngle = 0.0F;
            this.respawnForced = false;
        }

    }

    public SectionPos getLastSectionPos() {
        return this.lastSectionPos;
    }

    public void setLastSectionPos(SectionPos param0) {
        this.lastSectionPos = param0;
    }

    public ChunkTrackingView getChunkTrackingView() {
        return this.chunkTrackingView;
    }

    public void setChunkTrackingView(ChunkTrackingView param0) {
        this.chunkTrackingView = param0;
    }

    @Override
    public void playNotifySound(SoundEvent param0, SoundSource param1, float param2, float param3) {
        this.connection
            .send(
                new ClientboundSoundPacket(
                    BuiltInRegistries.SOUND_EVENT.wrapAsHolder(param0), param1, this.getX(), this.getY(), this.getZ(), param2, param3, this.random.nextLong()
                )
            );
    }

    @Override
    public ItemEntity drop(ItemStack param0, boolean param1, boolean param2) {
        ItemEntity var0 = super.drop(param0, param1, param2);
        if (var0 == null) {
            return null;
        } else {
            this.level().addFreshEntity(var0);
            ItemStack var1 = var0.getItem();
            if (param2) {
                if (!var1.isEmpty()) {
                    this.awardStat(Stats.ITEM_DROPPED.get(var1.getItem()), param0.getCount());
                }

                this.awardStat(Stats.DROP);
            }

            return var0;
        }
    }

    public TextFilter getTextFilter() {
        return this.textFilter;
    }

    public void setServerLevel(ServerLevel param0) {
        this.setLevel(param0);
        this.gameMode.setLevel(param0);
    }

    @Nullable
    private static GameType readPlayerMode(@Nullable CompoundTag param0, String param1) {
        return param0 != null && param0.contains(param1, 99) ? GameType.byId(param0.getInt(param1)) : null;
    }

    private GameType calculateGameModeForNewPlayer(@Nullable GameType param0) {
        GameType var0 = this.server.getForcedGameType();
        if (var0 != null) {
            return var0;
        } else {
            return param0 != null ? param0 : this.server.getDefaultGameType();
        }
    }

    public void loadGameTypes(@Nullable CompoundTag param0) {
        this.gameMode
            .setGameModeForPlayer(
                this.calculateGameModeForNewPlayer(readPlayerMode(param0, "playerGameType")), readPlayerMode(param0, "previousPlayerGameType")
            );
    }

    private void storeGameTypes(CompoundTag param0) {
        param0.putInt("playerGameType", this.gameMode.getGameModeForPlayer().getId());
        GameType var0 = this.gameMode.getPreviousGameModeForPlayer();
        if (var0 != null) {
            param0.putInt("previousPlayerGameType", var0.getId());
        }

    }

    @Override
    public boolean isTextFilteringEnabled() {
        return this.textFilteringEnabled;
    }

    public boolean shouldFilterMessageTo(ServerPlayer param0) {
        if (param0 == this) {
            return false;
        } else {
            return this.textFilteringEnabled || param0.textFilteringEnabled;
        }
    }

    @Override
    public boolean mayInteract(Level param0, BlockPos param1) {
        return super.mayInteract(param0, param1) && param0.mayInteract(this, param1);
    }

    @Override
    protected void updateUsingItem(ItemStack param0) {
        CriteriaTriggers.USING_ITEM.trigger(this, param0);
        super.updateUsingItem(param0);
    }

    public boolean drop(boolean param0) {
        Inventory var0 = this.getInventory();
        ItemStack var1 = var0.removeFromSelected(param0);
        this.containerMenu.findSlot(var0, var0.selected).ifPresent(param1 -> this.containerMenu.setRemoteSlot(param1, var0.getSelected()));
        return this.drop(var1, false, true) != null;
    }

    public boolean allowsListing() {
        return this.allowsListing;
    }

    @Override
    public Optional<WardenSpawnTracker> getWardenSpawnTracker() {
        return Optional.of(this.wardenSpawnTracker);
    }

    @Override
    public void onItemPickup(ItemEntity param0) {
        super.onItemPickup(param0);
        Entity var0 = param0.getOwner();
        if (var0 != null) {
            CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_PLAYER.trigger(this, param0.getItem(), var0);
        }

    }

    public void setChatSession(RemoteChatSession param0) {
        this.chatSession = param0;
    }

    @Nullable
    public RemoteChatSession getChatSession() {
        return this.chatSession != null && this.chatSession.hasExpired() ? null : this.chatSession;
    }

    @Override
    public void indicateDamage(double param0, double param1) {
        this.hurtDir = (float)(Mth.atan2(param1, param0) * 180.0F / (float)Math.PI - (double)this.getYRot());
        this.connection.send(new ClientboundHurtAnimationPacket(this));
    }

    @Override
    public boolean startRiding(Entity param0, boolean param1) {
        if (!super.startRiding(param0, param1)) {
            return false;
        } else {
            param0.positionRider(this);
            this.connection.teleport(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
            if (param0 instanceof LivingEntity var0) {
                for(MobEffectInstance var1 : var0.getActiveEffects()) {
                    this.connection.send(new ClientboundUpdateMobEffectPacket(param0.getId(), var1));
                }
            }

            return true;
        }
    }

    @Override
    public void stopRiding() {
        Entity var0 = this.getVehicle();
        super.stopRiding();
        if (var0 instanceof LivingEntity var1) {
            for(MobEffectInstance var2 : var1.getActiveEffects()) {
                this.connection.send(new ClientboundRemoveMobEffectPacket(var0.getId(), var2.getEffect()));
            }
        }

    }

    public CommonPlayerSpawnInfo createCommonSpawnInfo(ServerLevel param0) {
        return new CommonPlayerSpawnInfo(
            param0.dimensionTypeId(),
            param0.dimension(),
            BiomeManager.obfuscateSeed(param0.getSeed()),
            this.gameMode.getGameModeForPlayer(),
            this.gameMode.getPreviousGameModeForPlayer(),
            param0.isDebug(),
            param0.isFlat(),
            this.getLastDeathLocation(),
            this.getPortalCooldown()
        );
    }
}
