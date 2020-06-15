package net.minecraft.server.level;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundHorseScreenOpenPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerLookAtPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundResourcePackPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ComplexItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ServerItemCooldowns;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerPlayer extends Player implements ContainerListener {
    private static final Logger LOGGER = LogManager.getLogger();
    public ServerGamePacketListenerImpl connection;
    public final MinecraftServer server;
    public final ServerPlayerGameMode gameMode;
    private final List<Integer> entitiesToRemove = Lists.newLinkedList();
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
    private ChatVisiblity chatVisibility;
    private boolean canChatColor = true;
    private long lastActionTime = Util.getMillis();
    private Entity camera;
    private boolean isChangingDimension;
    private boolean seenCredits;
    private final ServerRecipeBook recipeBook = new ServerRecipeBook();
    private Vec3 levitationStartPos;
    private int levitationStartTime;
    private boolean disconnected;
    @Nullable
    private Vec3 enteredNetherPosition;
    private SectionPos lastSectionPos = SectionPos.of(0, 0, 0);
    private ResourceKey<Level> respawnDimension = Level.OVERWORLD;
    @Nullable
    private BlockPos respawnPosition;
    private boolean respawnForced;
    private int containerCounter;
    public boolean ignoreSlotUpdateHack;
    public int latency;
    public boolean wonGame;

    public ServerPlayer(MinecraftServer param0, ServerLevel param1, GameProfile param2, ServerPlayerGameMode param3) {
        super(param1, param1.getSharedSpawnPos(), param2);
        param3.player = this;
        this.gameMode = param3;
        this.server = param0;
        this.stats = param0.getPlayerList().getPlayerStats(this);
        this.advancements = param0.getPlayerList().getPlayerAdvancements(this);
        this.maxUpStep = 1.0F;
        this.fudgeSpawnLocation(param1);
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
            int var7 = new Random().nextInt(var5);

            for(int var8 = 0; var8 < var5; ++var8) {
                int var9 = (var7 + var6 * var8) % var5;
                int var10 = var9 % (var1 * 2 + 1);
                int var11 = var9 / (var1 * 2 + 1);
                BlockPos var12 = PlayerRespawnLogic.getOverworldRespawnPos(param0, var0.getX() + var10 - var1, var0.getZ() + var11 - var1, false);
                if (var12 != null) {
                    this.moveTo(var12, 0.0F, 0.0F);
                    if (param0.noCollision(this)) {
                        break;
                    }
                }
            }
        } else {
            this.moveTo(var0, 0.0F, 0.0F);

            while(!param0.noCollision(this) && this.getY() < 255.0) {
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
        if (param0.contains("playerGameType", 99)) {
            if (this.getServer().getForceGameType()) {
                this.gameMode.setGameModeForPlayer(this.getServer().getDefaultGameType(), GameType.NOT_SET);
            } else {
                this.gameMode
                    .setGameModeForPlayer(
                        GameType.byId(param0.getInt("playerGameType")),
                        param0.contains("previousPlayerGameType", 3) ? GameType.byId(param0.getInt("previousPlayerGameType")) : GameType.NOT_SET
                    );
            }
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
        param0.putInt("playerGameType", this.gameMode.getGameModeForPlayer().getId());
        param0.putInt("previousPlayerGameType", this.gameMode.getPreviousGameModeForPlayer().getId());
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
        if (var2 != null && var1 != this && var1.hasOnePlayerPassenger()) {
            CompoundTag var3 = new CompoundTag();
            CompoundTag var4 = new CompoundTag();
            var1.save(var4);
            var3.putUUID("Attach", var2.getUUID());
            var3.put("Entity", var4);
            param0.put("RootVehicle", var3);
        }

        param0.put("recipeBook", this.recipeBook.toNbt());
        param0.putString("Dimension", this.level.dimension().location().toString());
        if (this.respawnPosition != null) {
            param0.putInt("SpawnX", this.respawnPosition.getX());
            param0.putInt("SpawnY", this.respawnPosition.getY());
            param0.putInt("SpawnZ", this.respawnPosition.getZ());
            param0.putBoolean("SpawnForced", this.respawnForced);
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

    public void initMenu() {
        this.containerMenu.addSlotListener(this);
    }

    @Override
    public void onEnterCombat() {
        super.onEnterCombat();
        this.connection.send(new ClientboundPlayerCombatPacket(this.getCombatTracker(), ClientboundPlayerCombatPacket.Event.ENTER_COMBAT));
    }

    @Override
    public void onLeaveCombat() {
        super.onLeaveCombat();
        this.connection.send(new ClientboundPlayerCombatPacket(this.getCombatTracker(), ClientboundPlayerCombatPacket.Event.END_COMBAT));
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
        --this.spawnInvulnerableTime;
        if (this.invulnerableTime > 0) {
            --this.invulnerableTime;
        }

        this.containerMenu.broadcastChanges();
        if (!this.level.isClientSide && !this.containerMenu.stillValid(this)) {
            this.closeContainer();
            this.containerMenu = this.inventoryMenu;
        }

        while(!this.entitiesToRemove.isEmpty()) {
            int var0 = Math.min(this.entitiesToRemove.size(), Integer.MAX_VALUE);
            int[] var1 = new int[var0];
            Iterator<Integer> var2 = this.entitiesToRemove.iterator();
            int var3 = 0;

            while(var2.hasNext() && var3 < var0) {
                var1[var3++] = var2.next();
                var2.remove();
            }

            this.connection.send(new ClientboundRemoveEntitiesPacket(var1));
        }

        Entity var4 = this.getCamera();
        if (var4 != this) {
            if (var4.isAlive()) {
                this.absMoveTo(var4.getX(), var4.getY(), var4.getZ(), var4.yRot, var4.xRot);
                this.getLevel().getChunkSource().move(this);
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

        this.advancements.flushDirty(this);
    }

    public void doTick() {
        try {
            if (!this.isSpectator() || this.level.hasChunkAt(this.blockPosition())) {
                super.tick();
            }

            for(int var0 = 0; var0 < this.inventory.getContainerSize(); ++var0) {
                ItemStack var1 = this.inventory.getItem(var0);
                if (var1.getItem().isComplex()) {
                    Packet<?> var2 = ((ComplexItem)var1.getItem()).getUpdatePacket(var1, this.level, this);
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

    private void updateScoreForCriteria(ObjectiveCriteria param0, int param1) {
        this.getScoreboard().forAllObjectives(param0, this.getScoreboardName(), param1x -> param1x.setScore(param1));
    }

    @Override
    public void die(DamageSource param0) {
        boolean var0 = this.level.getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES);
        if (var0) {
            Component var1 = this.getCombatTracker().getDeathMessage();
            this.connection
                .send(
                    new ClientboundPlayerCombatPacket(this.getCombatTracker(), ClientboundPlayerCombatPacket.Event.ENTITY_DIED, var1),
                    param1 -> {
                        if (!param1.isSuccess()) {
                            int var0x = 256;
                            String var1x = var1.getString(256);
                            Component var2x = new TranslatableComponent(
                                "death.attack.message_too_long", new TextComponent(var1x).withStyle(ChatFormatting.YELLOW)
                            );
                            Component var3x = new TranslatableComponent("death.attack.even_more_magic", this.getDisplayName())
                                .withStyle(param1x -> param1x.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, var2x)));
                            this.connection
                                .send(new ClientboundPlayerCombatPacket(this.getCombatTracker(), ClientboundPlayerCombatPacket.Event.ENTITY_DIED, var3x));
                        }
        
                    }
                );
            Team var2 = this.getTeam();
            if (var2 == null || var2.getDeathMessageVisibility() == Team.Visibility.ALWAYS) {
                this.server.getPlayerList().broadcastMessage(var1, ChatType.SYSTEM, Util.NIL_UUID);
            } else if (var2.getDeathMessageVisibility() == Team.Visibility.HIDE_FOR_OTHER_TEAMS) {
                this.server.getPlayerList().broadcastToTeam(this, var1);
            } else if (var2.getDeathMessageVisibility() == Team.Visibility.HIDE_FOR_OWN_TEAM) {
                this.server.getPlayerList().broadcastToAllExceptTeam(this, var1);
            }
        } else {
            this.connection.send(new ClientboundPlayerCombatPacket(this.getCombatTracker(), ClientboundPlayerCombatPacket.Event.ENTITY_DIED));
        }

        this.removeEntitiesOnShoulder();
        if (this.level.getGameRules().getBoolean(GameRules.RULE_FORGIVE_DEAD_PLAYERS)) {
            this.tellNeutralMobsThatIDied();
        }

        if (!this.isSpectator()) {
            this.dropAllDeathLoot(param0);
        }

        this.getScoreboard().forAllObjectives(ObjectiveCriteria.DEATH_COUNT, this.getScoreboardName(), Score::increment);
        LivingEntity var3 = this.getKillCredit();
        if (var3 != null) {
            this.awardStat(Stats.ENTITY_KILLED_BY.get(var3.getType()));
            var3.awardKillScore(this, this.deathScore, param0);
            this.createWitherRose(var3);
        }

        this.level.broadcastEntityEvent(this, (byte)3);
        this.awardStat(Stats.DEATHS);
        this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_DEATH));
        this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
        this.clearFire();
        this.setSharedFlag(0, false);
        this.getCombatTracker().recheckStatus();
    }

    private void tellNeutralMobsThatIDied() {
        AABB var0 = new AABB(this.blockPosition()).inflate(32.0, 10.0, 32.0);
        this.level
            .getLoadedEntitiesOfClass(Mob.class, var0)
            .stream()
            .filter(param0 -> param0 instanceof NeutralMob)
            .forEach(param0 -> ((NeutralMob)param0).playerDied(this));
    }

    @Override
    public void awardKillScore(Entity param0, int param1, DamageSource param2) {
        if (param0 != this) {
            super.awardKillScore(param0, param1, param2);
            this.increaseScore(param1);
            String var0 = this.getScoreboardName();
            String var1 = param0.getScoreboardName();
            this.getScoreboard().forAllObjectives(ObjectiveCriteria.KILL_COUNT_ALL, var0, Score::increment);
            if (param0 instanceof Player) {
                this.awardStat(Stats.PLAYER_KILLS);
                this.getScoreboard().forAllObjectives(ObjectiveCriteria.KILL_COUNT_PLAYERS, var0, Score::increment);
            } else {
                this.awardStat(Stats.MOB_KILLS);
            }

            this.handleTeamKill(var0, var1, ObjectiveCriteria.TEAM_KILL);
            this.handleTeamKill(var1, var0, ObjectiveCriteria.KILLED_BY_TEAM);
            CriteriaTriggers.PLAYER_KILLED_ENTITY.trigger(this, param0, param2);
        }
    }

    private void handleTeamKill(String param0, String param1, ObjectiveCriteria[] param2) {
        PlayerTeam var0 = this.getScoreboard().getPlayersTeam(param1);
        if (var0 != null) {
            int var1 = var0.getColor().getId();
            if (var1 >= 0 && var1 < param2.length) {
                this.getScoreboard().forAllObjectives(param2[var1], param0, Score::increment);
            }
        }

    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        if (this.isInvulnerableTo(param0)) {
            return false;
        } else {
            boolean var0 = this.server.isDedicatedServer() && this.isPvpAllowed() && "fall".equals(param0.msgId);
            if (!var0 && this.spawnInvulnerableTime > 0 && param0 != DamageSource.OUT_OF_WORLD) {
                return false;
            } else {
                if (param0 instanceof EntityDamageSource) {
                    Entity var1 = param0.getEntity();
                    if (var1 instanceof Player && !this.canHarmPlayer((Player)var1)) {
                        return false;
                    }

                    if (var1 instanceof AbstractArrow) {
                        AbstractArrow var2 = (AbstractArrow)var1;
                        Entity var3 = var2.getOwner();
                        if (var3 instanceof Player && !this.canHarmPlayer((Player)var3)) {
                            return false;
                        }
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
    public Entity changeDimension(ServerLevel param0) {
        this.isChangingDimension = true;
        ServerLevel var0 = this.getLevel();
        ResourceKey<Level> var1 = var0.dimension();
        if (var1 == Level.END && param0.dimension() == Level.OVERWORLD) {
            this.unRide();
            this.getLevel().removePlayerImmediately(this);
            if (!this.wonGame) {
                this.wonGame = true;
                this.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.WIN_GAME, this.seenCredits ? 0.0F : 1.0F));
                this.seenCredits = true;
            }

            return this;
        } else {
            LevelData var2 = param0.getLevelData();
            this.connection
                .send(
                    new ClientboundRespawnPacket(
                        param0.dimensionTypeKey(),
                        param0.dimension(),
                        BiomeManager.obfuscateSeed(param0.getSeed()),
                        this.gameMode.getGameModeForPlayer(),
                        this.gameMode.getPreviousGameModeForPlayer(),
                        param0.isDebug(),
                        param0.isFlat(),
                        true
                    )
                );
            this.connection.send(new ClientboundChangeDifficultyPacket(var2.getDifficulty(), var2.isDifficultyLocked()));
            PlayerList var3 = this.server.getPlayerList();
            var3.sendPlayerPermissionLevel(this);
            var0.removePlayerImmediately(this);
            this.removed = false;
            double var4 = this.getX();
            double var5 = this.getY();
            double var6 = this.getZ();
            float var7 = this.xRot;
            float var8 = this.yRot;
            float var9 = var8;
            var0.getProfiler().push("moving");
            if (param0.dimension() == Level.END) {
                BlockPos var10 = ServerLevel.END_SPAWN_POINT;
                var4 = (double)var10.getX();
                var5 = (double)var10.getY();
                var6 = (double)var10.getZ();
                var8 = 90.0F;
                var7 = 0.0F;
            } else {
                if (var1 == Level.OVERWORLD && param0.dimension() == Level.NETHER) {
                    this.enteredNetherPosition = this.position();
                }

                DimensionType var11 = var0.dimensionType();
                DimensionType var12 = param0.dimensionType();
                double var13 = 8.0;
                if (!var11.shrunk() && var12.shrunk()) {
                    var4 /= 8.0;
                    var6 /= 8.0;
                } else if (var11.shrunk() && !var12.shrunk()) {
                    var4 *= 8.0;
                    var6 *= 8.0;
                }
            }

            this.moveTo(var4, var5, var6, var8, var7);
            var0.getProfiler().pop();
            var0.getProfiler().push("placing");
            double var14 = Math.min(-2.9999872E7, param0.getWorldBorder().getMinX() + 16.0);
            double var15 = Math.min(-2.9999872E7, param0.getWorldBorder().getMinZ() + 16.0);
            double var16 = Math.min(2.9999872E7, param0.getWorldBorder().getMaxX() - 16.0);
            double var17 = Math.min(2.9999872E7, param0.getWorldBorder().getMaxZ() - 16.0);
            var4 = Mth.clamp(var4, var14, var16);
            var6 = Mth.clamp(var6, var15, var17);
            this.moveTo(var4, var5, var6, var8, var7);
            if (param0.dimension() == Level.END) {
                int var18 = Mth.floor(this.getX());
                int var19 = Mth.floor(this.getY()) - 1;
                int var20 = Mth.floor(this.getZ());
                ServerLevel.makeObsidianPlatform(param0);
                this.moveTo((double)var18, (double)var19, (double)var20, var8, 0.0F);
                this.setDeltaMovement(Vec3.ZERO);
            } else if (!param0.getPortalForcer().findAndMoveToPortal(this, var9)) {
                param0.getPortalForcer().createPortal(this);
                param0.getPortalForcer().findAndMoveToPortal(this, var9);
            }

            var0.getProfiler().pop();
            this.setLevel(param0);
            param0.addDuringPortalTeleport(this);
            this.triggerDimensionChangeTriggers(var0);
            this.connection.teleport(this.getX(), this.getY(), this.getZ(), var8, var7);
            this.gameMode.setLevel(param0);
            this.connection.send(new ClientboundPlayerAbilitiesPacket(this.abilities));
            var3.sendLevelInfo(this, param0);
            var3.sendAllPlayerInfo(this);

            for(MobEffectInstance var21 : this.getActiveEffects()) {
                this.connection.send(new ClientboundUpdateMobEffectPacket(this.getId(), var21));
            }

            this.connection.send(new ClientboundLevelEventPacket(1032, BlockPos.ZERO, 0, false));
            this.lastSentExp = -1;
            this.lastSentHealth = -1.0F;
            this.lastSentFood = -1;
            return this;
        }
    }

    private void triggerDimensionChangeTriggers(ServerLevel param0) {
        ResourceKey<Level> var0 = param0.dimension();
        ResourceKey<Level> var1 = this.level.dimension();
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

    private void broadcast(BlockEntity param0) {
        if (param0 != null) {
            ClientboundBlockEntityDataPacket var0 = param0.getUpdatePacket();
            if (var0 != null) {
                this.connection.send(var0);
            }
        }

    }

    @Override
    public void take(Entity param0, int param1) {
        super.take(param0, param1);
        this.containerMenu.broadcastChanges();
    }

    @Override
    public Either<Player.BedSleepingProblem, Unit> startSleepInBed(BlockPos param0) {
        Direction var0 = this.level.getBlockState(param0).getValue(HorizontalDirectionalBlock.FACING);
        if (this.isSleeping() || !this.isAlive()) {
            return Either.left(Player.BedSleepingProblem.OTHER_PROBLEM);
        } else if (!this.level.dimensionType().natural()) {
            return Either.left(Player.BedSleepingProblem.NOT_POSSIBLE_HERE);
        } else if (!this.bedInRange(param0, var0)) {
            return Either.left(Player.BedSleepingProblem.TOO_FAR_AWAY);
        } else if (this.bedBlocked(param0, var0)) {
            return Either.left(Player.BedSleepingProblem.OBSTRUCTED);
        } else {
            this.setRespawnPosition(this.level.dimension(), param0, false, true);
            if (this.level.isDay()) {
                return Either.left(Player.BedSleepingProblem.NOT_POSSIBLE_NOW);
            } else {
                if (!this.isCreative()) {
                    double var1 = 8.0;
                    double var2 = 5.0;
                    Vec3 var3 = Vec3.atBottomCenterOf(param0);
                    List<Monster> var4 = this.level
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
                ((ServerLevel)this.level).updateSleepingPlayerList();
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
            this.getLevel().getChunkSource().broadcastAndSend(this, new ClientboundAnimatePacket(this, 2));
        }

        super.stopSleepInBed(param0, param1);
        if (this.connection != null) {
            this.connection.teleport(this.getX(), this.getY(), this.getZ(), this.yRot, this.xRot);
        }

    }

    @Override
    public boolean startRiding(Entity param0, boolean param1) {
        Entity var0 = this.getVehicle();
        if (!super.startRiding(param0, param1)) {
            return false;
        } else {
            Entity var1 = this.getVehicle();
            if (var1 != var0 && this.connection != null) {
                this.connection.teleport(this.getX(), this.getY(), this.getZ(), this.yRot, this.xRot);
            }

            return true;
        }
    }

    @Override
    public void stopRiding() {
        Entity var0 = this.getVehicle();
        super.stopRiding();
        Entity var1 = this.getVehicle();
        if (var1 != var0 && this.connection != null) {
            this.connection.teleport(this.getX(), this.getY(), this.getZ(), this.yRot, this.xRot);
        }

    }

    @Override
    public boolean isInvulnerableTo(DamageSource param0) {
        return super.isInvulnerableTo(param0) || this.isChangingDimension() || this.abilities.invulnerable && param0 == DamageSource.WITHER;
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

    public void doCheckFallDamage(double param0, boolean param1) {
        BlockPos var0 = this.getOnPos();
        if (this.level.hasChunkAt(var0)) {
            super.checkFallDamage(param0, param1, this.level.getBlockState(var0), var0);
        }
    }

    @Override
    public void openTextEdit(SignBlockEntity param0) {
        param0.setAllowedPlayerEditor(this);
        this.connection.send(new ClientboundOpenSignEditorPacket(param0.getBlockPos()));
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
            AbstractContainerMenu var0 = param0.createMenu(this.containerCounter, this.inventory, this);
            if (var0 == null) {
                if (this.isSpectator()) {
                    this.displayClientMessage(new TranslatableComponent("container.spectatorCantOpen").withStyle(ChatFormatting.RED), true);
                }

                return OptionalInt.empty();
            } else {
                this.connection.send(new ClientboundOpenScreenPacket(var0.containerId, var0.getType(), param0.getDisplayName()));
                var0.addSlotListener(this);
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
        this.containerMenu = new HorseInventoryMenu(this.containerCounter, this.inventory, param1, param0);
        this.containerMenu.addSlotListener(this);
    }

    @Override
    public void openItemGui(ItemStack param0, InteractionHand param1) {
        Item var0 = param0.getItem();
        if (var0 == Items.WRITTEN_BOOK) {
            if (WrittenBookItem.resolveBookComponents(param0, this.createCommandSourceStack(), this)) {
                this.containerMenu.broadcastChanges();
            }

            this.connection.send(new ClientboundOpenBookPacket(param1));
        }

    }

    @Override
    public void openCommandBlock(CommandBlockEntity param0) {
        param0.setSendToClient(true);
        this.broadcast(param0);
    }

    @Override
    public void slotChanged(AbstractContainerMenu param0, int param1, ItemStack param2) {
        if (!(param0.getSlot(param1) instanceof ResultSlot)) {
            if (param0 == this.inventoryMenu) {
                CriteriaTriggers.INVENTORY_CHANGED.trigger(this, this.inventory, param2);
            }

            if (!this.ignoreSlotUpdateHack) {
                this.connection.send(new ClientboundContainerSetSlotPacket(param0.containerId, param1, param2));
            }
        }
    }

    public void refreshContainer(AbstractContainerMenu param0) {
        this.refreshContainer(param0, param0.getItems());
    }

    @Override
    public void refreshContainer(AbstractContainerMenu param0, NonNullList<ItemStack> param1) {
        this.connection.send(new ClientboundContainerSetContentPacket(param0.containerId, param1));
        this.connection.send(new ClientboundContainerSetSlotPacket(-1, -1, this.inventory.getCarried()));
    }

    @Override
    public void setContainerData(AbstractContainerMenu param0, int param1, int param2) {
        this.connection.send(new ClientboundContainerSetDataPacket(param0.containerId, param1, param2));
    }

    @Override
    public void closeContainer() {
        this.connection.send(new ClientboundContainerClosePacket(this.containerMenu.containerId));
        this.doCloseContainer();
    }

    public void broadcastCarriedItem() {
        if (!this.ignoreSlotUpdateHack) {
            this.connection.send(new ClientboundContainerSetSlotPacket(-1, -1, this.inventory.getCarried()));
        }
    }

    public void doCloseContainer() {
        this.containerMenu.removed(this);
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
    public void awardStat(Stat<?> param0, int param1) {
        this.stats.increment(this, param0, param1);
        this.getScoreboard().forAllObjectives(param0, this.getScoreboardName(), param1x -> param1x.add(param1));
    }

    @Override
    public void resetStat(Stat<?> param0) {
        this.stats.setValue(this, param0, 0);
        this.getScoreboard().forAllObjectives(param0, this.getScoreboardName(), Score::reset);
    }

    @Override
    public int awardRecipes(Collection<Recipe<?>> param0) {
        return this.recipeBook.addRecipes(param0, this);
    }

    @Override
    public void awardRecipesByKey(ResourceLocation[] param0) {
        List<Recipe<?>> var0 = Lists.newArrayList();

        for(ResourceLocation var1 : param0) {
            this.server.getRecipeManager().byKey(var1).ifPresent(var0::add);
        }

        this.awardRecipes(var0);
    }

    @Override
    public int resetRecipes(Collection<Recipe<?>> param0) {
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
        this.connection.send(new ClientboundChatPacket(param0, param1 ? ChatType.GAME_INFO : ChatType.CHAT, Util.NIL_UUID));
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
        if (param1) {
            this.inventory.replaceWith(param0.inventory);
            this.setHealth(param0.getHealth());
            this.foodData = param0.foodData;
            this.experienceLevel = param0.experienceLevel;
            this.totalExperience = param0.totalExperience;
            this.experienceProgress = param0.experienceProgress;
            this.setScore(param0.getScore());
            this.portalEntranceBlock = param0.portalEntranceBlock;
            this.portalEntranceOffset = param0.portalEntranceOffset;
            this.portalEntranceForwards = param0.portalEntranceForwards;
        } else if (this.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) || param0.isSpectator()) {
            this.inventory.replaceWith(param0.inventory);
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
        this.entitiesToRemove.addAll(param0.entitiesToRemove);
        this.seenCredits = param0.seenCredits;
        this.enteredNetherPosition = param0.enteredNetherPosition;
        this.setShoulderEntityLeft(param0.getShoulderEntityLeft());
        this.setShoulderEntityRight(param0.getShoulderEntityRight());
    }

    @Override
    protected void onEffectAdded(MobEffectInstance param0) {
        super.onEffectAdded(param0);
        this.connection.send(new ClientboundUpdateMobEffectPacket(this.getId(), param0));
        if (param0.getEffect() == MobEffects.LEVITATION) {
            this.levitationStartTime = this.tickCount;
            this.levitationStartPos = this.position();
        }

        CriteriaTriggers.EFFECTS_CHANGED.trigger(this);
    }

    @Override
    protected void onEffectUpdated(MobEffectInstance param0, boolean param1) {
        super.onEffectUpdated(param0, param1);
        this.connection.send(new ClientboundUpdateMobEffectPacket(this.getId(), param0));
        CriteriaTriggers.EFFECTS_CHANGED.trigger(this);
    }

    @Override
    protected void onEffectRemoved(MobEffectInstance param0) {
        super.onEffectRemoved(param0);
        this.connection.send(new ClientboundRemoveMobEffectPacket(this.getId(), param0.getEffect()));
        if (param0.getEffect() == MobEffects.LEVITATION) {
            this.levitationStartPos = null;
        }

        CriteriaTriggers.EFFECTS_CHANGED.trigger(this);
    }

    @Override
    public void teleportTo(double param0, double param1, double param2) {
        this.connection.teleport(param0, param1, param2, this.yRot, this.xRot);
    }

    @Override
    public void moveTo(double param0, double param1, double param2) {
        this.connection.teleport(param0, param1, param2, this.yRot, this.xRot);
        this.connection.resetPosition();
    }

    @Override
    public void crit(Entity param0) {
        this.getLevel().getChunkSource().broadcastAndSend(this, new ClientboundAnimatePacket(param0, 4));
    }

    @Override
    public void magicCrit(Entity param0) {
        this.getLevel().getChunkSource().broadcastAndSend(this, new ClientboundAnimatePacket(param0, 5));
    }

    @Override
    public void onUpdateAbilities() {
        if (this.connection != null) {
            this.connection.send(new ClientboundPlayerAbilitiesPacket(this.abilities));
            this.updateInvisibilityStatus();
        }
    }

    public ServerLevel getLevel() {
        return (ServerLevel)this.level;
    }

    @Override
    public void setGameMode(GameType param0) {
        this.gameMode.setGameModeForPlayer(param0);
        this.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, (float)param0.getId()));
        if (param0 == GameType.SPECTATOR) {
            this.removeEntitiesOnShoulder();
            this.stopRiding();
        } else {
            this.setCamera(this);
        }

        this.onUpdateAbilities();
        this.updateEffectVisibility();
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
    public void sendMessage(Component param0, UUID param1) {
        this.sendMessage(param0, ChatType.SYSTEM, param1);
    }

    public void sendMessage(Component param0, ChatType param1, UUID param2) {
        this.connection
            .send(
                new ClientboundChatPacket(param0, param1, param2),
                param3 -> {
                    if (!param3.isSuccess() && (param1 == ChatType.GAME_INFO || param1 == ChatType.SYSTEM)) {
                        int var0 = 256;
                        String var1x = param0.getString(256);
                        Component var2x = new TextComponent(var1x).withStyle(ChatFormatting.YELLOW);
                        this.connection
                            .send(
                                new ClientboundChatPacket(
                                    new TranslatableComponent("multiplayer.message_not_delivered", var2x).withStyle(ChatFormatting.RED),
                                    ChatType.SYSTEM,
                                    param2
                                )
                            );
                    }
        
                }
            );
    }

    public String getIpAddress() {
        String var0 = this.connection.connection.getRemoteAddress().toString();
        var0 = var0.substring(var0.indexOf("/") + 1);
        return var0.substring(0, var0.indexOf(":"));
    }

    public void updateOptions(ServerboundClientInformationPacket param0) {
        this.chatVisibility = param0.getChatVisibility();
        this.canChatColor = param0.getChatColors();
        this.getEntityData().set(DATA_PLAYER_MODE_CUSTOMISATION, (byte)param0.getModelCustomisation());
        this.getEntityData().set(DATA_PLAYER_MAIN_HAND, (byte)(param0.getMainHand() == HumanoidArm.LEFT ? 0 : 1));
    }

    public ChatVisiblity getChatVisibility() {
        return this.chatVisibility;
    }

    public void sendTexturePack(String param0, String param1) {
        this.connection.send(new ClientboundResourcePackPacket(param0, param1));
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

    public void sendRemoveEntity(Entity param0) {
        if (param0 instanceof Player) {
            this.connection.send(new ClientboundRemoveEntitiesPacket(param0.getId()));
        } else {
            this.entitiesToRemove.add(param0.getId());
        }

    }

    public void cancelRemoveEntity(Entity param0) {
        this.entitiesToRemove.remove(Integer.valueOf(param0.getId()));
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

    public void setCamera(Entity param0) {
        Entity var0 = this.getCamera();
        this.camera = (Entity)(param0 == null ? this : param0);
        if (var0 != this.camera) {
            this.connection.send(new ClientboundSetCameraPacket(this.camera));
            this.teleportTo(this.camera.getX(), this.camera.getY(), this.camera.getZ());
        }

    }

    @Override
    protected void processDimensionDelay() {
        if (this.changingDimensionDelay > 0 && !this.isChangingDimension) {
            --this.changingDimensionDelay;
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
        if (param0 == this.level) {
            this.connection.teleport(param1, param2, param3, param4, param5);
        } else {
            ServerLevel var0 = this.getLevel();
            LevelData var1 = param0.getLevelData();
            this.connection
                .send(
                    new ClientboundRespawnPacket(
                        param0.dimensionTypeKey(),
                        param0.dimension(),
                        BiomeManager.obfuscateSeed(param0.getSeed()),
                        this.gameMode.getGameModeForPlayer(),
                        this.gameMode.getPreviousGameModeForPlayer(),
                        param0.isDebug(),
                        param0.isFlat(),
                        true
                    )
                );
            this.connection.send(new ClientboundChangeDifficultyPacket(var1.getDifficulty(), var1.isDifficultyLocked()));
            this.server.getPlayerList().sendPlayerPermissionLevel(this);
            var0.removePlayerImmediately(this);
            this.removed = false;
            this.moveTo(param1, param2, param3, param4, param5);
            this.setLevel(param0);
            param0.addDuringCommandTeleport(this);
            this.triggerDimensionChangeTriggers(var0);
            this.connection.teleport(param1, param2, param3, param4, param5);
            this.gameMode.setLevel(param0);
            this.server.getPlayerList().sendLevelInfo(this, param0);
            this.server.getPlayerList().sendAllPlayerInfo(this);
        }

    }

    @Nullable
    public BlockPos getRespawnPosition() {
        return this.respawnPosition;
    }

    public ResourceKey<Level> getRespawnDimension() {
        return this.respawnDimension;
    }

    public boolean isRespawnForced() {
        return this.respawnForced;
    }

    public void setRespawnPosition(ResourceKey<Level> param0, @Nullable BlockPos param1, boolean param2, boolean param3) {
        if (param1 != null) {
            boolean var0 = param1.equals(this.respawnPosition) && param0.equals(this.respawnDimension);
            if (param3 && !var0) {
                this.sendMessage(new TranslatableComponent("block.minecraft.set_spawn"), Util.NIL_UUID);
            }

            this.respawnPosition = param1;
            this.respawnDimension = param0;
            this.respawnForced = param2;
        } else {
            this.respawnPosition = null;
            this.respawnDimension = Level.OVERWORLD;
            this.respawnForced = false;
        }

    }

    public void trackChunk(ChunkPos param0, Packet<?> param1, Packet<?> param2) {
        this.connection.send(param2);
        this.connection.send(param1);
    }

    public void untrackChunk(ChunkPos param0) {
        if (this.isAlive()) {
            this.connection.send(new ClientboundForgetLevelChunkPacket(param0.x, param0.z));
        }

    }

    public SectionPos getLastSectionPos() {
        return this.lastSectionPos;
    }

    public void setLastSectionPos(SectionPos param0) {
        this.lastSectionPos = param0;
    }

    @Override
    public void playNotifySound(SoundEvent param0, SoundSource param1, float param2, float param3) {
        this.connection.send(new ClientboundSoundPacket(param0, param1, this.getX(), this.getY(), this.getZ(), param2, param3));
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddPlayerPacket(this);
    }

    @Override
    public ItemEntity drop(ItemStack param0, boolean param1, boolean param2) {
        ItemEntity var0 = super.drop(param0, param1, param2);
        if (var0 == null) {
            return null;
        } else {
            this.level.addFreshEntity(var0);
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
}
