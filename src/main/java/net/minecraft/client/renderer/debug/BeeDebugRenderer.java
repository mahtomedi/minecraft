package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.network.protocol.game.DebugEntityNameGenerator;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BeeDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
    private static final boolean SHOW_GOAL_FOR_ALL_BEES = true;
    private static final boolean SHOW_NAME_FOR_ALL_BEES = true;
    private static final boolean SHOW_HIVE_FOR_ALL_BEES = true;
    private static final boolean SHOW_FLOWER_POS_FOR_ALL_BEES = true;
    private static final boolean SHOW_TRAVEL_TICKS_FOR_ALL_BEES = true;
    private static final boolean SHOW_PATH_FOR_ALL_BEES = false;
    private static final boolean SHOW_GOAL_FOR_SELECTED_BEE = true;
    private static final boolean SHOW_NAME_FOR_SELECTED_BEE = true;
    private static final boolean SHOW_HIVE_FOR_SELECTED_BEE = true;
    private static final boolean SHOW_FLOWER_POS_FOR_SELECTED_BEE = true;
    private static final boolean SHOW_TRAVEL_TICKS_FOR_SELECTED_BEE = true;
    private static final boolean SHOW_PATH_FOR_SELECTED_BEE = true;
    private static final boolean SHOW_HIVE_MEMBERS = true;
    private static final boolean SHOW_BLACKLISTS = true;
    private static final int MAX_RENDER_DIST_FOR_HIVE_OVERLAY = 30;
    private static final int MAX_RENDER_DIST_FOR_BEE_OVERLAY = 30;
    private static final int MAX_TARGETING_DIST = 8;
    private static final int HIVE_TIMEOUT = 20;
    private static final float TEXT_SCALE = 0.02F;
    private static final int WHITE = -1;
    private static final int YELLOW = -256;
    private static final int ORANGE = -23296;
    private static final int GREEN = -16711936;
    private static final int GRAY = -3355444;
    private static final int PINK = -98404;
    private static final int RED = -65536;
    private final Minecraft minecraft;
    private final Map<BlockPos, BeeDebugRenderer.HiveInfo> hives = Maps.newHashMap();
    private final Map<UUID, BeeDebugRenderer.BeeInfo> beeInfosPerEntity = Maps.newHashMap();
    private UUID lastLookedAtUuid;

    public BeeDebugRenderer(Minecraft param0) {
        this.minecraft = param0;
    }

    @Override
    public void clear() {
        this.hives.clear();
        this.beeInfosPerEntity.clear();
        this.lastLookedAtUuid = null;
    }

    public void addOrUpdateHiveInfo(BeeDebugRenderer.HiveInfo param0) {
        this.hives.put(param0.pos, param0);
    }

    public void addOrUpdateBeeInfo(BeeDebugRenderer.BeeInfo param0) {
        this.beeInfosPerEntity.put(param0.uuid, param0);
    }

    public void removeBeeInfo(int param0) {
        this.beeInfosPerEntity.values().removeIf(param1 -> param1.id == param0);
    }

    @Override
    public void render(PoseStack param0, MultiBufferSource param1, double param2, double param3, double param4) {
        this.clearRemovedHives();
        this.clearRemovedBees();
        this.doRender(param0, param1);
        if (!this.minecraft.player.isSpectator()) {
            this.updateLastLookedAtUuid();
        }

    }

    private void clearRemovedBees() {
        this.beeInfosPerEntity.entrySet().removeIf(param0 -> this.minecraft.level.getEntity(param0.getValue().id) == null);
    }

    private void clearRemovedHives() {
        long var0 = this.minecraft.level.getGameTime() - 20L;
        this.hives.entrySet().removeIf(param1 -> param1.getValue().lastSeen < var0);
    }

    private void doRender(PoseStack param0, MultiBufferSource param1) {
        BlockPos var0 = this.getCamera().getBlockPosition();
        this.beeInfosPerEntity.values().forEach(param2 -> {
            if (this.isPlayerCloseEnoughToMob(param2)) {
                this.renderBeeInfo(param0, param1, param2);
            }

        });
        this.renderFlowerInfos(param0, param1);

        for(BlockPos var1 : this.hives.keySet()) {
            if (var0.closerThan(var1, 30.0)) {
                highlightHive(param0, param1, var1);
            }
        }

        Map<BlockPos, Set<UUID>> var2 = this.createHiveBlacklistMap();
        this.hives.values().forEach(param4 -> {
            if (var0.closerThan(param4.pos, 30.0)) {
                Set<UUID> var0x = var2.get(param4.pos);
                this.renderHiveInfo(param0, param1, param4, (Collection<UUID>)(var0x == null ? Sets.newHashSet() : var0x));
            }

        });
        this.getGhostHives().forEach((param3, param4) -> {
            if (var0.closerThan(param3, 30.0)) {
                this.renderGhostHive(param0, param1, param3, param4);
            }

        });
    }

    private Map<BlockPos, Set<UUID>> createHiveBlacklistMap() {
        Map<BlockPos, Set<UUID>> var0 = Maps.newHashMap();
        this.beeInfosPerEntity
            .values()
            .forEach(param1 -> param1.blacklistedHives.forEach(param2 -> var0.computeIfAbsent(param2, param0x -> Sets.newHashSet()).add(param1.getUuid())));
        return var0;
    }

    private void renderFlowerInfos(PoseStack param0, MultiBufferSource param1) {
        Map<BlockPos, Set<UUID>> var0 = Maps.newHashMap();
        this.beeInfosPerEntity
            .values()
            .stream()
            .filter(BeeDebugRenderer.BeeInfo::hasFlower)
            .forEach(param1x -> var0.computeIfAbsent(param1x.flowerPos, param0x -> Sets.newHashSet()).add(param1x.getUuid()));
        var0.entrySet().forEach(param2 -> {
            BlockPos var0x = param2.getKey();
            Set<UUID> var1x = param2.getValue();
            Set<String> var2x = var1x.stream().map(DebugEntityNameGenerator::getEntityName).collect(Collectors.toSet());
            int var3x = 1;
            renderTextOverPos(param0, param1, var2x.toString(), var0x, var3x++, -256);
            renderTextOverPos(param0, param1, "Flower", var0x, var3x++, -1);
            float var4 = 0.05F;
            DebugRenderer.renderFilledBox(param0, param1, var0x, 0.05F, 0.8F, 0.8F, 0.0F, 0.3F);
        });
    }

    private static String getBeeUuidsAsString(Collection<UUID> param0) {
        if (param0.isEmpty()) {
            return "-";
        } else {
            return param0.size() > 3
                ? param0.size() + " bees"
                : param0.stream().map(DebugEntityNameGenerator::getEntityName).collect(Collectors.toSet()).toString();
        }
    }

    private static void highlightHive(PoseStack param0, MultiBufferSource param1, BlockPos param2) {
        float var0 = 0.05F;
        DebugRenderer.renderFilledBox(param0, param1, param2, 0.05F, 0.2F, 0.2F, 1.0F, 0.3F);
    }

    private void renderGhostHive(PoseStack param0, MultiBufferSource param1, BlockPos param2, List<String> param3) {
        float var0 = 0.05F;
        DebugRenderer.renderFilledBox(param0, param1, param2, 0.05F, 0.2F, 0.2F, 1.0F, 0.3F);
        renderTextOverPos(param0, param1, param3 + "", param2, 0, -256);
        renderTextOverPos(param0, param1, "Ghost Hive", param2, 1, -65536);
    }

    private void renderHiveInfo(PoseStack param0, MultiBufferSource param1, BeeDebugRenderer.HiveInfo param2, Collection<UUID> param3) {
        int var0 = 0;
        if (!param3.isEmpty()) {
            renderTextOverHive(param0, param1, "Blacklisted by " + getBeeUuidsAsString(param3), param2, var0++, -65536);
        }

        renderTextOverHive(param0, param1, "Out: " + getBeeUuidsAsString(this.getHiveMembers(param2.pos)), param2, var0++, -3355444);
        if (param2.occupantCount == 0) {
            renderTextOverHive(param0, param1, "In: -", param2, var0++, -256);
        } else if (param2.occupantCount == 1) {
            renderTextOverHive(param0, param1, "In: 1 bee", param2, var0++, -256);
        } else {
            renderTextOverHive(param0, param1, "In: " + param2.occupantCount + " bees", param2, var0++, -256);
        }

        renderTextOverHive(param0, param1, "Honey: " + param2.honeyLevel, param2, var0++, -23296);
        renderTextOverHive(param0, param1, param2.hiveType + (param2.sedated ? " (sedated)" : ""), param2, var0++, -1);
    }

    private void renderPath(PoseStack param0, MultiBufferSource param1, BeeDebugRenderer.BeeInfo param2) {
        if (param2.path != null) {
            PathfindingRenderer.renderPath(
                param0,
                param1,
                param2.path,
                0.5F,
                false,
                false,
                this.getCamera().getPosition().x(),
                this.getCamera().getPosition().y(),
                this.getCamera().getPosition().z()
            );
        }

    }

    private void renderBeeInfo(PoseStack param0, MultiBufferSource param1, BeeDebugRenderer.BeeInfo param2) {
        boolean var0 = this.isBeeSelected(param2);
        int var1 = 0;
        renderTextOverMob(param0, param1, param2.pos, var1++, param2.toString(), -1, 0.03F);
        if (param2.hivePos == null) {
            renderTextOverMob(param0, param1, param2.pos, var1++, "No hive", -98404, 0.02F);
        } else {
            renderTextOverMob(param0, param1, param2.pos, var1++, "Hive: " + this.getPosDescription(param2, param2.hivePos), -256, 0.02F);
        }

        if (param2.flowerPos == null) {
            renderTextOverMob(param0, param1, param2.pos, var1++, "No flower", -98404, 0.02F);
        } else {
            renderTextOverMob(param0, param1, param2.pos, var1++, "Flower: " + this.getPosDescription(param2, param2.flowerPos), -256, 0.02F);
        }

        for(String var2 : param2.goals) {
            renderTextOverMob(param0, param1, param2.pos, var1++, var2, -16711936, 0.02F);
        }

        if (var0) {
            this.renderPath(param0, param1, param2);
        }

        if (param2.travelTicks > 0) {
            int var3 = param2.travelTicks < 600 ? -3355444 : -23296;
            renderTextOverMob(param0, param1, param2.pos, var1++, "Travelling: " + param2.travelTicks + " ticks", var3, 0.02F);
        }

    }

    private static void renderTextOverHive(PoseStack param0, MultiBufferSource param1, String param2, BeeDebugRenderer.HiveInfo param3, int param4, int param5) {
        BlockPos var0 = param3.pos;
        renderTextOverPos(param0, param1, param2, var0, param4, param5);
    }

    private static void renderTextOverPos(PoseStack param0, MultiBufferSource param1, String param2, BlockPos param3, int param4, int param5) {
        double var0 = 1.3;
        double var1 = 0.2;
        double var2 = (double)param3.getX() + 0.5;
        double var3 = (double)param3.getY() + 1.3 + (double)param4 * 0.2;
        double var4 = (double)param3.getZ() + 0.5;
        DebugRenderer.renderFloatingText(param0, param1, param2, var2, var3, var4, param5, 0.02F, true, 0.0F, true);
    }

    private static void renderTextOverMob(PoseStack param0, MultiBufferSource param1, Position param2, int param3, String param4, int param5, float param6) {
        double var0 = 2.4;
        double var1 = 0.25;
        BlockPos var2 = new BlockPos(param2);
        double var3 = (double)var2.getX() + 0.5;
        double var4 = param2.y() + 2.4 + (double)param3 * 0.25;
        double var5 = (double)var2.getZ() + 0.5;
        float var6 = 0.5F;
        DebugRenderer.renderFloatingText(param0, param1, param4, var3, var4, var5, param5, param6, false, 0.5F, true);
    }

    private Camera getCamera() {
        return this.minecraft.gameRenderer.getMainCamera();
    }

    private Set<String> getHiveMemberNames(BeeDebugRenderer.HiveInfo param0) {
        return this.getHiveMembers(param0.pos).stream().map(DebugEntityNameGenerator::getEntityName).collect(Collectors.toSet());
    }

    private String getPosDescription(BeeDebugRenderer.BeeInfo param0, BlockPos param1) {
        double var0 = Math.sqrt(param1.distToCenterSqr(param0.pos));
        double var1 = (double)Math.round(var0 * 10.0) / 10.0;
        return param1.toShortString() + " (dist " + var1 + ")";
    }

    private boolean isBeeSelected(BeeDebugRenderer.BeeInfo param0) {
        return Objects.equals(this.lastLookedAtUuid, param0.uuid);
    }

    private boolean isPlayerCloseEnoughToMob(BeeDebugRenderer.BeeInfo param0) {
        Player var0 = this.minecraft.player;
        BlockPos var1 = new BlockPos(var0.getX(), param0.pos.y(), var0.getZ());
        BlockPos var2 = new BlockPos(param0.pos);
        return var1.closerThan(var2, 30.0);
    }

    private Collection<UUID> getHiveMembers(BlockPos param0) {
        return this.beeInfosPerEntity
            .values()
            .stream()
            .filter(param1 -> param1.hasHive(param0))
            .map(BeeDebugRenderer.BeeInfo::getUuid)
            .collect(Collectors.toSet());
    }

    private Map<BlockPos, List<String>> getGhostHives() {
        Map<BlockPos, List<String>> var0 = Maps.newHashMap();

        for(BeeDebugRenderer.BeeInfo var1 : this.beeInfosPerEntity.values()) {
            if (var1.hivePos != null && !this.hives.containsKey(var1.hivePos)) {
                var0.computeIfAbsent(var1.hivePos, param0 -> Lists.newArrayList()).add(var1.getName());
            }
        }

        return var0;
    }

    private void updateLastLookedAtUuid() {
        DebugRenderer.getTargetedEntity(this.minecraft.getCameraEntity(), 8).ifPresent(param0 -> this.lastLookedAtUuid = param0.getUUID());
    }

    @OnlyIn(Dist.CLIENT)
    public static class BeeInfo {
        public final UUID uuid;
        public final int id;
        public final Position pos;
        @Nullable
        public final Path path;
        @Nullable
        public final BlockPos hivePos;
        @Nullable
        public final BlockPos flowerPos;
        public final int travelTicks;
        public final List<String> goals = Lists.newArrayList();
        public final Set<BlockPos> blacklistedHives = Sets.newHashSet();

        public BeeInfo(UUID param0, int param1, Position param2, @Nullable Path param3, @Nullable BlockPos param4, @Nullable BlockPos param5, int param6) {
            this.uuid = param0;
            this.id = param1;
            this.pos = param2;
            this.path = param3;
            this.hivePos = param4;
            this.flowerPos = param5;
            this.travelTicks = param6;
        }

        public boolean hasHive(BlockPos param0) {
            return this.hivePos != null && this.hivePos.equals(param0);
        }

        public UUID getUuid() {
            return this.uuid;
        }

        public String getName() {
            return DebugEntityNameGenerator.getEntityName(this.uuid);
        }

        @Override
        public String toString() {
            return this.getName();
        }

        public boolean hasFlower() {
            return this.flowerPos != null;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class HiveInfo {
        public final BlockPos pos;
        public final String hiveType;
        public final int occupantCount;
        public final int honeyLevel;
        public final boolean sedated;
        public final long lastSeen;

        public HiveInfo(BlockPos param0, String param1, int param2, int param3, boolean param4, long param5) {
            this.pos = param0;
            this.hiveType = param1;
            this.occupantCount = param2;
            this.honeyLevel = param3;
            this.sedated = param4;
            this.lastSeen = param5;
        }
    }
}
