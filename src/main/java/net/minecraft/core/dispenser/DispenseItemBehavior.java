package net.minecraft.core.dispenser;

import java.util.List;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.decoration.ArmorStand;
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
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.WitherSkullBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
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
                param0.getLevel().gameEvent(GameEvent.ENTITY_PLACE, param0.getPos());
                return param1;
            }
        };

        for(SpawnEggItem var1 : SpawnEggItem.eggs()) {
            DispenserBlock.registerBehavior(var1, var0);
        }

        DispenserBlock.registerBehavior(Items.ARMOR_STAND, new DefaultDispenseItemBehavior() {
            @Override
            public ItemStack execute(BlockSource param0, ItemStack param1) {
                Direction var0 = param0.getBlockState().getValue(DispenserBlock.FACING);
                BlockPos var1 = param0.getPos().relative(var0);
                Level var2 = param0.getLevel();
                ArmorStand var3 = new ArmorStand(var2, (double)var1.getX() + 0.5, (double)var1.getY(), (double)var1.getZ() + 0.5);
                EntityType.updateCustomEntityTag(var2, null, var3, param1.getTag());
                var3.yRot = var0.toYRot();
                var2.addFreshEntity(var3);
                param1.shrink(1);
                return param1;
            }
        });
        DispenserBlock.registerBehavior(Items.SADDLE, new OptionalDispenseItemBehavior() {
            @Override
            public ItemStack execute(BlockSource param0, ItemStack param1) {
                BlockPos var0 = param0.getPos().relative(param0.getBlockState().getValue(DispenserBlock.FACING));
                List<LivingEntity> var1 = param0.getLevel().getEntitiesOfClass(LivingEntity.class, new AABB(var0), param0x -> {
                    if (!(param0x instanceof Saddleable)) {
                        return false;
                    } else {
                        Saddleable var0x = (Saddleable)param0x;
                        return !var0x.isSaddled() && var0x.isSaddleable();
                    }
                });
                if (!var1.isEmpty()) {
                    ((Saddleable)var1.get(0)).equipSaddle(SoundSource.BLOCKS);
                    param1.shrink(1);
                    this.setSuccess(true);
                    return param1;
                } else {
                    return super.execute(param0, param1);
                }
            }
        });
        DefaultDispenseItemBehavior var2 = new OptionalDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource param0, ItemStack param1) {
                BlockPos var0 = param0.getPos().relative(param0.getBlockState().getValue(DispenserBlock.FACING));

                for(AbstractHorse var2 : param0.getLevel()
                    .getEntitiesOfClass(AbstractHorse.class, new AABB(var0), param0x -> param0x.isAlive() && param0x.canWearArmor())) {
                    if (var2.isArmor(param1) && !var2.isWearingArmor() && var2.isTamed()) {
                        var2.getSlot(401).set(param1.split(1));
                        this.setSuccess(true);
                        return param1;
                    }
                }

                return super.execute(param0, param1);
            }
        };
        DispenserBlock.registerBehavior(Items.LEATHER_HORSE_ARMOR, var2);
        DispenserBlock.registerBehavior(Items.IRON_HORSE_ARMOR, var2);
        DispenserBlock.registerBehavior(Items.GOLDEN_HORSE_ARMOR, var2);
        DispenserBlock.registerBehavior(Items.DIAMOND_HORSE_ARMOR, var2);
        DispenserBlock.registerBehavior(Items.WHITE_CARPET, var2);
        DispenserBlock.registerBehavior(Items.ORANGE_CARPET, var2);
        DispenserBlock.registerBehavior(Items.CYAN_CARPET, var2);
        DispenserBlock.registerBehavior(Items.BLUE_CARPET, var2);
        DispenserBlock.registerBehavior(Items.BROWN_CARPET, var2);
        DispenserBlock.registerBehavior(Items.BLACK_CARPET, var2);
        DispenserBlock.registerBehavior(Items.GRAY_CARPET, var2);
        DispenserBlock.registerBehavior(Items.GREEN_CARPET, var2);
        DispenserBlock.registerBehavior(Items.LIGHT_BLUE_CARPET, var2);
        DispenserBlock.registerBehavior(Items.LIGHT_GRAY_CARPET, var2);
        DispenserBlock.registerBehavior(Items.LIME_CARPET, var2);
        DispenserBlock.registerBehavior(Items.MAGENTA_CARPET, var2);
        DispenserBlock.registerBehavior(Items.PINK_CARPET, var2);
        DispenserBlock.registerBehavior(Items.PURPLE_CARPET, var2);
        DispenserBlock.registerBehavior(Items.RED_CARPET, var2);
        DispenserBlock.registerBehavior(Items.YELLOW_CARPET, var2);
        DispenserBlock.registerBehavior(
            Items.CHEST,
            new OptionalDispenseItemBehavior() {
                @Override
                public ItemStack execute(BlockSource param0, ItemStack param1) {
                    BlockPos var0 = param0.getPos().relative(param0.getBlockState().getValue(DispenserBlock.FACING));
    
                    for(AbstractChestedHorse var2 : param0.getLevel()
                        .getEntitiesOfClass(AbstractChestedHorse.class, new AABB(var0), param0x -> param0x.isAlive() && !param0x.hasChest())) {
                        if (var2.isTamed() && var2.getSlot(499).set(param1)) {
                            param1.shrink(1);
                            this.setSuccess(true);
                            return param1;
                        }
                    }
    
                    return super.execute(param0, param1);
                }
            }
        );
        DispenserBlock.registerBehavior(Items.FIREWORK_ROCKET, new DefaultDispenseItemBehavior() {
            @Override
            public ItemStack execute(BlockSource param0, ItemStack param1) {
                Direction var0 = param0.getBlockState().getValue(DispenserBlock.FACING);
                FireworkRocketEntity var1 = new FireworkRocketEntity(param0.getLevel(), param1, param0.x(), param0.y(), param0.x(), true);
                DispenseItemBehavior.setEntityPokingOutOfBlock(param0, var1, var0);
                var1.shoot((double)var0.getStepX(), (double)var0.getStepY(), (double)var0.getStepZ(), 0.5F, 1.0F);
                param0.getLevel().addFreshEntity(var1);
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
                SmallFireball var10 = new SmallFireball(var5, var2, var3, var4, var7, var8, var9);
                var5.addFreshEntity(Util.make(var10, param1x -> param1x.setItem(param1)));
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
        DispenseItemBehavior var3 = new DefaultDispenseItemBehavior() {
            private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();

            @Override
            public ItemStack execute(BlockSource param0, ItemStack param1) {
                DispensibleContainerItem var0 = (DispensibleContainerItem)param1.getItem();
                BlockPos var1 = param0.getPos().relative(param0.getBlockState().getValue(DispenserBlock.FACING));
                Level var2 = param0.getLevel();
                if (var0.emptyContents(null, var2, var1, null)) {
                    var0.checkExtraContent(null, var2, param1, var1);
                    return new ItemStack(Items.BUCKET);
                } else {
                    return this.defaultDispenseItemBehavior.dispense(param0, param1);
                }
            }
        };
        DispenserBlock.registerBehavior(Items.LAVA_BUCKET, var3);
        DispenserBlock.registerBehavior(Items.WATER_BUCKET, var3);
        DispenserBlock.registerBehavior(Items.POWDER_SNOW_BUCKET, var3);
        DispenserBlock.registerBehavior(Items.SALMON_BUCKET, var3);
        DispenserBlock.registerBehavior(Items.COD_BUCKET, var3);
        DispenserBlock.registerBehavior(Items.PUFFERFISH_BUCKET, var3);
        DispenserBlock.registerBehavior(Items.TROPICAL_FISH_BUCKET, var3);
        DispenserBlock.registerBehavior(Items.AXOLOTL_BUCKET, var3);
        DispenserBlock.registerBehavior(Items.BUCKET, new DefaultDispenseItemBehavior() {
            private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();

            @Override
            public ItemStack execute(BlockSource param0, ItemStack param1) {
                LevelAccessor var0 = param0.getLevel();
                BlockPos var1 = param0.getPos().relative(param0.getBlockState().getValue(DispenserBlock.FACING));
                BlockState var2 = var0.getBlockState(var1);
                Block var3 = var2.getBlock();
                if (var3 instanceof BucketPickup) {
                    ItemStack var4 = ((BucketPickup)var3).pickupBlock(var0, var1, var2);
                    if (var4.isEmpty()) {
                        return super.execute(param0, param1);
                    } else {
                        var0.gameEvent(null, GameEvent.FLUID_PICKUP, var1);
                        Item var5 = var4.getItem();
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
                this.setSuccess(true);
                Direction var1 = param0.getBlockState().getValue(DispenserBlock.FACING);
                BlockPos var2 = param0.getPos().relative(var1);
                BlockState var3 = var0.getBlockState(var2);
                if (BaseFireBlock.canBePlacedAt(var0, var2, var1)) {
                    var0.setBlockAndUpdate(var2, BaseFireBlock.getState(var0, var2));
                    var0.gameEvent(null, GameEvent.BLOCK_PLACE, var2);
                } else if (CampfireBlock.canLight(var3) || CandleBlock.canLight(var3) || CandleCakeBlock.canLight(var3)) {
                    var0.setBlockAndUpdate(var2, var3.setValue(BlockStateProperties.LIT, Boolean.valueOf(true)));
                    var0.gameEvent(null, GameEvent.BLOCK_CHANGE, var2);
                } else if (var3.getBlock() instanceof TntBlock) {
                    TntBlock.explode(var0, var2);
                    var0.removeBlock(var2, false);
                } else {
                    this.setSuccess(false);
                }

                if (this.isSuccess() && param1.hurt(1, var0.random, null)) {
                    param1.setCount(0);
                }

                return param1;
            }
        });
        DispenserBlock.registerBehavior(Items.BONE_MEAL, new OptionalDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource param0, ItemStack param1) {
                this.setSuccess(true);
                Level var0 = param0.getLevel();
                BlockPos var1 = param0.getPos().relative(param0.getBlockState().getValue(DispenserBlock.FACING));
                if (!BoneMealItem.growCrop(param1, var0, var1) && !BoneMealItem.growWaterPlant(param1, var0, var1, null)) {
                    this.setSuccess(false);
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
                var0.playSound(null, var2.getX(), var2.getY(), var2.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
                var0.gameEvent(null, GameEvent.ENTITY_PLACE, var1);
                param1.shrink(1);
                return param1;
            }
        });
        DispenseItemBehavior var4 = new OptionalDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource param0, ItemStack param1) {
                this.setSuccess(ArmorItem.dispenseArmor(param0, param1));
                return param1;
            }
        };
        DispenserBlock.registerBehavior(Items.CREEPER_HEAD, var4);
        DispenserBlock.registerBehavior(Items.ZOMBIE_HEAD, var4);
        DispenserBlock.registerBehavior(Items.DRAGON_HEAD, var4);
        DispenserBlock.registerBehavior(Items.SKELETON_SKULL, var4);
        DispenserBlock.registerBehavior(Items.PLAYER_HEAD, var4);
        DispenserBlock.registerBehavior(
            Items.WITHER_SKELETON_SKULL,
            new OptionalDispenseItemBehavior() {
                @Override
                protected ItemStack execute(BlockSource param0, ItemStack param1) {
                    Level var0 = param0.getLevel();
                    Direction var1 = param0.getBlockState().getValue(DispenserBlock.FACING);
                    BlockPos var2 = param0.getPos().relative(var1);
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
                        var0.gameEvent(null, GameEvent.BLOCK_PLACE, var2);
                        BlockEntity var3 = var0.getBlockEntity(var2);
                        if (var3 instanceof SkullBlockEntity) {
                            WitherSkullBlock.checkSpawn(var0, var2, (SkullBlockEntity)var3);
                        }
    
                        param1.shrink(1);
                        this.setSuccess(true);
                    } else {
                        this.setSuccess(ArmorItem.dispenseArmor(param0, param1));
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
                if (var0.isEmptyBlock(var1) && var2.canSpawnGolem(var0, var1)) {
                    if (!var0.isClientSide) {
                        var0.setBlock(var1, var2.defaultBlockState(), 3);
                        var0.gameEvent(null, GameEvent.BLOCK_PLACE, var1);
                    }

                    param1.shrink(1);
                    this.setSuccess(true);
                } else {
                    this.setSuccess(ArmorItem.dispenseArmor(param0, param1));
                }

                return param1;
            }
        });
        DispenserBlock.registerBehavior(Blocks.SHULKER_BOX.asItem(), new ShulkerBoxDispenseBehavior());

        for(DyeColor var5 : DyeColor.values()) {
            DispenserBlock.registerBehavior(ShulkerBoxBlock.getBlockByColor(var5).asItem(), new ShulkerBoxDispenseBehavior());
        }

        DispenserBlock.registerBehavior(Items.GLASS_BOTTLE.asItem(), new OptionalDispenseItemBehavior() {
            private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();

            private ItemStack takeLiquid(BlockSource param0, ItemStack param1, ItemStack param2) {
                param1.shrink(1);
                if (param1.isEmpty()) {
                    param0.getLevel().gameEvent(null, GameEvent.FLUID_PICKUP, param0.getPos());
                    return param2.copy();
                } else {
                    if (param0.<DispenserBlockEntity>getEntity().addItem(param2.copy()) < 0) {
                        this.defaultDispenseItemBehavior.dispense(param0, param2.copy());
                    }

                    return param1;
                }
            }

            @Override
            public ItemStack execute(BlockSource param0, ItemStack param1) {
                this.setSuccess(false);
                ServerLevel var0 = param0.getLevel();
                BlockPos var1 = param0.getPos().relative(param0.getBlockState().getValue(DispenserBlock.FACING));
                BlockState var2 = var0.getBlockState(var1);
                if (var2.is(BlockTags.BEEHIVES, param0x -> param0x.hasProperty(BeehiveBlock.HONEY_LEVEL)) && var2.getValue(BeehiveBlock.HONEY_LEVEL) >= 5) {
                    ((BeehiveBlock)var2.getBlock()).releaseBeesAndResetHoneyLevel(var0, var2, var1, null, BeehiveBlockEntity.BeeReleaseStatus.BEE_RELEASED);
                    this.setSuccess(true);
                    return this.takeLiquid(param0, param1, new ItemStack(Items.HONEY_BOTTLE));
                } else if (var0.getFluidState(var1).is(FluidTags.WATER)) {
                    this.setSuccess(true);
                    return this.takeLiquid(param0, param1, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER));
                } else {
                    return super.execute(param0, param1);
                }
            }
        });
        DispenserBlock.registerBehavior(Items.GLOWSTONE, new OptionalDispenseItemBehavior() {
            @Override
            public ItemStack execute(BlockSource param0, ItemStack param1) {
                Direction var0 = param0.getBlockState().getValue(DispenserBlock.FACING);
                BlockPos var1 = param0.getPos().relative(var0);
                Level var2 = param0.getLevel();
                BlockState var3 = var2.getBlockState(var1);
                this.setSuccess(true);
                if (var3.is(Blocks.RESPAWN_ANCHOR)) {
                    if (var3.getValue(RespawnAnchorBlock.CHARGE) != 4) {
                        RespawnAnchorBlock.charge(var2, var1, var3);
                        param1.shrink(1);
                    } else {
                        this.setSuccess(false);
                    }

                    return param1;
                } else {
                    return super.execute(param0, param1);
                }
            }
        });
        DispenserBlock.registerBehavior(Items.SHEARS.asItem(), new ShearsDispenseItemBehavior());
    }

    static void setEntityPokingOutOfBlock(BlockSource param0, Entity param1, Direction param2) {
        param1.setPos(
            param0.x() + (double)param2.getStepX() * (0.5000099999997474 - (double)param1.getBbWidth() / 2.0),
            param0.y() + (double)param2.getStepY() * (0.5000099999997474 - (double)param1.getBbHeight() / 2.0) - (double)param1.getBbHeight() / 2.0,
            param0.z() + (double)param2.getStepZ() * (0.5000099999997474 - (double)param1.getBbWidth() / 2.0)
        );
    }
}
