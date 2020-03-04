package net.minecraft.world.entity.raid;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public class Raid {
    private static final TranslatableComponent RAID_NAME_COMPONENT = new TranslatableComponent("event.minecraft.raid");
    private static final TranslatableComponent VICTORY = new TranslatableComponent("event.minecraft.raid.victory");
    private static final TranslatableComponent DEFEAT = new TranslatableComponent("event.minecraft.raid.defeat");
    private static final Component RAID_BAR_VICTORY_COMPONENT = RAID_NAME_COMPONENT.copy().append(" - ").append(VICTORY);
    private static final Component RAID_BAR_DEFEAT_COMPONENT = RAID_NAME_COMPONENT.copy().append(" - ").append(DEFEAT);
    private final Map<Integer, Raider> groupToLeaderMap = Maps.newHashMap();
    private final Map<Integer, Set<Raider>> groupRaiderMap = Maps.newHashMap();
    private final Set<UUID> heroesOfTheVillage = Sets.newHashSet();
    private long ticksActive;
    private BlockPos center;
    private final ServerLevel level;
    private boolean started;
    private final int id;
    private float totalHealth;
    private int badOmenLevel;
    private boolean active;
    private int groupsSpawned;
    private final ServerBossEvent raidEvent = new ServerBossEvent(RAID_NAME_COMPONENT, BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);
    private int postRaidTicks;
    private int raidCooldownTicks;
    private final Random random = new Random();
    private final int numGroups;
    private Raid.RaidStatus status;
    private int celebrationTicks;
    private Optional<BlockPos> waveSpawnPos = Optional.empty();

    public Raid(int param0, ServerLevel param1, BlockPos param2) {
        this.id = param0;
        this.level = param1;
        this.active = true;
        this.raidCooldownTicks = 300;
        this.raidEvent.setPercent(0.0F);
        this.center = param2;
        this.numGroups = this.getNumGroups(param1.getDifficulty());
        this.status = Raid.RaidStatus.ONGOING;
    }

    public Raid(ServerLevel param0, CompoundTag param1) {
        this.level = param0;
        this.id = param1.getInt("Id");
        this.started = param1.getBoolean("Started");
        this.active = param1.getBoolean("Active");
        this.ticksActive = param1.getLong("TicksActive");
        this.badOmenLevel = param1.getInt("BadOmenLevel");
        this.groupsSpawned = param1.getInt("GroupsSpawned");
        this.raidCooldownTicks = param1.getInt("PreRaidTicks");
        this.postRaidTicks = param1.getInt("PostRaidTicks");
        this.totalHealth = param1.getFloat("TotalHealth");
        this.center = new BlockPos(param1.getInt("CX"), param1.getInt("CY"), param1.getInt("CZ"));
        this.numGroups = param1.getInt("NumGroups");
        this.status = Raid.RaidStatus.getByName(param1.getString("Status"));
        this.heroesOfTheVillage.clear();
        if (param1.contains("HeroesOfTheVillage", 9)) {
            ListTag var0 = param1.getList("HeroesOfTheVillage", 10);

            for(int var1 = 0; var1 < var0.size(); ++var1) {
                CompoundTag var2 = var0.getCompound(var1);
                UUID var3 = var2.getUUID("UUID");
                this.heroesOfTheVillage.add(var3);
            }
        }

    }

    public boolean isOver() {
        return this.isVictory() || this.isLoss();
    }

    public boolean isBetweenWaves() {
        return this.hasFirstWaveSpawned() && this.getTotalRaidersAlive() == 0 && this.raidCooldownTicks > 0;
    }

    public boolean hasFirstWaveSpawned() {
        return this.groupsSpawned > 0;
    }

    public boolean isStopped() {
        return this.status == Raid.RaidStatus.STOPPED;
    }

    public boolean isVictory() {
        return this.status == Raid.RaidStatus.VICTORY;
    }

    public boolean isLoss() {
        return this.status == Raid.RaidStatus.LOSS;
    }

    public Level getLevel() {
        return this.level;
    }

    public boolean isStarted() {
        return this.started;
    }

    public int getGroupsSpawned() {
        return this.groupsSpawned;
    }

    private Predicate<ServerPlayer> validPlayer() {
        return param0 -> {
            BlockPos var0 = param0.blockPosition();
            return param0.isAlive() && this.level.getRaidAt(var0) == this;
        };
    }

    private void updatePlayers() {
        Set<ServerPlayer> var0 = Sets.newHashSet(this.raidEvent.getPlayers());
        List<ServerPlayer> var1 = this.level.getPlayers(this.validPlayer());

        for(ServerPlayer var2 : var1) {
            if (!var0.contains(var2)) {
                this.raidEvent.addPlayer(var2);
            }
        }

        for(ServerPlayer var3 : var0) {
            if (!var1.contains(var3)) {
                this.raidEvent.removePlayer(var3);
            }
        }

    }

    public int getMaxBadOmenLevel() {
        return 5;
    }

    public int getBadOmenLevel() {
        return this.badOmenLevel;
    }

    public void absorbBadOmen(Player param0) {
        if (param0.hasEffect(MobEffects.BAD_OMEN)) {
            this.badOmenLevel += param0.getEffect(MobEffects.BAD_OMEN).getAmplifier() + 1;
            this.badOmenLevel = Mth.clamp(this.badOmenLevel, 0, this.getMaxBadOmenLevel());
        }

        param0.removeEffect(MobEffects.BAD_OMEN);
    }

    public void stop() {
        this.active = false;
        this.raidEvent.removeAllPlayers();
        this.status = Raid.RaidStatus.STOPPED;
    }

    public void tick() {
        if (!this.isStopped()) {
            if (this.status == Raid.RaidStatus.ONGOING) {
                boolean var0 = this.active;
                this.active = this.level.hasChunkAt(this.center);
                if (this.level.getDifficulty() == Difficulty.PEACEFUL) {
                    this.stop();
                    return;
                }

                if (var0 != this.active) {
                    this.raidEvent.setVisible(this.active);
                }

                if (!this.active) {
                    return;
                }

                if (!this.level.isVillage(this.center)) {
                    this.moveRaidCenterToNearbyVillageSection();
                }

                if (!this.level.isVillage(this.center)) {
                    if (this.groupsSpawned > 0) {
                        this.status = Raid.RaidStatus.LOSS;
                    } else {
                        this.stop();
                    }
                }

                ++this.ticksActive;
                if (this.ticksActive >= 48000L) {
                    this.stop();
                    return;
                }

                int var1 = this.getTotalRaidersAlive();
                if (var1 == 0 && this.hasMoreWaves()) {
                    if (this.raidCooldownTicks <= 0) {
                        if (this.raidCooldownTicks == 0 && this.groupsSpawned > 0) {
                            this.raidCooldownTicks = 300;
                            this.raidEvent.setName(RAID_NAME_COMPONENT);
                            return;
                        }
                    } else {
                        boolean var2 = this.waveSpawnPos.isPresent();
                        boolean var3 = !var2 && this.raidCooldownTicks % 5 == 0;
                        if (var2 && !this.level.getChunkSource().isEntityTickingChunk(new ChunkPos(this.waveSpawnPos.get()))) {
                            var3 = true;
                        }

                        if (var3) {
                            int var4 = 0;
                            if (this.raidCooldownTicks < 100) {
                                var4 = 1;
                            } else if (this.raidCooldownTicks < 40) {
                                var4 = 2;
                            }

                            this.waveSpawnPos = this.getValidSpawnPos(var4);
                        }

                        if (this.raidCooldownTicks == 300 || this.raidCooldownTicks % 20 == 0) {
                            this.updatePlayers();
                        }

                        --this.raidCooldownTicks;
                        this.raidEvent.setPercent(Mth.clamp((float)(300 - this.raidCooldownTicks) / 300.0F, 0.0F, 1.0F));
                    }
                }

                if (this.ticksActive % 20L == 0L) {
                    this.updatePlayers();
                    this.updateRaiders();
                    if (var1 > 0) {
                        if (var1 <= 2) {
                            this.raidEvent
                                .setName(
                                    RAID_NAME_COMPONENT.copy().append(" - ").append(new TranslatableComponent("event.minecraft.raid.raiders_remaining", var1))
                                );
                        } else {
                            this.raidEvent.setName(RAID_NAME_COMPONENT);
                        }
                    } else {
                        this.raidEvent.setName(RAID_NAME_COMPONENT);
                    }
                }

                boolean var5 = false;
                int var6 = 0;

                while(this.shouldSpawnGroup()) {
                    BlockPos var7 = this.waveSpawnPos.isPresent() ? this.waveSpawnPos.get() : this.findRandomSpawnPos(var6, 20);
                    if (var7 != null) {
                        this.started = true;
                        this.spawnGroup(var7);
                        if (!var5) {
                            this.playSound(var7);
                            var5 = true;
                        }
                    } else {
                        ++var6;
                    }

                    if (var6 > 3) {
                        this.stop();
                        break;
                    }
                }

                if (this.isStarted() && !this.hasMoreWaves() && var1 == 0) {
                    if (this.postRaidTicks < 40) {
                        ++this.postRaidTicks;
                    } else {
                        this.status = Raid.RaidStatus.VICTORY;

                        for(UUID var8 : this.heroesOfTheVillage) {
                            Entity var9 = this.level.getEntity(var8);
                            if (var9 instanceof LivingEntity && !var9.isSpectator()) {
                                LivingEntity var10 = (LivingEntity)var9;
                                var10.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 48000, this.badOmenLevel - 1, false, false, true));
                                if (var10 instanceof ServerPlayer) {
                                    ServerPlayer var11 = (ServerPlayer)var10;
                                    var11.awardStat(Stats.RAID_WIN);
                                    CriteriaTriggers.RAID_WIN.trigger(var11);
                                }
                            }
                        }
                    }
                }

                this.setDirty();
            } else if (this.isOver()) {
                ++this.celebrationTicks;
                if (this.celebrationTicks >= 600) {
                    this.stop();
                    return;
                }

                if (this.celebrationTicks % 20 == 0) {
                    this.updatePlayers();
                    this.raidEvent.setVisible(true);
                    if (this.isVictory()) {
                        this.raidEvent.setPercent(0.0F);
                        this.raidEvent.setName(RAID_BAR_VICTORY_COMPONENT);
                    } else {
                        this.raidEvent.setName(RAID_BAR_DEFEAT_COMPONENT);
                    }
                }
            }

        }
    }

    private void moveRaidCenterToNearbyVillageSection() {
        Stream<SectionPos> var0 = SectionPos.cube(SectionPos.of(this.center), 2);
        var0.filter(this.level::isVillage)
            .map(SectionPos::center)
            .min(Comparator.comparingDouble(param0 -> param0.distSqr(this.center)))
            .ifPresent(this::setCenter);
    }

    private Optional<BlockPos> getValidSpawnPos(int param0) {
        for(int var0 = 0; var0 < 3; ++var0) {
            BlockPos var1 = this.findRandomSpawnPos(param0, 1);
            if (var1 != null) {
                return Optional.of(var1);
            }
        }

        return Optional.empty();
    }

    private boolean hasMoreWaves() {
        if (this.hasBonusWave()) {
            return !this.hasSpawnedBonusWave();
        } else {
            return !this.isFinalWave();
        }
    }

    private boolean isFinalWave() {
        return this.getGroupsSpawned() == this.numGroups;
    }

    private boolean hasBonusWave() {
        return this.badOmenLevel > 1;
    }

    private boolean hasSpawnedBonusWave() {
        return this.getGroupsSpawned() > this.numGroups;
    }

    private boolean shouldSpawnBonusGroup() {
        return this.isFinalWave() && this.getTotalRaidersAlive() == 0 && this.hasBonusWave();
    }

    private void updateRaiders() {
        Iterator<Set<Raider>> var0 = this.groupRaiderMap.values().iterator();
        Set<Raider> var1 = Sets.newHashSet();

        while(var0.hasNext()) {
            Set<Raider> var2 = var0.next();

            for(Raider var3 : var2) {
                BlockPos var4 = var3.blockPosition();
                if (var3.removed || var3.dimension != this.level.getDimension().getType() || this.center.distSqr(var4) >= 12544.0) {
                    var1.add(var3);
                } else if (var3.tickCount > 600) {
                    if (this.level.getEntity(var3.getUUID()) == null) {
                        var1.add(var3);
                    }

                    if (!this.level.isVillage(var4) && var3.getNoActionTime() > 2400) {
                        var3.setTicksOutsideRaid(var3.getTicksOutsideRaid() + 1);
                    }

                    if (var3.getTicksOutsideRaid() >= 30) {
                        var1.add(var3);
                    }
                }
            }
        }

        for(Raider var5 : var1) {
            this.removeFromRaid(var5, true);
        }

    }

    private void playSound(BlockPos param0) {
        float var0 = 13.0F;
        int var1 = 64;
        Collection<ServerPlayer> var2 = this.raidEvent.getPlayers();

        for(ServerPlayer var3 : this.level.players()) {
            Vec3 var4 = var3.position();
            Vec3 var5 = Vec3.atCenterOf(param0);
            float var6 = Mth.sqrt((var5.x - var4.x) * (var5.x - var4.x) + (var5.z - var4.z) * (var5.z - var4.z));
            double var7 = var4.x + (double)(13.0F / var6) * (var5.x - var4.x);
            double var8 = var4.z + (double)(13.0F / var6) * (var5.z - var4.z);
            if (var6 <= 64.0F || var2.contains(var3)) {
                var3.connection.send(new ClientboundSoundPacket(SoundEvents.RAID_HORN, SoundSource.NEUTRAL, var7, var3.getY(), var8, 64.0F, 1.0F));
            }
        }

    }

    private void spawnGroup(BlockPos param0) {
        boolean var0 = false;
        int var1 = this.groupsSpawned + 1;
        this.totalHealth = 0.0F;
        DifficultyInstance var2 = this.level.getCurrentDifficultyAt(param0);
        boolean var3 = this.shouldSpawnBonusGroup();

        for(Raid.RaiderType var4 : Raid.RaiderType.VALUES) {
            int var5 = this.getDefaultNumSpawns(var4, var1, var3) + this.getPotentialBonusSpawns(var4, this.random, var1, var2, var3);
            int var6 = 0;

            for(int var7 = 0; var7 < var5; ++var7) {
                Raider var8 = var4.entityType.create(this.level);
                if (!var0 && var8.canBeLeader()) {
                    var8.setPatrolLeader(true);
                    this.setLeader(var1, var8);
                    var0 = true;
                }

                this.joinRaid(var1, var8, param0, false);
                if (var4.entityType == EntityType.RAVAGER) {
                    Raider var9 = null;
                    if (var1 == this.getNumGroups(Difficulty.NORMAL)) {
                        var9 = EntityType.PILLAGER.create(this.level);
                    } else if (var1 >= this.getNumGroups(Difficulty.HARD)) {
                        if (var6 == 0) {
                            var9 = EntityType.EVOKER.create(this.level);
                        } else {
                            var9 = EntityType.VINDICATOR.create(this.level);
                        }
                    }

                    ++var6;
                    if (var9 != null) {
                        this.joinRaid(var1, var9, param0, false);
                        var9.moveTo(param0, 0.0F, 0.0F);
                        var9.startRiding(var8);
                    }
                }
            }
        }

        this.waveSpawnPos = Optional.empty();
        ++this.groupsSpawned;
        this.updateBossbar();
        this.setDirty();
    }

    public void joinRaid(int param0, Raider param1, @Nullable BlockPos param2, boolean param3) {
        boolean var0 = this.addWaveMob(param0, param1);
        if (var0) {
            param1.setCurrentRaid(this);
            param1.setWave(param0);
            param1.setCanJoinRaid(true);
            param1.setTicksOutsideRaid(0);
            if (!param3 && param2 != null) {
                param1.setPos((double)param2.getX() + 0.5, (double)param2.getY() + 1.0, (double)param2.getZ() + 0.5);
                param1.finalizeSpawn(this.level, this.level.getCurrentDifficultyAt(param2), MobSpawnType.EVENT, null, null);
                param1.applyRaidBuffs(param0, false);
                param1.setOnGround(true);
                this.level.addFreshEntity(param1);
            }
        }

    }

    public void updateBossbar() {
        this.raidEvent.setPercent(Mth.clamp(this.getHealthOfLivingRaiders() / this.totalHealth, 0.0F, 1.0F));
    }

    public float getHealthOfLivingRaiders() {
        float var0 = 0.0F;

        for(Set<Raider> var1 : this.groupRaiderMap.values()) {
            for(Raider var2 : var1) {
                var0 += var2.getHealth();
            }
        }

        return var0;
    }

    private boolean shouldSpawnGroup() {
        return this.raidCooldownTicks == 0 && (this.groupsSpawned < this.numGroups || this.shouldSpawnBonusGroup()) && this.getTotalRaidersAlive() == 0;
    }

    public int getTotalRaidersAlive() {
        return this.groupRaiderMap.values().stream().mapToInt(Set::size).sum();
    }

    public void removeFromRaid(Raider param0, boolean param1) {
        Set<Raider> var0 = this.groupRaiderMap.get(param0.getWave());
        if (var0 != null) {
            boolean var1 = var0.remove(param0);
            if (var1) {
                if (param1) {
                    this.totalHealth -= param0.getHealth();
                }

                param0.setCurrentRaid(null);
                this.updateBossbar();
                this.setDirty();
            }
        }

    }

    private void setDirty() {
        this.level.getRaids().setDirty();
    }

    public static ItemStack getLeaderBannerInstance() {
        ItemStack var0 = new ItemStack(Items.WHITE_BANNER);
        CompoundTag var1 = var0.getOrCreateTagElement("BlockEntityTag");
        ListTag var2 = new BannerPattern.Builder()
            .addPattern(BannerPattern.RHOMBUS_MIDDLE, DyeColor.CYAN)
            .addPattern(BannerPattern.STRIPE_BOTTOM, DyeColor.LIGHT_GRAY)
            .addPattern(BannerPattern.STRIPE_CENTER, DyeColor.GRAY)
            .addPattern(BannerPattern.BORDER, DyeColor.LIGHT_GRAY)
            .addPattern(BannerPattern.STRIPE_MIDDLE, DyeColor.BLACK)
            .addPattern(BannerPattern.HALF_HORIZONTAL, DyeColor.LIGHT_GRAY)
            .addPattern(BannerPattern.CIRCLE_MIDDLE, DyeColor.LIGHT_GRAY)
            .addPattern(BannerPattern.BORDER, DyeColor.BLACK)
            .toListTag();
        var1.put("Patterns", var2);
        var0.getOrCreateTag().putInt("HideFlags", 32);
        var0.setHoverName(new TranslatableComponent("block.minecraft.ominous_banner").withStyle(ChatFormatting.GOLD));
        return var0;
    }

    @Nullable
    public Raider getLeader(int param0) {
        return this.groupToLeaderMap.get(param0);
    }

    @Nullable
    private BlockPos findRandomSpawnPos(int param0, int param1) {
        int var0 = param0 == 0 ? 2 : 2 - param0;
        BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos();

        for(int var2 = 0; var2 < param1; ++var2) {
            float var3 = this.level.random.nextFloat() * (float) (Math.PI * 2);
            int var4 = this.center.getX() + Mth.floor(Mth.cos(var3) * 32.0F * (float)var0) + this.level.random.nextInt(5);
            int var5 = this.center.getZ() + Mth.floor(Mth.sin(var3) * 32.0F * (float)var0) + this.level.random.nextInt(5);
            int var6 = this.level.getHeight(Heightmap.Types.WORLD_SURFACE, var4, var5);
            var1.set(var4, var6, var5);
            if ((!this.level.isVillage(var1) || param0 >= 2)
                && this.level.hasChunksAt(var1.getX() - 10, var1.getY() - 10, var1.getZ() - 10, var1.getX() + 10, var1.getY() + 10, var1.getZ() + 10)
                && this.level.getChunkSource().isEntityTickingChunk(new ChunkPos(var1))
                && (
                    NaturalSpawner.isSpawnPositionOk(SpawnPlacements.Type.ON_GROUND, this.level, var1, EntityType.RAVAGER)
                        || this.level.getBlockState(var1.below()).getBlock() == Blocks.SNOW && this.level.getBlockState(var1).isAir()
                )) {
                return var1;
            }
        }

        return null;
    }

    private boolean addWaveMob(int param0, Raider param1) {
        return this.addWaveMob(param0, param1, true);
    }

    public boolean addWaveMob(int param0, Raider param1, boolean param2) {
        this.groupRaiderMap.computeIfAbsent(param0, param0x -> Sets.newHashSet());
        Set<Raider> var0 = this.groupRaiderMap.get(param0);
        Raider var1 = null;

        for(Raider var2 : var0) {
            if (var2.getUUID().equals(param1.getUUID())) {
                var1 = var2;
                break;
            }
        }

        if (var1 != null) {
            var0.remove(var1);
            var0.add(param1);
        }

        var0.add(param1);
        if (param2) {
            this.totalHealth += param1.getHealth();
        }

        this.updateBossbar();
        this.setDirty();
        return true;
    }

    public void setLeader(int param0, Raider param1) {
        this.groupToLeaderMap.put(param0, param1);
        param1.setItemSlot(EquipmentSlot.HEAD, getLeaderBannerInstance());
        param1.setDropChance(EquipmentSlot.HEAD, 2.0F);
    }

    public void removeLeader(int param0) {
        this.groupToLeaderMap.remove(param0);
    }

    public BlockPos getCenter() {
        return this.center;
    }

    private void setCenter(BlockPos param0) {
        this.center = param0;
    }

    public int getId() {
        return this.id;
    }

    private int getDefaultNumSpawns(Raid.RaiderType param0, int param1, boolean param2) {
        return param2 ? param0.spawnsPerWaveBeforeBonus[this.numGroups] : param0.spawnsPerWaveBeforeBonus[param1];
    }

    private int getPotentialBonusSpawns(Raid.RaiderType param0, Random param1, int param2, DifficultyInstance param3, boolean param4) {
        Difficulty var0 = param3.getDifficulty();
        boolean var1 = var0 == Difficulty.EASY;
        boolean var2 = var0 == Difficulty.NORMAL;
        int var4;
        switch(param0) {
            case WITCH:
                if (var1 || param2 <= 2 || param2 == 4) {
                    return 0;
                }

                var4 = 1;
                break;
            case PILLAGER:
            case VINDICATOR:
                if (var1) {
                    var4 = param1.nextInt(2);
                } else if (var2) {
                    var4 = 1;
                } else {
                    var4 = 2;
                }
                break;
            case RAVAGER:
                var4 = !var1 && param4 ? 1 : 0;
                break;
            default:
                return 0;
        }

        return var4 > 0 ? param1.nextInt(var4 + 1) : 0;
    }

    public boolean isActive() {
        return this.active;
    }

    public CompoundTag save(CompoundTag param0) {
        param0.putInt("Id", this.id);
        param0.putBoolean("Started", this.started);
        param0.putBoolean("Active", this.active);
        param0.putLong("TicksActive", this.ticksActive);
        param0.putInt("BadOmenLevel", this.badOmenLevel);
        param0.putInt("GroupsSpawned", this.groupsSpawned);
        param0.putInt("PreRaidTicks", this.raidCooldownTicks);
        param0.putInt("PostRaidTicks", this.postRaidTicks);
        param0.putFloat("TotalHealth", this.totalHealth);
        param0.putInt("NumGroups", this.numGroups);
        param0.putString("Status", this.status.getName());
        param0.putInt("CX", this.center.getX());
        param0.putInt("CY", this.center.getY());
        param0.putInt("CZ", this.center.getZ());
        ListTag var0 = new ListTag();

        for(UUID var1 : this.heroesOfTheVillage) {
            CompoundTag var2 = new CompoundTag();
            var2.putUUID("UUID", var1);
            var0.add(var2);
        }

        param0.put("HeroesOfTheVillage", var0);
        return param0;
    }

    public int getNumGroups(Difficulty param0) {
        switch(param0) {
            case EASY:
                return 3;
            case NORMAL:
                return 5;
            case HARD:
                return 7;
            default:
                return 0;
        }
    }

    public float getEnchantOdds() {
        int var0 = this.getBadOmenLevel();
        if (var0 == 2) {
            return 0.1F;
        } else if (var0 == 3) {
            return 0.25F;
        } else if (var0 == 4) {
            return 0.5F;
        } else {
            return var0 == 5 ? 0.75F : 0.0F;
        }
    }

    public void addHeroOfTheVillage(Entity param0) {
        this.heroesOfTheVillage.add(param0.getUUID());
    }

    static enum RaidStatus {
        ONGOING,
        VICTORY,
        LOSS,
        STOPPED;

        private static final Raid.RaidStatus[] VALUES = values();

        private static Raid.RaidStatus getByName(String param0) {
            for(Raid.RaidStatus var0 : VALUES) {
                if (param0.equalsIgnoreCase(var0.name())) {
                    return var0;
                }
            }

            return ONGOING;
        }

        public String getName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    static enum RaiderType {
        VINDICATOR(EntityType.VINDICATOR, new int[]{0, 0, 2, 0, 1, 4, 2, 5}),
        EVOKER(EntityType.EVOKER, new int[]{0, 0, 0, 0, 0, 1, 1, 2}),
        PILLAGER(EntityType.PILLAGER, new int[]{0, 4, 3, 3, 4, 4, 4, 2}),
        WITCH(EntityType.WITCH, new int[]{0, 0, 0, 0, 3, 0, 0, 1}),
        RAVAGER(EntityType.RAVAGER, new int[]{0, 0, 0, 1, 0, 1, 0, 2});

        private static final Raid.RaiderType[] VALUES = values();
        private final EntityType<? extends Raider> entityType;
        private final int[] spawnsPerWaveBeforeBonus;

        private RaiderType(EntityType<? extends Raider> param0, int[] param1) {
            this.entityType = param0;
            this.spawnsPerWaveBeforeBonus = param1;
        }
    }
}
