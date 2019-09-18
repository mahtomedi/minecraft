package net.minecraft.client.renderer.debug;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DebugRenderer {
    public final PathfindingRenderer pathfindingRenderer;
    public final DebugRenderer.SimpleDebugRenderer waterDebugRenderer;
    public final DebugRenderer.SimpleDebugRenderer chunkBorderRenderer;
    public final DebugRenderer.SimpleDebugRenderer heightMapRenderer;
    public final DebugRenderer.SimpleDebugRenderer collisionBoxRenderer;
    public final DebugRenderer.SimpleDebugRenderer neighborsUpdateRenderer;
    public final CaveDebugRenderer caveRenderer;
    public final StructureRenderer structureRenderer;
    public final DebugRenderer.SimpleDebugRenderer lightDebugRenderer;
    public final DebugRenderer.SimpleDebugRenderer worldGenAttemptRenderer;
    public final DebugRenderer.SimpleDebugRenderer solidFaceRenderer;
    public final DebugRenderer.SimpleDebugRenderer chunkRenderer;
    public final VillageDebugRenderer villageDebugRenderer;
    public final RaidDebugRenderer raidDebugRenderer;
    public final GoalSelectorDebugRenderer goalSelectorRenderer;
    public final GameTestDebugRenderer gameTestDebugRenderer;
    private boolean renderChunkborder;

    public DebugRenderer(Minecraft param0) {
        this.pathfindingRenderer = new PathfindingRenderer(param0);
        this.waterDebugRenderer = new WaterDebugRenderer(param0);
        this.chunkBorderRenderer = new ChunkBorderRenderer(param0);
        this.heightMapRenderer = new HeightMapRenderer(param0);
        this.collisionBoxRenderer = new CollisionBoxRenderer(param0);
        this.neighborsUpdateRenderer = new NeighborsUpdateRenderer(param0);
        this.caveRenderer = new CaveDebugRenderer(param0);
        this.structureRenderer = new StructureRenderer(param0);
        this.lightDebugRenderer = new LightDebugRenderer(param0);
        this.worldGenAttemptRenderer = new WorldGenAttemptRenderer(param0);
        this.solidFaceRenderer = new SolidFaceRenderer(param0);
        this.chunkRenderer = new ChunkDebugRenderer(param0);
        this.villageDebugRenderer = new VillageDebugRenderer(param0);
        this.raidDebugRenderer = new RaidDebugRenderer(param0);
        this.goalSelectorRenderer = new GoalSelectorDebugRenderer(param0);
        this.gameTestDebugRenderer = new GameTestDebugRenderer();
    }

    public void clear() {
        this.pathfindingRenderer.clear();
        this.waterDebugRenderer.clear();
        this.chunkBorderRenderer.clear();
        this.heightMapRenderer.clear();
        this.collisionBoxRenderer.clear();
        this.neighborsUpdateRenderer.clear();
        this.caveRenderer.clear();
        this.structureRenderer.clear();
        this.lightDebugRenderer.clear();
        this.worldGenAttemptRenderer.clear();
        this.solidFaceRenderer.clear();
        this.chunkRenderer.clear();
        this.villageDebugRenderer.clear();
        this.raidDebugRenderer.clear();
        this.goalSelectorRenderer.clear();
        this.gameTestDebugRenderer.clear();
    }

    public boolean switchRenderChunkborder() {
        this.renderChunkborder = !this.renderChunkborder;
        return this.renderChunkborder;
    }

    @OnlyIn(Dist.CLIENT)
    public interface SimpleDebugRenderer {
        default void clear() {
        }
    }
}
