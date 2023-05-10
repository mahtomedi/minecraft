package net.minecraft.world.damagesource;

import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;

public class DamageSources {
    private final Registry<DamageType> damageTypes;
    private final DamageSource inFire;
    private final DamageSource lightningBolt;
    private final DamageSource onFire;
    private final DamageSource lava;
    private final DamageSource hotFloor;
    private final DamageSource inWall;
    private final DamageSource cramming;
    private final DamageSource drown;
    private final DamageSource starve;
    private final DamageSource cactus;
    private final DamageSource fall;
    private final DamageSource flyIntoWall;
    private final DamageSource fellOutOfWorld;
    private final DamageSource generic;
    private final DamageSource magic;
    private final DamageSource wither;
    private final DamageSource dragonBreath;
    private final DamageSource dryOut;
    private final DamageSource sweetBerryBush;
    private final DamageSource freeze;
    private final DamageSource stalagmite;
    private final DamageSource outsideBorder;
    private final DamageSource genericKill;

    public DamageSources(RegistryAccess param0) {
        this.damageTypes = param0.registryOrThrow(Registries.DAMAGE_TYPE);
        this.inFire = this.source(DamageTypes.IN_FIRE);
        this.lightningBolt = this.source(DamageTypes.LIGHTNING_BOLT);
        this.onFire = this.source(DamageTypes.ON_FIRE);
        this.lava = this.source(DamageTypes.LAVA);
        this.hotFloor = this.source(DamageTypes.HOT_FLOOR);
        this.inWall = this.source(DamageTypes.IN_WALL);
        this.cramming = this.source(DamageTypes.CRAMMING);
        this.drown = this.source(DamageTypes.DROWN);
        this.starve = this.source(DamageTypes.STARVE);
        this.cactus = this.source(DamageTypes.CACTUS);
        this.fall = this.source(DamageTypes.FALL);
        this.flyIntoWall = this.source(DamageTypes.FLY_INTO_WALL);
        this.fellOutOfWorld = this.source(DamageTypes.FELL_OUT_OF_WORLD);
        this.generic = this.source(DamageTypes.GENERIC);
        this.magic = this.source(DamageTypes.MAGIC);
        this.wither = this.source(DamageTypes.WITHER);
        this.dragonBreath = this.source(DamageTypes.DRAGON_BREATH);
        this.dryOut = this.source(DamageTypes.DRY_OUT);
        this.sweetBerryBush = this.source(DamageTypes.SWEET_BERRY_BUSH);
        this.freeze = this.source(DamageTypes.FREEZE);
        this.stalagmite = this.source(DamageTypes.STALAGMITE);
        this.outsideBorder = this.source(DamageTypes.OUTSIDE_BORDER);
        this.genericKill = this.source(DamageTypes.GENERIC_KILL);
    }

    private DamageSource source(ResourceKey<DamageType> param0) {
        return new DamageSource(this.damageTypes.getHolderOrThrow(param0));
    }

    private DamageSource source(ResourceKey<DamageType> param0, @Nullable Entity param1) {
        return new DamageSource(this.damageTypes.getHolderOrThrow(param0), param1);
    }

    private DamageSource source(ResourceKey<DamageType> param0, @Nullable Entity param1, @Nullable Entity param2) {
        return new DamageSource(this.damageTypes.getHolderOrThrow(param0), param1, param2);
    }

    public DamageSource inFire() {
        return this.inFire;
    }

    public DamageSource lightningBolt() {
        return this.lightningBolt;
    }

    public DamageSource onFire() {
        return this.onFire;
    }

    public DamageSource lava() {
        return this.lava;
    }

    public DamageSource hotFloor() {
        return this.hotFloor;
    }

    public DamageSource inWall() {
        return this.inWall;
    }

    public DamageSource cramming() {
        return this.cramming;
    }

    public DamageSource drown() {
        return this.drown;
    }

    public DamageSource starve() {
        return this.starve;
    }

    public DamageSource cactus() {
        return this.cactus;
    }

    public DamageSource fall() {
        return this.fall;
    }

