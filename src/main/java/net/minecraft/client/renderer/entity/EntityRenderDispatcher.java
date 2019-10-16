package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EntityRenderDispatcher {
    private static final ResourceLocation SHADOW_LOCATION = new ResourceLocation("textures/misc/shadow.png");
    private final Map<EntityType<?>, EntityRenderer<?>> renderers = Maps.newHashMap();
    private final Map<String, PlayerRenderer> playerRenderers = Maps.newHashMap();
    private final PlayerRenderer defaultPlayerRenderer;
    private final Font font;
    public final TextureManager textureManager;
    private Level level;
    private Camera camera;
    public Entity crosshairPickEntity;
    public float playerRotY;
    public float playerRotX;
    public final Options options;
    private boolean shouldRenderShadow = true;
    private boolean renderHitBoxes;

    private <T extends Entity> void register(EntityType<T> param0, EntityRenderer<? super T> param1) {
        this.renderers.put(param0, param1);
    }

    private void registerRenderers(ItemRenderer param0, ReloadableResourceManager param1) {
        this.register(EntityType.AREA_EFFECT_CLOUD, new AreaEffectCloudRenderer(this));
        this.register(EntityType.ARMOR_STAND, new ArmorStandRenderer(this));
        this.register(EntityType.ARROW, new TippableArrowRenderer(this));
        this.register(EntityType.BAT, new BatRenderer(this));
        this.register(EntityType.BEE, new BeeRenderer(this));
        this.register(EntityType.BLAZE, new BlazeRenderer(this));
        this.register(EntityType.BOAT, new BoatRenderer(this));
        this.register(EntityType.CAT, new CatRenderer(this));
        this.register(EntityType.CAVE_SPIDER, new CaveSpiderRenderer(this));
        this.register(EntityType.CHEST_MINECART, new MinecartRenderer<>(this));
        this.register(EntityType.CHICKEN, new ChickenRenderer(this));
        this.register(EntityType.COD, new CodRenderer(this));
        this.register(EntityType.COMMAND_BLOCK_MINECART, new MinecartRenderer<>(this));
        this.register(EntityType.COW, new CowRenderer(this));
        this.register(EntityType.CREEPER, new CreeperRenderer(this));
        this.register(EntityType.DOLPHIN, new DolphinRenderer(this));
        this.register(EntityType.DONKEY, new ChestedHorseRenderer<>(this, 0.87F));
        this.register(EntityType.DRAGON_FIREBALL, new DragonFireballRenderer(this));
        this.register(EntityType.DROWNED, new DrownedRenderer(this));
        this.register(EntityType.EGG, new ThrownItemRenderer<>(this, param0));
        this.register(EntityType.ELDER_GUARDIAN, new ElderGuardianRenderer(this));
        this.register(EntityType.END_CRYSTAL, new EndCrystalRenderer(this));
        this.register(EntityType.ENDER_DRAGON, new EnderDragonRenderer(this));
        this.register(EntityType.ENDERMAN, new EndermanRenderer(this));
        this.register(EntityType.ENDERMITE, new EndermiteRenderer(this));
        this.register(EntityType.ENDER_PEARL, new ThrownItemRenderer<>(this, param0));
        this.register(EntityType.EVOKER_FANGS, new EvokerFangsRenderer(this));
        this.register(EntityType.EVOKER, new EvokerRenderer<>(this));
        this.register(EntityType.EXPERIENCE_BOTTLE, new ThrownItemRenderer<>(this, param0));
        this.register(EntityType.EXPERIENCE_ORB, new ExperienceOrbRenderer(this));
        this.register(EntityType.EYE_OF_ENDER, new ThrownItemRenderer<>(this, param0));
        this.register(EntityType.FALLING_BLOCK, new FallingBlockRenderer(this));
        this.register(EntityType.FIREBALL, new ThrownItemRenderer<>(this, param0, 3.0F));
        this.register(EntityType.FIREWORK_ROCKET, new FireworkEntityRenderer(this, param0));
        this.register(EntityType.FISHING_BOBBER, new FishingHookRenderer(this));
        this.register(EntityType.FOX, new FoxRenderer(this));
        this.register(EntityType.FURNACE_MINECART, new MinecartRenderer<>(this));
        this.register(EntityType.GHAST, new GhastRenderer(this));
        this.register(EntityType.GIANT, new GiantMobRenderer(this, 6.0F));
        this.register(EntityType.GUARDIAN, new GuardianRenderer(this));
        this.register(EntityType.HOPPER_MINECART, new MinecartRenderer<>(this));
        this.register(EntityType.HORSE, new HorseRenderer(this));
        this.register(EntityType.HUSK, new HuskRenderer(this));
        this.register(EntityType.ILLUSIONER, new IllusionerRenderer(this));
        this.register(EntityType.IRON_GOLEM, new IronGolemRenderer(this));
        this.register(EntityType.ITEM_FRAME, new ItemFrameRenderer(this, param0));
        this.register(EntityType.ITEM, new ItemEntityRenderer(this, param0));
        this.register(EntityType.LEASH_KNOT, new LeashKnotRenderer(this));
        this.register(EntityType.LIGHTNING_BOLT, new LightningBoltRenderer(this));
        this.register(EntityType.LLAMA, new LlamaRenderer(this));
        this.register(EntityType.LLAMA_SPIT, new LlamaSpitRenderer(this));
        this.register(EntityType.MAGMA_CUBE, new MagmaCubeRenderer(this));
        this.register(EntityType.MINECART, new MinecartRenderer<>(this));
        this.register(EntityType.MOOSHROOM, new MushroomCowRenderer(this));
        this.register(EntityType.MULE, new ChestedHorseRenderer<>(this, 0.92F));
        this.register(EntityType.OCELOT, new OcelotRenderer(this));
        this.register(EntityType.PAINTING, new PaintingRenderer(this));
        this.register(EntityType.PANDA, new PandaRenderer(this));
        this.register(EntityType.PARROT, new ParrotRenderer(this));
        this.register(EntityType.PHANTOM, new PhantomRenderer(this));
        this.register(EntityType.PIG, new PigRenderer(this));
        this.register(EntityType.PILLAGER, new PillagerRenderer(this));
        this.register(EntityType.POLAR_BEAR, new PolarBearRenderer(this));
        this.register(EntityType.POTION, new ThrownItemRenderer<>(this, param0));
        this.register(EntityType.PUFFERFISH, new PufferfishRenderer(this));
        this.register(EntityType.RABBIT, new RabbitRenderer(this));
        this.register(EntityType.RAVAGER, new RavagerRenderer(this));
        this.register(EntityType.SALMON, new SalmonRenderer(this));
        this.register(EntityType.SHEEP, new SheepRenderer(this));
        this.register(EntityType.SHULKER_BULLET, new ShulkerBulletRenderer(this));
        this.register(EntityType.SHULKER, new ShulkerRenderer(this));
        this.register(EntityType.SILVERFISH, new SilverfishRenderer(this));
        this.register(EntityType.SKELETON_HORSE, new UndeadHorseRenderer(this));
        this.register(EntityType.SKELETON, new SkeletonRenderer(this));
        this.register(EntityType.SLIME, new SlimeRenderer(this));
        this.register(EntityType.SMALL_FIREBALL, new ThrownItemRenderer<>(this, param0, 0.75F));
        this.register(EntityType.SNOWBALL, new ThrownItemRenderer<>(this, param0));
        this.register(EntityType.SNOW_GOLEM, new SnowGolemRenderer(this));
        this.register(EntityType.SPAWNER_MINECART, new MinecartRenderer<>(this));
        this.register(EntityType.SPECTRAL_ARROW, new SpectralArrowRenderer(this));
        this.register(EntityType.SPIDER, new SpiderRenderer<>(this));
        this.register(EntityType.SQUID, new SquidRenderer(this));
        this.register(EntityType.STRAY, new StrayRenderer(this));
        this.register(EntityType.TNT_MINECART, new TntMinecartRenderer(this));
        this.register(EntityType.TNT, new TntRenderer(this));
        this.register(EntityType.TRADER_LLAMA, new LlamaRenderer(this));
        this.register(EntityType.TRIDENT, new ThrownTridentRenderer(this));
        this.register(EntityType.TROPICAL_FISH, new TropicalFishRenderer(this));
        this.register(EntityType.TURTLE, new TurtleRenderer(this));
        this.register(EntityType.VEX, new VexRenderer(this));
        this.register(EntityType.VILLAGER, new VillagerRenderer(this, param1));
        this.register(EntityType.VINDICATOR, new VindicatorRenderer(this));
        this.register(EntityType.WANDERING_TRADER, new WanderingTraderRenderer(this));
        this.register(EntityType.WITCH, new WitchRenderer(this));
        this.register(EntityType.WITHER, new WitherBossRenderer(this));
        this.register(EntityType.WITHER_SKELETON, new WitherSkeletonRenderer(this));
        this.register(EntityType.WITHER_SKULL, new WitherSkullRenderer(this));
        this.register(EntityType.WOLF, new WolfRenderer(this));
        this.register(EntityType.ZOMBIE_HORSE, new UndeadHorseRenderer(this));
        this.register(EntityType.ZOMBIE, new ZombieRenderer(this));
        this.register(EntityType.ZOMBIE_PIGMAN, new PigZombieRenderer(this));
        this.register(EntityType.ZOMBIE_VILLAGER, new ZombieVillagerRenderer(this, param1));
    }

    public EntityRenderDispatcher(TextureManager param0, ItemRenderer param1, ReloadableResourceManager param2, Font param3, Options param4) {
        this.textureManager = param0;
        this.font = param3;
        this.options = param4;
        this.registerRenderers(param1, param2);
        this.defaultPlayerRenderer = new PlayerRenderer(this);
        this.playerRenderers.put("default", this.defaultPlayerRenderer);
        this.playerRenderers.put("slim", new PlayerRenderer(this, true));

        for(EntityType<?> var0 : Registry.ENTITY_TYPE) {
            if (var0 != EntityType.PLAYER && !this.renderers.containsKey(var0)) {
                throw new IllegalStateException("No renderer registered for " + Registry.ENTITY_TYPE.getKey(var0));
            }
        }

    }

    public <T extends Entity> EntityRenderer<? super T> getRenderer(T param0) {
        if (param0 instanceof AbstractClientPlayer) {
            String var0 = ((AbstractClientPlayer)param0).getModelName();
            PlayerRenderer var1 = this.playerRenderers.get(var0);
            return var1 != null ? var1 : this.defaultPlayerRenderer;
        } else {
            return (EntityRenderer<? super T>)this.renderers.get(param0.getType());
        }
    }

    public void prepare(Level param0, Camera param1, Entity param2) {
        this.level = param0;
        this.camera = param1;
        this.crosshairPickEntity = param2;
        if (param1.getEntity() instanceof LivingEntity && ((LivingEntity)param1.getEntity()).isSleeping()) {
            Direction var0 = ((LivingEntity)param1.getEntity()).getBedOrientation();
            if (var0 != null) {
                this.playerRotY = var0.getOpposite().toYRot();
                this.playerRotX = 0.0F;
            }
        } else {
            this.playerRotY = param1.getYRot();
            this.playerRotX = param1.getXRot();
        }

    }

    public void setPlayerRotY(float param0) {
        this.playerRotY = param0;
    }

    public void setRenderShadow(boolean param0) {
        this.shouldRenderShadow = param0;
    }

    public void setRenderHitBoxes(boolean param0) {
        this.renderHitBoxes = param0;
    }

    public boolean shouldRenderHitBoxes() {
        return this.renderHitBoxes;
    }

    public <E extends Entity> boolean shouldRender(E param0, Frustum param1, double param2, double param3, double param4) {
        EntityRenderer<? super E> var0 = this.getRenderer(param0);
        return var0.shouldRender(param0, param1, param2, param3, param4);
    }

    public void render(Entity param0, float param1) {
        MultiBufferSource.BufferSource var0 = Minecraft.getInstance().renderBuffers().bufferSource();
        this.render(param0, 0.0, 0.0, 0.0, 0.0F, param1, new PoseStack(), var0);
        var0.endBatch();
    }

    public <E extends Entity> void render(
        E param0, double param1, double param2, double param3, float param4, float param5, PoseStack param6, MultiBufferSource param7
    ) {
        EntityRenderer<? super E> var0 = this.getRenderer(param0);

        try {
            Vec3 var1 = var0.getRenderOffset(param0, param1, param2, param3, param5);
            double var2 = param1 + var1.x();
            double var3 = param2 + var1.y();
            double var4 = param3 + var1.z();
            param6.pushPose();
            param6.translate(var2, var3, var4);
            var0.render(param0, var2, var3, var4, param4, param5, param6, param7);
            if (param0.displayFireAnimation()) {
                this.renderFlame(param6, param7, param0);
            }

            param6.translate(-var1.x(), -var1.y(), -var1.z());
            if (this.options.entityShadows && this.shouldRenderShadow && var0.shadowRadius > 0.0F && !param0.isInvisible()) {
                double var5 = this.distanceToSqr(param0.getX(), param0.getY(), param0.getZ());
                float var6 = (float)((1.0 - var5 / 256.0) * (double)var0.shadowStrength);
                if (var6 > 0.0F) {
                    renderShadow(param6, param7, param0, var6, param5, this.level, var0.shadowRadius);
                }
            }

            if (this.renderHitBoxes && !param0.isInvisible() && !Minecraft.getInstance().showOnlyReducedInfo()) {
                this.renderHitbox(param6, param7.getBuffer(RenderType.lines()), param0, param5);
            }

            param6.popPose();
        } catch (Throwable var23) {
            CrashReport var8 = CrashReport.forThrowable(var23, "Rendering entity in world");
            CrashReportCategory var9 = var8.addCategory("Entity being rendered");
            param0.fillCrashReportCategory(var9);
            CrashReportCategory var10 = var8.addCategory("Renderer details");
            var10.setDetail("Assigned renderer", var0);
            var10.setDetail("Location", CrashReportCategory.formatLocation(param1, param2, param3));
            var10.setDetail("Rotation", param4);
            var10.setDetail("Delta", param5);
            throw new ReportedException(var8);
        }
    }

    private void renderHitbox(PoseStack param0, VertexConsumer param1, Entity param2, float param3) {
        float var0 = param2.getBbWidth() / 2.0F;
        this.renderBox(param0, param1, param2, 1.0F, 1.0F, 1.0F);
        if (param2 instanceof EnderDragon) {
            double var1 = param2.getX() - Mth.lerp((double)param3, param2.xOld, param2.getX());
            double var2 = param2.getY() - Mth.lerp((double)param3, param2.yOld, param2.getY());
            double var3 = param2.getZ() - Mth.lerp((double)param3, param2.zOld, param2.getZ());

            for(EnderDragonPart var4 : ((EnderDragon)param2).getSubEntities()) {
                param0.pushPose();
                double var5 = var1 + Mth.lerp((double)param3, var4.xOld, var4.getX());
                double var6 = var2 + Mth.lerp((double)param3, var4.yOld, var4.getY());
                double var7 = var3 + Mth.lerp((double)param3, var4.zOld, var4.getZ());
                param0.translate(var5, var6, var7);
                this.renderBox(param0, param1, var4, 0.25F, 1.0F, 0.0F);
                param0.popPose();
            }
        }

        if (param2 instanceof LivingEntity) {
            float var8 = 0.01F;
            LevelRenderer.renderLineBox(
                param0,
                param1,
                (double)(-var0),
                (double)(param2.getEyeHeight() - 0.01F),
                (double)(-var0),
                (double)var0,
                (double)(param2.getEyeHeight() + 0.01F),
                (double)var0,
                1.0F,
                0.0F,
                0.0F,
                1.0F
            );
        }

        Vec3 var9 = param2.getViewVector(param3);
        Matrix4f var10 = param0.getPose();
        param1.vertex(var10, 0.0F, param2.getEyeHeight(), 0.0F).color(0, 0, 255, 255).endVertex();
        param1.vertex(var10, (float)(var9.x * 2.0), (float)((double)param2.getEyeHeight() + var9.y * 2.0), (float)(var9.z * 2.0))
            .color(0, 0, 255, 255)
            .endVertex();
    }

    private void renderBox(PoseStack param0, VertexConsumer param1, Entity param2, float param3, float param4, float param5) {
        AABB var0 = param2.getBoundingBox().move(-param2.getX(), -param2.getY(), -param2.getZ());
        LevelRenderer.renderLineBox(param0, param1, var0, param3, param4, param5, 1.0F);
    }

    private void renderFlame(PoseStack param0, MultiBufferSource param1, Entity param2) {
        TextureAtlas var0 = Minecraft.getInstance().getTextureAtlas();
        TextureAtlasSprite var1 = var0.getSprite(ModelBakery.FIRE_0);
        TextureAtlasSprite var2 = var0.getSprite(ModelBakery.FIRE_1);
        param0.pushPose();
        float var3 = param2.getBbWidth() * 1.4F;
        param0.scale(var3, var3, var3);
        float var4 = 0.5F;
        float var5 = 0.0F;
        float var6 = param2.getBbHeight() / var3;
        float var7 = 0.0F;
        param0.mulPose(Vector3f.YP.rotationDegrees(-this.playerRotY));
        param0.translate(0.0, 0.0, (double)(-0.3F + (float)((int)var6) * 0.02F));
        float var8 = 0.0F;
        int var9 = 0;
        VertexConsumer var10 = param1.getBuffer(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS));

        for(Matrix4f var11 = param0.getPose(); var6 > 0.0F; ++var9) {
            TextureAtlasSprite var12 = var9 % 2 == 0 ? var1 : var2;
            float var13 = var12.getU0();
            float var14 = var12.getV0();
            float var15 = var12.getU1();
            float var16 = var12.getV1();
            if (var9 / 2 % 2 == 0) {
                float var17 = var15;
                var15 = var13;
                var13 = var17;
            }

            fireVertex(var11, var10, var4 - 0.0F, 0.0F - var7, var8, var15, var16);
            fireVertex(var11, var10, -var4 - 0.0F, 0.0F - var7, var8, var13, var16);
            fireVertex(var11, var10, -var4 - 0.0F, 1.4F - var7, var8, var13, var14);
            fireVertex(var11, var10, var4 - 0.0F, 1.4F - var7, var8, var15, var14);
            var6 -= 0.45F;
            var7 -= 0.45F;
            var4 *= 0.9F;
            var8 += 0.03F;
        }

        param0.popPose();
    }

    private static void fireVertex(Matrix4f param0, VertexConsumer param1, float param2, float param3, float param4, float param5, float param6) {
        param1.vertex(param0, param2, param3, param4)
            .color(255, 255, 255, 255)
            .uv(param5, param6)
            .overlayCoords(0, 10)
            .uv2(240)
            .normal(0.0F, 1.0F, 0.0F)
            .endVertex();
    }

    private static void renderShadow(PoseStack param0, MultiBufferSource param1, Entity param2, float param3, float param4, LevelReader param5, float param6) {
        float var0 = param6;
        if (param2 instanceof Mob) {
            Mob var1 = (Mob)param2;
            if (var1.isBaby()) {
                var0 = param6 * 0.5F;
            }
        }

        double var2 = Mth.lerp((double)param4, param2.xOld, param2.getX());
        double var3 = Mth.lerp((double)param4, param2.yOld, param2.getY());
        double var4 = Mth.lerp((double)param4, param2.zOld, param2.getZ());
        int var5 = Mth.floor(var2 - (double)var0);
        int var6 = Mth.floor(var2 + (double)var0);
        int var7 = Mth.floor(var3 - (double)var0);
        int var8 = Mth.floor(var3);
        int var9 = Mth.floor(var4 - (double)var0);
        int var10 = Mth.floor(var4 + (double)var0);
        Matrix4f var11 = param0.getPose();
        VertexConsumer var12 = param1.getBuffer(RenderType.entityNoOutline(SHADOW_LOCATION));

        for(BlockPos var13 : BlockPos.betweenClosed(new BlockPos(var5, var7, var9), new BlockPos(var6, var8, var10))) {
            renderBlockShadow(var11, var12, param5, var13, var2, var3, var4, var0, param3);
        }

    }

    private static void renderBlockShadow(
        Matrix4f param0, VertexConsumer param1, LevelReader param2, BlockPos param3, double param4, double param5, double param6, float param7, float param8
    ) {
        BlockPos var0 = param3.below();
        BlockState var1 = param2.getBlockState(var0);
        if (var1.getRenderShape() != RenderShape.INVISIBLE && param2.getMaxLocalRawBrightness(param3) > 3) {
            if (var1.isCollisionShapeFullBlock(param2, var0)) {
                VoxelShape var2 = var1.getShape(param2, param3.below());
                if (!var2.isEmpty()) {
                    float var3 = (float)(((double)param8 - (param5 - (double)param3.getY()) / 2.0) * 0.5 * (double)param2.getBrightness(param3));
                    if (var3 >= 0.0F) {
                        if (var3 > 1.0F) {
                            var3 = 1.0F;
                        }

                        AABB var4 = var2.bounds();
                        double var5 = (double)param3.getX() + var4.minX;
                        double var6 = (double)param3.getX() + var4.maxX;
                        double var7 = (double)param3.getY() + var4.minY;
                        double var8 = (double)param3.getZ() + var4.minZ;
                        double var9 = (double)param3.getZ() + var4.maxZ;
                        float var10 = (float)(var5 - param4);
                        float var11 = (float)(var6 - param4);
                        float var12 = (float)(var7 - param5 + 0.015625);
                        float var13 = (float)(var8 - param6);
                        float var14 = (float)(var9 - param6);
                        float var15 = -var10 / 2.0F / param7 + 0.5F;
                        float var16 = -var11 / 2.0F / param7 + 0.5F;
                        float var17 = -var13 / 2.0F / param7 + 0.5F;
                        float var18 = -var14 / 2.0F / param7 + 0.5F;
                        shadowVertex(param0, param1, var3, var10, var12, var13, var15, var17);
                        shadowVertex(param0, param1, var3, var10, var12, var14, var15, var18);
                        shadowVertex(param0, param1, var3, var11, var12, var14, var16, var18);
                        shadowVertex(param0, param1, var3, var11, var12, var13, var16, var17);
                    }

                }
            }
        }
    }

    private static void shadowVertex(Matrix4f param0, VertexConsumer param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        param1.vertex(param0, param3, param4, param5)
            .color(1.0F, 1.0F, 1.0F, param2)
            .uv(param6, param7)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(15728880)
            .normal(0.0F, 1.0F, 0.0F)
            .endVertex();
    }

    public void setLevel(@Nullable Level param0) {
        this.level = param0;
        if (param0 == null) {
            this.camera = null;
        }

    }

    public double distanceToSqr(Entity param0) {
        return this.camera.getPosition().distanceToSqr(param0.position());
    }

    public double distanceToSqr(double param0, double param1, double param2) {
        return this.camera.getPosition().distanceToSqr(param0, param1, param2);
    }

    public Font getFont() {
        return this.font;
    }
}
