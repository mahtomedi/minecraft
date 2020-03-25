package net.minecraft.world.damagesource;

import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;

public class DamageSource {
    public static final DamageSource IN_FIRE = new DamageSource("inFire").setIsFire();
    public static final DamageSource LIGHTNING_BOLT = new DamageSource("lightningBolt");
    public static final DamageSource ON_FIRE = new DamageSource("onFire").bypassArmor().setIsFire();
    public static final DamageSource LAVA = new DamageSource("lava").setIsFire();
    public static final DamageSource HOT_FLOOR = new DamageSource("hotFloor").setIsFire();
    public static final DamageSource IN_WALL = new DamageSource("inWall").bypassArmor();
    public static final DamageSource CRAMMING = new DamageSource("cramming").bypassArmor();
    public static final DamageSource DROWN = new DamageSource("drown").bypassArmor();
    public static final DamageSource STARVE = new DamageSource("starve").bypassArmor().bypassMagic();
    public static final DamageSource CACTUS = new DamageSource("cactus");
    public static final DamageSource FALL = new DamageSource("fall").bypassArmor();
    public static final DamageSource FLY_INTO_WALL = new DamageSource("flyIntoWall").bypassArmor();
    public static final DamageSource OUT_OF_WORLD = new DamageSource("outOfWorld").bypassArmor().bypassInvul();
    public static final DamageSource GENERIC = new DamageSource("generic").bypassArmor();
    public static final DamageSource MAGIC = new DamageSource("magic").bypassArmor().setMagic();
    public static final DamageSource WITHER = new DamageSource("wither").bypassArmor();
    public static final DamageSource ANVIL = new DamageSource("anvil");
    public static final DamageSource FALLING_BLOCK = new DamageSource("fallingBlock");
    public static final DamageSource DRAGON_BREATH = new DamageSource("dragonBreath").bypassArmor();
    public static final DamageSource DRY_OUT = new DamageSource("dryout");
    public static final DamageSource SWEET_BERRY_BUSH = new DamageSource("sweetBerryBush");
    private boolean bypassArmor;
    private boolean bypassInvul;
    private boolean bypassMagic;
    private float exhaustion = 0.1F;
    private boolean isFireSource;
    private boolean isProjectile;
    private boolean scalesWithDifficulty;
    private boolean isMagic;
    private boolean isExplosion;
    public final String msgId;

    public static DamageSource sting(LivingEntity param0) {
        return new EntityDamageSource("sting", param0);
    }

    public static DamageSource mobAttack(LivingEntity param0) {
        return new EntityDamageSource("mob", param0);
    }

    public static DamageSource indirectMobAttack(Entity param0, LivingEntity param1) {
        return new IndirectEntityDamageSource("mob", param0, param1);
    }

    public static DamageSource playerAttack(Player param0) {
        return new EntityDamageSource("player", param0);
    }

    public static DamageSource arrow(AbstractArrow param0, @Nullable Entity param1) {
        return new IndirectEntityDamageSource("arrow", param0, param1).setProjectile();
    }

    public static DamageSource trident(Entity param0, @Nullable Entity param1) {
        return new IndirectEntityDamageSource("trident", param0, param1).setProjectile();
    }

    public static DamageSource fireworks(FireworkRocketEntity param0, @Nullable Entity param1) {
        return new IndirectEntityDamageSource("fireworks", param0, param1).setExplosion();
    }

    public static DamageSource fireball(AbstractHurtingProjectile param0, @Nullable Entity param1) {
        return param1 == null
            ? new IndirectEntityDamageSource("onFire", param0, param0).setIsFire().setProjectile()
            : new IndirectEntityDamageSource("fireball", param0, param1).setIsFire().setProjectile();
    }

    public static DamageSource thrown(Entity param0, @Nullable Entity param1) {
        return new IndirectEntityDamageSource("thrown", param0, param1).setProjectile();
    }

    public static DamageSource indirectMagic(Entity param0, @Nullable Entity param1) {
        return new IndirectEntityDamageSource("indirectMagic", param0, param1).bypassArmor().setMagic();
    }

    public static DamageSource thorns(Entity param0) {
        return new EntityDamageSource("thorns", param0).setThorns().setMagic();
    }

    public static DamageSource explosion(@Nullable Explosion param0) {
        return param0 != null && param0.getSourceMob() != null
            ? new EntityDamageSource("explosion.player", param0.getSourceMob()).setScalesWithDifficulty().setExplosion()
            : new DamageSource("explosion").setScalesWithDifficulty().setExplosion();
    }

    public static DamageSource explosion(@Nullable LivingEntity param0) {
        return param0 != null
            ? new EntityDamageSource("explosion.player", param0).setScalesWithDifficulty().setExplosion()
            : new DamageSource("explosion").setScalesWithDifficulty().setExplosion();
    }

    public static DamageSource badRespawnPointExplosion() {
        return new BadRespawnPointDamage();
    }

    @Override
    public String toString() {
        return "DamageSource (" + this.msgId + ")";
    }

    public boolean isProjectile() {
        return this.isProjectile;
    }

    public DamageSource setProjectile() {
        this.isProjectile = true;
        return this;
    }

    public boolean isExplosion() {
        return this.isExplosion;
    }

    public DamageSource setExplosion() {
        this.isExplosion = true;
        return this;
    }

    public boolean isBypassArmor() {
        return this.bypassArmor;
    }

    public float getFoodExhaustion() {
        return this.exhaustion;
    }

    public boolean isBypassInvul() {
        return this.bypassInvul;
    }

    public boolean isBypassMagic() {
        return this.bypassMagic;
    }

    protected DamageSource(String param0) {
        this.msgId = param0;
    }

    @Nullable
    public Entity getDirectEntity() {
        return this.getEntity();
    }

    @Nullable
    public Entity getEntity() {
        return null;
    }

    protected DamageSource bypassArmor() {
        this.bypassArmor = true;
        this.exhaustion = 0.0F;
        return this;
    }

    protected DamageSource bypassInvul() {
        this.bypassInvul = true;
        return this;
    }

    protected DamageSource bypassMagic() {
        this.bypassMagic = true;
        this.exhaustion = 0.0F;
        return this;
    }

    protected DamageSource setIsFire() {
        this.isFireSource = true;
        return this;
    }

    public Component getLocalizedDeathMessage(LivingEntity param0) {
        LivingEntity var0 = param0.getKillCredit();
        String var1 = "death.attack." + this.msgId;
        String var2 = var1 + ".player";
        return var0 != null
            ? new TranslatableComponent(var2, param0.getDisplayName(), var0.getDisplayName())
            : new TranslatableComponent(var1, param0.getDisplayName());
    }

    public boolean isFire() {
        return this.isFireSource;
    }

    public String getMsgId() {
        return this.msgId;
    }

    public DamageSource setScalesWithDifficulty() {
        this.scalesWithDifficulty = true;
        return this;
    }

    public boolean scalesWithDifficulty() {
        return this.scalesWithDifficulty;
    }

    public boolean isMagic() {
        return this.isMagic;
    }

    public DamageSource setMagic() {
        this.isMagic = true;
        return this;
    }

    public boolean isCreativePlayer() {
        Entity var0 = this.getEntity();
        return var0 instanceof Player && ((Player)var0).abilities.instabuild;
    }

    @Nullable
    public Vec3 getSourcePosition() {
        return null;
    }
}
