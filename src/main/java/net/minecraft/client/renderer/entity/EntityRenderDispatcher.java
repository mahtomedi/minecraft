package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
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
import net.minecraft.client.renderer.culling.Culler;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.Direction;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cod;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Salmon;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.animal.horse.TraderLlama;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.fishing.FishingHook;
import net.minecraft.world.entity.global.LightningBolt;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.PigZombie;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.MinecartSpawner;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EntityRenderDispatcher {
    private final Map<Class<? extends Entity>, EntityRenderer<? extends Entity>> renderers = Maps.newHashMap();
    private final Map<String, PlayerRenderer> playerRenderers = Maps.newHashMap();
    private final PlayerRenderer defaultPlayerRenderer;
    private Font font;
    private double xOff;
    private double yOff;
    private double zOff;
    public final TextureManager textureManager;
    public Level level;
    public Camera camera;
    public Entity crosshairPickEntity;
    public float playerRotY;
    public float playerRotX;
    public Options options;
    private boolean solidRender;
    private boolean shouldRenderShadow = true;
    private boolean renderHitBoxes;

    private <T extends Entity> void register(Class<T> param0, EntityRenderer<? super T> param1) {
        this.renderers.put(param0, param1);
    }

    public EntityRenderDispatcher(TextureManager param0, ItemRenderer param1, ReloadableResourceManager param2) {
        this.textureManager = param0;
        this.register(CaveSpider.class, new CaveSpiderRenderer(this));
        this.register(Spider.class, new SpiderRenderer<>(this));
        this.register(Pig.class, new PigRenderer(this));
        this.register(Sheep.class, new SheepRenderer(this));
        this.register(Cow.class, new CowRenderer(this));
        this.register(MushroomCow.class, new MushroomCowRenderer(this));
        this.register(Wolf.class, new WolfRenderer(this));
        this.register(Chicken.class, new ChickenRenderer(this));
        this.register(Ocelot.class, new OcelotRenderer(this));
        this.register(Rabbit.class, new RabbitRenderer(this));
        this.register(Parrot.class, new ParrotRenderer(this));
        this.register(Turtle.class, new TurtleRenderer(this));
        this.register(Silverfish.class, new SilverfishRenderer(this));
        this.register(Endermite.class, new EndermiteRenderer(this));
        this.register(Creeper.class, new CreeperRenderer(this));
        this.register(EnderMan.class, new EndermanRenderer(this));
        this.register(SnowGolem.class, new SnowGolemRenderer(this));
        this.register(Skeleton.class, new SkeletonRenderer(this));
        this.register(WitherSkeleton.class, new WitherSkeletonRenderer(this));
        this.register(Stray.class, new StrayRenderer(this));
        this.register(Witch.class, new WitchRenderer(this));
        this.register(Blaze.class, new BlazeRenderer(this));
        this.register(PigZombie.class, new PigZombieRenderer(this));
        this.register(Zombie.class, new ZombieRenderer(this));
        this.register(ZombieVillager.class, new ZombieVillagerRenderer(this, param2));
        this.register(Husk.class, new HuskRenderer(this));
        this.register(Drowned.class, new DrownedRenderer(this));
        this.register(Slime.class, new SlimeRenderer(this));
        this.register(MagmaCube.class, new LavaSlimeRenderer(this));
        this.register(Giant.class, new GiantMobRenderer(this, 6.0F));
        this.register(Ghast.class, new GhastRenderer(this));
        this.register(Squid.class, new SquidRenderer(this));
        this.register(Villager.class, new VillagerRenderer(this, param2));
        this.register(WanderingTrader.class, new WanderingTraderRenderer(this));
        this.register(IronGolem.class, new IronGolemRenderer(this));
        this.register(Bat.class, new BatRenderer(this));
        this.register(Guardian.class, new GuardianRenderer(this));
        this.register(ElderGuardian.class, new ElderGuardianRenderer(this));
        this.register(Shulker.class, new ShulkerRenderer(this));
        this.register(PolarBear.class, new PolarBearRenderer(this));
        this.register(Evoker.class, new EvokerRenderer<>(this));
        this.register(Vindicator.class, new VindicatorRenderer(this));
        this.register(Pillager.class, new PillagerRenderer(this));
        this.register(Ravager.class, new RavagerRenderer(this));
        this.register(Vex.class, new VexRenderer(this));
        this.register(Illusioner.class, new IllusionerRenderer(this));
        this.register(Phantom.class, new PhantomRenderer(this));
        this.register(Pufferfish.class, new PufferfishRenderer(this));
        this.register(Salmon.class, new SalmonRenderer(this));
        this.register(Cod.class, new CodRenderer(this));
        this.register(TropicalFish.class, new TropicalFishRenderer(this));
        this.register(Dolphin.class, new DolphinRenderer(this));
        this.register(Panda.class, new PandaRenderer(this));
        this.register(Cat.class, new CatRenderer(this));
        this.register(Fox.class, new FoxRenderer(this));
        this.register(EnderDragon.class, new EnderDragonRenderer(this));
        this.register(EndCrystal.class, new EndCrystalRenderer(this));
        this.register(WitherBoss.class, new WitherBossRenderer(this));
        this.register(Entity.class, new DefaultRenderer(this));
        this.register(Painting.class, new PaintingRenderer(this));
        this.register(ItemFrame.class, new ItemFrameRenderer(this, param1));
        this.register(LeashFenceKnotEntity.class, new LeashKnotRenderer(this));
        this.register(Arrow.class, new TippableArrowRenderer(this));
        this.register(SpectralArrow.class, new SpectralArrowRenderer(this));
        this.register(ThrownTrident.class, new ThrownTridentRenderer(this));
        this.register(Snowball.class, new ThrownItemRenderer<>(this, param1));
        this.register(ThrownEnderpearl.class, new ThrownItemRenderer<>(this, param1));
        this.register(EyeOfEnder.class, new ThrownItemRenderer<>(this, param1));
        this.register(ThrownEgg.class, new ThrownItemRenderer<>(this, param1));
        this.register(ThrownPotion.class, new ThrownItemRenderer<>(this, param1));
        this.register(ThrownExperienceBottle.class, new ThrownItemRenderer<>(this, param1));
        this.register(FireworkRocketEntity.class, new FireworkEntityRenderer(this, param1));
        this.register(LargeFireball.class, new ThrownItemRenderer<>(this, param1, 3.0F));
        this.register(SmallFireball.class, new ThrownItemRenderer<>(this, param1, 0.75F));
        this.register(DragonFireball.class, new DragonFireballRenderer(this));
        this.register(WitherSkull.class, new WitherSkullRenderer(this));
        this.register(ShulkerBullet.class, new ShulkerBulletRenderer(this));
        this.register(ItemEntity.class, new ItemEntityRenderer(this, param1));
        this.register(ExperienceOrb.class, new ExperienceOrbRenderer(this));
        this.register(PrimedTnt.class, new TntRenderer(this));
        this.register(FallingBlockEntity.class, new FallingBlockRenderer(this));
        this.register(ArmorStand.class, new ArmorStandRenderer(this));
        this.register(EvokerFangs.class, new EvokerFangsRenderer(this));
        this.register(MinecartTNT.class, new TntMinecartRenderer(this));
        this.register(MinecartSpawner.class, new MinecartRenderer<>(this));
        this.register(AbstractMinecart.class, new MinecartRenderer<>(this));
        this.register(Boat.class, new BoatRenderer(this));
        this.register(FishingHook.class, new FishingHookRenderer(this));
        this.register(AreaEffectCloud.class, new AreaEffectCloudRenderer(this));
        this.register(Horse.class, new HorseRenderer(this));
        this.register(SkeletonHorse.class, new UndeadHorseRenderer(this));
        this.register(ZombieHorse.class, new UndeadHorseRenderer(this));
        this.register(Mule.class, new ChestedHorseRenderer<>(this, 0.92F));
        this.register(Donkey.class, new ChestedHorseRenderer<>(this, 0.87F));
        this.register(Llama.class, new LlamaRenderer(this));
        this.register(TraderLlama.class, new LlamaRenderer(this));
        this.register(LlamaSpit.class, new LlamaSpitRenderer(this));
        this.register(LightningBolt.class, new LightningBoltRenderer(this));
        this.defaultPlayerRenderer = new PlayerRenderer(this);
        this.playerRenderers.put("default", this.defaultPlayerRenderer);
        this.playerRenderers.put("slim", new PlayerRenderer(this, true));
    }

    public void setPosition(double param0, double param1, double param2) {
        this.xOff = param0;
        this.yOff = param1;
        this.zOff = param2;
    }

    public <T extends Entity, U extends EntityRenderer<T>> U getRenderer(Class<? extends Entity> param0) {
        EntityRenderer<? extends Entity> var0 = this.renderers.get(param0);
        if (var0 == null && param0 != Entity.class) {
            var0 = this.getRenderer((Class<? extends Entity>)param0.getSuperclass());
            this.renderers.put(param0, var0);
        }

        return (U)var0;
    }

    @Nullable
    public <T extends Entity, U extends EntityRenderer<T>> U getRenderer(T param0) {
        if (param0 instanceof AbstractClientPlayer) {
            String var0 = ((AbstractClientPlayer)param0).getModelName();
            PlayerRenderer var1 = this.playerRenderers.get(var0);
            return (U)(var1 != null ? var1 : this.defaultPlayerRenderer);
        } else {
            return this.getRenderer(param0.getClass());
        }
    }

    public void prepare(Level param0, Font param1, Camera param2, Entity param3, Options param4) {
        this.level = param0;
        this.options = param4;
        this.camera = param2;
        this.crosshairPickEntity = param3;
        this.font = param1;
        if (param2.getEntity() instanceof LivingEntity && ((LivingEntity)param2.getEntity()).isSleeping()) {
            Direction var0 = ((LivingEntity)param2.getEntity()).getBedOrientation();
            if (var0 != null) {
                this.playerRotY = var0.getOpposite().toYRot();
                this.playerRotX = 0.0F;
            }
        } else {
            this.playerRotY = param2.getYRot();
            this.playerRotX = param2.getXRot();
        }

    }

    public void setPlayerRotY(float param0) {
        this.playerRotY = param0;
    }

    public boolean shouldRenderShadow() {
        return this.shouldRenderShadow;
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

    public boolean hasSecondPass(Entity param0) {
        return this.getRenderer(param0).hasSecondPass();
    }

    public boolean shouldRender(Entity param0, Culler param1, double param2, double param3, double param4) {
        EntityRenderer<Entity> var0 = this.getRenderer(param0);
        return var0 != null && var0.shouldRender(param0, param1, param2, param3, param4);
    }

    public void render(Entity param0, float param1, boolean param2) {
        if (param0.tickCount == 0) {
            param0.xOld = param0.x;
            param0.yOld = param0.y;
            param0.zOld = param0.z;
        }

        double var0 = Mth.lerp((double)param1, param0.xOld, param0.x);
        double var1 = Mth.lerp((double)param1, param0.yOld, param0.y);
        double var2 = Mth.lerp((double)param1, param0.zOld, param0.z);
        float var3 = Mth.lerp(param1, param0.yRotO, param0.yRot);
        int var4 = param0.getLightColor();
        if (param0.isOnFire()) {
            var4 = 15728880;
        }

        int var5 = var4 % 65536;
        int var6 = var4 / 65536;
        GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, (float)var5, (float)var6);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.render(param0, var0 - this.xOff, var1 - this.yOff, var2 - this.zOff, var3, param1, param2);
    }

    public void render(Entity param0, double param1, double param2, double param3, float param4, float param5, boolean param6) {
        EntityRenderer<Entity> var0 = null;

        try {
            var0 = this.getRenderer(param0);
            if (var0 != null && this.textureManager != null) {
                try {
                    var0.setSolidRender(this.solidRender);
                    var0.render(param0, param1, param2, param3, param4, param5);
                } catch (Throwable var17) {
                    throw new ReportedException(CrashReport.forThrowable(var17, "Rendering entity in world"));
                }

                try {
                    if (!this.solidRender) {
                        var0.postRender(param0, param1, param2, param3, param4, param5);
                    }
                } catch (Throwable var18) {
                    throw new ReportedException(CrashReport.forThrowable(var18, "Post-rendering entity in world"));
                }

                if (this.renderHitBoxes && !param0.isInvisible() && !param6 && !Minecraft.getInstance().showOnlyReducedInfo()) {
                    try {
                        this.renderHitbox(param0, param1, param2, param3, param4, param5);
                    } catch (Throwable var16) {
                        throw new ReportedException(CrashReport.forThrowable(var16, "Rendering entity hitbox in world"));
                    }
                }
            }

        } catch (Throwable var19) {
            CrashReport var5 = CrashReport.forThrowable(var19, "Rendering entity in world");
            CrashReportCategory var6 = var5.addCategory("Entity being rendered");
            param0.fillCrashReportCategory(var6);
            CrashReportCategory var7 = var5.addCategory("Renderer details");
            var7.setDetail("Assigned renderer", var0);
            var7.setDetail("Location", CrashReportCategory.formatLocation(param1, param2, param3));
            var7.setDetail("Rotation", param4);
            var7.setDetail("Delta", param5);
            throw new ReportedException(var5);
        }
    }

    public void renderSecondPass(Entity param0, float param1) {
        if (param0.tickCount == 0) {
            param0.xOld = param0.x;
            param0.yOld = param0.y;
            param0.zOld = param0.z;
        }

        double var0 = Mth.lerp((double)param1, param0.xOld, param0.x);
        double var1 = Mth.lerp((double)param1, param0.yOld, param0.y);
        double var2 = Mth.lerp((double)param1, param0.zOld, param0.z);
        float var3 = Mth.lerp(param1, param0.yRotO, param0.yRot);
        int var4 = param0.getLightColor();
        if (param0.isOnFire()) {
            var4 = 15728880;
        }

        int var5 = var4 % 65536;
        int var6 = var4 / 65536;
        GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, (float)var5, (float)var6);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        EntityRenderer<Entity> var7 = this.getRenderer(param0);
        if (var7 != null && this.textureManager != null) {
            var7.renderSecondPass(param0, var0 - this.xOff, var1 - this.yOff, var2 - this.zOff, var3, param1);
        }

    }

    private void renderHitbox(Entity param0, double param1, double param2, double param3, float param4, float param5) {
        GlStateManager.depthMask(false);
        GlStateManager.disableTexture();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.disableBlend();
        float var0 = param0.getBbWidth() / 2.0F;
        AABB var1 = param0.getBoundingBox();
        LevelRenderer.renderLineBox(
            var1.minX - param0.x + param1,
            var1.minY - param0.y + param2,
            var1.minZ - param0.z + param3,
            var1.maxX - param0.x + param1,
            var1.maxY - param0.y + param2,
            var1.maxZ - param0.z + param3,
            1.0F,
            1.0F,
            1.0F,
            1.0F
        );
        if (param0 instanceof EnderDragon) {
            for(EnderDragonPart var2 : ((EnderDragon)param0).getSubEntities()) {
                double var3 = (var2.x - var2.xo) * (double)param5;
                double var4 = (var2.y - var2.yo) * (double)param5;
                double var5 = (var2.z - var2.zo) * (double)param5;
                AABB var6 = var2.getBoundingBox();
                LevelRenderer.renderLineBox(
                    var6.minX - this.xOff + var3,
                    var6.minY - this.yOff + var4,
                    var6.minZ - this.zOff + var5,
                    var6.maxX - this.xOff + var3,
                    var6.maxY - this.yOff + var4,
                    var6.maxZ - this.zOff + var5,
                    0.25F,
                    1.0F,
                    0.0F,
                    1.0F
                );
            }
        }

        if (param0 instanceof LivingEntity) {
            float var7 = 0.01F;
            LevelRenderer.renderLineBox(
                param1 - (double)var0,
                param2 + (double)param0.getEyeHeight() - 0.01F,
                param3 - (double)var0,
                param1 + (double)var0,
                param2 + (double)param0.getEyeHeight() + 0.01F,
                param3 + (double)var0,
                1.0F,
                0.0F,
                0.0F,
                1.0F
            );
        }

        Tesselator var8 = Tesselator.getInstance();
        BufferBuilder var9 = var8.getBuilder();
        Vec3 var10 = param0.getViewVector(param5);
        var9.begin(3, DefaultVertexFormat.POSITION_COLOR);
        var9.vertex(param1, param2 + (double)param0.getEyeHeight(), param3).color(0, 0, 255, 255).endVertex();
        var9.vertex(param1 + var10.x * 2.0, param2 + (double)param0.getEyeHeight() + var10.y * 2.0, param3 + var10.z * 2.0).color(0, 0, 255, 255).endVertex();
        var8.end();
        GlStateManager.enableTexture();
        GlStateManager.enableLighting();
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
    }

    public void setLevel(@Nullable Level param0) {
        this.level = param0;
        if (param0 == null) {
            this.camera = null;
        }

    }

    public double distanceToSqr(double param0, double param1, double param2) {
        return this.camera.getPosition().distanceToSqr(param0, param1, param2);
    }

    public Font getFont() {
        return this.font;
    }

    public void setSolidRendering(boolean param0) {
        this.solidRender = param0;
    }
}
