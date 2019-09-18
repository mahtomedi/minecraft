package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Frustum;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector4f;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.client.renderer.culling.Culler;
import net.minecraft.client.renderer.culling.FrustumCuller;
import net.minecraft.client.renderer.culling.FrustumData;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.ModelBakery;
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
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
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
    private final Map<Integer, BlockDestructionProgress> destroyingBlocks = Maps.newHashMap();
    private final Map<BlockPos, SoundInstance> playingRecords = Maps.newHashMap();
    private final List<TextureAtlasSprite> breakingTextures = IntStream.range(0, 10).mapToObj(param0x -> null).collect(Collectors.toList());
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
    private FrustumData capturedFrustum;
    private final Vector4f[] frustumPoints = new Vector4f[8];
    private final Vector3d frustumPos = new Vector3d(0.0, 0.0, 0.0);
    private double xTransparentOld;
    private double yTransparentOld;
    private double zTransparentOld;
    private boolean needsUpdate = true;
    private boolean hadRenderedEntityOutlines;
    private int frameId;
    private int rainSoundTime;
    private final float[] rainSizeX = new float[1024];
    private final float[] rainSizeZ = new float[1024];
    private final BufferBuilder builder = new BufferBuilder(RenderType.ENTITY.bufferSize());

    public LevelRenderer(Minecraft param0) {
        this.minecraft = param0;
        this.entityRenderDispatcher = param0.getEntityRenderDispatcher();
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

    private void renderSnowAndRain(LightTexture param0, Camera param1, float param2) {
        float var0 = this.minecraft.level.getRainLevel(param2);
        if (!(var0 <= 0.0F)) {
            param0.turnOnLightLayer();
            Level var1 = this.minecraft.level;
            int var2 = Mth.floor(param1.getPosition().x);
            int var3 = Mth.floor(param1.getPosition().y);
            int var4 = Mth.floor(param1.getPosition().z);
            Tesselator var5 = Tesselator.getInstance();
            BufferBuilder var6 = var5.getBuilder();
            RenderSystem.disableCull();
            RenderSystem.normal3f(0.0F, 1.0F, 0.0F);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.defaultAlphaFunc();
            double var7 = param1.getPosition().x;
            double var8 = param1.getPosition().y;
            double var9 = param1.getPosition().z;
            int var10 = Mth.floor(var8);
            int var11 = 5;
            if (this.minecraft.options.fancyGraphics) {
                var11 = 10;
            }

            int var12 = -1;
            float var13 = (float)this.ticks + param2;
            var6.offset(-var7, -var8, -var9);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            BlockPos.MutableBlockPos var14 = new BlockPos.MutableBlockPos();

            for(int var15 = var4 - var11; var15 <= var4 + var11; ++var15) {
                for(int var16 = var2 - var11; var16 <= var2 + var11; ++var16) {
                    int var17 = (var15 - var4 + 16) * 32 + var16 - var2 + 16;
                    double var18 = (double)this.rainSizeX[var17] * 0.5;
                    double var19 = (double)this.rainSizeZ[var17] * 0.5;
                    var14.set(var16, 0, var15);
                    Biome var20 = var1.getBiome(var14);
                    if (var20.getPrecipitation() != Biome.Precipitation.NONE) {
                        int var21 = var1.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, var14).getY();
                        int var22 = var3 - var11;
                        int var23 = var3 + var11;
                        if (var22 < var21) {
                            var22 = var21;
                        }

                        if (var23 < var21) {
                            var23 = var21;
                        }

                        int var24 = var21;
                        if (var21 < var10) {
                            var24 = var10;
                        }

                        if (var22 != var23) {
                            Random var25 = new Random((long)(var16 * var16 * 3121 + var16 * 45238971 ^ var15 * var15 * 418711 + var15 * 13761));
                            var14.set(var16, var22, var15);
                            float var26 = var20.getTemperature(var14);
                            if (var26 >= 0.15F) {
                                if (var12 != 0) {
                                    if (var12 >= 0) {
                                        var5.end();
                                    }

                                    var12 = 0;
                                    this.minecraft.getTextureManager().bind(RAIN_LOCATION);
                                    var6.begin(7, DefaultVertexFormat.PARTICLE);
                                }

                                double var27 = -(
                                        (double)(this.ticks + var16 * var16 * 3121 + var16 * 45238971 + var15 * var15 * 418711 + var15 * 13761 & 31)
                                            + (double)param2
                                    )
                                    / 32.0
                                    * (3.0 + var25.nextDouble());
                                double var28 = (double)((float)var16 + 0.5F) - param1.getPosition().x;
                                double var29 = (double)((float)var15 + 0.5F) - param1.getPosition().z;
                                float var30 = Mth.sqrt(var28 * var28 + var29 * var29) / (float)var11;
                                float var31 = ((1.0F - var30 * var30) * 0.5F + 0.5F) * var0;
                                var14.set(var16, var24, var15);
                                int var32 = var1.getLightColor(var14);
                                int var33 = var32 >> 16 & 65535;
                                int var34 = var32 & 65535;
                                var6.vertex((double)var16 - var18 + 0.5, (double)var23, (double)var15 - var19 + 0.5)
                                    .uv(0.0, (double)var22 * 0.25 + var27)
                                    .color(1.0F, 1.0F, 1.0F, var31)
                                    .uv2(var33, var34)
                                    .endVertex();
                                var6.vertex((double)var16 + var18 + 0.5, (double)var23, (double)var15 + var19 + 0.5)
                                    .uv(1.0, (double)var22 * 0.25 + var27)
                                    .color(1.0F, 1.0F, 1.0F, var31)
                                    .uv2(var33, var34)
                                    .endVertex();
                                var6.vertex((double)var16 + var18 + 0.5, (double)var22, (double)var15 + var19 + 0.5)
                                    .uv(1.0, (double)var23 * 0.25 + var27)
                                    .color(1.0F, 1.0F, 1.0F, var31)
                                    .uv2(var33, var34)
                                    .endVertex();
                                var6.vertex((double)var16 - var18 + 0.5, (double)var22, (double)var15 - var19 + 0.5)
                                    .uv(0.0, (double)var23 * 0.25 + var27)
                                    .color(1.0F, 1.0F, 1.0F, var31)
                                    .uv2(var33, var34)
                                    .endVertex();
                            } else {
                                if (var12 != 1) {
                                    if (var12 >= 0) {
                                        var5.end();
                                    }

                                    var12 = 1;
                                    this.minecraft.getTextureManager().bind(SNOW_LOCATION);
                                    var6.begin(7, DefaultVertexFormat.PARTICLE);
                                }

                                double var35 = (double)(-((float)(this.ticks & 511) + param2) / 512.0F);
                                double var36 = var25.nextDouble() + (double)var13 * 0.01 * (double)((float)var25.nextGaussian());
                                double var37 = var25.nextDouble() + (double)(var13 * (float)var25.nextGaussian()) * 0.001;
                                double var38 = (double)((float)var16 + 0.5F) - param1.getPosition().x;
                                double var39 = (double)((float)var15 + 0.5F) - param1.getPosition().z;
                                float var40 = Mth.sqrt(var38 * var38 + var39 * var39) / (float)var11;
                                float var41 = ((1.0F - var40 * var40) * 0.3F + 0.5F) * var0;
                                var14.set(var16, var24, var15);
                                int var42 = (var1.getLightColor(var14) * 3 + 15728880) / 4;
                                int var43 = var42 >> 16 & 65535;
                                int var44 = var42 & 65535;
                                var6.vertex((double)var16 - var18 + 0.5, (double)var23, (double)var15 - var19 + 0.5)
                                    .uv(0.0 + var36, (double)var22 * 0.25 + var35 + var37)
                                    .color(1.0F, 1.0F, 1.0F, var41)
                                    .uv2(var43, var44)
                                    .endVertex();
                                var6.vertex((double)var16 + var18 + 0.5, (double)var23, (double)var15 + var19 + 0.5)
                                    .uv(1.0 + var36, (double)var22 * 0.25 + var35 + var37)
                                    .color(1.0F, 1.0F, 1.0F, var41)
                                    .uv2(var43, var44)
                                    .endVertex();
                                var6.vertex((double)var16 + var18 + 0.5, (double)var22, (double)var15 + var19 + 0.5)
                                    .uv(1.0 + var36, (double)var23 * 0.25 + var35 + var37)
                                    .color(1.0F, 1.0F, 1.0F, var41)
                                    .uv2(var43, var44)
                                    .endVertex();
                                var6.vertex((double)var16 - var18 + 0.5, (double)var22, (double)var15 - var19 + 0.5)
                                    .uv(0.0 + var36, (double)var23 * 0.25 + var35 + var37)
                                    .color(1.0F, 1.0F, 1.0F, var41)
                                    .uv2(var43, var44)
                                    .endVertex();
                            }
                        }
                    }
                }
            }

            if (var12 >= 0) {
                var5.end();
            }

            var6.offset(0.0, 0.0, 0.0);
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
        this.setupBreakingTextureSprites();
        this.initOutline();
    }

    private void setupBreakingTextureSprites() {
        TextureAtlas var0 = this.minecraft.getTextureAtlas();

        for(int var1 = 0; var1 < 10; ++var1) {
            this.breakingTextures.set(var1, var0.getSprite(ModelBakery.DESTROY_STAGES.get(var1)));
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
                this.chunkRenderDispatcher = new ChunkRenderDispatcher(this.level, this, Util.backgroundExecutor(), this.minecraft.is64Bit());
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
                    this.viewArea.repositionCamera(var0.x, var0.z);
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

    private void setupRender(Camera param0, Culler param1, int param2, boolean param3) {
        if (this.minecraft.options.renderDistance != this.lastViewDistance) {
            this.allChanged();
        }

        this.level.getProfiler().push("camera");
        double var0 = this.minecraft.player.x - this.lastCameraX;
        double var1 = this.minecraft.player.y - this.lastCameraY;
        double var2 = this.minecraft.player.z - this.lastCameraZ;
        if (this.lastCameraChunkX != this.minecraft.player.xChunk
            || this.lastCameraChunkY != this.minecraft.player.yChunk
            || this.lastCameraChunkZ != this.minecraft.player.zChunk
            || var0 * var0 + var1 * var1 + var2 * var2 > 16.0) {
            this.lastCameraX = this.minecraft.player.x;
            this.lastCameraY = this.minecraft.player.y;
            this.lastCameraZ = this.minecraft.player.z;
            this.lastCameraChunkX = this.minecraft.player.xChunk;
            this.lastCameraChunkY = this.minecraft.player.yChunk;
            this.lastCameraChunkZ = this.minecraft.player.zChunk;
            this.viewArea.repositionCamera(this.minecraft.player.x, this.minecraft.player.z);
        }

        this.level.getProfiler().popPush("renderlistcamera");
        this.chunkRenderDispatcher.setCamera(param0.getPosition());
        this.level.getProfiler().popPush("cull");
        if (this.capturedFrustum != null) {
            FrustumCuller var3 = new FrustumCuller(this.capturedFrustum);
            var3.prepare(this.frustumPos.x, this.frustumPos.y, this.frustumPos.z);
            param1 = var3;
        }

        this.minecraft.getProfiler().popPush("culling");
        BlockPos var4 = param0.getBlockPosition();
        ChunkRenderDispatcher.RenderChunk var5 = this.viewArea.getRenderChunkAt(var4);
        BlockPos var6 = new BlockPos(
            Mth.floor(param0.getPosition().x / 16.0) * 16, Mth.floor(param0.getPosition().y / 16.0) * 16, Mth.floor(param0.getPosition().z / 16.0) * 16
        );
        float var7 = param0.getXRot();
        float var8 = param0.getYRot();
        this.needsUpdate = this.needsUpdate
            || !this.chunksToCompile.isEmpty()
            || param0.getPosition().x != this.prevCamX
            || param0.getPosition().y != this.prevCamY
            || param0.getPosition().z != this.prevCamZ
            || (double)var7 != this.prevCamRotX
            || (double)var8 != this.prevCamRotY;
        this.prevCamX = param0.getPosition().x;
        this.prevCamY = param0.getPosition().y;
        this.prevCamZ = param0.getPosition().z;
        this.prevCamRotX = (double)var7;
        this.prevCamRotY = (double)var8;
        boolean var9 = this.capturedFrustum != null;
        this.minecraft.getProfiler().popPush("update");
        if (!var9 && this.needsUpdate) {
            this.needsUpdate = false;
            this.renderChunks = Lists.newArrayList();
            Queue<LevelRenderer.RenderChunkInfo> var10 = Queues.newArrayDeque();
            Entity.setViewScale(Mth.clamp((double)this.minecraft.options.renderDistance / 8.0, 1.0, 2.5));
            boolean var11 = this.minecraft.smartCull;
            if (var5 != null) {
                boolean var16 = false;
                LevelRenderer.RenderChunkInfo var17 = new LevelRenderer.RenderChunkInfo(var5, null, 0);
                Set<Direction> var18 = this.getVisibleDirections(var4);
                if (var18.size() == 1) {
                    Vec3 var19 = param0.getLookVector();
                    Direction var20 = Direction.getNearest(var19.x, var19.y, var19.z).getOpposite();
                    var18.remove(var20);
                }

                if (var18.isEmpty()) {
                    var16 = true;
                }

                if (var16 && !param3) {
                    this.renderChunks.add(var17);
                } else {
                    if (param3 && this.level.getBlockState(var4).isSolidRender(this.level, var4)) {
                        var11 = false;
                    }

                    var5.setFrame(param2);
                    var10.add(var17);
                }
            } else {
                int var12 = var4.getY() > 0 ? 248 : 8;

                for(int var13 = -this.lastViewDistance; var13 <= this.lastViewDistance; ++var13) {
                    for(int var14 = -this.lastViewDistance; var14 <= this.lastViewDistance; ++var14) {
                        ChunkRenderDispatcher.RenderChunk var15 = this.viewArea.getRenderChunkAt(new BlockPos((var13 << 4) + 8, var12, (var14 << 4) + 8));
                        if (var15 != null && param1.isVisible(var15.bb)) {
                            var15.setFrame(param2);
                            var10.add(new LevelRenderer.RenderChunkInfo(var15, null, 0));
                        }
                    }
                }
            }

            this.minecraft.getProfiler().push("iteration");

            while(!var10.isEmpty()) {
                LevelRenderer.RenderChunkInfo var21 = var10.poll();
                ChunkRenderDispatcher.RenderChunk var22 = var21.chunk;
                Direction var23 = var21.sourceDirection;
                this.renderChunks.add(var21);

                for(Direction var24 : DIRECTIONS) {
                    ChunkRenderDispatcher.RenderChunk var25 = this.getRelativeFrom(var6, var22, var24);
                    if ((!var11 || !var21.hasDirection(var24.getOpposite()))
                        && (!var11 || var23 == null || var22.getCompiledChunk().facesCanSeeEachother(var23.getOpposite(), var24))
                        && var25 != null
                        && var25.hasAllNeighbors()
                        && var25.setFrame(param2)
                        && param1.isVisible(var25.bb)) {
                        LevelRenderer.RenderChunkInfo var26 = new LevelRenderer.RenderChunkInfo(var25, var24, var21.step + 1);
                        var26.setDirections(var21.directions, var24);
                        var10.add(var26);
                    }
                }
            }

            this.minecraft.getProfiler().pop();
        }

        this.minecraft.getProfiler().popPush("captureFrustum");
        if (this.captureFrustum) {
            this.captureFrustum(param0.getPosition().x, param0.getPosition().y, param0.getPosition().z);
            this.captureFrustum = false;
        }

        this.minecraft.getProfiler().popPush("rebuildNear");
        Set<ChunkRenderDispatcher.RenderChunk> var27 = this.chunksToCompile;
        this.chunksToCompile = Sets.newLinkedHashSet();

        for(LevelRenderer.RenderChunkInfo var28 : this.renderChunks) {
            ChunkRenderDispatcher.RenderChunk var29 = var28.chunk;
            if (var29.isDirty() || var27.contains(var29)) {
                this.needsUpdate = true;
                BlockPos var30 = var29.getOrigin().offset(8, 8, 8);
                boolean var31 = var30.distSqr(var4) < 768.0;
                if (!var29.isDirtyFromPlayer() && !var31) {
                    this.chunksToCompile.add(var29);
                } else {
                    this.minecraft.getProfiler().push("build near");
                    this.chunkRenderDispatcher.rebuildChunkSync(var29);
                    var29.setNotDirty();
                    this.minecraft.getProfiler().pop();
                }
            }
        }

        this.chunksToCompile.addAll(var27);
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

    private void captureFrustum(double param0, double param1, double param2) {
    }

    public void renderLevel(float param0, long param1, boolean param2, Camera param3, GameRenderer param4, LightTexture param5) {
        FrustumData var0 = Frustum.getFrustum();
        BlockEntityRenderDispatcher.instance.prepare(this.level, this.minecraft.getTextureManager(), this.minecraft.font, param3, this.minecraft.hitResult);
        this.entityRenderDispatcher.prepare(this.level, this.minecraft.font, param3, this.minecraft.crosshairPickEntity, this.minecraft.options);
        ProfilerFiller var1 = this.level.getProfiler();
        var1.popPush("light_updates");
        this.minecraft.level.getChunkSource().getLightEngine().runUpdates(Integer.MAX_VALUE, true, true);
        var1.popPush("clear");
        RenderSystem.viewport(0, 0, this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
        this.fog.setupClearColor(param3, param0, this.minecraft.level, this.minecraft.options.renderDistance, param4.getDarkenWorldAmount(param0));
        RenderSystem.clear(16640, Minecraft.ON_OSX);
        var1.popPush("culling");
        Culler var2 = new FrustumCuller(var0);
        double var3 = param3.getPosition().x;
        double var4 = param3.getPosition().y;
        double var5 = param3.getPosition().z;
        var2.prepare(var3, var4, var5);
        float var6 = param4.getRenderDistance();
        boolean var7 = this.minecraft.level.dimension.isFoggyAt(Mth.floor(var3), Mth.floor(var4)) || this.minecraft.gui.getBossOverlay().shouldCreateWorldFog();
        if (this.minecraft.options.renderDistance >= 4) {
            FogRenderer.setupFog(param3, -1, var6, var7);
            var1.popPush("sky");
            param4.resetProjectionMatrix(param3, param0, true, false, 2.0F);
            this.renderSky(param0);
            param4.resetProjectionMatrix(param3, param0, true, false, Mth.SQRT_OF_TWO);
        }

        FogRenderer.setupFog(param3, 0, var6, var7);
        var1.popPush("prepareterrain");
        FogRenderer.setupFog(param3, 0, var6, var7);
        var1.popPush("terrain_setup");
        this.setupRender(param3, var2, this.frameId++, this.minecraft.player.isSpectator());
        var1.popPush("updatechunks");
        this.compileChunksUntil(param1);
        var1.popPush("terrain");
        RenderSystem.pushMatrix();
        this.minecraft.getTextureManager().bind(TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.enableCull();
        RenderSystem.shadeModel(7425);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        Lighting.turnOff();
        this.renderChunkLayer(RenderType.SOLID, param3, param5);
        this.minecraft.getTextureManager().bind(TextureAtlas.LOCATION_BLOCKS);
        this.renderChunkLayer(RenderType.CUTOUT_MIPPED, param3, param5);
        this.renderChunkLayer(RenderType.CUTOUT, param3, param5);
        RenderSystem.popMatrix();
        var1.popPush("aboveClouds");
        if (this.minecraft.options.getCloudsType() != CloudStatus.OFF) {
            var1.popPush("clouds");
            param4.resetProjectionMatrix(param3, param0, true, false, 4.0F);
            FogRenderer.setupFog(param3, 0, param4.getRenderDistance(), var7);
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.enableAlphaTest();
            RenderSystem.enableDepthTest();
            RenderSystem.defaultAlphaFunc();
            RenderSystem.defaultBlendFunc();
            this.renderClouds(param0, var3, var4, var5);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableAlphaTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.disableFog();
            param4.resetProjectionMatrix(param3, param0, true, false, Mth.SQRT_OF_TWO);
        }

        RenderSystem.pushMatrix();
        var1.popPush("entities");
        var1.push("prepare");
        this.renderedEntities = 0;
        this.culledEntities = 0;
        BlockEntityRenderDispatcher.xOff = var3;
        BlockEntityRenderDispatcher.yOff = var4;
        BlockEntityRenderDispatcher.zOff = var5;
        this.entityRenderDispatcher.setPosition(var3, var4, var5);
        RenderSystem.shadeModel(7424);
        RenderSystem.defaultAlphaFunc();
        Lighting.turnOn();
        param5.turnOnLightLayer();
        var1.popPush("entities");
        List<Entity> var8 = Lists.newArrayList();
        List<Entity> var9 = Lists.newArrayList();

        for(Entity var10 : this.level.entitiesForRendering()) {
            if ((this.entityRenderDispatcher.shouldRender(var10, var2, var3, var4, var5) || var10.hasIndirectPassenger(this.minecraft.player))
                && (
                    var10 != param3.getEntity()
                        || param3.isDetached()
                        || param3.getEntity() instanceof LivingEntity && ((LivingEntity)param3.getEntity()).isSleeping()
                )) {
                ++this.renderedEntities;
                this.entityRenderDispatcher.render(var10, param0, false);
                if (var10.isGlowing() || var10 instanceof Player && this.minecraft.player.isSpectator() && this.minecraft.options.keySpectatorOutlines.isDown()
                    )
                 {
                    var8.add(var10);
                }

                if (this.entityRenderDispatcher.hasSecondPass(var10)) {
                    var9.add(var10);
                }
            }
        }

        if (!var9.isEmpty()) {
            for(Entity var11 : var9) {
                this.entityRenderDispatcher.renderSecondPass(var11, param0);
            }
        }

        if (this.shouldShowEntityOutlines() && (!var8.isEmpty() || this.hadRenderedEntityOutlines)) {
            var1.popPush("entityOutlines");
            this.entityTarget.clear(Minecraft.ON_OSX);
            this.hadRenderedEntityOutlines = !var8.isEmpty();
            if (!var8.isEmpty()) {
                RenderSystem.depthFunc(519);
                RenderSystem.disableFog();
                Lighting.turnOff();
                this.entityTarget.bindWrite(false);
                this.entityRenderDispatcher.setSolidRendering(true);

                for(int var12 = 0; var12 < var8.size(); ++var12) {
                    this.entityRenderDispatcher.render(var8.get(var12), param0, false);
                }

                this.entityRenderDispatcher.setSolidRendering(false);
                Lighting.turnOn();
                RenderSystem.depthMask(false);
                this.entityEffect.process(param0);
                RenderSystem.enableLighting();
                RenderSystem.depthMask(true);
                RenderSystem.enableFog();
                RenderSystem.enableBlend();
                RenderSystem.enableColorMaterial();
                RenderSystem.depthFunc(515);
                RenderSystem.enableDepthTest();
                RenderSystem.enableAlphaTest();
            }

            this.minecraft.getMainRenderTarget().bindWrite(false);
        }

        var1.popPush("blockentities");
        Lighting.turnOn();
        this.builder.begin(7, DefaultVertexFormat.BLOCK);

        for(LevelRenderer.RenderChunkInfo var13 : this.renderChunks) {
            List<BlockEntity> var14 = var13.chunk.getCompiledChunk().getRenderableBlockEntities();
            if (!var14.isEmpty()) {
                for(BlockEntity var15 : var14) {
                    BlockEntityRenderDispatcher.instance.render(var15, param0, RenderType.ENTITY, this.builder);
                }
            }
        }

        synchronized(this.globalBlockEntities) {
            for(BlockEntity var16 : this.globalBlockEntities) {
                BlockEntityRenderDispatcher.instance.render(var16, param0, RenderType.ENTITY, this.builder);
            }
        }

        RenderType.ENTITY.setupRenderState();
        Lighting.turnOff();
        this.builder.end();
        BufferUploader.end(this.builder);
        RenderType.ENTITY.clearRenderState();
        Lighting.turnOff();
        var1.popPush("destroyProgress");
        this.textureManager.bind(TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.pushMatrix();
        this.builder.begin(7, DefaultVertexFormat.BLOCK);
        this.builder.noColor();

        for(BlockDestructionProgress var17 : this.destroyingBlocks.values()) {
            BlockPos var18 = var17.getPos();
            BlockState var19 = this.level.getBlockState(var18);
            if (var19.getBlock().isEntityBlock()) {
                BlockEntity var20 = this.level.getBlockEntity(var18);
                if (var20 instanceof ChestBlockEntity && var19.getValue(ChestBlock.TYPE) == ChestType.LEFT) {
                    var18 = var18.relative(var19.getValue(ChestBlock.FACING).getClockWise());
                    var20 = this.level.getBlockEntity(var18);
                }

                if (var20 != null && var19.hasCustomBreakingProgress()) {
                    BlockEntityRenderDispatcher.instance.renderBreaking(var20, param0, var17.getProgress(), this.builder);
                }
            }
        }

        RenderType.CRUMBLING.setupRenderState();
        param5.turnOffLightLayer();
        if (!this.destroyingBlocks.isEmpty()) {
            this.builder.offset(-var3, -var4, -var5);
            Iterator<BlockDestructionProgress> var21 = this.destroyingBlocks.values().iterator();

            while(var21.hasNext()) {
                BlockDestructionProgress var22 = var21.next();
                BlockPos var23 = var22.getPos();
                Block var24 = this.level.getBlockState(var23).getBlock();
                if (!(var24 instanceof ChestBlock)
                    && !(var24 instanceof EnderChestBlock)
                    && !(var24 instanceof SignBlock)
                    && !(var24 instanceof AbstractSkullBlock)) {
                    double var25 = (double)var23.getX() - var3;
                    double var26 = (double)var23.getY() - var4;
                    double var27 = (double)var23.getZ() - var5;
                    if (var25 * var25 + var26 * var26 + var27 * var27 > 1024.0) {
                        var21.remove();
                    } else {
                        BlockState var28 = this.level.getBlockState(var23);
                        if (!var28.isAir()) {
                            int var29 = var22.getProgress();
                            TextureAtlasSprite var30 = this.breakingTextures.get(var29);
                            BlockRenderDispatcher var31 = this.minecraft.getBlockRenderer();
                            var31.renderBreakingTexture(var28, var23, var30, this.level);
                        }
                    }
                }
            }

            this.builder.offset(0.0, 0.0, 0.0);
        }

        RenderType.CRUMBLING.setupRenderState();
        Lighting.turnOff();
        if (RenderType.CRUMBLING == RenderType.TRANSLUCENT) {
            this.builder.sortQuads((float)var3, (float)var4, (float)var5);
        }

        this.builder.end();
        BufferUploader.end(this.builder);
        RenderType.CRUMBLING.clearRenderState();
        RenderSystem.popMatrix();
        var1.pop();
        Lighting.turnOff();
        param5.turnOffLightLayer();
        RenderSystem.popMatrix();
        if (param2 && this.minecraft.hitResult != null) {
            RenderSystem.disableAlphaTest();
            var1.popPush("outline");
            RenderSystem.lineWidth(Math.max(2.5F, (float)this.minecraft.getWindow().getWidth() / 1920.0F * 2.5F));
            RenderSystem.disableTexture();
            RenderSystem.depthMask(false);
            RenderSystem.matrixMode(5889);
            RenderSystem.pushMatrix();
            RenderSystem.scalef(1.0F, 1.0F, 0.999F);
            this.renderHitOutline(param3.getEntity(), this.minecraft.hitResult, 0, var3, var4, var5);
            RenderSystem.popMatrix();
            RenderSystem.matrixMode(5888);
            RenderSystem.depthMask(true);
            RenderSystem.enableTexture();
            RenderSystem.enableAlphaTest();
        }

        param5.turnOnLightLayer();
        FogRenderer.setupFog(param3, 0, var6, var7);
        var1.popPush("particles");
        this.minecraft.particleEngine.render(param3, param0);
        param5.turnOffLightLayer();
        RenderSystem.depthMask(false);
        var1.popPush("weather");
        this.renderSnowAndRain(param5, param3, param0);
        RenderSystem.depthMask(true);
        this.renderWorldBounds(param3, param0);
        this.minecraft.getTextureManager().bind(TextureAtlas.LOCATION_BLOCKS);
        FogRenderer.setupFog(param3, 0, var6, var7);
        RenderSystem.shadeModel(7425);
        var1.popPush("translucent");
        this.renderChunkLayer(RenderType.TRANSLUCENT, param3, param5);
        RenderSystem.shadeModel(7424);
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.disableFog();
    }

    private void renderChunkLayer(RenderType param0, Camera param1, LightTexture param2) {
        param0.setupRenderState();
        if (param0 == RenderType.TRANSLUCENT) {
            this.minecraft.getProfiler().push("translucent_sort");
            double var0 = param1.getPosition().x - this.xTransparentOld;
            double var1 = param1.getPosition().y - this.yTransparentOld;
            double var2 = param1.getPosition().z - this.zTransparentOld;
            if (var0 * var0 + var1 * var1 + var2 * var2 > 1.0) {
                this.xTransparentOld = param1.getPosition().x;
                this.yTransparentOld = param1.getPosition().y;
                this.zTransparentOld = param1.getPosition().z;
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

        for(LevelRenderer.RenderChunkInfo var6 : param0 == RenderType.TRANSLUCENT ? Lists.reverse(this.renderChunks) : this.renderChunks) {
            ChunkRenderDispatcher.RenderChunk var7 = var6.chunk;
            if (!var7.getCompiledChunk().isEmpty(param0)) {
                var5.add(var7);
            }
        }

        this.minecraft.getProfiler().popPush(() -> "render_" + param0);
        param2.turnOnLightLayer();
        Lighting.turnOff();
        double var8 = param1.getPosition().x();
        double var9 = param1.getPosition().y();
        double var10 = param1.getPosition().z();

        for(ChunkRenderDispatcher.RenderChunk var11 : var5) {
            VertexBuffer var12 = var11.getBuffer(param0);
            RenderSystem.pushMatrix();
            BlockPos var13 = var11.getOrigin();
            RenderSystem.translated((double)var13.getX() - var8, (double)var13.getY() - var9, (double)var13.getZ() - var10);
            var12.bind();
            this.format.setupBufferState(0L);
            var12.draw(7);
            RenderSystem.popMatrix();
        }

        VertexBuffer.unbind();
        RenderSystem.clearCurrentColor();
        this.format.clearBufferState();
        param2.turnOffLightLayer();
        this.minecraft.getProfiler().pop();
        param0.clearRenderState();
    }

    private void updateBlockDestruction(Iterator<BlockDestructionProgress> param0) {
        while(param0.hasNext()) {
            BlockDestructionProgress var0 = param0.next();
            int var1 = var0.getUpdatedRenderTick();
            if (this.ticks - var1 > 400) {
                param0.remove();
            }
        }

    }

    public void tick() {
        ++this.ticks;
        if (this.ticks % 20 == 0) {
            this.updateBlockDestruction(this.destroyingBlocks.values().iterator());
        }

    }

    private void renderEndSky() {
        RenderSystem.disableFog();
        RenderSystem.disableAlphaTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Lighting.turnOff();
        RenderSystem.depthMask(false);
        this.textureManager.bind(END_SKY_LOCATION);
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();

        for(int var2 = 0; var2 < 6; ++var2) {
            RenderSystem.pushMatrix();
            if (var2 == 1) {
                RenderSystem.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
            }

            if (var2 == 2) {
                RenderSystem.rotatef(-90.0F, 1.0F, 0.0F, 0.0F);
            }

            if (var2 == 3) {
                RenderSystem.rotatef(180.0F, 1.0F, 0.0F, 0.0F);
            }

            if (var2 == 4) {
                RenderSystem.rotatef(90.0F, 0.0F, 0.0F, 1.0F);
            }

            if (var2 == 5) {
                RenderSystem.rotatef(-90.0F, 0.0F, 0.0F, 1.0F);
            }

            var1.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
            var1.vertex(-100.0, -100.0, -100.0).uv(0.0, 0.0).color(40, 40, 40, 255).endVertex();
            var1.vertex(-100.0, -100.0, 100.0).uv(0.0, 16.0).color(40, 40, 40, 255).endVertex();
            var1.vertex(100.0, -100.0, 100.0).uv(16.0, 16.0).color(40, 40, 40, 255).endVertex();
            var1.vertex(100.0, -100.0, -100.0).uv(16.0, 0.0).color(40, 40, 40, 255).endVertex();
            var0.end();
            RenderSystem.popMatrix();
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
    }

    public void renderSky(float param0) {
        if (this.minecraft.level.dimension.getType() == DimensionType.THE_END) {
            this.renderEndSky();
        } else if (this.minecraft.level.dimension.isNaturalDimension()) {
            RenderSystem.disableTexture();
            Vec3 var0 = this.level.getSkyColor(this.minecraft.gameRenderer.getMainCamera().getBlockPosition(), param0);
            float var1 = (float)var0.x;
            float var2 = (float)var0.y;
            float var3 = (float)var0.z;
            RenderSystem.color3f(var1, var2, var3);
            Tesselator var4 = Tesselator.getInstance();
            BufferBuilder var5 = var4.getBuilder();
            RenderSystem.depthMask(false);
            RenderSystem.enableFog();
            RenderSystem.color3f(var1, var2, var3);
            this.skyBuffer.bind();
            this.skyFormat.setupBufferState(0L);
            this.skyBuffer.draw(7);
            VertexBuffer.unbind();
            this.skyFormat.clearBufferState();
            RenderSystem.disableFog();
            RenderSystem.disableAlphaTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            Lighting.turnOff();
            float[] var6 = this.level.dimension.getSunriseColor(this.level.getTimeOfDay(param0), param0);
            if (var6 != null) {
                RenderSystem.disableTexture();
                RenderSystem.shadeModel(7425);
                RenderSystem.pushMatrix();
                RenderSystem.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
                RenderSystem.rotatef(Mth.sin(this.level.getSunAngle(param0)) < 0.0F ? 180.0F : 0.0F, 0.0F, 0.0F, 1.0F);
                RenderSystem.rotatef(90.0F, 0.0F, 0.0F, 1.0F);
                float var7 = var6[0];
                float var8 = var6[1];
                float var9 = var6[2];
                var5.begin(6, DefaultVertexFormat.POSITION_COLOR);
                var5.vertex(0.0, 100.0, 0.0).color(var7, var8, var9, var6[3]).endVertex();
                int var10 = 16;

                for(int var11 = 0; var11 <= 16; ++var11) {
                    float var12 = (float)var11 * (float) (Math.PI * 2) / 16.0F;
                    float var13 = Mth.sin(var12);
                    float var14 = Mth.cos(var12);
                    var5.vertex((double)(var13 * 120.0F), (double)(var14 * 120.0F), (double)(-var14 * 40.0F * var6[3]))
                        .color(var6[0], var6[1], var6[2], 0.0F)
                        .endVertex();
                }

                var4.end();
                RenderSystem.popMatrix();
                RenderSystem.shadeModel(7424);
            }

            RenderSystem.enableTexture();
            RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
            );
            RenderSystem.pushMatrix();
            float var15 = 1.0F - this.level.getRainLevel(param0);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, var15);
            RenderSystem.rotatef(-90.0F, 0.0F, 1.0F, 0.0F);
            RenderSystem.rotatef(this.level.getTimeOfDay(param0) * 360.0F, 1.0F, 0.0F, 0.0F);
            float var16 = 30.0F;
            this.textureManager.bind(SUN_LOCATION);
            var5.begin(7, DefaultVertexFormat.POSITION_TEX);
            var5.vertex((double)(-var16), 100.0, (double)(-var16)).uv(0.0, 0.0).endVertex();
            var5.vertex((double)var16, 100.0, (double)(-var16)).uv(1.0, 0.0).endVertex();
            var5.vertex((double)var16, 100.0, (double)var16).uv(1.0, 1.0).endVertex();
            var5.vertex((double)(-var16), 100.0, (double)var16).uv(0.0, 1.0).endVertex();
            var4.end();
            var16 = 20.0F;
            this.textureManager.bind(MOON_LOCATION);
            int var17 = this.level.getMoonPhase();
            int var18 = var17 % 4;
            int var19 = var17 / 4 % 2;
            float var20 = (float)(var18 + 0) / 4.0F;
            float var21 = (float)(var19 + 0) / 2.0F;
            float var22 = (float)(var18 + 1) / 4.0F;
            float var23 = (float)(var19 + 1) / 2.0F;
            var5.begin(7, DefaultVertexFormat.POSITION_TEX);
            var5.vertex((double)(-var16), -100.0, (double)var16).uv((double)var22, (double)var23).endVertex();
            var5.vertex((double)var16, -100.0, (double)var16).uv((double)var20, (double)var23).endVertex();
            var5.vertex((double)var16, -100.0, (double)(-var16)).uv((double)var20, (double)var21).endVertex();
            var5.vertex((double)(-var16), -100.0, (double)(-var16)).uv((double)var22, (double)var21).endVertex();
            var4.end();
            RenderSystem.disableTexture();
            float var24 = this.level.getStarBrightness(param0) * var15;
            if (var24 > 0.0F) {
                RenderSystem.color4f(var24, var24, var24, var24);
                this.starBuffer.bind();
                this.skyFormat.setupBufferState(0L);
                this.starBuffer.draw(7);
                VertexBuffer.unbind();
                this.skyFormat.clearBufferState();
            }

            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
            RenderSystem.enableAlphaTest();
            RenderSystem.enableFog();
            RenderSystem.popMatrix();
            RenderSystem.disableTexture();
            RenderSystem.color3f(0.0F, 0.0F, 0.0F);
            double var25 = this.minecraft.player.getEyePosition(param0).y - this.level.getHorizonHeight();
            if (var25 < 0.0) {
                RenderSystem.pushMatrix();
                RenderSystem.translatef(0.0F, 12.0F, 0.0F);
                this.darkBuffer.bind();
                this.skyFormat.setupBufferState(0L);
                this.darkBuffer.draw(7);
                VertexBuffer.unbind();
                this.skyFormat.clearBufferState();
                RenderSystem.popMatrix();
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

    public void renderClouds(float param0, double param1, double param2, double param3) {
        if (this.minecraft.level.dimension.isNaturalDimension()) {
            float var0 = 12.0F;
            float var1 = 4.0F;
            double var2 = 2.0E-4;
            double var3 = (double)(((float)this.ticks + param0) * 0.03F);
            double var4 = (param1 + var3) / 12.0;
            double var5 = (double)(this.level.dimension.getCloudHeight() - (float)param2 + 0.33F);
            double var6 = param3 / 12.0 + 0.33F;
            var4 -= (double)(Mth.floor(var4 / 2048.0) * 2048);
            var6 -= (double)(Mth.floor(var6 / 2048.0) * 2048);
            float var7 = (float)(var4 - (double)Mth.floor(var4));
            float var8 = (float)(var5 / 4.0 - (double)Mth.floor(var5 / 4.0)) * 4.0F;
            float var9 = (float)(var6 - (double)Mth.floor(var6));
            Vec3 var10 = this.level.getCloudColor(param0);
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
                Tesselator var14 = Tesselator.getInstance();
                BufferBuilder var15 = var14.getBuilder();
                if (this.cloudBuffer != null) {
                    this.cloudBuffer.delete();
                }

                this.cloudBuffer = new VertexBuffer(DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
                this.buildClouds(var15, var4, var5, var6, var10);
                var15.end();
                this.cloudBuffer.upload(var15);
            }

            this.textureManager.bind(CLOUDS_LOCATION);
            RenderSystem.pushMatrix();
            RenderSystem.scalef(12.0F, 1.0F, 12.0F);
            RenderSystem.translatef(-var7, var8, -var9);
            if (this.cloudBuffer != null) {
                this.cloudBuffer.bind();
                DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL.setupBufferState(0L);
                int var16 = this.prevCloudsType == CloudStatus.FANCY ? 0 : 1;

                for(int var17 = var16; var17 < 2; ++var17) {
                    if (var17 == 0) {
                        RenderSystem.colorMask(false, false, false, false);
                    } else {
                        RenderSystem.colorMask(true, true, true, true);
                    }

                    this.cloudBuffer.draw(7);
                }

                VertexBuffer.unbind();
                DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL.clearBufferState();
            }

            RenderSystem.popMatrix();
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
                            .uv((double)((var22 + 0.0F) * 0.00390625F + var5), (double)((var23 + 8.0F) * 0.00390625F + var6))
                            .color(var13, var14, var15, 0.8F)
                            .normal(0.0F, -1.0F, 0.0F)
                            .endVertex();
                        param0.vertex((double)(var22 + 8.0F), (double)(var19 + 0.0F), (double)(var23 + 8.0F))
                            .uv((double)((var22 + 8.0F) * 0.00390625F + var5), (double)((var23 + 8.0F) * 0.00390625F + var6))
                            .color(var13, var14, var15, 0.8F)
                            .normal(0.0F, -1.0F, 0.0F)
                            .endVertex();
                        param0.vertex((double)(var22 + 8.0F), (double)(var19 + 0.0F), (double)(var23 + 0.0F))
                            .uv((double)((var22 + 8.0F) * 0.00390625F + var5), (double)((var23 + 0.0F) * 0.00390625F + var6))
                            .color(var13, var14, var15, 0.8F)
                            .normal(0.0F, -1.0F, 0.0F)
                            .endVertex();
                        param0.vertex((double)(var22 + 0.0F), (double)(var19 + 0.0F), (double)(var23 + 0.0F))
                            .uv((double)((var22 + 0.0F) * 0.00390625F + var5), (double)((var23 + 0.0F) * 0.00390625F + var6))
                            .color(var13, var14, var15, 0.8F)
                            .normal(0.0F, -1.0F, 0.0F)
                            .endVertex();
                    }

                    if (var19 <= 5.0F) {
                        param0.vertex((double)(var22 + 0.0F), (double)(var19 + 4.0F - 9.765625E-4F), (double)(var23 + 8.0F))
                            .uv((double)((var22 + 0.0F) * 0.00390625F + var5), (double)((var23 + 8.0F) * 0.00390625F + var6))
                            .color(var7, var8, var9, 0.8F)
                            .normal(0.0F, 1.0F, 0.0F)
                            .endVertex();
                        param0.vertex((double)(var22 + 8.0F), (double)(var19 + 4.0F - 9.765625E-4F), (double)(var23 + 8.0F))
                            .uv((double)((var22 + 8.0F) * 0.00390625F + var5), (double)((var23 + 8.0F) * 0.00390625F + var6))
                            .color(var7, var8, var9, 0.8F)
                            .normal(0.0F, 1.0F, 0.0F)
                            .endVertex();
                        param0.vertex((double)(var22 + 8.0F), (double)(var19 + 4.0F - 9.765625E-4F), (double)(var23 + 0.0F))
                            .uv((double)((var22 + 8.0F) * 0.00390625F + var5), (double)((var23 + 0.0F) * 0.00390625F + var6))
                            .color(var7, var8, var9, 0.8F)
                            .normal(0.0F, 1.0F, 0.0F)
                            .endVertex();
                        param0.vertex((double)(var22 + 0.0F), (double)(var19 + 4.0F - 9.765625E-4F), (double)(var23 + 0.0F))
                            .uv((double)((var22 + 0.0F) * 0.00390625F + var5), (double)((var23 + 0.0F) * 0.00390625F + var6))
                            .color(var7, var8, var9, 0.8F)
                            .normal(0.0F, 1.0F, 0.0F)
                            .endVertex();
                    }

                    if (var20 > -1) {
                        for(int var24 = 0; var24 < 8; ++var24) {
                            param0.vertex((double)(var22 + (float)var24 + 0.0F), (double)(var19 + 0.0F), (double)(var23 + 8.0F))
                                .uv((double)((var22 + (float)var24 + 0.5F) * 0.00390625F + var5), (double)((var23 + 8.0F) * 0.00390625F + var6))
                                .color(var10, var11, var12, 0.8F)
                                .normal(-1.0F, 0.0F, 0.0F)
                                .endVertex();
                            param0.vertex((double)(var22 + (float)var24 + 0.0F), (double)(var19 + 4.0F), (double)(var23 + 8.0F))
                                .uv((double)((var22 + (float)var24 + 0.5F) * 0.00390625F + var5), (double)((var23 + 8.0F) * 0.00390625F + var6))
                                .color(var10, var11, var12, 0.8F)
                                .normal(-1.0F, 0.0F, 0.0F)
                                .endVertex();
                            param0.vertex((double)(var22 + (float)var24 + 0.0F), (double)(var19 + 4.0F), (double)(var23 + 0.0F))
                                .uv((double)((var22 + (float)var24 + 0.5F) * 0.00390625F + var5), (double)((var23 + 0.0F) * 0.00390625F + var6))
                                .color(var10, var11, var12, 0.8F)
                                .normal(-1.0F, 0.0F, 0.0F)
                                .endVertex();
                            param0.vertex((double)(var22 + (float)var24 + 0.0F), (double)(var19 + 0.0F), (double)(var23 + 0.0F))
                                .uv((double)((var22 + (float)var24 + 0.5F) * 0.00390625F + var5), (double)((var23 + 0.0F) * 0.00390625F + var6))
                                .color(var10, var11, var12, 0.8F)
                                .normal(-1.0F, 0.0F, 0.0F)
                                .endVertex();
                        }
                    }

                    if (var20 <= 1) {
                        for(int var25 = 0; var25 < 8; ++var25) {
                            param0.vertex((double)(var22 + (float)var25 + 1.0F - 9.765625E-4F), (double)(var19 + 0.0F), (double)(var23 + 8.0F))
                                .uv((double)((var22 + (float)var25 + 0.5F) * 0.00390625F + var5), (double)((var23 + 8.0F) * 0.00390625F + var6))
                                .color(var10, var11, var12, 0.8F)
                                .normal(1.0F, 0.0F, 0.0F)
                                .endVertex();
                            param0.vertex((double)(var22 + (float)var25 + 1.0F - 9.765625E-4F), (double)(var19 + 4.0F), (double)(var23 + 8.0F))
                                .uv((double)((var22 + (float)var25 + 0.5F) * 0.00390625F + var5), (double)((var23 + 8.0F) * 0.00390625F + var6))
                                .color(var10, var11, var12, 0.8F)
                                .normal(1.0F, 0.0F, 0.0F)
                                .endVertex();
                            param0.vertex((double)(var22 + (float)var25 + 1.0F - 9.765625E-4F), (double)(var19 + 4.0F), (double)(var23 + 0.0F))
                                .uv((double)((var22 + (float)var25 + 0.5F) * 0.00390625F + var5), (double)((var23 + 0.0F) * 0.00390625F + var6))
                                .color(var10, var11, var12, 0.8F)
                                .normal(1.0F, 0.0F, 0.0F)
                                .endVertex();
                            param0.vertex((double)(var22 + (float)var25 + 1.0F - 9.765625E-4F), (double)(var19 + 0.0F), (double)(var23 + 0.0F))
                                .uv((double)((var22 + (float)var25 + 0.5F) * 0.00390625F + var5), (double)((var23 + 0.0F) * 0.00390625F + var6))
                                .color(var10, var11, var12, 0.8F)
                                .normal(1.0F, 0.0F, 0.0F)
                                .endVertex();
                        }
                    }

                    if (var21 > -1) {
                        for(int var26 = 0; var26 < 8; ++var26) {
                            param0.vertex((double)(var22 + 0.0F), (double)(var19 + 4.0F), (double)(var23 + (float)var26 + 0.0F))
                                .uv((double)((var22 + 0.0F) * 0.00390625F + var5), (double)((var23 + (float)var26 + 0.5F) * 0.00390625F + var6))
                                .color(var16, var17, var18, 0.8F)
                                .normal(0.0F, 0.0F, -1.0F)
                                .endVertex();
                            param0.vertex((double)(var22 + 8.0F), (double)(var19 + 4.0F), (double)(var23 + (float)var26 + 0.0F))
                                .uv((double)((var22 + 8.0F) * 0.00390625F + var5), (double)((var23 + (float)var26 + 0.5F) * 0.00390625F + var6))
                                .color(var16, var17, var18, 0.8F)
                                .normal(0.0F, 0.0F, -1.0F)
                                .endVertex();
                            param0.vertex((double)(var22 + 8.0F), (double)(var19 + 0.0F), (double)(var23 + (float)var26 + 0.0F))
                                .uv((double)((var22 + 8.0F) * 0.00390625F + var5), (double)((var23 + (float)var26 + 0.5F) * 0.00390625F + var6))
                                .color(var16, var17, var18, 0.8F)
                                .normal(0.0F, 0.0F, -1.0F)
                                .endVertex();
                            param0.vertex((double)(var22 + 0.0F), (double)(var19 + 0.0F), (double)(var23 + (float)var26 + 0.0F))
                                .uv((double)((var22 + 0.0F) * 0.00390625F + var5), (double)((var23 + (float)var26 + 0.5F) * 0.00390625F + var6))
                                .color(var16, var17, var18, 0.8F)
                                .normal(0.0F, 0.0F, -1.0F)
                                .endVertex();
                        }
                    }

                    if (var21 <= 1) {
                        for(int var27 = 0; var27 < 8; ++var27) {
                            param0.vertex((double)(var22 + 0.0F), (double)(var19 + 4.0F), (double)(var23 + (float)var27 + 1.0F - 9.765625E-4F))
                                .uv((double)((var22 + 0.0F) * 0.00390625F + var5), (double)((var23 + (float)var27 + 0.5F) * 0.00390625F + var6))
                                .color(var16, var17, var18, 0.8F)
                                .normal(0.0F, 0.0F, 1.0F)
                                .endVertex();
                            param0.vertex((double)(var22 + 8.0F), (double)(var19 + 4.0F), (double)(var23 + (float)var27 + 1.0F - 9.765625E-4F))
                                .uv((double)((var22 + 8.0F) * 0.00390625F + var5), (double)((var23 + (float)var27 + 0.5F) * 0.00390625F + var6))
                                .color(var16, var17, var18, 0.8F)
                                .normal(0.0F, 0.0F, 1.0F)
                                .endVertex();
                            param0.vertex((double)(var22 + 8.0F), (double)(var19 + 0.0F), (double)(var23 + (float)var27 + 1.0F - 9.765625E-4F))
                                .uv((double)((var22 + 8.0F) * 0.00390625F + var5), (double)((var23 + (float)var27 + 0.5F) * 0.00390625F + var6))
                                .color(var16, var17, var18, 0.8F)
                                .normal(0.0F, 0.0F, 1.0F)
                                .endVertex();
                            param0.vertex((double)(var22 + 0.0F), (double)(var19 + 0.0F), (double)(var23 + (float)var27 + 1.0F - 9.765625E-4F))
                                .uv((double)((var22 + 0.0F) * 0.00390625F + var5), (double)((var23 + (float)var27 + 0.5F) * 0.00390625F + var6))
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
                        .uv((double)((float)(var30 + 0) * 0.00390625F + var5), (double)((float)(var31 + 32) * 0.00390625F + var6))
                        .color(var7, var8, var9, 0.8F)
                        .normal(0.0F, -1.0F, 0.0F)
                        .endVertex();
                    param0.vertex((double)(var30 + 32), (double)var19, (double)(var31 + 32))
                        .uv((double)((float)(var30 + 32) * 0.00390625F + var5), (double)((float)(var31 + 32) * 0.00390625F + var6))
                        .color(var7, var8, var9, 0.8F)
                        .normal(0.0F, -1.0F, 0.0F)
                        .endVertex();
                    param0.vertex((double)(var30 + 32), (double)var19, (double)(var31 + 0))
                        .uv((double)((float)(var30 + 32) * 0.00390625F + var5), (double)((float)(var31 + 0) * 0.00390625F + var6))
                        .color(var7, var8, var9, 0.8F)
                        .normal(0.0F, -1.0F, 0.0F)
                        .endVertex();
                    param0.vertex((double)(var30 + 0), (double)var19, (double)(var31 + 0))
                        .uv((double)((float)(var30 + 0) * 0.00390625F + var5), (double)((float)(var31 + 0) * 0.00390625F + var6))
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
            var1.offset(-var5, -var6, -var7);
            double var16 = Math.max((double)Mth.floor(var7 - var3), var2.getMinZ());
            double var17 = Math.min((double)Mth.ceil(var7 + var3), var2.getMaxZ());
            if (var5 > var2.getMaxX() - var3) {
                float var18 = 0.0F;

                for(double var19 = var16; var19 < var17; var18 += 0.5F) {
                    double var20 = Math.min(1.0, var17 - var19);
                    float var21 = (float)var20 * 0.5F;
                    var1.vertex(var2.getMaxX(), 256.0, var19).uv((double)(var12 + var18), (double)(var12 + 0.0F)).endVertex();
                    var1.vertex(var2.getMaxX(), 256.0, var19 + var20).uv((double)(var12 + var21 + var18), (double)(var12 + 0.0F)).endVertex();
                    var1.vertex(var2.getMaxX(), 0.0, var19 + var20).uv((double)(var12 + var21 + var18), (double)(var12 + 128.0F)).endVertex();
                    var1.vertex(var2.getMaxX(), 0.0, var19).uv((double)(var12 + var18), (double)(var12 + 128.0F)).endVertex();
                    ++var19;
                }
            }

            if (var5 < var2.getMinX() + var3) {
                float var22 = 0.0F;

                for(double var23 = var16; var23 < var17; var22 += 0.5F) {
                    double var24 = Math.min(1.0, var17 - var23);
                    float var25 = (float)var24 * 0.5F;
                    var1.vertex(var2.getMinX(), 256.0, var23).uv((double)(var12 + var22), (double)(var12 + 0.0F)).endVertex();
                    var1.vertex(var2.getMinX(), 256.0, var23 + var24).uv((double)(var12 + var25 + var22), (double)(var12 + 0.0F)).endVertex();
                    var1.vertex(var2.getMinX(), 0.0, var23 + var24).uv((double)(var12 + var25 + var22), (double)(var12 + 128.0F)).endVertex();
                    var1.vertex(var2.getMinX(), 0.0, var23).uv((double)(var12 + var22), (double)(var12 + 128.0F)).endVertex();
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
                    var1.vertex(var27, 256.0, var2.getMaxZ()).uv((double)(var12 + var26), (double)(var12 + 0.0F)).endVertex();
                    var1.vertex(var27 + var28, 256.0, var2.getMaxZ()).uv((double)(var12 + var29 + var26), (double)(var12 + 0.0F)).endVertex();
                    var1.vertex(var27 + var28, 0.0, var2.getMaxZ()).uv((double)(var12 + var29 + var26), (double)(var12 + 128.0F)).endVertex();
                    var1.vertex(var27, 0.0, var2.getMaxZ()).uv((double)(var12 + var26), (double)(var12 + 128.0F)).endVertex();
                    ++var27;
                }
            }

            if (var7 < var2.getMinZ() + var3) {
                float var30 = 0.0F;

                for(double var31 = var16; var31 < var17; var30 += 0.5F) {
                    double var32 = Math.min(1.0, var17 - var31);
                    float var33 = (float)var32 * 0.5F;
                    var1.vertex(var31, 256.0, var2.getMinZ()).uv((double)(var12 + var30), (double)(var12 + 0.0F)).endVertex();
                    var1.vertex(var31 + var32, 256.0, var2.getMinZ()).uv((double)(var12 + var33 + var30), (double)(var12 + 0.0F)).endVertex();
                    var1.vertex(var31 + var32, 0.0, var2.getMinZ()).uv((double)(var12 + var33 + var30), (double)(var12 + 128.0F)).endVertex();
                    var1.vertex(var31, 0.0, var2.getMinZ()).uv((double)(var12 + var30), (double)(var12 + 128.0F)).endVertex();
                    ++var31;
                }
            }

            var0.end();
            var1.offset(0.0, 0.0, 0.0);
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

    private void renderHitOutline(Entity param0, HitResult param1, int param2, double param3, double param4, double param5) {
        if (param2 == 0 && param1.getType() == HitResult.Type.BLOCK) {
            BlockPos var0 = ((BlockHitResult)param1).getBlockPos();
            BlockState var1 = this.level.getBlockState(var0);
            if (!var1.isAir() && this.level.getWorldBorder().isWithinBounds(var0)) {
                renderShape(
                    var1.getShape(this.level, var0, CollisionContext.of(param0)),
                    (double)var0.getX() - param3,
                    (double)var0.getY() - param4,
                    (double)var0.getZ() - param5,
                    0.0F,
                    0.0F,
                    0.0F,
                    0.4F
                );
            }
        }
    }

    public static void renderShape(VoxelShape param0, double param1, double param2, double param3, float param4, float param5, float param6, float param7) {
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();
        var1.begin(1, DefaultVertexFormat.POSITION_COLOR);
        param0.forAllEdges((param8, param9, param10, param11, param12, param13) -> {
            var1.vertex(param8 + param1, param9 + param2, param10 + param3).color(param4, param5, param6, param7).endVertex();
            var1.vertex(param11 + param1, param12 + param2, param13 + param3).color(param4, param5, param6, param7).endVertex();
        });
        var0.end();
    }

    public static void renderLineBox(
        double param0, double param1, double param2, double param3, double param4, double param5, float param6, float param7, float param8, float param9
    ) {
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();
        var1.begin(3, DefaultVertexFormat.POSITION_COLOR);
        addChainedLineBoxVertices(var1, param0, param1, param2, param3, param4, param5, param6, param7, param8, param9);
        var0.end();
    }

    public static void addChainedLineBoxVertices(
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
        param0.vertex(param1, param2, param3).color(param7, param8, param9, 0.0F).endVertex();
        param0.vertex(param1, param2, param3).color(param7, param8, param9, param10).endVertex();
        param0.vertex(param4, param2, param3).color(param7, param8, param9, param10).endVertex();
        param0.vertex(param4, param2, param6).color(param7, param8, param9, param10).endVertex();
        param0.vertex(param1, param2, param6).color(param7, param8, param9, param10).endVertex();
        param0.vertex(param1, param2, param3).color(param7, param8, param9, param10).endVertex();
        param0.vertex(param1, param5, param3).color(param7, param8, param9, param10).endVertex();
        param0.vertex(param4, param5, param3).color(param7, param8, param9, param10).endVertex();
        param0.vertex(param4, param5, param6).color(param7, param8, param9, param10).endVertex();
        param0.vertex(param1, param5, param6).color(param7, param8, param9, param10).endVertex();
        param0.vertex(param1, param5, param3).color(param7, param8, param9, param10).endVertex();
        param0.vertex(param1, param5, param6).color(param7, param8, param9, 0.0F).endVertex();
        param0.vertex(param1, param2, param6).color(param7, param8, param9, param10).endVertex();
        param0.vertex(param4, param5, param6).color(param7, param8, param9, 0.0F).endVertex();
        param0.vertex(param4, param2, param6).color(param7, param8, param9, param10).endVertex();
        param0.vertex(param4, param5, param3).color(param7, param8, param9, 0.0F).endVertex();
        param0.vertex(param4, param2, param3).color(param7, param8, param9, param10).endVertex();
        param0.vertex(param4, param2, param3).color(param7, param8, param9, 0.0F).endVertex();
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
            BlockDestructionProgress var0 = this.destroyingBlocks.get(param0);
            if (var0 == null || var0.getPos().getX() != param1.getX() || var0.getPos().getY() != param1.getY() || var0.getPos().getZ() != param1.getZ()) {
                var0 = new BlockDestructionProgress(param0, param1);
                this.destroyingBlocks.put(param0, var0);
            }

            var0.setProgress(param2);
            var0.updateTick(this.ticks);
        } else {
            this.destroyingBlocks.remove(param0);
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
