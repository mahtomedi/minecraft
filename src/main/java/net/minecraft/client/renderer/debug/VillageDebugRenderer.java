package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.SectionPos;
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