    public DamageSource flyIntoWall() {
        return this.flyIntoWall;
    }

    public DamageSource fellOutOfWorld() {
        return this.fellOutOfWorld;
    }

    public DamageSource generic() {
        return this.generic;
    }

    public DamageSource magic() {
        return this.magic;
    }

    public DamageSource wither() {
        return this.wither;
    }

    public DamageSource dragonBreath() {
        return this.dragonBreath;
    }

    public DamageSource dryOut() {
        return this.dryOut;
    }

    public DamageSource sweetBerryBush() {
        return this.sweetBerryBush;
    }

    public DamageSource freeze() {
        return this.freeze;
    }

    public DamageSource stalagmite() {
        return this.stalagmite;
    }

    public DamageSource fallingBlock(Entity param0) {
        return this.source(DamageTypes.FALLING_BLOCK, param0);
    }

    public DamageSource anvil(Entity param0) {
        return this.source(DamageTypes.FALLING_ANVIL, param0);
    }

    public DamageSource fallingStalactite(Entity param0) {
        return this.source(DamageTypes.FALLING_STALACTITE, param0);
    }

    public DamageSource sting(LivingEntity param0) {
        return this.source(DamageTypes.STING, param0);
    }

    public DamageSource mobAttack(LivingEntity param0) {
        return this.source(DamageTypes.MOB_ATTACK, param0);
    }

    public DamageSource noAggroMobAttack(LivingEntity param0) {
        return this.source(DamageTypes.MOB_ATTACK_NO_AGGRO, param0);
    }

    public DamageSource playerAttack(Player param0) {
        return this.source(DamageTypes.PLAYER_ATTACK, param0);
    }

    public DamageSource arrow(AbstractArrow param0, @Nullable Entity param1) {
        return this.source(DamageTypes.ARROW, param0, param1);
    }

    public DamageSource trident(Entity param0, @Nullable Entity param1) {
        return this.source(DamageTypes.TRIDENT, param0, param1);
    }

    public DamageSource mobProjectile(Entity param0, @Nullable LivingEntity param1) {
        return this.source(DamageTypes.MOB_PROJECTILE, param0, param1);
    }

    public DamageSource fireworks(FireworkRocketEntity param0, @Nullable Entity param1) {
        return this.source(DamageTypes.FIREWORKS, param0, param1);
    }

    public DamageSource fireball(Fireball param0, @Nullable Entity param1) {
        return param1 == null ? this.source(DamageTypes.UNATTRIBUTED_FIREBALL, param0) : this.source(DamageTypes.FIREBALL, param0, param1);
    }

    public DamageSource witherSkull(WitherSkull param0, Entity param1) {
        return this.source(DamageTypes.WITHER_SKULL, param0, param1);
    }

    public DamageSource thrown(Entity param0, @Nullable Entity param1) {
        return this.source(DamageTypes.THROWN, param0, param1);
    }

    public DamageSource indirectMagic(Entity param0, @Nullable Entity param1) {
        return this.source(DamageTypes.INDIRECT_MAGIC, param0, param1);
    }

    public DamageSource thorns(Entity param0) {
        return this.source(DamageTypes.THORNS, param0);
    }

    public DamageSource explosion(@Nullable Explosion param0) {
        return param0 != null ? this.explosion(param0.getDirectSourceEntity(), param0.getIndirectSourceEntity()) : this.explosion(null, null);
    }

    public DamageSource explosion(@Nullable Entity param0, @Nullable Entity param1) {
        return this.source(param1 != null && param0 != null ? DamageTypes.PLAYER_EXPLOSION : DamageTypes.EXPLOSION, param0, param1);
    }

    public DamageSource sonicBoom(Entity param0) {
        return this.source(DamageTypes.SONIC_BOOM, param0);
    }

    public DamageSource badRespawnPointExplosion(Vec3 param0) {
        return new DamageSource(this.damageTypes.getHolderOrThrow(DamageTypes.BAD_RESPAWN_POINT), param0);
    }

    public DamageSource outOfBorder() {
        return this.outsideBorder;
    }

    public DamageSource genericKill() {
        return this.genericKill;
    }
}
