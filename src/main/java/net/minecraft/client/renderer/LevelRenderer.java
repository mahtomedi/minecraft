package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BreakingTextureGenerator;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import java.io.IOException;
import java.util.Collection;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.MultiPlayerLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
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
public class LevelRenderer implements AutoCloseable, ResourceManagerReloadListener {
    private static final Logger LOGGER = LogManager.getLogger();
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
    private final RenderBuffers renderBuffers;
    private final FogRenderer fog;
    private MultiPlayerLevel level;
    private Set<ChunkRenderDispatcher.RenderChunk> chunksToCompile = Sets.newLinkedHashSet();
    private List<LevelRenderer.RenderChunkInfo> renderChunks = Lists.newArrayListWithCapacity(69696);
    private final Set<BlockEntity> globalBlockEntities = Sets.newHashSet();
    private ViewArea viewArea;
    private final VertexFormat skyFormat = DefaultVertexFormat.POSITION;
    private VertexBuffer starBuffer;
    private VertexBuffer skyBuffer;
    private VertexBuffer darkBuffer;
    private boolean generateClouds = true;
    private VertexBuffer cloudBuffer;
    private int ticks;
    private final Int2ObjectMap<BlockDestructionProgress> destroyingBlocks = new Int2ObjectOpenHashMap<>();
    private final Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress = new Long2ObjectOpenHashMap<>();
    private final Map<BlockPos, SoundInstance> playingRecords = Maps.newHashMap();
    private RenderTarget entityTarget;
    private PostChain entityEffect;
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
    private final VertexFormat format = DefaultVertexFormat.BLOCK;
    private int lastViewDistance = -1;
    private int renderedEntities;
    private int culledEntities;
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
        this.renderBuffers = param1;
        this.fog = new FogRenderer();
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
            RenderSystem.normal3f(0.0F, 1.0F, 0.0F);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.defaultAlphaFunc();
            int var7 = 5;
            if (this.minecraft.options.fancyGraphics) {
                var7 = 10;
            }

