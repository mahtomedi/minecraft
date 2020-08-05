package net.minecraft.client.multiplayer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintCache;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagContainer;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.EmptyTickList;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientLevel extends Level {
    private final Int2ObjectMap<Entity> entitiesById = new Int2ObjectOpenHashMap<>();
    private final ClientPacketListener connection;
    private final LevelRenderer levelRenderer;
    private final ClientLevel.ClientLevelData clientLevelData;
    private final DimensionSpecialEffects effects;
    private final Minecraft minecraft = Minecraft.getInstance();
    private final List<AbstractClientPlayer> players = Lists.newArrayList();
    private Scoreboard scoreboard = new Scoreboard();
    private final Map<String, MapItemSavedData> mapData = Maps.newHashMap();
    private int skyFlashTime;
    private final Object2ObjectArrayMap<ColorResolver, BlockTintCache> tintCaches = Util.make(new Object2ObjectArrayMap<>(3), param0x -> {
        param0x.put(BiomeColors.GRASS_COLOR_RESOLVER, new BlockTintCache());
        param0x.put(BiomeColors.FOLIAGE_COLOR_RESOLVER, new BlockTintCache());
        param0x.put(BiomeColors.WATER_COLOR_RESOLVER, new BlockTintCache());
    });
    private final ClientChunkCache chunkSource;

    public ClientLevel(
        ClientPacketListener param0,
        ClientLevel.ClientLevelData param1,
        ResourceKey<Level> param2,
        ResourceKey<DimensionType> param3,
        DimensionType param4,
        int param5,
        Supplier<ProfilerFiller> param6,
        LevelRenderer param7,
        boolean param8,
        long param9
    ) {
        super(param1, param2, param3, param4, param6, true, param8, param9);
        this.connection = param0;
        this.chunkSource = new ClientChunkCache(this, param5);
        this.clientLevelData = param1;
        this.levelRenderer = param7;
        this.effects = DimensionSpecialEffects.forType(param0.registryAccess().dimensionTypes().getResourceKey(param4));
        this.setDefaultSpawnPos(new BlockPos(8, 64, 8), 0.0F);
        this.updateSkyBrightness();
        this.prepareWeather();
    }

    public DimensionSpecialEffects effects() {
        return this.effects;
    }

    public void tick(BooleanSupplier param0) {
        this.getWorldBorder().tick();
        this.tickTime();
        this.getProfiler().push("blocks");
        this.chunkSource.tick(param0);
        this.getProfiler().pop();
    }

    private void tickTime() {
        this.setGameTime(this.levelData.getGameTime() + 1L);
        if (this.levelData.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
            this.setDayTime(this.levelData.getDayTime() + 1L);
        }

    }

    public void setGameTime(long param0) {
        this.clientLevelData.setGameTime(param0);
    }

    public void setDayTime(long param0) {
        if (param0 < 0L) {
            param0 = -param0;
            this.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, null);
        } else {
            this.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(true, null);
        }

        this.clientLevelData.setDayTime(param0);
    }

    public Iterable<Entity> entitiesForRendering() {
        return this.entitiesById.values();
    }

    public void tickEntities() {
        ProfilerFiller var0 = this.getProfiler();
        var0.push("entities");
        ObjectIterator<Entry<Entity>> var1 = this.entitiesById.int2ObjectEntrySet().iterator();

        while(var1.hasNext()) {
            Entry<Entity> var2 = var1.next();
            Entity var3 = var2.getValue();
            if (!var3.isPassenger()) {
                var0.push("tick");
                if (!var3.removed) {
                    this.guardEntityTick(this::tickNonPassenger, var3);
                }

                var0.pop();
                var0.push("remove");
                if (var3.removed) {
                    var1.remove();
                    this.onEntityRemoved(var3);
                }

                var0.pop();
            }
        }

        this.tickBlockEntities();
        var0.pop();
    }

    public void tickNonPassenger(Entity param0) {
        if (!(param0 instanceof Player) && !this.getChunkSource().isEntityTickingChunk(param0)) {
            this.updateChunkPos(param0);
        } else {
            param0.setPosAndOldPos(param0.getX(), param0.getY(), param0.getZ());
            param0.yRotO = param0.yRot;
            param0.xRotO = param0.xRot;
            if (param0.inChunk || param0.isSpectator()) {
                ++param0.tickCount;
                this.getProfiler().push(() -> Registry.ENTITY_TYPE.getKey(param0.getType()).toString());
                param0.tick();
                this.getProfiler().pop();
            }

            this.updateChunkPos(param0);
            if (param0.inChunk) {
                for(Entity var0x : param0.getPassengers()) {
                    this.tickPassenger(param0, var0x);
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
                param1.rideTick();
            }

            this.updateChunkPos(param1);
            if (param1.inChunk) {
                for(Entity var0 : param1.getPassengers()) {
                    this.tickPassenger(param1, var0);
                }
            }

        }
    }

    private void updateChunkPos(Entity param0) {
        if (param0.checkAndResetUpdateChunkPos()) {
            this.getProfiler().push("chunkCheck");
            int var0 = Mth.floor(param0.getX() / 16.0);
            int var1 = Mth.floor(param0.getY() / 16.0);
            int var2 = Mth.floor(param0.getZ() / 16.0);
            if (!param0.inChunk || param0.xChunk != var0 || param0.yChunk != var1 || param0.zChunk != var2) {
                if (param0.inChunk && this.hasChunk(param0.xChunk, param0.zChunk)) {
                    this.getChunk(param0.xChunk, param0.zChunk).removeEntity(param0, param0.yChunk);
                }

                if (!param0.checkAndResetForcedChunkAdditionFlag() && !this.hasChunk(var0, var2)) {
                    if (param0.inChunk) {
                        LOGGER.warn("Entity {} left loaded chunk area", param0);
                    }

                    param0.inChunk = false;
                } else {
                    this.getChunk(var0, var2).addEntity(param0);
                }
            }

            this.getProfiler().pop();
        }
    }

    public void unload(LevelChunk param0) {
        this.blockEntitiesToUnload.addAll(param0.getBlockEntities().values());
        this.chunkSource.getLightEngine().enableLightSources(param0.getPos(), false);
    }

    public void onChunkLoaded(int param0, int param1) {
        this.tintCaches.forEach((param2, param3) -> param3.invalidateForChunk(param0, param1));
    }

    public void clearTintCaches() {
        this.tintCaches.forEach((param0, param1) -> param1.invalidateAll());
    }

    @Override
    public boolean hasChunk(int param0, int param1) {
        return true;
    }

    public int getEntityCount() {
        return this.entitiesById.size();
    }

    public void addPlayer(int param0, AbstractClientPlayer param1) {
        this.addEntity(param0, param1);
        this.players.add(param1);
    }

    public void putNonPlayerEntity(int param0, Entity param1) {
        this.addEntity(param0, param1);
    }

    private void addEntity(int param0, Entity param1) {
        this.removeEntity(param0);
        this.entitiesById.put(param0, param1);
        this.getChunkSource().getChunk(Mth.floor(param1.getX() / 16.0), Mth.floor(param1.getZ() / 16.0), ChunkStatus.FULL, true).addEntity(param1);
    }

    public void removeEntity(int param0) {
        Entity var0 = this.entitiesById.remove(param0);
        if (var0 != null) {
            var0.remove();
            this.onEntityRemoved(var0);
        }

    }

    private void onEntityRemoved(Entity param0) {
        param0.unRide();
        if (param0.inChunk) {
            this.getChunk(param0.xChunk, param0.zChunk).removeEntity(param0);
        }

        this.players.remove(param0);
    }

    public void reAddEntitiesToChunk(LevelChunk param0) {
        for(Entry<Entity> var0 : this.entitiesById.int2ObjectEntrySet()) {
            Entity var1 = var0.getValue();
            int var2 = Mth.floor(var1.getX() / 16.0);
            int var3 = Mth.floor(var1.getZ() / 16.0);
            if (var2 == param0.getPos().x && var3 == param0.getPos().z) {
                param0.addEntity(var1);
            }
        }

    }

    @Nullable
    @Override
    public Entity getEntity(int param0) {
        return this.entitiesById.get(param0);
    }

    public void setKnownState(BlockPos param0, BlockState param1) {
        this.setBlock(param0, param1, 19);
    }

    @Override
    public void disconnect() {
        this.connection.getConnection().disconnect(new TranslatableComponent("multiplayer.status.quitting"));
    }

    public void animateTick(int param0, int param1, int param2) {
        int var0 = 32;
        Random var1 = new Random();
        boolean var2 = false;
        if (this.minecraft.gameMode.getPlayerMode() == GameType.CREATIVE) {
            for(ItemStack var3 : this.minecraft.player.getHandSlots()) {
                if (var3.getItem() == Blocks.BARRIER.asItem()) {
                    var2 = true;
                    break;
                }
            }
        }

        BlockPos.MutableBlockPos var4 = new BlockPos.MutableBlockPos();

        for(int var5 = 0; var5 < 667; ++var5) {
            this.doAnimateTick(param0, param1, param2, 16, var1, var2, var4);
            this.doAnimateTick(param0, param1, param2, 32, var1, var2, var4);
        }

    }

    public void doAnimateTick(int param0, int param1, int param2, int param3, Random param4, boolean param5, BlockPos.MutableBlockPos param6) {
        int var0 = param0 + this.random.nextInt(param3) - this.random.nextInt(param3);
        int var1 = param1 + this.random.nextInt(param3) - this.random.nextInt(param3);
        int var2 = param2 + this.random.nextInt(param3) - this.random.nextInt(param3);
        param6.set(var0, var1, var2);
        BlockState var3 = this.getBlockState(param6);
        var3.getBlock().animateTick(var3, this, param6, param4);
        FluidState var4 = this.getFluidState(param6);
        if (!var4.isEmpty()) {
            var4.animateTick(this, param6, param4);
            ParticleOptions var5 = var4.getDripParticle();
            if (var5 != null && this.random.nextInt(10) == 0) {
                boolean var6 = var3.isFaceSturdy(this, param6, Direction.DOWN);
                BlockPos var7 = param6.below();
                this.trySpawnDripParticles(var7, this.getBlockState(var7), var5, var6);
            }
        }

        if (param5 && var3.is(Blocks.BARRIER)) {
            this.addParticle(ParticleTypes.BARRIER, (double)var0 + 0.5, (double)var1 + 0.5, (double)var2 + 0.5, 0.0, 0.0, 0.0);
        }

        if (!var3.isCollisionShapeFullBlock(this, param6)) {
            this.getBiome(param6)
                .getAmbientParticle()
                .ifPresent(
                    param1x -> {
                        if (param1x.canSpawn(this.random)) {
                            this.addParticle(
                                param1x.getOptions(),
                                (double)param6.getX() + this.random.nextDouble(),
                                (double)param6.getY() + this.random.nextDouble(),
                                (double)param6.getZ() + this.random.nextDouble(),
                                0.0,
                                0.0,
                                0.0
                            );
                        }
        
                    }
                );
        }

    }

    private void trySpawnDripParticles(BlockPos param0, BlockState param1, ParticleOptions param2, boolean param3) {
        if (param1.getFluidState().isEmpty()) {
            VoxelShape var0 = param1.getCollisionShape(this, param0);
            double var1 = var0.max(Direction.Axis.Y);
            if (var1 < 1.0) {
                if (param3) {
                    this.spawnFluidParticle(
                        (double)param0.getX(),
                        (double)(param0.getX() + 1),
                        (double)param0.getZ(),
                        (double)(param0.getZ() + 1),
                        (double)(param0.getY() + 1) - 0.05,
                        param2
                    );
                }
            } else if (!param1.is(BlockTags.IMPERMEABLE)) {
                double var2 = var0.min(Direction.Axis.Y);
                if (var2 > 0.0) {
                    this.spawnParticle(param0, param2, var0, (double)param0.getY() + var2 - 0.05);
                } else {
                    BlockPos var3 = param0.below();
                    BlockState var4 = this.getBlockState(var3);
                    VoxelShape var5 = var4.getCollisionShape(this, var3);
                    double var6 = var5.max(Direction.Axis.Y);
                    if (var6 < 1.0 && var4.getFluidState().isEmpty()) {
                        this.spawnParticle(param0, param2, var0, (double)param0.getY() - 0.05);
                    }
                }
            }

        }
    }

    private void spawnParticle(BlockPos param0, ParticleOptions param1, VoxelShape param2, double param3) {
        this.spawnFluidParticle(
            (double)param0.getX() + param2.min(Direction.Axis.X),
            (double)param0.getX() + param2.max(Direction.Axis.X),
            (double)param0.getZ() + param2.min(Direction.Axis.Z),
            (double)param0.getZ() + param2.max(Direction.Axis.Z),
            param3,
            param1
        );
    }

    private void spawnFluidParticle(double param0, double param1, double param2, double param3, double param4, ParticleOptions param5) {
        this.addParticle(param5, Mth.lerp(this.random.nextDouble(), param0, param1), param4, Mth.lerp(this.random.nextDouble(), param2, param3), 0.0, 0.0, 0.0);
    }

    public void removeAllPendingEntityRemovals() {
        ObjectIterator<Entry<Entity>> var0 = this.entitiesById.int2ObjectEntrySet().iterator();

        while(var0.hasNext()) {
            Entry<Entity> var1 = var0.next();
            Entity var2 = var1.getValue();
            if (var2.removed) {
                var0.remove();
                this.onEntityRemoved(var2);
            }
        }

    }

    @Override
    public CrashReportCategory fillReportDetails(CrashReport param0) {
        CrashReportCategory var0 = super.fillReportDetails(param0);
        var0.setDetail("Server brand", () -> this.minecraft.player.getServerBrand());
        var0.setDetail(
            "Server type", () -> this.minecraft.getSingleplayerServer() == null ? "Non-integrated multiplayer server" : "Integrated singleplayer server"
        );
        return var0;
    }

    @Override
    public void playSound(
        @Nullable Player param0, double param1, double param2, double param3, SoundEvent param4, SoundSource param5, float param6, float param7
    ) {
        if (param0 == this.minecraft.player) {
            this.playLocalSound(param1, param2, param3, param4, param5, param6, param7, false);
        }

    }

    @Override
    public void playSound(@Nullable Player param0, Entity param1, SoundEvent param2, SoundSource param3, float param4, float param5) {
        if (param0 == this.minecraft.player) {
            this.minecraft.getSoundManager().play(new EntityBoundSoundInstance(param2, param3, param1));
        }

    }

    public void playLocalSound(BlockPos param0, SoundEvent param1, SoundSource param2, float param3, float param4, boolean param5) {
        this.playLocalSound((double)param0.getX() + 0.5, (double)param0.getY() + 0.5, (double)param0.getZ() + 0.5, param1, param2, param3, param4, param5);
    }

    @Override
    public void playLocalSound(double param0, double param1, double param2, SoundEvent param3, SoundSource param4, float param5, float param6, boolean param7) {
        double var0 = this.minecraft.gameRenderer.getMainCamera().getPosition().distanceToSqr(param0, param1, param2);
        SimpleSoundInstance var1 = new SimpleSoundInstance(param3, param4, param5, param6, param0, param1, param2);
        if (param7 && var0 > 100.0) {
            double var2 = Math.sqrt(var0) / 40.0;
            this.minecraft.getSoundManager().playDelayed(var1, (int)(var2 * 20.0));
        } else {
            this.minecraft.getSoundManager().play(var1);
        }

    }

    @Override
    public void createFireworks(double param0, double param1, double param2, double param3, double param4, double param5, @Nullable CompoundTag param6) {
        this.minecraft
            .particleEngine
            .add(new FireworkParticles.Starter(this, param0, param1, param2, param3, param4, param5, this.minecraft.particleEngine, param6));
    }

    @Override
    public void sendPacketToServer(Packet<?> param0) {
        this.connection.send(param0);
    }

    @Override
    public RecipeManager getRecipeManager() {
        return this.connection.getRecipeManager();
    }

    public void setScoreboard(Scoreboard param0) {
        this.scoreboard = param0;
    }

    @Override
    public TickList<Block> getBlockTicks() {
        return EmptyTickList.empty();
    }

    @Override
    public TickList<Fluid> getLiquidTicks() {
        return EmptyTickList.empty();
    }

    public ClientChunkCache getChunkSource() {
        return this.chunkSource;
    }

    @Nullable
    @Override
    public MapItemSavedData getMapData(String param0) {
        return this.mapData.get(param0);
    }

    @Override
    public void setMapData(MapItemSavedData param0) {
        this.mapData.put(param0.getId(), param0);
    }

    @Override
    public int getFreeMapId() {
        return 0;
    }

    @Override
    public Scoreboard getScoreboard() {
        return this.scoreboard;
    }

    @Override
    public TagContainer getTagManager() {
        return this.connection.getTags();
    }

    @Override
    public RegistryAccess registryAccess() {
        return this.connection.registryAccess();
    }

    @Override
    public void sendBlockUpdated(BlockPos param0, BlockState param1, BlockState param2, int param3) {
        this.levelRenderer.blockChanged(this, param0, param1, param2, param3);
    }

    @Override
    public void setBlocksDirty(BlockPos param0, BlockState param1, BlockState param2) {
        this.levelRenderer.setBlockDirty(param0, param1, param2);
    }

    public void setSectionDirtyWithNeighbors(int param0, int param1, int param2) {
        this.levelRenderer.setSectionDirtyWithNeighbors(param0, param1, param2);
    }

    @Override
    public void destroyBlockProgress(int param0, BlockPos param1, int param2) {
        this.levelRenderer.destroyBlockProgress(param0, param1, param2);
    }

    @Override
    public void globalLevelEvent(int param0, BlockPos param1, int param2) {
        this.levelRenderer.globalLevelEvent(param0, param1, param2);
    }

    @Override
    public void levelEvent(@Nullable Player param0, int param1, BlockPos param2, int param3) {
        try {
            this.levelRenderer.levelEvent(param0, param1, param2, param3);
        } catch (Throwable var8) {
            CrashReport var1 = CrashReport.forThrowable(var8, "Playing level event");
            CrashReportCategory var2 = var1.addCategory("Level event being played");
            var2.setDetail("Block coordinates", CrashReportCategory.formatLocation(param2));
            var2.setDetail("Event source", param0);
            var2.setDetail("Event type", param1);
            var2.setDetail("Event data", param3);
            throw new ReportedException(var1);
        }
    }

    @Override
    public void addParticle(ParticleOptions param0, double param1, double param2, double param3, double param4, double param5, double param6) {
        this.levelRenderer.addParticle(param0, param0.getType().getOverrideLimiter(), param1, param2, param3, param4, param5, param6);
    }

    @Override
    public void addParticle(ParticleOptions param0, boolean param1, double param2, double param3, double param4, double param5, double param6, double param7) {
        this.levelRenderer.addParticle(param0, param0.getType().getOverrideLimiter() || param1, param2, param3, param4, param5, param6, param7);
    }

    @Override
    public void addAlwaysVisibleParticle(ParticleOptions param0, double param1, double param2, double param3, double param4, double param5, double param6) {
        this.levelRenderer.addParticle(param0, false, true, param1, param2, param3, param4, param5, param6);
    }

    @Override
    public void addAlwaysVisibleParticle(
        ParticleOptions param0, boolean param1, double param2, double param3, double param4, double param5, double param6, double param7
    ) {
        this.levelRenderer.addParticle(param0, param0.getType().getOverrideLimiter() || param1, true, param2, param3, param4, param5, param6, param7);
    }

    @Override
    public List<AbstractClientPlayer> players() {
        return this.players;
    }

    @Override
    public Biome getUncachedNoiseBiome(int param0, int param1, int param2) {
        return this.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getOrThrow(Biomes.PLAINS);
    }

    public float getSkyDarken(float param0) {
        float var0 = this.getTimeOfDay(param0);
        float var1 = 1.0F - (Mth.cos(var0 * (float) (Math.PI * 2)) * 2.0F + 0.2F);
        var1 = Mth.clamp(var1, 0.0F, 1.0F);
        var1 = 1.0F - var1;
        var1 = (float)((double)var1 * (1.0 - (double)(this.getRainLevel(param0) * 5.0F) / 16.0));
        var1 = (float)((double)var1 * (1.0 - (double)(this.getThunderLevel(param0) * 5.0F) / 16.0));
        return var1 * 0.8F + 0.2F;
    }

    public Vec3 getSkyColor(BlockPos param0, float param1) {
        float var0 = this.getTimeOfDay(param1);
        float var1 = Mth.cos(var0 * (float) (Math.PI * 2)) * 2.0F + 0.5F;
        var1 = Mth.clamp(var1, 0.0F, 1.0F);
        Biome var2 = this.getBiome(param0);
        int var3 = var2.getSkyColor();
        float var4 = (float)(var3 >> 16 & 0xFF) / 255.0F;
        float var5 = (float)(var3 >> 8 & 0xFF) / 255.0F;
        float var6 = (float)(var3 & 0xFF) / 255.0F;
        var4 *= var1;
        var5 *= var1;
        var6 *= var1;
        float var7 = this.getRainLevel(param1);
        if (var7 > 0.0F) {
            float var8 = (var4 * 0.3F + var5 * 0.59F + var6 * 0.11F) * 0.6F;
            float var9 = 1.0F - var7 * 0.75F;
            var4 = var4 * var9 + var8 * (1.0F - var9);
            var5 = var5 * var9 + var8 * (1.0F - var9);
            var6 = var6 * var9 + var8 * (1.0F - var9);
        }

        float var10 = this.getThunderLevel(param1);
        if (var10 > 0.0F) {
            float var11 = (var4 * 0.3F + var5 * 0.59F + var6 * 0.11F) * 0.2F;
            float var12 = 1.0F - var10 * 0.75F;
            var4 = var4 * var12 + var11 * (1.0F - var12);
            var5 = var5 * var12 + var11 * (1.0F - var12);
            var6 = var6 * var12 + var11 * (1.0F - var12);
        }

        if (this.skyFlashTime > 0) {
            float var13 = (float)this.skyFlashTime - param1;
            if (var13 > 1.0F) {
                var13 = 1.0F;
            }

            var13 *= 0.45F;
            var4 = var4 * (1.0F - var13) + 0.8F * var13;
            var5 = var5 * (1.0F - var13) + 0.8F * var13;
            var6 = var6 * (1.0F - var13) + 1.0F * var13;
        }

        return new Vec3((double)var4, (double)var5, (double)var6);
    }

    public Vec3 getCloudColor(float param0) {
        float var0 = this.getTimeOfDay(param0);
        float var1 = Mth.cos(var0 * (float) (Math.PI * 2)) * 2.0F + 0.5F;
        var1 = Mth.clamp(var1, 0.0F, 1.0F);
        float var2 = 1.0F;
        float var3 = 1.0F;
        float var4 = 1.0F;
        float var5 = this.getRainLevel(param0);
        if (var5 > 0.0F) {
            float var6 = (var2 * 0.3F + var3 * 0.59F + var4 * 0.11F) * 0.6F;
            float var7 = 1.0F - var5 * 0.95F;
            var2 = var2 * var7 + var6 * (1.0F - var7);
            var3 = var3 * var7 + var6 * (1.0F - var7);
            var4 = var4 * var7 + var6 * (1.0F - var7);
        }

        var2 *= var1 * 0.9F + 0.1F;
        var3 *= var1 * 0.9F + 0.1F;
        var4 *= var1 * 0.85F + 0.15F;
        float var8 = this.getThunderLevel(param0);
        if (var8 > 0.0F) {
            float var9 = (var2 * 0.3F + var3 * 0.59F + var4 * 0.11F) * 0.2F;
            float var10 = 1.0F - var8 * 0.95F;
            var2 = var2 * var10 + var9 * (1.0F - var10);
            var3 = var3 * var10 + var9 * (1.0F - var10);
            var4 = var4 * var10 + var9 * (1.0F - var10);
        }

        return new Vec3((double)var2, (double)var3, (double)var4);
    }

    public float getStarBrightness(float param0) {
        float var0 = this.getTimeOfDay(param0);
        float var1 = 1.0F - (Mth.cos(var0 * (float) (Math.PI * 2)) * 2.0F + 0.25F);
        var1 = Mth.clamp(var1, 0.0F, 1.0F);
        return var1 * var1 * 0.5F;
    }

    public int getSkyFlashTime() {
        return this.skyFlashTime;
    }

    @Override
    public void setSkyFlashTime(int param0) {
        this.skyFlashTime = param0;
    }

    @Override
    public float getShade(Direction param0, boolean param1) {
        boolean var0 = this.effects().constantAmbientLight();
        if (!param1) {
            return var0 ? 0.9F : 1.0F;
        } else {
            switch(param0) {
                case DOWN:
                    return var0 ? 0.9F : 0.5F;
                case UP:
                    return var0 ? 0.9F : 1.0F;
                case NORTH:
                case SOUTH:
                    return 0.8F;
                case WEST:
                case EAST:
                    return 0.6F;
                default:
                    return 1.0F;
            }
        }
    }

    @Override
    public int getBlockTint(BlockPos param0, ColorResolver param1) {
        BlockTintCache var0 = this.tintCaches.get(param1);
        return var0.getColor(param0, () -> this.calculateBlockTint(param0, param1));
    }

    public int calculateBlockTint(BlockPos param0, ColorResolver param1) {
        int var0 = Minecraft.getInstance().options.biomeBlendRadius;
        if (var0 == 0) {
            return param1.getColor(this.getBiome(param0), (double)param0.getX(), (double)param0.getZ());
        } else {
            int var1 = (var0 * 2 + 1) * (var0 * 2 + 1);
            int var2 = 0;
            int var3 = 0;
            int var4 = 0;
            Cursor3D var5 = new Cursor3D(param0.getX() - var0, param0.getY(), param0.getZ() - var0, param0.getX() + var0, param0.getY(), param0.getZ() + var0);

            int var7;
            for(BlockPos.MutableBlockPos var6 = new BlockPos.MutableBlockPos(); var5.advance(); var4 += var7 & 0xFF) {
                var6.set(var5.nextX(), var5.nextY(), var5.nextZ());
                var7 = param1.getColor(this.getBiome(var6), (double)var6.getX(), (double)var6.getZ());
                var2 += (var7 & 0xFF0000) >> 16;
                var3 += (var7 & 0xFF00) >> 8;
            }

            return (var2 / var1 & 0xFF) << 16 | (var3 / var1 & 0xFF) << 8 | var4 / var1 & 0xFF;
        }
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

    public void setDefaultSpawnPos(BlockPos param0, float param1) {
        this.levelData.setSpawn(param0, param1);
    }

    @Override
    public String toString() {
        return "ClientLevel";
    }

    public ClientLevel.ClientLevelData getLevelData() {
        return this.clientLevelData;
    }

    @OnlyIn(Dist.CLIENT)
    public static class ClientLevelData implements WritableLevelData {
        private final boolean hardcore;
        private final GameRules gameRules;
        private final boolean isFlat;
        private int xSpawn;
        private int ySpawn;
        private int zSpawn;
        private float spawnAngle;
        private long gameTime;
        private long dayTime;
        private boolean raining;
        private Difficulty difficulty;
        private boolean difficultyLocked;

        public ClientLevelData(Difficulty param0, boolean param1, boolean param2) {
            this.difficulty = param0;
            this.hardcore = param1;
            this.isFlat = param2;
            this.gameRules = new GameRules();
        }

        @Override
        public int getXSpawn() {
            return this.xSpawn;
        }

        @Override
        public int getYSpawn() {
            return this.ySpawn;
        }

        @Override
        public int getZSpawn() {
            return this.zSpawn;
        }

        @Override
        public float getSpawnAngle() {
            return this.spawnAngle;
        }

        @Override
        public long getGameTime() {
            return this.gameTime;
        }

        @Override
        public long getDayTime() {
            return this.dayTime;
        }

        @Override
        public void setXSpawn(int param0) {
            this.xSpawn = param0;
        }

        @Override
        public void setYSpawn(int param0) {
            this.ySpawn = param0;
        }

        @Override
        public void setZSpawn(int param0) {
            this.zSpawn = param0;
        }

        @Override
        public void setSpawnAngle(float param0) {
            this.spawnAngle = param0;
        }

        public void setGameTime(long param0) {
            this.gameTime = param0;
        }

        public void setDayTime(long param0) {
            this.dayTime = param0;
        }

        @Override
        public void setSpawn(BlockPos param0, float param1) {
            this.xSpawn = param0.getX();
            this.ySpawn = param0.getY();
            this.zSpawn = param0.getZ();
            this.spawnAngle = param1;
        }

        @Override
        public boolean isThundering() {
            return false;
        }

        @Override
        public boolean isRaining() {
            return this.raining;
        }

        @Override
        public void setRaining(boolean param0) {
            this.raining = param0;
        }

        @Override
        public boolean isHardcore() {
            return this.hardcore;
        }

        @Override
        public GameRules getGameRules() {
            return this.gameRules;
        }

        @Override
        public Difficulty getDifficulty() {
            return this.difficulty;
        }

        @Override
        public boolean isDifficultyLocked() {
            return this.difficultyLocked;
        }

        @Override
        public void fillCrashReportCategory(CrashReportCategory param0) {
            WritableLevelData.super.fillCrashReportCategory(param0);
        }

        public void setDifficulty(Difficulty param0) {
            this.difficulty = param0;
        }

        public void setDifficultyLocked(boolean param0) {
            this.difficultyLocked = param0;
        }

        public double getHorizonHeight() {
            return this.isFlat ? 0.0 : 63.0;
        }

        public double getClearColorScale() {
            return this.isFlat ? 1.0 : 0.03125;
        }
    }
}
