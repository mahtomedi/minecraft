package net.minecraft.client.particle;

import com.google.common.base.Charsets;
import com.google.common.collect.EvictingQueue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ParticleEngine implements PreparableReloadListener {
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
    private final Random random = new Random();
    private final Int2ObjectMap<ParticleProvider<?>> providers = new Int2ObjectOpenHashMap<>();
    private final Queue<Particle> particlesToAdd = Queues.newArrayDeque();
    private final Map<ResourceLocation, ParticleEngine.MutableSpriteSet> spriteSets = Maps.newHashMap();
    private final TextureAtlas textureAtlas = new TextureAtlas(TextureAtlas.LOCATION_PARTICLES);

    public ParticleEngine(ClientLevel param0, TextureManager param1) {
        param1.register(this.textureAtlas.location(), this.textureAtlas);
        this.level = param0;
        this.textureManager = param1;
        this.registerProviders();
    }

    private void registerProviders() {
        this.register(ParticleTypes.AMBIENT_ENTITY_EFFECT, SpellParticle.AmbientMobProvider::new);
        this.register(ParticleTypes.ANGRY_VILLAGER, HeartParticle.AngryVillagerProvider::new);
        this.register(ParticleTypes.BARRIER, new BarrierParticle.Provider());
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
        this.register(ParticleTypes.DRIPPING_LAVA, DripParticle.LavaHangProvider::new);
        this.register(ParticleTypes.FALLING_LAVA, DripParticle.LavaFallProvider::new);
        this.register(ParticleTypes.LANDING_LAVA, DripParticle.LavaLandProvider::new);
        this.register(ParticleTypes.DRIPPING_WATER, DripParticle.WaterHangProvider::new);
        this.register(ParticleTypes.FALLING_WATER, DripParticle.WaterFallProvider::new);
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
        this.register(ParticleTypes.FALLING_DUST, FallingDustParticle.Provider::new);
        this.register(ParticleTypes.FIREWORK, FireworkParticles.SparkProvider::new);
        this.register(ParticleTypes.FISHING, WakeParticle.Provider::new);
        this.register(ParticleTypes.FLAME, FlameParticle.Provider::new);
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
        this.register(ParticleTypes.SNEEZE, PlayerCloudParticle.SneezeProvider::new);
        this.register(ParticleTypes.SNOWFLAKE, SnowflakeParticle.Provider::new);
        this.register(ParticleTypes.SPIT, SpitParticle.Provider::new);
        this.register(ParticleTypes.SWEEP_ATTACK, AttackSweepParticle.Provider::new);
        this.register(ParticleTypes.TOTEM_OF_UNDYING, TotemParticle.Provider::new);
        this.register(ParticleTypes.SQUID_INK, SquidInkParticle.Provider::new);
        this.register(ParticleTypes.UNDERWATER, SuspendedParticle.UnderwaterProvider::new);
        this.register(ParticleTypes.SPLASH, SplashParticle.Provider::new);
        this.register(ParticleTypes.WITCH, SpellParticle.WitchProvider::new);
        this.register(ParticleTypes.DRIPPING_HONEY, DripParticle.HoneyHangProvider::new);
        this.register(ParticleTypes.FALLING_HONEY, DripParticle.HoneyFallProvider::new);
        this.register(ParticleTypes.LANDING_HONEY, DripParticle.HoneyLandProvider::new);
        this.register(ParticleTypes.FALLING_NECTAR, DripParticle.NectarFallProvider::new);
        this.register(ParticleTypes.ASH, AshParticle.Provider::new);
        this.register(ParticleTypes.CRIMSON_SPORE, SuspendedParticle.CrimsonSporeProvider::new);
        this.register(ParticleTypes.WARPED_SPORE, SuspendedParticle.WarpedSporeProvider::new);
        this.register(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, DripParticle.ObsidianTearHangProvider::new);
        this.register(ParticleTypes.FALLING_OBSIDIAN_TEAR, DripParticle.ObsidianTearFallProvider::new);
        this.register(ParticleTypes.LANDING_OBSIDIAN_TEAR, DripParticle.ObsidianTearLandProvider::new);
        this.register(ParticleTypes.REVERSE_PORTAL, ReversePortalParticle.ReversePortalProvider::new);
        this.register(ParticleTypes.WHITE_ASH, WhiteAshParticle.Provider::new);
        this.register(ParticleTypes.SMALL_FLAME, FlameParticle.SmallFlameProvider::new);
        this.register(ParticleTypes.DRIPPING_DRIPSTONE_WATER, DripParticle.DripstoneWaterHangProvider::new);
        this.register(ParticleTypes.FALLING_DRIPSTONE_WATER, DripParticle.DripstoneWaterFallProvider::new);
        this.register(ParticleTypes.DRIPPING_DRIPSTONE_LAVA, DripParticle.DripstoneLavaHangProvider::new);
        this.register(ParticleTypes.FALLING_DRIPSTONE_LAVA, DripParticle.DripstoneLavaFallProvider::new);
        this.register(ParticleTypes.VIBRATION, VibrationSignalParticle.Provider::new);
        this.register(ParticleTypes.GLOW_SQUID_INK, SquidInkParticle.GlowInkProvider::new);
        this.register(ParticleTypes.GLOW, GlowParticle.Provider::new);
    }

    private <T extends ParticleOptions> void register(ParticleType<T> param0, ParticleProvider<T> param1) {
        this.providers.put(Registry.PARTICLE_TYPE.getId(param0), param1);
    }

    private <T extends ParticleOptions> void register(ParticleType<T> param0, ParticleEngine.SpriteParticleRegistration<T> param1) {
        ParticleEngine.MutableSpriteSet var0 = new ParticleEngine.MutableSpriteSet();
        this.spriteSets.put(Registry.PARTICLE_TYPE.getKey(param0), var0);
        this.providers.put(Registry.PARTICLE_TYPE.getId(param0), param1.create(var0));
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
        Map<ResourceLocation, List<ResourceLocation>> var0 = Maps.newConcurrentMap();
        CompletableFuture<?>[] var1 = Registry.PARTICLE_TYPE
            .keySet()
            .stream()
            .map(param3x -> CompletableFuture.runAsync(() -> this.loadParticleDescription(param1, param3x, var0), param4))
            .toArray(param0x -> new CompletableFuture[param0x]);
        return CompletableFuture.allOf(var1)
            .thenApplyAsync(param3x -> {
                param2.startTick();
                param2.push("stitching");
                TextureAtlas.Preparations var0x = this.textureAtlas.prepareToStitch(param1, var0.values().stream().flatMap(Collection::stream), param2, 0);
                param2.pop();
                param2.endTick();
                return var0x;
            }, param4)
            .thenCompose(param0::wait)
            .thenAcceptAsync(
                param2x -> {
                    this.particles.clear();
                    param3.startTick();
                    param3.push("upload");
                    this.textureAtlas.reload(param2x);
                    param3.popPush("bindSpriteSets");
                    TextureAtlasSprite var0x = this.textureAtlas.getSprite(MissingTextureAtlasSprite.getLocation());
                    var0.forEach(
                        (param1x, param2xx) -> {
                            ImmutableList<TextureAtlasSprite> var0xx = param2xx.isEmpty()
                                ? ImmutableList.of(var0x)
                                : param2xx.stream().map(this.textureAtlas::getSprite).collect(ImmutableList.toImmutableList());
                            this.spriteSets.get(param1x).rebind(var0xx);
                        }
                    );
                    param3.pop();
                    param3.endTick();
                },
                param5
            );
    }

    public void close() {
        this.textureAtlas.clearTextureData();
    }

    private void loadParticleDescription(ResourceManager param0, ResourceLocation param1, Map<ResourceLocation, List<ResourceLocation>> param2) {
        ResourceLocation var0 = new ResourceLocation(param1.getNamespace(), "particles/" + param1.getPath() + ".json");

        try (
            Resource var1 = param0.getResource(var0);
            Reader var2 = new InputStreamReader(var1.getInputStream(), Charsets.UTF_8);
        ) {
            ParticleDescription var3 = ParticleDescription.fromJson(GsonHelper.parse(var2));
            List<ResourceLocation> var4 = var3.getTextures();
            boolean var5 = this.spriteSets.containsKey(param1);
            if (var4 == null) {
                if (var5) {
                    throw new IllegalStateException("Missing texture list for particle " + param1);
                }
            } else {
                if (!var5) {
                    throw new IllegalStateException("Redundant texture list for particle " + param1);
                }

                param2.put(
                    param1,
                    var4.stream().map(param0x -> new ResourceLocation(param0x.getNamespace(), "particle/" + param0x.getPath())).collect(Collectors.toList())
                );
            }

        } catch (IOException var39) {
            throw new IllegalStateException("Failed to load description for particle " + param1, var39);
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
        ParticleProvider<T> var0 = (ParticleProvider)this.providers.get(Registry.PARTICLE_TYPE.getId(param0.getType()));
        return var0 == null ? null : var0.createParticle(param0, this.level, param1, param2, param3, param4, param5, param6);
    }

    public void add(Particle param0) {
        this.particlesToAdd.add(param0);
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
                    var0.remove();
                }
            }
        }

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
        RenderSystem.enableAlphaTest();
        RenderSystem.defaultAlphaFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.enableFog();
        RenderSystem.pushMatrix();
        RenderSystem.multMatrix(param0.last().pose());

        for(ParticleRenderType var0 : RENDER_ORDER) {
            Iterable<Particle> var1 = this.particles.get(var0);
            if (var1 != null) {
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                Tesselator var2 = Tesselator.getInstance();
                BufferBuilder var3 = var2.getBuilder();
                var0.begin(var3, this.textureManager);

                for(Particle var4 : var1) {
                    try {
                        var4.render(var3, param3, param4);
                    } catch (Throwable var16) {
                        CrashReport var6 = CrashReport.forThrowable(var16, "Rendering Particle");
                        CrashReportCategory var7 = var6.addCategory("Particle being rendered");
                        var7.setDetail("Particle", var4::toString);
                        var7.setDetail("Particle Type", var0::toString);
                        throw new ReportedException(var6);
                    }
                }

                var0.end(var2);
            }
        }

        RenderSystem.popMatrix();
        RenderSystem.depthMask(true);
        RenderSystem.depthFunc(515);
        RenderSystem.disableBlend();
        RenderSystem.defaultAlphaFunc();
        param2.turnOffLightLayer();
        RenderSystem.disableFog();
    }

    public void setLevel(@Nullable ClientLevel param0) {
        this.level = param0;
        this.particles.clear();
        this.trackingEmitters.clear();
    }

    public void destroy(BlockPos param0, BlockState param1) {
        if (!param1.isAir()) {
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
                                            param1
                                        )
                                        .init(param0)
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
        if (var0.getRenderShape() != RenderShape.INVISIBLE) {
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

            this.add(new TerrainParticle(this.level, var6, var7, var8, 0.0, 0.0, 0.0, var0).init(param0).setPower(0.2F).scale(0.6F));
        }
    }

    public String countParticles() {
        return String.valueOf(this.particles.values().stream().mapToInt(Collection::size).sum());
    }

    @OnlyIn(Dist.CLIENT)
    class MutableSpriteSet implements SpriteSet {
        private List<TextureAtlasSprite> sprites;

        private MutableSpriteSet() {
        }

        @Override
        public TextureAtlasSprite get(int param0, int param1) {
            return this.sprites.get(param0 * (this.sprites.size() - 1) / param1);
        }

        @Override
        public TextureAtlasSprite get(Random param0) {
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
