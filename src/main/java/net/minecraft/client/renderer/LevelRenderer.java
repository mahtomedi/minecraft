package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.PrioritizeChunkUpdates;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SculkChargeParticleOptions;
import net.minecraft.core.particles.ShriekParticleOption;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector4f;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class LevelRenderer implements ResourceManagerReloadListener, AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int SECTION_SIZE = 16;
    public static final int HALF_SECTION_SIZE = 8;
    private static final float SKY_DISC_RADIUS = 512.0F;
    private static final int MIN_FOG_DISTANCE = 32;
    private static final int RAIN_RADIUS = 10;
    private static final int RAIN_DIAMETER = 21;
    private static final int TRANSPARENT_SORT_COUNT = 15;
    private static final ResourceLocation MOON_LOCATION = new ResourceLocation("textures/environment/moon_phases.png");
    private static final ResourceLocation SUN_LOCATION = new ResourceLocation("textures/environment/sun.png");
    private static final ResourceLocation CLOUDS_LOCATION = new ResourceLocation("textures/environment/clouds.png");
    private static final ResourceLocation END_SKY_LOCATION = new ResourceLocation("textures/environment/end_sky.png");
    private static final ResourceLocation FORCEFIELD_LOCATION = new ResourceLocation("textures/misc/forcefield.png");
    private static final ResourceLocation RAIN_LOCATION = new ResourceLocation("textures/environment/rain.png");
    private static final ResourceLocation SNOW_LOCATION = new ResourceLocation("textures/environment/snow.png");
    public static final Direction[] DIRECTIONS = Direction.values();
    private final Minecraft minecraft;
    private final EntityRenderDispatcher entityRenderDispatcher;
    private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
    private final RenderBuffers renderBuffers;
    @Nullable
    private ClientLevel level;
    private final SectionOcclusionGraph sectionOcclusionGraph = new SectionOcclusionGraph();
    private final ObjectArrayList<SectionRenderDispatcher.RenderSection> visibleSections = new ObjectArrayList<>(10000);
    private final Set<BlockEntity> globalBlockEntities = Sets.newHashSet();
    @Nullable
    private ViewArea viewArea;
    @Nullable
    private VertexBuffer starBuffer;
    @Nullable
    private VertexBuffer skyBuffer;
    @Nullable
    private VertexBuffer darkBuffer;
    private boolean generateClouds = true;
    @Nullable
    private VertexBuffer cloudBuffer;
    private final RunningTrimmedMean frameTimes = new RunningTrimmedMean(100);
    private int ticks;
    private final Int2ObjectMap<BlockDestructionProgress> destroyingBlocks = new Int2ObjectOpenHashMap<>();
    private final Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress = new Long2ObjectOpenHashMap<>();
    private final Map<BlockPos, SoundInstance> playingRecords = Maps.newHashMap();
    @Nullable
    private RenderTarget entityTarget;
    @Nullable
    private PostChain entityEffect;
    @Nullable
    private RenderTarget translucentTarget;
    @Nullable
    private RenderTarget itemEntityTarget;
    @Nullable
    private RenderTarget particlesTarget;
    @Nullable
    private RenderTarget weatherTarget;
    @Nullable
    private RenderTarget cloudsTarget;
    @Nullable
    private PostChain transparencyChain;
    private int lastCameraSectionX = Integer.MIN_VALUE;
    private int lastCameraSectionY = Integer.MIN_VALUE;
    private int lastCameraSectionZ = Integer.MIN_VALUE;
    private double prevCamX = Double.MIN_VALUE;
    private double prevCamY = Double.MIN_VALUE;
    private double prevCamZ = Double.MIN_VALUE;
    private double prevCamRotX = Double.MIN_VALUE;
    private double prevCamRotY = Double.MIN_VALUE;
    private int prevCloudX = Integer.MIN_VALUE;
    private int prevCloudY = Integer.MIN_VALUE;
    private int prevCloudZ = Integer.MIN_VALUE;
    private Vec3 prevCloudColor = Vec3.ZERO;
    @Nullable
    private CloudStatus prevCloudsType;
    @Nullable
    private SectionRenderDispatcher sectionRenderDispatcher;
    private int lastViewDistance = -1;
    private int renderedEntities;
    private int culledEntities;
    private Frustum cullingFrustum;
    private boolean captureFrustum;
    @Nullable
    private Frustum capturedFrustum;
    private final Vector4f[] frustumPoints = new Vector4f[8];
    private final Vector3d frustumPos = new Vector3d(0.0, 0.0, 0.0);
    private double xTransparentOld;
    private double yTransparentOld;
    private double zTransparentOld;
    private int rainSoundTime;
    private final float[] rainSizeX = new float[1024];
    private final float[] rainSizeZ = new float[1024];

    public LevelRenderer(Minecraft param0, EntityRenderDispatcher param1, BlockEntityRenderDispatcher param2, RenderBuffers param3) {
        this.minecraft = param0;
        this.entityRenderDispatcher = param1;
        this.blockEntityRenderDispatcher = param2;
        this.renderBuffers = param3;

        for(int var0 = 0; var0 < 32; ++var0) {
            for(int var1 = 0; var1 < 32; ++var1) {
                float var2 = (float)(var1 - 16);
                float var3 = (float)(var0 - 16);
                float var4 = Mth.sqrt(var2 * var2 + var3 * var3);
                this.rainSizeX[var0 << 5 | var1] = -var3 / var4;
                this.rainSizeZ[var0 << 5 | var1] = var2 / var4;
            }
        }

        this.createStars();
        this.createLightSky();
        this.createDarkSky();
    }

    private void renderSnowAndRain(LightTexture param0, float param1, double param2, double param3, double param4) {
        float var0 = this.minecraft.level.getRainLevel(param1);
        if (!(var0 <= 0.0F)) {
            param0.turnOnLightLayer();
            Level var1 = this.minecraft.level;
            int var2 = Mth.floor(param2);
            int var3 = Mth.floor(param3);
            int var4 = Mth.floor(param4);
            Tesselator var5 = Tesselator.getInstance();
            BufferBuilder var6 = var5.getBuilder();
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            int var7 = 5;
            if (Minecraft.useFancyGraphics()) {
                var7 = 10;
            }

            RenderSystem.depthMask(Minecraft.useShaderTransparency());
            int var8 = -1;
            float var9 = (float)this.ticks + param1;
            RenderSystem.setShader(GameRenderer::getParticleShader);
            BlockPos.MutableBlockPos var10 = new BlockPos.MutableBlockPos();

            for(int var11 = var4 - var7; var11 <= var4 + var7; ++var11) {
                for(int var12 = var2 - var7; var12 <= var2 + var7; ++var12) {
                    int var13 = (var11 - var4 + 16) * 32 + var12 - var2 + 16;
                    double var14 = (double)this.rainSizeX[var13] * 0.5;
                    double var15 = (double)this.rainSizeZ[var13] * 0.5;
                    var10.set((double)var12, param3, (double)var11);
                    Biome var16 = var1.getBiome(var10).value();
                    if (var16.hasPrecipitation()) {
                        int var17 = var1.getHeight(Heightmap.Types.MOTION_BLOCKING, var12, var11);
                        int var18 = var3 - var7;
                        int var19 = var3 + var7;
                        if (var18 < var17) {
                            var18 = var17;
                        }

                        if (var19 < var17) {
                            var19 = var17;
                        }

                        int var20 = var17;
                        if (var17 < var3) {
                            var20 = var3;
                        }

                        if (var18 != var19) {
                            RandomSource var21 = RandomSource.create((long)(var12 * var12 * 3121 + var12 * 45238971 ^ var11 * var11 * 418711 + var11 * 13761));
                            var10.set(var12, var18, var11);
                            Biome.Precipitation var22 = var16.getPrecipitationAt(var10);
                            if (var22 == Biome.Precipitation.RAIN) {
                                if (var8 != 0) {
                                    if (var8 >= 0) {
                                        var5.end();
                                    }

                                    var8 = 0;
                                    RenderSystem.setShaderTexture(0, RAIN_LOCATION);
                                    var6.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
                                }

                                int var23 = this.ticks + var12 * var12 * 3121 + var12 * 45238971 + var11 * var11 * 418711 + var11 * 13761 & 31;
                                float var24 = -((float)var23 + param1) / 32.0F * (3.0F + var21.nextFloat());
                                double var25 = (double)var12 + 0.5 - param2;
                                double var26 = (double)var11 + 0.5 - param4;
                                float var27 = (float)Math.sqrt(var25 * var25 + var26 * var26) / (float)var7;
                                float var28 = ((1.0F - var27 * var27) * 0.5F + 0.5F) * var0;
                                var10.set(var12, var20, var11);
                                int var29 = getLightColor(var1, var10);
                                var6.vertex((double)var12 - param2 - var14 + 0.5, (double)var19 - param3, (double)var11 - param4 - var15 + 0.5)
                                    .uv(0.0F, (float)var18 * 0.25F + var24)
                                    .color(1.0F, 1.0F, 1.0F, var28)
                                    .uv2(var29)
                                    .endVertex();
                                var6.vertex((double)var12 - param2 + var14 + 0.5, (double)var19 - param3, (double)var11 - param4 + var15 + 0.5)
                                    .uv(1.0F, (float)var18 * 0.25F + var24)
                                    .color(1.0F, 1.0F, 1.0F, var28)
                                    .uv2(var29)
                                    .endVertex();
                                var6.vertex((double)var12 - param2 + var14 + 0.5, (double)var18 - param3, (double)var11 - param4 + var15 + 0.5)
                                    .uv(1.0F, (float)var19 * 0.25F + var24)
                                    .color(1.0F, 1.0F, 1.0F, var28)
                                    .uv2(var29)
                                    .endVertex();
                                var6.vertex((double)var12 - param2 - var14 + 0.5, (double)var18 - param3, (double)var11 - param4 - var15 + 0.5)
                                    .uv(0.0F, (float)var19 * 0.25F + var24)
                                    .color(1.0F, 1.0F, 1.0F, var28)
                                    .uv2(var29)
                                    .endVertex();
                            } else if (var22 == Biome.Precipitation.SNOW) {
                                if (var8 != 1) {
                                    if (var8 >= 0) {
                                        var5.end();
                                    }

                                    var8 = 1;
                                    RenderSystem.setShaderTexture(0, SNOW_LOCATION);
                                    var6.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
                                }

                                float var30 = -((float)(this.ticks & 511) + param1) / 512.0F;
                                float var31 = (float)(var21.nextDouble() + (double)var9 * 0.01 * (double)((float)var21.nextGaussian()));
                                float var32 = (float)(var21.nextDouble() + (double)(var9 * (float)var21.nextGaussian()) * 0.001);
                                double var33 = (double)var12 + 0.5 - param2;
                                double var34 = (double)var11 + 0.5 - param4;
                                float var35 = (float)Math.sqrt(var33 * var33 + var34 * var34) / (float)var7;
                                float var36 = ((1.0F - var35 * var35) * 0.3F + 0.5F) * var0;
                                var10.set(var12, var20, var11);
                                int var37 = getLightColor(var1, var10);
                                int var38 = var37 >> 16 & 65535;
                                int var39 = var37 & 65535;
                                int var40 = (var38 * 3 + 240) / 4;
                                int var41 = (var39 * 3 + 240) / 4;
                                var6.vertex((double)var12 - param2 - var14 + 0.5, (double)var19 - param3, (double)var11 - param4 - var15 + 0.5)
                                    .uv(0.0F + var31, (float)var18 * 0.25F + var30 + var32)
                                    .color(1.0F, 1.0F, 1.0F, var36)
                                    .uv2(var41, var40)
                                    .endVertex();
                                var6.vertex((double)var12 - param2 + var14 + 0.5, (double)var19 - param3, (double)var11 - param4 + var15 + 0.5)
                                    .uv(1.0F + var31, (float)var18 * 0.25F + var30 + var32)
                                    .color(1.0F, 1.0F, 1.0F, var36)
                                    .uv2(var41, var40)
                                    .endVertex();
                                var6.vertex((double)var12 - param2 + var14 + 0.5, (double)var18 - param3, (double)var11 - param4 + var15 + 0.5)
                                    .uv(1.0F + var31, (float)var19 * 0.25F + var30 + var32)
                                    .color(1.0F, 1.0F, 1.0F, var36)
                                    .uv2(var41, var40)
                                    .endVertex();
                                var6.vertex((double)var12 - param2 - var14 + 0.5, (double)var18 - param3, (double)var11 - param4 - var15 + 0.5)
                                    .uv(0.0F + var31, (float)var19 * 0.25F + var30 + var32)
                                    .color(1.0F, 1.0F, 1.0F, var36)
                                    .uv2(var41, var40)
                                    .endVertex();
                            }
                        }
                    }
                }
            }

            if (var8 >= 0) {
                var5.end();
            }

            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            param0.turnOffLightLayer();
        }
    }

    public void tickRain(Camera param0) {
        float var0 = this.minecraft.level.getRainLevel(1.0F) / (Minecraft.useFancyGraphics() ? 1.0F : 2.0F);
        if (!(var0 <= 0.0F)) {
            RandomSource var1 = RandomSource.create((long)this.ticks * 312987231L);
            LevelReader var2 = this.minecraft.level;
            BlockPos var3 = BlockPos.containing(param0.getPosition());
            BlockPos var4 = null;
            int var5 = (int)(100.0F * var0 * var0) / (this.minecraft.options.particles().get() == ParticleStatus.DECREASED ? 2 : 1);

            for(int var6 = 0; var6 < var5; ++var6) {
                int var7 = var1.nextInt(21) - 10;
                int var8 = var1.nextInt(21) - 10;
                BlockPos var9 = var2.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, var3.offset(var7, 0, var8));
                if (var9.getY() > var2.getMinBuildHeight() && var9.getY() <= var3.getY() + 10 && var9.getY() >= var3.getY() - 10) {
                    Biome var10 = var2.getBiome(var9).value();
                    if (var10.getPrecipitationAt(var9) == Biome.Precipitation.RAIN) {
                        var4 = var9.below();
                        if (this.minecraft.options.particles().get() == ParticleStatus.MINIMAL) {
                            break;
                        }

                        double var11 = var1.nextDouble();
                        double var12 = var1.nextDouble();
                        BlockState var13 = var2.getBlockState(var4);
                        FluidState var14 = var2.getFluidState(var4);
                        VoxelShape var15 = var13.getCollisionShape(var2, var4);
                        double var16 = var15.max(Direction.Axis.Y, var11, var12);
                        double var17 = (double)var14.getHeight(var2, var4);
                        double var18 = Math.max(var16, var17);
                        ParticleOptions var19 = !var14.is(FluidTags.LAVA) && !var13.is(Blocks.MAGMA_BLOCK) && !CampfireBlock.isLitCampfire(var13)
                            ? ParticleTypes.RAIN
                            : ParticleTypes.SMOKE;
                        this.minecraft
                            .level
                            .addParticle(var19, (double)var4.getX() + var11, (double)var4.getY() + var18, (double)var4.getZ() + var12, 0.0, 0.0, 0.0);
                    }
                }
            }

            if (var4 != null && var1.nextInt(3) < this.rainSoundTime++) {
                this.rainSoundTime = 0;
                if (var4.getY() > var3.getY() + 1 && var2.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, var3).getY() > Mth.floor((float)var3.getY())) {
                    this.minecraft.level.playLocalSound(var4, SoundEvents.WEATHER_RAIN_ABOVE, SoundSource.WEATHER, 0.1F, 0.5F, false);
                } else {
                    this.minecraft.level.playLocalSound(var4, SoundEvents.WEATHER_RAIN, SoundSource.WEATHER, 0.2F, 1.0F, false);
                }
            }

        }
    }

    @Override
    public void close() {
        if (this.entityEffect != null) {
            this.entityEffect.close();
        }

        if (this.transparencyChain != null) {
            this.transparencyChain.close();
        }

    }

    @Override
    public void onResourceManagerReload(ResourceManager param0) {
        this.initOutline();
        if (Minecraft.useShaderTransparency()) {
            this.initTransparency();
        }

    }

    public void initOutline() {
        if (this.entityEffect != null) {
            this.entityEffect.close();
        }

        ResourceLocation var0 = new ResourceLocation("shaders/post/entity_outline.json");

        try {
            this.entityEffect = new PostChain(
                this.minecraft.getTextureManager(), this.minecraft.getResourceManager(), this.minecraft.getMainRenderTarget(), var0
            );
            this.entityEffect.resize(this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
            this.entityTarget = this.entityEffect.getTempTarget("final");
        } catch (IOException var3) {
            LOGGER.warn("Failed to load shader: {}", var0, var3);
            this.entityEffect = null;
            this.entityTarget = null;
        } catch (JsonSyntaxException var4) {
            LOGGER.warn("Failed to parse shader: {}", var0, var4);
            this.entityEffect = null;
            this.entityTarget = null;
        }

    }

    private void initTransparency() {
        this.deinitTransparency();
        ResourceLocation var0 = new ResourceLocation("shaders/post/transparency.json");

        try {
            PostChain var1 = new PostChain(this.minecraft.getTextureManager(), this.minecraft.getResourceManager(), this.minecraft.getMainRenderTarget(), var0);
            var1.resize(this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
            RenderTarget var2 = var1.getTempTarget("translucent");
            RenderTarget var3 = var1.getTempTarget("itemEntity");
            RenderTarget var4 = var1.getTempTarget("particles");
            RenderTarget var5 = var1.getTempTarget("weather");
            RenderTarget var6 = var1.getTempTarget("clouds");
            this.transparencyChain = var1;
            this.translucentTarget = var2;
            this.itemEntityTarget = var3;
            this.particlesTarget = var4;
            this.weatherTarget = var5;
            this.cloudsTarget = var6;
        } catch (Exception var81) {
            String var8 = var81 instanceof JsonSyntaxException ? "parse" : "load";
            String var9 = "Failed to " + var8 + " shader: " + var0;
            LevelRenderer.TransparencyShaderException var10 = new LevelRenderer.TransparencyShaderException(var9, var81);
            if (this.minecraft.getResourcePackRepository().getSelectedIds().size() > 1) {
                Component var11 = this.minecraft.getResourceManager().listPacks().findFirst().map(param0 -> Component.literal(param0.packId())).orElse(null);
                this.minecraft.options.graphicsMode().set(GraphicsStatus.FANCY);
                this.minecraft.clearResourcePacksOnError(var10, var11);
            } else {
                CrashReport var12 = this.minecraft.fillReport(new CrashReport(var9, var10));
                this.minecraft.options.graphicsMode().set(GraphicsStatus.FANCY);
                this.minecraft.options.save();
                LOGGER.error(LogUtils.FATAL_MARKER, var9, (Throwable)var10);
                this.minecraft.emergencySave();
                Minecraft.crash(var12);
            }
        }

    }

    private void deinitTransparency() {
        if (this.transparencyChain != null) {
            this.transparencyChain.close();
            this.translucentTarget.destroyBuffers();
            this.itemEntityTarget.destroyBuffers();
            this.particlesTarget.destroyBuffers();
            this.weatherTarget.destroyBuffers();
            this.cloudsTarget.destroyBuffers();
            this.transparencyChain = null;
            this.translucentTarget = null;
            this.itemEntityTarget = null;
            this.particlesTarget = null;
            this.weatherTarget = null;
            this.cloudsTarget = null;
        }

    }

    public void doEntityOutline() {
        if (this.shouldShowEntityOutlines()) {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ZERO,
                GlStateManager.DestFactor.ONE
            );
            this.entityTarget.blitToScreen(this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight(), false);
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        }

    }

    protected boolean shouldShowEntityOutlines() {
        return !this.minecraft.gameRenderer.isPanoramicMode() && this.entityTarget != null && this.entityEffect != null && this.minecraft.player != null;
    }

    private void createDarkSky() {
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();
        if (this.darkBuffer != null) {
            this.darkBuffer.close();
        }

        this.darkBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        BufferBuilder.RenderedBuffer var2 = buildSkyDisc(var1, -16.0F);
        this.darkBuffer.bind();
        this.darkBuffer.upload(var2);
        VertexBuffer.unbind();
    }

    private void createLightSky() {
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();
        if (this.skyBuffer != null) {
            this.skyBuffer.close();
        }

        this.skyBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        BufferBuilder.RenderedBuffer var2 = buildSkyDisc(var1, 16.0F);
        this.skyBuffer.bind();
        this.skyBuffer.upload(var2);
        VertexBuffer.unbind();
    }

    private static BufferBuilder.RenderedBuffer buildSkyDisc(BufferBuilder param0, float param1) {
        float var0 = Math.signum(param1) * 512.0F;
        float var1 = 512.0F;
        RenderSystem.setShader(GameRenderer::getPositionShader);
        param0.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
        param0.vertex(0.0, (double)param1, 0.0).endVertex();

        for(int var2 = -180; var2 <= 180; var2 += 45) {
            param0.vertex(
                    (double)(var0 * Mth.cos((float)var2 * (float) (Math.PI / 180.0))),
                    (double)param1,
                    (double)(512.0F * Mth.sin((float)var2 * (float) (Math.PI / 180.0)))
                )
                .endVertex();
        }

        return param0.end();
    }

    private void createStars() {
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        if (this.starBuffer != null) {
            this.starBuffer.close();
        }

        this.starBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        BufferBuilder.RenderedBuffer var2 = this.drawStars(var1);
        this.starBuffer.bind();
        this.starBuffer.upload(var2);
        VertexBuffer.unbind();
    }

    private BufferBuilder.RenderedBuffer drawStars(BufferBuilder param0) {
        RandomSource var0 = RandomSource.create(10842L);
        param0.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

        for(int var1 = 0; var1 < 1500; ++var1) {
            double var2 = (double)(var0.nextFloat() * 2.0F - 1.0F);
            double var3 = (double)(var0.nextFloat() * 2.0F - 1.0F);
            double var4 = (double)(var0.nextFloat() * 2.0F - 1.0F);
            double var5 = (double)(0.15F + var0.nextFloat() * 0.1F);
            double var6 = var2 * var2 + var3 * var3 + var4 * var4;
            if (var6 < 1.0 && var6 > 0.01) {
                var6 = 1.0 / Math.sqrt(var6);
                var2 *= var6;
                var3 *= var6;
                var4 *= var6;
                double var7 = var2 * 100.0;
                double var8 = var3 * 100.0;
                double var9 = var4 * 100.0;
                double var10 = Math.atan2(var2, var4);
                double var11 = Math.sin(var10);
                double var12 = Math.cos(var10);
                double var13 = Math.atan2(Math.sqrt(var2 * var2 + var4 * var4), var3);
                double var14 = Math.sin(var13);
                double var15 = Math.cos(var13);
                double var16 = var0.nextDouble() * Math.PI * 2.0;
                double var17 = Math.sin(var16);
                double var18 = Math.cos(var16);

                for(int var19 = 0; var19 < 4; ++var19) {
                    double var20 = 0.0;
                    double var21 = (double)((var19 & 2) - 1) * var5;
                    double var22 = (double)((var19 + 1 & 2) - 1) * var5;
                    double var23 = 0.0;
                    double var24 = var21 * var18 - var22 * var17;
                    double var25 = var22 * var18 + var21 * var17;
                    double var27 = var24 * var14 + 0.0 * var15;
                    double var28 = 0.0 * var14 - var24 * var15;
                    double var29 = var28 * var11 - var25 * var12;
                    double var31 = var25 * var11 + var28 * var12;
                    param0.vertex(var7 + var29, var8 + var27, var9 + var31).endVertex();
                }
            }
        }

        return param0.end();
    }

    public void setLevel(@Nullable ClientLevel param0) {
        this.lastCameraSectionX = Integer.MIN_VALUE;
        this.lastCameraSectionY = Integer.MIN_VALUE;
        this.lastCameraSectionZ = Integer.MIN_VALUE;
        this.entityRenderDispatcher.setLevel(param0);
        this.level = param0;
        if (param0 != null) {
            this.allChanged();
        } else {
            if (this.viewArea != null) {
                this.viewArea.releaseAllBuffers();
                this.viewArea = null;
            }

            if (this.sectionRenderDispatcher != null) {
                this.sectionRenderDispatcher.dispose();
            }

            this.sectionRenderDispatcher = null;
            this.globalBlockEntities.clear();
            this.sectionOcclusionGraph.waitAndReset(null);
            this.visibleSections.clear();
        }

    }

    public void graphicsChanged() {
        if (Minecraft.useShaderTransparency()) {
            this.initTransparency();
        } else {
            this.deinitTransparency();
        }

    }

    public void allChanged() {
        if (this.level != null) {
            this.graphicsChanged();
            this.level.clearTintCaches();
            if (this.sectionRenderDispatcher == null) {
                this.sectionRenderDispatcher = new SectionRenderDispatcher(
                    this.level, this, Util.backgroundExecutor(), this.minecraft.is64Bit(), this.renderBuffers.fixedBufferPack()
                );
            } else {
                this.sectionRenderDispatcher.setLevel(this.level);
            }

            this.generateClouds = true;
            ItemBlockRenderTypes.setFancy(Minecraft.useFancyGraphics());
            this.lastViewDistance = this.minecraft.options.getEffectiveRenderDistance();
            if (this.viewArea != null) {
                this.viewArea.releaseAllBuffers();
            }

            this.sectionRenderDispatcher.blockUntilClear();
            synchronized(this.globalBlockEntities) {
                this.globalBlockEntities.clear();
            }

            this.viewArea = new ViewArea(this.sectionRenderDispatcher, this.level, this.minecraft.options.getEffectiveRenderDistance(), this);
            this.sectionOcclusionGraph.waitAndReset(this.viewArea);
            this.visibleSections.clear();
            Entity var0 = this.minecraft.getCameraEntity();
            if (var0 != null) {
                this.viewArea.repositionCamera(var0.getX(), var0.getZ());
            }

        }
    }

    public void resize(int param0, int param1) {
        this.needsUpdate();
        if (this.entityEffect != null) {
            this.entityEffect.resize(param0, param1);
        }

        if (this.transparencyChain != null) {
            this.transparencyChain.resize(param0, param1);
        }

    }

    public String getSectionStatistics() {
        int var0 = this.viewArea.sections.length;
        int var1 = this.countRenderedSections();
        return String.format(
            Locale.ROOT,
            "C: %d/%d %sD: %d, %s",
            var1,
            var0,
            this.minecraft.smartCull ? "(s) " : "",
            this.lastViewDistance,
            this.sectionRenderDispatcher == null ? "null" : this.sectionRenderDispatcher.getStats()
        );
    }

    public SectionRenderDispatcher getSectionRenderDispatcher() {
        return this.sectionRenderDispatcher;
    }

    public double getTotalSections() {
        return (double)this.viewArea.sections.length;
    }

    public double getLastViewDistance() {
        return (double)this.lastViewDistance;
    }

    public int countRenderedSections() {
        int var0 = 0;

        for(SectionRenderDispatcher.RenderSection var1 : this.visibleSections) {
            if (!var1.getCompiled().hasNoRenderableLayers()) {
                ++var0;
            }
        }

        return var0;
    }

    public String getEntityStatistics() {
        return "E: "
            + this.renderedEntities
            + "/"
            + this.level.getEntityCount()
            + ", B: "
            + this.culledEntities
            + ", SD: "
            + this.level.getServerSimulationDistance();
    }

    private void setupRender(Camera param0, Frustum param1, boolean param2, boolean param3) {
        Vec3 var0 = param0.getPosition();
        if (this.minecraft.options.getEffectiveRenderDistance() != this.lastViewDistance) {
            this.allChanged();
        }

        this.level.getProfiler().push("camera");
        double var1 = this.minecraft.player.getX();
        double var2 = this.minecraft.player.getY();
        double var3 = this.minecraft.player.getZ();
        int var4 = SectionPos.posToSectionCoord(var1);
        int var5 = SectionPos.posToSectionCoord(var2);
        int var6 = SectionPos.posToSectionCoord(var3);
        if (this.lastCameraSectionX != var4 || this.lastCameraSectionY != var5 || this.lastCameraSectionZ != var6) {
            this.lastCameraSectionX = var4;
            this.lastCameraSectionY = var5;
            this.lastCameraSectionZ = var6;
            this.viewArea.repositionCamera(var1, var3);
        }

        this.sectionRenderDispatcher.setCamera(var0);
        this.level.getProfiler().popPush("cull");
        this.minecraft.getProfiler().popPush("culling");
        BlockPos var7 = param0.getBlockPosition();
        double var8 = Math.floor(var0.x / 8.0);
        double var9 = Math.floor(var0.y / 8.0);
        double var10 = Math.floor(var0.z / 8.0);
        if (var8 != this.prevCamX || var9 != this.prevCamY || var10 != this.prevCamZ) {
            this.sectionOcclusionGraph.invalidate();
        }

        this.prevCamX = var8;
        this.prevCamY = var9;
        this.prevCamZ = var10;
        this.minecraft.getProfiler().popPush("update");
        if (!param2) {
            boolean var11 = this.minecraft.smartCull;
            if (param3 && this.level.getBlockState(var7).isSolidRender(this.level, var7)) {
                var11 = false;
            }

            Entity.setViewScale(
                Mth.clamp((double)this.minecraft.options.getEffectiveRenderDistance() / 8.0, 1.0, 2.5) * this.minecraft.options.entityDistanceScaling().get()
            );
            this.minecraft.getProfiler().push("section_occlusion_graph");
            this.sectionOcclusionGraph.update(var11, param0, param1, this.visibleSections);
            this.minecraft.getProfiler().pop();
            double var12 = Math.floor((double)(param0.getXRot() / 2.0F));
            double var13 = Math.floor((double)(param0.getYRot() / 2.0F));
            if (this.sectionOcclusionGraph.consumeFrustumUpdate() || var12 != this.prevCamRotX || var13 != this.prevCamRotY) {
                this.applyFrustum(offsetFrustum(param1));
                this.prevCamRotX = var12;
                this.prevCamRotY = var13;
            }
        }

        this.minecraft.getProfiler().pop();
    }

    public static Frustum offsetFrustum(Frustum param0) {
        return new Frustum(param0).offsetToFullyIncludeCameraCube(8);
    }

    private void applyFrustum(Frustum param0) {
        if (!Minecraft.getInstance().isSameThread()) {
            throw new IllegalStateException("applyFrustum called from wrong thread: " + Thread.currentThread().getName());
        } else {
            this.minecraft.getProfiler().push("apply_frustum");
            this.visibleSections.clear();
            this.sectionOcclusionGraph.addSectionsInFrustum(param0, this.visibleSections);
            this.minecraft.getProfiler().pop();
        }
    }

    public void addRecentlyCompiledSection(SectionRenderDispatcher.RenderSection param0) {
        this.sectionOcclusionGraph.onSectionCompiled(param0);
    }

    private void captureFrustum(Matrix4f param0, Matrix4f param1, double param2, double param3, double param4, Frustum param5) {
        this.capturedFrustum = param5;
        Matrix4f var0 = new Matrix4f(param1);
        var0.mul(param0);
        var0.invert();
        this.frustumPos.x = param2;
        this.frustumPos.y = param3;
        this.frustumPos.z = param4;
        this.frustumPoints[0] = new Vector4f(-1.0F, -1.0F, -1.0F, 1.0F);
        this.frustumPoints[1] = new Vector4f(1.0F, -1.0F, -1.0F, 1.0F);
        this.frustumPoints[2] = new Vector4f(1.0F, 1.0F, -1.0F, 1.0F);
        this.frustumPoints[3] = new Vector4f(-1.0F, 1.0F, -1.0F, 1.0F);
        this.frustumPoints[4] = new Vector4f(-1.0F, -1.0F, 1.0F, 1.0F);
        this.frustumPoints[5] = new Vector4f(1.0F, -1.0F, 1.0F, 1.0F);
        this.frustumPoints[6] = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.frustumPoints[7] = new Vector4f(-1.0F, 1.0F, 1.0F, 1.0F);

        for(int var1 = 0; var1 < 8; ++var1) {
            var0.transform(this.frustumPoints[var1]);
            this.frustumPoints[var1].div(this.frustumPoints[var1].w());
        }

    }

    public void prepareCullFrustum(PoseStack param0, Vec3 param1, Matrix4f param2) {
        Matrix4f var0 = param0.last().pose();
        double var1 = param1.x();
        double var2 = param1.y();
        double var3 = param1.z();
        this.cullingFrustum = new Frustum(var0, param2);
        this.cullingFrustum.prepare(var1, var2, var3);
    }

    public void renderLevel(
        PoseStack param0, float param1, long param2, boolean param3, Camera param4, GameRenderer param5, LightTexture param6, Matrix4f param7
    ) {
        RenderSystem.setShaderGameTime(this.level.getGameTime(), param1);
        this.blockEntityRenderDispatcher.prepare(this.level, param4, this.minecraft.hitResult);
        this.entityRenderDispatcher.prepare(this.level, param4, this.minecraft.crosshairPickEntity);
        ProfilerFiller var0 = this.level.getProfiler();
        var0.popPush("light_update_queue");
        this.level.pollLightUpdates();
        var0.popPush("light_updates");
        this.level.getChunkSource().getLightEngine().runLightUpdates();
        Vec3 var1 = param4.getPosition();
        double var2 = var1.x();
        double var3 = var1.y();
        double var4 = var1.z();
        Matrix4f var5 = param0.last().pose();
        var0.popPush("culling");
        boolean var6 = this.capturedFrustum != null;
        Frustum var7;
        if (var6) {
            var7 = this.capturedFrustum;
            var7.prepare(this.frustumPos.x, this.frustumPos.y, this.frustumPos.z);
        } else {
            var7 = this.cullingFrustum;
        }

        this.minecraft.getProfiler().popPush("captureFrustum");
        if (this.captureFrustum) {
            this.captureFrustum(var5, param7, var1.x, var1.y, var1.z, var6 ? new Frustum(var5, param7) : var7);
            this.captureFrustum = false;
        }

        var0.popPush("clear");
        FogRenderer.setupColor(param4, param1, this.minecraft.level, this.minecraft.options.getEffectiveRenderDistance(), param5.getDarkenWorldAmount(param1));
        FogRenderer.levelFogColor();
        RenderSystem.clear(16640, Minecraft.ON_OSX);
        float var9 = param5.getRenderDistance();
        boolean var10 = this.minecraft.level.effects().isFoggyAt(Mth.floor(var2), Mth.floor(var3))
            || this.minecraft.gui.getBossOverlay().shouldCreateWorldFog();
        var0.popPush("sky");
        RenderSystem.setShader(GameRenderer::getPositionShader);
        this.renderSky(param0, param7, param1, param4, var10, () -> FogRenderer.setupFog(param4, FogRenderer.FogMode.FOG_SKY, var9, var10, param1));
        var0.popPush("fog");
        FogRenderer.setupFog(param4, FogRenderer.FogMode.FOG_TERRAIN, Math.max(var9, 32.0F), var10, param1);
        var0.popPush("terrain_setup");
        this.setupRender(param4, var7, var6, this.minecraft.player.isSpectator());
        var0.popPush("compile_sections");
        this.compileSections(param4);
        var0.popPush("terrain");
        this.renderSectionLayer(RenderType.solid(), param0, var2, var3, var4, param7);
        this.renderSectionLayer(RenderType.cutoutMipped(), param0, var2, var3, var4, param7);
        this.renderSectionLayer(RenderType.cutout(), param0, var2, var3, var4, param7);
        if (this.level.effects().constantAmbientLight()) {
            Lighting.setupNetherLevel(param0.last().pose());
        } else {
            Lighting.setupLevel(param0.last().pose());
        }

        var0.popPush("entities");
        this.renderedEntities = 0;
        this.culledEntities = 0;
        if (this.itemEntityTarget != null) {
            this.itemEntityTarget.clear(Minecraft.ON_OSX);
            this.itemEntityTarget.copyDepthFrom(this.minecraft.getMainRenderTarget());
            this.minecraft.getMainRenderTarget().bindWrite(false);
        }

        if (this.weatherTarget != null) {
            this.weatherTarget.clear(Minecraft.ON_OSX);
        }

        if (this.shouldShowEntityOutlines()) {
            this.entityTarget.clear(Minecraft.ON_OSX);
            this.minecraft.getMainRenderTarget().bindWrite(false);
        }

        boolean var11 = false;
        MultiBufferSource.BufferSource var12 = this.renderBuffers.bufferSource();

        for(Entity var13 : this.level.entitiesForRendering()) {
            if (this.entityRenderDispatcher.shouldRender(var13, var7, var2, var3, var4) || var13.hasIndirectPassenger(this.minecraft.player)) {
                BlockPos var14 = var13.blockPosition();
                if ((this.level.isOutsideBuildHeight(var14.getY()) || this.isSectionCompiled(var14))
                    && (
                        var13 != param4.getEntity()
                            || param4.isDetached()
                            || param4.getEntity() instanceof LivingEntity && ((LivingEntity)param4.getEntity()).isSleeping()
                    )
                    && (!(var13 instanceof LocalPlayer) || param4.getEntity() == var13)) {
                    ++this.renderedEntities;
                    if (var13.tickCount == 0) {
                        var13.xOld = var13.getX();
                        var13.yOld = var13.getY();
                        var13.zOld = var13.getZ();
                    }

                    MultiBufferSource var16;
                    if (this.shouldShowEntityOutlines() && this.minecraft.shouldEntityAppearGlowing(var13)) {
                        var11 = true;
                        OutlineBufferSource var15 = this.renderBuffers.outlineBufferSource();
                        var16 = var15;
                        int var17 = var13.getTeamColor();
                        var15.setColor(FastColor.ARGB32.red(var17), FastColor.ARGB32.green(var17), FastColor.ARGB32.blue(var17), 255);
                    } else {
                        var16 = var12;
                    }

                    this.renderEntity(var13, var2, var3, var4, param1, param0, var16);
                }
            }
        }

        var12.endLastBatch();
        this.checkPoseStack(param0);
        var12.endBatch(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS));
        var12.endBatch(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS));
        var12.endBatch(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
        var12.endBatch(RenderType.entitySmoothCutout(TextureAtlas.LOCATION_BLOCKS));
        var0.popPush("blockentities");

        for(SectionRenderDispatcher.RenderSection var19 : this.visibleSections) {
            List<BlockEntity> var20 = var19.getCompiled().getRenderableBlockEntities();
            if (!var20.isEmpty()) {
                for(BlockEntity var21 : var20) {
                    BlockPos var22 = var21.getBlockPos();
                    MultiBufferSource var23 = var12;
                    param0.pushPose();
                    param0.translate((double)var22.getX() - var2, (double)var22.getY() - var3, (double)var22.getZ() - var4);
                    SortedSet<BlockDestructionProgress> var24 = this.destructionProgress.get(var22.asLong());
                    if (var24 != null && !var24.isEmpty()) {
                        int var25 = var24.last().getProgress();
                        if (var25 >= 0) {
                            PoseStack.Pose var26 = param0.last();
                            VertexConsumer var27 = new SheetedDecalTextureGenerator(
                                this.renderBuffers.crumblingBufferSource().getBuffer(ModelBakery.DESTROY_TYPES.get(var25)), var26.pose(), var26.normal(), 1.0F
                            );
                            var23 = param2x -> {
                                VertexConsumer var0x = var12.getBuffer(param2x);
                                return param2x.affectsCrumbling() ? VertexMultiConsumer.create(var27, var0x) : var0x;
                            };
                        }
                    }

                    this.blockEntityRenderDispatcher.render(var21, param1, param0, var23);
                    param0.popPose();
                }
            }
        }

        synchronized(this.globalBlockEntities) {
            for(BlockEntity var28 : this.globalBlockEntities) {
                BlockPos var29 = var28.getBlockPos();
                param0.pushPose();
                param0.translate((double)var29.getX() - var2, (double)var29.getY() - var3, (double)var29.getZ() - var4);
                this.blockEntityRenderDispatcher.render(var28, param1, param0, var12);
                param0.popPose();
            }
        }

        this.checkPoseStack(param0);
        var12.endBatch(RenderType.solid());
        var12.endBatch(RenderType.endPortal());
        var12.endBatch(RenderType.endGateway());
        var12.endBatch(Sheets.solidBlockSheet());
        var12.endBatch(Sheets.cutoutBlockSheet());
        var12.endBatch(Sheets.bedSheet());
        var12.endBatch(Sheets.shulkerBoxSheet());
        var12.endBatch(Sheets.signSheet());
        var12.endBatch(Sheets.hangingSignSheet());
        var12.endBatch(Sheets.chestSheet());
        this.renderBuffers.outlineBufferSource().endOutlineBatch();
        if (var11) {
            this.entityEffect.process(param1);
            this.minecraft.getMainRenderTarget().bindWrite(false);
        }

        var0.popPush("destroyProgress");

        for(Entry<SortedSet<BlockDestructionProgress>> var30 : this.destructionProgress.long2ObjectEntrySet()) {
            BlockPos var31 = BlockPos.of(var30.getLongKey());
            double var32 = (double)var31.getX() - var2;
            double var33 = (double)var31.getY() - var3;
            double var34 = (double)var31.getZ() - var4;
            if (!(var32 * var32 + var33 * var33 + var34 * var34 > 1024.0)) {
                SortedSet<BlockDestructionProgress> var35 = var30.getValue();
                if (var35 != null && !var35.isEmpty()) {
                    int var36 = var35.last().getProgress();
                    param0.pushPose();
                    param0.translate((double)var31.getX() - var2, (double)var31.getY() - var3, (double)var31.getZ() - var4);
                    PoseStack.Pose var37 = param0.last();
                    VertexConsumer var38 = new SheetedDecalTextureGenerator(
                        this.renderBuffers.crumblingBufferSource().getBuffer(ModelBakery.DESTROY_TYPES.get(var36)), var37.pose(), var37.normal(), 1.0F
                    );
                    this.minecraft.getBlockRenderer().renderBreakingTexture(this.level.getBlockState(var31), var31, this.level, param0, var38);
                    param0.popPose();
                }
            }
        }

        this.checkPoseStack(param0);
        HitResult var39 = this.minecraft.hitResult;
        if (param3 && var39 != null && var39.getType() == HitResult.Type.BLOCK) {
            var0.popPush("outline");
            BlockPos var40 = ((BlockHitResult)var39).getBlockPos();
            BlockState var41 = this.level.getBlockState(var40);
            if (!var41.isAir() && this.level.getWorldBorder().isWithinBounds(var40)) {
                VertexConsumer var42 = var12.getBuffer(RenderType.lines());
                this.renderHitOutline(param0, var42, param4.getEntity(), var2, var3, var4, var40, var41);
            }
        }

        this.minecraft.debugRenderer.render(param0, var12, var2, var3, var4);
        var12.endLastBatch();
        PoseStack var43 = RenderSystem.getModelViewStack();
        RenderSystem.applyModelViewMatrix();
        var12.endBatch(Sheets.translucentCullBlockSheet());
        var12.endBatch(Sheets.bannerSheet());
        var12.endBatch(Sheets.shieldSheet());
        var12.endBatch(RenderType.armorGlint());
        var12.endBatch(RenderType.armorEntityGlint());
        var12.endBatch(RenderType.glint());
        var12.endBatch(RenderType.glintDirect());
        var12.endBatch(RenderType.glintTranslucent());
        var12.endBatch(RenderType.entityGlint());
        var12.endBatch(RenderType.entityGlintDirect());
        var12.endBatch(RenderType.waterMask());
        this.renderBuffers.crumblingBufferSource().endBatch();
        if (this.transparencyChain != null) {
            var12.endBatch(RenderType.lines());
            var12.endBatch();
            this.translucentTarget.clear(Minecraft.ON_OSX);
            this.translucentTarget.copyDepthFrom(this.minecraft.getMainRenderTarget());
            var0.popPush("translucent");
            this.renderSectionLayer(RenderType.translucent(), param0, var2, var3, var4, param7);
            var0.popPush("string");
            this.renderSectionLayer(RenderType.tripwire(), param0, var2, var3, var4, param7);
            this.particlesTarget.clear(Minecraft.ON_OSX);
            this.particlesTarget.copyDepthFrom(this.minecraft.getMainRenderTarget());
            RenderStateShard.PARTICLES_TARGET.setupRenderState();
            var0.popPush("particles");
            this.minecraft.particleEngine.render(param0, var12, param6, param4, param1);
            RenderStateShard.PARTICLES_TARGET.clearRenderState();
        } else {
            var0.popPush("translucent");
            if (this.translucentTarget != null) {
                this.translucentTarget.clear(Minecraft.ON_OSX);
            }

            this.renderSectionLayer(RenderType.translucent(), param0, var2, var3, var4, param7);
            var12.endBatch(RenderType.lines());
            var12.endBatch();
            var0.popPush("string");
            this.renderSectionLayer(RenderType.tripwire(), param0, var2, var3, var4, param7);
            var0.popPush("particles");
            this.minecraft.particleEngine.render(param0, var12, param6, param4, param1);
        }

        var43.pushPose();
        var43.mulPoseMatrix(param0.last().pose());
        RenderSystem.applyModelViewMatrix();
        if (this.minecraft.options.getCloudsType() != CloudStatus.OFF) {
            if (this.transparencyChain != null) {
                this.cloudsTarget.clear(Minecraft.ON_OSX);
                RenderStateShard.CLOUDS_TARGET.setupRenderState();
                var0.popPush("clouds");
                this.renderClouds(param0, param7, param1, var2, var3, var4);
                RenderStateShard.CLOUDS_TARGET.clearRenderState();
            } else {
                var0.popPush("clouds");
                RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
                this.renderClouds(param0, param7, param1, var2, var3, var4);
            }
        }

        if (this.transparencyChain != null) {
            RenderStateShard.WEATHER_TARGET.setupRenderState();
            var0.popPush("weather");
            this.renderSnowAndRain(param6, param1, var2, var3, var4);
            this.renderWorldBorder(param4);
            RenderStateShard.WEATHER_TARGET.clearRenderState();
            this.transparencyChain.process(param1);
            this.minecraft.getMainRenderTarget().bindWrite(false);
        } else {
            RenderSystem.depthMask(false);
            var0.popPush("weather");
            this.renderSnowAndRain(param6, param1, var2, var3, var4);
            this.renderWorldBorder(param4);
            RenderSystem.depthMask(true);
        }

        var43.popPose();
        RenderSystem.applyModelViewMatrix();
        this.renderDebug(param0, var12, param4);
        var12.endLastBatch();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        FogRenderer.setupNoFog();
    }

    private void checkPoseStack(PoseStack param0) {
        if (!param0.clear()) {
            throw new IllegalStateException("Pose stack not empty");
        }
    }

    private void renderEntity(Entity param0, double param1, double param2, double param3, float param4, PoseStack param5, MultiBufferSource param6) {
        double var0 = Mth.lerp((double)param4, param0.xOld, param0.getX());
        double var1 = Mth.lerp((double)param4, param0.yOld, param0.getY());
        double var2 = Mth.lerp((double)param4, param0.zOld, param0.getZ());
        float var3 = Mth.lerp(param4, param0.yRotO, param0.getYRot());
        this.entityRenderDispatcher
            .render(
                param0,
                var0 - param1,
                var1 - param2,
                var2 - param3,
                var3,
                param4,
                param5,
                param6,
                this.entityRenderDispatcher.getPackedLightCoords(param0, param4)
            );
    }

    private void renderSectionLayer(RenderType param0, PoseStack param1, double param2, double param3, double param4, Matrix4f param5) {
        RenderSystem.assertOnRenderThread();
        param0.setupRenderState();
        if (param0 == RenderType.translucent()) {
            this.minecraft.getProfiler().push("translucent_sort");
            double var0 = param2 - this.xTransparentOld;
            double var1 = param3 - this.yTransparentOld;
            double var2 = param4 - this.zTransparentOld;
            if (var0 * var0 + var1 * var1 + var2 * var2 > 1.0) {
                int var3 = SectionPos.posToSectionCoord(param2);
                int var4 = SectionPos.posToSectionCoord(param3);
                int var5 = SectionPos.posToSectionCoord(param4);
                boolean var6 = var3 != SectionPos.posToSectionCoord(this.xTransparentOld)
                    || var5 != SectionPos.posToSectionCoord(this.zTransparentOld)
                    || var4 != SectionPos.posToSectionCoord(this.yTransparentOld);
                this.xTransparentOld = param2;
                this.yTransparentOld = param3;
                this.zTransparentOld = param4;
                int var7 = 0;

                for(SectionRenderDispatcher.RenderSection var8 : this.visibleSections) {
                    if (var7 < 15 && (var6 || var8.isAxisAlignedWith(var3, var4, var5)) && var8.resortTransparency(param0, this.sectionRenderDispatcher)) {
                        ++var7;
                    }
                }
            }

            this.minecraft.getProfiler().pop();
        }

        this.minecraft.getProfiler().push("filterempty");
        this.minecraft.getProfiler().popPush(() -> "render_" + param0);
        boolean var9 = param0 != RenderType.translucent();
        ObjectListIterator<SectionRenderDispatcher.RenderSection> var10 = this.visibleSections.listIterator(var9 ? 0 : this.visibleSections.size());
        ShaderInstance var11 = RenderSystem.getShader();

        for(int var12 = 0; var12 < 12; ++var12) {
            int var13 = RenderSystem.getShaderTexture(var12);
            var11.setSampler("Sampler" + var12, var13);
        }

        if (var11.MODEL_VIEW_MATRIX != null) {
            var11.MODEL_VIEW_MATRIX.set(param1.last().pose());
        }

        if (var11.PROJECTION_MATRIX != null) {
            var11.PROJECTION_MATRIX.set(param5);
        }

        if (var11.COLOR_MODULATOR != null) {
            var11.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
        }

        if (var11.GLINT_ALPHA != null) {
            var11.GLINT_ALPHA.set(RenderSystem.getShaderGlintAlpha());
        }

        if (var11.FOG_START != null) {
            var11.FOG_START.set(RenderSystem.getShaderFogStart());
        }

        if (var11.FOG_END != null) {
            var11.FOG_END.set(RenderSystem.getShaderFogEnd());
        }

        if (var11.FOG_COLOR != null) {
            var11.FOG_COLOR.set(RenderSystem.getShaderFogColor());
        }

        if (var11.FOG_SHAPE != null) {
            var11.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
        }

        if (var11.TEXTURE_MATRIX != null) {
            var11.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
        }

        if (var11.GAME_TIME != null) {
            var11.GAME_TIME.set(RenderSystem.getShaderGameTime());
        }

        RenderSystem.setupShaderLights(var11);
        var11.apply();
        Uniform var14 = var11.CHUNK_OFFSET;

        while(true) {
            if (var9) {
                if (!var10.hasNext()) {
                    break;
                }
            } else if (!var10.hasPrevious()) {
                break;
            }

            SectionRenderDispatcher.RenderSection var15 = var9 ? var10.next() : var10.previous();
            if (!var15.getCompiled().isEmpty(param0)) {
                VertexBuffer var16 = var15.getBuffer(param0);
                BlockPos var17 = var15.getOrigin();
                if (var14 != null) {
                    var14.set((float)((double)var17.getX() - param2), (float)((double)var17.getY() - param3), (float)((double)var17.getZ() - param4));
                    var14.upload();
                }

                var16.bind();
                var16.draw();
            }
        }

        if (var14 != null) {
            var14.set(0.0F, 0.0F, 0.0F);
        }

        var11.clear();
        VertexBuffer.unbind();
        this.minecraft.getProfiler().pop();
        param0.clearRenderState();
    }

    private void renderDebug(PoseStack param0, MultiBufferSource param1, Camera param2) {
        if (this.minecraft.sectionPath || this.minecraft.sectionVisibility) {
            double var0 = param2.getPosition().x();
            double var1 = param2.getPosition().y();
            double var2 = param2.getPosition().z();

            for(SectionRenderDispatcher.RenderSection var3 : this.visibleSections) {
                SectionOcclusionGraph.Node var4 = this.sectionOcclusionGraph.getNode(var3);
                if (var4 != null) {
                    BlockPos var5 = var3.getOrigin();
                    param0.pushPose();
                    param0.translate((double)var5.getX() - var0, (double)var5.getY() - var1, (double)var5.getZ() - var2);
                    Matrix4f var6 = param0.last().pose();
                    if (this.minecraft.sectionPath) {
                        VertexConsumer var7 = param1.getBuffer(RenderType.lines());
                        int var8 = var4.step == 0 ? 0 : Mth.hsvToRgb((float)var4.step / 50.0F, 0.9F, 0.9F);
                        int var9 = var8 >> 16 & 0xFF;
                        int var10 = var8 >> 8 & 0xFF;
                        int var11 = var8 & 0xFF;

                        for(int var12 = 0; var12 < DIRECTIONS.length; ++var12) {
                            if (var4.hasSourceDirection(var12)) {
                                Direction var13 = DIRECTIONS[var12];
                                var7.vertex(var6, 8.0F, 8.0F, 8.0F)
                                    .color(var9, var10, var11, 255)
                                    .normal((float)var13.getStepX(), (float)var13.getStepY(), (float)var13.getStepZ())
                                    .endVertex();
                                var7.vertex(var6, (float)(8 - 16 * var13.getStepX()), (float)(8 - 16 * var13.getStepY()), (float)(8 - 16 * var13.getStepZ()))
                                    .color(var9, var10, var11, 255)
                                    .normal((float)var13.getStepX(), (float)var13.getStepY(), (float)var13.getStepZ())
                                    .endVertex();
                            }
                        }
                    }

                    if (this.minecraft.sectionVisibility && !var3.getCompiled().hasNoRenderableLayers()) {
                        VertexConsumer var14 = param1.getBuffer(RenderType.lines());
                        int var15 = 0;

                        for(Direction var16 : DIRECTIONS) {
                            for(Direction var17 : DIRECTIONS) {
                                boolean var18 = var3.getCompiled().facesCanSeeEachother(var16, var17);
                                if (!var18) {
                                    ++var15;
                                    var14.vertex(var6, (float)(8 + 8 * var16.getStepX()), (float)(8 + 8 * var16.getStepY()), (float)(8 + 8 * var16.getStepZ()))
                                        .color(255, 0, 0, 255)
                                        .normal((float)var16.getStepX(), (float)var16.getStepY(), (float)var16.getStepZ())
                                        .endVertex();
                                    var14.vertex(var6, (float)(8 + 8 * var17.getStepX()), (float)(8 + 8 * var17.getStepY()), (float)(8 + 8 * var17.getStepZ()))
                                        .color(255, 0, 0, 255)
                                        .normal((float)var17.getStepX(), (float)var17.getStepY(), (float)var17.getStepZ())
                                        .endVertex();
                                }
                            }
                        }

                        if (var15 > 0) {
                            VertexConsumer var19 = param1.getBuffer(RenderType.debugQuads());
                            float var20 = 0.5F;
                            float var21 = 0.2F;
                            var19.vertex(var6, 0.5F, 15.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                            var19.vertex(var6, 15.5F, 15.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                            var19.vertex(var6, 15.5F, 15.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                            var19.vertex(var6, 0.5F, 15.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                            var19.vertex(var6, 0.5F, 0.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                            var19.vertex(var6, 15.5F, 0.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                            var19.vertex(var6, 15.5F, 0.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                            var19.vertex(var6, 0.5F, 0.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                            var19.vertex(var6, 0.5F, 15.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                            var19.vertex(var6, 0.5F, 15.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                            var19.vertex(var6, 0.5F, 0.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                            var19.vertex(var6, 0.5F, 0.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                            var19.vertex(var6, 15.5F, 0.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                            var19.vertex(var6, 15.5F, 0.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                            var19.vertex(var6, 15.5F, 15.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                            var19.vertex(var6, 15.5F, 15.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                            var19.vertex(var6, 0.5F, 0.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                            var19.vertex(var6, 15.5F, 0.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                            var19.vertex(var6, 15.5F, 15.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                            var19.vertex(var6, 0.5F, 15.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                            var19.vertex(var6, 0.5F, 15.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                            var19.vertex(var6, 15.5F, 15.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                            var19.vertex(var6, 15.5F, 0.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                            var19.vertex(var6, 0.5F, 0.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        }
                    }

                    param0.popPose();
                }
            }
        }

        if (this.capturedFrustum != null) {
            param0.pushPose();
            param0.translate(
                (float)(this.frustumPos.x - param2.getPosition().x),
                (float)(this.frustumPos.y - param2.getPosition().y),
                (float)(this.frustumPos.z - param2.getPosition().z)
            );
            Matrix4f var22 = param0.last().pose();
            VertexConsumer var23 = param1.getBuffer(RenderType.debugQuads());
            this.addFrustumQuad(var23, var22, 0, 1, 2, 3, 0, 1, 1);
            this.addFrustumQuad(var23, var22, 4, 5, 6, 7, 1, 0, 0);
            this.addFrustumQuad(var23, var22, 0, 1, 5, 4, 1, 1, 0);
            this.addFrustumQuad(var23, var22, 2, 3, 7, 6, 0, 0, 1);
            this.addFrustumQuad(var23, var22, 0, 4, 7, 3, 0, 1, 0);
            this.addFrustumQuad(var23, var22, 1, 5, 6, 2, 1, 0, 1);
            VertexConsumer var24 = param1.getBuffer(RenderType.lines());
            this.addFrustumVertex(var24, var22, 0);
            this.addFrustumVertex(var24, var22, 1);
            this.addFrustumVertex(var24, var22, 1);
            this.addFrustumVertex(var24, var22, 2);
            this.addFrustumVertex(var24, var22, 2);
            this.addFrustumVertex(var24, var22, 3);
            this.addFrustumVertex(var24, var22, 3);
            this.addFrustumVertex(var24, var22, 0);
            this.addFrustumVertex(var24, var22, 4);
            this.addFrustumVertex(var24, var22, 5);
            this.addFrustumVertex(var24, var22, 5);
            this.addFrustumVertex(var24, var22, 6);
            this.addFrustumVertex(var24, var22, 6);
            this.addFrustumVertex(var24, var22, 7);
            this.addFrustumVertex(var24, var22, 7);
            this.addFrustumVertex(var24, var22, 4);
            this.addFrustumVertex(var24, var22, 0);
            this.addFrustumVertex(var24, var22, 4);
            this.addFrustumVertex(var24, var22, 1);
            this.addFrustumVertex(var24, var22, 5);
            this.addFrustumVertex(var24, var22, 2);
            this.addFrustumVertex(var24, var22, 6);
            this.addFrustumVertex(var24, var22, 3);
            this.addFrustumVertex(var24, var22, 7);
            param0.popPose();
        }

    }

    private void addFrustumVertex(VertexConsumer param0, Matrix4f param1, int param2) {
        param0.vertex(param1, this.frustumPoints[param2].x(), this.frustumPoints[param2].y(), this.frustumPoints[param2].z())
            .color(0, 0, 0, 255)
            .normal(0.0F, 0.0F, -1.0F)
            .endVertex();
    }

    private void addFrustumQuad(VertexConsumer param0, Matrix4f param1, int param2, int param3, int param4, int param5, int param6, int param7, int param8) {
        float var0 = 0.25F;
        param0.vertex(param1, this.frustumPoints[param2].x(), this.frustumPoints[param2].y(), this.frustumPoints[param2].z())
            .color((float)param6, (float)param7, (float)param8, 0.25F)
            .endVertex();
        param0.vertex(param1, this.frustumPoints[param3].x(), this.frustumPoints[param3].y(), this.frustumPoints[param3].z())
            .color((float)param6, (float)param7, (float)param8, 0.25F)
            .endVertex();
        param0.vertex(param1, this.frustumPoints[param4].x(), this.frustumPoints[param4].y(), this.frustumPoints[param4].z())
            .color((float)param6, (float)param7, (float)param8, 0.25F)
            .endVertex();
        param0.vertex(param1, this.frustumPoints[param5].x(), this.frustumPoints[param5].y(), this.frustumPoints[param5].z())
            .color((float)param6, (float)param7, (float)param8, 0.25F)
            .endVertex();
    }

    public void captureFrustum() {
        this.captureFrustum = true;
    }

    public void killFrustum() {
        this.capturedFrustum = null;
    }

    public void tick() {
        ++this.ticks;
        if (this.ticks % 20 == 0) {
            Iterator<BlockDestructionProgress> var0 = this.destroyingBlocks.values().iterator();

            while(var0.hasNext()) {
                BlockDestructionProgress var1 = var0.next();
                int var2 = var1.getUpdatedRenderTick();
                if (this.ticks - var2 > 400) {
                    var0.remove();
                    this.removeProgress(var1);
                }
            }

        }
    }

    private void removeProgress(BlockDestructionProgress param0) {
        long var0 = param0.getPos().asLong();
        Set<BlockDestructionProgress> var1 = this.destructionProgress.get(var0);
        var1.remove(param0);
        if (var1.isEmpty()) {
            this.destructionProgress.remove(var0);
        }

    }

    private void renderEndSky(PoseStack param0) {
        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, END_SKY_LOCATION);
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();

        for(int var2 = 0; var2 < 6; ++var2) {
            param0.pushPose();
            if (var2 == 1) {
                param0.mulPose(Axis.XP.rotationDegrees(90.0F));
            }

            if (var2 == 2) {
                param0.mulPose(Axis.XP.rotationDegrees(-90.0F));
            }

            if (var2 == 3) {
                param0.mulPose(Axis.XP.rotationDegrees(180.0F));
            }

            if (var2 == 4) {
                param0.mulPose(Axis.ZP.rotationDegrees(90.0F));
            }

            if (var2 == 5) {
                param0.mulPose(Axis.ZP.rotationDegrees(-90.0F));
            }

            Matrix4f var3 = param0.last().pose();
            var1.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            var1.vertex(var3, -100.0F, -100.0F, -100.0F).uv(0.0F, 0.0F).color(40, 40, 40, 255).endVertex();
            var1.vertex(var3, -100.0F, -100.0F, 100.0F).uv(0.0F, 16.0F).color(40, 40, 40, 255).endVertex();
            var1.vertex(var3, 100.0F, -100.0F, 100.0F).uv(16.0F, 16.0F).color(40, 40, 40, 255).endVertex();
            var1.vertex(var3, 100.0F, -100.0F, -100.0F).uv(16.0F, 0.0F).color(40, 40, 40, 255).endVertex();
            var0.end();
            param0.popPose();
        }

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    public void renderSky(PoseStack param0, Matrix4f param1, float param2, Camera param3, boolean param4, Runnable param5) {
        param5.run();
        if (!param4) {
            FogType var0 = param3.getFluidInCamera();
            if (var0 != FogType.POWDER_SNOW && var0 != FogType.LAVA && !this.doesMobEffectBlockSky(param3)) {
                if (this.minecraft.level.effects().skyType() == DimensionSpecialEffects.SkyType.END) {
                    this.renderEndSky(param0);
                } else if (this.minecraft.level.effects().skyType() == DimensionSpecialEffects.SkyType.NORMAL) {
                    Vec3 var1 = this.level.getSkyColor(this.minecraft.gameRenderer.getMainCamera().getPosition(), param2);
                    float var2 = (float)var1.x;
                    float var3 = (float)var1.y;
                    float var4 = (float)var1.z;
                    FogRenderer.levelFogColor();
                    BufferBuilder var5 = Tesselator.getInstance().getBuilder();
                    RenderSystem.depthMask(false);
                    RenderSystem.setShaderColor(var2, var3, var4, 1.0F);
                    ShaderInstance var6 = RenderSystem.getShader();
                    this.skyBuffer.bind();
                    this.skyBuffer.drawWithShader(param0.last().pose(), param1, var6);
                    VertexBuffer.unbind();
                    RenderSystem.enableBlend();
                    float[] var7 = this.level.effects().getSunriseColor(this.level.getTimeOfDay(param2), param2);
                    if (var7 != null) {
                        RenderSystem.setShader(GameRenderer::getPositionColorShader);
                        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                        param0.pushPose();
                        param0.mulPose(Axis.XP.rotationDegrees(90.0F));
                        float var8 = Mth.sin(this.level.getSunAngle(param2)) < 0.0F ? 180.0F : 0.0F;
                        param0.mulPose(Axis.ZP.rotationDegrees(var8));
                        param0.mulPose(Axis.ZP.rotationDegrees(90.0F));
                        float var9 = var7[0];
                        float var10 = var7[1];
                        float var11 = var7[2];
                        Matrix4f var12 = param0.last().pose();
                        var5.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
                        var5.vertex(var12, 0.0F, 100.0F, 0.0F).color(var9, var10, var11, var7[3]).endVertex();
                        int var13 = 16;

                        for(int var14 = 0; var14 <= 16; ++var14) {
                            float var15 = (float)var14 * (float) (Math.PI * 2) / 16.0F;
                            float var16 = Mth.sin(var15);
                            float var17 = Mth.cos(var15);
                            var5.vertex(var12, var16 * 120.0F, var17 * 120.0F, -var17 * 40.0F * var7[3]).color(var7[0], var7[1], var7[2], 0.0F).endVertex();
                        }

                        BufferUploader.drawWithShader(var5.end());
                        param0.popPose();
                    }

                    RenderSystem.blendFuncSeparate(
                        GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
                    );
                    param0.pushPose();
                    float var18 = 1.0F - this.level.getRainLevel(param2);
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, var18);
                    param0.mulPose(Axis.YP.rotationDegrees(-90.0F));
                    param0.mulPose(Axis.XP.rotationDegrees(this.level.getTimeOfDay(param2) * 360.0F));
                    Matrix4f var19 = param0.last().pose();
                    float var20 = 30.0F;
                    RenderSystem.setShader(GameRenderer::getPositionTexShader);
                    RenderSystem.setShaderTexture(0, SUN_LOCATION);
                    var5.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                    var5.vertex(var19, -var20, 100.0F, -var20).uv(0.0F, 0.0F).endVertex();
                    var5.vertex(var19, var20, 100.0F, -var20).uv(1.0F, 0.0F).endVertex();
                    var5.vertex(var19, var20, 100.0F, var20).uv(1.0F, 1.0F).endVertex();
                    var5.vertex(var19, -var20, 100.0F, var20).uv(0.0F, 1.0F).endVertex();
                    BufferUploader.drawWithShader(var5.end());
                    var20 = 20.0F;
                    RenderSystem.setShaderTexture(0, MOON_LOCATION);
                    int var21 = this.level.getMoonPhase();
                    int var22 = var21 % 4;
                    int var23 = var21 / 4 % 2;
                    float var24 = (float)(var22 + 0) / 4.0F;
                    float var25 = (float)(var23 + 0) / 2.0F;
                    float var26 = (float)(var22 + 1) / 4.0F;
                    float var27 = (float)(var23 + 1) / 2.0F;
                    var5.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                    var5.vertex(var19, -var20, -100.0F, var20).uv(var26, var27).endVertex();
                    var5.vertex(var19, var20, -100.0F, var20).uv(var24, var27).endVertex();
                    var5.vertex(var19, var20, -100.0F, -var20).uv(var24, var25).endVertex();
                    var5.vertex(var19, -var20, -100.0F, -var20).uv(var26, var25).endVertex();
                    BufferUploader.drawWithShader(var5.end());
                    float var28 = this.level.getStarBrightness(param2) * var18;
                    if (var28 > 0.0F) {
                        RenderSystem.setShaderColor(var28, var28, var28, var28);
                        FogRenderer.setupNoFog();
                        this.starBuffer.bind();
                        this.starBuffer.drawWithShader(param0.last().pose(), param1, GameRenderer.getPositionShader());
                        VertexBuffer.unbind();
                        param5.run();
                    }

                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    RenderSystem.disableBlend();
                    RenderSystem.defaultBlendFunc();
                    param0.popPose();
                    RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
                    double var29 = this.minecraft.player.getEyePosition(param2).y - this.level.getLevelData().getHorizonHeight(this.level);
                    if (var29 < 0.0) {
                        param0.pushPose();
                        param0.translate(0.0F, 12.0F, 0.0F);
                        this.darkBuffer.bind();
                        this.darkBuffer.drawWithShader(param0.last().pose(), param1, var6);
                        VertexBuffer.unbind();
                        param0.popPose();
                    }

                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    RenderSystem.depthMask(true);
                }
            }
        }
    }

    private boolean doesMobEffectBlockSky(Camera param0) {
        Entity var3 = param0.getEntity();
        if (!(var3 instanceof LivingEntity)) {
            return false;
        } else {
            LivingEntity var0 = (LivingEntity)var3;
            return var0.hasEffect(MobEffects.BLINDNESS) || var0.hasEffect(MobEffects.DARKNESS);
        }
    }

    public void renderClouds(PoseStack param0, Matrix4f param1, float param2, double param3, double param4, double param5) {
        float var0 = this.level.effects().getCloudHeight();
        if (!Float.isNaN(var0)) {
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
            );
            RenderSystem.depthMask(true);
            float var1 = 12.0F;
            float var2 = 4.0F;
            double var3 = 2.0E-4;
            double var4 = (double)(((float)this.ticks + param2) * 0.03F);
            double var5 = (param3 + var4) / 12.0;
            double var6 = (double)(var0 - (float)param4 + 0.33F);
            double var7 = param5 / 12.0 + 0.33F;
            var5 -= (double)(Mth.floor(var5 / 2048.0) * 2048);
            var7 -= (double)(Mth.floor(var7 / 2048.0) * 2048);
            float var8 = (float)(var5 - (double)Mth.floor(var5));
            float var9 = (float)(var6 / 4.0 - (double)Mth.floor(var6 / 4.0)) * 4.0F;
            float var10 = (float)(var7 - (double)Mth.floor(var7));
            Vec3 var11 = this.level.getCloudColor(param2);
            int var12 = (int)Math.floor(var5);
            int var13 = (int)Math.floor(var6 / 4.0);
            int var14 = (int)Math.floor(var7);
            if (var12 != this.prevCloudX
                || var13 != this.prevCloudY
                || var14 != this.prevCloudZ
                || this.minecraft.options.getCloudsType() != this.prevCloudsType
                || this.prevCloudColor.distanceToSqr(var11) > 2.0E-4) {
                this.prevCloudX = var12;
                this.prevCloudY = var13;
                this.prevCloudZ = var14;
                this.prevCloudColor = var11;
                this.prevCloudsType = this.minecraft.options.getCloudsType();
                this.generateClouds = true;
            }

            if (this.generateClouds) {
                this.generateClouds = false;
                BufferBuilder var15 = Tesselator.getInstance().getBuilder();
                if (this.cloudBuffer != null) {
                    this.cloudBuffer.close();
                }

                this.cloudBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
                BufferBuilder.RenderedBuffer var16 = this.buildClouds(var15, var5, var6, var7, var11);
                this.cloudBuffer.bind();
                this.cloudBuffer.upload(var16);
                VertexBuffer.unbind();
            }

            RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
            RenderSystem.setShaderTexture(0, CLOUDS_LOCATION);
            FogRenderer.levelFogColor();
            param0.pushPose();
            param0.scale(12.0F, 1.0F, 12.0F);
            param0.translate(-var8, var9, -var10);
            if (this.cloudBuffer != null) {
                this.cloudBuffer.bind();
                int var17 = this.prevCloudsType == CloudStatus.FANCY ? 0 : 1;

                for(int var18 = var17; var18 < 2; ++var18) {
                    if (var18 == 0) {
                        RenderSystem.colorMask(false, false, false, false);
                    } else {
                        RenderSystem.colorMask(true, true, true, true);
                    }

                    ShaderInstance var19 = RenderSystem.getShader();
                    this.cloudBuffer.drawWithShader(param0.last().pose(), param1, var19);
                }

                VertexBuffer.unbind();
            }

            param0.popPose();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        }
    }

    private BufferBuilder.RenderedBuffer buildClouds(BufferBuilder param0, double param1, double param2, double param3, Vec3 param4) {
        float var0 = 4.0F;
        float var1 = 0.00390625F;
        int var2 = 8;
        int var3 = 4;
        float var4 = 9.765625E-4F;
        float var5 = (float)Mth.floor(param1) * 0.00390625F;
        float var6 = (float)Mth.floor(param3) * 0.00390625F;
        float var7 = (float)param4.x;
        float var8 = (float)param4.y;
        float var9 = (float)param4.z;
        float var10 = var7 * 0.9F;
        float var11 = var8 * 0.9F;
        float var12 = var9 * 0.9F;
        float var13 = var7 * 0.7F;
        float var14 = var8 * 0.7F;
        float var15 = var9 * 0.7F;
        float var16 = var7 * 0.8F;
        float var17 = var8 * 0.8F;
        float var18 = var9 * 0.8F;
        RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
        param0.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
        float var19 = (float)Math.floor(param2 / 4.0) * 4.0F;
        if (this.prevCloudsType == CloudStatus.FANCY) {
            for(int var20 = -3; var20 <= 4; ++var20) {
                for(int var21 = -3; var21 <= 4; ++var21) {
                    float var22 = (float)(var20 * 8);
                    float var23 = (float)(var21 * 8);
                    if (var19 > -5.0F) {
                        param0.vertex((double)(var22 + 0.0F), (double)(var19 + 0.0F), (double)(var23 + 8.0F))
                            .uv((var22 + 0.0F) * 0.00390625F + var5, (var23 + 8.0F) * 0.00390625F + var6)
                            .color(var13, var14, var15, 0.8F)
                            .normal(0.0F, -1.0F, 0.0F)
                            .endVertex();
                        param0.vertex((double)(var22 + 8.0F), (double)(var19 + 0.0F), (double)(var23 + 8.0F))
                            .uv((var22 + 8.0F) * 0.00390625F + var5, (var23 + 8.0F) * 0.00390625F + var6)
                            .color(var13, var14, var15, 0.8F)
                            .normal(0.0F, -1.0F, 0.0F)
                            .endVertex();
                        param0.vertex((double)(var22 + 8.0F), (double)(var19 + 0.0F), (double)(var23 + 0.0F))
                            .uv((var22 + 8.0F) * 0.00390625F + var5, (var23 + 0.0F) * 0.00390625F + var6)
                            .color(var13, var14, var15, 0.8F)
                            .normal(0.0F, -1.0F, 0.0F)
                            .endVertex();
                        param0.vertex((double)(var22 + 0.0F), (double)(var19 + 0.0F), (double)(var23 + 0.0F))
                            .uv((var22 + 0.0F) * 0.00390625F + var5, (var23 + 0.0F) * 0.00390625F + var6)
                            .color(var13, var14, var15, 0.8F)
                            .normal(0.0F, -1.0F, 0.0F)
                            .endVertex();
                    }

                    if (var19 <= 5.0F) {
                        param0.vertex((double)(var22 + 0.0F), (double)(var19 + 4.0F - 9.765625E-4F), (double)(var23 + 8.0F))
                            .uv((var22 + 0.0F) * 0.00390625F + var5, (var23 + 8.0F) * 0.00390625F + var6)
                            .color(var7, var8, var9, 0.8F)
                            .normal(0.0F, 1.0F, 0.0F)
                            .endVertex();
                        param0.vertex((double)(var22 + 8.0F), (double)(var19 + 4.0F - 9.765625E-4F), (double)(var23 + 8.0F))
                            .uv((var22 + 8.0F) * 0.00390625F + var5, (var23 + 8.0F) * 0.00390625F + var6)
                            .color(var7, var8, var9, 0.8F)
                            .normal(0.0F, 1.0F, 0.0F)
                            .endVertex();
                        param0.vertex((double)(var22 + 8.0F), (double)(var19 + 4.0F - 9.765625E-4F), (double)(var23 + 0.0F))
                            .uv((var22 + 8.0F) * 0.00390625F + var5, (var23 + 0.0F) * 0.00390625F + var6)
                            .color(var7, var8, var9, 0.8F)
                            .normal(0.0F, 1.0F, 0.0F)
                            .endVertex();
                        param0.vertex((double)(var22 + 0.0F), (double)(var19 + 4.0F - 9.765625E-4F), (double)(var23 + 0.0F))
                            .uv((var22 + 0.0F) * 0.00390625F + var5, (var23 + 0.0F) * 0.00390625F + var6)
                            .color(var7, var8, var9, 0.8F)
                            .normal(0.0F, 1.0F, 0.0F)
                            .endVertex();
                    }

                    if (var20 > -1) {
                        for(int var24 = 0; var24 < 8; ++var24) {
                            param0.vertex((double)(var22 + (float)var24 + 0.0F), (double)(var19 + 0.0F), (double)(var23 + 8.0F))
                                .uv((var22 + (float)var24 + 0.5F) * 0.00390625F + var5, (var23 + 8.0F) * 0.00390625F + var6)
                                .color(var10, var11, var12, 0.8F)
                                .normal(-1.0F, 0.0F, 0.0F)
                                .endVertex();
                            param0.vertex((double)(var22 + (float)var24 + 0.0F), (double)(var19 + 4.0F), (double)(var23 + 8.0F))
                                .uv((var22 + (float)var24 + 0.5F) * 0.00390625F + var5, (var23 + 8.0F) * 0.00390625F + var6)
                                .color(var10, var11, var12, 0.8F)
                                .normal(-1.0F, 0.0F, 0.0F)
                                .endVertex();
                            param0.vertex((double)(var22 + (float)var24 + 0.0F), (double)(var19 + 4.0F), (double)(var23 + 0.0F))
                                .uv((var22 + (float)var24 + 0.5F) * 0.00390625F + var5, (var23 + 0.0F) * 0.00390625F + var6)
                                .color(var10, var11, var12, 0.8F)
                                .normal(-1.0F, 0.0F, 0.0F)
                                .endVertex();
                            param0.vertex((double)(var22 + (float)var24 + 0.0F), (double)(var19 + 0.0F), (double)(var23 + 0.0F))
                                .uv((var22 + (float)var24 + 0.5F) * 0.00390625F + var5, (var23 + 0.0F) * 0.00390625F + var6)
                                .color(var10, var11, var12, 0.8F)
                                .normal(-1.0F, 0.0F, 0.0F)
                                .endVertex();
                        }
                    }

                    if (var20 <= 1) {
                        for(int var25 = 0; var25 < 8; ++var25) {
                            param0.vertex((double)(var22 + (float)var25 + 1.0F - 9.765625E-4F), (double)(var19 + 0.0F), (double)(var23 + 8.0F))
                                .uv((var22 + (float)var25 + 0.5F) * 0.00390625F + var5, (var23 + 8.0F) * 0.00390625F + var6)
                                .color(var10, var11, var12, 0.8F)
                                .normal(1.0F, 0.0F, 0.0F)
                                .endVertex();
                            param0.vertex((double)(var22 + (float)var25 + 1.0F - 9.765625E-4F), (double)(var19 + 4.0F), (double)(var23 + 8.0F))
                                .uv((var22 + (float)var25 + 0.5F) * 0.00390625F + var5, (var23 + 8.0F) * 0.00390625F + var6)
                                .color(var10, var11, var12, 0.8F)
                                .normal(1.0F, 0.0F, 0.0F)
                                .endVertex();
                            param0.vertex((double)(var22 + (float)var25 + 1.0F - 9.765625E-4F), (double)(var19 + 4.0F), (double)(var23 + 0.0F))
                                .uv((var22 + (float)var25 + 0.5F) * 0.00390625F + var5, (var23 + 0.0F) * 0.00390625F + var6)
                                .color(var10, var11, var12, 0.8F)
                                .normal(1.0F, 0.0F, 0.0F)
                                .endVertex();
                            param0.vertex((double)(var22 + (float)var25 + 1.0F - 9.765625E-4F), (double)(var19 + 0.0F), (double)(var23 + 0.0F))
                                .uv((var22 + (float)var25 + 0.5F) * 0.00390625F + var5, (var23 + 0.0F) * 0.00390625F + var6)
                                .color(var10, var11, var12, 0.8F)
                                .normal(1.0F, 0.0F, 0.0F)
                                .endVertex();
                        }
                    }

                    if (var21 > -1) {
                        for(int var26 = 0; var26 < 8; ++var26) {
                            param0.vertex((double)(var22 + 0.0F), (double)(var19 + 4.0F), (double)(var23 + (float)var26 + 0.0F))
                                .uv((var22 + 0.0F) * 0.00390625F + var5, (var23 + (float)var26 + 0.5F) * 0.00390625F + var6)
                                .color(var16, var17, var18, 0.8F)
                                .normal(0.0F, 0.0F, -1.0F)
                                .endVertex();
                            param0.vertex((double)(var22 + 8.0F), (double)(var19 + 4.0F), (double)(var23 + (float)var26 + 0.0F))
                                .uv((var22 + 8.0F) * 0.00390625F + var5, (var23 + (float)var26 + 0.5F) * 0.00390625F + var6)
                                .color(var16, var17, var18, 0.8F)
                                .normal(0.0F, 0.0F, -1.0F)
                                .endVertex();
                            param0.vertex((double)(var22 + 8.0F), (double)(var19 + 0.0F), (double)(var23 + (float)var26 + 0.0F))
                                .uv((var22 + 8.0F) * 0.00390625F + var5, (var23 + (float)var26 + 0.5F) * 0.00390625F + var6)
                                .color(var16, var17, var18, 0.8F)
                                .normal(0.0F, 0.0F, -1.0F)
                                .endVertex();
                            param0.vertex((double)(var22 + 0.0F), (double)(var19 + 0.0F), (double)(var23 + (float)var26 + 0.0F))
                                .uv((var22 + 0.0F) * 0.00390625F + var5, (var23 + (float)var26 + 0.5F) * 0.00390625F + var6)
                                .color(var16, var17, var18, 0.8F)
                                .normal(0.0F, 0.0F, -1.0F)
                                .endVertex();
                        }
                    }

                    if (var21 <= 1) {
                        for(int var27 = 0; var27 < 8; ++var27) {
                            param0.vertex((double)(var22 + 0.0F), (double)(var19 + 4.0F), (double)(var23 + (float)var27 + 1.0F - 9.765625E-4F))
                                .uv((var22 + 0.0F) * 0.00390625F + var5, (var23 + (float)var27 + 0.5F) * 0.00390625F + var6)
                                .color(var16, var17, var18, 0.8F)
                                .normal(0.0F, 0.0F, 1.0F)
                                .endVertex();
                            param0.vertex((double)(var22 + 8.0F), (double)(var19 + 4.0F), (double)(var23 + (float)var27 + 1.0F - 9.765625E-4F))
                                .uv((var22 + 8.0F) * 0.00390625F + var5, (var23 + (float)var27 + 0.5F) * 0.00390625F + var6)
                                .color(var16, var17, var18, 0.8F)
                                .normal(0.0F, 0.0F, 1.0F)
                                .endVertex();
                            param0.vertex((double)(var22 + 8.0F), (double)(var19 + 0.0F), (double)(var23 + (float)var27 + 1.0F - 9.765625E-4F))
                                .uv((var22 + 8.0F) * 0.00390625F + var5, (var23 + (float)var27 + 0.5F) * 0.00390625F + var6)
                                .color(var16, var17, var18, 0.8F)
                                .normal(0.0F, 0.0F, 1.0F)
                                .endVertex();
                            param0.vertex((double)(var22 + 0.0F), (double)(var19 + 0.0F), (double)(var23 + (float)var27 + 1.0F - 9.765625E-4F))
                                .uv((var22 + 0.0F) * 0.00390625F + var5, (var23 + (float)var27 + 0.5F) * 0.00390625F + var6)
                                .color(var16, var17, var18, 0.8F)
                                .normal(0.0F, 0.0F, 1.0F)
                                .endVertex();
                        }
                    }
                }
            }
        } else {
            int var28 = 1;
            int var29 = 32;

            for(int var30 = -32; var30 < 32; var30 += 32) {
                for(int var31 = -32; var31 < 32; var31 += 32) {
                    param0.vertex((double)(var30 + 0), (double)var19, (double)(var31 + 32))
                        .uv((float)(var30 + 0) * 0.00390625F + var5, (float)(var31 + 32) * 0.00390625F + var6)
                        .color(var7, var8, var9, 0.8F)
                        .normal(0.0F, -1.0F, 0.0F)
                        .endVertex();
                    param0.vertex((double)(var30 + 32), (double)var19, (double)(var31 + 32))
                        .uv((float)(var30 + 32) * 0.00390625F + var5, (float)(var31 + 32) * 0.00390625F + var6)
                        .color(var7, var8, var9, 0.8F)
                        .normal(0.0F, -1.0F, 0.0F)
                        .endVertex();
                    param0.vertex((double)(var30 + 32), (double)var19, (double)(var31 + 0))
                        .uv((float)(var30 + 32) * 0.00390625F + var5, (float)(var31 + 0) * 0.00390625F + var6)
                        .color(var7, var8, var9, 0.8F)
                        .normal(0.0F, -1.0F, 0.0F)
                        .endVertex();
                    param0.vertex((double)(var30 + 0), (double)var19, (double)(var31 + 0))
                        .uv((float)(var30 + 0) * 0.00390625F + var5, (float)(var31 + 0) * 0.00390625F + var6)
                        .color(var7, var8, var9, 0.8F)
                        .normal(0.0F, -1.0F, 0.0F)
                        .endVertex();
                }
            }
        }

        return param0.end();
    }

    private void compileSections(Camera param0) {
        this.minecraft.getProfiler().push("populate_sections_to_compile");
        LevelLightEngine var0 = this.level.getLightEngine();
        RenderRegionCache var1 = new RenderRegionCache();
        BlockPos var2 = param0.getBlockPosition();
        List<SectionRenderDispatcher.RenderSection> var3 = Lists.newArrayList();

        for(SectionRenderDispatcher.RenderSection var4 : this.visibleSections) {
            SectionPos var5 = SectionPos.of(var4.getOrigin());
            if (var4.isDirty() && var0.lightOnInSection(var5)) {
                boolean var6 = false;
                if (this.minecraft.options.prioritizeChunkUpdates().get() != PrioritizeChunkUpdates.NEARBY) {
                    if (this.minecraft.options.prioritizeChunkUpdates().get() == PrioritizeChunkUpdates.PLAYER_AFFECTED) {
                        var6 = var4.isDirtyFromPlayer();
                    }
                } else {
                    BlockPos var7 = var4.getOrigin().offset(8, 8, 8);
                    var6 = var7.distSqr(var2) < 768.0 || var4.isDirtyFromPlayer();
                }

                if (var6) {
                    this.minecraft.getProfiler().push("build_near_sync");
                    this.sectionRenderDispatcher.rebuildSectionSync(var4, var1);
                    var4.setNotDirty();
                    this.minecraft.getProfiler().pop();
                } else {
                    var3.add(var4);
                }
            }
        }

        this.minecraft.getProfiler().popPush("upload");
        this.sectionRenderDispatcher.uploadAllPendingUploads();
        this.minecraft.getProfiler().popPush("schedule_async_compile");

        for(SectionRenderDispatcher.RenderSection var8 : var3) {
            var8.rebuildSectionAsync(this.sectionRenderDispatcher, var1);
            var8.setNotDirty();
        }

        this.minecraft.getProfiler().pop();
    }

    private void renderWorldBorder(Camera param0) {
        BufferBuilder var0 = Tesselator.getInstance().getBuilder();
        WorldBorder var1 = this.level.getWorldBorder();
        double var2 = (double)(this.minecraft.options.getEffectiveRenderDistance() * 16);
        if (!(param0.getPosition().x < var1.getMaxX() - var2)
            || !(param0.getPosition().x > var1.getMinX() + var2)
            || !(param0.getPosition().z < var1.getMaxZ() - var2)
            || !(param0.getPosition().z > var1.getMinZ() + var2)) {
            double var3 = 1.0 - var1.getDistanceToBorder(param0.getPosition().x, param0.getPosition().z) / var2;
            var3 = Math.pow(var3, 4.0);
            var3 = Mth.clamp(var3, 0.0, 1.0);
            double var4 = param0.getPosition().x;
            double var5 = param0.getPosition().z;
            double var6 = (double)this.minecraft.gameRenderer.getDepthFar();
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
            );
            RenderSystem.setShaderTexture(0, FORCEFIELD_LOCATION);
            RenderSystem.depthMask(Minecraft.useShaderTransparency());
            PoseStack var7 = RenderSystem.getModelViewStack();
            var7.pushPose();
            RenderSystem.applyModelViewMatrix();
            int var8 = var1.getStatus().getColor();
            float var9 = (float)(var8 >> 16 & 0xFF) / 255.0F;
            float var10 = (float)(var8 >> 8 & 0xFF) / 255.0F;
            float var11 = (float)(var8 & 0xFF) / 255.0F;
            RenderSystem.setShaderColor(var9, var10, var11, (float)var3);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.polygonOffset(-3.0F, -3.0F);
            RenderSystem.enablePolygonOffset();
            RenderSystem.disableCull();
            float var12 = (float)(Util.getMillis() % 3000L) / 3000.0F;
            float var13 = (float)(-Mth.frac(param0.getPosition().y * 0.5));
            float var14 = var13 + (float)var6;
            var0.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            double var15 = Math.max((double)Mth.floor(var5 - var2), var1.getMinZ());
            double var16 = Math.min((double)Mth.ceil(var5 + var2), var1.getMaxZ());
            float var17 = (float)(Mth.floor(var15) & 1) * 0.5F;
            if (var4 > var1.getMaxX() - var2) {
                float var18 = var17;

                for(double var19 = var15; var19 < var16; var18 += 0.5F) {
                    double var20 = Math.min(1.0, var16 - var19);
                    float var21 = (float)var20 * 0.5F;
                    var0.vertex(var1.getMaxX() - var4, -var6, var19 - var5).uv(var12 - var18, var12 + var14).endVertex();
                    var0.vertex(var1.getMaxX() - var4, -var6, var19 + var20 - var5).uv(var12 - (var21 + var18), var12 + var14).endVertex();
                    var0.vertex(var1.getMaxX() - var4, var6, var19 + var20 - var5).uv(var12 - (var21 + var18), var12 + var13).endVertex();
                    var0.vertex(var1.getMaxX() - var4, var6, var19 - var5).uv(var12 - var18, var12 + var13).endVertex();
                    ++var19;
                }
            }

            if (var4 < var1.getMinX() + var2) {
                float var22 = var17;

                for(double var23 = var15; var23 < var16; var22 += 0.5F) {
                    double var24 = Math.min(1.0, var16 - var23);
                    float var25 = (float)var24 * 0.5F;
                    var0.vertex(var1.getMinX() - var4, -var6, var23 - var5).uv(var12 + var22, var12 + var14).endVertex();
                    var0.vertex(var1.getMinX() - var4, -var6, var23 + var24 - var5).uv(var12 + var25 + var22, var12 + var14).endVertex();
                    var0.vertex(var1.getMinX() - var4, var6, var23 + var24 - var5).uv(var12 + var25 + var22, var12 + var13).endVertex();
                    var0.vertex(var1.getMinX() - var4, var6, var23 - var5).uv(var12 + var22, var12 + var13).endVertex();
                    ++var23;
                }
            }

            var15 = Math.max((double)Mth.floor(var4 - var2), var1.getMinX());
            var16 = Math.min((double)Mth.ceil(var4 + var2), var1.getMaxX());
            var17 = (float)(Mth.floor(var15) & 1) * 0.5F;
            if (var5 > var1.getMaxZ() - var2) {
                float var26 = var17;

                for(double var27 = var15; var27 < var16; var26 += 0.5F) {
                    double var28 = Math.min(1.0, var16 - var27);
                    float var29 = (float)var28 * 0.5F;
                    var0.vertex(var27 - var4, -var6, var1.getMaxZ() - var5).uv(var12 + var26, var12 + var14).endVertex();
                    var0.vertex(var27 + var28 - var4, -var6, var1.getMaxZ() - var5).uv(var12 + var29 + var26, var12 + var14).endVertex();
                    var0.vertex(var27 + var28 - var4, var6, var1.getMaxZ() - var5).uv(var12 + var29 + var26, var12 + var13).endVertex();
                    var0.vertex(var27 - var4, var6, var1.getMaxZ() - var5).uv(var12 + var26, var12 + var13).endVertex();
                    ++var27;
                }
            }

            if (var5 < var1.getMinZ() + var2) {
                float var30 = var17;

                for(double var31 = var15; var31 < var16; var30 += 0.5F) {
                    double var32 = Math.min(1.0, var16 - var31);
                    float var33 = (float)var32 * 0.5F;
                    var0.vertex(var31 - var4, -var6, var1.getMinZ() - var5).uv(var12 - var30, var12 + var14).endVertex();
                    var0.vertex(var31 + var32 - var4, -var6, var1.getMinZ() - var5).uv(var12 - (var33 + var30), var12 + var14).endVertex();
                    var0.vertex(var31 + var32 - var4, var6, var1.getMinZ() - var5).uv(var12 - (var33 + var30), var12 + var13).endVertex();
                    var0.vertex(var31 - var4, var6, var1.getMinZ() - var5).uv(var12 - var30, var12 + var13).endVertex();
                    ++var31;
                }
            }

            BufferUploader.drawWithShader(var0.end());
            RenderSystem.enableCull();
            RenderSystem.polygonOffset(0.0F, 0.0F);
            RenderSystem.disablePolygonOffset();
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
            var7.popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.depthMask(true);
        }
    }

    private void renderHitOutline(
        PoseStack param0, VertexConsumer param1, Entity param2, double param3, double param4, double param5, BlockPos param6, BlockState param7
    ) {
        renderShape(
            param0,
            param1,
            param7.getShape(this.level, param6, CollisionContext.of(param2)),
            (double)param6.getX() - param3,
            (double)param6.getY() - param4,
            (double)param6.getZ() - param5,
            0.0F,
            0.0F,
            0.0F,
            0.4F
        );
    }

    private static Vec3 mixColor(float param0) {
        float var0 = 5.99999F;
        int var1 = (int)(Mth.clamp(param0, 0.0F, 1.0F) * 5.99999F);
        float var2 = param0 * 5.99999F - (float)var1;

        return switch(var1) {
            case 0 -> new Vec3(1.0, (double)var2, 0.0);
            case 1 -> new Vec3((double)(1.0F - var2), 1.0, 0.0);
            case 2 -> new Vec3(0.0, 1.0, (double)var2);
            case 3 -> new Vec3(0.0, 1.0 - (double)var2, 1.0);
            case 4 -> new Vec3((double)var2, 0.0, 1.0);
            case 5 -> new Vec3(1.0, 0.0, 1.0 - (double)var2);
            default -> throw new IllegalStateException("Unexpected value: " + var1);
        };
    }

    private static Vec3 shiftHue(float param0, float param1, float param2, float param3) {
        Vec3 var0 = mixColor(param3).scale((double)param0);
        Vec3 var1 = mixColor((param3 + 0.33333334F) % 1.0F).scale((double)param1);
        Vec3 var2 = mixColor((param3 + 0.6666667F) % 1.0F).scale((double)param2);
        Vec3 var3 = var0.add(var1).add(var2);
        double var4 = Math.max(Math.max(1.0, var3.x), Math.max(var3.y, var3.z));
        return new Vec3(var3.x / var4, var3.y / var4, var3.z / var4);
    }

    public static void renderVoxelShape(
        PoseStack param0,
        VertexConsumer param1,
        VoxelShape param2,
        double param3,
        double param4,
        double param5,
        float param6,
        float param7,
        float param8,
        float param9,
        boolean param10
    ) {
        List<AABB> var0 = param2.toAabbs();
        if (!var0.isEmpty()) {
            int var1 = param10 ? var0.size() : var0.size() * 8;
            renderShape(param0, param1, Shapes.create(var0.get(0)), param3, param4, param5, param6, param7, param8, param9);

            for(int var2 = 1; var2 < var0.size(); ++var2) {
                AABB var3 = var0.get(var2);
                float var4 = (float)var2 / (float)var1;
                Vec3 var5 = shiftHue(param6, param7, param8, var4);
                renderShape(param0, param1, Shapes.create(var3), param3, param4, param5, (float)var5.x, (float)var5.y, (float)var5.z, param9);
            }

        }
    }

    private static void renderShape(
        PoseStack param0,
        VertexConsumer param1,
        VoxelShape param2,
        double param3,
        double param4,
        double param5,
        float param6,
        float param7,
        float param8,
        float param9
    ) {
        PoseStack.Pose var0 = param0.last();
        param2.forAllEdges(
            (param9x, param10, param11, param12, param13, param14) -> {
                float var0x = (float)(param12 - param9x);
                float var1x = (float)(param13 - param10);
                float var2x = (float)(param14 - param11);
                float var3x = Mth.sqrt(var0x * var0x + var1x * var1x + var2x * var2x);
                var0x /= var3x;
                var1x /= var3x;
                var2x /= var3x;
                param1.vertex(var0.pose(), (float)(param9x + param3), (float)(param10 + param4), (float)(param11 + param5))
                    .color(param6, param7, param8, param9)
                    .normal(var0.normal(), var0x, var1x, var2x)
                    .endVertex();
                param1.vertex(var0.pose(), (float)(param12 + param3), (float)(param13 + param4), (float)(param14 + param5))
                    .color(param6, param7, param8, param9)
                    .normal(var0.normal(), var0x, var1x, var2x)
                    .endVertex();
            }
        );
    }

    public static void renderLineBox(
        VertexConsumer param0,
        double param1,
        double param2,
        double param3,
        double param4,
        double param5,
        double param6,
        float param7,
        float param8,
        float param9,
        float param10
    ) {
        renderLineBox(new PoseStack(), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param7, param8, param9);
    }

    public static void renderLineBox(PoseStack param0, VertexConsumer param1, AABB param2, float param3, float param4, float param5, float param6) {
        renderLineBox(
            param0,
            param1,
            param2.minX,
            param2.minY,
            param2.minZ,
            param2.maxX,
            param2.maxY,
            param2.maxZ,
            param3,
            param4,
            param5,
            param6,
            param3,
            param4,
            param5
        );
    }

    public static void renderLineBox(
        PoseStack param0,
        VertexConsumer param1,
        double param2,
        double param3,
        double param4,
        double param5,
        double param6,
        double param7,
        float param8,
        float param9,
        float param10,
        float param11
    ) {
        renderLineBox(param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11, param8, param9, param10);
    }

    public static void renderLineBox(
        PoseStack param0,
        VertexConsumer param1,
        double param2,
        double param3,
        double param4,
        double param5,
        double param6,
        double param7,
        float param8,
        float param9,
        float param10,
        float param11,
        float param12,
        float param13,
        float param14
    ) {
        Matrix4f var0 = param0.last().pose();
        Matrix3f var1 = param0.last().normal();
        float var2 = (float)param2;
        float var3 = (float)param3;
        float var4 = (float)param4;
        float var5 = (float)param5;
        float var6 = (float)param6;
        float var7 = (float)param7;
        param1.vertex(var0, var2, var3, var4).color(param8, param13, param14, param11).normal(var1, 1.0F, 0.0F, 0.0F).endVertex();
        param1.vertex(var0, var5, var3, var4).color(param8, param13, param14, param11).normal(var1, 1.0F, 0.0F, 0.0F).endVertex();
        param1.vertex(var0, var2, var3, var4).color(param12, param9, param14, param11).normal(var1, 0.0F, 1.0F, 0.0F).endVertex();
        param1.vertex(var0, var2, var6, var4).color(param12, param9, param14, param11).normal(var1, 0.0F, 1.0F, 0.0F).endVertex();
        param1.vertex(var0, var2, var3, var4).color(param12, param13, param10, param11).normal(var1, 0.0F, 0.0F, 1.0F).endVertex();
        param1.vertex(var0, var2, var3, var7).color(param12, param13, param10, param11).normal(var1, 0.0F, 0.0F, 1.0F).endVertex();
        param1.vertex(var0, var5, var3, var4).color(param8, param9, param10, param11).normal(var1, 0.0F, 1.0F, 0.0F).endVertex();
        param1.vertex(var0, var5, var6, var4).color(param8, param9, param10, param11).normal(var1, 0.0F, 1.0F, 0.0F).endVertex();
        param1.vertex(var0, var5, var6, var4).color(param8, param9, param10, param11).normal(var1, -1.0F, 0.0F, 0.0F).endVertex();
        param1.vertex(var0, var2, var6, var4).color(param8, param9, param10, param11).normal(var1, -1.0F, 0.0F, 0.0F).endVertex();
        param1.vertex(var0, var2, var6, var4).color(param8, param9, param10, param11).normal(var1, 0.0F, 0.0F, 1.0F).endVertex();
        param1.vertex(var0, var2, var6, var7).color(param8, param9, param10, param11).normal(var1, 0.0F, 0.0F, 1.0F).endVertex();
        param1.vertex(var0, var2, var6, var7).color(param8, param9, param10, param11).normal(var1, 0.0F, -1.0F, 0.0F).endVertex();
        param1.vertex(var0, var2, var3, var7).color(param8, param9, param10, param11).normal(var1, 0.0F, -1.0F, 0.0F).endVertex();
        param1.vertex(var0, var2, var3, var7).color(param8, param9, param10, param11).normal(var1, 1.0F, 0.0F, 0.0F).endVertex();
        param1.vertex(var0, var5, var3, var7).color(param8, param9, param10, param11).normal(var1, 1.0F, 0.0F, 0.0F).endVertex();
        param1.vertex(var0, var5, var3, var7).color(param8, param9, param10, param11).normal(var1, 0.0F, 0.0F, -1.0F).endVertex();
        param1.vertex(var0, var5, var3, var4).color(param8, param9, param10, param11).normal(var1, 0.0F, 0.0F, -1.0F).endVertex();
        param1.vertex(var0, var2, var6, var7).color(param8, param9, param10, param11).normal(var1, 1.0F, 0.0F, 0.0F).endVertex();
        param1.vertex(var0, var5, var6, var7).color(param8, param9, param10, param11).normal(var1, 1.0F, 0.0F, 0.0F).endVertex();
        param1.vertex(var0, var5, var3, var7).color(param8, param9, param10, param11).normal(var1, 0.0F, 1.0F, 0.0F).endVertex();
        param1.vertex(var0, var5, var6, var7).color(param8, param9, param10, param11).normal(var1, 0.0F, 1.0F, 0.0F).endVertex();
        param1.vertex(var0, var5, var6, var4).color(param8, param9, param10, param11).normal(var1, 0.0F, 0.0F, 1.0F).endVertex();
        param1.vertex(var0, var5, var6, var7).color(param8, param9, param10, param11).normal(var1, 0.0F, 0.0F, 1.0F).endVertex();
    }

    public static void addChainedFilledBoxVertices(
        PoseStack param0,
        VertexConsumer param1,
        double param2,
        double param3,
        double param4,
        double param5,
        double param6,
        double param7,
        float param8,
        float param9,
        float param10,
        float param11
    ) {
        addChainedFilledBoxVertices(
            param0, param1, (float)param2, (float)param3, (float)param4, (float)param5, (float)param6, (float)param7, param8, param9, param10, param11
        );
    }

    public static void addChainedFilledBoxVertices(
        PoseStack param0,
        VertexConsumer param1,
        float param2,
        float param3,
        float param4,
        float param5,
        float param6,
        float param7,
        float param8,
        float param9,
        float param10,
        float param11
    ) {
        Matrix4f var0 = param0.last().pose();
        param1.vertex(var0, param2, param3, param4).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, param2, param3, param4).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, param2, param3, param4).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, param2, param3, param7).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, param2, param6, param4).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, param2, param6, param7).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, param2, param6, param7).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, param2, param3, param7).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, param5, param6, param7).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, param5, param3, param7).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, param5, param3, param7).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, param5, param3, param4).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, param5, param6, param7).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, param5, param6, param4).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, param5, param6, param4).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, param5, param3, param4).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, param2, param6, param4).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, param2, param3, param4).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, param2, param3, param4).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, param5, param3, param4).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, param2, param3, param7).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, param5, param3, param7).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, param5, param3, param7).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, param2, param6, param4).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, param2, param6, param4).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, param2, param6, param7).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, param5, param6, param4).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, param5, param6, param7).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, param5, param6, param7).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, param5, param6, param7).color(param8, param9, param10, param11).endVertex();
    }

    public void blockChanged(BlockGetter param0, BlockPos param1, BlockState param2, BlockState param3, int param4) {
        this.setBlockDirty(param1, (param4 & 8) != 0);
    }

    private void setBlockDirty(BlockPos param0, boolean param1) {
        for(int var0 = param0.getZ() - 1; var0 <= param0.getZ() + 1; ++var0) {
            for(int var1 = param0.getX() - 1; var1 <= param0.getX() + 1; ++var1) {
                for(int var2 = param0.getY() - 1; var2 <= param0.getY() + 1; ++var2) {
                    this.setSectionDirty(
                        SectionPos.blockToSectionCoord(var1), SectionPos.blockToSectionCoord(var2), SectionPos.blockToSectionCoord(var0), param1
                    );
                }
            }
        }

    }

    public void setBlocksDirty(int param0, int param1, int param2, int param3, int param4, int param5) {
        for(int var0 = param2 - 1; var0 <= param5 + 1; ++var0) {
            for(int var1 = param0 - 1; var1 <= param3 + 1; ++var1) {
                for(int var2 = param1 - 1; var2 <= param4 + 1; ++var2) {
                    this.setSectionDirty(SectionPos.blockToSectionCoord(var1), SectionPos.blockToSectionCoord(var2), SectionPos.blockToSectionCoord(var0));
                }
            }
        }

    }

    public void setBlockDirty(BlockPos param0, BlockState param1, BlockState param2) {
        if (this.minecraft.getModelManager().requiresRender(param1, param2)) {
            this.setBlocksDirty(param0.getX(), param0.getY(), param0.getZ(), param0.getX(), param0.getY(), param0.getZ());
        }

    }

    public void setSectionDirtyWithNeighbors(int param0, int param1, int param2) {
        for(int var0 = param2 - 1; var0 <= param2 + 1; ++var0) {
            for(int var1 = param0 - 1; var1 <= param0 + 1; ++var1) {
                for(int var2 = param1 - 1; var2 <= param1 + 1; ++var2) {
                    this.setSectionDirty(var1, var2, var0);
                }
            }
        }

    }

    public void setSectionDirty(int param0, int param1, int param2) {
        this.setSectionDirty(param0, param1, param2, false);
    }

    private void setSectionDirty(int param0, int param1, int param2, boolean param3) {
        this.viewArea.setDirty(param0, param1, param2, param3);
    }

    public void playStreamingMusic(@Nullable SoundEvent param0, BlockPos param1) {
        SoundInstance var0 = this.playingRecords.get(param1);
        if (var0 != null) {
            this.minecraft.getSoundManager().stop(var0);
            this.playingRecords.remove(param1);
        }

        if (param0 != null) {
            RecordItem var1 = RecordItem.getBySound(param0);
            if (var1 != null) {
                this.minecraft.gui.setNowPlaying(var1.getDisplayName());
            }

            SoundInstance var5 = SimpleSoundInstance.forRecord(param0, Vec3.atCenterOf(param1));
            this.playingRecords.put(param1, var5);
            this.minecraft.getSoundManager().play(var5);
        }

        this.notifyNearbyEntities(this.level, param1, param0 != null);
    }

    private void notifyNearbyEntities(Level param0, BlockPos param1, boolean param2) {
        for(LivingEntity var1 : param0.getEntitiesOfClass(LivingEntity.class, new AABB(param1).inflate(3.0))) {
            var1.setRecordPlayingNearby(param1, param2);
        }

    }

    public void addParticle(ParticleOptions param0, boolean param1, double param2, double param3, double param4, double param5, double param6, double param7) {
        this.addParticle(param0, param1, false, param2, param3, param4, param5, param6, param7);
    }

    public void addParticle(
        ParticleOptions param0, boolean param1, boolean param2, double param3, double param4, double param5, double param6, double param7, double param8
    ) {
        try {
            this.addParticleInternal(param0, param1, param2, param3, param4, param5, param6, param7, param8);
        } catch (Throwable var19) {
            CrashReport var1 = CrashReport.forThrowable(var19, "Exception while adding particle");
            CrashReportCategory var2 = var1.addCategory("Particle being added");
            var2.setDetail("ID", BuiltInRegistries.PARTICLE_TYPE.getKey(param0.getType()));
            var2.setDetail("Parameters", param0.writeToString());
            var2.setDetail("Position", () -> CrashReportCategory.formatLocation(this.level, param3, param4, param5));
            throw new ReportedException(var1);
        }
    }

    private <T extends ParticleOptions> void addParticle(T param0, double param1, double param2, double param3, double param4, double param5, double param6) {
        this.addParticle(param0, param0.getType().getOverrideLimiter(), param1, param2, param3, param4, param5, param6);
    }

    @Nullable
    private Particle addParticleInternal(
        ParticleOptions param0, boolean param1, double param2, double param3, double param4, double param5, double param6, double param7
    ) {
        return this.addParticleInternal(param0, param1, false, param2, param3, param4, param5, param6, param7);
    }

    @Nullable
    private Particle addParticleInternal(
        ParticleOptions param0, boolean param1, boolean param2, double param3, double param4, double param5, double param6, double param7, double param8
    ) {
        Camera var0 = this.minecraft.gameRenderer.getMainCamera();
        ParticleStatus var1 = this.calculateParticleLevel(param2);
        if (param1) {
            return this.minecraft.particleEngine.createParticle(param0, param3, param4, param5, param6, param7, param8);
        } else if (var0.getPosition().distanceToSqr(param3, param4, param5) > 1024.0) {
            return null;
        } else {
            return var1 == ParticleStatus.MINIMAL ? null : this.minecraft.particleEngine.createParticle(param0, param3, param4, param5, param6, param7, param8);
        }
    }

    private ParticleStatus calculateParticleLevel(boolean param0) {
        ParticleStatus var0 = this.minecraft.options.particles().get();
        if (param0 && var0 == ParticleStatus.MINIMAL && this.level.random.nextInt(10) == 0) {
            var0 = ParticleStatus.DECREASED;
        }

        if (var0 == ParticleStatus.DECREASED && this.level.random.nextInt(3) == 0) {
            var0 = ParticleStatus.MINIMAL;
        }

        return var0;
    }

    public void clear() {
    }

    public void globalLevelEvent(int param0, BlockPos param1, int param2) {
        switch(param0) {
            case 1023:
            case 1028:
            case 1038:
                Camera var0 = this.minecraft.gameRenderer.getMainCamera();
                if (var0.isInitialized()) {
                    double var1 = (double)param1.getX() - var0.getPosition().x;
                    double var2 = (double)param1.getY() - var0.getPosition().y;
                    double var3 = (double)param1.getZ() - var0.getPosition().z;
                    double var4 = Math.sqrt(var1 * var1 + var2 * var2 + var3 * var3);
                    double var5 = var0.getPosition().x;
                    double var6 = var0.getPosition().y;
                    double var7 = var0.getPosition().z;
                    if (var4 > 0.0) {
                        var5 += var1 / var4 * 2.0;
                        var6 += var2 / var4 * 2.0;
                        var7 += var3 / var4 * 2.0;
                    }

                    if (param0 == 1023) {
                        this.level.playLocalSound(var5, var6, var7, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.0F, 1.0F, false);
                    } else if (param0 == 1038) {
                        this.level.playLocalSound(var5, var6, var7, SoundEvents.END_PORTAL_SPAWN, SoundSource.HOSTILE, 1.0F, 1.0F, false);
                    } else {
                        this.level.playLocalSound(var5, var6, var7, SoundEvents.ENDER_DRAGON_DEATH, SoundSource.HOSTILE, 5.0F, 1.0F, false);
                    }
                }
        }
    }

    public void levelEvent(int param0, BlockPos param1, int param2) {
        RandomSource var0 = this.level.random;
        switch(param0) {
            case 1000:
                this.level.playLocalSound(param1, SoundEvents.DISPENSER_DISPENSE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
                break;
            case 1001:
                this.level.playLocalSound(param1, SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 1.0F, 1.2F, false);
                break;
            case 1002:
                this.level.playLocalSound(param1, SoundEvents.DISPENSER_LAUNCH, SoundSource.BLOCKS, 1.0F, 1.2F, false);
                break;
            case 1003:
                this.level.playLocalSound(param1, SoundEvents.ENDER_EYE_LAUNCH, SoundSource.NEUTRAL, 1.0F, 1.2F, false);
                break;
            case 1004:
                this.level.playLocalSound(param1, SoundEvents.FIREWORK_ROCKET_SHOOT, SoundSource.NEUTRAL, 1.0F, 1.2F, false);
                break;
            case 1009:
                if (param2 == 0) {
                    this.level
                        .playLocalSound(
                            param1, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (var0.nextFloat() - var0.nextFloat()) * 0.8F, false
                        );
                } else if (param2 == 1) {
                    this.level
                        .playLocalSound(
                            param1, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.7F, 1.6F + (var0.nextFloat() - var0.nextFloat()) * 0.4F, false
                        );
                }
                break;
            case 1010:
                Item var73 = Item.byId(param2);
                if (var73 instanceof RecordItem var84) {
                    this.playStreamingMusic(var84.getSound(), param1);
                }
                break;
            case 1011:
                this.playStreamingMusic(null, param1);
                break;
            case 1015:
                this.level
                    .playLocalSound(param1, SoundEvents.GHAST_WARN, SoundSource.HOSTILE, 10.0F, (var0.nextFloat() - var0.nextFloat()) * 0.2F + 1.0F, false);
                break;
            case 1016:
                this.level
                    .playLocalSound(param1, SoundEvents.GHAST_SHOOT, SoundSource.HOSTILE, 10.0F, (var0.nextFloat() - var0.nextFloat()) * 0.2F + 1.0F, false);
                break;
            case 1017:
                this.level
                    .playLocalSound(
                        param1, SoundEvents.ENDER_DRAGON_SHOOT, SoundSource.HOSTILE, 10.0F, (var0.nextFloat() - var0.nextFloat()) * 0.2F + 1.0F, false
                    );
                break;
            case 1018:
                this.level
                    .playLocalSound(param1, SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 2.0F, (var0.nextFloat() - var0.nextFloat()) * 0.2F + 1.0F, false);
                break;
            case 1019:
                this.level
                    .playLocalSound(
                        param1, SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, SoundSource.HOSTILE, 2.0F, (var0.nextFloat() - var0.nextFloat()) * 0.2F + 1.0F, false
                    );
                break;
            case 1020:
                this.level
                    .playLocalSound(
                        param1, SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, SoundSource.HOSTILE, 2.0F, (var0.nextFloat() - var0.nextFloat()) * 0.2F + 1.0F, false
                    );
                break;
            case 1021:
                this.level
                    .playLocalSound(
                        param1, SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.HOSTILE, 2.0F, (var0.nextFloat() - var0.nextFloat()) * 0.2F + 1.0F, false
                    );
                break;
            case 1022:
                this.level
                    .playLocalSound(
                        param1, SoundEvents.WITHER_BREAK_BLOCK, SoundSource.HOSTILE, 2.0F, (var0.nextFloat() - var0.nextFloat()) * 0.2F + 1.0F, false
                    );
                break;
            case 1024:
                this.level
                    .playLocalSound(param1, SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 2.0F, (var0.nextFloat() - var0.nextFloat()) * 0.2F + 1.0F, false);
                break;
            case 1025:
                this.level
                    .playLocalSound(param1, SoundEvents.BAT_TAKEOFF, SoundSource.NEUTRAL, 0.05F, (var0.nextFloat() - var0.nextFloat()) * 0.2F + 1.0F, false);
                break;
            case 1026:
                this.level
                    .playLocalSound(param1, SoundEvents.ZOMBIE_INFECT, SoundSource.HOSTILE, 2.0F, (var0.nextFloat() - var0.nextFloat()) * 0.2F + 1.0F, false);
                break;
            case 1027:
                this.level
                    .playLocalSound(
                        param1, SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.HOSTILE, 2.0F, (var0.nextFloat() - var0.nextFloat()) * 0.2F + 1.0F, false
                    );
                break;
            case 1029:
                this.level.playLocalSound(param1, SoundEvents.ANVIL_DESTROY, SoundSource.BLOCKS, 1.0F, var0.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1030:
                this.level.playLocalSound(param1, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F, var0.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1031:
                this.level.playLocalSound(param1, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.3F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1032:
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forLocalAmbience(SoundEvents.PORTAL_TRAVEL, var0.nextFloat() * 0.4F + 0.8F, 0.25F));
                break;
            case 1033:
                this.level.playLocalSound(param1, SoundEvents.CHORUS_FLOWER_GROW, SoundSource.BLOCKS, 1.0F, 1.0F, false);
                break;
            case 1034:
                this.level.playLocalSound(param1, SoundEvents.CHORUS_FLOWER_DEATH, SoundSource.BLOCKS, 1.0F, 1.0F, false);
                break;
            case 1035:
                this.level.playLocalSound(param1, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 1.0F, false);
                break;
            case 1039:
                this.level.playLocalSound(param1, SoundEvents.PHANTOM_BITE, SoundSource.HOSTILE, 0.3F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1040:
                this.level
                    .playLocalSound(
                        param1, SoundEvents.ZOMBIE_CONVERTED_TO_DROWNED, SoundSource.HOSTILE, 2.0F, (var0.nextFloat() - var0.nextFloat()) * 0.2F + 1.0F, false
                    );
                break;
            case 1041:
                this.level
                    .playLocalSound(
                        param1, SoundEvents.HUSK_CONVERTED_TO_ZOMBIE, SoundSource.HOSTILE, 2.0F, (var0.nextFloat() - var0.nextFloat()) * 0.2F + 1.0F, false
                    );
                break;
            case 1042:
                this.level.playLocalSound(param1, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1043:
                this.level.playLocalSound(param1, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1044:
                this.level.playLocalSound(param1, SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1045:
                this.level
                    .playLocalSound(param1, SoundEvents.POINTED_DRIPSTONE_LAND, SoundSource.BLOCKS, 2.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1046:
                this.level
                    .playLocalSound(
                        param1,
                        SoundEvents.POINTED_DRIPSTONE_DRIP_LAVA_INTO_CAULDRON,
                        SoundSource.BLOCKS,
                        2.0F,
                        this.level.random.nextFloat() * 0.1F + 0.9F,
                        false
                    );
                break;
            case 1047:
                this.level
                    .playLocalSound(
                        param1,
                        SoundEvents.POINTED_DRIPSTONE_DRIP_WATER_INTO_CAULDRON,
                        SoundSource.BLOCKS,
                        2.0F,
                        this.level.random.nextFloat() * 0.1F + 0.9F,
                        false
                    );
                break;
            case 1048:
                this.level
                    .playLocalSound(
                        param1, SoundEvents.SKELETON_CONVERTED_TO_STRAY, SoundSource.HOSTILE, 2.0F, (var0.nextFloat() - var0.nextFloat()) * 0.2F + 1.0F, false
                    );
                break;
            case 1500:
                ComposterBlock.handleFill(this.level, param1, param2 > 0);
                break;
            case 1501:
                this.level
                    .playLocalSound(param1, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (var0.nextFloat() - var0.nextFloat()) * 0.8F, false);

                for(int var67 = 0; var67 < 8; ++var67) {
                    this.level
                        .addParticle(
                            ParticleTypes.LARGE_SMOKE,
                            (double)param1.getX() + var0.nextDouble(),
                            (double)param1.getY() + 1.2,
                            (double)param1.getZ() + var0.nextDouble(),
                            0.0,
                            0.0,
                            0.0
                        );
                }
                break;
            case 1502:
                this.level
                    .playLocalSound(
                        param1, SoundEvents.REDSTONE_TORCH_BURNOUT, SoundSource.BLOCKS, 0.5F, 2.6F + (var0.nextFloat() - var0.nextFloat()) * 0.8F, false
                    );

                for(int var68 = 0; var68 < 5; ++var68) {
                    double var69 = (double)param1.getX() + var0.nextDouble() * 0.6 + 0.2;
                    double var70 = (double)param1.getY() + var0.nextDouble() * 0.6 + 0.2;
                    double var71 = (double)param1.getZ() + var0.nextDouble() * 0.6 + 0.2;
                    this.level.addParticle(ParticleTypes.SMOKE, var69, var70, var71, 0.0, 0.0, 0.0);
                }
                break;
            case 1503:
                this.level.playLocalSound(param1, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 1.0F, 1.0F, false);

                for(int var72 = 0; var72 < 16; ++var72) {
                    double var73 = (double)param1.getX() + (5.0 + var0.nextDouble() * 6.0) / 16.0;
                    double var74 = (double)param1.getY() + 0.8125;
                    double var75 = (double)param1.getZ() + (5.0 + var0.nextDouble() * 6.0) / 16.0;
                    this.level.addParticle(ParticleTypes.SMOKE, var73, var74, var75, 0.0, 0.0, 0.0);
                }
                break;
            case 1504:
                PointedDripstoneBlock.spawnDripParticle(this.level, param1, this.level.getBlockState(param1));
                break;
            case 1505:
                BoneMealItem.addGrowthParticles(this.level, param1, param2);
                this.level.playLocalSound(param1, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
                break;
            case 2000:
                Direction var1 = Direction.from3DDataValue(param2);
                int var2 = var1.getStepX();
                int var3 = var1.getStepY();
                int var4 = var1.getStepZ();
                double var5 = (double)param1.getX() + (double)var2 * 0.6 + 0.5;
                double var6 = (double)param1.getY() + (double)var3 * 0.6 + 0.5;
                double var7 = (double)param1.getZ() + (double)var4 * 0.6 + 0.5;

                for(int var8 = 0; var8 < 10; ++var8) {
                    double var9 = var0.nextDouble() * 0.2 + 0.01;
                    double var10 = var5 + (double)var2 * 0.01 + (var0.nextDouble() - 0.5) * (double)var4 * 0.5;
                    double var11 = var6 + (double)var3 * 0.01 + (var0.nextDouble() - 0.5) * (double)var3 * 0.5;
                    double var12 = var7 + (double)var4 * 0.01 + (var0.nextDouble() - 0.5) * (double)var2 * 0.5;
                    double var13 = (double)var2 * var9 + var0.nextGaussian() * 0.01;
                    double var14 = (double)var3 * var9 + var0.nextGaussian() * 0.01;
                    double var15 = (double)var4 * var9 + var0.nextGaussian() * 0.01;
                    this.addParticle(ParticleTypes.SMOKE, var10, var11, var12, var13, var14, var15);
                }
                break;
            case 2001:
                BlockState var35 = Block.stateById(param2);
                if (!var35.isAir()) {
                    SoundType var36 = var35.getSoundType();
                    this.level
                        .playLocalSound(param1, var36.getBreakSound(), SoundSource.BLOCKS, (var36.getVolume() + 1.0F) / 2.0F, var36.getPitch() * 0.8F, false);
                }

                this.level.addDestroyBlockEffect(param1, var35);
                break;
            case 2002:
            case 2007:
                Vec3 var21 = Vec3.atBottomCenterOf(param1);

                for(int var22 = 0; var22 < 8; ++var22) {
                    this.addParticle(
                        new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.SPLASH_POTION)),
                        var21.x,
                        var21.y,
                        var21.z,
                        var0.nextGaussian() * 0.15,
                        var0.nextDouble() * 0.2,
                        var0.nextGaussian() * 0.15
                    );
                }

                float var23 = (float)(param2 >> 16 & 0xFF) / 255.0F;
                float var24 = (float)(param2 >> 8 & 0xFF) / 255.0F;
                float var25 = (float)(param2 >> 0 & 0xFF) / 255.0F;
                ParticleOptions var26 = param0 == 2007 ? ParticleTypes.INSTANT_EFFECT : ParticleTypes.EFFECT;

                for(int var27 = 0; var27 < 100; ++var27) {
                    double var28 = var0.nextDouble() * 4.0;
                    double var29 = var0.nextDouble() * Math.PI * 2.0;
                    double var30 = Math.cos(var29) * var28;
                    double var31 = 0.01 + var0.nextDouble() * 0.5;
                    double var32 = Math.sin(var29) * var28;
                    Particle var33 = this.addParticleInternal(
                        var26, var26.getType().getOverrideLimiter(), var21.x + var30 * 0.1, var21.y + 0.3, var21.z + var32 * 0.1, var30, var31, var32
                    );
                    if (var33 != null) {
                        float var34 = 0.75F + var0.nextFloat() * 0.25F;
                        var33.setColor(var23 * var34, var24 * var34, var25 * var34);
                        var33.setPower((float)var28);
                    }
                }

                this.level.playLocalSound(param1, SoundEvents.SPLASH_POTION_BREAK, SoundSource.NEUTRAL, 1.0F, var0.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 2003:
                double var16 = (double)param1.getX() + 0.5;
                double var17 = (double)param1.getY();
                double var18 = (double)param1.getZ() + 0.5;

                for(int var19 = 0; var19 < 8; ++var19) {
                    this.addParticle(
                        new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.ENDER_EYE)),
                        var16,
                        var17,
                        var18,
                        var0.nextGaussian() * 0.15,
                        var0.nextDouble() * 0.2,
                        var0.nextGaussian() * 0.15
                    );
                }

                for(double var20 = 0.0; var20 < Math.PI * 2; var20 += Math.PI / 20) {
                    this.addParticle(
                        ParticleTypes.PORTAL,
                        var16 + Math.cos(var20) * 5.0,
                        var17 - 0.4,
                        var18 + Math.sin(var20) * 5.0,
                        Math.cos(var20) * -5.0,
                        0.0,
                        Math.sin(var20) * -5.0
                    );
                    this.addParticle(
                        ParticleTypes.PORTAL,
                        var16 + Math.cos(var20) * 5.0,
                        var17 - 0.4,
                        var18 + Math.sin(var20) * 5.0,
                        Math.cos(var20) * -7.0,
                        0.0,
                        Math.sin(var20) * -7.0
                    );
                }
                break;
            case 2004:
                for(int var39 = 0; var39 < 20; ++var39) {
                    double var40 = (double)param1.getX() + 0.5 + (var0.nextDouble() - 0.5) * 2.0;
                    double var41 = (double)param1.getY() + 0.5 + (var0.nextDouble() - 0.5) * 2.0;
                    double var42 = (double)param1.getZ() + 0.5 + (var0.nextDouble() - 0.5) * 2.0;
                    this.level.addParticle(ParticleTypes.SMOKE, var40, var41, var42, 0.0, 0.0, 0.0);
                    this.level.addParticle(ParticleTypes.FLAME, var40, var41, var42, 0.0, 0.0, 0.0);
                }
                break;
            case 2005:
                BoneMealItem.addGrowthParticles(this.level, param1, param2);
                break;
            case 2006:
                for(int var76 = 0; var76 < 200; ++var76) {
                    float var77 = var0.nextFloat() * 4.0F;
                    float var78 = var0.nextFloat() * (float) (Math.PI * 2);
                    double var79 = (double)(Mth.cos(var78) * var77);
                    double var80 = 0.01 + var0.nextDouble() * 0.5;
                    double var81 = (double)(Mth.sin(var78) * var77);
                    Particle var82 = this.addParticleInternal(
                        ParticleTypes.DRAGON_BREATH,
                        false,
                        (double)param1.getX() + var79 * 0.1,
                        (double)param1.getY() + 0.3,
                        (double)param1.getZ() + var81 * 0.1,
                        var79,
                        var80,
                        var81
                    );
                    if (var82 != null) {
                        var82.setPower(var77);
                    }
                }

                if (param2 == 1) {
                    this.level.playLocalSound(param1, SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.HOSTILE, 1.0F, var0.nextFloat() * 0.1F + 0.9F, false);
                }
                break;
            case 2008:
                this.level
                    .addParticle(ParticleTypes.EXPLOSION, (double)param1.getX() + 0.5, (double)param1.getY() + 0.5, (double)param1.getZ() + 0.5, 0.0, 0.0, 0.0);
                break;
            case 2009:
                for(int var83 = 0; var83 < 8; ++var83) {
                    this.level
                        .addParticle(
                            ParticleTypes.CLOUD,
                            (double)param1.getX() + var0.nextDouble(),
                            (double)param1.getY() + 1.2,
                            (double)param1.getZ() + var0.nextDouble(),
                            0.0,
                            0.0,
                            0.0
                        );
                }
                break;
            case 3000:
                this.level
                    .addParticle(
                        ParticleTypes.EXPLOSION_EMITTER,
                        true,
                        (double)param1.getX() + 0.5,
                        (double)param1.getY() + 0.5,
                        (double)param1.getZ() + 0.5,
                        0.0,
                        0.0,
                        0.0
                    );
                this.level
                    .playLocalSound(
                        param1,
                        SoundEvents.END_GATEWAY_SPAWN,
                        SoundSource.BLOCKS,
                        10.0F,
                        (1.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 0.7F,
                        false
                    );
                break;
            case 3001:
                this.level
                    .playLocalSound(param1, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 64.0F, 0.8F + this.level.random.nextFloat() * 0.3F, false);
                break;
            case 3002:
                if (param2 >= 0 && param2 < Direction.Axis.VALUES.length) {
                    ParticleUtils.spawnParticlesAlongAxis(
                        Direction.Axis.VALUES[param2], this.level, param1, 0.125, ParticleTypes.ELECTRIC_SPARK, UniformInt.of(10, 19)
                    );
                } else {
                    ParticleUtils.spawnParticlesOnBlockFaces(this.level, param1, ParticleTypes.ELECTRIC_SPARK, UniformInt.of(3, 5));
                }
                break;
            case 3003:
                ParticleUtils.spawnParticlesOnBlockFaces(this.level, param1, ParticleTypes.WAX_ON, UniformInt.of(3, 5));
                this.level.playLocalSound(param1, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0F, 1.0F, false);
                break;
            case 3004:
                ParticleUtils.spawnParticlesOnBlockFaces(this.level, param1, ParticleTypes.WAX_OFF, UniformInt.of(3, 5));
                break;
            case 3005:
                ParticleUtils.spawnParticlesOnBlockFaces(this.level, param1, ParticleTypes.SCRAPE, UniformInt.of(3, 5));
                break;
            case 3006:
                int var43 = param2 >> 6;
                if (var43 > 0) {
                    if (var0.nextFloat() < 0.3F + (float)var43 * 0.1F) {
                        float var44 = 0.15F + 0.02F * (float)var43 * (float)var43 * var0.nextFloat();
                        float var45 = 0.4F + 0.3F * (float)var43 * var0.nextFloat();
                        this.level.playLocalSound(param1, SoundEvents.SCULK_BLOCK_CHARGE, SoundSource.BLOCKS, var44, var45, false);
                    }

                    byte var46 = (byte)(param2 & 63);
                    IntProvider var47 = UniformInt.of(0, var43);
                    float var48 = 0.005F;
                    Supplier<Vec3> var49 = () -> new Vec3(
                            Mth.nextDouble(var0, -0.005F, 0.005F), Mth.nextDouble(var0, -0.005F, 0.005F), Mth.nextDouble(var0, -0.005F, 0.005F)
                        );
                    if (var46 == 0) {
                        for(Direction var50 : Direction.values()) {
                            float var51 = var50 == Direction.DOWN ? (float) Math.PI : 0.0F;
                            double var52 = var50.getAxis() == Direction.Axis.Y ? 0.65 : 0.57;
                            ParticleUtils.spawnParticlesOnBlockFace(this.level, param1, new SculkChargeParticleOptions(var51), var47, var50, var49, var52);
                        }
                    } else {
                        for(Direction var53 : MultifaceBlock.unpack(var46)) {
                            float var54 = var53 == Direction.UP ? (float) Math.PI : 0.0F;
                            double var55 = 0.35;
                            ParticleUtils.spawnParticlesOnBlockFace(this.level, param1, new SculkChargeParticleOptions(var54), var47, var53, var49, 0.35);
                        }
                    }
                } else {
                    this.level.playLocalSound(param1, SoundEvents.SCULK_BLOCK_CHARGE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
                    boolean var56 = this.level.getBlockState(param1).isCollisionShapeFullBlock(this.level, param1);
                    int var57 = var56 ? 40 : 20;
                    float var58 = var56 ? 0.45F : 0.25F;
                    float var59 = 0.07F;

                    for(int var60 = 0; var60 < var57; ++var60) {
                        float var61 = 2.0F * var0.nextFloat() - 1.0F;
                        float var62 = 2.0F * var0.nextFloat() - 1.0F;
                        float var63 = 2.0F * var0.nextFloat() - 1.0F;
                        this.level
                            .addParticle(
                                ParticleTypes.SCULK_CHARGE_POP,
                                (double)param1.getX() + 0.5 + (double)(var61 * var58),
                                (double)param1.getY() + 0.5 + (double)(var62 * var58),
                                (double)param1.getZ() + 0.5 + (double)(var63 * var58),
                                (double)(var61 * 0.07F),
                                (double)(var62 * 0.07F),
                                (double)(var63 * 0.07F)
                            );
                    }
                }
                break;
            case 3007:
                for(int var64 = 0; var64 < 10; ++var64) {
                    this.level
                        .addParticle(
                            new ShriekParticleOption(var64 * 5),
                            false,
                            (double)param1.getX() + 0.5,
                            (double)param1.getY() + SculkShriekerBlock.TOP_Y,
                            (double)param1.getZ() + 0.5,
                            0.0,
                            0.0,
                            0.0
                        );
                }

                BlockState var65 = this.level.getBlockState(param1);
                boolean var66 = var65.hasProperty(BlockStateProperties.WATERLOGGED) && var65.getValue(BlockStateProperties.WATERLOGGED);
                if (!var66) {
                    this.level
                        .playLocalSound(
                            (double)param1.getX() + 0.5,
                            (double)param1.getY() + SculkShriekerBlock.TOP_Y,
                            (double)param1.getZ() + 0.5,
                            SoundEvents.SCULK_SHRIEKER_SHRIEK,
                            SoundSource.BLOCKS,
                            2.0F,
                            0.6F + this.level.random.nextFloat() * 0.4F,
                            false
                        );
                }
                break;
            case 3008:
                BlockState var37 = Block.stateById(param2);
                Block var65 = var37.getBlock();
                if (var65 instanceof BrushableBlock var38) {
                    this.level.playLocalSound(param1, var38.getBrushCompletedSound(), SoundSource.PLAYERS, 1.0F, 1.0F, false);
                }

                this.level.addDestroyBlockEffect(param1, var37);
                break;
            case 3009:
                ParticleUtils.spawnParticlesOnBlockFaces(this.level, param1, ParticleTypes.EGG_CRACK, UniformInt.of(3, 6));
        }

    }

    public void destroyBlockProgress(int param0, BlockPos param1, int param2) {
        if (param2 >= 0 && param2 < 10) {
            BlockDestructionProgress var1 = this.destroyingBlocks.get(param0);
            if (var1 != null) {
                this.removeProgress(var1);
            }

            if (var1 == null || var1.getPos().getX() != param1.getX() || var1.getPos().getY() != param1.getY() || var1.getPos().getZ() != param1.getZ()) {
                var1 = new BlockDestructionProgress(param0, param1);
                this.destroyingBlocks.put(param0, var1);
            }

            var1.setProgress(param2);
            var1.updateTick(this.ticks);
            this.destructionProgress.computeIfAbsent(var1.getPos().asLong(), param0x -> Sets.newTreeSet()).add(var1);
        } else {
            BlockDestructionProgress var0 = this.destroyingBlocks.remove(param0);
            if (var0 != null) {
                this.removeProgress(var0);
            }
        }

    }

    public boolean hasRenderedAllSections() {
        return this.sectionRenderDispatcher.isQueueEmpty();
    }

    public void onChunkLoaded(ChunkPos param0) {
        this.sectionOcclusionGraph.onChunkLoaded(param0);
    }

    public void needsUpdate() {
        this.sectionOcclusionGraph.invalidate();
        this.generateClouds = true;
    }

    public void updateGlobalBlockEntities(Collection<BlockEntity> param0, Collection<BlockEntity> param1) {
        synchronized(this.globalBlockEntities) {
            this.globalBlockEntities.removeAll(param0);
            this.globalBlockEntities.addAll(param1);
        }
    }

    public static int getLightColor(BlockAndTintGetter param0, BlockPos param1) {
        return getLightColor(param0, param0.getBlockState(param1), param1);
    }

    public static int getLightColor(BlockAndTintGetter param0, BlockState param1, BlockPos param2) {
        if (param1.emissiveRendering(param0, param2)) {
            return 15728880;
        } else {
            int var0 = param0.getBrightness(LightLayer.SKY, param2);
            int var1 = param0.getBrightness(LightLayer.BLOCK, param2);
            int var2 = param1.getLightEmission();
            if (var1 < var2) {
                var1 = var2;
            }

            return var0 << 20 | var1 << 4;
        }
    }

    public boolean isSectionCompiled(BlockPos param0) {
        SectionRenderDispatcher.RenderSection var0 = this.viewArea.getRenderSectionAt(param0);
        return var0 != null && var0.compiled.get() != SectionRenderDispatcher.CompiledSection.UNCOMPILED;
    }

    @Nullable
    public RenderTarget entityTarget() {
        return this.entityTarget;
    }

    @Nullable
    public RenderTarget getTranslucentTarget() {
        return this.translucentTarget;
    }

    @Nullable
    public RenderTarget getItemEntityTarget() {
        return this.itemEntityTarget;
    }

    @Nullable
    public RenderTarget getParticlesTarget() {
        return this.particlesTarget;
    }

    @Nullable
    public RenderTarget getWeatherTarget() {
        return this.weatherTarget;
    }

    @Nullable
    public RenderTarget getCloudsTarget() {
        return this.cloudsTarget;
    }

    @OnlyIn(Dist.CLIENT)
    public static class TransparencyShaderException extends RuntimeException {
        public TransparencyShaderException(String param0, Throwable param1) {
            super(param0, param1);
        }
    }
}
