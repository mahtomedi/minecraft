package net.minecraft.world.level.dimension.end;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.worldgen.features.EndFeatures;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockPredicate;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

public class EndDragonFight {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_TICKS_BEFORE_DRAGON_RESPAWN = 1200;
    private static final int TIME_BETWEEN_CRYSTAL_SCANS = 100;
    private static final int TIME_BETWEEN_PLAYER_SCANS = 20;
    private static final int ARENA_SIZE_CHUNKS = 8;
    public static final int ARENA_TICKET_LEVEL = 9;
    private static final int GATEWAY_COUNT = 20;
    private static final int GATEWAY_DISTANCE = 96;
    public static final int DRAGON_SPAWN_Y = 128;
    private static final Predicate<Entity> VALID_PLAYER = EntitySelector.ENTITY_STILL_ALIVE.and(EntitySelector.withinDistance(0.0, 128.0, 0.0, 192.0));
    private final ServerBossEvent dragonEvent = (ServerBossEvent)new ServerBossEvent(
            new TranslatableComponent("entity.minecraft.ender_dragon"), BossEvent.BossBarColor.PINK, BossEvent.BossBarOverlay.PROGRESS
        )
        .setPlayBossMusic(true)
        .setCreateWorldFog(true);
    private final ServerLevel level;
    private final List<Integer> gateways = Lists.newArrayList();
    private final BlockPattern exitPortalPattern;
    private int ticksSinceDragonSeen;
    private int crystalsAlive;
    private int ticksSinceCrystalsScanned;
    private int ticksSinceLastPlayerScan;
    private boolean dragonKilled;
    private boolean previouslyKilled;
    @Nullable
    private UUID dragonUUID;
    private boolean needsStateScanning = true;
    @Nullable
    private BlockPos portalLocation;
    @Nullable
    private DragonRespawnAnimation respawnStage;
    private int respawnTime;
    @Nullable
    private List<EndCrystal> respawnCrystals;

    public EndDragonFight(ServerLevel param0, long param1, CompoundTag param2) {
        this.level = param0;
        if (param2.contains("NeedsStateScanning")) {
            this.needsStateScanning = param2.getBoolean("NeedsStateScanning");
        }

        if (param2.contains("DragonKilled", 99)) {
            if (param2.hasUUID("Dragon")) {
                this.dragonUUID = param2.getUUID("Dragon");
            }

            this.dragonKilled = param2.getBoolean("DragonKilled");
            this.previouslyKilled = param2.getBoolean("PreviouslyKilled");
            if (param2.getBoolean("IsRespawning")) {
                this.respawnStage = DragonRespawnAnimation.START;
            }

            if (param2.contains("ExitPortalLocation", 10)) {
                this.portalLocation = NbtUtils.readBlockPos(param2.getCompound("ExitPortalLocation"));
            }
        } else {
            this.dragonKilled = true;
            this.previouslyKilled = true;
        }

        if (param2.contains("Gateways", 9)) {
            ListTag var0 = param2.getList("Gateways", 3);

            for(int var1 = 0; var1 < var0.size(); ++var1) {
                this.gateways.add(var0.getInt(var1));
            }
        } else {
            this.gateways.addAll(ContiguousSet.create(Range.closedOpen(0, 20), DiscreteDomain.integers()));
            Util.shuffle(this.gateways, RandomSource.create(param1));
        }

        this.exitPortalPattern = BlockPatternBuilder.start()
            .aisle("       ", "       ", "       ", "   #   ", "       ", "       ", "       ")
            .aisle("       ", "       ", "       ", "   #   ", "       ", "       ", "       ")
            .aisle("       ", "       ", "       ", "   #   ", "       ", "       ", "       ")
            .aisle("  ###  ", " #   # ", "#     #", "#  #  #", "#     #", " #   # ", "  ###  ")
            .aisle("       ", "  ###  ", " ##### ", " ##### ", " ##### ", "  ###  ", "       ")
            .where('#', BlockInWorld.hasState(BlockPredicate.forBlock(Blocks.BEDROCK)))
            .build();
    }

    public CompoundTag saveData() {
        CompoundTag var0 = new CompoundTag();
        var0.putBoolean("NeedsStateScanning", this.needsStateScanning);
        if (this.dragonUUID != null) {
            var0.putUUID("Dragon", this.dragonUUID);
        }

        var0.putBoolean("DragonKilled", this.dragonKilled);
        var0.putBoolean("PreviouslyKilled", this.previouslyKilled);
        if (this.portalLocation != null) {
            var0.put("ExitPortalLocation", NbtUtils.writeBlockPos(this.portalLocation));
        }

        ListTag var1 = new ListTag();

        for(int var2 : this.gateways) {
            var1.add(IntTag.valueOf(var2));
        }

        var0.put("Gateways", var1);
        return var0;
    }

