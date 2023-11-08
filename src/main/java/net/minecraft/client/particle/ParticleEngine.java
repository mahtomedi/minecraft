package net.minecraft.client.particle;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleGroup;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ParticleEngine implements PreparableReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final FileToIdConverter PARTICLE_LISTER = FileToIdConverter.json("particles");
    private static final ResourceLocation PARTICLES_ATLAS_INFO = new ResourceLocation("particles");
    private static final int MAX_PARTICLES_PER_LAYER = 16384;
    private static final List<ParticleRenderType> RENDER_ORDER = ImmutableList.of(
        ParticleRenderType.TERRAIN_SHEET,
        ParticleRenderType.PARTICLE_SHEET_OPAQUE,
        ParticleRenderType.PARTICLE_SHEET_LIT,
        ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT,
        ParticleRenderType.CUSTOM
    );
    protected ClientLevel level;
    private final Map<ParticleRenderType, Queue<Particle>> particles = Maps.newIdentityHashMap();
    private final Queue<TrackingEmitter> trackingEmitters = Queues.newArrayDeque();
    private final TextureManager textureManager;
    private final RandomSource random = RandomSource.create();
    private final Int2ObjectMap<ParticleProvider<?>> providers = new Int2ObjectOpenHashMap<>();
    private final Queue<Particle> particlesToAdd = Queues.newArrayDeque();
    private final Map<ResourceLocation, ParticleEngine.MutableSpriteSet> spriteSets = Maps.newHashMap();
    private final TextureAtlas textureAtlas;
    private final Object2IntOpenHashMap<ParticleGroup> trackedParticleCounts = new Object2IntOpenHashMap<>();

    public ParticleEngine(ClientLevel param0, TextureManager param1) {
        this.textureAtlas = new TextureAtlas(TextureAtlas.LOCATION_PARTICLES);
        param1.register(this.textureAtlas.location(), this.textureAtlas);
        this.level = param0;
        this.textureManager = param1;
        this.registerProviders();
    }

    private void registerProviders() {
        this.register(ParticleTypes.AMBIENT_ENTITY_EFFECT, SpellParticle.AmbientMobProvider::new);
        this.register(ParticleTypes.ANGRY_VILLAGER, HeartParticle.AngryVillagerProvider::new);
        this.register(ParticleTypes.BLOCK_MARKER, new BlockMarker.Provider());
        this.register(ParticleTypes.BLOCK, new TerrainParticle.Provider());
        this.register(ParticleTypes.BUBBLE, BubbleParticle.Provider::new);
        this.register(ParticleTypes.BUBBLE_COLUMN_UP, BubbleColumnUpParticle.Provider::new);
        this.register(ParticleTypes.BUBBLE_POP, BubblePopParticle.Provider::new);
        this.register(ParticleTypes.CAMPFIRE_COSY_SMOKE, CampfireSmokeParticle.CosyProvider::new);
        this.register(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, CampfireSmokeParticle.SignalProvider::new);
        this.register(ParticleTypes.CLOUD, PlayerCloudParticle.Provider::new);
        this.register(ParticleTypes.COMPOSTER, SuspendedTownParticle.ComposterFillProvider::new);
        this.register(ParticleTypes.CRIT, CritParticle.Provider::new);
        this.register(ParticleTypes.CURRENT_DOWN, WaterCurrentDownParticle.Provider::new);
        this.register(ParticleTypes.DAMAGE_INDICATOR, CritParticle.DamageIndicatorProvider::new);
        this.register(ParticleTypes.DRAGON_BREATH, DragonBreathParticle.Provider::new);
        this.register(ParticleTypes.DOLPHIN, SuspendedTownParticle.DolphinSpeedProvider::new);
        this.register(ParticleTypes.DRIPPING_LAVA, DripParticle::createLavaHangParticle);
        this.register(ParticleTypes.FALLING_LAVA, DripParticle::createLavaFallParticle);
        this.register(ParticleTypes.LANDING_LAVA, DripParticle::createLavaLandParticle);
        this.register(ParticleTypes.DRIPPING_WATER, DripParticle::createWaterHangParticle);
        this.register(ParticleTypes.FALLING_WATER, DripParticle::createWaterFallParticle);
        this.register(ParticleTypes.DUST, DustParticle.Provider::new);
        this.register(ParticleTypes.DUST_COLOR_TRANSITION, DustColorTransitionParticle.Provider::new);
        this.register(ParticleTypes.EFFECT, SpellParticle.Provider::new);
        this.register(ParticleTypes.ELDER_GUARDIAN, new MobAppearanceParticle.Provider());
        this.register(ParticleTypes.ENCHANTED_HIT, CritParticle.MagicProvider::new);
        this.register(ParticleTypes.ENCHANT, EnchantmentTableParticle.Provider::new);
        this.register(ParticleTypes.END_ROD, EndRodParticle.Provider::new);
        this.register(ParticleTypes.ENTITY_EFFECT, SpellParticle.MobProvider::new);
        this.register(ParticleTypes.EXPLOSION_EMITTER, new HugeExplosionSeedParticle.Provider());
        this.register(ParticleTypes.EXPLOSION, HugeExplosionParticle.Provider::new);
        this.register(ParticleTypes.SONIC_BOOM, SonicBoomParticle.Provider::new);
        this.register(ParticleTypes.FALLING_DUST, FallingDustParticle.Provider::new);
        this.register(ParticleTypes.GUST, GustParticle.Provider::new);
        this.register(ParticleTypes.GUST_EMITTER, new GustSeedParticle.Provider());
        this.register(ParticleTypes.FIREWORK, FireworkParticles.SparkProvider::new);
        this.register(ParticleTypes.FISHING, WakeParticle.Provider::new);
        this.register(ParticleTypes.FLAME, FlameParticle.Provider::new);
        this.register(ParticleTypes.SCULK_SOUL, SoulParticle.EmissiveProvider::new);
        this.register(ParticleTypes.SCULK_CHARGE, SculkChargeParticle.Provider::new);
        this.register(ParticleTypes.SCULK_CHARGE_POP, SculkChargePopParticle.Provider::new);
        this.register(ParticleTypes.SOUL, SoulParticle.Provider::new);
        this.register(ParticleTypes.SOUL_FIRE_FLAME, FlameParticle.Provider::new);
        this.register(ParticleTypes.FLASH, FireworkParticles.FlashProvider::new);
        this.register(ParticleTypes.HAPPY_VILLAGER, SuspendedTownParticle.HappyVillagerProvider::new);
        this.register(ParticleTypes.HEART, HeartParticle.Provider::new);
        this.register(ParticleTypes.INSTANT_EFFECT, SpellParticle.InstantProvider::new);
        this.register(ParticleTypes.ITEM, new BreakingItemParticle.Provider());
        this.register(ParticleTypes.ITEM_SLIME, new BreakingItemParticle.SlimeProvider());
        this.register(ParticleTypes.ITEM_SNOWBALL, new BreakingItemParticle.SnowballProvider());
        this.register(ParticleTypes.LARGE_SMOKE, LargeSmokeParticle.Provider::new);
        this.register(ParticleTypes.LAVA, LavaParticle.Provider::new);
        this.register(ParticleTypes.MYCELIUM, SuspendedTownParticle.Provider::new);
        this.register(ParticleTypes.NAUTILUS, EnchantmentTableParticle.NautilusProvider::new);
        this.register(ParticleTypes.NOTE, NoteParticle.Provider::new);
        this.register(ParticleTypes.POOF, ExplodeParticle.Provider::new);
        this.register(ParticleTypes.PORTAL, PortalParticle.Provider::new);
        this.register(ParticleTypes.RAIN, WaterDropParticle.Provider::new);
        this.register(ParticleTypes.SMOKE, SmokeParticle.Provider::new);
        this.register(ParticleTypes.WHITE_SMOKE, WhiteSmokeParticle.Provider::new);
        this.register(ParticleTypes.SNEEZE, PlayerCloudParticle.SneezeProvider::new);
        this.register(ParticleTypes.SNOWFLAKE, SnowflakeParticle.Provider::new);
        this.register(ParticleTypes.SPIT, SpitParticle.Provider::new);
        this.register(ParticleTypes.SWEEP_ATTACK, AttackSweepParticle.Provider::new);
        this.register(ParticleTypes.TOTEM_OF_UNDYING, TotemParticle.Provider::new);
        this.register(ParticleTypes.SQUID_INK, SquidInkParticle.Provider::new);
        this.register(ParticleTypes.UNDERWATER, SuspendedParticle.UnderwaterProvider::new);
        this.register(ParticleTypes.SPLASH, SplashParticle.Provider::new);
        this.register(ParticleTypes.WITCH, SpellParticle.WitchProvider::new);
        this.register(ParticleTypes.DRIPPING_HONEY, DripParticle::createHoneyHangParticle);
        this.register(ParticleTypes.FALLING_HONEY, DripParticle::createHoneyFallParticle);
        this.register(ParticleTypes.LANDING_HONEY, DripParticle::createHoneyLandParticle);
        this.register(ParticleTypes.FALLING_NECTAR, DripParticle::createNectarFallParticle);
        this.register(ParticleTypes.FALLING_SPORE_BLOSSOM, DripParticle::createSporeBlossomFallParticle);
        this.register(ParticleTypes.SPORE_BLOSSOM_AIR, SuspendedParticle.SporeBlossomAirProvider::new);
        this.register(ParticleTypes.ASH, AshParticle.Provider::new);
        this.register(ParticleTypes.CRIMSON_SPORE, SuspendedParticle.CrimsonSporeProvider::new);
        this.register(ParticleTypes.WARPED_SPORE, SuspendedParticle.WarpedSporeProvider::new);
        this.register(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, DripParticle::createObsidianTearHangParticle);
        this.register(ParticleTypes.FALLING_OBSIDIAN_TEAR, DripParticle::createObsidianTearFallParticle);
        this.register(ParticleTypes.LANDING_OBSIDIAN_TEAR, DripParticle::createObsidianTearLandParticle);
        this.register(ParticleTypes.REVERSE_PORTAL, ReversePortalParticle.ReversePortalProvider::new);
        this.register(ParticleTypes.WHITE_ASH, WhiteAshParticle.Provider::new);
        this.register(ParticleTypes.SMALL_FLAME, FlameParticle.SmallFlameProvider::new);
        this.register(ParticleTypes.DRIPPING_DRIPSTONE_WATER, DripParticle::createDripstoneWaterHangParticle);
        this.register(ParticleTypes.FALLING_DRIPSTONE_WATER, DripParticle::createDripstoneWaterFallParticle);
        this.register(
            ParticleTypes.CHERRY_LEAVES,
            param0 -> (param1, param2, param3, param4, param5, param6, param7, param8) -> new CherryParticle(param2, param3, param4, param5, param0)
        );
        this.register(ParticleTypes.DRIPPING_DRIPSTONE_LAVA, DripParticle::createDripstoneLavaHangParticle);
        this.register(ParticleTypes.FALLING_DRIPSTONE_LAVA, DripParticle::createDripstoneLavaFallParticle);
        this.register(ParticleTypes.VIBRATION, VibrationSignalParticle.Provider::new);
        this.register(ParticleTypes.GLOW_SQUID_INK, SquidInkParticle.GlowInkProvider::new);
        this.register(ParticleTypes.GLOW, GlowParticle.GlowSquidProvider::new);
        this.register(ParticleTypes.WAX_ON, GlowParticle.WaxOnProvider::new);
        this.register(ParticleTypes.WAX_OFF, GlowParticle.WaxOffProvider::new);
        this.register(ParticleTypes.ELECTRIC_SPARK, GlowParticle.ElectricSparkProvider::new);
        this.register(ParticleTypes.SCRAPE, GlowParticle.ScrapeProvider::new);
        this.register(ParticleTypes.SHRIEK, ShriekParticle.Provider::new);
        this.register(ParticleTypes.EGG_CRACK, SuspendedTownParticle.EggCrackProvider::new);
        this.register(ParticleTypes.DUST_PLUME, DustPlumeParticle.Provider::new);
        this.register(ParticleTypes.GUST_DUST, GustDustParticle.GustDustParticleProvider::new);
        this.register(ParticleTypes.TRIAL_SPAWNER_DETECTION, TrialSpawnerDetectionParticle.Provider::new);
    }

    private <T extends ParticleOptions> void register(ParticleType<T> param0, ParticleProvider<T> param1) {
        this.providers.put(BuiltInRegistries.PARTICLE_TYPE.getId(param0), param1);
    }

    private <T extends ParticleOptions> void register(ParticleType<T> param0, ParticleProvider.Sprite<T> param1) {
        this.register(param0, param1x -> (param2, param3, param4, param5, param6, param7, param8, param9) -> {
                TextureSheetParticle var0x = param1.createParticle(param2, param3, param4, param5, param6, param7, param8, param9);
                if (var0x != null) {
                    var0x.pickSprite(param1x);
                }

                return var0x;
            });
    }

    private <T extends ParticleOptions> void register(ParticleType<T> param0, ParticleEngine.SpriteParticleRegistration<T> param1) {
        ParticleEngine.MutableSpriteSet var0 = new ParticleEngine.MutableSpriteSet();
        this.spriteSets.put(BuiltInRegistries.PARTICLE_TYPE.getKey(param0), var0);
        this.providers.put(BuiltInRegistries.PARTICLE_TYPE.getId(param0), param1.create(var0));
    }

    @Override
    public CompletableFuture<Void> reload(
        PreparableReloadListener.PreparationBarrier param0,
        ResourceManager param1,
        ProfilerFiller param2,
        ProfilerFiller param3,
        Executor param4,
        Executor param5
    ) {
        @OnlyIn(Dist.CLIENT)
        record ParticleDefinition(ResourceLocation id, Optional<List<ResourceLocation>> sprites) {
        }

        CompletableFuture<List<ParticleDefinition>> var0 = CompletableFuture.<Map<ResourceLocation, Resource>>supplyAsync(
                () -> PARTICLE_LISTER.listMatchingResources(param1), param4
            )
            .thenCompose(param1x -> {
                List<CompletableFuture<ParticleDefinition>> var0x = new ArrayList<>(param1x.size());
                param1x.forEach((param2x, param3x) -> {
                    ResourceLocation var0xx = PARTICLE_LISTER.fileToId(param2x);
                    var0x.add(CompletableFuture.supplyAsync(() -> new ParticleDefinition(var0xx, this.loadParticleDescription(var0xx, param3x)), param4));
                });
                return Util.sequence(var0x);
            });
        CompletableFuture<SpriteLoader.Preparations> var1 = SpriteLoader.create(this.textureAtlas)
            .loadAndStitch(param1, PARTICLES_ATLAS_INFO, 0, param4)
            .thenCompose(SpriteLoader.Preparations::waitForUpload);
        return CompletableFuture.allOf(var1, var0).thenCompose(param0::wait).thenAcceptAsync(param3x -> {
            this.clearParticles();
            param3.startTick();
            param3.push("upload");
            SpriteLoader.Preparations var0x = var1.join();
            this.textureAtlas.upload(var0x);
            param3.popPush("bindSpriteSets");
            Set<ResourceLocation> var1x = new HashSet<>();
            TextureAtlasSprite var2x = var0x.missing();
            var0.join().forEach(param3xx -> {
                Optional<List<ResourceLocation>> var0xx = param3xx.sprites();
                if (!var0xx.isEmpty()) {
                    List<TextureAtlasSprite> var1xx = new ArrayList();

                    for(ResourceLocation var2xx : (List)var0xx.get()) {
                        TextureAtlasSprite var3x = var0x.regions().get(var2xx);
                        if (var3x == null) {
                            var1x.add(var2xx);
                            var1xx.add(var2x);
                        } else {
                            var1xx.add(var3x);
                        }
                    }

                    if (var1xx.isEmpty()) {
                        var1xx.add(var2x);
                    }

                    this.spriteSets.get(param3xx.id()).rebind(var1xx);
                }
            });
            if (!var1x.isEmpty()) {
                LOGGER.warn("Missing particle sprites: {}", var1x.stream().sorted().map(ResourceLocation::toString).collect(Collectors.joining(",")));
            }

            param3.pop();
            param3.endTick();
        }, param5);
    }

    public void close() {
        this.textureAtlas.clearTextureData();
    }

    private Optional<List<ResourceLocation>> loadParticleDescription(ResourceLocation param0, Resource param1) {
        if (!this.spriteSets.containsKey(param0)) {
            LOGGER.debug("Redundant texture list for particle: {}", param0);
            return Optional.empty();
        } else {
            try {
                Optional var5;
                try (Reader var0 = param1.openAsReader()) {
                    ParticleDescription var1 = ParticleDescription.fromJson(GsonHelper.parse(var0));
                    var5 = Optional.of(var1.getTextures());
                }

                return var5;
            } catch (IOException var8) {
                throw new IllegalStateException("Failed to load description for particle " + param0, var8);
            }
        }
    }

    public void createTrackingEmitter(Entity param0, ParticleOptions param1) {
        this.trackingEmitters.add(new TrackingEmitter(this.level, param0, param1));
    }

    public void createTrackingEmitter(Entity param0, ParticleOptions param1, int param2) {
        this.trackingEmitters.add(new TrackingEmitter(this.level, param0, param1, param2));
    }

    @Nullable
    public Particle createParticle(ParticleOptions param0, double param1, double param2, double param3, double param4, double param5, double param6) {
        Particle var0 = this.makeParticle(param0, param1, param2, param3, param4, param5, param6);
        if (var0 != null) {
            this.add(var0);
            return var0;
        } else {
            return null;
        }
    }

    @Nullable
    private <T extends ParticleOptions> Particle makeParticle(
        T param0, double param1, double param2, double param3, double param4, double param5, double param6
    ) {
        ParticleProvider<T> var0 = (ParticleProvider)this.providers.get(BuiltInRegistries.PARTICLE_TYPE.getId(param0.getType()));
        return var0 == null ? null : var0.createParticle(param0, this.level, param1, param2, param3, param4, param5, param6);
    }

    public void add(Particle param0) {
        Optional<ParticleGroup> var0 = param0.getParticleGroup();
        if (var0.isPresent()) {
            if (this.hasSpaceInParticleLimit(var0.get())) {
                this.particlesToAdd.add(param0);
                this.updateCount(var0.get(), 1);
            }
        } else {
            this.particlesToAdd.add(param0);
        }

    }

    public void tick() {
        this.particles.forEach((param0, param1) -> {
            this.level.getProfiler().push(param0.toString());
            this.tickParticleList(param1);
            this.level.getProfiler().pop();
        });
        if (!this.trackingEmitters.isEmpty()) {
            List<TrackingEmitter> var0 = Lists.newArrayList();

            for(TrackingEmitter var1 : this.trackingEmitters) {
                var1.tick();
                if (!var1.isAlive()) {
                    var0.add(var1);
                }
            }

            this.trackingEmitters.removeAll(var0);
        }

        Particle var2;
        if (!this.particlesToAdd.isEmpty()) {
            while((var2 = this.particlesToAdd.poll()) != null) {
                this.particles.computeIfAbsent(var2.getRenderType(), param0 -> EvictingQueue.create(16384)).add(var2);
            }
        }

    }

    private void tickParticleList(Collection<Particle> param0) {
        if (!param0.isEmpty()) {
            Iterator<Particle> var0 = param0.iterator();

            while(var0.hasNext()) {
                Particle var1 = var0.next();
                this.tickParticle(var1);
                if (!var1.isAlive()) {
                    var1.getParticleGroup().ifPresent(param0x -> this.updateCount(param0x, -1));
                    var0.remove();
                }
            }
        }

    }

    private void updateCount(ParticleGroup param0, int param1) {
        this.trackedParticleCounts.addTo(param0, param1);
    }

    private void tickParticle(Particle param0) {
        try {
            param0.tick();
        } catch (Throwable var5) {
            CrashReport var1 = CrashReport.forThrowable(var5, "Ticking Particle");
            CrashReportCategory var2 = var1.addCategory("Particle being ticked");
            var2.setDetail("Particle", param0::toString);
            var2.setDetail("Particle Type", param0.getRenderType()::toString);
            throw new ReportedException(var1);
        }
    }

    public void render(PoseStack param0, MultiBufferSource.BufferSource param1, LightTexture param2, Camera param3, float param4) {
        param2.turnOnLightLayer();
        RenderSystem.enableDepthTest();
        PoseStack var0 = RenderSystem.getModelViewStack();
        var0.pushPose();
        var0.mulPoseMatrix(param0.last().pose());
        RenderSystem.applyModelViewMatrix();

        for(ParticleRenderType var1 : RENDER_ORDER) {
            Iterable<Particle> var2 = this.particles.get(var1);
            if (var2 != null) {
                RenderSystem.setShader(GameRenderer::getParticleShader);
                Tesselator var3 = Tesselator.getInstance();
                BufferBuilder var4 = var3.getBuilder();
                var1.begin(var4, this.textureManager);

                for(Particle var5 : var2) {
                    try {
                        var5.render(var4, param3, param4);
                    } catch (Throwable var17) {
                        CrashReport var7 = CrashReport.forThrowable(var17, "Rendering Particle");
                        CrashReportCategory var8 = var7.addCategory("Particle being rendered");
                        var8.setDetail("Particle", var5::toString);
                        var8.setDetail("Particle Type", var1::toString);
                        throw new ReportedException(var7);
                    }
                }

                var1.end(var3);
            }
        }

        var0.popPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        param2.turnOffLightLayer();
    }

    public void setLevel(@Nullable ClientLevel param0) {
        this.level = param0;
        this.clearParticles();
        this.trackingEmitters.clear();
    }

    public void destroy(BlockPos param0, BlockState param1) {
        if (!param1.isAir() && param1.shouldSpawnTerrainParticles()) {
            VoxelShape var0 = param1.getShape(this.level, param0);
            double var1 = 0.25;
            var0.forAllBoxes(
                (param2, param3, param4, param5, param6, param7) -> {
                    double var0x = Math.min(1.0, param5 - param2);
                    double var1x = Math.min(1.0, param6 - param3);
                    double var2x = Math.min(1.0, param7 - param4);
                    int var3x = Math.max(2, Mth.ceil(var0x / 0.25));
                    int var4x = Math.max(2, Mth.ceil(var1x / 0.25));
                    int var5 = Math.max(2, Mth.ceil(var2x / 0.25));
    
                    for(int var6 = 0; var6 < var3x; ++var6) {
                        for(int var7 = 0; var7 < var4x; ++var7) {
                            for(int var8 = 0; var8 < var5; ++var8) {
                                double var9 = ((double)var6 + 0.5) / (double)var3x;
                                double var10 = ((double)var7 + 0.5) / (double)var4x;
                                double var11 = ((double)var8 + 0.5) / (double)var5;
                                double var12 = var9 * var0x + param2;
                                double var13 = var10 * var1x + param3;
                                double var14 = var11 * var2x + param4;
                                this.add(
                                    new TerrainParticle(
                                        this.level,
                                        (double)param0.getX() + var12,
                                        (double)param0.getY() + var13,
                                        (double)param0.getZ() + var14,
                                        var9 - 0.5,
                                        var10 - 0.5,
                                        var11 - 0.5,
                                        param1,
                                        param0
                                    )
                                );
                            }
                        }
                    }
    
                }
            );
        }
    }

    public void crack(BlockPos param0, Direction param1) {
        BlockState var0 = this.level.getBlockState(param0);
        if (var0.getRenderShape() != RenderShape.INVISIBLE && var0.shouldSpawnTerrainParticles()) {
            int var1 = param0.getX();
            int var2 = param0.getY();
            int var3 = param0.getZ();
            float var4 = 0.1F;
            AABB var5 = var0.getShape(this.level, param0).bounds();
            double var6 = (double)var1 + this.random.nextDouble() * (var5.maxX - var5.minX - 0.2F) + 0.1F + var5.minX;
            double var7 = (double)var2 + this.random.nextDouble() * (var5.maxY - var5.minY - 0.2F) + 0.1F + var5.minY;
            double var8 = (double)var3 + this.random.nextDouble() * (var5.maxZ - var5.minZ - 0.2F) + 0.1F + var5.minZ;
            if (param1 == Direction.DOWN) {
                var7 = (double)var2 + var5.minY - 0.1F;
            }

            if (param1 == Direction.UP) {
                var7 = (double)var2 + var5.maxY + 0.1F;
            }

            if (param1 == Direction.NORTH) {
                var8 = (double)var3 + var5.minZ - 0.1F;
            }

            if (param1 == Direction.SOUTH) {
                var8 = (double)var3 + var5.maxZ + 0.1F;
            }

            if (param1 == Direction.WEST) {
                var6 = (double)var1 + var5.minX - 0.1F;
            }

            if (param1 == Direction.EAST) {
                var6 = (double)var1 + var5.maxX + 0.1F;
            }

            this.add(new TerrainParticle(this.level, var6, var7, var8, 0.0, 0.0, 0.0, var0, param0).setPower(0.2F).scale(0.6F));
        }
    }

    public String countParticles() {
        return String.valueOf(this.particles.values().stream().mapToInt(Collection::size).sum());
    }

    private boolean hasSpaceInParticleLimit(ParticleGroup param0) {
        return this.trackedParticleCounts.getInt(param0) < param0.getLimit();
    }

    private void clearParticles() {
        this.particles.clear();
        this.particlesToAdd.clear();
        this.trackingEmitters.clear();
        this.trackedParticleCounts.clear();
    }

    @OnlyIn(Dist.CLIENT)
    static class MutableSpriteSet implements SpriteSet {
        private List<TextureAtlasSprite> sprites;

        @Override
        public TextureAtlasSprite get(int param0, int param1) {
            return this.sprites.get(param0 * (this.sprites.size() - 1) / param1);
        }

        @Override
        public TextureAtlasSprite get(RandomSource param0) {
            return this.sprites.get(param0.nextInt(this.sprites.size()));
        }

        public void rebind(List<TextureAtlasSprite> param0) {
            this.sprites = ImmutableList.copyOf(param0);
        }
    }

    @FunctionalInterface
    @OnlyIn(Dist.CLIENT)
    interface SpriteParticleRegistration<T extends ParticleOptions> {
        ParticleProvider<T> create(SpriteSet var1);
    }
}
