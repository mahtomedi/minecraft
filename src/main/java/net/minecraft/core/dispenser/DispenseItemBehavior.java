package net.minecraft.core.dispenser;

import java.util.Random;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.WitherSkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;

public interface DispenseItemBehavior {
    DispenseItemBehavior NOOP = (param0, param1) -> param1;

    ItemStack dispense(BlockSource var1, ItemStack var2);

    static void bootStrap() {
        DispenserBlock.registerBehavior(Items.ARROW, new AbstractProjectileDispenseBehavior() {
            @Override
            protected Projectile getProjectile(Level param0, Position param1, ItemStack param2) {
                Arrow var0 = new Arrow(param0, param1.x(), param1.y(), param1.z());
                var0.pickup = AbstractArrow.Pickup.ALLOWED;
                return var0;
            }
        });
        DispenserBlock.registerBehavior(Items.TIPPED_ARROW, new AbstractProjectileDispenseBehavior() {
            @Override
            protected Projectile getProjectile(Level param0, Position param1, ItemStack param2) {
                Arrow var0 = new Arrow(param0, param1.x(), param1.y(), param1.z());
                var0.setEffectsFromItem(param2);
                var0.pickup = AbstractArrow.Pickup.ALLOWED;
                return var0;
            }
        });
        DispenserBlock.registerBehavior(Items.SPECTRAL_ARROW, new AbstractProjectileDispenseBehavior() {
            @Override
            protected Projectile getProjectile(Level param0, Position param1, ItemStack param2) {
                AbstractArrow var0 = new SpectralArrow(param0, param1.x(), param1.y(), param1.z());
                var0.pickup = AbstractArrow.Pickup.ALLOWED;
                return var0;
            }
        });
        DispenserBlock.registerBehavior(Items.EGG, new AbstractProjectileDispenseBehavior() {
            @Override
            protected Projectile getProjectile(Level param0, Position param1, ItemStack param2) {
                return Util.make(new ThrownEgg(param0, param1.x(), param1.y(), param1.z()), param1x -> param1x.setItem(param2));
            }
        });
        DispenserBlock.registerBehavior(Items.SNOWBALL, new AbstractProjectileDispenseBehavior() {
            @Override
            protected Projectile getProjectile(Level param0, Position param1, ItemStack param2) {
                return Util.make(new Snowball(param0, param1.x(), param1.y(), param1.z()), param1x -> param1x.setItem(param2));
            }
        });
        DispenserBlock.registerBehavior(Items.EXPERIENCE_BOTTLE, new AbstractProjectileDispenseBehavior() {
            @Override
            protected Projectile getProjectile(Level param0, Position param1, ItemStack param2) {
                return Util.make(new ThrownExperienceBottle(param0, param1.x(), param1.y(), param1.z()), param1x -> param1x.setItem(param2));
            }

            @Override
            protected float getUncertainty() {
                return super.getUncertainty() * 0.5F;
            }

            @Override
            protected float getPower() {
                return super.getPower() * 1.25F;
            }
        });
        DispenserBlock.registerBehavior(Items.SPLASH_POTION, new DispenseItemBehavior() {
            @Override
            public ItemStack dispense(BlockSource param0, ItemStack param1) {
                return (new AbstractProjectileDispenseBehavior() {
                    @Override
                    protected Projectile getProjectile(Level param0, Position param1, ItemStack param2) {
                        return Util.make(new ThrownPotion(param0, param1.x(), param1.y(), param1.z()), param1x -> param1x.setItem(param2));
                    }

                    @Override
                    protected float getUncertainty() {
                        return super.getUncertainty() * 0.5F;
                    }

                    @Override
                    protected float getPower() {
                        return super.getPower() * 1.25F;
                    }
                }).dispense(param0, param1);
            }
        });
        DispenserBlock.registerBehavior(Items.LINGERING_POTION, new DispenseItemBehavior() {
            @Override
            public ItemStack dispense(BlockSource param0, ItemStack param1) {
                return (new AbstractProjectileDispenseBehavior() {
                    @Override
                    protected Projectile getProjectile(Level param0, Position param1, ItemStack param2) {
                        return Util.make(new ThrownPotion(param0, param1.x(), param1.y(), param1.z()), param1x -> param1x.setItem(param2));
                    }

                    @Override
                    protected float getUncertainty() {
                        return super.getUncertainty() * 0.5F;
                    }

                    @Override
                    protected float getPower() {
                        return super.getPower() * 1.25F;
                    }
                }).dispense(param0, param1);
            }
        });
        DefaultDispenseItemBehavior var0 = new DefaultDispenseItemBehavior() {
            @Override
            public ItemStack execute(BlockSource param0, ItemStack param1) {
                Direction var0 = param0.getBlockState().getValue(DispenserBlock.FACING);
                EntityType<?> var1 = ((SpawnEggItem)param1.getItem()).getType(param1.getTag());
                var1.spawn(param0.getLevel(), param1, null, param0.getPos().relative(var0), MobSpawnType.DISPENSER, var0 != Direction.UP, false);
                param1.shrink(1);
                return param1;
            }
        };

        for(SpawnEggItem var1 : SpawnEggItem.eggs()) {
            DispenserBlock.registerBehavior(var1, var0);
        }

        DispenserBlock.registerBehavior(Items.FIREWORK_ROCKET, new DefaultDispenseItemBehavior() {
            @Override
            public ItemStack execute(BlockSource param0, ItemStack param1) {
                Direction var0 = param0.getBlockState().getValue(DispenserBlock.FACING);
                double var1 = param0.x() + (double)var0.getStepX();
                double var2 = (double)((float)param0.getPos().getY() + 0.2F);
                double var3 = param0.z() + (double)var0.getStepZ();
                param0.getLevel().addFreshEntity(new FireworkRocketEntity(param0.getLevel(), var1, var2, var3, param1));
                param1.shrink(1);
                return param1;
            }

            @Override
            protected void playSound(BlockSource param0) {
                param0.getLevel().levelEvent(1004, param0.getPos(), 0);
            }
        });
        DispenserBlock.registerBehavior(Items.FIRE_CHARGE, new DefaultDispenseItemBehavior() {
            @Override
            public ItemStack execute(BlockSource param0, ItemStack param1) {
                Direction var0 = param0.getBlockState().getValue(DispenserBlock.FACING);
                Position var1 = DispenserBlock.getDispensePosition(param0);
                double var2 = var1.x() + (double)((float)var0.getStepX() * 0.3F);
                double var3 = var1.y() + (double)((float)var0.getStepY() * 0.3F);
                double var4 = var1.z() + (double)((float)var0.getStepZ() * 0.3F);
                Level var5 = param0.getLevel();
                Random var6 = var5.random;
                double var7 = var6.nextGaussian() * 0.05 + (double)var0.getStepX();
                double var8 = var6.nextGaussian() * 0.05 + (double)var0.getStepY();
                double var9 = var6.nextGaussian() * 0.05 + (double)var0.getStepZ();
                var5.addFreshEntity(Util.make(new SmallFireball(var5, var2, var3, var4, var7, var8, var9), param1x -> param1x.setItem(param1)));
                param1.shrink(1);
                return param1;
            }

            @Override
            protected void playSound(BlockSource param0) {
                param0.getLevel().levelEvent(1018, param0.getPos(), 0);
            }
        });
        DispenserBlock.registerBehavior(Items.OAK_BOAT, new BoatDispenseItemBehavior(Boat.Type.OAK));
        DispenserBlock.registerBehavior(Items.SPRUCE_BOAT, new BoatDispenseItemBehavior(Boat.Type.SPRUCE));
        DispenserBlock.registerBehavior(Items.BIRCH_BOAT, new BoatDispenseItemBehavior(Boat.Type.BIRCH));
        DispenserBlock.registerBehavior(Items.JUNGLE_BOAT, new BoatDispenseItemBehavior(Boat.Type.JUNGLE));
        DispenserBlock.registerBehavior(Items.DARK_OAK_BOAT, new BoatDispenseItemBehavior(Boat.Type.DARK_OAK));
        DispenserBlock.registerBehavior(Items.ACACIA_BOAT, new BoatDispenseItemBehavior(Boat.Type.ACACIA));
        DispenseItemBehavior var2 = new DefaultDispenseItemBehavior() {
            private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();

            @Override
            public ItemStack execute(BlockSource param0, ItemStack param1) {
                BucketItem var0 = (BucketItem)param1.getItem();
                BlockPos var1 = param0.getPos().relative(param0.getBlockState().getValue(DispenserBlock.FACING));
                Level var2 = param0.getLevel();
                if (var0.emptyBucket(null, var2, var1, null)) {
                    var0.checkExtraContent(var2, param1, var1);
                    return new ItemStack(Items.BUCKET);
                } else {
                    return this.defaultDispenseItemBehavior.dispense(param0, param1);
                }
            }
        };
        DispenserBlock.registerBehavior(Items.LAVA_BUCKET, var2);
        DispenserBlock.registerBehavior(Items.WATER_BUCKET, var2);
        DispenserBlock.registerBehavior(Items.SALMON_BUCKET, var2);
        DispenserBlock.registerBehavior(Items.COD_BUCKET, var2);
        DispenserBlock.registerBehavior(Items.PUFFERFISH_BUCKET, var2);
        DispenserBlock.registerBehavior(Items.TROPICAL_FISH_BUCKET, var2);
        DispenserBlock.registerBehavior(Items.BUCKET, new DefaultDispenseItemBehavior() {
            private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();

            @Override
            public ItemStack execute(BlockSource param0, ItemStack param1) {
                LevelAccessor var0 = param0.getLevel();
                BlockPos var1 = param0.getPos().relative(param0.getBlockState().getValue(DispenserBlock.FACING));
                BlockState var2 = var0.getBlockState(var1);
                Block var3 = var2.getBlock();
                if (var3 instanceof BucketPickup) {
                    Fluid var4 = ((BucketPickup)var3).takeLiquid(var0, var1, var2);
                    if (!(var4 instanceof FlowingFluid)) {
                        return super.execute(param0, param1);
                    } else {
                        Item var5 = var4.getBucket();
                        param1.shrink(1);
                        if (param1.isEmpty()) {
                            return new ItemStack(var5);
                        } else {
                            if (param0.<DispenserBlockEntity>getEntity().addItem(new ItemStack(var5)) < 0) {
                                this.defaultDispenseItemBehavior.dispense(param0, new ItemStack(var5));
                            }

                            return param1;
                        }
                    }
                } else {
                    return super.execute(param0, param1);
                }
            }
        });
        DispenserBlock.registerBehavior(Items.FLINT_AND_STEEL, new OptionalDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource param0, ItemStack param1) {
                Level var0 = param0.getLevel();
                this.success = true;
                BlockPos var1 = param0.getPos().relative(param0.getBlockState().getValue(DispenserBlock.FACING));
                BlockState var2 = var0.getBlockState(var1);
                if (FlintAndSteelItem.canUse(var2, var0, var1)) {
                    var0.setBlockAndUpdate(var1, Blocks.FIRE.defaultBlockState());
                } else if (FlintAndSteelItem.canLightCampFire(var2)) {
                    var0.setBlockAndUpdate(var1, var2.setValue(BlockStateProperties.LIT, Boolean.valueOf(true)));
                } else if (var2.getBlock() instanceof TntBlock) {
                    TntBlock.explode(var0, var1);
                    var0.removeBlock(var1, false);
                } else {
                    this.success = false;
                }

                if (this.success && param1.hurt(1, var0.random, null)) {
                    param1.setCount(0);
                }

                return param1;
            }
        });
        DispenserBlock.registerBehavior(Items.BONE_MEAL, new OptionalDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource param0, ItemStack param1) {
                this.success = true;
                Level var0 = param0.getLevel();
                BlockPos var1 = param0.getPos().relative(param0.getBlockState().getValue(DispenserBlock.FACING));
                if (!BoneMealItem.growCrop(param1, var0, var1) && !BoneMealItem.growWaterPlant(param1, var0, var1, null)) {
                    this.success = false;
                } else if (!var0.isClientSide) {
                    var0.levelEvent(2005, var1, 0);
                }

                return param1;
            }
        });
        DispenserBlock.registerBehavior(Blocks.TNT, new DefaultDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource param0, ItemStack param1) {
                Level var0 = param0.getLevel();
                BlockPos var1 = param0.getPos().relative(param0.getBlockState().getValue(DispenserBlock.FACING));
                PrimedTnt var2 = new PrimedTnt(var0, (double)var1.getX() + 0.5, (double)var1.getY(), (double)var1.getZ() + 0.5, null);
                var0.addFreshEntity(var2);
                var0.playSound(null, var2.x, var2.y, var2.z, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
                param1.shrink(1);
                return param1;
            }
        });
        DispenseItemBehavior var3 = new OptionalDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource param0, ItemStack param1) {
                this.success = !ArmorItem.dispenseArmor(param0, param1).isEmpty();
                return param1;
            }
        };
        DispenserBlock.registerBehavior(Items.CREEPER_HEAD, var3);
        DispenserBlock.registerBehavior(Items.ZOMBIE_HEAD, var3);
        DispenserBlock.registerBehavior(Items.DRAGON_HEAD, var3);
        DispenserBlock.registerBehavior(Items.SKELETON_SKULL, var3);
        DispenserBlock.registerBehavior(Items.PLAYER_HEAD, var3);
        DispenserBlock.registerBehavior(
            Items.WITHER_SKELETON_SKULL,
            new OptionalDispenseItemBehavior() {
                @Override
                protected ItemStack execute(BlockSource param0, ItemStack param1) {
                    Level var0 = param0.getLevel();
                    Direction var1 = param0.getBlockState().getValue(DispenserBlock.FACING);
                    BlockPos var2 = param0.getPos().relative(var1);
                    this.success = true;
                    if (var0.isEmptyBlock(var2) && WitherSkullBlock.canSpawnMob(var0, var2, param1)) {
                        var0.setBlock(
                            var2,
                            Blocks.WITHER_SKELETON_SKULL
                                .defaultBlockState()
                                .setValue(
                                    SkullBlock.ROTATION, Integer.valueOf(var1.getAxis() == Direction.Axis.Y ? 0 : var1.getOpposite().get2DDataValue() * 4)
                                ),
                            3
                        );
                        BlockEntity var3 = var0.getBlockEntity(var2);
                        if (var3 instanceof SkullBlockEntity) {
                            WitherSkullBlock.checkSpawn(var0, var2, (SkullBlockEntity)var3);
                        }
    
                        param1.shrink(1);
                    } else if (ArmorItem.dispenseArmor(param0, param1).isEmpty()) {
                        this.success = false;
                    }
    
                    return param1;
                }
            }
        );
        DispenserBlock.registerBehavior(Blocks.CARVED_PUMPKIN, new OptionalDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource param0, ItemStack param1) {
                Level var0 = param0.getLevel();
                BlockPos var1 = param0.getPos().relative(param0.getBlockState().getValue(DispenserBlock.FACING));
                CarvedPumpkinBlock var2 = (CarvedPumpkinBlock)Blocks.CARVED_PUMPKIN;
                this.success = true;
                if (var0.isEmptyBlock(var1) && var2.canSpawnGolem(var0, var1)) {
                    if (!var0.isClientSide) {
                        var0.setBlock(var1, var2.defaultBlockState(), 3);
                    }

                    param1.shrink(1);
                } else {
                    ItemStack var3 = ArmorItem.dispenseArmor(param0, param1);
                    if (var3.isEmpty()) {
                        this.success = false;
                    }
                }

                return param1;
            }
        });
        DispenserBlock.registerBehavior(Blocks.SHULKER_BOX.asItem(), new ShulkerBoxDispenseBehavior());

        for(DyeColor var4 : DyeColor.values()) {
            DispenserBlock.registerBehavior(ShulkerBoxBlock.getBlockByColor(var4).asItem(), new ShulkerBoxDispenseBehavior());
        }

        DispenserBlock.registerBehavior(Items.SHEARS.asItem(), new OptionalDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource param0, ItemStack param1) {
                Level var0 = param0.getLevel();
                if (!var0.isClientSide()) {
                    this.success = false;
                    BlockPos var1 = param0.getPos().relative(param0.getBlockState().getValue(DispenserBlock.FACING));

                    for(Sheep var3 : var0.getEntitiesOfClass(Sheep.class, new AABB(var1))) {
                        if (var3.isAlive() && !var3.isSheared() && !var3.isBaby()) {
                            var3.shear();
                            if (param1.hurt(1, var0.random, null)) {
                                param1.setCount(0);
                            }

                            this.success = true;
                            break;
                        }
                    }
                }

                return param1;
            }
        });
    }
}