    public void tick() {
        this.dragonEvent.setVisible(!this.dragonKilled);
        if (++this.ticksSinceLastPlayerScan >= 20) {
            this.updatePlayers();
            this.ticksSinceLastPlayerScan = 0;
        }

        if (!this.dragonEvent.getPlayers().isEmpty()) {
            this.level.getChunkSource().addRegionTicket(TicketType.DRAGON, new ChunkPos(0, 0), 9, Unit.INSTANCE);
            boolean var0 = this.isArenaLoaded();
            if (this.needsStateScanning && var0) {
                this.scanState();
                this.needsStateScanning = false;
            }

            if (this.respawnStage != null) {
                if (this.respawnCrystals == null && var0) {
                    this.respawnStage = null;
                    this.tryRespawn();
                }

                this.respawnStage.tick(this.level, this, this.respawnCrystals, this.respawnTime++, this.portalLocation);
            }

            if (!this.dragonKilled) {
                if ((this.dragonUUID == null || ++this.ticksSinceDragonSeen >= 1200) && var0) {
                    this.findOrCreateDragon();
                    this.ticksSinceDragonSeen = 0;
                }

                if (++this.ticksSinceCrystalsScanned >= 100 && var0) {
                    this.updateCrystalCount();
                    this.ticksSinceCrystalsScanned = 0;
                }
            }
        } else {
            this.level.getChunkSource().removeRegionTicket(TicketType.DRAGON, new ChunkPos(0, 0), 9, Unit.INSTANCE);
        }

    }

    private void scanState() {
        LOGGER.info("Scanning for legacy world dragon fight...");
        boolean var0 = this.hasActiveExitPortal();
        if (var0) {
            LOGGER.info("Found that the dragon has been killed in this world already.");
            this.previouslyKilled = true;
        } else {
            LOGGER.info("Found that the dragon has not yet been killed in this world.");
            this.previouslyKilled = false;
            if (this.findExitPortal() == null) {
                this.spawnExitPortal(false);
            }
        }

        List<? extends EnderDragon> var1 = this.level.getDragons();
        if (var1.isEmpty()) {
            this.dragonKilled = true;
        } else {
            EnderDragon var2 = var1.get(0);
            this.dragonUUID = var2.getUUID();
            LOGGER.info("Found that there's a dragon still alive ({})", var2);
            this.dragonKilled = false;
            if (!var0) {
                LOGGER.info("But we didn't have a portal, let's remove it.");
                var2.discard();
                this.dragonUUID = null;
            }
        }

        if (!this.previouslyKilled && this.dragonKilled) {
            this.dragonKilled = false;
        }

    }

    private void findOrCreateDragon() {
        List<? extends EnderDragon> var0 = this.level.getDragons();
        if (var0.isEmpty()) {
            LOGGER.debug("Haven't seen the dragon, respawning it");
            this.createNewDragon();
        } else {
            LOGGER.debug("Haven't seen our dragon, but found another one to use.");
            this.dragonUUID = var0.get(0).getUUID();
        }

    }

    protected void setRespawnStage(DragonRespawnAnimation param0) {
        if (this.respawnStage == null) {
            throw new IllegalStateException("Dragon respawn isn't in progress, can't skip ahead in the animation.");
        } else {
            this.respawnTime = 0;
            if (param0 == DragonRespawnAnimation.END) {
                this.respawnStage = null;
                this.dragonKilled = false;
                EnderDragon var0 = this.createNewDragon();

                for(ServerPlayer var1 : this.dragonEvent.getPlayers()) {
                    CriteriaTriggers.SUMMONED_ENTITY.trigger(var1, var0);
                }
            } else {
                this.respawnStage = param0;
            }

        }
    }

