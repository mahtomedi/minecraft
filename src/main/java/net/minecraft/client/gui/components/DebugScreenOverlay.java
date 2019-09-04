package net.minecraft.client.gui.components;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.DataFixUtils;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DebugScreenOverlay extends GuiComponent {
    private static final Map<Heightmap.Types, String> HEIGHTMAP_NAMES = Util.make(new EnumMap<>(Heightmap.Types.class), param0 -> {
        param0.put(Heightmap.Types.WORLD_SURFACE_WG, "SW");
        param0.put(Heightmap.Types.WORLD_SURFACE, "S");
        param0.put(Heightmap.Types.OCEAN_FLOOR_WG, "OW");
        param0.put(Heightmap.Types.OCEAN_FLOOR, "O");
        param0.put(Heightmap.Types.MOTION_BLOCKING, "M");
        param0.put(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, "ML");
    });
    private final Minecraft minecraft;
    private final Font font;
    private HitResult block;
    private HitResult liquid;
    @Nullable
    private ChunkPos lastPos;
    @Nullable
    private LevelChunk clientChunk;
    @Nullable
    private CompletableFuture<LevelChunk> serverChunk;

    public DebugScreenOverlay(Minecraft param0) {
        this.minecraft = param0;
        this.font = param0.font;
    }

    public void clearChunkCache() {
        this.serverChunk = null;
        this.clientChunk = null;
    }

    public void render() {
        this.minecraft.getProfiler().push("debug");
        RenderSystem.pushMatrix();
        Entity var0 = this.minecraft.getCameraEntity();
        this.block = var0.pick(20.0, 0.0F, false);
        this.liquid = var0.pick(20.0, 0.0F, true);
        this.drawGameInformation();
        this.drawSystemInformation();
        RenderSystem.popMatrix();
        if (this.minecraft.options.renderFpsChart) {
            int var1 = this.minecraft.window.getGuiScaledWidth();
            this.drawChart(this.minecraft.getFrameTimer(), 0, var1 / 2, true);
            IntegratedServer var2 = this.minecraft.getSingleplayerServer();
            if (var2 != null) {
                this.drawChart(var2.getFrameTimer(), var1 - Math.min(var1 / 2, 240), var1 / 2, false);
            }
        }

        this.minecraft.getProfiler().pop();
    }

    protected void drawGameInformation() {
        List<String> var0 = this.getGameInformation();
        var0.add("");
        boolean var1 = this.minecraft.getSingleplayerServer() != null;
        var0.add(
            "Debug: Pie [shift]: "
                + (this.minecraft.options.renderDebugCharts ? "visible" : "hidden")
                + (var1 ? " FPS + TPS" : " FPS")
                + " [alt]: "
                + (this.minecraft.options.renderFpsChart ? "visible" : "hidden")
        );
        var0.add("For help: press F3 + Q");

        for(int var2 = 0; var2 < var0.size(); ++var2) {
            String var3 = var0.get(var2);
            if (!Strings.isNullOrEmpty(var3)) {
                int var4 = 9;
                int var5 = this.font.width(var3);
                int var6 = 2;
                int var7 = 2 + var4 * var2;
                fill(1, var7 - 1, 2 + var5 + 1, var7 + var4 - 1, -1873784752);
                this.font.draw(var3, 2.0F, (float)var7, 14737632);
            }
        }

    }

    protected void drawSystemInformation() {
        List<String> var0 = this.getSystemInformation();

        for(int var1 = 0; var1 < var0.size(); ++var1) {
            String var2 = var0.get(var1);
            if (!Strings.isNullOrEmpty(var2)) {
                int var3 = 9;
                int var4 = this.font.width(var2);
                int var5 = this.minecraft.window.getGuiScaledWidth() - 2 - var4;
                int var6 = 2 + var3 * var1;
                fill(var5 - 1, var6 - 1, var5 + var4 + 1, var6 + var3 - 1, -1873784752);
                this.font.draw(var2, (float)var5, (float)var6, 14737632);
            }
        }

    }

    protected List<String> getGameInformation() {
        IntegratedServer var0 = this.minecraft.getSingleplayerServer();
        Connection var1 = this.minecraft.getConnection().getConnection();
        float var2 = var1.getAverageSentPackets();
        float var3 = var1.getAverageReceivedPackets();
        String var4;
        if (var0 != null) {
            var4 = String.format("Integrated server @ %.0f ms ticks, %.0f tx, %.0f rx", var0.getAverageTickTime(), var2, var3);
        } else {
            var4 = String.format("\"%s\" server, %.0f tx, %.0f rx", this.minecraft.player.getServerBrand(), var2, var3);
        }

        BlockPos var6 = new BlockPos(
            this.minecraft.getCameraEntity().x, this.minecraft.getCameraEntity().getBoundingBox().minY, this.minecraft.getCameraEntity().z
        );
        if (this.minecraft.showOnlyReducedInfo()) {
            return Lists.newArrayList(
                "Minecraft "
                    + SharedConstants.getCurrentVersion().getName()
                    + " ("
                    + this.minecraft.getLaunchedVersion()
                    + "/"
                    + ClientBrandRetriever.getClientModName()
                    + ")",
                this.minecraft.fpsString,
                var4,
                this.minecraft.levelRenderer.getChunkStatistics(),
                this.minecraft.levelRenderer.getEntityStatistics(),
                "P: " + this.minecraft.particleEngine.countParticles() + ". T: " + this.minecraft.level.getEntityCount(),
                this.minecraft.level.gatherChunkSourceStats(),
                "",
                String.format("Chunk-relative: %d %d %d", var6.getX() & 15, var6.getY() & 15, var6.getZ() & 15)
            );
        } else {
            Entity var7 = this.minecraft.getCameraEntity();
            Direction var8 = var7.getDirection();
            String var9;
            switch(var8) {
                case NORTH:
                    var9 = "Towards negative Z";
                    break;
                case SOUTH:
                    var9 = "Towards positive Z";
                    break;
                case WEST:
                    var9 = "Towards negative X";
                    break;
                case EAST:
                    var9 = "Towards positive X";
                    break;
                default:
                    var9 = "Invalid";
            }

            ChunkPos var14 = new ChunkPos(var6);
            if (!Objects.equals(this.lastPos, var14)) {
                this.lastPos = var14;
                this.clearChunkCache();
            }

            Level var15 = this.getLevel();
            LongSet var16 = (LongSet)(var15 instanceof ServerLevel ? ((ServerLevel)var15).getForcedChunks() : LongSets.EMPTY_SET);
            List<String> var17 = Lists.newArrayList(
                "Minecraft "
                    + SharedConstants.getCurrentVersion().getName()
                    + " ("
                    + this.minecraft.getLaunchedVersion()
                    + "/"
                    + ClientBrandRetriever.getClientModName()
                    + ("release".equalsIgnoreCase(this.minecraft.getVersionType()) ? "" : "/" + this.minecraft.getVersionType())
                    + ")",
                this.minecraft.fpsString,
                var4,
                this.minecraft.levelRenderer.getChunkStatistics(),
                this.minecraft.levelRenderer.getEntityStatistics(),
                "P: " + this.minecraft.particleEngine.countParticles() + ". T: " + this.minecraft.level.getEntityCount(),
                this.minecraft.level.gatherChunkSourceStats()
            );
            String var18 = this.getServerChunkStats();
            if (var18 != null) {
                var17.add(var18);
            }

            var17.add(DimensionType.getName(this.minecraft.level.dimension.getType()).toString() + " FC: " + Integer.toString(var16.size()));
            var17.add("");
            var17.add(
                String.format(
                    Locale.ROOT,
                    "XYZ: %.3f / %.5f / %.3f",
                    this.minecraft.getCameraEntity().x,
                    this.minecraft.getCameraEntity().getBoundingBox().minY,
                    this.minecraft.getCameraEntity().z
                )
            );
            var17.add(String.format("Block: %d %d %d", var6.getX(), var6.getY(), var6.getZ()));
            var17.add(
                String.format(
                    "Chunk: %d %d %d in %d %d %d", var6.getX() & 15, var6.getY() & 15, var6.getZ() & 15, var6.getX() >> 4, var6.getY() >> 4, var6.getZ() >> 4
                )
            );
            var17.add(String.format(Locale.ROOT, "Facing: %s (%s) (%.1f / %.1f)", var8, var9, Mth.wrapDegrees(var7.yRot), Mth.wrapDegrees(var7.xRot)));
            if (this.minecraft.level != null) {
                if (this.minecraft.level.hasChunkAt(var6)) {
                    LevelChunk var19 = this.getClientChunk();
                    if (var19.isEmpty()) {
                        var17.add("Waiting for chunk...");
                    } else {
                        int var20 = this.minecraft.level.getChunkSource().getLightEngine().getRawBrightness(var6, 0);
                        int var21 = this.minecraft.level.getBrightness(LightLayer.SKY, var6);
                        int var22 = this.minecraft.level.getBrightness(LightLayer.BLOCK, var6);
                        var17.add("Client Light: " + var20 + " (" + var21 + " sky, " + var22 + " block)");
                        LevelChunk var23 = this.getServerChunk();
                        if (var23 != null) {
                            LevelLightEngine var24 = var15.getChunkSource().getLightEngine();
                            var17.add(
                                "Server Light: ("
                                    + var24.getLayerListener(LightLayer.SKY).getLightValue(var6)
                                    + " sky, "
                                    + var24.getLayerListener(LightLayer.BLOCK).getLightValue(var6)
                                    + " block)"
                            );
                        }

                        StringBuilder var25 = new StringBuilder("CH");

                        for(Heightmap.Types var26 : Heightmap.Types.values()) {
                            if (var26.sendToClient()) {
                                var25.append(" ").append(HEIGHTMAP_NAMES.get(var26)).append(": ").append(var19.getHeight(var26, var6.getX(), var6.getZ()));
                            }
                        }

                        var17.add(var25.toString());
                        if (var23 != null) {
                            var25.setLength(0);
                            var25.append("SH");

                            for(Heightmap.Types var27 : Heightmap.Types.values()) {
                                if (var27.keepAfterWorldgen()) {
                                    var25.append(" ").append(HEIGHTMAP_NAMES.get(var27)).append(": ").append(var23.getHeight(var27, var6.getX(), var6.getZ()));
                                }
                            }

                            var17.add(var25.toString());
                        }

                        if (var6.getY() >= 0 && var6.getY() < 256) {
                            var17.add("Biome: " + Registry.BIOME.getKey(this.minecraft.level.getBiome(var6)));
                            long var28 = 0L;
                            float var29 = 0.0F;
                            if (var23 != null) {
                                var29 = var15.getMoonBrightness();
                                var28 = var23.getInhabitedTime();
                            }

                            DifficultyInstance var30 = new DifficultyInstance(var15.getDifficulty(), var15.getDayTime(), var28, var29);
                            var17.add(
                                String.format(
                                    Locale.ROOT,
                                    "Local Difficulty: %.2f // %.2f (Day %d)",
                                    var30.getEffectiveDifficulty(),
                                    var30.getSpecialMultiplier(),
                                    this.minecraft.level.getDayTime() / 24000L
                                )
                            );
                        }
                    }
                } else {
                    var17.add("Outside of world...");
                }
            } else {
                var17.add("Outside of world...");
            }

            if (this.minecraft.gameRenderer != null && this.minecraft.gameRenderer.postEffectActive()) {
                var17.add("Shader: " + this.minecraft.gameRenderer.currentEffect().getName());
            }

            if (this.block.getType() == HitResult.Type.BLOCK) {
                BlockPos var31 = ((BlockHitResult)this.block).getBlockPos();
                var17.add(String.format("Looking at block: %d %d %d", var31.getX(), var31.getY(), var31.getZ()));
            }

            if (this.liquid.getType() == HitResult.Type.BLOCK) {
                BlockPos var32 = ((BlockHitResult)this.liquid).getBlockPos();
                var17.add(String.format("Looking at liquid: %d %d %d", var32.getX(), var32.getY(), var32.getZ()));
            }

            var17.add(this.minecraft.getSoundManager().getDebugString());
            return var17;
        }
    }

    @Nullable
    private String getServerChunkStats() {
        IntegratedServer var0 = this.minecraft.getSingleplayerServer();
        if (var0 != null) {
            ServerLevel var1 = var0.getLevel(this.minecraft.level.getDimension().getType());
            if (var1 != null) {
                return var1.gatherChunkSourceStats();
            }
        }

        return null;
    }

    private Level getLevel() {
        return DataFixUtils.orElse(
            Optional.ofNullable(this.minecraft.getSingleplayerServer()).map(param0 -> param0.getLevel(this.minecraft.level.dimension.getType())),
            this.minecraft.level
        );
    }

    @Nullable
    private LevelChunk getServerChunk() {
        if (this.serverChunk == null) {
            IntegratedServer var0 = this.minecraft.getSingleplayerServer();
            if (var0 != null) {
                ServerLevel var1 = var0.getLevel(this.minecraft.level.dimension.getType());
                if (var1 != null) {
                    this.serverChunk = var1.getChunkSource()
                        .getChunkFuture(this.lastPos.x, this.lastPos.z, ChunkStatus.FULL, false)
                        .thenApply(param0 -> param0.map(param0x -> (LevelChunk)param0x, param0x -> null));
                }
            }

            if (this.serverChunk == null) {
                this.serverChunk = CompletableFuture.completedFuture(this.getClientChunk());
            }
        }

        return this.serverChunk.getNow(null);
    }

    private LevelChunk getClientChunk() {
        if (this.clientChunk == null) {
            this.clientChunk = this.minecraft.level.getChunk(this.lastPos.x, this.lastPos.z);
        }

        return this.clientChunk;
    }

    protected List<String> getSystemInformation() {
        long var0 = Runtime.getRuntime().maxMemory();
        long var1 = Runtime.getRuntime().totalMemory();
        long var2 = Runtime.getRuntime().freeMemory();
        long var3 = var1 - var2;
        List<String> var4 = Lists.newArrayList(
            String.format("Java: %s %dbit", System.getProperty("java.version"), this.minecraft.is64Bit() ? 64 : 32),
            String.format("Mem: % 2d%% %03d/%03dMB", var3 * 100L / var0, bytesToMegabytes(var3), bytesToMegabytes(var0)),
            String.format("Allocated: % 2d%% %03dMB", var1 * 100L / var0, bytesToMegabytes(var1)),
            "",
            String.format("CPU: %s", GlUtil.getCpuInfo()),
            "",
            String.format("Display: %dx%d (%s)", Minecraft.getInstance().window.getWidth(), Minecraft.getInstance().window.getHeight(), GlUtil.getVendor()),
            GlUtil.getRenderer(),
            GlUtil.getOpenGLVersion()
        );
        if (this.minecraft.showOnlyReducedInfo()) {
            return var4;
        } else {
            if (this.block.getType() == HitResult.Type.BLOCK) {
                BlockPos var5 = ((BlockHitResult)this.block).getBlockPos();
                BlockState var6 = this.minecraft.level.getBlockState(var5);
                var4.add("");
                var4.add(ChatFormatting.UNDERLINE + "Targeted Block");
                var4.add(String.valueOf(Registry.BLOCK.getKey(var6.getBlock())));

                for(Entry<Property<?>, Comparable<?>> var7 : var6.getValues().entrySet()) {
                    var4.add(this.getPropertyValueString(var7));
                }

                for(ResourceLocation var8 : this.minecraft.getConnection().getTags().getBlocks().getMatchingTags(var6.getBlock())) {
                    var4.add("#" + var8);
                }
            }

            if (this.liquid.getType() == HitResult.Type.BLOCK) {
                BlockPos var9 = ((BlockHitResult)this.liquid).getBlockPos();
                FluidState var10 = this.minecraft.level.getFluidState(var9);
                var4.add("");
                var4.add(ChatFormatting.UNDERLINE + "Targeted Fluid");
                var4.add(String.valueOf(Registry.FLUID.getKey(var10.getType())));

                for(Entry<Property<?>, Comparable<?>> var11 : var10.getValues().entrySet()) {
                    var4.add(this.getPropertyValueString(var11));
                }

                for(ResourceLocation var12 : this.minecraft.getConnection().getTags().getFluids().getMatchingTags(var10.getType())) {
                    var4.add("#" + var12);
                }
            }

            Entity var13 = this.minecraft.crosshairPickEntity;
            if (var13 != null) {
                var4.add("");
                var4.add(ChatFormatting.UNDERLINE + "Targeted Entity");
                var4.add(String.valueOf(Registry.ENTITY_TYPE.getKey(var13.getType())));
            }

            return var4;
        }
    }

    private String getPropertyValueString(Entry<Property<?>, Comparable<?>> param0) {
        Property<?> var0 = param0.getKey();
        Comparable<?> var1 = param0.getValue();
        String var2 = Util.getPropertyName(var0, var1);
        if (Boolean.TRUE.equals(var1)) {
            var2 = ChatFormatting.GREEN + var2;
        } else if (Boolean.FALSE.equals(var1)) {
            var2 = ChatFormatting.RED + var2;
        }

        return var0.getName() + ": " + var2;
    }

    private void drawChart(FrameTimer param0, int param1, int param2, boolean param3) {
        RenderSystem.disableDepthTest();
        int var0 = param0.getLogStart();
        int var1 = param0.getLogEnd();
        long[] var2 = param0.getLog();
        int var4 = param1;
        int var5 = Math.max(0, var2.length - param2);
        int var6 = var2.length - var5;
        int var3 = param0.wrapIndex(var0 + var5);
        long var7 = 0L;
        int var8x = Integer.MAX_VALUE;
        int var9 = Integer.MIN_VALUE;

        for(int var10 = 0; var10 < var6; ++var10) {
            int var11 = (int)(var2[param0.wrapIndex(var3 + var10)] / 1000000L);
            var8x = Math.min(var8x, var11);
            var9 = Math.max(var9, var11);
            var7 += (long)var11;
        }

        int var12 = this.minecraft.window.getGuiScaledHeight();
        fill(param1, var12 - 60, param1 + var6, var12, -1873784752);

        while(var3 != var1) {
            int var13 = param0.scaleSampleTo(var2[var3], param3 ? 30 : 60, param3 ? 60 : 20);
            int var14 = param3 ? 100 : 60;
            int var15 = this.getSampleColor(Mth.clamp(var13, 0, var14), 0, var14 / 2, var14);
            this.vLine(var4, var12, var12 - var13, var15);
            ++var4;
            var3 = param0.wrapIndex(var3 + 1);
        }

        if (param3) {
            fill(param1 + 1, var12 - 30 + 1, param1 + 14, var12 - 30 + 10, -1873784752);
            this.font.draw("60 FPS", (float)(param1 + 2), (float)(var12 - 30 + 2), 14737632);
            this.hLine(param1, param1 + var6 - 1, var12 - 30, -1);
            fill(param1 + 1, var12 - 60 + 1, param1 + 14, var12 - 60 + 10, -1873784752);
            this.font.draw("30 FPS", (float)(param1 + 2), (float)(var12 - 60 + 2), 14737632);
            this.hLine(param1, param1 + var6 - 1, var12 - 60, -1);
        } else {
            fill(param1 + 1, var12 - 60 + 1, param1 + 14, var12 - 60 + 10, -1873784752);
            this.font.draw("20 TPS", (float)(param1 + 2), (float)(var12 - 60 + 2), 14737632);
            this.hLine(param1, param1 + var6 - 1, var12 - 60, -1);
        }

        this.hLine(param1, param1 + var6 - 1, var12 - 1, -1);
        this.vLine(param1, var12 - 60, var12, -1);
        this.vLine(param1 + var6 - 1, var12 - 60, var12, -1);
        if (param3 && this.minecraft.options.framerateLimit > 0 && this.minecraft.options.framerateLimit <= 250) {
            this.hLine(param1, param1 + var6 - 1, var12 - 1 - (int)(1800.0 / (double)this.minecraft.options.framerateLimit), -16711681);
        }

        String var16 = var8x + " ms min";
        String var17 = var7 / (long)var6 + " ms avg";
        String var18 = var9 + " ms max";
        this.font.drawShadow(var16, (float)(param1 + 2), (float)(var12 - 60 - 9), 14737632);
        this.font.drawShadow(var17, (float)(param1 + var6 / 2 - this.font.width(var17) / 2), (float)(var12 - 60 - 9), 14737632);
        this.font.drawShadow(var18, (float)(param1 + var6 - this.font.width(var18)), (float)(var12 - 60 - 9), 14737632);
        RenderSystem.enableDepthTest();
    }

    private int getSampleColor(int param0, int param1, int param2, int param3) {
        return param0 < param2
            ? this.colorLerp(-16711936, -256, (float)param0 / (float)param2)
            : this.colorLerp(-256, -65536, (float)(param0 - param2) / (float)(param3 - param2));
    }

    private int colorLerp(int param0, int param1, float param2) {
        int var0 = param0 >> 24 & 0xFF;
        int var1 = param0 >> 16 & 0xFF;
        int var2 = param0 >> 8 & 0xFF;
        int var3 = param0 & 0xFF;
        int var4 = param1 >> 24 & 0xFF;
        int var5 = param1 >> 16 & 0xFF;
        int var6 = param1 >> 8 & 0xFF;
        int var7 = param1 & 0xFF;
        int var8 = Mth.clamp((int)Mth.lerp(param2, (float)var0, (float)var4), 0, 255);
        int var9 = Mth.clamp((int)Mth.lerp(param2, (float)var1, (float)var5), 0, 255);
        int var10 = Mth.clamp((int)Mth.lerp(param2, (float)var2, (float)var6), 0, 255);
        int var11 = Mth.clamp((int)Mth.lerp(param2, (float)var3, (float)var7), 0, 255);
        return var8 << 24 | var9 << 16 | var10 << 8 | var11;
    }

    private static long bytesToMegabytes(long param0) {
        return param0 / 1024L / 1024L;
    }
}
