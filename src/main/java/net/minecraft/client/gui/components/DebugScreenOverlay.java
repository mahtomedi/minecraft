package net.minecraft.client.gui.components;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlUtil;
import com.mojang.datafixers.DataFixUtils;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.debugchart.BandwidthDebugChart;
import net.minecraft.client.gui.components.debugchart.FpsDebugChart;
import net.minecraft.client.gui.components.debugchart.PingDebugChart;
import net.minecraft.client.gui.components.debugchart.TpsDebugChart;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.Connection;
import net.minecraft.server.ServerTickRateManager;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.SampleLogger;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DebugScreenOverlay {
    private static final int COLOR_GREY = 14737632;
    private static final int MARGIN_RIGHT = 2;
    private static final int MARGIN_LEFT = 2;
    private static final int MARGIN_TOP = 2;
    private static final Map<Heightmap.Types, String> HEIGHTMAP_NAMES = Util.make(new EnumMap<>(Heightmap.Types.class), param0 -> {
        param0.put(Heightmap.Types.WORLD_SURFACE_WG, "SW");
        param0.put(Heightmap.Types.WORLD_SURFACE, "S");
        param0.put(Heightmap.Types.OCEAN_FLOOR_WG, "OW");
        param0.put(Heightmap.Types.OCEAN_FLOOR, "O");
        param0.put(Heightmap.Types.MOTION_BLOCKING, "M");
        param0.put(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, "ML");
    });
    private final Minecraft minecraft;
    private final DebugScreenOverlay.AllocationRateCalculator allocationRateCalculator;
    private final Font font;
    private HitResult block;
    private HitResult liquid;
    @Nullable
    private ChunkPos lastPos;
    @Nullable
    private LevelChunk clientChunk;
    @Nullable
    private CompletableFuture<LevelChunk> serverChunk;
    private boolean renderDebug;
    private boolean renderProfilerChart;
    private boolean renderFpsCharts;
    private boolean renderNetworkCharts;
    private final SampleLogger frameTimeLogger = new SampleLogger();
    private final SampleLogger tickTimeLogger = new SampleLogger();
    private final SampleLogger pingLogger = new SampleLogger();
    private final SampleLogger bandwidthLogger = new SampleLogger();
    private final FpsDebugChart fpsChart;
    private final TpsDebugChart tpsChart;
    private final PingDebugChart pingChart;
    private final BandwidthDebugChart bandwidthChart;

    public DebugScreenOverlay(Minecraft param0) {
        this.minecraft = param0;
        this.allocationRateCalculator = new DebugScreenOverlay.AllocationRateCalculator();
        this.font = param0.font;
        this.fpsChart = new FpsDebugChart(this.font, this.frameTimeLogger);
        this.tpsChart = new TpsDebugChart(this.font, this.tickTimeLogger, () -> param0.level.tickRateManager().millisecondsPerTick());
        this.pingChart = new PingDebugChart(this.font, this.pingLogger);
        this.bandwidthChart = new BandwidthDebugChart(this.font, this.bandwidthLogger);
    }

    public void clearChunkCache() {
        this.serverChunk = null;
        this.clientChunk = null;
    }

    public void render(GuiGraphics param0) {
        this.minecraft.getProfiler().push("debug");
        Entity var0 = this.minecraft.getCameraEntity();
        this.block = var0.pick(20.0, 0.0F, false);
        this.liquid = var0.pick(20.0, 0.0F, true);
        param0.drawManaged(() -> {
            this.drawGameInformation(param0);
            this.drawSystemInformation(param0);
            if (this.renderFpsCharts) {
                int var0x = param0.guiWidth();
                int var1 = var0x / 2;
                this.fpsChart.drawChart(param0, 0, this.fpsChart.getWidth(var1));
                if (this.minecraft.getSingleplayerServer() != null) {
                    int var2 = this.tpsChart.getWidth(var1);
                    this.tpsChart.drawChart(param0, var0x - var2, var2);
                }
            }

            if (this.renderNetworkCharts) {
                int var3 = param0.guiWidth();
                int var4 = var3 / 2;
                if (!this.minecraft.isLocalServer()) {
                    this.bandwidthChart.drawChart(param0, 0, this.bandwidthChart.getWidth(var4));
                }

                int var5 = this.pingChart.getWidth(var4);
                this.pingChart.drawChart(param0, var3 - var5, var5);
            }

        });
        this.minecraft.getProfiler().pop();
    }

    protected void drawGameInformation(GuiGraphics param0) {
        List<String> var0 = this.getGameInformation();
        var0.add("");
        boolean var1 = this.minecraft.getSingleplayerServer() != null;
        var0.add(
            "Debug charts: [F3+1] Profiler "
                + (this.renderProfilerChart ? "visible" : "hidden")
                + "; [F3+2] "
                + (var1 ? "FPS + TPS " : "FPS ")
                + (this.renderFpsCharts ? "visible" : "hidden")
                + "; [F3+3] "
                + (!this.minecraft.isLocalServer() ? "Bandwidth + Ping" : "Ping")
                + (this.renderNetworkCharts ? " visible" : " hidden")
        );
        var0.add("For help: press F3 + Q");
        this.renderLines(param0, var0, true);
    }

    protected void drawSystemInformation(GuiGraphics param0) {
        List<String> var0 = this.getSystemInformation();
        this.renderLines(param0, var0, false);
    }

    private void renderLines(GuiGraphics param0, List<String> param1, boolean param2) {
        int var0 = 9;

        for(int var1 = 0; var1 < param1.size(); ++var1) {
            String var2 = param1.get(var1);
            if (!Strings.isNullOrEmpty(var2)) {
                int var3 = this.font.width(var2);
                int var4 = param2 ? 2 : param0.guiWidth() - 2 - var3;
                int var5 = 2 + var0 * var1;
                param0.fill(var4 - 1, var5 - 1, var4 + var3 + 1, var5 + var0 - 1, -1873784752);
            }
        }

        for(int var6 = 0; var6 < param1.size(); ++var6) {
            String var7 = param1.get(var6);
            if (!Strings.isNullOrEmpty(var7)) {
                int var8 = this.font.width(var7);
                int var9 = param2 ? 2 : param0.guiWidth() - 2 - var8;
                int var10 = 2 + var0 * var6;
                param0.drawString(this.font, var7, var9, var10, 14737632, false);
            }
        }

    }

    protected List<String> getGameInformation() {
        IntegratedServer var0 = this.minecraft.getSingleplayerServer();
        ClientPacketListener var1 = this.minecraft.getConnection();
        Connection var2 = var1.getConnection();
        float var3 = var2.getAverageSentPackets();
        float var4 = var2.getAverageReceivedPackets();
        TickRateManager var5 = this.getLevel().tickRateManager();
        String var6;
        if (var5.isSteppingForward()) {
            var6 = " (frozen - stepping)";
        } else if (var5.isFrozen()) {
            var6 = " (frozen)";
        } else {
            var6 = "";
        }

        String var12;
        if (var0 != null) {
            ServerTickRateManager var9 = var0.tickRateManager();
            boolean var10 = var9.isSprinting();
            if (var10) {
                var6 = " (sprinting)";
            }

            String var11 = var10 ? "-" : String.format(Locale.ROOT, "%.1f", var5.millisecondsPerTick());
            var12 = String.format(Locale.ROOT, "Integrated server @ %.1f/%s ms%s, %.0f tx, %.0f rx", var0.getCurrentSmoothedTickTime(), var11, var6, var3, var4);
        } else {
            var12 = String.format(Locale.ROOT, "\"%s\" server%s, %.0f tx, %.0f rx", var1.serverBrand(), var6, var3, var4);
        }

        BlockPos var14 = this.minecraft.getCameraEntity().blockPosition();
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
                var12,
                this.minecraft.levelRenderer.getSectionStatistics(),
                this.minecraft.levelRenderer.getEntityStatistics(),
                "P: " + this.minecraft.particleEngine.countParticles() + ". T: " + this.minecraft.level.getEntityCount(),
                this.minecraft.level.gatherChunkSourceStats(),
                "",
                String.format(Locale.ROOT, "Chunk-relative: %d %d %d", var14.getX() & 15, var14.getY() & 15, var14.getZ() & 15)
            );
        } else {
            Entity var15 = this.minecraft.getCameraEntity();
            Direction var16 = var15.getDirection();

            String var21 = switch(var16) {
                case NORTH -> "Towards negative Z";
                case SOUTH -> "Towards positive Z";
                case WEST -> "Towards negative X";
                case EAST -> "Towards positive X";
                default -> "Invalid";
            };
            ChunkPos var22 = new ChunkPos(var14);
            if (!Objects.equals(this.lastPos, var22)) {
                this.lastPos = var22;
                this.clearChunkCache();
            }

            Level var23 = this.getLevel();
            LongSet var24 = (LongSet)(var23 instanceof ServerLevel ? ((ServerLevel)var23).getForcedChunks() : LongSets.EMPTY_SET);
            List<String> var25 = Lists.newArrayList(
                "Minecraft "
                    + SharedConstants.getCurrentVersion().getName()
                    + " ("
                    + this.minecraft.getLaunchedVersion()
                    + "/"
                    + ClientBrandRetriever.getClientModName()
                    + ("release".equalsIgnoreCase(this.minecraft.getVersionType()) ? "" : "/" + this.minecraft.getVersionType())
                    + ")",
                this.minecraft.fpsString,
                var12,
                this.minecraft.levelRenderer.getSectionStatistics(),
                this.minecraft.levelRenderer.getEntityStatistics(),
                "P: " + this.minecraft.particleEngine.countParticles() + ". T: " + this.minecraft.level.getEntityCount(),
                this.minecraft.level.gatherChunkSourceStats()
            );
            String var26 = this.getServerChunkStats();
            if (var26 != null) {
                var25.add(var26);
            }

            var25.add(this.minecraft.level.dimension().location() + " FC: " + var24.size());
            var25.add("");
            var25.add(
                String.format(
                    Locale.ROOT,
                    "XYZ: %.3f / %.5f / %.3f",
                    this.minecraft.getCameraEntity().getX(),
                    this.minecraft.getCameraEntity().getY(),
                    this.minecraft.getCameraEntity().getZ()
                )
            );
            var25.add(
                String.format(
                    Locale.ROOT,
                    "Block: %d %d %d [%d %d %d]",
                    var14.getX(),
                    var14.getY(),
                    var14.getZ(),
                    var14.getX() & 15,
                    var14.getY() & 15,
                    var14.getZ() & 15
                )
            );
            var25.add(
                String.format(
                    Locale.ROOT,
                    "Chunk: %d %d %d [%d %d in r.%d.%d.mca]",
                    var22.x,
                    SectionPos.blockToSectionCoord(var14.getY()),
                    var22.z,
                    var22.getRegionLocalX(),
                    var22.getRegionLocalZ(),
                    var22.getRegionX(),
                    var22.getRegionZ()
                )
            );
            var25.add(
                String.format(Locale.ROOT, "Facing: %s (%s) (%.1f / %.1f)", var16, var21, Mth.wrapDegrees(var15.getYRot()), Mth.wrapDegrees(var15.getXRot()))
            );
            LevelChunk var27 = this.getClientChunk();
            if (var27.isEmpty()) {
                var25.add("Waiting for chunk...");
            } else {
                int var28 = this.minecraft.level.getChunkSource().getLightEngine().getRawBrightness(var14, 0);
                int var29 = this.minecraft.level.getBrightness(LightLayer.SKY, var14);
                int var30 = this.minecraft.level.getBrightness(LightLayer.BLOCK, var14);
                var25.add("Client Light: " + var28 + " (" + var29 + " sky, " + var30 + " block)");
                LevelChunk var31 = this.getServerChunk();
                StringBuilder var32 = new StringBuilder("CH");

                for(Heightmap.Types var33 : Heightmap.Types.values()) {
                    if (var33.sendToClient()) {
                        var32.append(" ").append(HEIGHTMAP_NAMES.get(var33)).append(": ").append(var27.getHeight(var33, var14.getX(), var14.getZ()));
                    }
                }

                var25.add(var32.toString());
                var32.setLength(0);
                var32.append("SH");

                for(Heightmap.Types var34 : Heightmap.Types.values()) {
                    if (var34.keepAfterWorldgen()) {
                        var32.append(" ").append(HEIGHTMAP_NAMES.get(var34)).append(": ");
                        if (var31 != null) {
                            var32.append(var31.getHeight(var34, var14.getX(), var14.getZ()));
                        } else {
                            var32.append("??");
                        }
                    }
                }

                var25.add(var32.toString());
                if (var14.getY() >= this.minecraft.level.getMinBuildHeight() && var14.getY() < this.minecraft.level.getMaxBuildHeight()) {
                    var25.add("Biome: " + printBiome(this.minecraft.level.getBiome(var14)));
                    if (var31 != null) {
                        float var35 = var23.getMoonBrightness();
                        long var36 = var31.getInhabitedTime();
                        DifficultyInstance var37 = new DifficultyInstance(var23.getDifficulty(), var23.getDayTime(), var36, var35);
                        var25.add(
                            String.format(
                                Locale.ROOT,
                                "Local Difficulty: %.2f // %.2f (Day %d)",
                                var37.getEffectiveDifficulty(),
                                var37.getSpecialMultiplier(),
                                this.minecraft.level.getDayTime() / 24000L
                            )
                        );
                    } else {
                        var25.add("Local Difficulty: ??");
                    }
                }

                if (var31 != null && var31.isOldNoiseGeneration()) {
                    var25.add("Blending: Old");
                }
            }

            ServerLevel var38 = this.getServerLevel();
            if (var38 != null) {
                ServerChunkCache var39 = var38.getChunkSource();
                ChunkGenerator var40 = var39.getGenerator();
                RandomState var41 = var39.randomState();
                var40.addDebugScreenInfo(var25, var41, var14);
                Climate.Sampler var42 = var41.sampler();
                BiomeSource var43 = var40.getBiomeSource();
                var43.addDebugInfo(var25, var14, var42);
                NaturalSpawner.SpawnState var44 = var39.getLastSpawnState();
                if (var44 != null) {
                    Object2IntMap<MobCategory> var45 = var44.getMobCategoryCounts();
                    int var46 = var44.getSpawnableChunkCount();
                    var25.add(
                        "SC: "
                            + var46
                            + ", "
                            + (String)Stream.of(MobCategory.values())
                                .map(param1 -> Character.toUpperCase(param1.getName().charAt(0)) + ": " + var45.getInt(param1))
                                .collect(Collectors.joining(", "))
                    );
                } else {
                    var25.add("SC: N/A");
                }
            }

            PostChain var47 = this.minecraft.gameRenderer.currentEffect();
            if (var47 != null) {
                var25.add("Shader: " + var47.getName());
            }

            var25.add(
                this.minecraft.getSoundManager().getDebugString()
                    + String.format(Locale.ROOT, " (Mood %d%%)", Math.round(this.minecraft.player.getCurrentMood() * 100.0F))
            );
            return var25;
        }
    }

    private static String printBiome(Holder<Biome> param0) {
        return param0.unwrap().map(param0x -> param0x.location().toString(), param0x -> "[unregistered " + param0x + "]");
    }

    @Nullable
    private ServerLevel getServerLevel() {
        IntegratedServer var0 = this.minecraft.getSingleplayerServer();
        return var0 != null ? var0.getLevel(this.minecraft.level.dimension()) : null;
    }

    @Nullable
    private String getServerChunkStats() {
        ServerLevel var0 = this.getServerLevel();
        return var0 != null ? var0.gatherChunkSourceStats() : null;
    }

    private Level getLevel() {
        return DataFixUtils.orElse(
            Optional.ofNullable(this.minecraft.getSingleplayerServer())
                .flatMap(param0 -> Optional.ofNullable(param0.getLevel(this.minecraft.level.dimension()))),
            this.minecraft.level
        );
    }

    @Nullable
    private LevelChunk getServerChunk() {
        if (this.serverChunk == null) {
            ServerLevel var0 = this.getServerLevel();
            if (var0 == null) {
                return null;
            }

            this.serverChunk = var0.getChunkSource()
                .getChunkFuture(this.lastPos.x, this.lastPos.z, ChunkStatus.FULL, false)
                .thenApply(param0 -> param0.map(param0x -> (LevelChunk)param0x, param0x -> null));
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
            String.format(Locale.ROOT, "Java: %s %dbit", System.getProperty("java.version"), this.minecraft.is64Bit() ? 64 : 32),
            String.format(Locale.ROOT, "Mem: % 2d%% %03d/%03dMB", var3 * 100L / var0, bytesToMegabytes(var3), bytesToMegabytes(var0)),
            String.format(Locale.ROOT, "Allocation rate: %03dMB /s", bytesToMegabytes(this.allocationRateCalculator.bytesAllocatedPerSecond(var3))),
            String.format(Locale.ROOT, "Allocated: % 2d%% %03dMB", var1 * 100L / var0, bytesToMegabytes(var1)),
            "",
            String.format(Locale.ROOT, "CPU: %s", GlUtil.getCpuInfo()),
            "",
            String.format(
                Locale.ROOT,
                "Display: %dx%d (%s)",
                Minecraft.getInstance().getWindow().getWidth(),
                Minecraft.getInstance().getWindow().getHeight(),
                GlUtil.getVendor()
            ),
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
                var4.add(ChatFormatting.UNDERLINE + "Targeted Block: " + var5.getX() + ", " + var5.getY() + ", " + var5.getZ());
                var4.add(String.valueOf(BuiltInRegistries.BLOCK.getKey(var6.getBlock())));

                for(Entry<Property<?>, Comparable<?>> var7 : var6.getValues().entrySet()) {
                    var4.add(this.getPropertyValueString(var7));
                }

                var6.getTags().map(param0 -> "#" + param0.location()).forEach(var4::add);
            }

            if (this.liquid.getType() == HitResult.Type.BLOCK) {
                BlockPos var8 = ((BlockHitResult)this.liquid).getBlockPos();
                FluidState var9 = this.minecraft.level.getFluidState(var8);
                var4.add("");
                var4.add(ChatFormatting.UNDERLINE + "Targeted Fluid: " + var8.getX() + ", " + var8.getY() + ", " + var8.getZ());
                var4.add(String.valueOf(BuiltInRegistries.FLUID.getKey(var9.getType())));

                for(Entry<Property<?>, Comparable<?>> var10 : var9.getValues().entrySet()) {
                    var4.add(this.getPropertyValueString(var10));
                }

                var9.getTags().map(param0 -> "#" + param0.location()).forEach(var4::add);
            }

            Entity var11 = this.minecraft.crosshairPickEntity;
            if (var11 != null) {
                var4.add("");
                var4.add(ChatFormatting.UNDERLINE + "Targeted Entity");
                var4.add(String.valueOf(BuiltInRegistries.ENTITY_TYPE.getKey(var11.getType())));
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

    private static long bytesToMegabytes(long param0) {
        return param0 / 1024L / 1024L;
    }

    public boolean showDebugScreen() {
        return this.renderDebug && !this.minecraft.options.hideGui;
    }

    public boolean showProfilerChart() {
        return this.showDebugScreen() && this.renderProfilerChart;
    }

    public boolean showNetworkCharts() {
        return this.showDebugScreen() && this.renderNetworkCharts;
    }

    public void toggleOverlay() {
        this.renderDebug = !this.renderDebug;
    }

    public void toggleNetworkCharts() {
        this.renderNetworkCharts = !this.renderDebug || !this.renderNetworkCharts;
        if (this.renderNetworkCharts) {
            this.renderDebug = true;
            this.renderFpsCharts = false;
        }

    }

    public void toggleFpsCharts() {
        this.renderFpsCharts = !this.renderDebug || !this.renderFpsCharts;
        if (this.renderFpsCharts) {
            this.renderDebug = true;
            this.renderNetworkCharts = false;
        }

    }

    public void toggleProfilerChart() {
        this.renderProfilerChart = !this.renderDebug || !this.renderProfilerChart;
        if (this.renderProfilerChart) {
            this.renderDebug = true;
        }

    }

    public void logFrameDuration(long param0) {
        this.frameTimeLogger.logSample(param0);
    }

    public void logTickDuration(long param0) {
        this.tickTimeLogger.logSample(param0);
    }

    public SampleLogger getPingLogger() {
        return this.pingLogger;
    }

    public SampleLogger getBandwidthLogger() {
        return this.bandwidthLogger;
    }

    public void reset() {
        this.renderDebug = false;
        this.tickTimeLogger.reset();
        this.pingLogger.reset();
        this.bandwidthLogger.reset();
    }

    @OnlyIn(Dist.CLIENT)
    static class AllocationRateCalculator {
        private static final int UPDATE_INTERVAL_MS = 500;
        private static final List<GarbageCollectorMXBean> GC_MBEANS = ManagementFactory.getGarbageCollectorMXBeans();
        private long lastTime = 0L;
        private long lastHeapUsage = -1L;
        private long lastGcCounts = -1L;
        private long lastRate = 0L;

        long bytesAllocatedPerSecond(long param0) {
            long var0 = System.currentTimeMillis();
            if (var0 - this.lastTime < 500L) {
                return this.lastRate;
            } else {
                long var1 = gcCounts();
                if (this.lastTime != 0L && var1 == this.lastGcCounts) {
                    double var2 = (double)TimeUnit.SECONDS.toMillis(1L) / (double)(var0 - this.lastTime);
                    long var3 = param0 - this.lastHeapUsage;
                    this.lastRate = Math.round((double)var3 * var2);
                }

                this.lastTime = var0;
                this.lastHeapUsage = param0;
                this.lastGcCounts = var1;
                return this.lastRate;
            }
        }

        private static long gcCounts() {
            long var0 = 0L;

            for(GarbageCollectorMXBean var1 : GC_MBEANS) {
                var0 += var1.getCollectionCount();
            }

            return var0;
        }
    }
}
