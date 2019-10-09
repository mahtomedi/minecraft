package net.minecraft.client.multiplayer;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagManager;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.global.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.EmptyTickList;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MultiPlayerLevel extends Level {
    private final List<Entity> globalEntities = Lists.newArrayList();
    private final Int2ObjectMap<Entity> entitiesById = new Int2ObjectOpenHashMap<>();
    private final ClientPacketListener connection;
    private final LevelRenderer levelRenderer;
    private final Minecraft minecraft = Minecraft.getInstance();
    private final List<AbstractClientPlayer> players = Lists.newArrayList();
    private int delayUntilNextMoodSound = this.random.nextInt(12000);
    private Scoreboard scoreboard = new Scoreboard();
    private final Map<String, MapItemSavedData> mapData = Maps.newHashMap();

    public MultiPlayerLevel(ClientPacketListener param0, LevelSettings param1, DimensionType param2, int param3, ProfilerFiller param4, LevelRenderer param5) {
        super(new LevelData(param1, "MpServer"), param2, (param1x, param2x) -> new ClientChunkCache((MultiPlayerLevel)param1x, param3), param4, true);
        this.connection = param0;
        this.levelRenderer = param5;
        this.setSpawnPos(new BlockPos(8, 64, 8));
        this.updateSkyBrightness();
        this.prepareWeather();
    }

    public void tick(BooleanSupplier param0) {
        this.getWorldBorder().tick();
        this.tickTime();
        this.getProfiler().push("blocks");
        this.chunkSource.tick(param0);
        this.playMoodSounds();
        this.getProfiler().pop();
    }

    public Iterable<Entity> entitiesForRendering() {
        return Iterables.concat(this.entitiesById.values(), this.globalEntities);
    }

    public void tickEntities() {
        ProfilerFiller var0 = this.getProfiler();
        var0.push("entities");
        var0.push("global");

        for(int var1 = 0; var1 < this.globalEntities.size(); ++var1) {
            Entity var2 = this.globalEntities.get(var1);
            this.guardEntityTick(param0 -> {
                ++param0.tickCount;
                param0.tick();
            }, var2);
            if (var2.removed) {
                this.globalEntities.remove(var1--);
            }
        }

        var0.popPush("regular");
        ObjectIterator<Entry<Entity>> var3 = this.entitiesById.int2ObjectEntrySet().iterator();

        while(var3.hasNext()) {
            Entry<Entity> var4 = var3.next();
            Entity var5 = var4.getValue();
            if (!var5.isPassenger()) {
                var0.push("tick");
                if (!var5.removed) {
                    this.guardEntityTick(this::tickNonPassenger, var5);
                }

                var0.pop();
                var0.push("remove");
                if (var5.removed) {
                    var3.remove();
                    this.onEntityRemoved(var5);
                }

                var0.pop();
            }
        }

        var0.pop();
        this.tickBlockEntities();
        var0.pop();
    }

    public void tickNonPassenger(Entity param0) {
        if (param0 instanceof Player || this.getChunkSource().isEntityTickingChunk(param0)) {
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

    public void unload(LevelChunk param0) {
        this.blockEntitiesToUnload.addAll(param0.getBlockEntities().values());
        this.chunkSource.getLightEngine().enableLightSources(param0.getPos(), false);
    }

    @Override
    public boolean hasChunk(int param0, int param1) {
        return true;
    }

    private void playMoodSounds() {
        if (this.minecraft.player != null) {
            if (this.delayUntilNextMoodSound > 0) {
                --this.delayUntilNextMoodSound;
            } else {
                BlockPos var0 = new BlockPos(this.minecraft.player);
                BlockPos var1 = var0.offset(4 * (this.random.nextInt(3) - 1), 4 * (this.random.nextInt(3) - 1), 4 * (this.random.nextInt(3) - 1));
                double var2 = var0.distSqr(var1);
                if (var2 >= 4.0 && var2 <= 256.0) {
                    BlockState var3 = this.getBlockState(var1);
                    if (var3.isAir() && this.getRawBrightness(var1, 0) <= this.random.nextInt(8) && this.getBrightness(LightLayer.SKY, var1) <= 0) {
                        this.playLocalSound(
                            (double)var1.getX() + 0.5,
                            (double)var1.getY() + 0.5,
                            (double)var1.getZ() + 0.5,
                            SoundEvents.AMBIENT_CAVE,
                            SoundSource.AMBIENT,
                            0.7F,
                            0.8F + this.random.nextFloat() * 0.2F,
                            false
                        );
                        this.delayUntilNextMoodSound = this.random.nextInt(12000) + 6000;
                    }
                }

            }
        }
    }

    public int getEntityCount() {
        return this.entitiesById.size();
    }

    public void addLightning(LightningBolt param0) {
        this.globalEntities.add(param0);
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
        ItemStack var2 = this.minecraft.player.getMainHandItem();
        boolean var3 = this.minecraft.gameMode.getPlayerMode() == GameType.CREATIVE && !var2.isEmpty() && var2.getItem() == Blocks.BARRIER.asItem();
        BlockPos.MutableBlockPos var4 = new BlockPos.MutableBlockPos();

        for(int var5 = 0; var5 < 667; ++var5) {
            this.doAnimateTick(param0, param1, param2, 16, var1, var3, var4);
            this.doAnimateTick(param0, param1, param2, 32, var1, var3, var4);
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

        if (param5 && var3.getBlock() == Blocks.BARRIER) {
            this.addParticle(ParticleTypes.BARRIER, (double)((float)var0 + 0.5F), (double)((float)var1 + 0.5F), (double)((float)var2 + 0.5F), 0.0, 0.0, 0.0);
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
        SimpleSoundInstance var1 = new SimpleSoundInstance(param3, param4, param5, param6, (float)param0, (float)param1, (float)param2);
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
    public void setDayTime(long param0) {
        if (param0 < 0L) {
            param0 = -param0;
            this.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, null);
        } else {
            this.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(true, null);
        }

        super.setDayTime(param0);
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
        return (ClientChunkCache)super.getChunkSource();
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
    public TagManager getTagManager() {
        return this.connection.getTags();
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
        return Biomes.PLAINS;
    }
}
