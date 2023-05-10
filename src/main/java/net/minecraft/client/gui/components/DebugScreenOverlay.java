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
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
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
    private static final int RED = -65536;
    private static final int YELLOW = -256;
    private static final int GREEN = -16711936;

    public DebugScreenOverlay(Minecraft param0) {
        this.minecraft = param0;
        this.allocationRateCalculator = new DebugScreenOverlay.AllocationRateCalculator();
        this.font = param0.font;
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
            if (this.minecraft.options.renderFpsChart) {
                int var0x = param0.guiWidth();
                this.drawChart(param0, this.minecraft.getFrameTimer(), 0, var0x / 2, true);
                IntegratedServer var1x = this.minecraft.getSingleplayerServer();
                if (var1x != null) {
                    this.drawChart(param0, var1x.getFrameTimer(), var0x - Math.min(var0x / 2, 240), var0x / 2, false);
                }
            }

        });
        this.minecraft.getProfiler().pop();
    }

    protected void drawGameInformation(GuiGraphics param0) {
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
        Connection var1 = this.minecraft.getConnection().getConnection();
        float var2 = var1.getAverageSentPackets();
        float var3 = var1.getAverageReceivedPackets();
        String var4;
        if (var0 != null) {
            var4 = String.format(Locale.ROOT, "Integrated server @ %.0f ms ticks, %.0f tx, %.0f rx", var0.getAverageTickTime(), var2, var3);
        } else {
            var4 = String.format(Locale.ROOT, "\"%s\" server, %.0f tx, %.0f rx", this.minecraft.player.getServerBrand(), var2, var3);
        }

        BlockPos var6 = this.minecraft.getCameraEntity().blockPosition();
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
                String.format(Locale.ROOT, "Chunk-relative: %d %d %d", var6.getX() & 15, var6.getY() & 15, var6.getZ() & 15)
            );
        } else {
            Entity var7 = this.minecraft.getCameraEntity();
            Direction var8 = var7.getDirection();

            String var13 = switch(var8) {
                case NORTH -> "Towards negative Z";
                case SOUTH -> "Towards positive Z";
                case WEST -> "Towards negative X";
                case EAST -> "Towards positive X";
                default -> "Invalid";
            };
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

            var17.add(this.minecraft.level.dimension().location() + " FC: " + var16.size());
            var17.add("");
            var17.add(
                String.format(
                    Locale.ROOT,
                    "XYZ: %.3f / %.5f / %.3f",
                    this.minecraft.getCameraEntity().getX(),
                    this.minecraft.getCameraEntity().getY(),
                    this.minecraft.getCameraEntity().getZ()
                )
            );
            var17.add(
                String.format(
                    Locale.ROOT, "Block: %d %d %d [%d %d %d]", var6.getX(), var6.getY(), var6.getZ(), var6.getX() & 15, var6.getY() & 15, var6.getZ() & 15
                )
            );
            var17.add(
                String.format(
                    Locale.ROOT,
                    "Chunk: %d %d %d [%d %d in r.%d.%d.mca]",
                    var14.x,
                    SectionPos.blockToSectionCoord(var6.getY()),
                    var14.z,
                    var14.getRegionLocalX(),
                    var14.getRegionLocalZ(),
                    var14.getRegionX(),
                    var14.getRegionZ()
                )
            );
            var17.add(
                String.format(Locale.ROOT, "Facing: %s (%s) (%.1f / %.1f)", var8, var13, Mth.wrapDegrees(var7.getYRot()), Mth.wrapDegrees(var7.getXRot()))
            );
            LevelChunk var19 = this.getClientChunk();
            if (var19.isEmpty()) {
                var17.add("Waiting for chunk...");
            } else {
                int var20 = this.minecraft.level.getChunkSource().getLightEngine().getRawBrightness(var6, 0);
                int var21 = this.minecraft.level.getBrightness(LightLayer.SKY, var6);
                int var22 = this.minecraft.level.getBrightness(LightLayer.BLOCK, var6);
                var17.add("Client Light: " + var20 + " (" + var21 + " sky, " + var22 + " block)");
                LevelChunk var23 = this.getServerChunk();
                StringBuilder var24 = new StringBuilder("CH");

                for(Heightmap.Types var25 : Heightmap.Types.values()) {
                    if (var25.sendToClient()) {
                        var24.append(" ").append(HEIGHTMAP_NAMES.get(var25)).append(": ").append(var19.getHeight(var25, var6.getX(), var6.getZ()));
                    }
                }

                var17.add(var24.toString());
                var24.setLength(0);
                var24.append("SH");

                for(Heightmap.Types var26 : Heightmap.Types.values()) {
                    if (var26.keepAfterWorldgen()) {
                        var24.append(" ").append(HEIGHTMAP_NAMES.get(var26)).append(": ");
                        if (var23 != null) {
                            var24.append(var23.getHeight(var26, var6.getX(), var6.getZ()));
                        } else {
                            var24.append("??");
                        }
                    }
                }

                var17.add(var24.toString());
                if (var6.getY() >= this.minecraft.level.getMinBuildHeight() && var6.getY() < this.minecraft.level.getMaxBuildHeight()) {
                    var17.add("Biome: " + printBiome(this.minecraft.level.getBiome(var6)));
                    long var27 = 0L;
                    float var28 = 0.0F;
                    if (var23 != null) {
                        var28 = var15.getMoonBrightness();
                        var27 = var23.getInhabitedTime();
                    }

                    DifficultyInstance var29 = new DifficultyInstance(var15.getDifficulty(), var15.getDayTime(), var27, var28);
                    var17.add(
                        String.format(
                            Locale.ROOT,
                            "Local Difficulty: %.2f // %.2f (Day %d)",
                            var29.getEffectiveDifficulty(),
                            var29.getSpecialMultiplier(),
                            this.minecraft.level.getDayTime() / 24000L
                        )
                    );
                }

                if (var23 != null && var23.isOldNoiseGeneration()) {
                    var17.add("Blending: Old");
                }
            }

            ServerLevel var30 = this.getServerLevel();
            if (var30 != null) {
                ServerChunkCache var31 = var30.getChunkSource();
                ChunkGenerator var32 = var31.getGenerator();
                RandomState var33 = var31.randomState();
                var32.addDebugScreenInfo(var17, var33, var6);
                Climate.Sampler var34 = var33.sampler();
                BiomeSource var35 = var32.getBiomeSource();
                var35.addDebugInfo(var17, var6, var34);
                NaturalSpawner.SpawnState var36 = var31.getLastSpawnState();
                if (var36 != null) {
                    Object2IntMap<MobCategory> var37 = var36.getMobCategoryCounts();
                    int var38 = var36.getSpawnableChunkCount();
                    var17.add(
                        "SC: "
                            + var38
                            + ", "
                            + (String)Stream.of(MobCategory.values())
                                .map(param1 -> Character.toUpperCase(param1.getName().charAt(0)) + ": " + var37.getInt(param1))
                                .collect(Collectors.joining(", "))
                    );
                } else {
                    var17.add("SC: N/A");
                }
            }

            PostChain var39 = this.minecraft.gameRenderer.currentEffect();
            if (var39 != null) {
                var17.add("Shader: " + var39.getName());
            }

            var17.add(
                this.minecraft.getSoundManager().getDebugString()
                    + String.format(Locale.ROOT, " (Mood %d%%)", Math.round(this.minecraft.player.getCurrentMood() * 100.0F))
            );
            return var17;
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
            if (var0 != null) {
                this.serverChunk = var0.getChunkSource()
                    .getChunkFuture(this.lastPos.x, this.lastPos.z, ChunkStatus.FULL, false)
                    .thenApply(param0 -> param0.map(param0x -> (LevelChunk)param0x, param0x -> null));
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

    private void drawChart(GuiGraphics param0, FrameTimer param1, int param2, int param3, boolean param4) {
        int var0 = param1.getLogStart();
        int var1 = param1.getLogEnd();
        long[] var2 = param1.getLog();
        int var4 = param2;
        int var5 = Math.max(0, var2.length - param3);
        int var6 = var2.length - var5;
        int var3 = param1.wrapIndex(var0 + var5);
        long var7 = 0L;
        int var8 = Integer.MAX_VALUE;
        int var9x = Integer.MIN_VALUE;

        for(int var10 = 0; var10 < var6; ++var10) {
            int var11 = (int)(var2[param1.wrapIndex(var3 + var10)] / 1000000L);
            var8 = Math.min(var8, var11);
            var9x = Math.max(var9x, var11);
            var7 += (long)var11;
        }

        int var12 = param0.guiHeight();
        param0.fill(RenderType.guiOverlay(), param2, var12 - 60, param2 + var6, var12, -1873784752);

        while(var3 != var1) {
            int var13 = param1.scaleSampleTo(var2[var3], param4 ? 30 : 60, param4 ? 60 : 20);
            int var14 = param4 ? 100 : 60;
            int var15 = this.getSampleColor(Mth.clamp(var13, 0, var14), 0, var14 / 2, var14);
            param0.fill(RenderType.guiOverlay(), var4, var12 - var13, var4 + 1, var12, var15);
            ++var4;
            var3 = param1.wrapIndex(var3 + 1);
        }

        if (param4) {
            param0.fill(RenderType.guiOverlay(), param2 + 1, var12 - 30 + 1, param2 + 14, var12 - 30 + 10, -1873784752);
            param0.drawString(this.font, "60 FPS", param2 + 2, var12 - 30 + 2, 14737632, false);
            param0.hLine(RenderType.guiOverlay(), param2, param2 + var6 - 1, var12 - 30, -1);
            param0.fill(RenderType.guiOverlay(), param2 + 1, var12 - 60 + 1, param2 + 14, var12 - 60 + 10, -1873784752);
            param0.drawString(this.font, "30 FPS", param2 + 2, var12 - 60 + 2, 14737632, false);
            param0.hLine(RenderType.guiOverlay(), param2, param2 + var6 - 1, var12 - 60, -1);
        } else {
            param0.fill(RenderType.guiOverlay(), param2 + 1, var12 - 60 + 1, param2 + 14, var12 - 60 + 10, -1873784752);
            param0.drawString(this.font, "20 TPS", param2 + 2, var12 - 60 + 2, 14737632, false);
            param0.hLine(RenderType.guiOverlay(), param2, param2 + var6 - 1, var12 - 60, -1);
        }

        param0.hLine(RenderType.guiOverlay(), param2, param2 + var6 - 1, var12 - 1, -1);
        param0.vLine(RenderType.guiOverlay(), param2, var12 - 60, var12, -1);
        param0.vLine(RenderType.guiOverlay(), param2 + var6 - 1, var12 - 60, var12, -1);
        int var16 = this.minecraft.options.framerateLimit().get();
        if (param4 && var16 > 0 && var16 <= 250) {
            param0.hLine(RenderType.guiOverlay(), param2, param2 + var6 - 1, var12 - 1 - (int)(1800.0 / (double)var16), -16711681);
        }

        String var17 = var8 + " ms min";
        String var18 = var7 / (long)var6 + " ms avg";
        String var19 = var9x + " ms max";
        param0.drawString(this.font, var17, param2 + 2, var12 - 60 - 9, 14737632);
        param0.drawCenteredString(this.font, var18, param2 + var6 / 2, var12 - 60 - 9, 14737632);
        param0.drawString(this.font, var19, param2 + var6 - this.font.width(var19), var12 - 60 - 9, 14737632);
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
