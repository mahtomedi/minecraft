package net.minecraft.world.entity.raid;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;

public class Raids extends SavedData {
    private static final String RAID_FILE_ID = "raids";
    private final Map<Integer, Raid> raidMap = Maps.newHashMap();
    private final ServerLevel level;
    private int nextAvailableID;
    private int tick;

    public Raids(ServerLevel param0) {
        this.level = param0;
        this.nextAvailableID = 1;
        this.setDirty();
    }

    public Raid get(int param0) {
        return this.raidMap.get(param0);
    }

    public void tick() {
        ++this.tick;
        Iterator<Raid> var0 = this.raidMap.values().iterator();

        while(var0.hasNext()) {
            Raid var1 = var0.next();
            if (this.level.getGameRules().getBoolean(GameRules.RULE_DISABLE_RAIDS)) {
                var1.stop();
            }

            if (var1.isStopped()) {
                var0.remove();
                this.setDirty();
            } else {
                var1.tick();
            }
        }

        if (this.tick % 200 == 0) {
            this.setDirty();
        }

        DebugPackets.sendRaids(this.level, this.raidMap.values());
    }

    public static boolean canJoinRaid(Raider param0, Raid param1) {
        if (param0 != null && param1 != null && param1.getLevel() != null) {
            return param0.isAlive()
                && param0.canJoinRaid()
                && param0.getNoActionTime() <= 2400
                && param0.level.dimensionType() == param1.getLevel().dimensionType();
        } else {
            return false;
        }
    }

    @Nullable
    public Raid createOrExtendRaid(ServerPlayer param0) {
        if (param0.isSpectator()) {
            return null;
        } else if (this.level.getGameRules().getBoolean(GameRules.RULE_DISABLE_RAIDS)) {
            return null;
        } else {
            DimensionType var0 = param0.level.dimensionType();
            if (!var0.hasRaids()) {
                return null;
            } else {
                BlockPos var1 = param0.blockPosition();
                List<PoiRecord> var2 = this.level
                    .getPoiManager()
                    .getInRange(PoiType.ALL, var1, 64, PoiManager.Occupancy.IS_OCCUPIED)
                    .collect(Collectors.toList());
                int var3 = 0;
                Vec3 var4 = Vec3.ZERO;

                for(PoiRecord var5 : var2) {
                    BlockPos var6 = var5.getPos();
                    var4 = var4.add((double)var6.getX(), (double)var6.getY(), (double)var6.getZ());
                    ++var3;
                }

                BlockPos var7;
                if (var3 > 0) {
                    var4 = var4.scale(1.0 / (double)var3);
                    var7 = new BlockPos(var4);
                } else {
                    var7 = var1;
                }

                Raid var9 = this.getOrCreateRaid(param0.getLevel(), var7);
                boolean var10 = false;
                if (!var9.isStarted()) {
                    if (!this.raidMap.containsKey(var9.getId())) {
                        this.raidMap.put(var9.getId(), var9);
                    }

                    var10 = true;
                } else if (var9.getBadOmenLevel() < var9.getMaxBadOmenLevel()) {
                    var10 = true;
                } else {
                    param0.removeEffect(MobEffects.BAD_OMEN);
                    param0.connection.send(new ClientboundEntityEventPacket(param0, (byte)43));
                }

                if (var10) {
                    var9.absorbBadOmen(param0);
                    param0.connection.send(new ClientboundEntityEventPacket(param0, (byte)43));
                    if (!var9.hasFirstWaveSpawned()) {
                        param0.awardStat(Stats.RAID_TRIGGER);
                        CriteriaTriggers.BAD_OMEN.trigger(param0);
                    }
                }

                this.setDirty();
                return var9;
            }
        }
    }

    private Raid getOrCreateRaid(ServerLevel param0, BlockPos param1) {
        Raid var0 = param0.getRaidAt(param1);
        return var0 != null ? var0 : new Raid(this.getUniqueId(), param0, param1);
    }

    public static Raids load(ServerLevel param0, CompoundTag param1) {
        Raids var0 = new Raids(param0);
        var0.nextAvailableID = param1.getInt("NextAvailableID");
        var0.tick = param1.getInt("Tick");
        ListTag var1 = param1.getList("Raids", 10);

        for(int var2 = 0; var2 < var1.size(); ++var2) {
            CompoundTag var3 = var1.getCompound(var2);
            Raid var4 = new Raid(param0, var3);
            var0.raidMap.put(var4.getId(), var4);
        }

        return var0;
    }

    @Override
    public CompoundTag save(CompoundTag param0) {
        param0.putInt("NextAvailableID", this.nextAvailableID);
        param0.putInt("Tick", this.tick);
        ListTag var0 = new ListTag();

        for(Raid var1 : this.raidMap.values()) {
            CompoundTag var2 = new CompoundTag();
            var1.save(var2);
            var0.add(var2);
        }

        param0.put("Raids", var0);
        return param0;
    }

    public static String getFileId(Holder<DimensionType> param0) {
        return param0.is(DimensionType.END_LOCATION) ? "raids_end" : "raids";
    }

    private int getUniqueId() {
        return ++this.nextAvailableID;
    }

    @Nullable
    public Raid getNearbyRaid(BlockPos param0, int param1) {
        Raid var0 = null;
        double var1 = (double)param1;

        for(Raid var2 : this.raidMap.values()) {
            double var3 = var2.getCenter().distSqr(param0);
            if (var2.isActive() && var3 < var1) {
                var0 = var2;
                var1 = var3;
            }
        }

        return var0;
    }
}