    private boolean hasActiveExitPortal() {
        for(int var0 = -8; var0 <= 8; ++var0) {
            for(int var1 = -8; var1 <= 8; ++var1) {
                LevelChunk var2 = this.level.getChunk(var0, var1);

                for(BlockEntity var3 : var2.getBlockEntities().values()) {
                    if (var3 instanceof TheEndPortalBlockEntity) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Nullable
    private BlockPattern.BlockPatternMatch findExitPortal() {
        for(int var0 = -8; var0 <= 8; ++var0) {
            for(int var1 = -8; var1 <= 8; ++var1) {
                LevelChunk var2 = this.level.getChunk(var0, var1);

                for(BlockEntity var3 : var2.getBlockEntities().values()) {
                    if (var3 instanceof TheEndPortalBlockEntity) {
                        BlockPattern.BlockPatternMatch var4 = this.exitPortalPattern.find(this.level, var3.getBlockPos());
                        if (var4 != null) {
                            BlockPos var5 = var4.getBlock(3, 3, 3).getPos();
                            if (this.portalLocation == null) {
                                this.portalLocation = var5;
                            }

                            return var4;
                        }
                    }
                }
            }
        }

        int var6 = this.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, EndPodiumFeature.END_PODIUM_LOCATION).getY();

        for(int var7 = var6; var7 >= this.level.getMinBuildHeight(); --var7) {
            BlockPattern.BlockPatternMatch var8 = this.exitPortalPattern
                .find(this.level, new BlockPos(EndPodiumFeature.END_PODIUM_LOCATION.getX(), var7, EndPodiumFeature.END_PODIUM_LOCATION.getZ()));
            if (var8 != null) {
                if (this.portalLocation == null) {
                    this.portalLocation = var8.getBlock(3, 3, 3).getPos();
                }

                return var8;
            }
        }

        return null;
    }

    private boolean isArenaLoaded() {
        for(int var0 = -8; var0 <= 8; ++var0) {
            for(int var1 = 8; var1 <= 8; ++var1) {
                ChunkAccess var2 = this.level.getChunk(var0, var1, ChunkStatus.FULL, false);
                if (!(var2 instanceof LevelChunk)) {
                    return false;
                }

                ChunkHolder.FullChunkStatus var3 = ((LevelChunk)var2).getFullStatus();
                if (!var3.isOrAfter(ChunkHolder.FullChunkStatus.TICKING)) {
                    return false;
                }
            }
        }

        return true;
    }

    private void updatePlayers() {
        Set<ServerPlayer> var0 = Sets.newHashSet();

        for(ServerPlayer var1 : this.level.getPlayers(VALID_PLAYER)) {
            this.dragonEvent.addPlayer(var1);
            var0.add(var1);
        }

        Set<ServerPlayer> var2 = Sets.newHashSet(this.dragonEvent.getPlayers());
        var2.removeAll(var0);

        for(ServerPlayer var3 : var2) {
            this.dragonEvent.removePlayer(var3);
        }

    }

    private void updateCrystalCount() {
        this.ticksSinceCrystalsScanned = 0;
        this.crystalsAlive = 0;

        for(SpikeFeature.EndSpike var0 : SpikeFeature.getSpikesForLevel(this.level)) {
            this.crystalsAlive += this.level.getEntitiesOfClass(EndCrystal.class, var0.getTopBoundingBox()).size();
        }

        LOGGER.debug("Found {} end crystals still alive", this.crystalsAlive);
    }

    public void setDragonKilled(EnderDragon param0) {
        if (param0.getUUID().equals(this.dragonUUID)) {
            this.dragonEvent.setProgress(0.0F);
            this.dragonEvent.setVisible(false);
            this.spawnExitPortal(true);
            this.spawnNewGateway();
            if (!this.previouslyKilled) {
                this.level
                    .setBlockAndUpdate(
                        this.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, EndPodiumFeature.END_PODIUM_LOCATION),
                        Blocks.DRAGON_EGG.defaultBlockState()
                    );
            }

            this.previouslyKilled = true;
            this.dragonKilled = true;
        }

    }

    private void spawnNewGateway() {
        if (!this.gateways.isEmpty()) {
            int var0 = this.gateways.remove(this.gateways.size() - 1);
            int var1 = Mth.floor(96.0 * Math.cos(2.0 * (-Math.PI + (Math.PI / 20) * (double)var0)));
            int var2 = Mth.floor(96.0 * Math.sin(2.0 * (-Math.PI + (Math.PI / 20) * (double)var0)));
            this.spawnNewGateway(new BlockPos(var1, 75, var2));
        }
    }

    private void spawnNewGateway(BlockPos param0) {
        this.level.levelEvent(3000, param0, 0);
        EndFeatures.END_GATEWAY_DELAYED.value().place(this.level, this.level.getChunkSource().getGenerator(), RandomSource.create(), param0);
    }

    private void spawnExitPortal(boolean param0) {
        EndPodiumFeature var0 = new EndPodiumFeature(param0);
        if (this.portalLocation == null) {
            this.portalLocation = this.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION).below();

            while(this.level.getBlockState(this.portalLocation).is(Blocks.BEDROCK) && this.portalLocation.getY() > this.level.getSeaLevel()) {
                this.portalLocation = this.portalLocation.below();
            }
        }

        var0.place(FeatureConfiguration.NONE, this.level, this.level.getChunkSource().getGenerator(), RandomSource.create(), this.portalLocation);
    }

    private EnderDragon createNewDragon() {
        this.level.getChunkAt(new BlockPos(0, 128, 0));
        EnderDragon var0 = EntityType.ENDER_DRAGON.create(this.level);
        var0.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
        var0.moveTo(0.0, 128.0, 0.0, this.level.random.nextFloat() * 360.0F, 0.0F);
        this.level.addFreshEntity(var0);
        this.dragonUUID = var0.getUUID();
        return var0;
    }

    public void updateDragon(EnderDragon param0) {
        if (param0.getUUID().equals(this.dragonUUID)) {
            this.dragonEvent.setProgress(param0.getHealth() / param0.getMaxHealth());
            this.ticksSinceDragonSeen = 0;
            if (param0.hasCustomName()) {
                this.dragonEvent.setName(param0.getDisplayName());
            }
        }

    }

    public int getCrystalsAlive() {
        return this.crystalsAlive;
    }

    public void onCrystalDestroyed(EndCrystal param0, DamageSource param1) {
        if (this.respawnStage != null && this.respawnCrystals.contains(param0)) {
            LOGGER.debug("Aborting respawn sequence");
            this.respawnStage = null;
            this.respawnTime = 0;
            this.resetSpikeCrystals();
            this.spawnExitPortal(true);
        } else {
            this.updateCrystalCount();
            Entity var0 = this.level.getEntity(this.dragonUUID);
            if (var0 instanceof EnderDragon) {
                ((EnderDragon)var0).onCrystalDestroyed(param0, param0.blockPosition(), param1);
            }
        }

    }

    public boolean hasPreviouslyKilledDragon() {
        return this.previouslyKilled;
    }

    public void tryRespawn() {
        if (this.dragonKilled && this.respawnStage == null) {
            BlockPos var0 = this.portalLocation;
            if (var0 == null) {
                LOGGER.debug("Tried to respawn, but need to find the portal first.");
                BlockPattern.BlockPatternMatch var1 = this.findExitPortal();
                if (var1 == null) {
                    LOGGER.debug("Couldn't find a portal, so we made one.");
                    this.spawnExitPortal(true);
                } else {
                    LOGGER.debug("Found the exit portal & saved its location for next time.");
                }

                var0 = this.portalLocation;
            }

            List<EndCrystal> var2 = Lists.newArrayList();
            BlockPos var3 = var0.above(1);

            for(Direction var4 : Direction.Plane.HORIZONTAL) {
                List<EndCrystal> var5 = this.level.getEntitiesOfClass(EndCrystal.class, new AABB(var3.relative(var4, 2)));
                if (var5.isEmpty()) {
                    return;
                }

                var2.addAll(var5);
            }

            LOGGER.debug("Found all crystals, respawning dragon.");
            this.respawnDragon(var2);
        }

    }

    private void respawnDragon(List<EndCrystal> param0) {
        if (this.dragonKilled && this.respawnStage == null) {
            for(BlockPattern.BlockPatternMatch var0 = this.findExitPortal(); var0 != null; var0 = this.findExitPortal()) {
                for(int var1 = 0; var1 < this.exitPortalPattern.getWidth(); ++var1) {
                    for(int var2 = 0; var2 < this.exitPortalPattern.getHeight(); ++var2) {
                        for(int var3 = 0; var3 < this.exitPortalPattern.getDepth(); ++var3) {
                            BlockInWorld var4 = var0.getBlock(var1, var2, var3);
                            if (var4.getState().is(Blocks.BEDROCK) || var4.getState().is(Blocks.END_PORTAL)) {
                                this.level.setBlockAndUpdate(var4.getPos(), Blocks.END_STONE.defaultBlockState());
                            }
                        }
                    }
                }
            }

            this.respawnStage = DragonRespawnAnimation.START;
            this.respawnTime = 0;
            this.spawnExitPortal(false);
            this.respawnCrystals = param0;
        }

    }

    public void resetSpikeCrystals() {
        for(SpikeFeature.EndSpike var0 : SpikeFeature.getSpikesForLevel(this.level)) {
            for(EndCrystal var2 : this.level.getEntitiesOfClass(EndCrystal.class, var0.getTopBoundingBox())) {
                var2.setInvulnerable(false);
                var2.setBeamTarget(null);
            }
        }

    }
}
