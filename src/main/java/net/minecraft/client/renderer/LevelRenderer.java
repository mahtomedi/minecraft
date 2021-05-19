package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
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
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Option;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class LevelRenderer implements ResourceManagerReloadListener, AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final int CHUNK_SIZE = 16;
    public static final int MAX_CHUNKS_WIDTH = 66;
    public static final int MAX_CHUNKS_AREA = 4356;
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
    private final TextureManager textureManager;
    private final EntityRenderDispatcher entityRenderDispatcher;
    private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
    private final RenderBuffers renderBuffers;
    private ClientLevel level;
    private Set<ChunkRenderDispatcher.RenderChunk> chunksToCompile = Sets.newLinkedHashSet();
    private final ObjectArrayList<LevelRenderer.RenderChunkInfo> renderChunks = new ObjectArrayList<>();
    private final Set<BlockEntity> globalBlockEntities = Sets.newHashSet();
    private ViewArea viewArea;
    private LevelRenderer.RenderInfoMap renderInfoMap;
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
    private double lastCameraX = Double.MIN_VALUE;
    private double lastCameraY = Double.MIN_VALUE;
    private double lastCameraZ = Double.MIN_VALUE;
    private int lastCameraChunkX = Integer.MIN_VALUE;
    private int lastCameraChunkY = Integer.MIN_VALUE;
    private int lastCameraChunkZ = Integer.MIN_VALUE;
    private double prevCamX = Double.MIN_VALUE;
    private double prevCamY = Double.MIN_VALUE;
    private double prevCamZ = Double.MIN_VALUE;
    private double prevCamRotX = Double.MIN_VALUE;
    private double prevCamRotY = Double.MIN_VALUE;
    private int prevCloudX = Integer.MIN_VALUE;
    private int prevCloudY = Integer.MIN_VALUE;
    private int prevCloudZ = Integer.MIN_VALUE;
    private Vec3 prevCloudColor = Vec3.ZERO;
    private CloudStatus prevCloudsType;
    private ChunkRenderDispatcher chunkRenderDispatcher;
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
    private boolean needsUpdate = true;
    private int frameId;
    private int rainSoundTime;
    private final float[] rainSizeX = new float[1024];
    private final float[] rainSizeZ = new float[1024];

    public LevelRenderer(Minecraft param0, RenderBuffers param1) {
        this.minecraft = param0;
        this.entityRenderDispatcher = param0.getEntityRenderDispatcher();
        this.blockEntityRenderDispatcher = param0.getBlockEntityRenderDispatcher();
        this.renderBuffers = param1;
        this.textureManager = param0.getTextureManager();

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
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            int var7 = 5;
            if (Minecraft.useFancyGraphics()) {
                var7 = 10;
            }

            RenderSystem.depthMask(Minecraft.useShaderTransparency());
            int var8 = -1;
            float var9 = (float)this.ticks + param1;
            RenderSystem.setShader(GameRenderer::getParticleShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            BlockPos.MutableBlockPos var10 = new BlockPos.MutableBlockPos();

            for(int var11 = var4 - var7; var11 <= var4 + var7; ++var11) {
                for(int var12 = var2 - var7; var12 <= var2 + var7; ++var12) {
                    int var13 = (var11 - var4 + 16) * 32 + var12 - var2 + 16;
                    double var14 = (double)this.rainSizeX[var13] * 0.5;
                    double var15 = (double)this.rainSizeZ[var13] * 0.5;
                    var10.set(var12, 0, var11);
                    Biome var16 = var1.getBiome(var10);
                    if (var16.getPrecipitation() != Biome.Precipitation.NONE) {
                        int var17 = var1.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, var10).getY();
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
                            Random var21 = new Random((long)(var12 * var12 * 3121 + var12 * 45238971 ^ var11 * var11 * 418711 + var11 * 13761));
                            var10.set(var12, var18, var11);
                            float var22 = var16.getTemperature(var10);
                            if (var22 >= 0.15F) {
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
                                double var25 = (double)((float)var12 + 0.5F) - param2;
                                double var26 = (double)((float)var11 + 0.5F) - param4;
                                float var27 = Mth.sqrt(var25 * var25 + var26 * var26) / (float)var7;
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
                            } else {
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
                                double var33 = (double)((float)var12 + 0.5F) - param2;
                                double var34 = (double)((float)var11 + 0.5F) - param4;
                                float var35 = Mth.sqrt(var33 * var33 + var34 * var34) / (float)var7;
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
            Random var1 = new Random((long)this.ticks * 312987231L);
            LevelReader var2 = this.minecraft.level;
            BlockPos var3 = new BlockPos(param0.getPosition());
            BlockPos var4 = null;
            int var5 = (int)(100.0F * var0 * var0) / (this.minecraft.options.particles == ParticleStatus.DECREASED ? 2 : 1);

            for(int var6 = 0; var6 < var5; ++var6) {
                int var7 = var1.nextInt(21) - 10;
                int var8 = var1.nextInt(21) - 10;
                BlockPos var9 = var2.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, var3.offset(var7, 0, var8)).below();
                Biome var10 = var2.getBiome(var9);
                if (var9.getY() > var2.getMinBuildHeight()
                    && var9.getY() <= var3.getY() + 10
                    && var9.getY() >= var3.getY() - 10
                    && var10.getPrecipitation() == Biome.Precipitation.RAIN
                    && var10.getTemperature(var9) >= 0.15F) {
                    var4 = var9;
                    if (this.minecraft.options.particles == ParticleStatus.MINIMAL) {
                        break;
                    }

                    double var11 = var1.nextDouble();
                    double var12 = var1.nextDouble();
                    BlockState var13 = var2.getBlockState(var9);
                    FluidState var14 = var2.getFluidState(var9);
                    VoxelShape var15 = var13.getCollisionShape(var2, var9);
                    double var16 = var15.max(Direction.Axis.Y, var11, var12);
                    double var17 = (double)var14.getHeight(var2, var9);
                    double var18 = Math.max(var16, var17);
                    ParticleOptions var19 = !var14.is(FluidTags.LAVA) && !var13.is(Blocks.MAGMA_BLOCK) && !CampfireBlock.isLitCampfire(var13)
                        ? ParticleTypes.RAIN
                        : ParticleTypes.SMOKE;
                    this.minecraft
                        .level
                        .addParticle(var19, (double)var9.getX() + var11, (double)var9.getY() + var18, (double)var9.getZ() + var12, 0.0, 0.0, 0.0);
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
        } catch (Exception var91) {
            String var8 = var91 instanceof JsonSyntaxException ? "parse" : "load";
            String var9 = "Failed to " + var8 + " shader: " + var0;
            LevelRenderer.TransparencyShaderException var10 = new LevelRenderer.TransparencyShaderException(var9, var91);
            if (this.minecraft.getResourcePackRepository().getSelectedIds().size() > 1) {
                Component var11;
                try {
                    var11 = new TextComponent(this.minecraft.getResourceManager().getResource(var0).getSourceName());
                } catch (IOException var81) {
                    var11 = null;
                }

                this.minecraft.options.graphicsMode = GraphicsStatus.FANCY;
                this.minecraft.clearResourcePacksOnError(var10, var11);
            } else {
                CrashReport var14 = this.minecraft.fillReport(new CrashReport(var9, var10));
                this.minecraft.options.graphicsMode = GraphicsStatus.FANCY;
                this.minecraft.options.save();
                LOGGER.fatal(var9, (Throwable)var10);
                this.minecraft.emergencySave();
                Minecraft.crash(var14);
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

        this.darkBuffer = new VertexBuffer();
        buildSkyDisc(var1, -16.0F);
        this.darkBuffer.upload(var1);
    }

    private void createLightSky() {
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();
        if (this.skyBuffer != null) {
            this.skyBuffer.close();
        }

        this.skyBuffer = new VertexBuffer();
        buildSkyDisc(var1, 16.0F);
        this.skyBuffer.upload(var1);
    }

    private static void buildSkyDisc(BufferBuilder param0, float param1) {
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

        param0.end();
    }

    private void createStars() {
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        if (this.starBuffer != null) {
            this.starBuffer.close();
        }

        this.starBuffer = new VertexBuffer();
        this.drawStars(var1);
        var1.end();
        this.starBuffer.upload(var1);
    }

    private void drawStars(BufferBuilder param0) {
        Random var0 = new Random(10842L);
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

    }

    public void setLevel(@Nullable ClientLevel param0) {
        this.lastCameraX = Double.MIN_VALUE;
        this.lastCameraY = Double.MIN_VALUE;
        this.lastCameraZ = Double.MIN_VALUE;
        this.lastCameraChunkX = Integer.MIN_VALUE;
        this.lastCameraChunkY = Integer.MIN_VALUE;
        this.lastCameraChunkZ = Integer.MIN_VALUE;
        this.entityRenderDispatcher.setLevel(param0);
        this.level = param0;
        if (param0 != null) {
            this.renderChunks.ensureCapacity(4356 * param0.getSectionsCount());
            this.allChanged();
        } else {
            this.chunksToCompile.clear();
            this.renderChunks.clear();
            if (this.viewArea != null) {
                this.viewArea.releaseAllBuffers();
                this.viewArea = null;
            }

            if (this.chunkRenderDispatcher != null) {
                this.chunkRenderDispatcher.dispose();
            }

            this.chunkRenderDispatcher = null;
            this.globalBlockEntities.clear();
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
            if (this.chunkRenderDispatcher == null) {
                this.chunkRenderDispatcher = new ChunkRenderDispatcher(
                    this.level, this, Util.backgroundExecutor(), this.minecraft.is64Bit(), this.renderBuffers.fixedBufferPack()
                );
            } else {
                this.chunkRenderDispatcher.setLevel(this.level);
            }

            this.needsUpdate = true;
            this.generateClouds = true;
            ItemBlockRenderTypes.setFancy(Minecraft.useFancyGraphics());
            this.lastViewDistance = this.minecraft.options.renderDistance;
            if (this.viewArea != null) {
                this.viewArea.releaseAllBuffers();
            }

            this.resetChunksToCompile();
            synchronized(this.globalBlockEntities) {
                this.globalBlockEntities.clear();
            }

            this.viewArea = new ViewArea(this.chunkRenderDispatcher, this.level, this.minecraft.options.renderDistance, this);
            this.renderInfoMap = new LevelRenderer.RenderInfoMap(this.viewArea.chunks.length);
            if (this.level != null) {
                Entity var0 = this.minecraft.getCameraEntity();
                if (var0 != null) {
                    this.viewArea.repositionCamera(var0.getX(), var0.getZ());
                }
            }

        }
    }

    protected void resetChunksToCompile() {
        this.chunksToCompile.clear();
        this.chunkRenderDispatcher.blockUntilClear();
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

    public String getChunkStatistics() {
        int var0 = this.viewArea.chunks.length;
        int var1 = this.countRenderedChunks();
        return String.format(
            "C: %d/%d %sD: %d, %s",
            var1,
            var0,
            this.minecraft.smartCull ? "(s) " : "",
            this.lastViewDistance,
            this.chunkRenderDispatcher == null ? "null" : this.chunkRenderDispatcher.getStats()
        );
    }

    public ChunkRenderDispatcher getChunkRenderDispatcher() {
        return this.chunkRenderDispatcher;
    }

    public double getTotalChunks() {
        return (double)this.viewArea.chunks.length;
    }

    public double getLastViewDistance() {
        return (double)this.lastViewDistance;
    }

    public int countRenderedChunks() {
        int var0 = 0;

        for(LevelRenderer.RenderChunkInfo var1 : this.renderChunks) {
            if (!var1.chunk.getCompiledChunk().hasNoRenderableLayers()) {
                ++var0;
            }
        }

        return var0;
    }

    public String getEntityStatistics() {
        return "E: " + this.renderedEntities + "/" + this.level.getEntityCount() + ", B: " + this.culledEntities;
    }

    private void setupRender(Camera param0, Frustum param1, boolean param2, int param3, boolean param4) {
        Vec3 var0 = param0.getPosition();
        if (this.minecraft.options.renderDistance != this.lastViewDistance) {
            this.allChanged();
        }

        this.level.getProfiler().push("camera");
        double var1 = this.minecraft.player.getX();
        double var2 = this.minecraft.player.getY();
        double var3 = this.minecraft.player.getZ();
        double var4 = var1 - this.lastCameraX;
        double var5 = var2 - this.lastCameraY;
        double var6 = var3 - this.lastCameraZ;
        int var7 = SectionPos.posToSectionCoord(var1);
        int var8 = SectionPos.posToSectionCoord(var2);
        int var9 = SectionPos.posToSectionCoord(var3);
        if (this.lastCameraChunkX != var7 || this.lastCameraChunkY != var8 || this.lastCameraChunkZ != var9 || var4 * var4 + var5 * var5 + var6 * var6 > 16.0) {
            this.lastCameraX = var1;
            this.lastCameraY = var2;
            this.lastCameraZ = var3;
            this.lastCameraChunkX = var7;
            this.lastCameraChunkY = var8;
            this.lastCameraChunkZ = var9;
            this.viewArea.repositionCamera(var1, var3);
        }

        this.chunkRenderDispatcher.setCamera(var0);
        this.level.getProfiler().popPush("cull");
        this.minecraft.getProfiler().popPush("culling");
        BlockPos var10 = param0.getBlockPosition();
        ChunkRenderDispatcher.RenderChunk var11 = this.viewArea.getRenderChunkAt(var10);
        int var12 = 16;
        BlockPos var13 = new BlockPos(Mth.floor(var0.x / 16.0) * 16, Mth.floor(var0.y / 16.0) * 16, Mth.floor(var0.z / 16.0) * 16);
        float var14 = param0.getXRot();
        float var15 = param0.getYRot();
        this.needsUpdate = this.needsUpdate
            || !this.chunksToCompile.isEmpty()
            || var0.x != this.prevCamX
            || var0.y != this.prevCamY
            || var0.z != this.prevCamZ
            || (double)var14 != this.prevCamRotX
            || (double)var15 != this.prevCamRotY;
        this.prevCamX = var0.x;
        this.prevCamY = var0.y;
        this.prevCamZ = var0.z;
        this.prevCamRotX = (double)var14;
        this.prevCamRotY = (double)var15;
        this.minecraft.getProfiler().popPush("update");
        if (!param2 && this.needsUpdate) {
            this.needsUpdate = false;
            this.updateRenderChunks(param1, param3, param4, var0, var10, var11, 16, var13);
        }

        this.minecraft.getProfiler().popPush("rebuildNear");
        Set<ChunkRenderDispatcher.RenderChunk> var16 = this.chunksToCompile;
        this.chunksToCompile = Sets.newLinkedHashSet();

        for(LevelRenderer.RenderChunkInfo var17 : this.renderChunks) {
            ChunkRenderDispatcher.RenderChunk var18 = var17.chunk;
            if (var18.isDirty() || var16.contains(var18)) {
                this.needsUpdate = true;
                BlockPos var19 = var18.getOrigin().offset(8, 8, 8);
                boolean var20 = var19.distSqr(var10) < 768.0;
                if (!var18.isDirtyFromPlayer() && !var20) {
                    this.chunksToCompile.add(var18);
                } else {
                    this.minecraft.getProfiler().push("build near");
                    this.chunkRenderDispatcher.rebuildChunkSync(var18);
                    var18.setNotDirty();
                    this.minecraft.getProfiler().pop();
                }
            }
        }

        this.chunksToCompile.addAll(var16);
        this.minecraft.getProfiler().pop();
    }

    private void updateRenderChunks(
        Frustum param0, int param1, boolean param2, Vec3 param3, BlockPos param4, ChunkRenderDispatcher.RenderChunk param5, int param6, BlockPos param7
    ) {
        this.renderChunks.clear();
        Queue<LevelRenderer.RenderChunkInfo> var0 = Queues.newArrayDeque();
        Entity.setViewScale(Mth.clamp((double)this.minecraft.options.renderDistance / 8.0, 1.0, 2.5) * (double)this.minecraft.options.entityDistanceScaling);
        boolean var1 = this.minecraft.smartCull;
        if (param5 == null) {
            int var2 = param4.getY() > this.level.getMinBuildHeight() ? this.level.getMaxBuildHeight() - 8 : this.level.getMinBuildHeight() + 8;
            int var3 = Mth.floor(param3.x / (double)param6) * param6;
            int var4 = Mth.floor(param3.z / (double)param6) * param6;
            List<LevelRenderer.RenderChunkInfo> var5 = Lists.newArrayList();

            for(int var6 = -this.lastViewDistance; var6 <= this.lastViewDistance; ++var6) {
                for(int var7 = -this.lastViewDistance; var7 <= this.lastViewDistance; ++var7) {
                    ChunkRenderDispatcher.RenderChunk var8 = this.viewArea
                        .getRenderChunkAt(new BlockPos(var3 + SectionPos.sectionToBlockCoord(var6, 8), var2, var4 + SectionPos.sectionToBlockCoord(var7, 8)));
                    if (var8 != null && param0.isVisible(var8.bb)) {
                        var8.setFrame(param1);
                        var5.add(new LevelRenderer.RenderChunkInfo(var8, null, 0));
                    }
                }
            }

            var5.sort(Comparator.comparingDouble(param1x -> param4.distSqr(param1x.chunk.getOrigin().offset(8, 8, 8))));
            var0.addAll(var5);
        } else {
            if (param2 && this.level.getBlockState(param4).isSolidRender(this.level, param4)) {
                var1 = false;
            }

            param5.setFrame(param1);
            var0.add(new LevelRenderer.RenderChunkInfo(param5, null, 0));
        }

        this.minecraft.getProfiler().push("iteration");
        int var9 = this.minecraft.options.renderDistance;
        this.renderInfoMap.clear();

        while(!var0.isEmpty()) {
            LevelRenderer.RenderChunkInfo var10 = var0.poll();
            ChunkRenderDispatcher.RenderChunk var11 = var10.chunk;
            this.renderChunks.add(var10);

            for(Direction var12 : DIRECTIONS) {
                ChunkRenderDispatcher.RenderChunk var13 = this.getRelativeFrom(param7, var11, var12);
                if (!var1 || !var10.hasDirection(var12.getOpposite())) {
                    if (var1 && var10.hasSourceDirections()) {
                        ChunkRenderDispatcher.CompiledChunk var14 = var11.getCompiledChunk();
                        boolean var15 = false;

                        for(int var16 = 0; var16 < DIRECTIONS.length; ++var16) {
                            if (var10.hasSourceDirection(var16) && var14.facesCanSeeEachother(DIRECTIONS[var16].getOpposite(), var12)) {
                                var15 = true;
                                break;
                            }
                        }

                        if (!var15) {
                            continue;
                        }
                    }

                    if (var13 != null && var13.hasAllNeighbors()) {
                        if (!var13.setFrame(param1)) {
                            LevelRenderer.RenderChunkInfo var17 = this.renderInfoMap.get(var13);
                            if (var17 != null) {
                                var17.addSourceDirection(var12);
                            }
                        } else if (param0.isVisible(var13.bb)) {
                            LevelRenderer.RenderChunkInfo var18 = new LevelRenderer.RenderChunkInfo(var13, var12, var10.step + 1);
                            var18.setDirections(var10.directions, var12);
                            var0.add(var18);
                            this.renderInfoMap.put(var13, var18);
                        }
                    }
                }
            }
        }

        this.minecraft.getProfiler().pop();
    }

    @Nullable
    private ChunkRenderDispatcher.RenderChunk getRelativeFrom(BlockPos param0, ChunkRenderDispatcher.RenderChunk param1, Direction param2) {
        BlockPos var0 = param1.getRelativeOrigin(param2);
        if (Mth.abs(param0.getX() - var0.getX()) > this.lastViewDistance * 16) {
            return null;
        } else if (var0.getY() < this.level.getMinBuildHeight() || var0.getY() >= this.level.getMaxBuildHeight()) {
            return null;
        } else {
            return Mth.abs(param0.getZ() - var0.getZ()) > this.lastViewDistance * 16 ? null : this.viewArea.getRenderChunkAt(var0);
        }
    }

    private void captureFrustum(Matrix4f param0, Matrix4f param1, double param2, double param3, double param4, Frustum param5) {
        this.capturedFrustum = param5;
        Matrix4f var0 = param1.copy();
        var0.multiply(param0);
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
            this.frustumPoints[var1].transform(var0);
            this.frustumPoints[var1].perspectiveDivide();
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
        var0.popPush("light_updates");
        this.minecraft.level.getChunkSource().getLightEngine().runUpdates(Integer.MAX_VALUE, true, true);
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
        FogRenderer.setupColor(param4, param1, this.minecraft.level, this.minecraft.options.renderDistance, param5.getDarkenWorldAmount(param1));
        FogRenderer.levelFogColor();
        RenderSystem.clear(16640, Minecraft.ON_OSX);
        float var9 = param5.getRenderDistance();
        boolean var10 = this.minecraft.level.effects().isFoggyAt(Mth.floor(var2), Mth.floor(var3))
            || this.minecraft.gui.getBossOverlay().shouldCreateWorldFog();
        if (this.minecraft.options.renderDistance >= 4) {
            FogRenderer.setupFog(param4, FogRenderer.FogMode.FOG_SKY, var9, var10);
            var0.popPush("sky");
            RenderSystem.setShader(GameRenderer::getPositionShader);
            this.renderSky(param0, param7, param1);
        }

        var0.popPush("fog");
        FogRenderer.setupFog(param4, FogRenderer.FogMode.FOG_TERRAIN, Math.max(var9 - 16.0F, 32.0F), var10);
        var0.popPush("terrain_setup");
        this.setupRender(param4, var7, var6, this.frameId++, this.minecraft.player.isSpectator());
        var0.popPush("updatechunks");
        int var11 = 30;
        int var12 = this.minecraft.options.framerateLimit;
        long var13 = 33333333L;
        long var14;
        if ((double)var12 == Option.FRAMERATE_LIMIT.getMaxValue()) {
            var14 = 0L;
        } else {
            var14 = (long)(1000000000 / var12);
        }

        long var16 = Util.getNanos() - param2;
        long var17 = this.frameTimes.registerValueAndGetMean(var16);
        long var18 = var17 * 3L / 2L;
        long var19 = Mth.clamp(var18, var14, 33333333L);
        this.compileChunksUntil(param2 + var19);
        var0.popPush("terrain");
        this.renderChunkLayer(RenderType.solid(), param0, var2, var3, var4, param7);
        this.renderChunkLayer(RenderType.cutoutMipped(), param0, var2, var3, var4, param7);
        this.renderChunkLayer(RenderType.cutout(), param0, var2, var3, var4, param7);
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

        boolean var20 = false;
        MultiBufferSource.BufferSource var21 = this.renderBuffers.bufferSource();

        for(Entity var22 : this.level.entitiesForRendering()) {
            if ((this.entityRenderDispatcher.shouldRender(var22, var7, var2, var3, var4) || var22.hasIndirectPassenger(this.minecraft.player))
                && (
                    var22 != param4.getEntity()
                        || param4.isDetached()
                        || param4.getEntity() instanceof LivingEntity && ((LivingEntity)param4.getEntity()).isSleeping()
                )
                && (!(var22 instanceof LocalPlayer) || param4.getEntity() == var22)) {
                ++this.renderedEntities;
                if (var22.tickCount == 0) {
                    var22.xOld = var22.getX();
                    var22.yOld = var22.getY();
                    var22.zOld = var22.getZ();
                }

                MultiBufferSource var24;
                if (this.shouldShowEntityOutlines() && this.minecraft.shouldEntityAppearGlowing(var22)) {
                    var20 = true;
                    OutlineBufferSource var23 = this.renderBuffers.outlineBufferSource();
                    var24 = var23;
                    int var25 = var22.getTeamColor();
                    int var26 = 255;
                    int var27 = var25 >> 16 & 0xFF;
                    int var28 = var25 >> 8 & 0xFF;
                    int var29 = var25 & 0xFF;
                    var23.setColor(var27, var28, var29, 255);
                } else {
                    var24 = var21;
                }

                this.renderEntity(var22, var2, var3, var4, param1, param0, var24);
            }
        }

        var21.endLastBatch();
        this.checkPoseStack(param0);
        var21.endBatch(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS));
        var21.endBatch(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS));
        var21.endBatch(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
        var21.endBatch(RenderType.entitySmoothCutout(TextureAtlas.LOCATION_BLOCKS));
        var0.popPush("blockentities");

        for(LevelRenderer.RenderChunkInfo var31 : this.renderChunks) {
            List<BlockEntity> var32 = var31.chunk.getCompiledChunk().getRenderableBlockEntities();
            if (!var32.isEmpty()) {
                for(BlockEntity var33 : var32) {
                    BlockPos var34 = var33.getBlockPos();
                    MultiBufferSource var35 = var21;
                    param0.pushPose();
                    param0.translate((double)var34.getX() - var2, (double)var34.getY() - var3, (double)var34.getZ() - var4);
                    SortedSet<BlockDestructionProgress> var36 = this.destructionProgress.get(var34.asLong());
                    if (var36 != null && !var36.isEmpty()) {
                        int var37 = var36.last().getProgress();
                        if (var37 >= 0) {
                            PoseStack.Pose var38 = param0.last();
                            VertexConsumer var39 = new SheetedDecalTextureGenerator(
                                this.renderBuffers.crumblingBufferSource().getBuffer(ModelBakery.DESTROY_TYPES.get(var37)), var38.pose(), var38.normal()
                            );
                            var35 = param2x -> {
                                VertexConsumer var0x = var21.getBuffer(param2x);
                                return param2x.affectsCrumbling() ? VertexMultiConsumer.create(var39, var0x) : var0x;
                            };
                        }
                    }

                    this.blockEntityRenderDispatcher.render(var33, param1, param0, var35);
                    param0.popPose();
                }
            }
        }

        synchronized(this.globalBlockEntities) {
            for(BlockEntity var40 : this.globalBlockEntities) {
                BlockPos var41 = var40.getBlockPos();
                param0.pushPose();
                param0.translate((double)var41.getX() - var2, (double)var41.getY() - var3, (double)var41.getZ() - var4);
                this.blockEntityRenderDispatcher.render(var40, param1, param0, var21);
                param0.popPose();
            }
        }

        this.checkPoseStack(param0);
        var21.endBatch(RenderType.solid());
        var21.endBatch(RenderType.endPortal());
        var21.endBatch(RenderType.endGateway());
        var21.endBatch(Sheets.solidBlockSheet());
        var21.endBatch(Sheets.cutoutBlockSheet());
        var21.endBatch(Sheets.bedSheet());
        var21.endBatch(Sheets.shulkerBoxSheet());
        var21.endBatch(Sheets.signSheet());
        var21.endBatch(Sheets.chestSheet());
        this.renderBuffers.outlineBufferSource().endOutlineBatch();
        if (var20) {
            this.entityEffect.process(param1);
            this.minecraft.getMainRenderTarget().bindWrite(false);
        }

        var0.popPush("destroyProgress");

        for(Entry<SortedSet<BlockDestructionProgress>> var42 : this.destructionProgress.long2ObjectEntrySet()) {
            BlockPos var43 = BlockPos.of(var42.getLongKey());
            double var44 = (double)var43.getX() - var2;
            double var45 = (double)var43.getY() - var3;
            double var46 = (double)var43.getZ() - var4;
            if (!(var44 * var44 + var45 * var45 + var46 * var46 > 1024.0)) {
                SortedSet<BlockDestructionProgress> var47 = var42.getValue();
                if (var47 != null && !var47.isEmpty()) {
                    int var48 = var47.last().getProgress();
                    param0.pushPose();
                    param0.translate((double)var43.getX() - var2, (double)var43.getY() - var3, (double)var43.getZ() - var4);
                    PoseStack.Pose var49 = param0.last();
                    VertexConsumer var50 = new SheetedDecalTextureGenerator(
                        this.renderBuffers.crumblingBufferSource().getBuffer(ModelBakery.DESTROY_TYPES.get(var48)), var49.pose(), var49.normal()
                    );
                    this.minecraft.getBlockRenderer().renderBreakingTexture(this.level.getBlockState(var43), var43, this.level, param0, var50);
                    param0.popPose();
                }
            }
        }

        this.checkPoseStack(param0);
        HitResult var51 = this.minecraft.hitResult;
        if (param3 && var51 != null && var51.getType() == HitResult.Type.BLOCK) {
            var0.popPush("outline");
            BlockPos var52 = ((BlockHitResult)var51).getBlockPos();
            BlockState var53 = this.level.getBlockState(var52);
            if (!var53.isAir() && this.level.getWorldBorder().isWithinBounds(var52)) {
                VertexConsumer var54 = var21.getBuffer(RenderType.lines());
                this.renderHitOutline(param0, var54, param4.getEntity(), var2, var3, var4, var52, var53);
            }
        }

        PoseStack var55 = RenderSystem.getModelViewStack();
        var55.pushPose();
        var55.mulPoseMatrix(param0.last().pose());
        RenderSystem.applyModelViewMatrix();
        this.minecraft.debugRenderer.render(param0, var21, var2, var3, var4);
        var55.popPose();
        RenderSystem.applyModelViewMatrix();
        var21.endBatch(Sheets.translucentCullBlockSheet());
        var21.endBatch(Sheets.bannerSheet());
        var21.endBatch(Sheets.shieldSheet());
        var21.endBatch(RenderType.armorGlint());
        var21.endBatch(RenderType.armorEntityGlint());
        var21.endBatch(RenderType.glint());
        var21.endBatch(RenderType.glintDirect());
        var21.endBatch(RenderType.glintTranslucent());
        var21.endBatch(RenderType.entityGlint());
        var21.endBatch(RenderType.entityGlintDirect());
        var21.endBatch(RenderType.waterMask());
        this.renderBuffers.crumblingBufferSource().endBatch();
        if (this.transparencyChain != null) {
            var21.endBatch(RenderType.lines());
            var21.endBatch();
            this.translucentTarget.clear(Minecraft.ON_OSX);
            this.translucentTarget.copyDepthFrom(this.minecraft.getMainRenderTarget());
            var0.popPush("translucent");
            this.renderChunkLayer(RenderType.translucent(), param0, var2, var3, var4, param7);
            var0.popPush("string");
            this.renderChunkLayer(RenderType.tripwire(), param0, var2, var3, var4, param7);
            this.particlesTarget.clear(Minecraft.ON_OSX);
            this.particlesTarget.copyDepthFrom(this.minecraft.getMainRenderTarget());
            RenderStateShard.PARTICLES_TARGET.setupRenderState();
            var0.popPush("particles");
            this.minecraft.particleEngine.render(param0, var21, param6, param4, param1);
            RenderStateShard.PARTICLES_TARGET.clearRenderState();
        } else {
            var0.popPush("translucent");
            if (this.translucentTarget != null) {
                this.translucentTarget.clear(Minecraft.ON_OSX);
            }

            this.renderChunkLayer(RenderType.translucent(), param0, var2, var3, var4, param7);
            var21.endBatch(RenderType.lines());
            var21.endBatch();
            var0.popPush("string");
            this.renderChunkLayer(RenderType.tripwire(), param0, var2, var3, var4, param7);
            var0.popPush("particles");
            this.minecraft.particleEngine.render(param0, var21, param6, param4, param1);
        }

        var55.pushPose();
        var55.mulPoseMatrix(param0.last().pose());
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

        this.renderDebug(param4);
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        var55.popPose();
        RenderSystem.applyModelViewMatrix();
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

    private void renderChunkLayer(RenderType param0, PoseStack param1, double param2, double param3, double param4, Matrix4f param5) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        param0.setupRenderState();
        if (param0 == RenderType.translucent()) {
            this.minecraft.getProfiler().push("translucent_sort");
            double var0 = param2 - this.xTransparentOld;
            double var1 = param3 - this.yTransparentOld;
            double var2 = param4 - this.zTransparentOld;
            if (var0 * var0 + var1 * var1 + var2 * var2 > 1.0) {
                this.xTransparentOld = param2;
                this.yTransparentOld = param3;
                this.zTransparentOld = param4;
                int var3 = 0;

                for(LevelRenderer.RenderChunkInfo var4 : this.renderChunks) {
                    if (var3 < 15 && var4.chunk.resortTransparency(param0, this.chunkRenderDispatcher)) {
                        ++var3;
                    }
                }
            }

            this.minecraft.getProfiler().pop();
        }

        this.minecraft.getProfiler().push("filterempty");
        this.minecraft.getProfiler().popPush(() -> "render_" + param0);
        boolean var5 = param0 != RenderType.translucent();
        ObjectListIterator<LevelRenderer.RenderChunkInfo> var6 = this.renderChunks.listIterator(var5 ? 0 : this.renderChunks.size());
        VertexFormat var7 = param0.format();
        ShaderInstance var8 = RenderSystem.getShader();
        BufferUploader.reset();

        for(int var9 = 0; var9 < 12; ++var9) {
            int var10 = RenderSystem.getShaderTexture(var9);
            var8.setSampler("Sampler" + var9, var10);
        }

        if (var8.MODEL_VIEW_MATRIX != null) {
            var8.MODEL_VIEW_MATRIX.set(param1.last().pose());
        }

        if (var8.PROJECTION_MATRIX != null) {
            var8.PROJECTION_MATRIX.set(param5);
        }

        if (var8.COLOR_MODULATOR != null) {
            var8.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
        }

        if (var8.FOG_START != null) {
            var8.FOG_START.set(RenderSystem.getShaderFogStart());
        }

        if (var8.FOG_END != null) {
            var8.FOG_END.set(RenderSystem.getShaderFogEnd());
        }

        if (var8.FOG_COLOR != null) {
            var8.FOG_COLOR.set(RenderSystem.getShaderFogColor());
        }

        if (var8.TEXTURE_MATRIX != null) {
            var8.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
        }

        if (var8.GAME_TIME != null) {
            var8.GAME_TIME.set(RenderSystem.getShaderGameTime());
        }

        RenderSystem.setupShaderLights(var8);
        var8.apply();
        Uniform var11 = var8.CHUNK_OFFSET;
        boolean var12 = false;

        while(true) {
            if (var5) {
                if (!var6.hasNext()) {
                    break;
                }
            } else if (!var6.hasPrevious()) {
                break;
            }

            LevelRenderer.RenderChunkInfo var13 = var5 ? var6.next() : var6.previous();
            ChunkRenderDispatcher.RenderChunk var14 = var13.chunk;
            if (!var14.getCompiledChunk().isEmpty(param0)) {
                VertexBuffer var15 = var14.getBuffer(param0);
                BlockPos var16 = var14.getOrigin();
                if (var11 != null) {
                    var11.set((float)((double)var16.getX() - param2), (float)((double)var16.getY() - param3), (float)((double)var16.getZ() - param4));
                    var11.upload();
                }

                var15.drawChunkLayer();
                var12 = true;
            }
        }

        if (var11 != null) {
            var11.set(Vector3f.ZERO);
        }

        var8.clear();
        if (var12) {
            var7.clearBufferState();
        }

        VertexBuffer.unbind();
        VertexBuffer.unbindVertexArray();
        this.minecraft.getProfiler().pop();
        param0.clearRenderState();
    }

    private void renderDebug(Camera param0) {
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        if (this.minecraft.chunkPath || this.minecraft.chunkVisibility) {
            double var2 = param0.getPosition().x();
            double var3 = param0.getPosition().y();
            double var4 = param0.getPosition().z();
            RenderSystem.depthMask(true);
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableTexture();

            for(LevelRenderer.RenderChunkInfo var5 : this.renderChunks) {
                ChunkRenderDispatcher.RenderChunk var6 = var5.chunk;
                BlockPos var7 = var6.getOrigin();
                PoseStack var8 = RenderSystem.getModelViewStack();
                var8.pushPose();
                var8.translate((double)var7.getX() - var2, (double)var7.getY() - var3, (double)var7.getZ() - var4);
                RenderSystem.applyModelViewMatrix();
                if (this.minecraft.chunkPath) {
                    var1.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
                    RenderSystem.lineWidth(10.0F);
                    int var9 = var5.step == 0 ? 0 : Mth.hsvToRgb((float)var5.step / 50.0F, 0.9F, 0.9F);
                    int var10 = var9 >> 16 & 0xFF;
                    int var11 = var9 >> 8 & 0xFF;
                    int var12 = var9 & 0xFF;

                    for(int var13 = 0; var13 < DIRECTIONS.length; ++var13) {
                        if (var5.hasSourceDirection(var13)) {
                            Direction var14 = DIRECTIONS[var13];
                            var1.vertex(8.0, 8.0, 8.0).color(var10, var11, var12, 255).endVertex();
                            var1.vertex((double)(8 - 16 * var14.getStepX()), (double)(8 - 16 * var14.getStepY()), (double)(8 - 16 * var14.getStepZ()))
                                .color(var10, var11, var12, 255)
                                .endVertex();
                        }
                    }

                    var0.end();
                    RenderSystem.lineWidth(1.0F);
                }

                if (this.minecraft.chunkVisibility && !var6.getCompiledChunk().hasNoRenderableLayers()) {
                    var1.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
                    RenderSystem.lineWidth(10.0F);
                    int var15 = 0;

                    for(Direction var16 : DIRECTIONS) {
                        for(Direction var17 : DIRECTIONS) {
                            boolean var18 = var6.getCompiledChunk().facesCanSeeEachother(var16, var17);
                            if (!var18) {
                                ++var15;
                                var1.vertex((double)(8 + 8 * var16.getStepX()), (double)(8 + 8 * var16.getStepY()), (double)(8 + 8 * var16.getStepZ()))
                                    .color(1, 0, 0, 1)
                                    .endVertex();
                                var1.vertex((double)(8 + 8 * var17.getStepX()), (double)(8 + 8 * var17.getStepY()), (double)(8 + 8 * var17.getStepZ()))
                                    .color(1, 0, 0, 1)
                                    .endVertex();
                            }
                        }
                    }

                    var0.end();
                    RenderSystem.lineWidth(1.0F);
                    if (var15 > 0) {
                        var1.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                        float var19 = 0.5F;
                        float var20 = 0.2F;
                        var1.vertex(0.5, 15.5, 0.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        var1.vertex(15.5, 15.5, 0.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        var1.vertex(15.5, 15.5, 15.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        var1.vertex(0.5, 15.5, 15.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        var1.vertex(0.5, 0.5, 15.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        var1.vertex(15.5, 0.5, 15.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        var1.vertex(15.5, 0.5, 0.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        var1.vertex(0.5, 0.5, 0.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        var1.vertex(0.5, 15.5, 0.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        var1.vertex(0.5, 15.5, 15.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        var1.vertex(0.5, 0.5, 15.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        var1.vertex(0.5, 0.5, 0.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        var1.vertex(15.5, 0.5, 0.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        var1.vertex(15.5, 0.5, 15.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        var1.vertex(15.5, 15.5, 15.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        var1.vertex(15.5, 15.5, 0.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        var1.vertex(0.5, 0.5, 0.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        var1.vertex(15.5, 0.5, 0.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        var1.vertex(15.5, 15.5, 0.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        var1.vertex(0.5, 15.5, 0.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        var1.vertex(0.5, 15.5, 15.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        var1.vertex(15.5, 15.5, 15.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        var1.vertex(15.5, 0.5, 15.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        var1.vertex(0.5, 0.5, 15.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        var0.end();
                    }
                }

                var8.popPose();
                RenderSystem.applyModelViewMatrix();
            }

            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.enableTexture();
        }

        if (this.capturedFrustum != null) {
            RenderSystem.disableCull();
            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.lineWidth(10.0F);
            PoseStack var21 = RenderSystem.getModelViewStack();
            var21.pushPose();
            var21.translate(
                (double)((float)(this.frustumPos.x - param0.getPosition().x)),
                (double)((float)(this.frustumPos.y - param0.getPosition().y)),
                (double)((float)(this.frustumPos.z - param0.getPosition().z))
            );
            RenderSystem.applyModelViewMatrix();
            RenderSystem.depthMask(true);
            var1.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            this.addFrustumQuad(var1, 0, 1, 2, 3, 0, 1, 1);
            this.addFrustumQuad(var1, 4, 5, 6, 7, 1, 0, 0);
            this.addFrustumQuad(var1, 0, 1, 5, 4, 1, 1, 0);
            this.addFrustumQuad(var1, 2, 3, 7, 6, 0, 0, 1);
            this.addFrustumQuad(var1, 0, 4, 7, 3, 0, 1, 0);
            this.addFrustumQuad(var1, 1, 5, 6, 2, 1, 0, 1);
            var0.end();
            RenderSystem.depthMask(false);
            RenderSystem.setShader(GameRenderer::getPositionShader);
            var1.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            this.addFrustumVertex(var1, 0);
            this.addFrustumVertex(var1, 1);
            this.addFrustumVertex(var1, 1);
            this.addFrustumVertex(var1, 2);
            this.addFrustumVertex(var1, 2);
            this.addFrustumVertex(var1, 3);
            this.addFrustumVertex(var1, 3);
            this.addFrustumVertex(var1, 0);
            this.addFrustumVertex(var1, 4);
            this.addFrustumVertex(var1, 5);
            this.addFrustumVertex(var1, 5);
            this.addFrustumVertex(var1, 6);
            this.addFrustumVertex(var1, 6);
            this.addFrustumVertex(var1, 7);
            this.addFrustumVertex(var1, 7);
            this.addFrustumVertex(var1, 4);
            this.addFrustumVertex(var1, 0);
            this.addFrustumVertex(var1, 4);
            this.addFrustumVertex(var1, 1);
            this.addFrustumVertex(var1, 5);
            this.addFrustumVertex(var1, 2);
            this.addFrustumVertex(var1, 6);
            this.addFrustumVertex(var1, 3);
            this.addFrustumVertex(var1, 7);
            var0.end();
            var21.popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.enableTexture();
            RenderSystem.lineWidth(1.0F);
        }

    }

    private void addFrustumVertex(VertexConsumer param0, int param1) {
        param0.vertex((double)this.frustumPoints[param1].x(), (double)this.frustumPoints[param1].y(), (double)this.frustumPoints[param1].z()).endVertex();
    }

    private void addFrustumQuad(VertexConsumer param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7) {
        float var0 = 0.25F;
        param0.vertex((double)this.frustumPoints[param1].x(), (double)this.frustumPoints[param1].y(), (double)this.frustumPoints[param1].z())
            .color((float)param5, (float)param6, (float)param7, 0.25F)
            .endVertex();
        param0.vertex((double)this.frustumPoints[param2].x(), (double)this.frustumPoints[param2].y(), (double)this.frustumPoints[param2].z())
            .color((float)param5, (float)param6, (float)param7, 0.25F)
            .endVertex();
        param0.vertex((double)this.frustumPoints[param3].x(), (double)this.frustumPoints[param3].y(), (double)this.frustumPoints[param3].z())
            .color((float)param5, (float)param6, (float)param7, 0.25F)
            .endVertex();
        param0.vertex((double)this.frustumPoints[param4].x(), (double)this.frustumPoints[param4].y(), (double)this.frustumPoints[param4].z())
            .color((float)param5, (float)param6, (float)param7, 0.25F)
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
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, END_SKY_LOCATION);
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();

        for(int var2 = 0; var2 < 6; ++var2) {
            param0.pushPose();
            if (var2 == 1) {
                param0.mulPose(Vector3f.XP.rotationDegrees(90.0F));
            }

            if (var2 == 2) {
                param0.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
            }

            if (var2 == 3) {
                param0.mulPose(Vector3f.XP.rotationDegrees(180.0F));
            }

            if (var2 == 4) {
                param0.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
            }

            if (var2 == 5) {
                param0.mulPose(Vector3f.ZP.rotationDegrees(-90.0F));
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
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public void renderSky(PoseStack param0, Matrix4f param1, float param2) {
        if (this.minecraft.level.effects().skyType() == DimensionSpecialEffects.SkyType.END) {
            this.renderEndSky(param0);
        } else if (this.minecraft.level.effects().skyType() == DimensionSpecialEffects.SkyType.NORMAL) {
            RenderSystem.disableTexture();
            Vec3 var0 = this.level.getSkyColor(this.minecraft.gameRenderer.getMainCamera().getPosition(), param2);
            float var1 = (float)var0.x;
            float var2 = (float)var0.y;
            float var3 = (float)var0.z;
            FogRenderer.levelFogColor();
            BufferBuilder var4 = Tesselator.getInstance().getBuilder();
            RenderSystem.depthMask(false);
            RenderSystem.setShaderColor(var1, var2, var3, 1.0F);
            ShaderInstance var5 = RenderSystem.getShader();
            this.skyBuffer.drawWithShader(param0.last().pose(), param1, var5);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            float[] var6 = this.level.effects().getSunriseColor(this.level.getTimeOfDay(param2), param2);
            if (var6 != null) {
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                RenderSystem.disableTexture();
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                param0.pushPose();
                param0.mulPose(Vector3f.XP.rotationDegrees(90.0F));
                float var7 = Mth.sin(this.level.getSunAngle(param2)) < 0.0F ? 180.0F : 0.0F;
                param0.mulPose(Vector3f.ZP.rotationDegrees(var7));
                param0.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
                float var8 = var6[0];
                float var9 = var6[1];
                float var10 = var6[2];
                Matrix4f var11 = param0.last().pose();
                var4.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
                var4.vertex(var11, 0.0F, 100.0F, 0.0F).color(var8, var9, var10, var6[3]).endVertex();
                int var12 = 16;

                for(int var13 = 0; var13 <= 16; ++var13) {
                    float var14 = (float)var13 * (float) (Math.PI * 2) / 16.0F;
                    float var15 = Mth.sin(var14);
                    float var16 = Mth.cos(var14);
                    var4.vertex(var11, var15 * 120.0F, var16 * 120.0F, -var16 * 40.0F * var6[3]).color(var6[0], var6[1], var6[2], 0.0F).endVertex();
                }

                var4.end();
                BufferUploader.end(var4);
                param0.popPose();
            }

            RenderSystem.enableTexture();
            RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
            );
            param0.pushPose();
            float var17 = 1.0F - this.level.getRainLevel(param2);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, var17);
            param0.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
            param0.mulPose(Vector3f.XP.rotationDegrees(this.level.getTimeOfDay(param2) * 360.0F));
            Matrix4f var18 = param0.last().pose();
            float var19 = 30.0F;
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, SUN_LOCATION);
            var4.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            var4.vertex(var18, -var19, 100.0F, -var19).uv(0.0F, 0.0F).endVertex();
            var4.vertex(var18, var19, 100.0F, -var19).uv(1.0F, 0.0F).endVertex();
            var4.vertex(var18, var19, 100.0F, var19).uv(1.0F, 1.0F).endVertex();
            var4.vertex(var18, -var19, 100.0F, var19).uv(0.0F, 1.0F).endVertex();
            var4.end();
            BufferUploader.end(var4);
            var19 = 20.0F;
            RenderSystem.setShaderTexture(0, MOON_LOCATION);
            int var20 = this.level.getMoonPhase();
            int var21 = var20 % 4;
            int var22 = var20 / 4 % 2;
            float var23 = (float)(var21 + 0) / 4.0F;
            float var24 = (float)(var22 + 0) / 2.0F;
            float var25 = (float)(var21 + 1) / 4.0F;
            float var26 = (float)(var22 + 1) / 2.0F;
            var4.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            var4.vertex(var18, -var19, -100.0F, var19).uv(var25, var26).endVertex();
            var4.vertex(var18, var19, -100.0F, var19).uv(var23, var26).endVertex();
            var4.vertex(var18, var19, -100.0F, -var19).uv(var23, var24).endVertex();
            var4.vertex(var18, -var19, -100.0F, -var19).uv(var25, var24).endVertex();
            var4.end();
            BufferUploader.end(var4);
            RenderSystem.disableTexture();
            float var27 = this.level.getStarBrightness(param2) * var17;
            if (var27 > 0.0F) {
                RenderSystem.setShaderColor(var27, var27, var27, var27);
                this.starBuffer.drawWithShader(param0.last().pose(), param1, GameRenderer.getPositionShader());
            }

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
            param0.popPose();
            RenderSystem.disableTexture();
            RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
            double var28 = this.minecraft.player.getEyePosition(param2).y - this.level.getLevelData().getHorizonHeight(this.level);
            if (var28 < 0.0) {
                param0.pushPose();
                param0.translate(0.0, 12.0, 0.0);
                this.darkBuffer.drawWithShader(param0.last().pose(), param1, var5);
                param0.popPose();
            }

            if (this.level.effects().hasGround()) {
                RenderSystem.setShaderColor(var1 * 0.2F + 0.04F, var2 * 0.2F + 0.04F, var3 * 0.6F + 0.1F, 1.0F);
            } else {
                RenderSystem.setShaderColor(var1, var2, var3, 1.0F);
            }

            RenderSystem.enableTexture();
            RenderSystem.depthMask(true);
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

                this.cloudBuffer = new VertexBuffer();
                this.buildClouds(var15, var5, var6, var7, var11);
                var15.end();
                this.cloudBuffer.upload(var15);
            }

            RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
            RenderSystem.setShaderTexture(0, CLOUDS_LOCATION);
            FogRenderer.levelFogColor();
            param0.pushPose();
            param0.scale(12.0F, 1.0F, 12.0F);
            param0.translate((double)(-var8), (double)var9, (double)(-var10));
            if (this.cloudBuffer != null) {
                int var16 = this.prevCloudsType == CloudStatus.FANCY ? 0 : 1;

                for(int var17 = var16; var17 < 2; ++var17) {
                    if (var17 == 0) {
                        RenderSystem.colorMask(false, false, false, false);
                    } else {
                        RenderSystem.colorMask(true, true, true, true);
                    }

                    ShaderInstance var18 = RenderSystem.getShader();
                    this.cloudBuffer.drawWithShader(param0.last().pose(), param1, var18);
                }
            }

            param0.popPose();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
        }
    }

    private void buildClouds(BufferBuilder param0, double param1, double param2, double param3, Vec3 param4) {
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

    }

    private void compileChunksUntil(long param0) {
        this.needsUpdate |= this.chunkRenderDispatcher.uploadAllPendingUploads();
        long var0 = Util.getNanos();
        int var1 = 0;
        if (!this.chunksToCompile.isEmpty()) {
            Iterator<ChunkRenderDispatcher.RenderChunk> var2 = this.chunksToCompile.iterator();

            while(var2.hasNext()) {
                ChunkRenderDispatcher.RenderChunk var3 = var2.next();
                if (var3.isDirtyFromPlayer()) {
                    this.chunkRenderDispatcher.rebuildChunkSync(var3);
                } else {
                    var3.rebuildChunkAsync(this.chunkRenderDispatcher);
                }

                var3.setNotDirty();
                var2.remove();
                ++var1;
                long var4 = Util.getNanos();
                long var5 = var4 - var0;
                long var6 = var5 / (long)var1;
                long var7 = param0 - var4;
                if (var7 < var6) {
                    break;
                }
            }
        }

    }

    private void renderWorldBorder(Camera param0) {
        BufferBuilder var0 = Tesselator.getInstance().getBuilder();
        WorldBorder var1 = this.level.getWorldBorder();
        double var2 = (double)(this.minecraft.options.renderDistance * 16);
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
            float var13 = 0.0F;
            float var14 = 0.0F;
            float var15 = (float)(var6 - Mth.frac(param0.getPosition().y));
            var0.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            double var16 = Math.max((double)Mth.floor(var5 - var2), var1.getMinZ());
            double var17 = Math.min((double)Mth.ceil(var5 + var2), var1.getMaxZ());
            if (var4 > var1.getMaxX() - var2) {
                float var18 = 0.0F;

                for(double var19 = var16; var19 < var17; var18 += 0.5F) {
                    double var20 = Math.min(1.0, var17 - var19);
                    float var21 = (float)var20 * 0.5F;
                    var0.vertex(var1.getMaxX() - var4, -var6, var19 - var5).uv(var12 - var18, var12 + var15).endVertex();
                    var0.vertex(var1.getMaxX() - var4, -var6, var19 + var20 - var5).uv(var12 - (var21 + var18), var12 + var15).endVertex();
                    var0.vertex(var1.getMaxX() - var4, var6, var19 + var20 - var5).uv(var12 - (var21 + var18), var12 + 0.0F).endVertex();
                    var0.vertex(var1.getMaxX() - var4, var6, var19 - var5).uv(var12 - var18, var12 + 0.0F).endVertex();
                    ++var19;
                }
            }

            if (var4 < var1.getMinX() + var2) {
                float var22 = 0.0F;

                for(double var23 = var16; var23 < var17; var22 += 0.5F) {
                    double var24 = Math.min(1.0, var17 - var23);
                    float var25 = (float)var24 * 0.5F;
                    var0.vertex(var1.getMinX() - var4, -var6, var23 - var5).uv(var12 + var22, var12 + var15).endVertex();
                    var0.vertex(var1.getMinX() - var4, -var6, var23 + var24 - var5).uv(var12 + var25 + var22, var12 + var15).endVertex();
                    var0.vertex(var1.getMinX() - var4, var6, var23 + var24 - var5).uv(var12 + var25 + var22, var12 + 0.0F).endVertex();
                    var0.vertex(var1.getMinX() - var4, var6, var23 - var5).uv(var12 + var22, var12 + 0.0F).endVertex();
                    ++var23;
                }
            }

            var16 = Math.max((double)Mth.floor(var4 - var2), var1.getMinX());
            var17 = Math.min((double)Mth.ceil(var4 + var2), var1.getMaxX());
            if (var5 > var1.getMaxZ() - var2) {
                float var26 = 0.0F;

                for(double var27 = var16; var27 < var17; var26 += 0.5F) {
                    double var28 = Math.min(1.0, var17 - var27);
                    float var29 = (float)var28 * 0.5F;
                    var0.vertex(var27 - var4, -var6, var1.getMaxZ() - var5).uv(var12 + var26, var12 + var15).endVertex();
                    var0.vertex(var27 + var28 - var4, -var6, var1.getMaxZ() - var5).uv(var12 + var29 + var26, var12 + var15).endVertex();
                    var0.vertex(var27 + var28 - var4, var6, var1.getMaxZ() - var5).uv(var12 + var29 + var26, var12 + 0.0F).endVertex();
                    var0.vertex(var27 - var4, var6, var1.getMaxZ() - var5).uv(var12 + var26, var12 + 0.0F).endVertex();
                    ++var27;
                }
            }

            if (var5 < var1.getMinZ() + var2) {
                float var30 = 0.0F;

                for(double var31 = var16; var31 < var17; var30 += 0.5F) {
                    double var32 = Math.min(1.0, var17 - var31);
                    float var33 = (float)var32 * 0.5F;
                    var0.vertex(var31 - var4, -var6, var1.getMinZ() - var5).uv(var12 - var30, var12 + var15).endVertex();
                    var0.vertex(var31 + var32 - var4, -var6, var1.getMinZ() - var5).uv(var12 - (var33 + var30), var12 + var15).endVertex();
                    var0.vertex(var31 + var32 - var4, var6, var1.getMinZ() - var5).uv(var12 - (var33 + var30), var12 + 0.0F).endVertex();
                    var0.vertex(var31 - var4, var6, var1.getMinZ() - var5).uv(var12 - var30, var12 + 0.0F).endVertex();
                    ++var31;
                }
            }

            var0.end();
            BufferUploader.end(var0);
            RenderSystem.enableCull();
            RenderSystem.polygonOffset(0.0F, 0.0F);
            RenderSystem.disablePolygonOffset();
            RenderSystem.disableBlend();
            var7.popPose();
            RenderSystem.applyModelViewMatrix();
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
        float param9
    ) {
        List<AABB> var0 = param2.toAabbs();
        int var1 = Mth.ceil((double)var0.size() / 3.0);

        for(int var2 = 0; var2 < var0.size(); ++var2) {
            AABB var3 = var0.get(var2);
            float var4 = ((float)var2 % (float)var1 + 1.0F) / (float)var1;
            float var5 = (float)(var2 / var1);
            float var6 = var4 * (float)(var5 == 0.0F ? 1 : 0);
            float var7 = var4 * (float)(var5 == 1.0F ? 1 : 0);
            float var8 = var4 * (float)(var5 == 2.0F ? 1 : 0);
            renderShape(param0, param1, Shapes.create(var3.move(0.0, 0.0, 0.0)), param3, param4, param5, var6, var7, var8, 1.0F);
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
        BufferBuilder param0,
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
        param0.vertex(param1, param2, param3).color(param7, param8, param9, param10).endVertex();
        param0.vertex(param1, param2, param3).color(param7, param8, param9, param10).endVertex();
        param0.vertex(param1, param2, param3).color(param7, param8, param9, param10).endVertex();
        param0.vertex(param1, param2, param6).color(param7, param8, param9, param10).endVertex();
        param0.vertex(param1, param5, param3).color(param7, param8, param9, param10).endVertex();
        param0.vertex(param1, param5, param6).color(param7, param8, param9, param10).endVertex();
        param0.vertex(param1, param5, param6).color(param7, param8, param9, param10).endVertex();
        param0.vertex(param1, param2, param6).color(param7, param8, param9, param10).endVertex();
        param0.vertex(param4, param5, param6).color(param7, param8, param9, param10).endVertex();
        param0.vertex(param4, param2, param6).color(param7, param8, param9, param10).endVertex();
        param0.vertex(param4, param2, param6).color(param7, param8, param9, param10).endVertex();
        param0.vertex(param4, param2, param3).color(param7, param8, param9, param10).endVertex();
        param0.vertex(param4, param5, param6).color(param7, param8, param9, param10).endVertex();
        param0.vertex(param4, param5, param3).color(param7, param8, param9, param10).endVertex();
        param0.vertex(param4, param5, param3).color(param7, param8, param9, param10).endVertex();
        param0.vertex(param4, param2, param3).color(param7, param8, param9, param10).endVertex();
        param0.vertex(param1, param5, param3).color(param7, param8, param9, param10).endVertex();
        param0.vertex(param1, param2, param3).color(param7, param8, param9, param10).endVertex();
        param0.vertex(param1, param2, param3).color(param7, param8, param9, param10).endVertex();
        param0.vertex(param4, param2, param3).color(param7, param8, param9, param10).endVertex();
        param0.vertex(param1, param2, param6).color(param7, param8, param9, param10).endVertex();
        param0.vertex(param4, param2, param6).color(param7, param8, param9, param10).endVertex();
        param0.vertex(param4, param2, param6).color(param7, param8, param9, param10).endVertex();
        param0.vertex(param1, param5, param3).color(param7, param8, param9, param10).endVertex();
        param0.vertex(param1, param5, param3).color(param7, param8, param9, param10).endVertex();
        param0.vertex(param1, param5, param6).color(param7, param8, param9, param10).endVertex();
        param0.vertex(param4, param5, param3).color(param7, param8, param9, param10).endVertex();
        param0.vertex(param4, param5, param6).color(param7, param8, param9, param10).endVertex();
        param0.vertex(param4, param5, param6).color(param7, param8, param9, param10).endVertex();
        param0.vertex(param4, param5, param6).color(param7, param8, param9, param10).endVertex();
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

            SoundInstance var5 = SimpleSoundInstance.forRecord(param0, (double)param1.getX(), (double)param1.getY(), (double)param1.getZ());
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
            var2.setDetail("ID", Registry.PARTICLE_TYPE.getKey(param0.getType()));
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
        if (this.minecraft != null && var0.isInitialized() && this.minecraft.particleEngine != null) {
            ParticleStatus var1 = this.calculateParticleLevel(param2);
            if (param1) {
                return this.minecraft.particleEngine.createParticle(param0, param3, param4, param5, param6, param7, param8);
            } else if (var0.getPosition().distanceToSqr(param3, param4, param5) > 1024.0) {
                return null;
            } else {
                return var1 == ParticleStatus.MINIMAL
                    ? null
                    : this.minecraft.particleEngine.createParticle(param0, param3, param4, param5, param6, param7, param8);
            }
        } else {
            return null;
        }
    }

    private ParticleStatus calculateParticleLevel(boolean param0) {
        ParticleStatus var0 = this.minecraft.options.particles;
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

    public void levelEvent(Player param0, int param1, BlockPos param2, int param3) {
        Random var0 = this.level.random;
        switch(param1) {
            case 1000:
                this.level.playLocalSound(param2, SoundEvents.DISPENSER_DISPENSE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
                break;
            case 1001:
                this.level.playLocalSound(param2, SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 1.0F, 1.2F, false);
                break;
            case 1002:
                this.level.playLocalSound(param2, SoundEvents.DISPENSER_LAUNCH, SoundSource.BLOCKS, 1.0F, 1.2F, false);
                break;
            case 1003:
                this.level.playLocalSound(param2, SoundEvents.ENDER_EYE_LAUNCH, SoundSource.NEUTRAL, 1.0F, 1.2F, false);
                break;
            case 1004:
                this.level.playLocalSound(param2, SoundEvents.FIREWORK_ROCKET_SHOOT, SoundSource.NEUTRAL, 1.0F, 1.2F, false);
                break;
            case 1005:
                this.level.playLocalSound(param2, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1.0F, var0.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1006:
                this.level.playLocalSound(param2, SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 1.0F, var0.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1007:
                this.level.playLocalSound(param2, SoundEvents.WOODEN_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.0F, var0.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1008:
                this.level.playLocalSound(param2, SoundEvents.FENCE_GATE_OPEN, SoundSource.BLOCKS, 1.0F, var0.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1009:
                if (param3 == 0) {
                    this.level
                        .playLocalSound(
                            param2, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (var0.nextFloat() - var0.nextFloat()) * 0.8F, false
                        );
                } else if (param3 == 1) {
                    this.level
                        .playLocalSound(
                            param2, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.7F, 1.6F + (var0.nextFloat() - var0.nextFloat()) * 0.4F, false
                        );
                }
                break;
            case 1010:
                if (Item.byId(param3) instanceof RecordItem) {
                    this.playStreamingMusic(((RecordItem)Item.byId(param3)).getSound(), param2);
                } else {
                    this.playStreamingMusic(null, param2);
                }
                break;
            case 1011:
                this.level.playLocalSound(param2, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1.0F, var0.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1012:
                this.level.playLocalSound(param2, SoundEvents.WOODEN_DOOR_CLOSE, SoundSource.BLOCKS, 1.0F, var0.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1013:
                this.level.playLocalSound(param2, SoundEvents.WOODEN_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1.0F, var0.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1014:
                this.level.playLocalSound(param2, SoundEvents.FENCE_GATE_CLOSE, SoundSource.BLOCKS, 1.0F, var0.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1015:
                this.level
                    .playLocalSound(param2, SoundEvents.GHAST_WARN, SoundSource.HOSTILE, 10.0F, (var0.nextFloat() - var0.nextFloat()) * 0.2F + 1.0F, false);
                break;
            case 1016:
                this.level
                    .playLocalSound(param2, SoundEvents.GHAST_SHOOT, SoundSource.HOSTILE, 10.0F, (var0.nextFloat() - var0.nextFloat()) * 0.2F + 1.0F, false);
                break;
            case 1017:
                this.level
                    .playLocalSound(
                        param2, SoundEvents.ENDER_DRAGON_SHOOT, SoundSource.HOSTILE, 10.0F, (var0.nextFloat() - var0.nextFloat()) * 0.2F + 1.0F, false
                    );
                break;
            case 1018:
                this.level
                    .playLocalSound(param2, SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 2.0F, (var0.nextFloat() - var0.nextFloat()) * 0.2F + 1.0F, false);
                break;
            case 1019:
                this.level
                    .playLocalSound(
                        param2, SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, SoundSource.HOSTILE, 2.0F, (var0.nextFloat() - var0.nextFloat()) * 0.2F + 1.0F, false
                    );
                break;
            case 1020:
                this.level
                    .playLocalSound(
                        param2, SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, SoundSource.HOSTILE, 2.0F, (var0.nextFloat() - var0.nextFloat()) * 0.2F + 1.0F, false
                    );
                break;
            case 1021:
                this.level
                    .playLocalSound(
                        param2, SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.HOSTILE, 2.0F, (var0.nextFloat() - var0.nextFloat()) * 0.2F + 1.0F, false
                    );
                break;
            case 1022:
                this.level
                    .playLocalSound(
                        param2, SoundEvents.WITHER_BREAK_BLOCK, SoundSource.HOSTILE, 2.0F, (var0.nextFloat() - var0.nextFloat()) * 0.2F + 1.0F, false
                    );
                break;
            case 1024:
                this.level
                    .playLocalSound(param2, SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 2.0F, (var0.nextFloat() - var0.nextFloat()) * 0.2F + 1.0F, false);
                break;
            case 1025:
                this.level
                    .playLocalSound(param2, SoundEvents.BAT_TAKEOFF, SoundSource.NEUTRAL, 0.05F, (var0.nextFloat() - var0.nextFloat()) * 0.2F + 1.0F, false);
                break;
            case 1026:
                this.level
                    .playLocalSound(param2, SoundEvents.ZOMBIE_INFECT, SoundSource.HOSTILE, 2.0F, (var0.nextFloat() - var0.nextFloat()) * 0.2F + 1.0F, false);
                break;
            case 1027:
                this.level
                    .playLocalSound(
                        param2, SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.NEUTRAL, 2.0F, (var0.nextFloat() - var0.nextFloat()) * 0.2F + 1.0F, false
                    );
                break;
            case 1029:
                this.level.playLocalSound(param2, SoundEvents.ANVIL_DESTROY, SoundSource.BLOCKS, 1.0F, var0.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1030:
                this.level.playLocalSound(param2, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F, var0.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1031:
                this.level.playLocalSound(param2, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.3F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1032:
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forLocalAmbience(SoundEvents.PORTAL_TRAVEL, var0.nextFloat() * 0.4F + 0.8F, 0.25F));
                break;
            case 1033:
                this.level.playLocalSound(param2, SoundEvents.CHORUS_FLOWER_GROW, SoundSource.BLOCKS, 1.0F, 1.0F, false);
                break;
            case 1034:
                this.level.playLocalSound(param2, SoundEvents.CHORUS_FLOWER_DEATH, SoundSource.BLOCKS, 1.0F, 1.0F, false);
                break;
            case 1035:
                this.level.playLocalSound(param2, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 1.0F, false);
                break;
            case 1036:
                this.level.playLocalSound(param2, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1.0F, var0.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1037:
                this.level.playLocalSound(param2, SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.0F, var0.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1039:
                this.level.playLocalSound(param2, SoundEvents.PHANTOM_BITE, SoundSource.HOSTILE, 0.3F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1040:
                this.level
                    .playLocalSound(
                        param2, SoundEvents.ZOMBIE_CONVERTED_TO_DROWNED, SoundSource.NEUTRAL, 2.0F, (var0.nextFloat() - var0.nextFloat()) * 0.2F + 1.0F, false
                    );
                break;
            case 1041:
                this.level
                    .playLocalSound(
                        param2, SoundEvents.HUSK_CONVERTED_TO_ZOMBIE, SoundSource.NEUTRAL, 2.0F, (var0.nextFloat() - var0.nextFloat()) * 0.2F + 1.0F, false
                    );
                break;
            case 1042:
                this.level.playLocalSound(param2, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1043:
                this.level.playLocalSound(param2, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1044:
                this.level.playLocalSound(param2, SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1045:
                this.level
                    .playLocalSound(param2, SoundEvents.POINTED_DRIPSTONE_LAND, SoundSource.BLOCKS, 2.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1046:
                this.level
                    .playLocalSound(
                        param2,
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
                        param2,
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
                        param2, SoundEvents.SKELETON_CONVERTED_TO_STRAY, SoundSource.NEUTRAL, 2.0F, (var0.nextFloat() - var0.nextFloat()) * 0.2F + 1.0F, false
                    );
                break;
            case 1500:
                ComposterBlock.handleFill(this.level, param2, param3 > 0);
                break;
            case 1501:
                this.level
                    .playLocalSound(param2, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (var0.nextFloat() - var0.nextFloat()) * 0.8F, false);

                for(int var41 = 0; var41 < 8; ++var41) {
                    this.level
                        .addParticle(
                            ParticleTypes.LARGE_SMOKE,
                            (double)param2.getX() + var0.nextDouble(),
                            (double)param2.getY() + 1.2,
                            (double)param2.getZ() + var0.nextDouble(),
                            0.0,
                            0.0,
                            0.0
                        );
                }
                break;
            case 1502:
                this.level
                    .playLocalSound(
                        param2, SoundEvents.REDSTONE_TORCH_BURNOUT, SoundSource.BLOCKS, 0.5F, 2.6F + (var0.nextFloat() - var0.nextFloat()) * 0.8F, false
                    );

                for(int var42 = 0; var42 < 5; ++var42) {
                    double var43 = (double)param2.getX() + var0.nextDouble() * 0.6 + 0.2;
                    double var44 = (double)param2.getY() + var0.nextDouble() * 0.6 + 0.2;
                    double var45 = (double)param2.getZ() + var0.nextDouble() * 0.6 + 0.2;
                    this.level.addParticle(ParticleTypes.SMOKE, var43, var44, var45, 0.0, 0.0, 0.0);
                }
                break;
            case 1503:
                this.level.playLocalSound(param2, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 1.0F, 1.0F, false);

                for(int var46 = 0; var46 < 16; ++var46) {
                    double var47 = (double)param2.getX() + (5.0 + var0.nextDouble() * 6.0) / 16.0;
                    double var48 = (double)param2.getY() + 0.8125;
                    double var49 = (double)param2.getZ() + (5.0 + var0.nextDouble() * 6.0) / 16.0;
                    this.level.addParticle(ParticleTypes.SMOKE, var47, var48, var49, 0.0, 0.0, 0.0);
                }
                break;
            case 1504:
                PointedDripstoneBlock.spawnDripParticle(this.level, param2, this.level.getBlockState(param2));
                break;
            case 1505:
                BoneMealItem.addGrowthParticles(this.level, param2, param3);
                this.level.playLocalSound(param2, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
                break;
            case 2000:
                Direction var1 = Direction.from3DDataValue(param3);
                int var2 = var1.getStepX();
                int var3 = var1.getStepY();
                int var4 = var1.getStepZ();
                double var5 = (double)param2.getX() + (double)var2 * 0.6 + 0.5;
                double var6 = (double)param2.getY() + (double)var3 * 0.6 + 0.5;
                double var7 = (double)param2.getZ() + (double)var4 * 0.6 + 0.5;

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
                BlockState var35 = Block.stateById(param3);
                if (!var35.isAir()) {
                    SoundType var36 = var35.getSoundType();
                    this.level
                        .playLocalSound(param2, var36.getBreakSound(), SoundSource.BLOCKS, (var36.getVolume() + 1.0F) / 2.0F, var36.getPitch() * 0.8F, false);
                }

                this.level.addDestroyBlockEffect(param2, var35);
                break;
            case 2002:
            case 2007:
                Vec3 var21 = Vec3.atBottomCenterOf(param2);

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

                float var23 = (float)(param3 >> 16 & 0xFF) / 255.0F;
                float var24 = (float)(param3 >> 8 & 0xFF) / 255.0F;
                float var25 = (float)(param3 >> 0 & 0xFF) / 255.0F;
                ParticleOptions var26 = param1 == 2007 ? ParticleTypes.INSTANT_EFFECT : ParticleTypes.EFFECT;

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

                this.level.playLocalSound(param2, SoundEvents.SPLASH_POTION_BREAK, SoundSource.NEUTRAL, 1.0F, var0.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 2003:
                double var16 = (double)param2.getX() + 0.5;
                double var17 = (double)param2.getY();
                double var18 = (double)param2.getZ() + 0.5;

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
                for(int var37 = 0; var37 < 20; ++var37) {
                    double var38 = (double)param2.getX() + 0.5 + (var0.nextDouble() - 0.5) * 2.0;
                    double var39 = (double)param2.getY() + 0.5 + (var0.nextDouble() - 0.5) * 2.0;
                    double var40 = (double)param2.getZ() + 0.5 + (var0.nextDouble() - 0.5) * 2.0;
                    this.level.addParticle(ParticleTypes.SMOKE, var38, var39, var40, 0.0, 0.0, 0.0);
                    this.level.addParticle(ParticleTypes.FLAME, var38, var39, var40, 0.0, 0.0, 0.0);
                }
                break;
            case 2005:
                BoneMealItem.addGrowthParticles(this.level, param2, param3);
                break;
            case 2006:
                for(int var50 = 0; var50 < 200; ++var50) {
                    float var51 = var0.nextFloat() * 4.0F;
                    float var52 = var0.nextFloat() * (float) (Math.PI * 2);
                    double var53 = (double)(Mth.cos(var52) * var51);
                    double var54 = 0.01 + var0.nextDouble() * 0.5;
                    double var55 = (double)(Mth.sin(var52) * var51);
                    Particle var56 = this.addParticleInternal(
                        ParticleTypes.DRAGON_BREATH,
                        false,
                        (double)param2.getX() + var53 * 0.1,
                        (double)param2.getY() + 0.3,
                        (double)param2.getZ() + var55 * 0.1,
                        var53,
                        var54,
                        var55
                    );
                    if (var56 != null) {
                        var56.setPower(var51);
                    }
                }

                if (param3 == 1) {
                    this.level.playLocalSound(param2, SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.HOSTILE, 1.0F, var0.nextFloat() * 0.1F + 0.9F, false);
                }
                break;
            case 2008:
                this.level
                    .addParticle(ParticleTypes.EXPLOSION, (double)param2.getX() + 0.5, (double)param2.getY() + 0.5, (double)param2.getZ() + 0.5, 0.0, 0.0, 0.0);
                break;
            case 2009:
                for(int var57 = 0; var57 < 8; ++var57) {
                    this.level
                        .addParticle(
                            ParticleTypes.CLOUD,
                            (double)param2.getX() + var0.nextDouble(),
                            (double)param2.getY() + 1.2,
                            (double)param2.getZ() + var0.nextDouble(),
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
                        (double)param2.getX() + 0.5,
                        (double)param2.getY() + 0.5,
                        (double)param2.getZ() + 0.5,
                        0.0,
                        0.0,
                        0.0
                    );
                this.level
                    .playLocalSound(
                        param2,
                        SoundEvents.END_GATEWAY_SPAWN,
                        SoundSource.BLOCKS,
                        10.0F,
                        (1.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 0.7F,
                        false
                    );
                break;
            case 3001:
                this.level
                    .playLocalSound(param2, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 64.0F, 0.8F + this.level.random.nextFloat() * 0.3F, false);
                break;
            case 3002:
                if (param3 >= 0 && param3 < Direction.Axis.VALUES.length) {
                    ParticleUtils.spawnParticlesAlongAxis(
                        Direction.Axis.VALUES[param3], this.level, param2, 0.125, ParticleTypes.ELECTRIC_SPARK, UniformInt.of(10, 19)
                    );
                } else {
                    ParticleUtils.spawnParticlesOnBlockFaces(this.level, param2, ParticleTypes.ELECTRIC_SPARK, UniformInt.of(3, 5));
                }
                break;
            case 3003:
                ParticleUtils.spawnParticlesOnBlockFaces(this.level, param2, ParticleTypes.WAX_ON, UniformInt.of(3, 5));
                this.level.playLocalSound(param2, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0F, 1.0F, false);
                break;
            case 3004:
                ParticleUtils.spawnParticlesOnBlockFaces(this.level, param2, ParticleTypes.WAX_OFF, UniformInt.of(3, 5));
                break;
            case 3005:
                ParticleUtils.spawnParticlesOnBlockFaces(this.level, param2, ParticleTypes.SCRAPE, UniformInt.of(3, 5));
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

    public boolean hasRenderedAllChunks() {
        return this.chunksToCompile.isEmpty() && this.chunkRenderDispatcher.isQueueEmpty();
    }

    public void needsUpdate() {
        this.needsUpdate = true;
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
    static class RenderChunkInfo {
        final ChunkRenderDispatcher.RenderChunk chunk;
        private byte sourceDirections;
        byte directions;
        final int step;

        RenderChunkInfo(ChunkRenderDispatcher.RenderChunk param0, @Nullable Direction param1, int param2) {
            this.chunk = param0;
            if (param1 != null) {
                this.addSourceDirection(param1);
            }

            this.step = param2;
        }

        public void setDirections(byte param0, Direction param1) {
            this.directions = (byte)(this.directions | param0 | 1 << param1.ordinal());
        }

        public boolean hasDirection(Direction param0) {
            return (this.directions & 1 << param0.ordinal()) > 0;
        }

        public void addSourceDirection(Direction param0) {
            this.sourceDirections = (byte)(this.sourceDirections | this.sourceDirections | 1 << param0.ordinal());
        }

        public boolean hasSourceDirection(int param0) {
            return (this.sourceDirections & 1 << param0) > 0;
        }

        public boolean hasSourceDirections() {
            return this.sourceDirections != 0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class RenderInfoMap {
        private final LevelRenderer.RenderChunkInfo[] infos;
        private final LevelRenderer.RenderChunkInfo[] blank;

        RenderInfoMap(int param0) {
            this.infos = new LevelRenderer.RenderChunkInfo[param0];
            this.blank = new LevelRenderer.RenderChunkInfo[param0];
        }

        void clear() {
            System.arraycopy(this.blank, 0, this.infos, 0, this.infos.length);
        }

        public void put(ChunkRenderDispatcher.RenderChunk param0, LevelRenderer.RenderChunkInfo param1) {
            this.infos[param0.index] = param1;
        }

        public LevelRenderer.RenderChunkInfo get(ChunkRenderDispatcher.RenderChunk param0) {
            return this.infos[param0.index];
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class TransparencyShaderException extends RuntimeException {
        public TransparencyShaderException(String param0, Throwable param1) {
            super(param0, param1);
        }
    }
}
