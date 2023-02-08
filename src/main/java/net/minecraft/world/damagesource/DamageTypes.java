package net.minecraft.world.damagesource;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public interface DamageTypes {
    ResourceKey<DamageType> IN_FIRE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("in_fire"));
    ResourceKey<DamageType> LIGHTNING_BOLT = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("lightning_bolt"));
    ResourceKey<DamageType> ON_FIRE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("on_fire"));
    ResourceKey<DamageType> LAVA = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("lava"));
    ResourceKey<DamageType> HOT_FLOOR = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("hot_floor"));
    ResourceKey<DamageType> IN_WALL = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("in_wall"));
    ResourceKey<DamageType> CRAMMING = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("cramming"));
    ResourceKey<DamageType> DROWN = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("drown"));
    ResourceKey<DamageType> STARVE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("starve"));
    ResourceKey<DamageType> CACTUS = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("cactus"));
    ResourceKey<DamageType> FALL = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("fall"));
    ResourceKey<DamageType> FLY_INTO_WALL = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("fly_into_wall"));
    ResourceKey<DamageType> OUT_OF_WORLD = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("out_of_world"));
    ResourceKey<DamageType> GENERIC = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("generic"));
    ResourceKey<DamageType> MAGIC = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("magic"));
    ResourceKey<DamageType> WITHER = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("wither"));
    ResourceKey<DamageType> DRAGON_BREATH = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("dragon_breath"));
    ResourceKey<DamageType> DRY_OUT = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("dry_out"));
    ResourceKey<DamageType> SWEET_BERRY_BUSH = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sweet_berry_bush"));
    ResourceKey<DamageType> FREEZE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("freeze"));
    ResourceKey<DamageType> STALAGMITE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("stalagmite"));
    ResourceKey<DamageType> FALLING_BLOCK = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("falling_block"));
    ResourceKey<DamageType> FALLING_ANVIL = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("falling_anvil"));
    ResourceKey<DamageType> FALLING_STALACTITE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("falling_stalactite"));
    ResourceKey<DamageType> STING = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sting"));
    ResourceKey<DamageType> MOB_ATTACK = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("mob_attack"));
    ResourceKey<DamageType> MOB_ATTACK_NO_AGGRO = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("mob_attack_no_aggro"));
    ResourceKey<DamageType> PLAYER_ATTACK = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("player_attack"));
    ResourceKey<DamageType> ARROW = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("arrow"));
    ResourceKey<DamageType> TRIDENT = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("trident"));
    ResourceKey<DamageType> MOB_PROJECTILE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("mob_projectile"));
    ResourceKey<DamageType> FIREWORKS = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("fireworks"));
    ResourceKey<DamageType> FIREBALL = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("fireball"));
    ResourceKey<DamageType> UNATTRIBUTED_FIREBALL = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("unattributed_fireball"));
    ResourceKey<DamageType> WITHER_SKULL = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("wither_skull"));
    ResourceKey<DamageType> THROWN = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("thrown"));
    ResourceKey<DamageType> INDIRECT_MAGIC = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("indirect_magic"));
    ResourceKey<DamageType> THORNS = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("thorns"));
    ResourceKey<DamageType> EXPLOSION = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("explosion"));
    ResourceKey<DamageType> PLAYER_EXPLOSION = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("player_explosion"));
    ResourceKey<DamageType> SONIC_BOOM = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sonic_boom"));
    ResourceKey<DamageType> BAD_RESPAWN_POINT = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("bad_respawn_point"));

    static void bootstrap(BootstapContext<DamageType> param0) {
        param0.register(IN_FIRE, new DamageType("inFire", 0.1F, DamageEffects.BURNING));
        param0.register(LIGHTNING_BOLT, new DamageType("lightningBolt", 0.1F));
        param0.register(ON_FIRE, new DamageType("onFire", 0.0F, DamageEffects.BURNING));
        param0.register(LAVA, new DamageType("lava", 0.1F, DamageEffects.BURNING));
        param0.register(HOT_FLOOR, new DamageType("hotFloor", 0.1F, DamageEffects.BURNING));
        param0.register(IN_WALL, new DamageType("inWall", 0.0F));
        param0.register(CRAMMING, new DamageType("cramming", 0.0F));
        param0.register(DROWN, new DamageType("drown", 0.0F, DamageEffects.DROWNING));
        param0.register(STARVE, new DamageType("starve", 0.0F));
        param0.register(CACTUS, new DamageType("cactus", 0.1F));
        param0.register(FALL, new DamageType("fall", DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0.0F, DamageEffects.HURT, DeathMessageType.FALL_VARIANTS));
        param0.register(FLY_INTO_WALL, new DamageType("flyIntoWall", 0.0F));
        param0.register(OUT_OF_WORLD, new DamageType("outOfWorld", 0.0F));
        param0.register(GENERIC, new DamageType("generic", 0.0F));
        param0.register(MAGIC, new DamageType("magic", 0.0F));
        param0.register(WITHER, new DamageType("wither", 0.0F));
        param0.register(DRAGON_BREATH, new DamageType("dragonBreath", 0.0F));
        param0.register(DRY_OUT, new DamageType("dryout", 0.1F));
        param0.register(SWEET_BERRY_BUSH, new DamageType("sweetBerryBush", 0.1F, DamageEffects.POKING));
        param0.register(FREEZE, new DamageType("freeze", 0.0F, DamageEffects.FREEZING));
        param0.register(STALAGMITE, new DamageType("stalagmite", 0.0F));
        param0.register(FALLING_BLOCK, new DamageType("fallingBlock", 0.1F));
        param0.register(FALLING_ANVIL, new DamageType("anvil", 0.1F));
        param0.register(FALLING_STALACTITE, new DamageType("fallingStalactite", 0.1F));
        param0.register(STING, new DamageType("sting", 0.1F));
        param0.register(MOB_ATTACK, new DamageType("mob", 0.1F));
        param0.register(MOB_ATTACK_NO_AGGRO, new DamageType("mob", 0.1F));
        param0.register(PLAYER_ATTACK, new DamageType("player", 0.1F));
        param0.register(ARROW, new DamageType("arrow", 0.1F));
        param0.register(TRIDENT, new DamageType("trident", 0.1F));
        param0.register(MOB_PROJECTILE, new DamageType("mob", 0.1F));
        param0.register(FIREWORKS, new DamageType("fireworks", 0.1F));
        param0.register(UNATTRIBUTED_FIREBALL, new DamageType("onFire", 0.1F, DamageEffects.BURNING));
        param0.register(FIREBALL, new DamageType("fireball", 0.1F, DamageEffects.BURNING));
        param0.register(WITHER_SKULL, new DamageType("witherSkull", 0.1F));
        param0.register(THROWN, new DamageType("thrown", 0.1F));
        param0.register(INDIRECT_MAGIC, new DamageType("indirectMagic", 0.0F));
        param0.register(THORNS, new DamageType("thorns", 0.1F, DamageEffects.THORNS));
        param0.register(EXPLOSION, new DamageType("explosion", DamageScaling.ALWAYS, 0.1F));
        param0.register(PLAYER_EXPLOSION, new DamageType("explosion.player", DamageScaling.ALWAYS, 0.1F));
        param0.register(SONIC_BOOM, new DamageType("sonic_boom", DamageScaling.ALWAYS, 0.0F));
        param0.register(
            BAD_RESPAWN_POINT, new DamageType("badRespawnPoint", DamageScaling.ALWAYS, 0.1F, DamageEffects.HURT, DeathMessageType.INTENTIONAL_GAME_DESIGN)
        );
    }
}
