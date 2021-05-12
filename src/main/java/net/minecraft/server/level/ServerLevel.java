package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddVibrationSignalPacket;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.players.SleepStatus;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagContainer;
import net.minecraft.util.CsvOutput;
import net.minecraft.util.Mth;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ReputationEventHandler;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raids;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.BlockEventData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.ServerTickList;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.TickNextTickData;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.entity.EntityPersistentStorage;
import net.minecraft.world.level.entity.EntityTickList;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelCallback;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListenerRegistrar;
import net.minecraft.world.level.gameevent.vibrations.VibrationPath;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.portal.PortalForcer;
import net.minecraft.world.level.saveddata.maps.MapIndex;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerLevel extends Level implements WorldGenLevel {
    public static final BlockPos END_SPAWN_POINT = new BlockPos(100, 50, 0);
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int EMPTY_TIME_NO_TICK = 300;
    final List<ServerPlayer> players = Lists.newArrayList();
    private final ServerChunkCache chunkSource;
    private final MinecraftServer server;
    private final ServerLevelData serverLevelData;
    final EntityTickList entityTickList = new EntityTickList();
    private final PersistentEntitySectionManager<Entity> entityManager;
    public boolean noSave;
    private final SleepStatus sleepStatus;
    private int emptyTime;
    private final PortalForcer portalForcer;
    private final ServerTickList<Block> blockTicks = new ServerTickList<>(
        this, param0x -> param0x == null || param0x.defaultBlockState().isAir(), Registry.BLOCK::getKey, this::tickBlock
    );
    private final ServerTickList<Fluid> liquidTicks = new ServerTickList<>(
        this, param0x -> param0x == null || param0x == Fluids.EMPTY, Registry.FLUID::getKey, this::tickLiquid
    );
    final Set<Mob> navigatingMobs = new ObjectOpenHashSet<>();
    protected final Raids raids;
    private final ObjectLinkedOpenHashSet<BlockEventData> blockEvents = new ObjectLinkedOpenHashSet<>();
    private boolean handlingTick;
    private final List<CustomSpawner> customSpawners;
    @Nullable
    private final EndDragonFight dragonFight;
    final Int2ObjectMap<EnderDragonPart> dragonParts = new Int2ObjectOpenHashMap<>();
    private final StructureFeatureManager structureFeatureManager;
    private final boolean tickTime;

    public ServerLevel(
        MinecraftServer param0,
        Executor param1,
        LevelStorageSource.LevelStorageAccess param2,
        ServerLevelData param3,
        ResourceKey<Level> param4,
        DimensionType param5,
        ChunkProgressListener param6,
        ChunkGenerator param7,
        boolean param8,
        long param9,
        List<CustomSpawner> param10,
        boolean param11
    ) {
        super(param3, param4, param5, param0::getProfiler, false, param8, param9);
        this.tickTime = param11;
        this.server = param0;
        this.customSpawners = param10;
        this.serverLevelData = param3;
        boolean var0 = param0.forceSynchronousWrites();
        DataFixer var1 = param0.getFixerUpper();
        EntityPersistentStorage<Entity> var2 = new EntityStorage(this, new File(param2.getDimensionPath(param4), "entities"), var1, var0, param0);
        this.entityManager = new PersistentEntitySectionManager<>(Entity.class, new ServerLevel.EntityCallbacks(), var2);
        this.chunkSource = new ServerChunkCache(
            this,
            param2,
            var1,
            param0.getStructureManager(),
            param1,
            param7,
            param0.getPlayerList().getViewDistance(),
            var0,
            param6,
            this.entityManager::updateChunkStatus,
            () -> param0.overworld().getDataStorage()
        );
        this.portalForcer = new PortalForcer(this);
        this.updateSkyBrightness();
        this.prepareWeather();
        this.getWorldBorder().setAbsoluteMaxSize(param0.getAbsoluteMaxWorldSize());
        this.raids = this.getDataStorage().computeIfAbsent(param0x -> Raids.load(this, param0x), () -> new Raids(this), Raids.getFileId(this.dimensionType()));
        if (!param0.isSingleplayer()) {
            param3.setGameType(param0.getDefaultGameType());
        }

        this.structureFeatureManager = new StructureFeatureManager(this, param0.getWorldData().worldGenSettings());
        if (this.dimensionType().createDragonFight()) {
            this.dragonFight = new EndDragonFight(this, param0.getWorldData().worldGenSettings().seed(), param0.getWorldData().endDragonFightData());
        } else {
            this.dragonFight = null;
        }

        this.sleepStatus = new SleepStatus();
    }

    public void setWeatherParameters(int param0, int param1, boolean param2, boolean param3) {
        this.serverLevelData.setClearWeatherTime(param0);
        this.serverLevelData.setRainTime(param1);
        this.serverLevelData.setThunderTime(param1);
        this.serverLevelData.setRaining(param2);
        this.serverLevelData.setThundering(param3);
    }

    @Override
    public Biome getUncachedNoiseBiome(int param0, int param1, int param2) {
        return this.getChunkSource().getGenerator().getBiomeSource().getNoiseBiome(param0, param1, param2);
    }

    public StructureFeatureManager structureFeatureManager() {
        return this.structureFeatureManager;
    }

    public void tick(BooleanSupplier param0) {
        ProfilerFiller var0 = this.getProfiler();
        this.handlingTick = true;
        var0.push("world border");
        this.getWorldBorder().tick();
        var0.popPush("weather");
        boolean var1 = this.isRaining();
        if (this.dimensionType().hasSkyLight()) {
            if (this.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE)) {
                int var2 = this.serverLevelData.getClearWeatherTime();
                int var3 = this.serverLevelData.getThunderTime();
                int var4 = this.serverLevelData.getRainTime();
                boolean var5 = this.levelData.isThundering();
                boolean var6 = this.levelData.isRaining();
                if (var2 > 0) {
                    --var2;
                    var3 = var5 ? 0 : 1;
                    var4 = var6 ? 0 : 1;
                    var5 = false;
                    var6 = false;
                } else {
                    if (var3 > 0) {
                        if (--var3 == 0) {
                            var5 = !var5;
                        }
                    } else if (var5) {
                        var3 = this.random.nextInt(12000) + 3600;
                    } else {
                        var3 = this.random.nextInt(168000) + 12000;
                    }

                    if (var4 > 0) {
                        if (--var4 == 0) {
                            var6 = !var6;
                        }
                    } else if (var6) {
                        var4 = this.random.nextInt(12000) + 12000;
                    } else {
                        var4 = this.random.nextInt(168000) + 12000;
                    }
                }

                this.serverLevelData.setThunderTime(var3);
                this.serverLevelData.setRainTime(var4);
                this.serverLevelData.setClearWeatherTime(var2);
                this.serverLevelData.setThundering(var5);
                this.serverLevelData.setRaining(var6);
            }

            this.oThunderLevel = this.thunderLevel;
            if (this.levelData.isThundering()) {
                this.thunderLevel = (float)((double)this.thunderLevel + 0.01);
            } else {
                this.thunderLevel = (float)((double)this.thunderLevel - 0.01);
            }

            this.thunderLevel = Mth.clamp(this.thunderLevel, 0.0F, 1.0F);
            this.oRainLevel = this.rainLevel;
            if (this.levelData.isRaining()) {
                this.rainLevel = (float)((double)this.rainLevel + 0.01);
            } else {
                this.rainLevel = (float)((double)this.rainLevel - 0.01);
            }

            this.rainLevel = Mth.clamp(this.rainLevel, 0.0F, 1.0F);
        }

        if (this.oRainLevel != this.rainLevel) {
            this.server
                .getPlayerList()
                .broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, this.rainLevel), this.dimension());
        }

        if (this.oThunderLevel != this.thunderLevel) {
            this.server
                .getPlayerList()
                .broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, this.thunderLevel), this.dimension());
        }

        if (var1 != this.isRaining()) {
            if (var1) {
                this.server.getPlayerList().broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.STOP_RAINING, 0.0F));
            } else {
                this.server.getPlayerList().broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.START_RAINING, 0.0F));
            }

            this.server.getPlayerList().broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, this.rainLevel));
            this.server.getPlayerList().broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, this.thunderLevel));
        }

        int var7 = this.getGameRules().getInt(GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE);
        if (this.sleepStatus.areEnoughSleeping(var7) && this.sleepStatus.areEnoughDeepSleeping(var7, this.players)) {
            if (this.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
                long var8 = this.levelData.getDayTime() + 24000L;
                this.setDayTime(var8 - var8 % 24000L);
            }

            this.wakeUpAllPlayers();
            if (this.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE)) {
                this.stopWeather();
            }
        }

        this.updateSkyBrightness();
        this.tickTime();
        var0.popPush("chunkSource");
        this.getChunkSource().tick(param0);
        var0.popPush("tickPending");
        if (!this.isDebug()) {
            this.blockTicks.tick();
            this.liquidTicks.tick();
        }

        var0.popPush("raid");
        this.raids.tick();
        var0.popPush("blockEvents");
        this.runBlockEvents();
        this.handlingTick = false;
        var0.pop();
        boolean var9 = !this.players.isEmpty() || !this.getForcedChunks().isEmpty();
        if (var9) {
            this.resetEmptyTime();
        }

        if (var9 || this.emptyTime++ < 300) {
            var0.push("entities");
            if (this.dragonFight != null) {
                var0.push("dragonFight");
                this.dragonFight.tick();
                var0.pop();
            }

            this.entityTickList.forEach(param1 -> {
                if (!param1.isRemoved()) {
                    if (this.shouldDiscardEntity(param1)) {
                        param1.discard();
                    } else {
                        var0.push("checkDespawn");
                        param1.checkDespawn();
                        var0.pop();
                        Entity var0x = param1.getVehicle();
                        if (var0x != null) {
                            if (!var0x.isRemoved() && var0x.hasPassenger(param1)) {
                                return;
                            }

                            param1.stopRiding();
                        }

                        var0.push("tick");
                        this.guardEntityTick(this::tickNonPassenger, param1);
                        var0.pop();
                    }
                }
            });
            var0.pop();
            this.tickBlockEntities();
        }

        var0.push("entityManagement");
        this.entityManager.tick();
        var0.pop();
    }

    protected void tickTime() {
        if (this.tickTime) {
            long var0 = this.levelData.getGameTime() + 1L;
            this.serverLevelData.setGameTime(var0);
            this.serverLevelData.getScheduledEvents().tick(this.server, var0);
            if (this.levelData.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
                this.setDayTime(this.levelData.getDayTime() + 1L);
            }

        }
    }

    public void setDayTime(long param0) {
        this.serverLevelData.setDayTime(param0);
    }

    public void tickCustomSpawners(boolean param0, boolean param1) {
        for(CustomSpawner var0 : this.customSpawners) {
            var0.tick(this, param0, param1);
        }

    }

    private boolean shouldDiscardEntity(Entity param0) {
        if (this.server.isSpawningAnimals() || !(param0 instanceof Animal) && !(param0 instanceof WaterAnimal)) {
            return !this.server.areNpcsEnabled() && param0 instanceof Npc;
        } else {
            return true;
        }
    }

    private void wakeUpAllPlayers() {
        this.sleepStatus.removeAllSleepers();
        this.players.stream().filter(LivingEntity::isSleeping).collect(Collectors.toList()).forEach(param0 -> param0.stopSleepInBed(false, false));
    }

    public void tickChunk(LevelChunk param0, int param1) {
        ChunkPos var0 = param0.getPos();
        boolean var1 = this.isRaining();
        int var2 = var0.getMinBlockX();
        int var3 = var0.getMinBlockZ();
        ProfilerFiller var4 = this.getProfiler();
        var4.push("thunder");
        if (var1 && this.isThundering() && this.random.nextInt(100000) == 0) {
            BlockPos var5 = this.findLightningTargetAround(this.getBlockRandomPos(var2, 0, var3, 15));
            if (this.isRainingAt(var5)) {
                DifficultyInstance var6 = this.getCurrentDifficultyAt(var5);
                boolean var7 = this.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)
                    && this.random.nextDouble() < (double)var6.getEffectiveDifficulty() * 0.01
                    && !this.getBlockState(var5.below()).is(Blocks.LIGHTNING_ROD);
                if (var7) {
                    SkeletonHorse var8 = EntityType.SKELETON_HORSE.create(this);
                    var8.setTrap(true);
                    var8.setAge(0);
                    var8.setPos((double)var5.getX(), (double)var5.getY(), (double)var5.getZ());
                    this.addFreshEntity(var8);
                }

                LightningBolt var9 = EntityType.LIGHTNING_BOLT.create(this);
                var9.moveTo(Vec3.atBottomCenterOf(var5));
                var9.setVisualOnly(var7);
                this.addFreshEntity(var9);
            }
        }

        var4.popPush("iceandsnow");
        if (this.random.nextInt(16) == 0) {
            BlockPos var10 = this.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, this.getBlockRandomPos(var2, 0, var3, 15));
            BlockPos var11 = var10.below();
            Biome var12 = this.getBiome(var10);
            if (var12.shouldFreeze(this, var11)) {
                this.setBlockAndUpdate(var11, Blocks.ICE.defaultBlockState());
            }

            if (var1) {
                if (var12.shouldSnow(this, var10)) {
                    this.setBlockAndUpdate(var10, Blocks.SNOW.defaultBlockState());
                }

                BlockState var13 = this.getBlockState(var11);
                Biome.Precipitation var14 = this.getBiome(var10).getPrecipitation();
                if (var14 == Biome.Precipitation.RAIN && var12.isColdEnoughToSnow(var11)) {
                    var14 = Biome.Precipitation.SNOW;
                }

                var13.getBlock().handlePrecipitation(var13, this, var11, var14);
            }
        }

        var4.popPush("tickBlocks");
        if (param1 > 0) {
            for(LevelChunkSection var15 : param0.getSections()) {
                if (var15 != LevelChunk.EMPTY_SECTION && var15.isRandomlyTicking()) {
                    int var16 = var15.bottomBlockY();

                    for(int var17 = 0; var17 < param1; ++var17) {
                        BlockPos var18 = this.getBlockRandomPos(var2, var16, var3, 15);
                        var4.push("randomTick");
                        BlockState var19 = var15.getBlockState(var18.getX() - var2, var18.getY() - var16, var18.getZ() - var3);
                        if (var19.isRandomlyTicking()) {
                            var19.randomTick(this, var18, this.random);
                        }

                        FluidState var20 = var19.getFluidState();
                        if (var20.isRandomlyTicking()) {
                            var20.randomTick(this, var18, this.random);
                        }

                        var4.pop();
                    }
                }
            }
        }

        var4.pop();
    }

    private Optional<BlockPos> findLightningRod(BlockPos param0) {
        Optional<BlockPos> var0 = this.getPoiManager()
            .findClosest(
                param0x -> param0x == PoiType.LIGHTNING_ROD,
                param0x -> param0x.getY() == this.getLevel().getHeight(Heightmap.Types.WORLD_SURFACE, param0x.getX(), param0x.getZ()) - 1,
                param0,
                128,
                PoiManager.Occupancy.ANY
            );
        return var0.map(param0x -> param0x.above(1));
    }

    protected BlockPos findLightningTargetAround(BlockPos param0) {
        BlockPos var0 = this.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, param0);
        Optional<BlockPos> var1 = this.findLightningRod(var0);
        if (var1.isPresent()) {
            return var1.get();
        } else {
            AABB var2 = new AABB(var0, new BlockPos(var0.getX(), this.getMaxBuildHeight(), var0.getZ())).inflate(3.0);
            List<LivingEntity> var3 = this.getEntitiesOfClass(
                LivingEntity.class, var2, param0x -> param0x != null && param0x.isAlive() && this.canSeeSky(param0x.blockPosition())
            );
            if (!var3.isEmpty()) {
                return var3.get(this.random.nextInt(var3.size())).blockPosition();
            } else {
                if (var0.getY() == this.getMinBuildHeight() - 1) {
                    var0 = var0.above(2);
                }

                return var0;
            }
        }
    }

    public boolean isHandlingTick() {
        return this.handlingTick;
    }

    public boolean canSleepThroughNights() {
        return this.getGameRules().getInt(GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE) <= 100;
    }

    private void announceSleepStatus() {
        if (this.canSleepThroughNights()) {
            if (!this.getServer().isSingleplayer() || this.getServer().isPublished()) {
                int var0 = this.getGameRules().getInt(GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE);
                Component var1;
                if (this.sleepStatus.areEnoughSleeping(var0)) {
                    var1 = new TranslatableComponent("sleep.skipping_night");
                } else {
                    var1 = new TranslatableComponent("sleep.players_sleeping", this.sleepStatus.amountSleeping(), this.sleepStatus.sleepersNeeded(var0));
                }

                for(ServerPlayer var3 : this.players) {
                    var3.displayClientMessage(var1, true);
                }

            }
        }
    }

    public void updateSleepingPlayerList() {
        if (!this.players.isEmpty() && this.sleepStatus.update(this.players)) {
            this.announceSleepStatus();
        }

    }

    public ServerScoreboard getScoreboard() {
        return this.server.getScoreboard();
    }

    private void stopWeather() {
        this.serverLevelData.setRainTime(0);
        this.serverLevelData.setRaining(false);
        this.serverLevelData.setThunderTime(0);
        this.serverLevelData.setThundering(false);
    }

    public void resetEmptyTime() {
        this.emptyTime = 0;
    }

    private void tickLiquid(TickNextTickData<Fluid> param0x) {
        FluidState var0x = this.getFluidState(param0x.pos);
        if (var0x.getType() == param0x.getType()) {
            var0x.tick(this, param0x.pos);
        }

    }

    private void tickBlock(TickNextTickData<Block> param0x) {
        BlockState var0x = this.getBlockState(param0x.pos);
        if (var0x.is(param0x.getType())) {
            var0x.tick(this, param0x.pos, this.random);
        }

    }

    public void tickNonPassenger(Entity param0) {
        param0.setOldPosAndRot();
        ProfilerFiller var0 = this.getProfiler();
        ++param0.tickCount;
        this.getProfiler().push(() -> Registry.ENTITY_TYPE.getKey(param0.getType()).toString());
        var0.incrementCounter("tickNonPassenger");
        param0.tick();
        this.getProfiler().pop();

        for(Entity var1 : param0.getPassengers()) {
            this.tickPassenger(param0, var1);
        }

    }

    private void tickPassenger(Entity param0, Entity param1) {
        if (param1.isRemoved() || param1.getVehicle() != param0) {
            param1.stopRiding();
        } else if (param1 instanceof Player || this.entityTickList.contains(param1)) {
            param1.setOldPosAndRot();
            ++param1.tickCount;
            ProfilerFiller var0 = this.getProfiler();
            var0.push(() -> Registry.ENTITY_TYPE.getKey(param1.getType()).toString());
            var0.incrementCounter("tickPassenger");
            param1.rideTick();
            var0.pop();

            for(Entity var1 : param1.getPassengers()) {
                this.tickPassenger(param1, var1);
            }

        }
    }

    @Override
    public boolean mayInteract(Player param0, BlockPos param1) {
        return !this.server.isUnderSpawnProtection(this, param1, param0) && this.getWorldBorder().isWithinBounds(param1);
    }

    public void save(@Nullable ProgressListener param0, boolean param1, boolean param2) {
        ServerChunkCache var0 = this.getChunkSource();
        if (!param2) {
            if (param0 != null) {
                param0.progressStartNoAbort(new TranslatableComponent("menu.savingLevel"));
            }

            this.saveLevelData();
            if (param0 != null) {
                param0.progressStage(new TranslatableComponent("menu.savingChunks"));
            }

            var0.save(param1);
            if (param1) {
                this.entityManager.saveAll();
            } else {
                this.entityManager.autoSave();
            }

        }
    }

    private void saveLevelData() {
        if (this.dragonFight != null) {
            this.server.getWorldData().setEndDragonFightData(this.dragonFight.saveData());
        }

        this.getChunkSource().getDataStorage().save();
    }

    public <T extends Entity> List<? extends T> getEntities(EntityTypeTest<Entity, T> param0, Predicate<? super T> param1) {
        List<T> var0 = Lists.newArrayList();
        this.getEntities().get(param0, param2 -> {
            if (param1.test(param2)) {
                var0.add(param2);
            }

        });
        return var0;
    }

    public List<? extends EnderDragon> getDragons() {
        return this.getEntities(EntityType.ENDER_DRAGON, LivingEntity::isAlive);
    }

    public List<ServerPlayer> getPlayers(Predicate<? super ServerPlayer> param0) {
        List<ServerPlayer> var0 = Lists.newArrayList();

        for(ServerPlayer var1 : this.players) {
            if (param0.test(var1)) {
                var0.add(var1);
            }
        }

        return var0;
    }

    @Nullable
    public ServerPlayer getRandomPlayer() {
        List<ServerPlayer> var0 = this.getPlayers(LivingEntity::isAlive);
        return var0.isEmpty() ? null : var0.get(this.random.nextInt(var0.size()));
    }

    @Override
    public boolean addFreshEntity(Entity param0) {
        return this.addEntity(param0);
    }

    public boolean addWithUUID(Entity param0) {
        return this.addEntity(param0);
    }

    public void addDuringTeleport(Entity param0) {
        this.addEntity(param0);
    }

    public void addDuringCommandTeleport(ServerPlayer param0) {
        this.addPlayer(param0);
    }

    public void addDuringPortalTeleport(ServerPlayer param0) {
        this.addPlayer(param0);
    }

    public void addNewPlayer(ServerPlayer param0) {
        this.addPlayer(param0);
    }

    public void addRespawnedPlayer(ServerPlayer param0) {
        this.addPlayer(param0);
    }

    private void addPlayer(ServerPlayer param0) {
        Entity var0 = this.getEntities().get(param0.getUUID());
        if (var0 != null) {
            LOGGER.warn("Force-added player with duplicate UUID {}", param0.getUUID().toString());
            var0.unRide();
            this.removePlayerImmediately((ServerPlayer)var0, Entity.RemovalReason.DISCARDED);
        }

        this.entityManager.addNewEntity(param0);
    }

    private boolean addEntity(Entity param0) {
        if (param0.isRemoved()) {
            LOGGER.warn("Tried to add entity {} but it was marked as removed already", EntityType.getKey(param0.getType()));
            return false;
        } else {
            return this.entityManager.addNewEntity(param0);
        }
    }

    public boolean tryAddFreshEntityWithPassengers(Entity param0) {
        if (param0.getSelfAndPassengers().map(Entity::getUUID).anyMatch(this.entityManager::isLoaded)) {
            return false;
        } else {
            this.addFreshEntityWithPassengers(param0);
            return true;
        }
    }

    public void unload(LevelChunk param0) {
        param0.invalidateAllBlockEntities();
    }

    public void removePlayerImmediately(ServerPlayer param0, Entity.RemovalReason param1) {
        param0.remove(param1);
    }

    @Override
    public void destroyBlockProgress(int param0, BlockPos param1, int param2) {
        for(ServerPlayer var0 : this.server.getPlayerList().getPlayers()) {
            if (var0 != null && var0.level == this && var0.getId() != param0) {
                double var1 = (double)param1.getX() - var0.getX();
                double var2 = (double)param1.getY() - var0.getY();
                double var3 = (double)param1.getZ() - var0.getZ();
                if (var1 * var1 + var2 * var2 + var3 * var3 < 1024.0) {
                    var0.connection.send(new ClientboundBlockDestructionPacket(param0, param1, param2));
                }
            }
        }

    }

    @Override
    public void playSound(
        @Nullable Player param0, double param1, double param2, double param3, SoundEvent param4, SoundSource param5, float param6, float param7
    ) {
        this.server
            .getPlayerList()
            .broadcast(
                param0,
                param1,
                param2,
                param3,
                param6 > 1.0F ? (double)(16.0F * param6) : 16.0,
                this.dimension(),
                new ClientboundSoundPacket(param4, param5, param1, param2, param3, param6, param7)
            );
    }

    @Override
    public void playSound(@Nullable Player param0, Entity param1, SoundEvent param2, SoundSource param3, float param4, float param5) {
        this.server
            .getPlayerList()
            .broadcast(
                param0,
                param1.getX(),
                param1.getY(),
                param1.getZ(),
                param4 > 1.0F ? (double)(16.0F * param4) : 16.0,
                this.dimension(),
                new ClientboundSoundEntityPacket(param2, param3, param1, param4, param5)
            );
    }

    @Override
    public void globalLevelEvent(int param0, BlockPos param1, int param2) {
        this.server.getPlayerList().broadcastAll(new ClientboundLevelEventPacket(param0, param1, param2, true));
    }

    @Override
    public void levelEvent(@Nullable Player param0, int param1, BlockPos param2, int param3) {
        this.server
            .getPlayerList()
            .broadcast(
                param0,
                (double)param2.getX(),
                (double)param2.getY(),
                (double)param2.getZ(),
                64.0,
                this.dimension(),
                new ClientboundLevelEventPacket(param1, param2, param3, false)
            );
    }

    @Override
    public int getLogicalHeight() {
        return this.dimensionType().logicalHeight();
    }

    @Override
    public void gameEvent(@Nullable Entity param0, GameEvent param1, BlockPos param2) {
        this.postGameEventInRadius(param0, param1, param2, param1.getNotificationRadius());
    }

    @Override
    public void sendBlockUpdated(BlockPos param0, BlockState param1, BlockState param2, int param3) {
        this.getChunkSource().blockChanged(param0);
        VoxelShape var0 = param1.getCollisionShape(this, param0);
        VoxelShape var1 = param2.getCollisionShape(this, param0);
        if (Shapes.joinIsNotEmpty(var0, var1, BooleanOp.NOT_SAME)) {
            for(Mob var2 : this.navigatingMobs) {
                PathNavigation var3 = var2.getNavigation();
                if (!var3.hasDelayedRecomputation()) {
                    var3.recomputePath(param0);
                }
            }

        }
    }

    @Override
    public void broadcastEntityEvent(Entity param0, byte param1) {
        this.getChunkSource().broadcastAndSend(param0, new ClientboundEntityEventPacket(param0, param1));
    }

    public ServerChunkCache getChunkSource() {
        return this.chunkSource;
    }

    @Override
    public Explosion explode(
        @Nullable Entity param0,
        @Nullable DamageSource param1,
        @Nullable ExplosionDamageCalculator param2,
        double param3,
        double param4,
        double param5,
        float param6,
        boolean param7,
        Explosion.BlockInteraction param8
    ) {
        Explosion var0 = new Explosion(this, param0, param1, param2, param3, param4, param5, param6, param7, param8);
        var0.explode();
        var0.finalizeExplosion(false);
        if (param8 == Explosion.BlockInteraction.NONE) {
            var0.clearToBlow();
        }

        for(ServerPlayer var1 : this.players) {
            if (var1.distanceToSqr(param3, param4, param5) < 4096.0) {
                var1.connection.send(new ClientboundExplodePacket(param3, param4, param5, param6, var0.getToBlow(), var0.getHitPlayers().get(var1)));
            }
        }

        return var0;
    }

    @Override
    public void blockEvent(BlockPos param0, Block param1, int param2, int param3) {
        this.blockEvents.add(new BlockEventData(param0, param1, param2, param3));
    }

    private void runBlockEvents() {
        while(!this.blockEvents.isEmpty()) {
            BlockEventData var0 = this.blockEvents.removeFirst();
            if (this.doBlockEvent(var0)) {
                this.server
                    .getPlayerList()
                    .broadcast(
                        null,
                        (double)var0.getPos().getX(),
                        (double)var0.getPos().getY(),
                        (double)var0.getPos().getZ(),
                        64.0,
                        this.dimension(),
                        new ClientboundBlockEventPacket(var0.getPos(), var0.getBlock(), var0.getParamA(), var0.getParamB())
                    );
            }
        }

    }

    private boolean doBlockEvent(BlockEventData param0) {
        BlockState var0 = this.getBlockState(param0.getPos());
        return var0.is(param0.getBlock()) ? var0.triggerEvent(this, param0.getPos(), param0.getParamA(), param0.getParamB()) : false;
    }

    public ServerTickList<Block> getBlockTicks() {
        return this.blockTicks;
    }

    public ServerTickList<Fluid> getLiquidTicks() {
        return this.liquidTicks;
    }

    @Nonnull
    @Override
    public MinecraftServer getServer() {
        return this.server;
    }

    public PortalForcer getPortalForcer() {
        return this.portalForcer;
    }

    public StructureManager getStructureManager() {
        return this.server.getStructureManager();
    }

    public void sendVibrationParticle(VibrationPath param0) {
        BlockPos var0 = param0.getOrigin();
        ClientboundAddVibrationSignalPacket var1 = new ClientboundAddVibrationSignalPacket(param0);
        this.players.forEach(param2 -> this.sendParticles(param2, false, (double)var0.getX(), (double)var0.getY(), (double)var0.getZ(), var1));
    }

    public <T extends ParticleOptions> int sendParticles(
        T param0, double param1, double param2, double param3, int param4, double param5, double param6, double param7, double param8
    ) {
        ClientboundLevelParticlesPacket var0 = new ClientboundLevelParticlesPacket(
            param0, false, param1, param2, param3, (float)param5, (float)param6, (float)param7, (float)param8, param4
        );
        int var1 = 0;

        for(int var2 = 0; var2 < this.players.size(); ++var2) {
            ServerPlayer var3 = this.players.get(var2);
            if (this.sendParticles(var3, false, param1, param2, param3, var0)) {
                ++var1;
            }
        }

        return var1;
    }

    public <T extends ParticleOptions> boolean sendParticles(
        ServerPlayer param0,
        T param1,
        boolean param2,
        double param3,
        double param4,
        double param5,
        int param6,
        double param7,
        double param8,
        double param9,
        double param10
    ) {
        Packet<?> var0 = new ClientboundLevelParticlesPacket(
            param1, param2, param3, param4, param5, (float)param7, (float)param8, (float)param9, (float)param10, param6
        );
        return this.sendParticles(param0, param2, param3, param4, param5, var0);
    }

    private boolean sendParticles(ServerPlayer param0, boolean param1, double param2, double param3, double param4, Packet<?> param5) {
        if (param0.getLevel() != this) {
            return false;
        } else {
            BlockPos var0 = param0.blockPosition();
            if (var0.closerThan(new Vec3(param2, param3, param4), param1 ? 512.0 : 32.0)) {
                param0.connection.send(param5);
                return true;
            } else {
                return false;
            }
        }
    }

    @Nullable
    @Override
    public Entity getEntity(int param0) {
        return this.getEntities().get(param0);
    }

    @Deprecated
    @Nullable
    public Entity getEntityOrPart(int param0) {
        Entity var0 = this.getEntities().get(param0);
        return var0 != null ? var0 : this.dragonParts.get(param0);
    }

    @Nullable
    public Entity getEntity(UUID param0) {
        return this.getEntities().get(param0);
    }

    @Nullable
    public BlockPos findNearestMapFeature(StructureFeature<?> param0, BlockPos param1, int param2, boolean param3) {
        return !this.server.getWorldData().worldGenSettings().generateFeatures()
            ? null
            : this.getChunkSource().getGenerator().findNearestMapFeature(this, param0, param1, param2, param3);
    }

    @Nullable
    public BlockPos findNearestBiome(Biome param0, BlockPos param1, int param2, int param3) {
        return this.getChunkSource()
            .getGenerator()
            .getBiomeSource()
            .findBiomeHorizontal(param1.getX(), param1.getY(), param1.getZ(), param2, param3, param1x -> param1x == param0, this.random, true);
    }

    @Override
    public RecipeManager getRecipeManager() {
        return this.server.getRecipeManager();
    }

    @Override
    public TagContainer getTagManager() {
        return this.server.getTags();
    }

    @Override
    public boolean noSave() {
        return this.noSave;
    }

    @Override
    public RegistryAccess registryAccess() {
        return this.server.registryAccess();
    }

    public DimensionDataStorage getDataStorage() {
        return this.getChunkSource().getDataStorage();
    }

    @Nullable
    @Override
    public MapItemSavedData getMapData(String param0) {
        return this.getServer().overworld().getDataStorage().get(MapItemSavedData::load, param0);
    }

    @Override
    public void setMapData(String param0, MapItemSavedData param1) {
        this.getServer().overworld().getDataStorage().set(param0, param1);
    }

    @Override
    public int getFreeMapId() {
        return this.getServer().overworld().getDataStorage().computeIfAbsent(MapIndex::load, MapIndex::new, "idcounts").getFreeAuxValueForMap();
    }

    public void setDefaultSpawnPos(BlockPos param0, float param1) {
        ChunkPos var0 = new ChunkPos(new BlockPos(this.levelData.getXSpawn(), 0, this.levelData.getZSpawn()));
        this.levelData.setSpawn(param0, param1);
        this.getChunkSource().removeRegionTicket(TicketType.START, var0, 11, Unit.INSTANCE);
        this.getChunkSource().addRegionTicket(TicketType.START, new ChunkPos(param0), 11, Unit.INSTANCE);
        this.getServer().getPlayerList().broadcastAll(new ClientboundSetDefaultSpawnPositionPacket(param0, param1));
    }

    public BlockPos getSharedSpawnPos() {
        BlockPos var0 = new BlockPos(this.levelData.getXSpawn(), this.levelData.getYSpawn(), this.levelData.getZSpawn());
        if (!this.getWorldBorder().isWithinBounds(var0)) {
            var0 = this.getHeightmapPos(
                Heightmap.Types.MOTION_BLOCKING, new BlockPos(this.getWorldBorder().getCenterX(), 0.0, this.getWorldBorder().getCenterZ())
            );
        }

        return var0;
    }

    public float getSharedSpawnAngle() {
        return this.levelData.getSpawnAngle();
    }

    public LongSet getForcedChunks() {
        ForcedChunksSavedData var0 = this.getDataStorage().get(ForcedChunksSavedData::load, "chunks");
        return (LongSet)(var0 != null ? LongSets.unmodifiable(var0.getChunks()) : LongSets.EMPTY_SET);
    }

    public boolean setChunkForced(int param0, int param1, boolean param2) {
        ForcedChunksSavedData var0 = this.getDataStorage().computeIfAbsent(ForcedChunksSavedData::load, ForcedChunksSavedData::new, "chunks");
        ChunkPos var1 = new ChunkPos(param0, param1);
        long var2 = var1.toLong();
        boolean var3;
        if (param2) {
            var3 = var0.getChunks().add(var2);
            if (var3) {
                this.getChunk(param0, param1);
            }
        } else {
            var3 = var0.getChunks().remove(var2);
        }

        var0.setDirty(var3);
        if (var3) {
            this.getChunkSource().updateChunkForced(var1, param2);
        }

        return var3;
    }

    @Override
    public List<ServerPlayer> players() {
        return this.players;
    }

    @Override
    public void onBlockStateChange(BlockPos param0, BlockState param1, BlockState param2) {
        Optional<PoiType> var0 = PoiType.forState(param1);
        Optional<PoiType> var1 = PoiType.forState(param2);
        if (!Objects.equals(var0, var1)) {
            BlockPos var2 = param0.immutable();
            var0.ifPresent(param1x -> this.getServer().execute(() -> {
                    this.getPoiManager().remove(var2);
                    DebugPackets.sendPoiRemovedPacket(this, var2);
                }));
            var1.ifPresent(param1x -> this.getServer().execute(() -> {
                    this.getPoiManager().add(var2, param1x);
                    DebugPackets.sendPoiAddedPacket(this, var2);
                }));
        }
    }

    public PoiManager getPoiManager() {
        return this.getChunkSource().getPoiManager();
    }

    public boolean isVillage(BlockPos param0) {
        return this.isCloseToVillage(param0, 1);
    }

    public boolean isVillage(SectionPos param0) {
        return this.isVillage(param0.center());
    }

    public boolean isCloseToVillage(BlockPos param0, int param1) {
        if (param1 > 6) {
            return false;
        } else {
            return this.sectionsToVillage(SectionPos.of(param0)) <= param1;
        }
    }

    public int sectionsToVillage(SectionPos param0) {
        return this.getPoiManager().sectionsToVillage(param0);
    }

    public Raids getRaids() {
        return this.raids;
    }

    @Nullable
    public Raid getRaidAt(BlockPos param0) {
        return this.raids.getNearbyRaid(param0, 9216);
    }

    public boolean isRaided(BlockPos param0) {
        return this.getRaidAt(param0) != null;
    }

    public void onReputationEvent(ReputationEventType param0, Entity param1, ReputationEventHandler param2) {
        param2.onReputationEventFrom(param0, param1);
    }

    public void saveDebugReport(Path param0) throws IOException {
        ChunkMap var0 = this.getChunkSource().chunkMap;

        try (Writer var1 = Files.newBufferedWriter(param0.resolve("stats.txt"))) {
            var1.write(String.format("spawning_chunks: %d\n", var0.getDistanceManager().getNaturalSpawnChunkCount()));
            NaturalSpawner.SpawnState var2 = this.getChunkSource().getLastSpawnState();
            if (var2 != null) {
                for(Entry<MobCategory> var3 : var2.getMobCategoryCounts().object2IntEntrySet()) {
                    var1.write(String.format("spawn_count.%s: %d\n", var3.getKey().getName(), var3.getIntValue()));
                }
            }

            var1.write(String.format("entities: %s\n", this.entityManager.gatherStats()));
            var1.write(String.format("block_entity_tickers: %d\n", this.blockEntityTickers.size()));
            var1.write(String.format("block_ticks: %d\n", this.getBlockTicks().size()));
            var1.write(String.format("fluid_ticks: %d\n", this.getLiquidTicks().size()));
            var1.write("distance_manager: " + var0.getDistanceManager().getDebugStatus() + "\n");
            var1.write(String.format("pending_tasks: %d\n", this.getChunkSource().getPendingTasksCount()));
        }

        CrashReport var4 = new CrashReport("Level dump", new Exception("dummy"));
        this.fillReportDetails(var4);

        try (Writer var5 = Files.newBufferedWriter(param0.resolve("example_crash.txt"))) {
            var5.write(var4.getFriendlyReport());
        }

        Path var6 = param0.resolve("chunks.csv");

        try (Writer var7 = Files.newBufferedWriter(var6)) {
            var0.dumpChunks(var7);
        }

        Path var8 = param0.resolve("entity_chunks.csv");

        try (Writer var9 = Files.newBufferedWriter(var8)) {
            this.entityManager.dumpSections(var9);
        }

        Path var10 = param0.resolve("entities.csv");

        try (Writer var11 = Files.newBufferedWriter(var10)) {
            dumpEntities(var11, this.getEntities().getAll());
        }

        Path var12 = param0.resolve("block_entities.csv");

        try (Writer var13 = Files.newBufferedWriter(var12)) {
            this.dumpBlockEntityTickers(var13);
        }

    }

    private static void dumpEntities(Writer param0, Iterable<Entity> param1) throws IOException {
        CsvOutput var0 = CsvOutput.builder()
            .addColumn("x")
            .addColumn("y")
            .addColumn("z")
            .addColumn("uuid")
            .addColumn("type")
            .addColumn("alive")
            .addColumn("display_name")
            .addColumn("custom_name")
            .build(param0);

        for(Entity var1 : param1) {
            Component var2 = var1.getCustomName();
            Component var3 = var1.getDisplayName();
            var0.writeRow(
                var1.getX(),
                var1.getY(),
                var1.getZ(),
                var1.getUUID(),
                Registry.ENTITY_TYPE.getKey(var1.getType()),
                var1.isAlive(),
                var3.getString(),
                var2 != null ? var2.getString() : null
            );
        }

    }

    private void dumpBlockEntityTickers(Writer param0) throws IOException {
        CsvOutput var0 = CsvOutput.builder().addColumn("x").addColumn("y").addColumn("z").addColumn("type").build(param0);

        for(TickingBlockEntity var1 : this.blockEntityTickers) {
            BlockPos var2 = var1.getPos();
            var0.writeRow(var2.getX(), var2.getY(), var2.getZ(), var1.getType());
        }

    }

    @VisibleForTesting
    public void clearBlockEvents(BoundingBox param0) {
        this.blockEvents.removeIf(param1 -> param0.isInside(param1.getPos()));
    }

    @Override
    public void blockUpdated(BlockPos param0, Block param1) {
        if (!this.isDebug()) {
            this.updateNeighborsAt(param0, param1);
        }

    }

    @Override
    public float getShade(Direction param0, boolean param1) {
        return 1.0F;
    }

    public Iterable<Entity> getAllEntities() {
        return this.getEntities().getAll();
    }

    @Override
    public String toString() {
        return "ServerLevel[" + this.serverLevelData.getLevelName() + "]";
    }

    public boolean isFlat() {
        return this.server.getWorldData().worldGenSettings().isFlatWorld();
    }

    @Override
    public long getSeed() {
        return this.server.getWorldData().worldGenSettings().seed();
    }

    @Nullable
    public EndDragonFight dragonFight() {
        return this.dragonFight;
    }

    @Override
    public Stream<? extends StructureStart<?>> startsForFeature(SectionPos param0, StructureFeature<?> param1) {
        return this.structureFeatureManager().startsForFeature(param0, param1);
    }

    @Override
    public ServerLevel getLevel() {
        return this;
    }

    @VisibleForTesting
    public String getWatchdogStats() {
        return String.format(
            "players: %s, entities: %s [%s], block_entities: %d [%s], block_ticks: %d, fluid_ticks: %d, chunk_source: %s",
            this.players.size(),
            this.entityManager.gatherStats(),
            getTypeCount(this.entityManager.getEntityGetter().getAll(), param0 -> Registry.ENTITY_TYPE.getKey(param0.getType()).toString()),
            this.blockEntityTickers.size(),
            getTypeCount(this.blockEntityTickers, TickingBlockEntity::getType),
            this.getBlockTicks().size(),
            this.getLiquidTicks().size(),
            this.gatherChunkSourceStats()
        );
    }

    private static <T> String getTypeCount(Iterable<T> param0, Function<T, String> param1) {
        try {
            Object2IntOpenHashMap<String> var0 = new Object2IntOpenHashMap<>();

            for(T var1 : param0) {
                String var2 = param1.apply(var1);
                var0.addTo(var2, 1);
            }

            return var0.object2IntEntrySet()
                .stream()
                .sorted(Comparator.comparing(Entry::getIntValue).reversed())
                .limit(5L)
                .map(param0x -> (String)param0x.getKey() + ":" + param0x.getIntValue())
                .collect(Collectors.joining(","));
        } catch (Exception var6) {
            return "";
        }
    }

    public static void makeObsidianPlatform(ServerLevel param0) {
        BlockPos var0 = END_SPAWN_POINT;
        int var1 = var0.getX();
        int var2 = var0.getY() - 2;
        int var3 = var0.getZ();
        BlockPos.betweenClosed(var1 - 2, var2 + 1, var3 - 2, var1 + 2, var2 + 3, var3 + 2)
            .forEach(param1 -> param0.setBlockAndUpdate(param1, Blocks.AIR.defaultBlockState()));
        BlockPos.betweenClosed(var1 - 2, var2, var3 - 2, var1 + 2, var2, var3 + 2)
            .forEach(param1 -> param0.setBlockAndUpdate(param1, Blocks.OBSIDIAN.defaultBlockState()));
    }

    @Override
    protected LevelEntityGetter<Entity> getEntities() {
        return this.entityManager.getEntityGetter();
    }

    public void addLegacyChunkEntities(Stream<Entity> param0) {
        this.entityManager.addLegacyChunkEntities(param0);
    }

    public void addWorldGenChunkEntities(Stream<Entity> param0) {
        this.entityManager.addWorldGenChunkEntities(param0);
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.entityManager.close();
    }

    @Override
    public String gatherChunkSourceStats() {
        return "Chunks[S] W: " + this.chunkSource.gatherStats() + " E: " + this.entityManager.gatherStats();
    }

    final class EntityCallbacks implements LevelCallback<Entity> {
        public void onCreated(Entity param0) {
        }

        public void onDestroyed(Entity param0) {
            ServerLevel.this.getScoreboard().entityRemoved(param0);
        }

        public void onTickingStart(Entity param0) {
            ServerLevel.this.entityTickList.add(param0);
        }

        public void onTickingEnd(Entity param0) {
            ServerLevel.this.entityTickList.remove(param0);
        }

        public void onTrackingStart(Entity param0) {
            ServerLevel.this.getChunkSource().addEntity(param0);
            if (param0 instanceof ServerPlayer) {
                ServerLevel.this.players.add((ServerPlayer)param0);
                ServerLevel.this.updateSleepingPlayerList();
            }

            if (param0 instanceof Mob) {
                ServerLevel.this.navigatingMobs.add((Mob)param0);
            }

            if (param0 instanceof EnderDragon) {
                for(EnderDragonPart var0 : ((EnderDragon)param0).getSubEntities()) {
                    ServerLevel.this.dragonParts.put(var0.getId(), var0);
                }
            }

        }

        public void onTrackingEnd(Entity param0) {
            ServerLevel.this.getChunkSource().removeEntity(param0);
            if (param0 instanceof ServerPlayer var0) {
                ServerLevel.this.players.remove(var0);
                ServerLevel.this.updateSleepingPlayerList();
            }

            if (param0 instanceof Mob) {
                ServerLevel.this.navigatingMobs.remove(param0);
            }

            if (param0 instanceof EnderDragon) {
                for(EnderDragonPart var1 : ((EnderDragon)param0).getSubEntities()) {
                    ServerLevel.this.dragonParts.remove(var1.getId());
                }
            }

            GameEventListenerRegistrar var2 = param0.getGameEventListenerRegistrar();
            if (var2 != null) {
                var2.onListenerRemoved(param0.level);
            }

        }
    }
}