            int var8 = -1;
            float var9 = (float)this.ticks + param1;
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
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
                                    this.minecraft.getTextureManager().bind(RAIN_LOCATION);
                                    var6.begin(7, DefaultVertexFormat.PARTICLE);
                                }

                                int var23 = this.ticks + var12 * var12 * 3121 + var12 * 45238971 + var11 * var11 * 418711 + var11 * 13761 & 31;
                                float var24 = -((float)var23 + param1) / 32.0F * (3.0F + var21.nextFloat());
                                double var25 = (double)((float)var12 + 0.5F) - param2;
                                double var26 = (double)((float)var11 + 0.5F) - param4;
                                float var27 = Mth.sqrt(var25 * var25 + var26 * var26) / (float)var7;
                                float var28 = ((1.0F - var27 * var27) * 0.5F + 0.5F) * var0;
                                var10.set(var12, var20, var11);
                                int var29 = var1.getLightColor(var10);
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
                                    this.minecraft.getTextureManager().bind(SNOW_LOCATION);
                                    var6.begin(7, DefaultVertexFormat.PARTICLE);
                                }

                                float var30 = -((float)(this.ticks & 511) + param1) / 512.0F;
                                float var31 = (float)(var21.nextDouble() + (double)var9 * 0.01 * (double)((float)var21.nextGaussian()));
                                float var32 = (float)(var21.nextDouble() + (double)(var9 * (float)var21.nextGaussian()) * 0.001);
                                double var33 = (double)((float)var12 + 0.5F) - param2;
                                double var34 = (double)((float)var11 + 0.5F) - param4;
                                float var35 = Mth.sqrt(var33 * var33 + var34 * var34) / (float)var7;
                                float var36 = ((1.0F - var35 * var35) * 0.3F + 0.5F) * var0;
                                var10.set(var12, var20, var11);
                                int var37 = var1.getLightColor(var10);
                                int var38 = var37 >> 16 & 65535;
                                int var39 = (var37 & 65535) * 3;
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
            RenderSystem.defaultAlphaFunc();
            param0.turnOffLightLayer();
        }
    }

    public void tickRain(Camera param0) {
        float var0 = this.minecraft.level.getRainLevel(1.0F);
        if (!this.minecraft.options.fancyGraphics) {
            var0 /= 2.0F;
        }

        if (var0 != 0.0F) {
            Random var1 = new Random((long)this.ticks * 312987231L);
            LevelReader var2 = this.minecraft.level;
            BlockPos var3 = new BlockPos(param0.getPosition());
            int var4 = 10;
            double var5 = 0.0;
            double var6 = 0.0;
            double var7 = 0.0;
            int var8 = 0;
            int var9 = (int)(100.0F * var0 * var0);
            if (this.minecraft.options.particles == ParticleStatus.DECREASED) {
                var9 >>= 1;
            } else if (this.minecraft.options.particles == ParticleStatus.MINIMAL) {
                var9 = 0;
            }

            for(int var10 = 0; var10 < var9; ++var10) {
                BlockPos var11 = var2.getHeightmapPos(
                    Heightmap.Types.MOTION_BLOCKING, var3.offset(var1.nextInt(10) - var1.nextInt(10), 0, var1.nextInt(10) - var1.nextInt(10))
                );
                Biome var12 = var2.getBiome(var11);
                BlockPos var13 = var11.below();
                if (var11.getY() <= var3.getY() + 10
                    && var11.getY() >= var3.getY() - 10
                    && var12.getPrecipitation() == Biome.Precipitation.RAIN
                    && var12.getTemperature(var11) >= 0.15F) {
                    double var14 = var1.nextDouble();
                    double var15 = var1.nextDouble();
                    BlockState var16 = var2.getBlockState(var13);
                    FluidState var17 = var2.getFluidState(var11);
                    VoxelShape var18 = var16.getCollisionShape(var2, var13);
                    double var19 = var18.max(Direction.Axis.Y, var14, var15);
                    double var20 = (double)var17.getHeight(var2, var11);
                    double var21;
                    double var22;
                    if (var19 >= var20) {
                        var21 = var19;
                        var22 = var18.min(Direction.Axis.Y, var14, var15);
                    } else {
                        var21 = 0.0;
                        var22 = 0.0;
                    }

                    if (var21 > -Double.MAX_VALUE) {
                        if (!var17.is(FluidTags.LAVA)
                            && var16.getBlock() != Blocks.MAGMA_BLOCK
                            && (var16.getBlock() != Blocks.CAMPFIRE || !var16.getValue(CampfireBlock.LIT))) {
                            if (var1.nextInt(++var8) == 0) {
                                var5 = (double)var13.getX() + var14;
                                var6 = (double)((float)var13.getY() + 0.1F) + var21 - 1.0;
                                var7 = (double)var13.getZ() + var15;
                            }

                            this.minecraft
                                .level
                                .addParticle(
                                    ParticleTypes.RAIN,
                                    (double)var13.getX() + var14,
                                    (double)((float)var13.getY() + 0.1F) + var21,
                                    (double)var13.getZ() + var15,
                                    0.0,
                                    0.0,
                                    0.0
                                );
                        } else {
                            this.minecraft
                                .level
                                .addParticle(
                                    ParticleTypes.SMOKE,
                                    (double)var11.getX() + var14,
                                    (double)((float)var11.getY() + 0.1F) - var22,
                                    (double)var11.getZ() + var15,
                                    0.0,
                                    0.0,
                                    0.0
                                );
                        }
                    }
                }
            }

            if (var8 > 0 && var1.nextInt(3) < this.rainSoundTime++) {
                this.rainSoundTime = 0;
                if (var6 > (double)(var3.getY() + 1) && var2.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, var3).getY() > Mth.floor((float)var3.getY())) {
                    this.minecraft.level.playLocalSound(var5, var6, var7, SoundEvents.WEATHER_RAIN_ABOVE, SoundSource.WEATHER, 0.1F, 0.5F, false);
                } else {
                    this.minecraft.level.playLocalSound(var5, var6, var7, SoundEvents.WEATHER_RAIN, SoundSource.WEATHER, 0.2F, 1.0F, false);
                }
            }

        }
    }

    @Override
    public void close() {
        if (this.entityEffect != null) {
            this.entityEffect.close();
        }

    }

    @Override
    public void onResourceManagerReload(ResourceManager param0) {
        this.textureManager.bind(FORCEFIELD_LOCATION);
        RenderSystem.texParameter(3553, 10242, 10497);
        RenderSystem.texParameter(3553, 10243, 10497);
        RenderSystem.bindTexture(0);
        this.initOutline();
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
            LOGGER.warn("Failed to load shader: {}", var0, var4);
            this.entityEffect = null;
            this.entityTarget = null;
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
        return this.entityTarget != null && this.entityEffect != null && this.minecraft.player != null;
    }

    private void createDarkSky() {
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();
        if (this.darkBuffer != null) {
            this.darkBuffer.delete();
        }

        this.darkBuffer = new VertexBuffer(this.skyFormat);
        this.drawSkyHemisphere(var1, -16.0F, true);
        var1.end();
        this.darkBuffer.upload(var1);
    }

    private void createLightSky() {
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();
        if (this.skyBuffer != null) {
            this.skyBuffer.delete();
        }

        this.skyBuffer = new VertexBuffer(this.skyFormat);
        this.drawSkyHemisphere(var1, 16.0F, false);
        var1.end();
        this.skyBuffer.upload(var1);
    }

    private void drawSkyHemisphere(BufferBuilder param0, float param1, boolean param2) {
        int var0 = 64;
        int var1 = 6;
        param0.begin(7, DefaultVertexFormat.POSITION);

        for(int var2 = -384; var2 <= 384; var2 += 64) {
            for(int var3 = -384; var3 <= 384; var3 += 64) {
                float var4 = (float)var2;
                float var5 = (float)(var2 + 64);
                if (param2) {
                    var5 = (float)var2;
                    var4 = (float)(var2 + 64);
                }

                param0.vertex((double)var4, (double)param1, (double)var3).endVertex();
                param0.vertex((double)var5, (double)param1, (double)var3).endVertex();
                param0.vertex((double)var5, (double)param1, (double)(var3 + 64)).endVertex();
                param0.vertex((double)var4, (double)param1, (double)(var3 + 64)).endVertex();
            }
        }

    }

    private void createStars() {
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();
        if (this.starBuffer != null) {
            this.starBuffer.delete();
        }

        this.starBuffer = new VertexBuffer(this.skyFormat);
        this.drawStars(var1);
        var1.end();
        this.starBuffer.upload(var1);
    }

    private void drawStars(BufferBuilder param0) {
        Random var0 = new Random(10842L);
        param0.begin(7, DefaultVertexFormat.POSITION);

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

    public void setLevel(@Nullable MultiPlayerLevel param0) {
        this.lastCameraX = Double.MIN_VALUE;
        this.lastCameraY = Double.MIN_VALUE;
        this.lastCameraZ = Double.MIN_VALUE;
        this.lastCameraChunkX = Integer.MIN_VALUE;
        this.lastCameraChunkY = Integer.MIN_VALUE;
        this.lastCameraChunkZ = Integer.MIN_VALUE;
        this.entityRenderDispatcher.setLevel(param0);
        this.level = param0;
        if (param0 != null) {
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

    public void allChanged() {
        if (this.level != null) {
            if (this.chunkRenderDispatcher == null) {
                this.chunkRenderDispatcher = new ChunkRenderDispatcher(
                    this.level, this, Util.backgroundExecutor(), this.minecraft.is64Bit(), this.renderBuffers.fixedBufferPack()
                );
            } else {
                this.chunkRenderDispatcher.setLevel(this.level);
            }

            this.needsUpdate = true;
            this.generateClouds = true;
            RenderType.setFancy(this.minecraft.options.fancyGraphics);
            this.lastViewDistance = this.minecraft.options.renderDistance;
            if (this.viewArea != null) {
                this.viewArea.releaseAllBuffers();
            }

            this.resetChunksToCompile();
            synchronized(this.globalBlockEntities) {
                this.globalBlockEntities.clear();
            }

            this.viewArea = new ViewArea(this.chunkRenderDispatcher, this.level, this.minecraft.options.renderDistance, this);
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

    protected int countRenderedChunks() {
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
        double var1 = this.minecraft.player.getX() - this.lastCameraX;
        double var2 = this.minecraft.player.getY() - this.lastCameraY;
        double var3 = this.minecraft.player.getZ() - this.lastCameraZ;
        if (this.lastCameraChunkX != this.minecraft.player.xChunk
            || this.lastCameraChunkY != this.minecraft.player.yChunk
            || this.lastCameraChunkZ != this.minecraft.player.zChunk
            || var1 * var1 + var2 * var2 + var3 * var3 > 16.0) {
            this.lastCameraX = this.minecraft.player.getX();
            this.lastCameraY = this.minecraft.player.getY();
            this.lastCameraZ = this.minecraft.player.getZ();
            this.lastCameraChunkX = this.minecraft.player.xChunk;
            this.lastCameraChunkY = this.minecraft.player.yChunk;
            this.lastCameraChunkZ = this.minecraft.player.zChunk;
            this.viewArea.repositionCamera(this.minecraft.player.getX(), this.minecraft.player.getZ());
        }

        this.chunkRenderDispatcher.setCamera(var0);
        this.level.getProfiler().popPush("cull");
        this.minecraft.getProfiler().popPush("culling");
        BlockPos var4 = param0.getBlockPosition();
        ChunkRenderDispatcher.RenderChunk var5 = this.viewArea.getRenderChunkAt(var4);
        BlockPos var6 = new BlockPos(Mth.floor(var0.x / 16.0) * 16, Mth.floor(var0.y / 16.0) * 16, Mth.floor(var0.z / 16.0) * 16);
        float var7 = param0.getXRot();
        float var8 = param0.getYRot();
        this.needsUpdate = this.needsUpdate
            || !this.chunksToCompile.isEmpty()
            || var0.x != this.prevCamX
            || var0.y != this.prevCamY
            || var0.z != this.prevCamZ
            || (double)var7 != this.prevCamRotX
            || (double)var8 != this.prevCamRotY;
        this.prevCamX = var0.x;
        this.prevCamY = var0.y;
        this.prevCamZ = var0.z;
        this.prevCamRotX = (double)var7;
        this.prevCamRotY = (double)var8;
        this.minecraft.getProfiler().popPush("update");
        if (!param2 && this.needsUpdate) {
            this.needsUpdate = false;
            this.renderChunks = Lists.newArrayList();
            Queue<LevelRenderer.RenderChunkInfo> var9 = Queues.newArrayDeque();
            Entity.setViewScale(Mth.clamp((double)this.minecraft.options.renderDistance / 8.0, 1.0, 2.5));
            boolean var10 = this.minecraft.smartCull;
            if (var5 != null) {
                boolean var15 = false;
                LevelRenderer.RenderChunkInfo var16 = new LevelRenderer.RenderChunkInfo(var5, null, 0);
                Set<Direction> var17 = this.getVisibleDirections(var4);
                if (var17.size() == 1) {
                    Vec3 var18 = param0.getLookVector();
                    Direction var19 = Direction.getNearest(var18.x, var18.y, var18.z).getOpposite();
                    var17.remove(var19);
                }

                if (var17.isEmpty()) {
                    var15 = true;
                }

                if (var15 && !param4) {
                    this.renderChunks.add(var16);
                } else {
                    if (param4 && this.level.getBlockState(var4).isSolidRender(this.level, var4)) {
                        var10 = false;
                    }

                    var5.setFrame(param3);
                    var9.add(var16);
                }
            } else {
                int var11 = var4.getY() > 0 ? 248 : 8;

                for(int var12 = -this.lastViewDistance; var12 <= this.lastViewDistance; ++var12) {
                    for(int var13 = -this.lastViewDistance; var13 <= this.lastViewDistance; ++var13) {
                        ChunkRenderDispatcher.RenderChunk var14 = this.viewArea.getRenderChunkAt(new BlockPos((var12 << 4) + 8, var11, (var13 << 4) + 8));
                        if (var14 != null && param1.isVisible(var14.bb)) {
                            var14.setFrame(param3);
                            var9.add(new LevelRenderer.RenderChunkInfo(var14, null, 0));
                        }
                    }
                }
            }

            this.minecraft.getProfiler().push("iteration");

            while(!var9.isEmpty()) {
                LevelRenderer.RenderChunkInfo var20 = var9.poll();
                ChunkRenderDispatcher.RenderChunk var21 = var20.chunk;
                Direction var22 = var20.sourceDirection;
                this.renderChunks.add(var20);

                for(Direction var23 : DIRECTIONS) {
                    ChunkRenderDispatcher.RenderChunk var24 = this.getRelativeFrom(var6, var21, var23);
                    if ((!var10 || !var20.hasDirection(var23.getOpposite()))
                        && (!var10 || var22 == null || var21.getCompiledChunk().facesCanSeeEachother(var22.getOpposite(), var23))
                        && var24 != null
                        && var24.hasAllNeighbors()
                        && var24.setFrame(param3)
                        && param1.isVisible(var24.bb)) {
                        LevelRenderer.RenderChunkInfo var25 = new LevelRenderer.RenderChunkInfo(var24, var23, var20.step + 1);
                        var25.setDirections(var20.directions, var23);
                        var9.add(var25);
                    }
                }
            }

            this.minecraft.getProfiler().pop();
        }

        this.minecraft.getProfiler().popPush("rebuildNear");
        Set<ChunkRenderDispatcher.RenderChunk> var26 = this.chunksToCompile;
        this.chunksToCompile = Sets.newLinkedHashSet();

        for(LevelRenderer.RenderChunkInfo var27 : this.renderChunks) {
            ChunkRenderDispatcher.RenderChunk var28 = var27.chunk;
            if (var28.isDirty() || var26.contains(var28)) {
                this.needsUpdate = true;
                BlockPos var29 = var28.getOrigin().offset(8, 8, 8);
                boolean var30 = var29.distSqr(var4) < 768.0;
                if (!var28.isDirtyFromPlayer() && !var30) {
                    this.chunksToCompile.add(var28);
                } else {
                    this.minecraft.getProfiler().push("build near");
                    this.chunkRenderDispatcher.rebuildChunkSync(var28);
                    var28.setNotDirty();
                    this.minecraft.getProfiler().pop();
                }
            }
        }

        this.chunksToCompile.addAll(var26);
        this.minecraft.getProfiler().pop();
    }

    private Set<Direction> getVisibleDirections(BlockPos param0) {
        VisGraph var0 = new VisGraph();
        BlockPos var1 = new BlockPos(param0.getX() >> 4 << 4, param0.getY() >> 4 << 4, param0.getZ() >> 4 << 4);
        LevelChunk var2 = this.level.getChunkAt(var1);

        for(BlockPos var3 : BlockPos.betweenClosed(var1, var1.offset(15, 15, 15))) {
            if (var2.getBlockState(var3).isSolidRender(this.level, var3)) {
                var0.setOpaque(var3);
            }
        }

        return var0.floodFill(param0);
    }

    @Nullable
    private ChunkRenderDispatcher.RenderChunk getRelativeFrom(BlockPos param0, ChunkRenderDispatcher.RenderChunk param1, Direction param2) {
        BlockPos var0 = param1.getRelativeOrigin(param2);
        if (Mth.abs(param0.getX() - var0.getX()) > this.lastViewDistance * 16) {
            return null;
        } else if (var0.getY() < 0 || var0.getY() >= 256) {
            return null;
        } else {
            return Mth.abs(param0.getZ() - var0.getZ()) > this.lastViewDistance * 16 ? null : this.viewArea.getRenderChunkAt(var0);
        }
    }

    private void captureFrustum(Matrix4f param0, Matrix4f param1, double param2, double param3, double param4, Frustum param5) {
        this.capturedFrustum = param5;
        Matrix4f var0 = new Matrix4f(param1);
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

    public void renderLevel(PoseStack param0, float param1, long param2, boolean param3, Camera param4, GameRenderer param5, LightTexture param6) {
        BlockEntityRenderDispatcher.instance.prepare(this.level, this.minecraft.getTextureManager(), this.minecraft.font, param4, this.minecraft.hitResult);
        this.entityRenderDispatcher.prepare(this.level, param4, this.minecraft.crosshairPickEntity);
        ProfilerFiller var0 = this.level.getProfiler();
        var0.popPush("light_updates");
        this.minecraft.level.getChunkSource().getLightEngine().runUpdates(Integer.MAX_VALUE, true, true);
        Vec3 var1 = param4.getPosition();
        double var2 = var1.x();
        double var3 = var1.y();
        double var4 = var1.z();
        Matrix4f var5 = param0.getPose();
        Matrix4f var6 = param5.getProjectionMatrix(param4, param1, true, true, Mth.SQRT_OF_TWO);
        var0.popPush("culling");
        boolean var7 = this.capturedFrustum != null;
        Frustum var8;
        if (var7) {
            var8 = this.capturedFrustum;
            var8.prepare(this.frustumPos.x, this.frustumPos.y, this.frustumPos.z);
        } else {
            var8 = new Frustum(var5, var6);
            var8.prepare(var2, var3, var4);
        }

        this.minecraft.getProfiler().popPush("captureFrustum");
        if (this.captureFrustum) {
            this.captureFrustum(var5, var6, var1.x, var1.y, var1.z, var7 ? new Frustum(var5, var6) : var8);
            this.captureFrustum = false;
        }

        var0.popPush("clear");
        this.fog.setupClearColor(param4, param1, this.minecraft.level, this.minecraft.options.renderDistance, param5.getDarkenWorldAmount(param1));
        RenderSystem.clear(16640, Minecraft.ON_OSX);
        float var10 = param5.getRenderDistance();
        boolean var11 = this.minecraft.level.dimension.isFoggyAt(Mth.floor(var2), Mth.floor(var3))
            || this.minecraft.gui.getBossOverlay().shouldCreateWorldFog();
        if (this.minecraft.options.renderDistance >= 4) {
            FogRenderer.setupFog(param4, FogRenderer.FogMode.FOG_SKY, var10, var11);
            var0.popPush("sky");
            param5.resetProjectionMatrix(param4, param1, true, false, 2.0F);
            this.renderSky(param0, param1);
            param5.resetProjectionMatrix(param4, param1, true, false, Mth.SQRT_OF_TWO);
        }

        var0.popPush("fog");
        FogRenderer.setupFog(param4, FogRenderer.FogMode.FOG_TERRAIN, var10, var11);
        var0.popPush("terrain_setup");
        this.setupRender(param4, var8, var7, this.frameId++, this.minecraft.player.isSpectator());
        var0.popPush("updatechunks");
        this.compileChunksUntil(param2);
        var0.popPush("terrain");
        this.renderChunkLayer(RenderType.solid(), param0, var2, var3, var4);
        this.renderChunkLayer(RenderType.cutoutMipped(), param0, var2, var3, var4);
        this.renderChunkLayer(RenderType.cutout(), param0, var2, var3, var4);
        Lighting.setupLevel(param0.getPose());
        var0.popPush("entities");
        var0.push("prepare");
        this.renderedEntities = 0;
        this.culledEntities = 0;
        RenderSystem.defaultAlphaFunc();
        RenderSystem.enableAlphaTest();
        var0.popPush("entities");
        if (this.shouldShowEntityOutlines()) {
            var0.popPush("entityOutlines");
            this.entityTarget.clear(Minecraft.ON_OSX);
            this.minecraft.getMainRenderTarget().bindWrite(false);
        }

        boolean var12 = false;
        MultiBufferSource.BufferSource var13 = this.renderBuffers.bufferSource();

        for(Entity var14 : this.level.entitiesForRendering()) {
            if ((this.entityRenderDispatcher.shouldRender(var14, var8, var2, var3, var4) || var14.hasIndirectPassenger(this.minecraft.player))
                && (
                    var14 != param4.getEntity()
                        || param4.isDetached()
                        || param4.getEntity() instanceof LivingEntity && ((LivingEntity)param4.getEntity()).isSleeping()
                )
                && (!(var14 instanceof LocalPlayer) || param4.getEntity() == var14)) {
                ++this.renderedEntities;
                if (var14.tickCount == 0) {
                    var14.xOld = var14.getX();
                    var14.yOld = var14.getY();
                    var14.zOld = var14.getZ();
                }

                boolean var15 = this.shouldShowEntityOutlines()
                    && (
                        var14.isGlowing()
                            || var14 instanceof Player && this.minecraft.player.isSpectator() && this.minecraft.options.keySpectatorOutlines.isDown()
                    );
                MultiBufferSource var17;
                if (var15) {
                    var12 = true;
                    OutlineBufferSource var16 = this.renderBuffers.outlineBufferSource();
                    var17 = var16;
                    int var18 = var14.getTeamColor();
                    int var19 = 255;
                    int var20 = var18 >> 16 & 0xFF;
                    int var21 = var18 >> 8 & 0xFF;
                    int var22 = var18 & 0xFF;
                    var16.setColor(var20, var21, var22, 255);
                } else {
                    var17 = var13;
                }

                this.renderEntity(var14, var2, var3, var4, param1, param0, var17);
            }
        }

        this.checkPoseStack(param0);
        var0.popPush("blockentities");

        for(LevelRenderer.RenderChunkInfo var24 : this.renderChunks) {
            List<BlockEntity> var25 = var24.chunk.getCompiledChunk().getRenderableBlockEntities();
            if (!var25.isEmpty()) {
                for(BlockEntity var26 : var25) {
                    BlockPos var27 = var26.getBlockPos();
                    MultiBufferSource var28 = var13;
                    param0.pushPose();
                    param0.translate((double)var27.getX() - var2, (double)var27.getY() - var3, (double)var27.getZ() - var4);
                    SortedSet<BlockDestructionProgress> var29 = this.destructionProgress.get(var27.asLong());
                    if (var29 != null && !var29.isEmpty()) {
                        int var30 = var29.last().getProgress();
                        if (var30 >= 0) {
                            VertexConsumer var31 = new BreakingTextureGenerator(
                                this.renderBuffers.effectBufferSource().getBuffer(RenderType.crumbling(var30)), param0.getPose()
                            );
                            var28 = param2x -> {
                                VertexConsumer var0x = var13.getBuffer(param2x);
                                return (VertexConsumer)(param2x.affectsCrumbling() ? new VertexMultiConsumer(ImmutableList.of(var31, var0x)) : var0x);
                            };
                        }
                    }

                    BlockEntityRenderDispatcher.instance.render(var26, param1, param0, var28, var2, var3, var4);
                    param0.popPose();
                }
            }
        }

        synchronized(this.globalBlockEntities) {
            for(BlockEntity var32 : this.globalBlockEntities) {
                BlockPos var33 = var32.getBlockPos();
                param0.pushPose();
                param0.translate((double)var33.getX() - var2, (double)var33.getY() - var3, (double)var33.getZ() - var4);
                BlockEntityRenderDispatcher.instance.render(var32, param1, param0, var13, var2, var3, var4);
                param0.popPose();
            }
        }

        this.checkPoseStack(param0);
        var13.endBatch(RenderType.solid());
        var13.endBatch(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS));
        var13.endBatch(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS));
        this.renderBuffers.outlineBufferSource().endOutlineBatch();
        if (var12) {
            this.entityEffect.process(param1);
            this.minecraft.getMainRenderTarget().bindWrite(false);
        }

        var0.popPush("destroyProgress");

        for(Entry<SortedSet<BlockDestructionProgress>> var34 : this.destructionProgress.long2ObjectEntrySet()) {
            BlockPos var35 = BlockPos.of(var34.getLongKey());
            double var36 = (double)var35.getX() - var2;
            double var37 = (double)var35.getY() - var3;
            double var38 = (double)var35.getZ() - var4;
            if (!(var36 * var36 + var37 * var37 + var38 * var38 > 1024.0)) {
                param0.pushPose();
                param0.translate((double)(var35.getX() & -16) - var2, (double)(var35.getY() & -16) - var3, (double)(var35.getZ() & -16) - var4);
                SortedSet<BlockDestructionProgress> var39 = var34.getValue();
                if (var39 != null && !var39.isEmpty()) {
                    int var40 = var39.last().getProgress();
                    VertexConsumer var41 = new BreakingTextureGenerator(
                        this.renderBuffers.effectBufferSource().getBuffer(RenderType.crumbling(var40)), param0.getPose()
                    );
                    this.minecraft.getBlockRenderer().renderBreakingTexture(this.level.getBlockState(var35), var35, this.level, param0, var41);
                    param0.popPose();
                }
            }
        }

        this.checkPoseStack(param0);
        var0.pop();
        HitResult var42 = this.minecraft.hitResult;
        if (param3 && var42 != null && var42.getType() == HitResult.Type.BLOCK) {
            var0.popPush("outline");
            BlockPos var43 = ((BlockHitResult)var42).getBlockPos();
            BlockState var44 = this.level.getBlockState(var43);
            if (!var44.isAir() && this.level.getWorldBorder().isWithinBounds(var43)) {
                VertexConsumer var45 = var13.getBuffer(RenderType.lines());
                this.renderHitOutline(param0, var45, param4.getEntity(), var2, var3, var4, var43, var44);
            }
        }

        var13.endBatch();
        this.renderBuffers.effectBufferSource().endBatch();
        RenderSystem.pushMatrix();
        RenderSystem.multMatrix(param0.getPose());
        this.minecraft.debugRenderer.render(param2);
        this.renderWorldBounds(param4, param1);
        this.minecraft.getTextureManager().bind(TextureAtlas.LOCATION_BLOCKS);
        FogRenderer.setupFog(param4, FogRenderer.FogMode.FOG_TERRAIN, var10, var11);
        var0.popPush("translucent");
        this.renderChunkLayer(RenderType.translucent(), param0, var2, var3, var4);
        param6.turnOnLightLayer();
        FogRenderer.setupFog(param4, FogRenderer.FogMode.FOG_TERRAIN, var10, var11);
        var0.popPush("particles");
        RenderSystem.enableAlphaTest();
        RenderSystem.defaultAlphaFunc();
        RenderSystem.enableDepthTest();
        this.minecraft.particleEngine.render(param4, param1);
        param6.turnOffLightLayer();
        var0.popPush("cloudsLayers");
        if (this.minecraft.options.getCloudsType() != CloudStatus.OFF) {
            var0.popPush("clouds");
            param5.resetProjectionMatrix(param4, param1, true, false, 4.0F);
            FogRenderer.setupFog(param4, FogRenderer.FogMode.FOG_TERRAIN, param5.getRenderDistance(), var11);
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.enableAlphaTest();
            RenderSystem.enableDepthTest();
            RenderSystem.defaultAlphaFunc();
            RenderSystem.defaultBlendFunc();
            this.renderClouds(param0, param1, var2, var3, var4);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableAlphaTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.disableFog();
            param5.resetProjectionMatrix(param4, param1, true, false, Mth.SQRT_OF_TWO);
        }

        RenderSystem.depthMask(false);
        var0.popPush("weather");
        this.renderSnowAndRain(param6, param1, var2, var3, var4);
        RenderSystem.depthMask(true);
        this.renderDebug(param4);
        RenderSystem.shadeModel(7424);
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.disableFog();
        RenderSystem.popMatrix();
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
        float var3 = Mth.lerp(param4, param0.yRotO, param0.yRot);
        this.entityRenderDispatcher.render(param0, var0 - param1, var1 - param2, var2 - param3, var3, param4, param5, param6);
    }

    private void renderChunkLayer(RenderType param0, PoseStack param1, double param2, double param3, double param4) {
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
        List<ChunkRenderDispatcher.RenderChunk> var5 = Lists.newArrayList();

        for(LevelRenderer.RenderChunkInfo var6 : param0 == RenderType.translucent() ? Lists.reverse(this.renderChunks) : this.renderChunks) {
            ChunkRenderDispatcher.RenderChunk var7 = var6.chunk;
            if (!var7.getCompiledChunk().isEmpty(param0)) {
                var5.add(var7);
            }
        }

        this.minecraft.getProfiler().popPush(() -> "render_" + param0);

        for(ChunkRenderDispatcher.RenderChunk var8 : var5) {
            VertexBuffer var9 = var8.getBuffer(param0);
            param1.pushPose();
            BlockPos var10 = var8.getOrigin();
            param1.translate((double)var10.getX() - param2, (double)var10.getY() - param3, (double)var10.getZ() - param4);
            var9.bind();
            this.format.setupBufferState(0L);
            var9.draw(param1.getPose(), 7);
            param1.popPose();
        }

        VertexBuffer.unbind();
        RenderSystem.clearCurrentColor();
        this.format.clearBufferState();
        this.minecraft.getProfiler().pop();
        param0.clearRenderState();
    }

    private void renderDebug(Camera param0) {
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();
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
                RenderSystem.pushMatrix();
                BlockPos var7 = var6.getOrigin();
                RenderSystem.translated((double)var7.getX() - var2, (double)var7.getY() - var3, (double)var7.getZ() - var4);
                if (this.minecraft.chunkPath) {
                    var1.begin(1, DefaultVertexFormat.POSITION_COLOR);
                    RenderSystem.lineWidth(10.0F);
                    int var8 = var5.step == 0 ? 0 : Mth.hsvToRgb((float)var5.step / 50.0F, 0.9F, 0.9F);
                    int var9 = var8 >> 16 & 0xFF;
                    int var10 = var8 >> 8 & 0xFF;
                    int var11 = var8 & 0xFF;
                    Direction var12 = var5.sourceDirection;
                    if (var12 != null) {
                        var1.vertex(8.0, 8.0, 8.0).color(var9, var10, var11, 255).endVertex();
                        var1.vertex((double)(8 - 16 * var12.getStepX()), (double)(8 - 16 * var12.getStepY()), (double)(8 - 16 * var12.getStepZ()))
                            .color(var9, var10, var11, 255)
                            .endVertex();
                    }

                    var0.end();
                    RenderSystem.lineWidth(1.0F);
                }

                if (this.minecraft.chunkVisibility && !var6.getCompiledChunk().hasNoRenderableLayers()) {
                    var1.begin(1, DefaultVertexFormat.POSITION_COLOR);
                    RenderSystem.lineWidth(10.0F);
                    int var13 = 0;

                    for(Direction var14 : Direction.values()) {
                        for(Direction var15 : Direction.values()) {
                            boolean var16 = var6.getCompiledChunk().facesCanSeeEachother(var14, var15);
                            if (!var16) {
                                ++var13;
                                var1.vertex((double)(8 + 8 * var14.getStepX()), (double)(8 + 8 * var14.getStepY()), (double)(8 + 8 * var14.getStepZ()))
                                    .color(1, 0, 0, 1)
                                    .endVertex();
                                var1.vertex((double)(8 + 8 * var15.getStepX()), (double)(8 + 8 * var15.getStepY()), (double)(8 + 8 * var15.getStepZ()))
                                    .color(1, 0, 0, 1)
                                    .endVertex();
                            }
                        }
                    }

                    var0.end();
                    RenderSystem.lineWidth(1.0F);
                    if (var13 > 0) {
                        var1.begin(7, DefaultVertexFormat.POSITION_COLOR);
                        float var17 = 0.5F;
                        float var18 = 0.2F;
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

                RenderSystem.popMatrix();
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
            RenderSystem.disableFog();
            RenderSystem.lineWidth(10.0F);
            RenderSystem.pushMatrix();
            RenderSystem.translatef(
                (float)(this.frustumPos.x - param0.getPosition().x),
                (float)(this.frustumPos.y - param0.getPosition().y),
                (float)(this.frustumPos.z - param0.getPosition().z)
            );
            RenderSystem.depthMask(true);
            var1.begin(7, DefaultVertexFormat.POSITION_COLOR);
            this.addFrustumQuad(var1, 0, 1, 2, 3, 0, 1, 1);
            this.addFrustumQuad(var1, 4, 5, 6, 7, 1, 0, 0);
            this.addFrustumQuad(var1, 0, 1, 5, 4, 1, 1, 0);
            this.addFrustumQuad(var1, 2, 3, 7, 6, 0, 0, 1);
            this.addFrustumQuad(var1, 0, 4, 7, 3, 0, 1, 0);
            this.addFrustumQuad(var1, 1, 5, 6, 2, 1, 0, 1);
            var0.end();
            RenderSystem.depthMask(false);
            var1.begin(1, DefaultVertexFormat.POSITION);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
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
            RenderSystem.popMatrix();
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.enableTexture();
            RenderSystem.enableFog();
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
        RenderSystem.disableFog();
        RenderSystem.disableAlphaTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        this.textureManager.bind(END_SKY_LOCATION);
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

            Matrix4f var3 = param0.getPose();
            var1.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
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
        RenderSystem.enableAlphaTest();
    }

    public void renderSky(PoseStack param0, float param1) {
        if (this.minecraft.level.dimension.getType() == DimensionType.THE_END) {
            this.renderEndSky(param0);
        } else if (this.minecraft.level.dimension.isNaturalDimension()) {
            RenderSystem.disableTexture();
            Vec3 var0 = this.level.getSkyColor(this.minecraft.gameRenderer.getMainCamera().getBlockPosition(), param1);
            float var1 = (float)var0.x;
            float var2 = (float)var0.y;
            float var3 = (float)var0.z;
            RenderSystem.color3f(var1, var2, var3);
            BufferBuilder var4 = Tesselator.getInstance().getBuilder();
            RenderSystem.depthMask(false);
            RenderSystem.enableFog();
            RenderSystem.color3f(var1, var2, var3);
            this.skyBuffer.bind();
            this.skyFormat.setupBufferState(0L);
            this.skyBuffer.draw(param0.getPose(), 7);
            VertexBuffer.unbind();
            this.skyFormat.clearBufferState();
            RenderSystem.disableFog();
            RenderSystem.disableAlphaTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            float[] var5 = this.level.dimension.getSunriseColor(this.level.getTimeOfDay(param1), param1);
            if (var5 != null) {
                RenderSystem.disableTexture();
                RenderSystem.shadeModel(7425);
                param0.pushPose();
                param0.mulPose(Vector3f.XP.rotationDegrees(90.0F));
                float var6 = Mth.sin(this.level.getSunAngle(param1)) < 0.0F ? 180.0F : 0.0F;
                param0.mulPose(Vector3f.ZP.rotationDegrees(var6));
                param0.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
                float var7 = var5[0];
                float var8 = var5[1];
                float var9 = var5[2];
                Matrix4f var10 = param0.getPose();
                var4.begin(6, DefaultVertexFormat.POSITION_COLOR);
                var4.vertex(var10, 0.0F, 100.0F, 0.0F).color(var7, var8, var9, var5[3]).endVertex();
                int var11 = 16;

                for(int var12 = 0; var12 <= 16; ++var12) {
                    float var13 = (float)var12 * (float) (Math.PI * 2) / 16.0F;
                    float var14 = Mth.sin(var13);
                    float var15 = Mth.cos(var13);
                    var4.vertex(var10, var14 * 120.0F, var15 * 120.0F, -var15 * 40.0F * var5[3]).color(var5[0], var5[1], var5[2], 0.0F).endVertex();
                }

                var4.end();
                BufferUploader.end(var4);
                param0.popPose();
                RenderSystem.shadeModel(7424);
            }

            RenderSystem.enableTexture();
            RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
            );
            param0.pushPose();
            float var16 = 1.0F - this.level.getRainLevel(param1);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, var16);
            param0.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
            param0.mulPose(Vector3f.XP.rotationDegrees(this.level.getTimeOfDay(param1) * 360.0F));
            Matrix4f var17 = param0.getPose();
            float var18 = 30.0F;
            this.textureManager.bind(SUN_LOCATION);
            var4.begin(7, DefaultVertexFormat.POSITION_TEX);
            var4.vertex(var17, -var18, 100.0F, -var18).uv(0.0F, 0.0F).endVertex();
            var4.vertex(var17, var18, 100.0F, -var18).uv(1.0F, 0.0F).endVertex();
            var4.vertex(var17, var18, 100.0F, var18).uv(1.0F, 1.0F).endVertex();
            var4.vertex(var17, -var18, 100.0F, var18).uv(0.0F, 1.0F).endVertex();
            var4.end();
            BufferUploader.end(var4);
            var18 = 20.0F;
            this.textureManager.bind(MOON_LOCATION);
            int var19 = this.level.getMoonPhase();
            int var20 = var19 % 4;
            int var21 = var19 / 4 % 2;
            float var22 = (float)(var20 + 0) / 4.0F;
            float var23 = (float)(var21 + 0) / 2.0F;
            float var24 = (float)(var20 + 1) / 4.0F;
            float var25 = (float)(var21 + 1) / 2.0F;
            var4.begin(7, DefaultVertexFormat.POSITION_TEX);
            var4.vertex(var17, -var18, -100.0F, var18).uv(var24, var25).endVertex();
            var4.vertex(var17, var18, -100.0F, var18).uv(var22, var25).endVertex();
            var4.vertex(var17, var18, -100.0F, -var18).uv(var22, var23).endVertex();
            var4.vertex(var17, -var18, -100.0F, -var18).uv(var24, var23).endVertex();
            var4.end();
            BufferUploader.end(var4);
            RenderSystem.disableTexture();
            float var26 = this.level.getStarBrightness(param1) * var16;
            if (var26 > 0.0F) {
                RenderSystem.color4f(var26, var26, var26, var26);
                this.starBuffer.bind();
                this.skyFormat.setupBufferState(0L);
                this.starBuffer.draw(param0.getPose(), 7);
                VertexBuffer.unbind();
                this.skyFormat.clearBufferState();
            }

            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
            RenderSystem.enableAlphaTest();
            RenderSystem.enableFog();
            param0.popPose();
            RenderSystem.disableTexture();
            RenderSystem.color3f(0.0F, 0.0F, 0.0F);
            double var27 = this.minecraft.player.getEyePosition(param1).y - this.level.getHorizonHeight();
            if (var27 < 0.0) {
                param0.pushPose();
                param0.translate(0.0, 12.0, 0.0);
                this.darkBuffer.bind();
                this.skyFormat.setupBufferState(0L);
                this.darkBuffer.draw(param0.getPose(), 7);
                VertexBuffer.unbind();
                this.skyFormat.clearBufferState();
                param0.popPose();
            }

            if (this.level.dimension.hasGround()) {
                RenderSystem.color3f(var1 * 0.2F + 0.04F, var2 * 0.2F + 0.04F, var3 * 0.6F + 0.1F);
            } else {
                RenderSystem.color3f(var1, var2, var3);
            }

            RenderSystem.enableTexture();
            RenderSystem.depthMask(true);
        }
    }

    public void renderClouds(PoseStack param0, float param1, double param2, double param3, double param4) {
        if (this.minecraft.level.dimension.isNaturalDimension()) {
            float var0 = 12.0F;
            float var1 = 4.0F;
            double var2 = 2.0E-4;
            double var3 = (double)(((float)this.ticks + param1) * 0.03F);
            double var4 = (param2 + var3) / 12.0;
            double var5 = (double)(this.level.dimension.getCloudHeight() - (float)param3 + 0.33F);
            double var6 = param4 / 12.0 + 0.33F;
            var4 -= (double)(Mth.floor(var4 / 2048.0) * 2048);
            var6 -= (double)(Mth.floor(var6 / 2048.0) * 2048);
            float var7 = (float)(var4 - (double)Mth.floor(var4));
            float var8 = (float)(var5 / 4.0 - (double)Mth.floor(var5 / 4.0)) * 4.0F;
            float var9 = (float)(var6 - (double)Mth.floor(var6));
            Vec3 var10 = this.level.getCloudColor(param1);
            int var11 = (int)Math.floor(var4);
            int var12 = (int)Math.floor(var5 / 4.0);
            int var13 = (int)Math.floor(var6);
            if (var11 != this.prevCloudX
                || var12 != this.prevCloudY
                || var13 != this.prevCloudZ
                || this.minecraft.options.getCloudsType() != this.prevCloudsType
                || this.prevCloudColor.distanceToSqr(var10) > 2.0E-4) {
                this.prevCloudX = var11;
                this.prevCloudY = var12;
                this.prevCloudZ = var13;
                this.prevCloudColor = var10;
                this.prevCloudsType = this.minecraft.options.getCloudsType();
                this.generateClouds = true;
            }

            if (this.generateClouds) {
                this.generateClouds = false;
                BufferBuilder var14 = Tesselator.getInstance().getBuilder();
                if (this.cloudBuffer != null) {
                    this.cloudBuffer.delete();
                }

                this.cloudBuffer = new VertexBuffer(DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
                this.buildClouds(var14, var4, var5, var6, var10);
                var14.end();
                this.cloudBuffer.upload(var14);
            }

            this.textureManager.bind(CLOUDS_LOCATION);
            param0.pushPose();
            param0.scale(12.0F, 1.0F, 12.0F);
            param0.translate((double)(-var7), (double)var8, (double)(-var9));
            if (this.cloudBuffer != null) {
                this.cloudBuffer.bind();
                DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL.setupBufferState(0L);
                int var15 = this.prevCloudsType == CloudStatus.FANCY ? 0 : 1;

                for(int var16 = var15; var16 < 2; ++var16) {
                    if (var16 == 0) {
                        RenderSystem.colorMask(false, false, false, false);
                    } else {
                        RenderSystem.colorMask(true, true, true, true);
                    }

                    this.cloudBuffer.draw(param0.getPose(), 7);
                }

                VertexBuffer.unbind();
                DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL.clearBufferState();
            }

            param0.popPose();
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
        param0.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
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
        if (!this.chunksToCompile.isEmpty()) {
            Iterator<ChunkRenderDispatcher.RenderChunk> var0 = this.chunksToCompile.iterator();

            while(var0.hasNext()) {
                ChunkRenderDispatcher.RenderChunk var1 = var0.next();
                if (var1.isDirtyFromPlayer()) {
                    this.chunkRenderDispatcher.rebuildChunkSync(var1);
                } else {
                    var1.rebuildChunkAsync(this.chunkRenderDispatcher);
                }

                var1.setNotDirty();
                var0.remove();
                long var2 = param0 - Util.getNanos();
                if (var2 < 0L) {
                    break;
                }
            }
        }

    }

    private void renderWorldBounds(Camera param0, float param1) {
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();
        WorldBorder var2 = this.level.getWorldBorder();
        double var3 = (double)(this.minecraft.options.renderDistance * 16);
        if (!(param0.getPosition().x < var2.getMaxX() - var3)
            || !(param0.getPosition().x > var2.getMinX() + var3)
            || !(param0.getPosition().z < var2.getMaxZ() - var3)
            || !(param0.getPosition().z > var2.getMinZ() + var3)) {
            double var4 = 1.0 - var2.getDistanceToBorder(param0.getPosition().x, param0.getPosition().z) / var3;
            var4 = Math.pow(var4, 4.0);
            double var5 = param0.getPosition().x;
            double var6 = param0.getPosition().y;
            double var7 = param0.getPosition().z;
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
            );
            this.textureManager.bind(FORCEFIELD_LOCATION);
            RenderSystem.depthMask(false);
            RenderSystem.pushMatrix();
            int var8 = var2.getStatus().getColor();
            float var9 = (float)(var8 >> 16 & 0xFF) / 255.0F;
            float var10 = (float)(var8 >> 8 & 0xFF) / 255.0F;
            float var11 = (float)(var8 & 0xFF) / 255.0F;
            RenderSystem.color4f(var9, var10, var11, (float)var4);
            RenderSystem.polygonOffset(-3.0F, -3.0F);
            RenderSystem.enablePolygonOffset();
            RenderSystem.defaultAlphaFunc();
            RenderSystem.enableAlphaTest();
            RenderSystem.disableCull();
            float var12 = (float)(Util.getMillis() % 3000L) / 3000.0F;
            float var13 = 0.0F;
            float var14 = 0.0F;
            float var15 = 128.0F;
            var1.begin(7, DefaultVertexFormat.POSITION_TEX);
            double var16 = Math.max((double)Mth.floor(var7 - var3), var2.getMinZ());
            double var17 = Math.min((double)Mth.ceil(var7 + var3), var2.getMaxZ());
            if (var5 > var2.getMaxX() - var3) {
                float var18 = 0.0F;

                for(double var19 = var16; var19 < var17; var18 += 0.5F) {
                    double var20 = Math.min(1.0, var17 - var19);
                    float var21 = (float)var20 * 0.5F;
                    this.vertex(var1, var5, var6, var7, var2.getMaxX(), 256, var19, var12 + var18, var12 + 0.0F);
                    this.vertex(var1, var5, var6, var7, var2.getMaxX(), 256, var19 + var20, var12 + var21 + var18, var12 + 0.0F);
                    this.vertex(var1, var5, var6, var7, var2.getMaxX(), 0, var19 + var20, var12 + var21 + var18, var12 + 128.0F);
                    this.vertex(var1, var5, var6, var7, var2.getMaxX(), 0, var19, var12 + var18, var12 + 128.0F);
                    ++var19;
                }
            }

            if (var5 < var2.getMinX() + var3) {
                float var22 = 0.0F;

                for(double var23 = var16; var23 < var17; var22 += 0.5F) {
                    double var24 = Math.min(1.0, var17 - var23);
                    float var25 = (float)var24 * 0.5F;
                    this.vertex(var1, var5, var6, var7, var2.getMinX(), 256, var23, var12 + var22, var12 + 0.0F);
                    this.vertex(var1, var5, var6, var7, var2.getMinX(), 256, var23 + var24, var12 + var25 + var22, var12 + 0.0F);
                    this.vertex(var1, var5, var6, var7, var2.getMinX(), 0, var23 + var24, var12 + var25 + var22, var12 + 128.0F);
                    this.vertex(var1, var5, var6, var7, var2.getMinX(), 0, var23, var12 + var22, var12 + 128.0F);
                    ++var23;
                }
            }

            var16 = Math.max((double)Mth.floor(var5 - var3), var2.getMinX());
            var17 = Math.min((double)Mth.ceil(var5 + var3), var2.getMaxX());
            if (var7 > var2.getMaxZ() - var3) {
                float var26 = 0.0F;

                for(double var27 = var16; var27 < var17; var26 += 0.5F) {
                    double var28 = Math.min(1.0, var17 - var27);
                    float var29 = (float)var28 * 0.5F;
                    this.vertex(var1, var5, var6, var7, var27, 256, var2.getMaxZ(), var12 + var26, var12 + 0.0F);
                    this.vertex(var1, var5, var6, var7, var27 + var28, 256, var2.getMaxZ(), var12 + var29 + var26, var12 + 0.0F);
                    this.vertex(var1, var5, var6, var7, var27 + var28, 0, var2.getMaxZ(), var12 + var29 + var26, var12 + 128.0F);
                    this.vertex(var1, var5, var6, var7, var27, 0, var2.getMaxZ(), var12 + var26, var12 + 128.0F);
                    ++var27;
                }
            }

            if (var7 < var2.getMinZ() + var3) {
                float var30 = 0.0F;

                for(double var31 = var16; var31 < var17; var30 += 0.5F) {
                    double var32 = Math.min(1.0, var17 - var31);
                    float var33 = (float)var32 * 0.5F;
                    this.vertex(var1, var5, var6, var7, var31, 256, var2.getMinZ(), var12 + var30, var12 + 0.0F);
                    this.vertex(var1, var5, var6, var7, var31 + var32, 256, var2.getMinZ(), var12 + var33 + var30, var12 + 0.0F);
                    this.vertex(var1, var5, var6, var7, var31 + var32, 0, var2.getMinZ(), var12 + var33 + var30, var12 + 128.0F);
                    this.vertex(var1, var5, var6, var7, var31, 0, var2.getMinZ(), var12 + var30, var12 + 128.0F);
                    ++var31;
                }
            }

            var0.end();
            RenderSystem.enableCull();
            RenderSystem.disableAlphaTest();
            RenderSystem.polygonOffset(0.0F, 0.0F);
            RenderSystem.disablePolygonOffset();
            RenderSystem.enableAlphaTest();
            RenderSystem.disableBlend();
            RenderSystem.popMatrix();
            RenderSystem.depthMask(true);
        }
    }

    private void vertex(BufferBuilder param0, double param1, double param2, double param3, double param4, int param5, double param6, float param7, float param8) {
        param0.vertex(param4 - param1, (double)param5 - param2, param6 - param3).uv(param7, param8).endVertex();
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
        Matrix4f var0 = param0.getPose();
        param2.forAllEdges(
            (param9x, param10, param11, param12, param13, param14) -> {
                param1.vertex(var0, (float)(param9x + param3), (float)(param10 + param4), (float)(param11 + param5))
                    .color(param6, param7, param8, param9)
                    .endVertex();
                param1.vertex(var0, (float)(param12 + param3), (float)(param13 + param4), (float)(param14 + param5))
                    .color(param6, param7, param8, param9)
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
        Matrix4f var0 = param0.getPose();
        float var1 = (float)param2;
        float var2 = (float)param3;
        float var3 = (float)param4;
        float var4 = (float)param5;
        float var5 = (float)param6;
        float var6 = (float)param7;
        param1.vertex(var0, var1, var2, var3).color(param8, param13, param14, param11).endVertex();
        param1.vertex(var0, var4, var2, var3).color(param8, param13, param14, param11).endVertex();
        param1.vertex(var0, var1, var2, var3).color(param12, param9, param14, param11).endVertex();
        param1.vertex(var0, var1, var5, var3).color(param12, param9, param14, param11).endVertex();
        param1.vertex(var0, var1, var2, var3).color(param12, param13, param10, param11).endVertex();
        param1.vertex(var0, var1, var2, var6).color(param12, param13, param10, param11).endVertex();
        param1.vertex(var0, var4, var2, var3).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, var4, var5, var3).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, var4, var5, var3).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, var1, var5, var3).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, var1, var5, var3).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, var1, var5, var6).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, var1, var5, var6).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, var1, var2, var6).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, var1, var2, var6).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, var4, var2, var6).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, var4, var2, var6).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, var4, var2, var3).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, var1, var5, var6).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, var4, var5, var6).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, var4, var2, var6).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, var4, var5, var6).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, var4, var5, var3).color(param8, param9, param10, param11).endVertex();
        param1.vertex(var0, var4, var5, var6).color(param8, param9, param10, param11).endVertex();
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
                    this.setSectionDirty(var1 >> 4, var2 >> 4, var0 >> 4, param1);
                }
            }
        }

    }

    public void setBlocksDirty(int param0, int param1, int param2, int param3, int param4, int param5) {
        for(int var0 = param2 - 1; var0 <= param5 + 1; ++var0) {
            for(int var1 = param0 - 1; var1 <= param3 + 1; ++var1) {
                for(int var2 = param1 - 1; var2 <= param4 + 1; ++var2) {
                    this.setSectionDirty(var1 >> 4, var2 >> 4, var0 >> 4);
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
                this.minecraft.gui.setNowPlaying(var1.getDisplayName().getColoredString());
            }

            SoundInstance var5 = SimpleSoundInstance.forRecord(param0, (float)param1.getX(), (float)param1.getY(), (float)param1.getZ());
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
            var2.setDetail("Position", () -> CrashReportCategory.formatLocation(param3, param4, param5));
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
                this.level.playLocalSound(param2, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1006:
                this.level.playLocalSound(param2, SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1007:
                this.level
                    .playLocalSound(param2, SoundEvents.WOODEN_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1008:
                this.level.playLocalSound(param2, SoundEvents.FENCE_GATE_OPEN, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1009:
                this.level
                    .playLocalSound(param2, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (var0.nextFloat() - var0.nextFloat()) * 0.8F, false);
                break;
            case 1010:
                if (Item.byId(param3) instanceof RecordItem) {
                    this.playStreamingMusic(((RecordItem)Item.byId(param3)).getSound(), param2);
                } else {
                    this.playStreamingMusic(null, param2);
                }
                break;
            case 1011:
                this.level.playLocalSound(param2, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1012:
                this.level.playLocalSound(param2, SoundEvents.WOODEN_DOOR_CLOSE, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1013:
                this.level
                    .playLocalSound(param2, SoundEvents.WOODEN_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1014:
                this.level.playLocalSound(param2, SoundEvents.FENCE_GATE_CLOSE, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
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
                this.level.playLocalSound(param2, SoundEvents.ANVIL_DESTROY, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1030:
                this.level.playLocalSound(param2, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1031:
                this.level.playLocalSound(param2, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.3F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1032:
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.PORTAL_TRAVEL, var0.nextFloat() * 0.4F + 0.8F));
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
                this.level
                    .playLocalSound(param2, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1037:
                this.level.playLocalSound(param2, SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
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
            case 1500:
                ComposterBlock.handleFill(this.level, param2, param3 > 0);
                break;
            case 1501:
                this.level
                    .playLocalSound(
                        param2,
                        SoundEvents.LAVA_EXTINGUISH,
                        SoundSource.BLOCKS,
                        0.5F,
                        2.6F + (this.level.getRandom().nextFloat() - this.level.getRandom().nextFloat()) * 0.8F,
                        false
                    );

                for(int var43 = 0; var43 < 8; ++var43) {
                    this.level
                        .addParticle(
                            ParticleTypes.LARGE_SMOKE,
                            (double)param2.getX() + Math.random(),
                            (double)param2.getY() + 1.2,
                            (double)param2.getZ() + Math.random(),
                            0.0,
                            0.0,
                            0.0
                        );
                }
                break;
            case 1502:
                this.level
                    .playLocalSound(
                        param2,
                        SoundEvents.REDSTONE_TORCH_BURNOUT,
                        SoundSource.BLOCKS,
                        0.5F,
                        2.6F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.8F,
                        false
                    );

                for(int var44 = 0; var44 < 5; ++var44) {
                    double var45 = (double)param2.getX() + var0.nextDouble() * 0.6 + 0.2;
                    double var46 = (double)param2.getY() + var0.nextDouble() * 0.6 + 0.2;
                    double var47 = (double)param2.getZ() + var0.nextDouble() * 0.6 + 0.2;
                    this.level.addParticle(ParticleTypes.SMOKE, var45, var46, var47, 0.0, 0.0, 0.0);
                }
                break;
            case 1503:
                this.level.playLocalSound(param2, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 1.0F, 1.0F, false);

                for(int var48 = 0; var48 < 16; ++var48) {
                    double var49 = (double)((float)param2.getX() + (5.0F + var0.nextFloat() * 6.0F) / 16.0F);
                    double var50 = (double)((float)param2.getY() + 0.8125F);
                    double var51 = (double)((float)param2.getZ() + (5.0F + var0.nextFloat() * 6.0F) / 16.0F);
                    double var52 = 0.0;
                    double var53 = 0.0;
                    double var54 = 0.0;
                    this.level.addParticle(ParticleTypes.SMOKE, var49, var50, var51, 0.0, 0.0, 0.0);
                }
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
                BlockState var37 = Block.stateById(param3);
                if (!var37.isAir()) {
                    SoundType var38 = var37.getSoundType();
                    this.level
                        .playLocalSound(param2, var38.getBreakSound(), SoundSource.BLOCKS, (var38.getVolume() + 1.0F) / 2.0F, var38.getPitch() * 0.8F, false);
                }

                this.minecraft.particleEngine.destroy(param2, var37);
                break;
            case 2002:
            case 2007:
                double var21 = (double)param2.getX();
                double var22 = (double)param2.getY();
                double var23 = (double)param2.getZ();

                for(int var24 = 0; var24 < 8; ++var24) {
                    this.addParticle(
                        new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.SPLASH_POTION)),
                        var21,
                        var22,
                        var23,
                        var0.nextGaussian() * 0.15,
                        var0.nextDouble() * 0.2,
                        var0.nextGaussian() * 0.15
                    );
                }

                float var25 = (float)(param3 >> 16 & 0xFF) / 255.0F;
                float var26 = (float)(param3 >> 8 & 0xFF) / 255.0F;
                float var27 = (float)(param3 >> 0 & 0xFF) / 255.0F;
                ParticleOptions var28 = param1 == 2007 ? ParticleTypes.INSTANT_EFFECT : ParticleTypes.EFFECT;

                for(int var29 = 0; var29 < 100; ++var29) {
                    double var30 = var0.nextDouble() * 4.0;
                    double var31 = var0.nextDouble() * Math.PI * 2.0;
                    double var32 = Math.cos(var31) * var30;
                    double var33 = 0.01 + var0.nextDouble() * 0.5;
                    double var34 = Math.sin(var31) * var30;
                    Particle var35 = this.addParticleInternal(
                        var28, var28.getType().getOverrideLimiter(), var21 + var32 * 0.1, var22 + 0.3, var23 + var34 * 0.1, var32, var33, var34
                    );
                    if (var35 != null) {
                        float var36 = 0.75F + var0.nextFloat() * 0.25F;
                        var35.setColor(var25 * var36, var26 * var36, var27 * var36);
                        var35.setPower((float)var30);
                    }
                }

                this.level
                    .playLocalSound(param2, SoundEvents.SPLASH_POTION_BREAK, SoundSource.NEUTRAL, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
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
                for(int var39 = 0; var39 < 20; ++var39) {
                    double var40 = (double)param2.getX() + 0.5 + ((double)this.level.random.nextFloat() - 0.5) * 2.0;
                    double var41 = (double)param2.getY() + 0.5 + ((double)this.level.random.nextFloat() - 0.5) * 2.0;
                    double var42 = (double)param2.getZ() + 0.5 + ((double)this.level.random.nextFloat() - 0.5) * 2.0;
                    this.level.addParticle(ParticleTypes.SMOKE, var40, var41, var42, 0.0, 0.0, 0.0);
                    this.level.addParticle(ParticleTypes.FLAME, var40, var41, var42, 0.0, 0.0, 0.0);
                }
                break;
            case 2005:
                BoneMealItem.addGrowthParticles(this.level, param2, param3);
                break;
            case 2006:
                for(int var55 = 0; var55 < 200; ++var55) {
                    float var56 = var0.nextFloat() * 4.0F;
                    float var57 = var0.nextFloat() * (float) (Math.PI * 2);
                    double var58 = (double)(Mth.cos(var57) * var56);
                    double var59 = 0.01 + var0.nextDouble() * 0.5;
                    double var60 = (double)(Mth.sin(var57) * var56);
                    Particle var61 = this.addParticleInternal(
                        ParticleTypes.DRAGON_BREATH,
                        false,
                        (double)param2.getX() + var58 * 0.1,
                        (double)param2.getY() + 0.3,
                        (double)param2.getZ() + var60 * 0.1,
                        var58,
                        var59,
                        var60
                    );
                    if (var61 != null) {
                        var61.setPower(var56);
                    }
                }

                this.level
                    .playLocalSound(param2, SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.HOSTILE, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 2008:
                this.level
                    .addParticle(ParticleTypes.EXPLOSION, (double)param2.getX() + 0.5, (double)param2.getY() + 0.5, (double)param2.getZ() + 0.5, 0.0, 0.0, 0.0);
                break;
            case 2009:
                for(int var62 = 0; var62 < 8; ++var62) {
                    this.level
                        .addParticle(
                            ParticleTypes.CLOUD,
                            (double)param2.getX() + Math.random(),
                            (double)param2.getY() + 1.2,
                            (double)param2.getZ() + Math.random(),
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

    public RenderTarget entityTarget() {
        return this.entityTarget;
    }

    @OnlyIn(Dist.CLIENT)
    class RenderChunkInfo {
        private final ChunkRenderDispatcher.RenderChunk chunk;
        private final Direction sourceDirection;
        private byte directions;
        private final int step;

        private RenderChunkInfo(ChunkRenderDispatcher.RenderChunk param0, @Nullable Direction param1, int param2) {
            this.chunk = param0;
            this.sourceDirection = param1;
            this.step = param2;
        }

        public void setDirections(byte param0, Direction param1) {
            this.directions = (byte)(this.directions | param0 | 1 << param1.ordinal());
        }

        public boolean hasDirection(Direction param0) {
            return (this.directions & 1 << param0.ordinal()) > 0;
        }
    }
}
