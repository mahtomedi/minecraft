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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.DebugMobNameGenerator;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class VillageDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Minecraft minecraft;
    private final Map<BlockPos, VillageDebugRenderer.PoiInfo> pois = Maps.newHashMap();
    private final Set<SectionPos> villageSections = Sets.newHashSet();
    private final Map<UUID, VillageDebugRenderer.BrainDump> brainDumpsPerEntity = Maps.newHashMap();
    private UUID lastLookedAtUuid;

    public VillageDebugRenderer(Minecraft param0) {
        this.minecraft = param0;
    }

    @Override
    public void clear() {
        this.pois.clear();
        this.villageSections.clear();
        this.brainDumpsPerEntity.clear();
        this.lastLookedAtUuid = null;
    }

    public void addPoi(VillageDebugRenderer.PoiInfo param0) {
        this.pois.put(param0.pos, param0);
    }

    public void removePoi(BlockPos param0) {
        this.pois.remove(param0);
    }

    public void setFreeTicketCount(BlockPos param0, int param1) {
        VillageDebugRenderer.PoiInfo var0 = this.pois.get(param0);
        if (var0 == null) {
            LOGGER.warn("Strange, setFreeTicketCount was called for an unknown POI: " + param0);
        } else {
            var0.freeTicketCount = param1;
        }
    }

    public void setVillageSection(SectionPos param0) {
        this.villageSections.add(param0);
    }

    public void setNotVillageSection(SectionPos param0) {
        this.villageSections.remove(param0);
    }

    public void addOrUpdateBrainDump(VillageDebugRenderer.BrainDump param0) {
        this.brainDumpsPerEntity.put(param0.uuid, param0);
    }

    @Override
    public void render(PoseStack param0, MultiBufferSource param1, double param2, double param3, double param4, long param5) {
        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        this.doRender(param2, param3, param4);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.popMatrix();
        if (!this.minecraft.player.isSpectator()) {
            this.updateLastLookedAtUuid();
        }

    }

    private void doRender(double param0, double param1, double param2) {
        BlockPos var0 = new BlockPos(param0, param1, param2);
        this.villageSections.forEach(param1x -> {
            if (var0.closerThan(param1x.center(), 60.0)) {
                highlightVillageSection(param1x);
            }

        });
        this.brainDumpsPerEntity.values().forEach(param3 -> {
            if (this.isPlayerCloseEnoughToMob(param3)) {
                this.renderVillagerInfo(param3, param0, param1, param2);
            }

        });

        for(BlockPos var1 : this.pois.keySet()) {
            if (var0.closerThan(var1, 30.0)) {
                highlightPoi(var1);
            }
        }

        this.pois.values().forEach(param1x -> {
            if (var0.closerThan(param1x.pos, 30.0)) {
                this.renderPoiInfo(param1x);
            }

        });
        this.getGhostPois().forEach((param1x, param2x) -> {
            if (var0.closerThan(param1x, 30.0)) {
                this.renderGhostPoi(param1x, param2x);
            }

        });
    }

    private static void highlightVillageSection(SectionPos param0) {
        float var0 = 1.0F;
        BlockPos var1 = param0.center();
        BlockPos var2 = var1.offset(-1.0, -1.0, -1.0);
        BlockPos var3 = var1.offset(1.0, 1.0, 1.0);
        DebugRenderer.renderFilledBox(var2, var3, 0.2F, 1.0F, 0.2F, 0.15F);
    }

    private static void highlightPoi(BlockPos param0) {
        float var0 = 0.05F;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        DebugRenderer.renderFilledBox(param0, 0.05F, 0.2F, 0.2F, 1.0F, 0.3F);
    }

    private void renderGhostPoi(BlockPos param0, List<String> param1) {
        float var0 = 0.05F;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        DebugRenderer.renderFilledBox(param0, 0.05F, 0.2F, 0.2F, 1.0F, 0.3F);
        renderTextOverPos("" + param1, param0, 0, -256);
        renderTextOverPos("Ghost POI", param0, 1, -65536);
    }

    private void renderPoiInfo(VillageDebugRenderer.PoiInfo param0) {
        int var0 = 0;
        if (this.getTicketHolderNames(param0).size() < 4) {
            renderTextOverPoi("" + this.getTicketHolderNames(param0), param0, var0, -256);
        } else {
            renderTextOverPoi("" + this.getTicketHolderNames(param0).size() + " ticket holders", param0, var0, -256);
        }

        renderTextOverPoi("Free tickets: " + param0.freeTicketCount, param0, ++var0, -256);
        renderTextOverPoi(param0.type, param0, ++var0, -1);
    }

    private void renderPath(VillageDebugRenderer.BrainDump param0, double param1, double param2, double param3) {
        if (param0.path != null) {
            PathfindingRenderer.renderPath(param0.path, 0.5F, false, false, param1, param2, param3);
        }

    }

    private void renderVillagerInfo(VillageDebugRenderer.BrainDump param0, double param1, double param2, double param3) {
        boolean var0 = this.isVillagerSelected(param0);
        int var1 = 0;
        renderTextOverMob(param0.pos, var1, param0.name, -1, 0.03F);
        ++var1;
        if (var0) {
            renderTextOverMob(param0.pos, var1, param0.profession + " " + param0.xp + "xp", -1, 0.02F);
            ++var1;
        }

        if (var0 && !param0.inventory.equals("")) {
            renderTextOverMob(param0.pos, var1, param0.inventory, -98404, 0.02F);
            ++var1;
        }

        if (var0) {
            for(String var2 : param0.behaviors) {
                renderTextOverMob(param0.pos, var1, var2, -16711681, 0.02F);
                ++var1;
            }
        }

        if (var0) {
            for(String var3 : param0.activities) {
                renderTextOverMob(param0.pos, var1, var3, -16711936, 0.02F);
                ++var1;
            }
        }

        if (param0.wantsGolem) {
            renderTextOverMob(param0.pos, var1, "Wants Golem", -23296, 0.02F);
            ++var1;
        }

        if (var0) {
            for(String var4 : param0.gossips) {
                if (var4.startsWith(param0.name)) {
                    renderTextOverMob(param0.pos, var1, var4, -1, 0.02F);
                } else {
                    renderTextOverMob(param0.pos, var1, var4, -23296, 0.02F);
                }

                ++var1;
            }
        }

        if (var0) {
            for(String var5 : Lists.reverse(param0.memories)) {
                renderTextOverMob(param0.pos, var1, var5, -3355444, 0.02F);
                ++var1;
            }
        }

        if (var0) {
            this.renderPath(param0, param1, param2, param3);
        }

    }

    private static void renderTextOverPoi(String param0, VillageDebugRenderer.PoiInfo param1, int param2, int param3) {
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

    private Set<String> getTicketHolderNames(VillageDebugRenderer.PoiInfo param0) {
        return this.getTicketHolders(param0.pos).stream().map(DebugMobNameGenerator::getMobName).collect(Collectors.toSet());
    }

    private boolean isVillagerSelected(VillageDebugRenderer.BrainDump param0) {
        return Objects.equals(this.lastLookedAtUuid, param0.uuid);
    }

    private boolean isPlayerCloseEnoughToMob(VillageDebugRenderer.BrainDump param0) {
        Player var0 = this.minecraft.player;
        BlockPos var1 = new BlockPos(var0.getX(), param0.pos.y(), var0.getZ());
        BlockPos var2 = new BlockPos(param0.pos);
        return var1.closerThan(var2, 30.0);
    }

    private Collection<UUID> getTicketHolders(BlockPos param0) {
        return this.brainDumpsPerEntity
            .values()
            .stream()
            .filter(param1 -> param1.hasPoi(param0))
            .map(VillageDebugRenderer.BrainDump::getUuid)
            .collect(Collectors.toSet());
    }

    private Map<BlockPos, List<String>> getGhostPois() {
        Map<BlockPos, List<String>> var0 = Maps.newHashMap();

        for(VillageDebugRenderer.BrainDump var1 : this.brainDumpsPerEntity.values()) {
            for(BlockPos var2 : var1.pois) {
                if (!this.pois.containsKey(var2)) {
                    List<String> var3 = var0.get(var2);
                    if (var3 == null) {
                        var3 = Lists.newArrayList();
                        var0.put(var2, var3);
                    }

                    var3.add(var1.name);
                }
            }
        }

        return var0;
    }

    private void updateLastLookedAtUuid() {
        DebugRenderer.getTargetedEntity(this.minecraft.getCameraEntity(), 8).ifPresent(param0 -> this.lastLookedAtUuid = param0.getUUID());
    }

    @OnlyIn(Dist.CLIENT)
    public static class BrainDump {
        public final UUID uuid;
        public final int id;
        public final String name;
        public final String profession;
        public final int xp;
        public final Position pos;
        public final String inventory;
        public final Path path;
        public final boolean wantsGolem;
        public final List<String> activities = Lists.newArrayList();
        public final List<String> behaviors = Lists.newArrayList();
        public final List<String> memories = Lists.newArrayList();
        public final List<String> gossips = Lists.newArrayList();
        public final Set<BlockPos> pois = Sets.newHashSet();

        public BrainDump(
            UUID param0, int param1, String param2, String param3, int param4, Position param5, String param6, @Nullable Path param7, boolean param8
        ) {
            this.uuid = param0;
            this.id = param1;
            this.name = param2;
            this.profession = param3;
            this.xp = param4;
            this.pos = param5;
            this.inventory = param6;
            this.path = param7;
            this.wantsGolem = param8;
        }

        private boolean hasPoi(BlockPos param0) {
            return this.pois.stream().anyMatch(param0::equals);
        }

        public UUID getUuid() {
            return this.uuid;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class PoiInfo {
        public final BlockPos pos;
        public String type;
        public int freeTicketCount;

        public PoiInfo(BlockPos param0, String param1, int param2) {
            this.pos = param0;
            this.type = param1;
            this.freeTicketCount = param2;
        }
    }
}
