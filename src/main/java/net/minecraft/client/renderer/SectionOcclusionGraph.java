package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkTrackingView;
import net.minecraft.util.Mth;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class SectionOcclusionGraph {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final int MINIMUM_ADVANCED_CULLING_DISTANCE = 60;
    private static final double CEILED_SECTION_DIAGONAL = Math.ceil(Math.sqrt(3.0) * 16.0);
    private boolean needsFullUpdate = true;
    @Nullable
    private Future<?> fullUpdateTask;
    @Nullable
    private ViewArea viewArea;
    private final AtomicReference<SectionOcclusionGraph.GraphState> currentGraph = new AtomicReference<>();
    private final AtomicReference<SectionOcclusionGraph.GraphEvents> nextGraphEvents = new AtomicReference<>();
    private final AtomicBoolean needsFrustumUpdate = new AtomicBoolean(false);

    public void waitAndReset(@Nullable ViewArea param0) {
        if (this.fullUpdateTask != null) {
            try {
                this.fullUpdateTask.get();
                this.fullUpdateTask = null;
            } catch (Exception var3) {
                LOGGER.warn("Full update failed", (Throwable)var3);
            }
        }

        this.viewArea = param0;
        if (param0 != null) {
            this.currentGraph.set(new SectionOcclusionGraph.GraphState(param0.sections.length));
            this.invalidate();
        } else {
            this.currentGraph.set(null);
        }

    }

    public void invalidate() {
        this.needsFullUpdate = true;
    }

    public void addSectionsInFrustum(Frustum param0, List<SectionRenderDispatcher.RenderSection> param1) {
        for(SectionOcclusionGraph.Node var0 : this.currentGraph.get().storage().renderSections) {
            if (param0.isVisible(var0.section.getBoundingBox())) {
                param1.add(var0.section);
            }
        }

    }

    public boolean consumeFrustumUpdate() {
        return this.needsFrustumUpdate.compareAndSet(true, false);
    }

    public void onChunkLoaded(ChunkPos param0) {
        SectionOcclusionGraph.GraphEvents var0 = this.nextGraphEvents.get();
        if (var0 != null) {
            this.addNeighbors(var0, param0);
        }

        SectionOcclusionGraph.GraphEvents var1 = this.currentGraph.get().events;
        if (var1 != var0) {
            this.addNeighbors(var1, param0);
        }

    }

    public void onSectionCompiled(SectionRenderDispatcher.RenderSection param0) {
        SectionOcclusionGraph.GraphEvents var0 = this.nextGraphEvents.get();
        if (var0 != null) {
            var0.sectionsToPropagateFrom.add(param0);
        }

        SectionOcclusionGraph.GraphEvents var1 = this.currentGraph.get().events;
        if (var1 != var0) {
            var1.sectionsToPropagateFrom.add(param0);
        }

    }

    public void update(boolean param0, Camera param1, Frustum param2, List<SectionRenderDispatcher.RenderSection> param3) {
        Vec3 var0 = param1.getPosition();
        if (this.needsFullUpdate && (this.fullUpdateTask == null || this.fullUpdateTask.isDone())) {
            this.scheduleFullUpdate(param0, param1, var0);
        }

        this.runPartialUpdate(param0, param2, param3, var0);
    }

    private void scheduleFullUpdate(boolean param0, Camera param1, Vec3 param2) {
        this.needsFullUpdate = false;
        this.fullUpdateTask = Util.backgroundExecutor().submit(() -> {
            SectionOcclusionGraph.GraphState var0 = new SectionOcclusionGraph.GraphState(this.viewArea.sections.length);
            this.nextGraphEvents.set(var0.events);
            Queue<SectionOcclusionGraph.Node> var1x = Queues.newArrayDeque();
            this.initializeQueueForFullUpdate(param1, var1x);
            var1x.forEach(param1x -> var0.storage.sectionToNodeMap.put(param1x.section, param1x));
            this.runUpdates(var0.storage, param2, var1x, param0, param0x -> {
            });
            this.currentGraph.set(var0);
            this.nextGraphEvents.set(null);
            this.needsFrustumUpdate.set(true);
        });
    }

    private void runPartialUpdate(boolean param0, Frustum param1, List<SectionRenderDispatcher.RenderSection> param2, Vec3 param3) {
        SectionOcclusionGraph.GraphState var0 = this.currentGraph.get();
        this.queueSectionsWithNewNeighbors(var0);
        if (!var0.events.sectionsToPropagateFrom.isEmpty()) {
            Queue<SectionOcclusionGraph.Node> var1 = Queues.newArrayDeque();

            while(!var0.events.sectionsToPropagateFrom.isEmpty()) {
                SectionRenderDispatcher.RenderSection var2 = var0.events.sectionsToPropagateFrom.poll();
                SectionOcclusionGraph.Node var3 = var0.storage.sectionToNodeMap.get(var2);
                if (var3 != null && var3.section == var2) {
                    var1.add(var3);
                }
            }

            Frustum var4 = LevelRenderer.offsetFrustum(param1);
            Consumer<SectionRenderDispatcher.RenderSection> var5 = param2x -> {
                if (var4.isVisible(param2x.getBoundingBox())) {
                    param2.add(param2x);
                }

            };
            this.runUpdates(var0.storage, param3, var1, param0, var5);
        }

    }

    private void queueSectionsWithNewNeighbors(SectionOcclusionGraph.GraphState param0) {
        LongIterator var0 = param0.events.chunksWhichReceivedNeighbors.iterator();

        while(var0.hasNext()) {
            long var1 = var0.nextLong();
            List<SectionRenderDispatcher.RenderSection> var2 = param0.storage.chunksWaitingForNeighbors.get(var1);
            if (var2 != null && var2.get(0).hasAllNeighbors()) {
                param0.events.sectionsToPropagateFrom.addAll(var2);
                param0.storage.chunksWaitingForNeighbors.remove(var1);
            }
        }

        param0.events.chunksWhichReceivedNeighbors.clear();
    }

    private void addNeighbors(SectionOcclusionGraph.GraphEvents param0, ChunkPos param1) {
        param0.chunksWhichReceivedNeighbors.add(ChunkPos.asLong(param1.x - 1, param1.z));
        param0.chunksWhichReceivedNeighbors.add(ChunkPos.asLong(param1.x, param1.z - 1));
        param0.chunksWhichReceivedNeighbors.add(ChunkPos.asLong(param1.x + 1, param1.z));
        param0.chunksWhichReceivedNeighbors.add(ChunkPos.asLong(param1.x, param1.z + 1));
    }

    private void initializeQueueForFullUpdate(Camera param0, Queue<SectionOcclusionGraph.Node> param1) {
        int var0 = 16;
        Vec3 var1 = param0.getPosition();
        BlockPos var2 = param0.getBlockPosition();
        SectionRenderDispatcher.RenderSection var3 = this.viewArea.getRenderSectionAt(var2);
        if (var3 == null) {
            LevelHeightAccessor var4 = this.viewArea.getLevelHeightAccessor();
            boolean var5 = var2.getY() > var4.getMinBuildHeight();
            int var6 = var5 ? var4.getMaxBuildHeight() - 8 : var4.getMinBuildHeight() + 8;
            int var7 = Mth.floor(var1.x / 16.0) * 16;
            int var8 = Mth.floor(var1.z / 16.0) * 16;
            int var9 = this.viewArea.getViewDistance();
            List<SectionOcclusionGraph.Node> var10 = Lists.newArrayList();

            for(int var11 = -var9; var11 <= var9; ++var11) {
                for(int var12 = -var9; var12 <= var9; ++var12) {
                    SectionRenderDispatcher.RenderSection var13 = this.viewArea
                        .getRenderSectionAt(
                            new BlockPos(var7 + SectionPos.sectionToBlockCoord(var11, 8), var6, var8 + SectionPos.sectionToBlockCoord(var12, 8))
                        );
                    if (var13 != null && this.isInViewDistance(var2, var13.getOrigin())) {
                        Direction var14 = var5 ? Direction.DOWN : Direction.UP;
                        SectionOcclusionGraph.Node var15 = new SectionOcclusionGraph.Node(var13, var14, 0);
                        var15.setDirections(var15.directions, var14);
                        if (var11 > 0) {
                            var15.setDirections(var15.directions, Direction.EAST);
                        } else if (var11 < 0) {
                            var15.setDirections(var15.directions, Direction.WEST);
                        }

                        if (var12 > 0) {
                            var15.setDirections(var15.directions, Direction.SOUTH);
                        } else if (var12 < 0) {
                            var15.setDirections(var15.directions, Direction.NORTH);
                        }

                        var10.add(var15);
                    }
                }
            }

            var10.sort(Comparator.comparingDouble(param1x -> var2.distSqr(param1x.section.getOrigin().offset(8, 8, 8))));
            param1.addAll(var10);
        } else {
            param1.add(new SectionOcclusionGraph.Node(var3, null, 0));
        }

    }

    private void runUpdates(
        SectionOcclusionGraph.GraphStorage param0,
        Vec3 param1,
        Queue<SectionOcclusionGraph.Node> param2,
        boolean param3,
        Consumer<SectionRenderDispatcher.RenderSection> param4
    ) {
        int var0 = 16;
        BlockPos var1 = new BlockPos(Mth.floor(param1.x / 16.0) * 16, Mth.floor(param1.y / 16.0) * 16, Mth.floor(param1.z / 16.0) * 16);
        BlockPos var2 = var1.offset(8, 8, 8);

        while(!param2.isEmpty()) {
            SectionOcclusionGraph.Node var3 = param2.poll();
            SectionRenderDispatcher.RenderSection var4 = var3.section;
            if (param0.renderSections.add(var3)) {
                param4.accept(var3.section);
            }

            boolean var5 = Math.abs(var4.getOrigin().getX() - var1.getX()) > 60
                || Math.abs(var4.getOrigin().getY() - var1.getY()) > 60
                || Math.abs(var4.getOrigin().getZ() - var1.getZ()) > 60;

            for(Direction var6 : DIRECTIONS) {
                SectionRenderDispatcher.RenderSection var7 = this.getRelativeFrom(var1, var4, var6);
                if (var7 != null && (!param3 || !var3.hasDirection(var6.getOpposite()))) {
                    if (param3 && var3.hasSourceDirections()) {
                        SectionRenderDispatcher.CompiledSection var8 = var4.getCompiled();
                        boolean var9 = false;

                        for(int var10 = 0; var10 < DIRECTIONS.length; ++var10) {
                            if (var3.hasSourceDirection(var10) && var8.facesCanSeeEachother(DIRECTIONS[var10].getOpposite(), var6)) {
                                var9 = true;
                                break;
                            }
                        }

                        if (!var9) {
                            continue;
                        }
                    }

                    if (param3 && var5) {
                        BlockPos var11;
                        byte var10001;
                        label130: {
                            label129: {
                                var11 = var7.getOrigin();
                                if (var6.getAxis() == Direction.Axis.X) {
                                    if (var2.getX() > var11.getX()) {
                                        break label129;
                                    }
                                } else if (var2.getX() < var11.getX()) {
                                    break label129;
                                }

                                var10001 = 0;
                                break label130;
                            }

                            var10001 = 16;
                        }

                        byte var10002;
                        label122: {
                            label121: {
                                if (var6.getAxis() == Direction.Axis.Y) {
                                    if (var2.getY() > var11.getY()) {
                                        break label121;
                                    }
                                } else if (var2.getY() < var11.getY()) {
                                    break label121;
                                }

                                var10002 = 0;
                                break label122;
                            }

                            var10002 = 16;
                        }

                        byte var10003;
                        label114: {
                            label113: {
                                if (var6.getAxis() == Direction.Axis.Z) {
                                    if (var2.getZ() > var11.getZ()) {
                                        break label113;
                                    }
                                } else if (var2.getZ() < var11.getZ()) {
                                    break label113;
                                }

                                var10003 = 0;
                                break label114;
                            }

                            var10003 = 16;
                        }

                        BlockPos var12 = var11.offset(var10001, var10002, var10003);
                        Vec3 var13 = new Vec3((double)var12.getX(), (double)var12.getY(), (double)var12.getZ());
                        Vec3 var14 = param1.subtract(var13).normalize().scale(CEILED_SECTION_DIAGONAL);
                        boolean var15 = true;

                        while(param1.subtract(var13).lengthSqr() > 3600.0) {
                            var13 = var13.add(var14);
                            LevelHeightAccessor var16 = this.viewArea.getLevelHeightAccessor();
                            if (var13.y > (double)var16.getMaxBuildHeight() || var13.y < (double)var16.getMinBuildHeight()) {
                                break;
                            }

                            SectionRenderDispatcher.RenderSection var17 = this.viewArea.getRenderSectionAt(BlockPos.containing(var13.x, var13.y, var13.z));
                            if (var17 == null || param0.sectionToNodeMap.get(var17) == null) {
                                var15 = false;
                                break;
                            }
                        }

                        if (!var15) {
                            continue;
                        }
                    }

                    SectionOcclusionGraph.Node var18 = param0.sectionToNodeMap.get(var7);
                    if (var18 != null) {
                        var18.addSourceDirection(var6);
                    } else {
                        SectionOcclusionGraph.Node var19 = new SectionOcclusionGraph.Node(var7, var6, var3.step + 1);
                        var19.setDirections(var3.directions, var6);
                        if (var7.hasAllNeighbors()) {
                            param2.add(var19);
                            param0.sectionToNodeMap.put(var7, var19);
                        } else if (this.isInViewDistance(var1, var7.getOrigin())) {
                            param0.sectionToNodeMap.put(var7, var19);
                            param0.chunksWaitingForNeighbors.computeIfAbsent(ChunkPos.asLong(var7.getOrigin()), param0x -> new ArrayList()).add(var7);
                        }
                    }
                }
            }
        }

    }

    private boolean isInViewDistance(BlockPos param0, BlockPos param1) {
        int var0 = SectionPos.blockToSectionCoord(param0.getX());
        int var1 = SectionPos.blockToSectionCoord(param0.getZ());
        int var2 = SectionPos.blockToSectionCoord(param1.getX());
        int var3 = SectionPos.blockToSectionCoord(param1.getZ());
        return ChunkTrackingView.isInViewDistance(var0, var1, this.viewArea.getViewDistance(), var2, var3);
    }

    @Nullable
    private SectionRenderDispatcher.RenderSection getRelativeFrom(BlockPos param0, SectionRenderDispatcher.RenderSection param1, Direction param2) {
        BlockPos var0 = param1.getRelativeOrigin(param2);
        if (!this.isInViewDistance(param0, var0)) {
            return null;
        } else {
            return Mth.abs(param0.getY() - var0.getY()) > this.viewArea.getViewDistance() * 16 ? null : this.viewArea.getRenderSectionAt(var0);
        }
    }

    @Nullable
    @VisibleForDebug
    protected SectionOcclusionGraph.Node getNode(SectionRenderDispatcher.RenderSection param0) {
        return this.currentGraph.get().storage.sectionToNodeMap.get(param0);
    }

    @OnlyIn(Dist.CLIENT)
    static record GraphEvents(LongSet chunksWhichReceivedNeighbors, BlockingQueue<SectionRenderDispatcher.RenderSection> sectionsToPropagateFrom) {
        public GraphEvents() {
            this(new LongOpenHashSet(), new LinkedBlockingQueue<>());
        }
    }

    @OnlyIn(Dist.CLIENT)
    static record GraphState(SectionOcclusionGraph.GraphStorage storage, SectionOcclusionGraph.GraphEvents events) {
        public GraphState(int param0) {
            this(new SectionOcclusionGraph.GraphStorage(param0), new SectionOcclusionGraph.GraphEvents());
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class GraphStorage {
        public final SectionOcclusionGraph.SectionToNodeMap sectionToNodeMap;
        public final LinkedHashSet<SectionOcclusionGraph.Node> renderSections;
        public final Long2ObjectMap<List<SectionRenderDispatcher.RenderSection>> chunksWaitingForNeighbors;

        public GraphStorage(int param0) {
            this.sectionToNodeMap = new SectionOcclusionGraph.SectionToNodeMap(param0);
            this.renderSections = new LinkedHashSet<>(param0);
            this.chunksWaitingForNeighbors = new Long2ObjectOpenHashMap<>();
        }
    }

    @OnlyIn(Dist.CLIENT)
    @VisibleForDebug
    protected static class Node {
        @VisibleForDebug
        protected final SectionRenderDispatcher.RenderSection section;
        private byte sourceDirections;
        byte directions;
        @VisibleForDebug
        protected final int step;

        Node(SectionRenderDispatcher.RenderSection param0, @Nullable Direction param1, int param2) {
            this.section = param0;
            if (param1 != null) {
                this.addSourceDirection(param1);
            }

            this.step = param2;
        }

        void setDirections(byte param0, Direction param1) {
            this.directions = (byte)(this.directions | param0 | 1 << param1.ordinal());
        }

        boolean hasDirection(Direction param0) {
            return (this.directions & 1 << param0.ordinal()) > 0;
        }

        void addSourceDirection(Direction param0) {
            this.sourceDirections = (byte)(this.sourceDirections | this.sourceDirections | 1 << param0.ordinal());
        }

        @VisibleForDebug
        protected boolean hasSourceDirection(int param0) {
            return (this.sourceDirections & 1 << param0) > 0;
        }

        boolean hasSourceDirections() {
            return this.sourceDirections != 0;
        }

        @Override
        public int hashCode() {
            return this.section.getOrigin().hashCode();
        }

        @Override
        public boolean equals(Object param0) {
            if (!(param0 instanceof SectionOcclusionGraph.Node)) {
                return false;
            } else {
                SectionOcclusionGraph.Node var0 = (SectionOcclusionGraph.Node)param0;
                return this.section.getOrigin().equals(var0.section.getOrigin());
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class SectionToNodeMap {
        private final SectionOcclusionGraph.Node[] nodes;

        SectionToNodeMap(int param0) {
            this.nodes = new SectionOcclusionGraph.Node[param0];
        }

        public void put(SectionRenderDispatcher.RenderSection param0, SectionOcclusionGraph.Node param1) {
            this.nodes[param0.index] = param1;
        }

        @Nullable
        public SectionOcclusionGraph.Node get(SectionRenderDispatcher.RenderSection param0) {
            int var0 = param0.index;
            return var0 >= 0 && var0 < this.nodes.length ? this.nodes[var0] : null;
        }
    }
}
