package net.minecraft.client.renderer.debug;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
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
import net.minecraft.network.protocol.game.DebugEntityNameGenerator;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class BrainDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean SHOW_NAME_FOR_ALL = true;
    private static final boolean SHOW_PROFESSION_FOR_ALL = false;
    private static final boolean SHOW_BEHAVIORS_FOR_ALL = false;
    private static final boolean SHOW_ACTIVITIES_FOR_ALL = false;
    private static final boolean SHOW_INVENTORY_FOR_ALL = false;
    private static final boolean SHOW_GOSSIPS_FOR_ALL = false;
    private static final boolean SHOW_PATH_FOR_ALL = false;
    private static final boolean SHOW_HEALTH_FOR_ALL = false;
    private static final boolean SHOW_WANTS_GOLEM_FOR_ALL = true;
    private static final boolean SHOW_ANGER_LEVEL_FOR_ALL = false;
    private static final boolean SHOW_NAME_FOR_SELECTED = true;
    private static final boolean SHOW_PROFESSION_FOR_SELECTED = true;
    private static final boolean SHOW_BEHAVIORS_FOR_SELECTED = true;
    private static final boolean SHOW_ACTIVITIES_FOR_SELECTED = true;
    private static final boolean SHOW_MEMORIES_FOR_SELECTED = true;
    private static final boolean SHOW_INVENTORY_FOR_SELECTED = true;
    private static final boolean SHOW_GOSSIPS_FOR_SELECTED = true;
    private static final boolean SHOW_PATH_FOR_SELECTED = true;
    private static final boolean SHOW_HEALTH_FOR_SELECTED = true;
    private static final boolean SHOW_WANTS_GOLEM_FOR_SELECTED = true;
    private static final boolean SHOW_ANGER_LEVEL_FOR_SELECTED = true;
    private static final boolean SHOW_POI_INFO = true;
    private static final int MAX_RENDER_DIST_FOR_BRAIN_INFO = 30;
    private static final int MAX_RENDER_DIST_FOR_POI_INFO = 30;
    private static final int MAX_TARGETING_DIST = 8;
    private static final float TEXT_SCALE = 0.02F;
    private static final int WHITE = -1;
    private static final int YELLOW = -256;
    private static final int CYAN = -16711681;
    private static final int GREEN = -16711936;
    private static final int GRAY = -3355444;
    private static final int PINK = -98404;
    private static final int RED = -65536;
    private static final int ORANGE = -23296;
    private final Minecraft minecraft;
    private final Map<BlockPos, BrainDebugRenderer.PoiInfo> pois = Maps.newHashMap();
    private final Map<UUID, BrainDebugRenderer.BrainDump> brainDumpsPerEntity = Maps.newHashMap();
    @Nullable
    private UUID lastLookedAtUuid;

    public BrainDebugRenderer(Minecraft param0) {
        this.minecraft = param0;
    }

    @Override
    public void clear() {
        this.pois.clear();
        this.brainDumpsPerEntity.clear();
        this.lastLookedAtUuid = null;
    }

    public void addPoi(BrainDebugRenderer.PoiInfo param0) {
        this.pois.put(param0.pos, param0);
    }

    public void removePoi(BlockPos param0) {
        this.pois.remove(param0);
    }

    public void setFreeTicketCount(BlockPos param0, int param1) {
        BrainDebugRenderer.PoiInfo var0 = this.pois.get(param0);
        if (var0 == null) {
            LOGGER.warn("Strange, setFreeTicketCount was called for an unknown POI: {}", param0);
        } else {
            var0.freeTicketCount = param1;
        }
    }

    public void addOrUpdateBrainDump(BrainDebugRenderer.BrainDump param0) {
        this.brainDumpsPerEntity.put(param0.uuid, param0);
    }

    public void removeBrainDump(int param0) {
        this.brainDumpsPerEntity.values().removeIf(param1 -> param1.id == param0);
    }

    @Override
    public void render(PoseStack param0, MultiBufferSource param1, double param2, double param3, double param4) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        this.clearRemovedEntities();
        this.doRender(param2, param3, param4);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        if (!this.minecraft.player.isSpectator()) {
            this.updateLastLookedAtUuid();
        }

    }

    private void clearRemovedEntities() {
        this.brainDumpsPerEntity.entrySet().removeIf(param0 -> {
            Entity var0 = this.minecraft.level.getEntity(param0.getValue().id);
            return var0 == null || var0.isRemoved();
        });
    }

    private void doRender(double param0, double param1, double param2) {
        BlockPos var0 = new BlockPos(param0, param1, param2);
        this.brainDumpsPerEntity.values().forEach(param3 -> {
            if (this.isPlayerCloseEnoughToMob(param3)) {
                this.renderBrainInfo(param3, param0, param1, param2);
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
        renderTextOverPos(param1 + "", param0, 0, -256);
        renderTextOverPos("Ghost POI", param0, 1, -65536);
    }

    private void renderPoiInfo(BrainDebugRenderer.PoiInfo param0) {
        int var0 = 0;
        Set<String> var1 = this.getTicketHolderNames(param0);
        if (var1.size() < 4) {
            renderTextOverPoi("Owners: " + var1, param0, var0, -256);
        } else {
            renderTextOverPoi(var1.size() + " ticket holders", param0, var0, -256);
        }

        ++var0;
        Set<String> var2 = this.getPotentialTicketHolderNames(param0);
        if (var2.size() < 4) {
            renderTextOverPoi("Candidates: " + var2, param0, var0, -23296);
        } else {
            renderTextOverPoi(var2.size() + " potential owners", param0, var0, -23296);
        }

        renderTextOverPoi("Free tickets: " + param0.freeTicketCount, param0, ++var0, -256);
        renderTextOverPoi(param0.type, param0, ++var0, -1);
    }

    private void renderPath(BrainDebugRenderer.BrainDump param0, double param1, double param2, double param3) {
        if (param0.path != null) {
            PathfindingRenderer.renderPath(param0.path, 0.5F, false, false, param1, param2, param3);
        }

    }

    private void renderBrainInfo(BrainDebugRenderer.BrainDump param0, double param1, double param2, double param3) {
        boolean var0 = this.isMobSelected(param0);
        int var1 = 0;
        renderTextOverMob(param0.pos, var1, param0.name, -1, 0.03F);
        ++var1;
        if (var0) {
            renderTextOverMob(param0.pos, var1, param0.profession + " " + param0.xp + " xp", -1, 0.02F);
            ++var1;
        }

        if (var0) {
            int var2 = param0.health < param0.maxHealth ? -23296 : -1;
            renderTextOverMob(
                param0.pos,
                var1,
                "health: " + String.format(Locale.ROOT, "%.1f", param0.health) + " / " + String.format(Locale.ROOT, "%.1f", param0.maxHealth),
                var2,
                0.02F
            );
            ++var1;
        }

        if (var0 && !param0.inventory.equals("")) {
            renderTextOverMob(param0.pos, var1, param0.inventory, -98404, 0.02F);
            ++var1;
        }

        if (var0) {
            for(String var3 : param0.behaviors) {
                renderTextOverMob(param0.pos, var1, var3, -16711681, 0.02F);
                ++var1;
            }
        }

        if (var0) {
            for(String var4 : param0.activities) {
                renderTextOverMob(param0.pos, var1, var4, -16711936, 0.02F);
                ++var1;
            }
        }

        if (param0.wantsGolem) {
            renderTextOverMob(param0.pos, var1, "Wants Golem", -23296, 0.02F);
            ++var1;
        }

        if (var0 && param0.angerLevel != -1) {
            renderTextOverMob(param0.pos, var1, "Anger Level: " + param0.angerLevel, -98404, 0.02F);
            ++var1;
        }

        if (var0) {
            for(String var5 : param0.gossips) {
                if (var5.startsWith(param0.name)) {
                    renderTextOverMob(param0.pos, var1, var5, -1, 0.02F);
                } else {
                    renderTextOverMob(param0.pos, var1, var5, -23296, 0.02F);
                }

                ++var1;
            }
        }

        if (var0) {
            for(String var6 : Lists.reverse(param0.memories)) {
                renderTextOverMob(param0.pos, var1, var6, -3355444, 0.02F);
                ++var1;
            }
        }

        if (var0) {
            this.renderPath(param0, param1, param2, param3);
        }

    }

    private static void renderTextOverPoi(String param0, BrainDebugRenderer.PoiInfo param1, int param2, int param3) {
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

    private Set<String> getTicketHolderNames(BrainDebugRenderer.PoiInfo param0) {
        return this.getTicketHolders(param0.pos).stream().map(DebugEntityNameGenerator::getEntityName).collect(Collectors.toSet());
    }

    private Set<String> getPotentialTicketHolderNames(BrainDebugRenderer.PoiInfo param0) {
        return this.getPotentialTicketHolders(param0.pos).stream().map(DebugEntityNameGenerator::getEntityName).collect(Collectors.toSet());
    }

    private boolean isMobSelected(BrainDebugRenderer.BrainDump param0) {
        return Objects.equals(this.lastLookedAtUuid, param0.uuid);
    }

    private boolean isPlayerCloseEnoughToMob(BrainDebugRenderer.BrainDump param0) {
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
            .map(BrainDebugRenderer.BrainDump::getUuid)
            .collect(Collectors.toSet());
    }

    private Collection<UUID> getPotentialTicketHolders(BlockPos param0) {
        return this.brainDumpsPerEntity
            .values()
            .stream()
            .filter(param1 -> param1.hasPotentialPoi(param0))
            .map(BrainDebugRenderer.BrainDump::getUuid)
            .collect(Collectors.toSet());
    }

    private Map<BlockPos, List<String>> getGhostPois() {
        Map<BlockPos, List<String>> var0 = Maps.newHashMap();

        for(BrainDebugRenderer.BrainDump var1 : this.brainDumpsPerEntity.values()) {
            for(BlockPos var2 : Iterables.concat(var1.pois, var1.potentialPois)) {
                if (!this.pois.containsKey(var2)) {
                    var0.computeIfAbsent(var2, param0 -> Lists.newArrayList()).add(var1.name);
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
        public final float health;
        public final float maxHealth;
        public final Position pos;
        public final String inventory;
        public final Path path;
        public final boolean wantsGolem;
        public final int angerLevel;
        public final List<String> activities = Lists.newArrayList();
        public final List<String> behaviors = Lists.newArrayList();
        public final List<String> memories = Lists.newArrayList();
        public final List<String> gossips = Lists.newArrayList();
        public final Set<BlockPos> pois = Sets.newHashSet();
        public final Set<BlockPos> potentialPois = Sets.newHashSet();

        public BrainDump(
            UUID param0,
            int param1,
            String param2,
            String param3,
            int param4,
            float param5,
            float param6,
            Position param7,
            String param8,
            @Nullable Path param9,
            boolean param10,
            int param11
        ) {
            this.uuid = param0;
            this.id = param1;
            this.name = param2;
            this.profession = param3;
            this.xp = param4;
            this.health = param5;
            this.maxHealth = param6;
            this.pos = param7;
            this.inventory = param8;
            this.path = param9;
            this.wantsGolem = param10;
            this.angerLevel = param11;
        }

        boolean hasPoi(BlockPos param0) {
            return this.pois.stream().anyMatch(param0::equals);
        }

        boolean hasPotentialPoi(BlockPos param0) {
            return this.potentialPois.contains(param0);
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
