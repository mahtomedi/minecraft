package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddGlobalEntityPacket;
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
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagManager;
import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.util.CsvOutput;
import net.minecraft.util.Mth;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
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
import net.minecraft.world.entity.global.LightningBolt;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.npc.WanderingTraderSpawner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raids;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.BlockEventData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelConflictException;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.PortalForcer;
import net.minecraft.world.level.ServerTickList;
import net.minecraft.world.level.TickNextTickData;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.saveddata.maps.MapIndex;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerLevel extends Level {
    private static final Logger LOGGER = LogManager.getLogger();
    private final List<Entity> globalEntities = Lists.newArrayList();
    private final Int2ObjectMap<Entity> entitiesById = new Int2ObjectLinkedOpenHashMap<>();
    private final Map<UUID, Entity> entitiesByUuid = Maps.newHashMap();
    private final Queue<Entity> toAddAfterTick = Queues.newArrayDeque();
    private final List<ServerPlayer> players = Lists.newArrayList();
    boolean tickingEntities;
    private final MinecraftServer server;
    private final LevelStorage levelStorage;
    public boolean noSave;
    private boolean allPlayersSleeping;
    private int emptyTime;
    private final PortalForcer portalForcer;
    private final ServerTickList<Block> blockTicks = new ServerTickList<>(
        this, param0x -> param0x == null || param0x.defaultBlockState().isAir(), Registry.BLOCK::getKey, Registry.BLOCK::get, this::tickBlock
    );
    private final ServerTickList<Fluid> liquidTicks = new ServerTickList<>(
        this, param0x -> param0x == null || param0x == Fluids.EMPTY, Registry.FLUID::getKey, Registry.FLUID::get, this::tickLiquid
    );
    private final Set<PathNavigation> navigations = Sets.newHashSet();
    protected final Raids raids;
    private final ObjectLinkedOpenHashSet<BlockEventData> blockEvents = new ObjectLinkedOpenHashSet<>();
    private boolean handlingTick;
    @Nullable
    private final WanderingTraderSpawner wanderingTraderSpawner;

    public ServerLevel(MinecraftServer param0, Executor param1, LevelStorage param2, LevelData param3, DimensionType param4, ChunkProgressListener param5) {
        super(
            param3,
            param4,
            (param4x, param5x) -> new ServerChunkCache(
                    (ServerLevel)param4x,
                    param2.getFolder(),
                    param2.getFixerUpper(),
                    param2.getStructureManager(),
                    param1,
                    param5x.createRandomLevelGenerator(),
                    param0.getPlayerList().getViewDistance(),
                    param5,
                    () -> param0.getLevel(DimensionType.OVERWORLD).getDataStorage()
                ),
            param0::getProfiler,
            false
        );
        this.levelStorage = param2;
        this.server = param0;
        this.portalForcer = new PortalForcer(this);
        this.updateSkyBrightness();
        this.prepareWeather();
        this.getWorldBorder().setAbsoluteMaxSize(param0.getAbsoluteMaxWorldSize());
        this.raids = this.getDataStorage().computeIfAbsent(() -> new Raids(this), Raids.getFileId(this.dimension));
        if (!param0.isSingleplayer()) {
            this.getLevelData().setGameType(param0.getDefaultGameType());
        }

        this.wanderingTraderSpawner = this.dimension.getType() == DimensionType.OVERWORLD ? new WanderingTraderSpawner(this) : null;
    }

    @Override
    public Biome getUncachedNoiseBiome(int param0, int param1, int param2) {
        return this.getChunkSource().getGenerator().getBiomeSource().getNoiseBiome(param0, param1, param2);
    }

    public void tick(BooleanSupplier param0) {
        ProfilerFiller var0 = this.getProfiler();
        this.handlingTick = true;
        var0.push("world border");
        this.getWorldBorder().tick();
        var0.popPush("weather");
        boolean var1 = this.isRaining();
        if (this.dimension.isHasSkyLight()) {
            if (this.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE)) {
                int var2 = this.levelData.getClearWeatherTime();
                int var3 = this.levelData.getThunderTime();
                int var4 = this.levelData.getRainTime();
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

                this.levelData.setThunderTime(var3);
                this.levelData.setRainTime(var4);
                this.levelData.setClearWeatherTime(var2);
                this.levelData.setThundering(var5);
                this.levelData.setRaining(var6);
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
            this.server.getPlayerList().broadcastAll(new ClientboundGameEventPacket(7, this.rainLevel), this.dimension.getType());
        }

        if (this.oThunderLevel != this.thunderLevel) {
            this.server.getPlayerList().broadcastAll(new ClientboundGameEventPacket(8, this.thunderLevel), this.dimension.getType());
        }

        if (var1 != this.isRaining()) {
            if (var1) {
                this.server.getPlayerList().broadcastAll(new ClientboundGameEventPacket(2, 0.0F));
            } else {
                this.server.getPlayerList().broadcastAll(new ClientboundGameEventPacket(1, 0.0F));
            }

            this.server.getPlayerList().broadcastAll(new ClientboundGameEventPacket(7, this.rainLevel));
            this.server.getPlayerList().broadcastAll(new ClientboundGameEventPacket(8, this.thunderLevel));
        }

        if (this.getLevelData().isHardcore() && this.getDifficulty() != Difficulty.HARD) {
            this.getLevelData().setDifficulty(Difficulty.HARD);
        }

        if (this.allPlayersSleeping && this.players.stream().noneMatch(param0x -> !param0x.isSpectator() && !param0x.isSleepingLongEnough())) {
            this.allPlayersSleeping = false;
            if (this.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
                long var7 = this.levelData.getDayTime() + 24000L;
                this.setDayTime(var7 - var7 % 24000L);
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
        if (this.levelData.getGeneratorType() != LevelType.DEBUG_ALL_BLOCK_STATES) {
            this.blockTicks.tick();
            this.liquidTicks.tick();
        }

        var0.popPush("raid");
        this.raids.tick();
        if (this.wanderingTraderSpawner != null) {
            this.wanderingTraderSpawner.tick();
        }

        var0.popPush("blockEvents");
        this.runBlockEvents();
        this.handlingTick = false;
        var0.popPush("entities");
        boolean var8 = !this.players.isEmpty() || !this.getForcedChunks().isEmpty();
        if (var8) {
            this.resetEmptyTime();
        }

        if (var8 || this.emptyTime++ < 300) {
            this.dimension.tick();
            var0.push("global");

            for(int var9 = 0; var9 < this.globalEntities.size(); ++var9) {
                Entity var10 = this.globalEntities.get(var9);
                this.guardEntityTick(param0x -> {
                    ++param0x.tickCount;
                    param0x.tick();
                }, var10);
                if (var10.removed) {
                    this.globalEntities.remove(var9--);
                }
            }

            var0.popPush("regular");
            this.tickingEntities = true;
            ObjectIterator<Entry<Entity>> var11 = this.entitiesById.int2ObjectEntrySet().iterator();

            label174:
            while(true) {
                Entity var13;
                while(true) {
                    if (!var11.hasNext()) {
                        this.tickingEntities = false;

                        Entity var15;
                        while((var15 = this.toAddAfterTick.poll()) != null) {
                            this.add(var15);
                        }

                        var0.pop();
                        this.tickBlockEntities();
                        break label174;
                    }

                    Entry<Entity> var12 = var11.next();
                    var13 = var12.getValue();
                    Entity var14 = var13.getVehicle();
                    if (!this.server.isAnimals() && (var13 instanceof Animal || var13 instanceof WaterAnimal)) {
                        var13.remove();
                    }

                    if (!this.server.isNpcsEnabled() && var13 instanceof Npc) {
                        var13.remove();
                    }

                    var0.push("checkDespawn");
                    if (!var13.removed) {
                        var13.checkDespawn();
                    }

                    var0.pop();
                    if (var14 == null) {
                        break;
                    }

                    if (var14.removed || !var14.hasPassenger(var13)) {
                        var13.stopRiding();
                        break;
                    }
                }

                var0.push("tick");
                if (!var13.removed && !(var13 instanceof EnderDragonPart)) {
                    this.guardEntityTick(this::tickNonPassenger, var13);
                }

                var0.pop();
                var0.push("remove");
                if (var13.removed) {
                    this.removeFromChunk(var13);
                    var11.remove();
                    this.onEntityRemoved(var13);
                }

                var0.pop();
            }
        }

        var0.pop();
    }

    private void wakeUpAllPlayers() {
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
            BlockPos var5 = this.findLightingTargetAround(this.getBlockRandomPos(var2, 0, var3, 15));
            if (this.isRainingAt(var5)) {
                DifficultyInstance var6 = this.getCurrentDifficultyAt(var5);
                boolean var7 = this.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)
                    && this.random.nextDouble() < (double)var6.getEffectiveDifficulty() * 0.01;
                if (var7) {
                    SkeletonHorse var8 = EntityType.SKELETON_HORSE.create(this);
                    var8.setTrap(true);
                    var8.setAge(0);
                    var8.setPos((double)var5.getX(), (double)var5.getY(), (double)var5.getZ());
                    this.addFreshEntity(var8);
                }

                this.addGlobalEntity(new LightningBolt(this, (double)var5.getX() + 0.5, (double)var5.getY(), (double)var5.getZ() + 0.5, var7));
            }
        }

        var4.popPush("iceandsnow");
        if (this.random.nextInt(16) == 0) {
            BlockPos var9 = this.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, this.getBlockRandomPos(var2, 0, var3, 15));
            BlockPos var10 = var9.below();
            Biome var11 = this.getBiome(var9);
            if (var11.shouldFreeze(this, var10)) {
                this.setBlockAndUpdate(var10, Blocks.ICE.defaultBlockState());
            }

            if (var1 && var11.shouldSnow(this, var9)) {
                this.setBlockAndUpdate(var9, Blocks.SNOW.defaultBlockState());
            }

            if (var1 && this.getBiome(var10).getPrecipitation() == Biome.Precipitation.RAIN) {
                this.getBlockState(var10).getBlock().handleRain(this, var10);
            }
        }

        var4.popPush("tickBlocks");
        if (param1 > 0) {
            for(LevelChunkSection var12 : param0.getSections()) {
                if (var12 != LevelChunk.EMPTY_SECTION && var12.isRandomlyTicking()) {
                    int var13 = var12.bottomBlockY();

                    for(int var14 = 0; var14 < param1; ++var14) {
                        BlockPos var15 = this.getBlockRandomPos(var2, var13, var3, 15);
                        var4.push("randomTick");
                        BlockState var16 = var12.getBlockState(var15.getX() - var2, var15.getY() - var13, var15.getZ() - var3);
                        if (var16.isRandomlyTicking()) {
                            var16.randomTick(this, var15, this.random);
                        }

                        FluidState var17 = var16.getFluidState();
                        if (var17.isRandomlyTicking()) {
                            var17.randomTick(this, var15, this.random);
                        }

                        var4.pop();
                    }
                }
            }
        }

        var4.pop();
    }

    protected BlockPos findLightingTargetAround(BlockPos param0) {
        BlockPos var0 = this.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, param0);
        AABB var1 = new AABB(var0, new BlockPos(var0.getX(), this.getMaxBuildHeight(), var0.getZ())).inflate(3.0);
        List<LivingEntity> var2 = this.getEntitiesOfClass(
            LivingEntity.class, var1, param0x -> param0x != null && param0x.isAlive() && this.canSeeSky(param0x.blockPosition())
        );
        if (!var2.isEmpty()) {
            return var2.get(this.random.nextInt(var2.size())).blockPosition();
        } else {
            if (var0.getY() == -1) {
                var0 = var0.above(2);
            }

            return var0;
        }
    }

    public boolean isHandlingTick() {
        return this.handlingTick;
    }

    public void updateSleepingPlayerList() {
        this.allPlayersSleeping = false;
        if (!this.players.isEmpty()) {
            int var0 = 0;
            int var1 = 0;

            for(ServerPlayer var2 : this.players) {
                if (var2.isSpectator()) {
                    ++var0;
                } else if (var2.isSleeping()) {
                    ++var1;
                }
            }

            this.allPlayersSleeping = var1 > 0 && var1 >= this.players.size() - var0;
        }

    }

    public ServerScoreboard getScoreboard() {
        return this.server.getScoreboard();
    }

    private void stopWeather() {
        this.levelData.setRainTime(0);
        this.levelData.setRaining(false);
        this.levelData.setThunderTime(0);
        this.levelData.setThundering(false);
    }

    public void resetEmptyTime() {
        this.emptyTime = 0;
    }

    private void tickLiquid(TickNextTickData<Fluid> param0x) {
        FluidState var0 = this.getFluidState(param0x.pos);
        if (var0.getType() == param0x.getType()) {
            var0.tick(this, param0x.pos);
        }

    }

    private void tickBlock(TickNextTickData<Block> param0x) {
        BlockState var0 = this.getBlockState(param0x.pos);
        if (var0.getBlock() == param0x.getType()) {
            var0.tick(this, param0x.pos, this.random);
        }

    }

    public void tickNonPassenger(Entity param0x) {
        if (param0x instanceof Player || this.getChunkSource().isEntityTickingChunk(param0x)) {
            param0x.setPosAndOldPos(param0x.getX(), param0x.getY(), param0x.getZ());
            param0x.yRotO = param0x.yRot;
            param0x.xRotO = param0x.xRot;
            if (param0x.inChunk) {
                ++param0x.tickCount;
                ProfilerFiller var0x = this.getProfiler();
                var0x.push(() -> Registry.ENTITY_TYPE.getKey(param0x.getType()).toString());
                var0x.incrementCounter("tickNonPassenger");
                param0x.tick();
                var0x.pop();
            }

            this.updateChunkPos(param0x);
            if (param0x.inChunk) {
                for(Entity var1x : param0x.getPassengers()) {
                    this.tickPassenger(param0x, var1x);
                }
            }

        }
    }

    public void tickPassenger(Entity param0, Entity param1) {
        if (param1.removed || param1.getVehicle() != param0) {
            param1.stopRiding();
        } else if (param1 instanceof Player || this.getChunkSource().isEntityTickingChunk(param1)) {
            param1.setPosAndOldPos(param1.getX(), param1.getY(), param1.getZ());
            param1.yRotO = param1.yRot;
            param1.xRotO = param1.xRot;
            if (param1.inChunk) {
                ++param1.tickCount;
                ProfilerFiller var0 = this.getProfiler();
                var0.push(() -> Registry.ENTITY_TYPE.getKey(param1.getType()).toString());
                var0.incrementCounter("tickPassenger");
                param1.rideTick();
                var0.pop();
            }

            this.updateChunkPos(param1);
            if (param1.inChunk) {
                for(Entity var1 : param1.getPassengers()) {
                    this.tickPassenger(param1, var1);
                }
            }

        }
    }

    public void updateChunkPos(Entity param0) {
        this.getProfiler().push("chunkCheck");
        int var0 = Mth.floor(param0.getX() / 16.0);
        int var1 = Mth.floor(param0.getY() / 16.0);
        int var2 = Mth.floor(param0.getZ() / 16.0);
        if (!param0.inChunk || param0.xChunk != var0 || param0.yChunk != var1 || param0.zChunk != var2) {
            if (param0.inChunk && this.hasChunk(param0.xChunk, param0.zChunk)) {
                this.getChunk(param0.xChunk, param0.zChunk).removeEntity(param0, param0.yChunk);
            }

            if (!param0.checkAndResetTeleportedFlag() && !this.hasChunk(var0, var2)) {
                param0.inChunk = false;
            } else {
                this.getChunk(var0, var2).addEntity(param0);
            }
        }

        this.getProfiler().pop();
    }

    @Override
    public boolean mayInteract(Player param0, BlockPos param1) {
        return !this.server.isUnderSpawnProtection(this, param1, param0) && this.getWorldBorder().isWithinBounds(param1);
    }

    public void setInitialSpawn(LevelSettings param0) {
        if (!this.dimension.mayRespawn()) {
            this.levelData.setSpawn(BlockPos.ZERO.above(this.getChunkSource().getGenerator().getSpawnHeight()));
        } else if (this.levelData.getGeneratorType() == LevelType.DEBUG_ALL_BLOCK_STATES) {
            this.levelData.setSpawn(BlockPos.ZERO.above());
        } else {
            BiomeSource var0 = this.getChunkSource().getGenerator().getBiomeSource();
            List<Biome> var1 = var0.getPlayerSpawnBiomes();
            Random var2 = new Random(this.getSeed());
            BlockPos var3 = var0.findBiomeHorizontal(0, this.getSeaLevel(), 0, 256, var1, var2);
            ChunkPos var4 = var3 == null ? new ChunkPos(0, 0) : new ChunkPos(var3);
            if (var3 == null) {
                LOGGER.warn("Unable to find spawn biome");
            }

            boolean var5 = false;

            for(Block var6 : BlockTags.VALID_SPAWN.getValues()) {
                if (var0.getSurfaceBlocks().contains(var6.defaultBlockState())) {
                    var5 = true;
                    break;
                }
            }

            this.levelData.setSpawn(var4.getWorldPosition().offset(8, this.getChunkSource().getGenerator().getSpawnHeight(), 8));
            int var7 = 0;
            int var8 = 0;
            int var9 = 0;
            int var10 = -1;
            int var11 = 32;

            for(int var12 = 0; var12 < 1024; ++var12) {
                if (var7 > -16 && var7 <= 16 && var8 > -16 && var8 <= 16) {
                    BlockPos var13 = this.dimension.getSpawnPosInChunk(new ChunkPos(var4.x + var7, var4.z + var8), var5);
                    if (var13 != null) {
                        this.levelData.setSpawn(var13);
                        break;
                    }
                }

                if (var7 == var8 || var7 < 0 && var7 == -var8 || var7 > 0 && var7 == 1 - var8) {
                    int var14 = var9;
                    var9 = -var10;
                    var10 = var14;
                }

                var7 += var9;
                var8 += var10;
            }

            if (param0.hasStartingBonusItems()) {
                this.generateBonusItemsNearSpawn();
            }

        }
    }

    protected void generateBonusItemsNearSpawn() {
        ConfiguredFeature<?, ?> var0 = Feature.BONUS_CHEST.configured(FeatureConfiguration.NONE);
        var0.place(
            this,
            this.getChunkSource().getGenerator(),
            this.random,
            new BlockPos(this.levelData.getXSpawn(), this.levelData.getYSpawn(), this.levelData.getZSpawn())
        );
    }

    @Nullable
    public BlockPos getDimensionSpecificSpawn() {
        return this.dimension.getDimensionSpecificSpawn();
    }

    public void save(@Nullable ProgressListener param0, boolean param1, boolean param2) throws LevelConflictException {
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
        }
    }

    protected void saveLevelData() throws LevelConflictException {
        this.checkSession();
        this.dimension.saveData();
        this.getChunkSource().getDataStorage().save();
    }

    public List<Entity> getEntities(@Nullable EntityType<?> param0, Predicate<? super Entity> param1) {
        List<Entity> var0 = Lists.newArrayList();
        ServerChunkCache var1 = this.getChunkSource();

        for(Entity var2 : this.entitiesById.values()) {
            if ((param0 == null || var2.getType() == param0) && var1.hasChunk(Mth.floor(var2.getX()) >> 4, Mth.floor(var2.getZ()) >> 4) && param1.test(var2)) {
                var0.add(var2);
            }
        }

        return var0;
    }

    public List<EnderDragon> getDragons() {
        List<EnderDragon> var0 = Lists.newArrayList();

        for(Entity var1 : this.entitiesById.values()) {
            if (var1 instanceof EnderDragon && var1.isAlive()) {
                var0.add((EnderDragon)var1);
            }
        }

        return var0;
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

    public Object2IntMap<MobCategory> getMobCategoryCounts() {
        Object2IntMap<MobCategory> var0 = new Object2IntOpenHashMap<>();
        ObjectIterator var2 = this.entitiesById.values().iterator();

        while(true) {
            Entity var1;
            Mob var2;
            do {
                if (!var2.hasNext()) {
                    return var0;
                }

                var1 = (Entity)var2.next();
                if (!(var1 instanceof Mob)) {
                    break;
                }

                var2 = (Mob)var1;
            } while(var2.isPersistenceRequired() || var2.requiresCustomPersistence());

            MobCategory var3 = var1.getType().getCategory();
            if (var3 != MobCategory.MISC && this.getChunkSource().isInAccessibleChunk(var1)) {
                var0.mergeInt(var3, 1, Integer::sum);
            }
        }
    }

    @Override
    public boolean addFreshEntity(Entity param0) {
        return this.addEntity(param0);
    }

    public boolean addWithUUID(Entity param0) {
        return this.addEntity(param0);
    }

    public void addFromAnotherDimension(Entity param0) {
        boolean var0 = param0.forcedLoading;
        param0.forcedLoading = true;
        this.addWithUUID(param0);
        param0.forcedLoading = var0;
        this.updateChunkPos(param0);
    }

    public void addDuringCommandTeleport(ServerPlayer param0) {
        this.addPlayer(param0);
        this.updateChunkPos(param0);
    }

    public void addDuringPortalTeleport(ServerPlayer param0) {
        this.addPlayer(param0);
        this.updateChunkPos(param0);
    }

    public void addNewPlayer(ServerPlayer param0) {
        this.addPlayer(param0);
    }

    public void addRespawnedPlayer(ServerPlayer param0) {
        this.addPlayer(param0);
    }

    private void addPlayer(ServerPlayer param0) {
        Entity var0 = this.entitiesByUuid.get(param0.getUUID());
        if (var0 != null) {
            LOGGER.warn("Force-added player with duplicate UUID {}", param0.getUUID().toString());
            var0.unRide();
            this.removePlayerImmediately((ServerPlayer)var0);
        }

        this.players.add(param0);
        this.updateSleepingPlayerList();
        ChunkAccess var1 = this.getChunk(Mth.floor(param0.getX() / 16.0), Mth.floor(param0.getZ() / 16.0), ChunkStatus.FULL, true);
        if (var1 instanceof LevelChunk) {
            var1.addEntity(param0);
        }

        this.add(param0);
    }

    private boolean addEntity(Entity param0) {
        if (param0.removed) {
            LOGGER.warn("Tried to add entity {} but it was marked as removed already", EntityType.getKey(param0.getType()));
            return false;
        } else if (this.isUUIDUsed(param0)) {
            return false;
        } else {
            ChunkAccess var0 = this.getChunk(Mth.floor(param0.getX() / 16.0), Mth.floor(param0.getZ() / 16.0), ChunkStatus.FULL, param0.forcedLoading);
            if (!(var0 instanceof LevelChunk)) {
                return false;
            } else {
                var0.addEntity(param0);
                this.add(param0);
                return true;
            }
        }
    }

    public boolean loadFromChunk(Entity param0) {
        if (this.isUUIDUsed(param0)) {
            return false;
        } else {
            this.add(param0);
            return true;
        }
    }

    private boolean isUUIDUsed(Entity param0) {
        Entity var0 = this.entitiesByUuid.get(param0.getUUID());
        if (var0 == null) {
            return false;
        } else {
            LOGGER.warn("Keeping entity {} that already exists with UUID {}", EntityType.getKey(var0.getType()), param0.getUUID().toString());
            return true;
        }
    }

    public void unload(LevelChunk param0) {
        this.blockEntitiesToUnload.addAll(param0.getBlockEntities().values());
        ClassInstanceMultiMap[] var2 = param0.getEntitySections();
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            for(Entity var1 : var2[var4]) {
                if (!(var1 instanceof ServerPlayer)) {
                    if (this.tickingEntities) {
                        throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("Removing entity while ticking!"));
                    }

                    this.entitiesById.remove(var1.getId());
                    this.onEntityRemoved(var1);
                }
            }
        }

    }

    public void onEntityRemoved(Entity param0) {
        if (param0 instanceof EnderDragon) {
            for(EnderDragonPart var0 : ((EnderDragon)param0).getSubEntities()) {
                var0.remove();
            }
        }

        this.entitiesByUuid.remove(param0.getUUID());
        this.getChunkSource().removeEntity(param0);
        if (param0 instanceof ServerPlayer) {
            ServerPlayer var1 = (ServerPlayer)param0;
            this.players.remove(var1);
        }

        this.getScoreboard().entityRemoved(param0);
        if (param0 instanceof Mob) {
            this.navigations.remove(((Mob)param0).getNavigation());
        }

    }

    private void add(Entity param0) {
        if (this.tickingEntities) {
            this.toAddAfterTick.add(param0);
        } else {
            this.entitiesById.put(param0.getId(), param0);
            if (param0 instanceof EnderDragon) {
                for(EnderDragonPart var0 : ((EnderDragon)param0).getSubEntities()) {
                    this.entitiesById.put(var0.getId(), var0);
                }
            }

            this.entitiesByUuid.put(param0.getUUID(), param0);
            this.getChunkSource().addEntity(param0);
            if (param0 instanceof Mob) {
                this.navigations.add(((Mob)param0).getNavigation());
            }
        }

    }

    public void despawn(Entity param0) {
        if (this.tickingEntities) {
            throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("Removing entity while ticking!"));
        } else {
            this.removeFromChunk(param0);
            this.entitiesById.remove(param0.getId());
            this.onEntityRemoved(param0);
        }
    }

    private void removeFromChunk(Entity param0) {
        ChunkAccess var0 = this.getChunk(param0.xChunk, param0.zChunk, ChunkStatus.FULL, false);
        if (var0 instanceof LevelChunk) {
            ((LevelChunk)var0).removeEntity(param0);
        }

    }

    public void removePlayerImmediately(ServerPlayer param0) {
        param0.remove();
        this.despawn(param0);
        this.updateSleepingPlayerList();
    }

    public void addGlobalEntity(LightningBolt param0) {
        this.globalEntities.add(param0);
        this.server
            .getPlayerList()
            .broadcast(null, param0.getX(), param0.getY(), param0.getZ(), 512.0, this.dimension.getType(), new ClientboundAddGlobalEntityPacket(param0));
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
                this.dimension.getType(),
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
                this.dimension.getType(),
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
                this.dimension.getType(),
                new ClientboundLevelEventPacket(param1, param2, param3, false)
            );
    }

    @Override
    public void sendBlockUpdated(BlockPos param0, BlockState param1, BlockState param2, int param3) {
        this.getChunkSource().blockChanged(param0);
        VoxelShape var0 = param1.getCollisionShape(this, param0);
        VoxelShape var1 = param2.getCollisionShape(this, param0);
        if (Shapes.joinIsNotEmpty(var0, var1, BooleanOp.NOT_SAME)) {
            for(PathNavigation var2 : this.navigations) {
                if (!var2.hasDelayedRecomputation()) {
                    var2.recomputePath(param0);
                }
            }

        }
    }

    @Override
    public void broadcastEntityEvent(Entity param0, byte param1) {
        this.getChunkSource().broadcastAndSend(param0, new ClientboundEntityEventPacket(param0, param1));
    }

    public ServerChunkCache getChunkSource() {
        return (ServerChunkCache)super.getChunkSource();
    }

    @Override
    public Explosion explode(
        @Nullable Entity param0,
        @Nullable DamageSource param1,
        double param2,
        double param3,
        double param4,
        float param5,
        boolean param6,
        Explosion.BlockInteraction param7
    ) {
        Explosion var0 = new Explosion(this, param0, param2, param3, param4, param5, param6, param7);
        if (param1 != null) {
            var0.setDamageSource(param1);
        }

        var0.explode();
        var0.finalizeExplosion(false);
        if (param7 == Explosion.BlockInteraction.NONE) {
            var0.clearToBlow();
        }

        for(ServerPlayer var1 : this.players) {
            if (var1.distanceToSqr(param2, param3, param4) < 4096.0) {
                var1.connection.send(new ClientboundExplodePacket(param2, param3, param4, param5, var0.getToBlow(), var0.getHitPlayers().get(var1)));
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
                        this.dimension.getType(),
                        new ClientboundBlockEventPacket(var0.getPos(), var0.getBlock(), var0.getParamA(), var0.getParamB())
                    );
            }
        }

    }

    private boolean doBlockEvent(BlockEventData param0) {
        BlockState var0 = this.getBlockState(param0.getPos());
        return var0.getBlock() == param0.getBlock() ? var0.triggerEvent(this, param0.getPos(), param0.getParamA(), param0.getParamB()) : false;
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
        return this.levelStorage.getStructureManager();
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
        return this.entitiesById.get(param0);
    }

    @Nullable
    public Entity getEntity(UUID param0) {
        return this.entitiesByUuid.get(param0);
    }

    @Nullable
    public BlockPos findNearestMapFeature(String param0, BlockPos param1, int param2, boolean param3) {
        return this.getChunkSource().getGenerator().findNearestMapFeature(this, param0, param1, param2, param3);
    }

    @Nullable
    public BlockPos findNearestBiome(Biome param0, BlockPos param1, int param2, int param3) {
        return this.getChunkSource()
            .getGenerator()
            .getBiomeSource()
            .findBiomeHorizontal(param1.getX(), param1.getY(), param1.getZ(), param2, param3, ImmutableList.of(param0), this.random, true);
    }

    @Override
    public RecipeManager getRecipeManager() {
        return this.server.getRecipeManager();
    }

    @Override
    public TagManager getTagManager() {
        return this.server.getTags();
    }

    @Override
    public void setGameTime(long param0) {
        super.setGameTime(param0);
        this.levelData.getScheduledEvents().tick(this.server, param0);
    }

    @Override
    public boolean noSave() {
        return this.noSave;
    }

    public void checkSession() throws LevelConflictException {
        this.levelStorage.checkSession();
    }

    public LevelStorage getLevelStorage() {
        return this.levelStorage;
    }

    public DimensionDataStorage getDataStorage() {
        return this.getChunkSource().getDataStorage();
    }

    @Nullable
    @Override
    public MapItemSavedData getMapData(String param0) {
        return this.getServer().getLevel(DimensionType.OVERWORLD).getDataStorage().get(() -> new MapItemSavedData(param0), param0);
    }

    @Override
    public void setMapData(MapItemSavedData param0) {
        this.getServer().getLevel(DimensionType.OVERWORLD).getDataStorage().set(param0);
    }

    @Override
    public int getFreeMapId() {
        return this.getServer().getLevel(DimensionType.OVERWORLD).getDataStorage().computeIfAbsent(MapIndex::new, "idcounts").getFreeAuxValueForMap();
    }

    @Override
    public void setDefaultSpawnPos(BlockPos param0) {
        ChunkPos var0 = new ChunkPos(new BlockPos(this.levelData.getXSpawn(), 0, this.levelData.getZSpawn()));
        super.setDefaultSpawnPos(param0);
        this.getChunkSource().removeRegionTicket(TicketType.START, var0, 11, Unit.INSTANCE);
        this.getChunkSource().addRegionTicket(TicketType.START, new ChunkPos(param0), 11, Unit.INSTANCE);
        this.getServer().getPlayerList().broadcastAll(new ClientboundSetDefaultSpawnPositionPacket(param0));
    }

    public LongSet getForcedChunks() {
        ForcedChunksSavedData var0 = this.getDataStorage().get(ForcedChunksSavedData::new, "chunks");
        return (LongSet)(var0 != null ? LongSets.unmodifiable(var0.getChunks()) : LongSets.EMPTY_SET);
    }

    public boolean setChunkForced(int param0, int param1, boolean param2) {
        ForcedChunksSavedData var0 = this.getDataStorage().computeIfAbsent(ForcedChunksSavedData::new, "chunks");
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
        return this.closeToVillage(param0, 1);
    }

    public boolean isVillage(SectionPos param0) {
        return this.isVillage(param0.center());
    }

    public boolean closeToVillage(BlockPos param0, int param1) {
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

            for(it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<MobCategory> var2 : this.getMobCategoryCounts().object2IntEntrySet()) {
                var1.write(String.format("spawn_count.%s: %d\n", var2.getKey().getName(), var2.getIntValue()));
            }

            var1.write(String.format("entities: %d\n", this.entitiesById.size()));
            var1.write(String.format("block_entities: %d\n", this.blockEntityList.size()));
            var1.write(String.format("block_ticks: %d\n", this.getBlockTicks().size()));
            var1.write(String.format("fluid_ticks: %d\n", this.getLiquidTicks().size()));
            var1.write("distance_manager: " + var0.getDistanceManager().getDebugStatus() + "\n");
            var1.write(String.format("pending_tasks: %d\n", this.getChunkSource().getPendingTasksCount()));
        }

        CrashReport var3 = new CrashReport("Level dump", new Exception("dummy"));
        this.fillReportDetails(var3);

        try (Writer var4 = Files.newBufferedWriter(param0.resolve("example_crash.txt"))) {
            var4.write(var3.getFriendlyReport());
        }

        Path var5 = param0.resolve("chunks.csv");

        try (Writer var6 = Files.newBufferedWriter(var5)) {
            var0.dumpChunks(var6);
        }

        Path var7 = param0.resolve("entities.csv");

        try (Writer var8 = Files.newBufferedWriter(var7)) {
            dumpEntities(var8, this.entitiesById.values());
        }

        Path var9 = param0.resolve("global_entities.csv");

        try (Writer var10 = Files.newBufferedWriter(var9)) {
            dumpEntities(var10, this.globalEntities);
        }

        Path var11 = param0.resolve("block_entities.csv");

        try (Writer var12 = Files.newBufferedWriter(var11)) {
            this.dumpBlockEntities(var12);
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

    private void dumpBlockEntities(Writer param0) throws IOException {
        CsvOutput var0 = CsvOutput.builder().addColumn("x").addColumn("y").addColumn("z").addColumn("type").build(param0);

        for(BlockEntity var1 : this.blockEntityList) {
            BlockPos var2 = var1.getBlockPos();
            var0.writeRow(var2.getX(), var2.getY(), var2.getZ(), Registry.BLOCK_ENTITY_TYPE.getKey(var1.getType()));
        }

    }

    @VisibleForTesting
    public void clearBlockEvents(BoundingBox param0) {
        this.blockEvents.removeIf(param1 -> param0.isInside(param1.getPos()));
    }

    @Override
    public void blockUpdated(BlockPos param0, Block param1) {
        if (this.levelData.getGeneratorType() != LevelType.DEBUG_ALL_BLOCK_STATES) {
            this.updateNeighborsAt(param0, param1);
        }

    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public float getShade(Direction param0, boolean param1) {
        return 1.0F;
    }
}
