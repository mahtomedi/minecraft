package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
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
import net.minecraft.network.protocol.game.DebugMobNameGenerator;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BeeDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
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

    @Override
    public void render(PoseStack param0, MultiBufferSource param1, double param2, double param3, double param4, long param5) {
        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        this.clearRemovedHives();
        this.doRender();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.popMatrix();
        if (!this.minecraft.player.isSpectator()) {
            this.updateLastLookedAtUuid();
        }

    }

    private void clearRemovedHives() {
        long var0 = this.minecraft.level.getGameTime() - 20L;
        this.hives.entrySet().removeIf(param1 -> param1.getValue().lastSeen < var0);
    }

    private void doRender() {
        BlockPos var0 = this.getCamera().getBlockPosition();
        this.beeInfosPerEntity.values().forEach(param0 -> {
            if (this.isPlayerCloseEnoughToMob(param0)) {
                this.renderBeeInfo(param0);
            }

        });
        this.renderFlowerInfos();

        for(BlockPos var1 : this.hives.keySet()) {
            if (var0.closerThan(var1, 30.0)) {
                highlightHive(var1);
            }
        }

        this.hives.values().forEach(param1 -> {
            if (var0.closerThan(param1.pos, 30.0)) {
                this.renderHiveInfo(param1);
            }

        });
        this.getGhostHives().forEach((param1, param2) -> {
            if (var0.closerThan(param1, 30.0)) {
                this.renderGhostHive(param1, param2);
            }

        });
    }

    private void renderFlowerInfos() {
        Map<BlockPos, Set<UUID>> var0 = Maps.newHashMap();
        this.beeInfosPerEntity.values().stream().filter(BeeDebugRenderer.BeeInfo::hasFlower).forEach(param1 -> {
            Set<UUID> var0x = var0.get(param1.flowerPos);
            if (var0x == null) {
                var0x = Sets.newHashSet();
                var0.put(param1.flowerPos, var0x);
            }

            var0x.add(param1.getUuid());
        });
        var0.entrySet().forEach(param0 -> {
            BlockPos var0x = param0.getKey();
            Set<UUID> var1x = param0.getValue();
            Set<String> var2 = var1x.stream().map(DebugMobNameGenerator::getMobName).collect(Collectors.toSet());
            renderTextOverPos(var2.toString(), var0x, 1, -256);
            renderTextOverPos("Flower", var0x, 2, -1);
            float var3 = 0.05F;
            renderTransparentFilledBox(var0x, 0.05F, 0.8F, 0.8F, 0.0F, 0.3F);
        });
    }

    private static void highlightHive(BlockPos param0) {
        float var0 = 0.05F;
        renderTransparentFilledBox(param0, 0.05F, 0.2F, 0.2F, 1.0F, 0.3F);
    }

    private void renderGhostHive(BlockPos param0, List<String> param1) {
        float var0 = 0.05F;
        renderTransparentFilledBox(param0, 0.05F, 0.2F, 0.2F, 1.0F, 0.3F);
        renderTextOverPos("" + param1, param0, 0, -256);
        renderTextOverPos("Ghost Hive", param0, 1, -65536);
    }

    private static void renderTransparentFilledBox(BlockPos param0, float param1, float param2, float param3, float param4, float param5) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        DebugRenderer.renderFilledBox(param0, param1, param2, param3, param4, param5);
    }

    private void renderHiveInfo(BeeDebugRenderer.HiveInfo param0) {
        int var0 = 0;
        if (this.getHiveMemberNames(param0).isEmpty()) {
            renderTextOverHive("Out: -", param0, var0, -3355444);
        } else if (this.getHiveMemberNames(param0).size() < 4) {
            renderTextOverHive("Out: " + this.getHiveMemberNames(param0), param0, var0, -3355444);
        } else {
            renderTextOverHive("Out: " + this.getHiveMemberNames(param0).size() + " bees", param0, var0, -3355444);
        }

        ++var0;
        if (param0.occupantCount == 0) {
            renderTextOverHive("In: -", param0, var0, -256);
        } else if (param0.occupantCount == 1) {
            renderTextOverHive("In: 1 bee", param0, var0, -256);
        } else {
            renderTextOverHive("In: " + param0.occupantCount + " bees", param0, var0, -256);
        }

        renderTextOverHive(param0.hiveType, param0, ++var0, -1);
    }

    private void renderPath(BeeDebugRenderer.BeeInfo param0) {
        if (param0.path != null) {
            PathfindingRenderer.renderPath(
                param0.path, 0.5F, false, false, this.getCamera().getPosition().x(), this.getCamera().getPosition().y(), this.getCamera().getPosition().z()
            );
        }

    }

    private void renderBeeInfo(BeeDebugRenderer.BeeInfo param0) {
        boolean var0 = this.isBeeSelected(param0);
        int var1 = 0;
        renderTextOverMob(param0.pos, var1, param0.toString(), -1, 0.03F);
        ++var1;
        if (param0.hivePos.equals(BlockPos.ZERO)) {
            renderTextOverMob(param0.pos, var1, "Homeless :(", -98404, 0.02F);
        } else {
            renderTextOverMob(param0.pos, var1, "Hive: " + this.getPosDescription(param0, param0.hivePos), -256, 0.02F);
        }

        ++var1;
        if (param0.flowerPos.equals(BlockPos.ZERO)) {
            renderTextOverMob(param0.pos, var1, "No flower :(", -98404, 0.02F);
        } else {
            renderTextOverMob(param0.pos, var1, "Flower: " + this.getPosDescription(param0, param0.flowerPos), -256, 0.02F);
        }

        ++var1;

        for(String var2 : param0.goals) {
            renderTextOverMob(param0.pos, var1, var2, -16711936, 0.02F);
            ++var1;
        }

        if (var0) {
            this.renderPath(param0);
        }

    }

    private static void renderTextOverHive(String param0, BeeDebugRenderer.HiveInfo param1, int param2, int param3) {
        BlockPos var0 = param1.pos;
        renderTextOverPos(param0, var0, param2, param3);
    }

    private static void renderTextOverPos(String param0, BlockPos param1, int param2, int param3) {
        double var0 = 1.3;
        double var1 = 0.2;
        double var2 = (double)param1.getX() + 0.5;
        double var3 = (double)param1.getY() + 1.3 + (double)param2 * 0.2;
        double var4 = (double)param1.getZ() + 0.5;
        DebugRenderer.renderFloatingText(param0, var2, var3, var4, param3, 0.02F, true, 0.0F, true);
    }

    private static void renderTextOverMob(Position param0, int param1, String param2, int param3, float param4) {
        double var0 = 2.4;
        double var1 = 0.25;
        BlockPos var2 = new BlockPos(param0);
        double var3 = (double)var2.getX() + 0.5;
        double var4 = param0.y() + 2.4 + (double)param1 * 0.25;
        double var5 = (double)var2.getZ() + 0.5;
        float var6 = 0.5F;
        DebugRenderer.renderFloatingText(param2, var3, var4, var5, param3, param4, false, 0.5F, true);
    }

    private Camera getCamera() {
        return this.minecraft.gameRenderer.getMainCamera();
    }

    private Set<String> getHiveMemberNames(BeeDebugRenderer.HiveInfo param0) {
        return this.getHiveMembers(param0.pos).stream().map(DebugMobNameGenerator::getMobName).collect(Collectors.toSet());
    }

    private String getPosDescription(BeeDebugRenderer.BeeInfo param0, BlockPos param1) {
        float var0 = Mth.sqrt(param1.distSqr(param0.pos.x(), param0.pos.y(), param0.pos.z(), true));
        double var1 = (double)Math.round(var0 * 10.0F) / 10.0;
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
                List<String> var2 = var0.get(var1.hivePos);
                if (var2 == null) {
                    var2 = Lists.newArrayList();
                    var0.put(var1.hivePos, var2);
                }

                var2.add(var1.getName());
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
        public final BlockPos hivePos;
        public final BlockPos flowerPos;
        public final List<String> goals = Lists.newArrayList();

        public BeeInfo(UUID param0, int param1, Position param2, Path param3, BlockPos param4, BlockPos param5) {
            this.uuid = param0;
            this.id = param1;
            this.pos = param2;
            this.path = param3;
            this.hivePos = param4;
            this.flowerPos = param5;
        }

        public boolean hasHive(BlockPos param0) {
            return this.hivePos != null && this.hivePos.equals(param0);
        }

        public UUID getUuid() {
            return this.uuid;
        }

        public String getName() {
            return DebugMobNameGenerator.getMobName(this.uuid);
        }

        @Override
        public String toString() {
            return this.getName();
        }

        public boolean hasFlower() {
            return this.flowerPos != BlockPos.ZERO;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class HiveInfo {
        public final BlockPos pos;
        public final String hiveType;
        public final int occupantCount;
        public final long lastSeen;

        public HiveInfo(BlockPos param0, String param1, int param2, long param3) {
            this.pos = param0;
            this.hiveType = param1;
            this.occupantCount = param2;
            this.lastSeen = param3;
        }
    }
}