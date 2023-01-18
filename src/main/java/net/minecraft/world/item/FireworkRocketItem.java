package net.minecraft.world.item;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FireworkRocketItem extends Item {
    public static final byte[] CRAFTABLE_DURATIONS = new byte[]{1, 2, 3};
    public static final String TAG_FIREWORKS = "Fireworks";
    public static final String TAG_EXPLOSION = "Explosion";
    public static final String TAG_EXPLOSIONS = "Explosions";
    public static final String TAG_FLIGHT = "Flight";
    public static final String TAG_EXPLOSION_TYPE = "Type";
    public static final String TAG_EXPLOSION_TRAIL = "Trail";
    public static final String TAG_EXPLOSION_FLICKER = "Flicker";
    public static final String TAG_EXPLOSION_COLORS = "Colors";
    public static final String TAG_EXPLOSION_FADECOLORS = "FadeColors";
    public static final double ROCKET_PLACEMENT_OFFSET = 0.15;

    public FireworkRocketItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public InteractionResult useOn(UseOnContext param0) {
        Level var0 = param0.getLevel();
        if (!var0.isClientSide) {
            ItemStack var1 = param0.getItemInHand();
            Vec3 var2 = param0.getClickLocation();
            Direction var3 = param0.getClickedFace();
            FireworkRocketEntity var4 = new FireworkRocketEntity(
                var0,
                param0.getPlayer(),
                var2.x + (double)var3.getStepX() * 0.15,
                var2.y + (double)var3.getStepY() * 0.15,
                var2.z + (double)var3.getStepZ() * 0.15,
                var1
            );
            var0.addFreshEntity(var4);
            var1.shrink(1);
        }

        return InteractionResult.sidedSuccess(var0.isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level param0, Player param1, InteractionHand param2) {
        if (param1.isFallFlying()) {
            ItemStack var0 = param1.getItemInHand(param2);
            if (!param0.isClientSide) {
                FireworkRocketEntity var1 = new FireworkRocketEntity(param0, var0, param1);
                param0.addFreshEntity(var1);
                if (!param1.getAbilities().instabuild) {
                    var0.shrink(1);
                }

                param1.awardStat(Stats.ITEM_USED.get(this));
            }

            return InteractionResultHolder.sidedSuccess(param1.getItemInHand(param2), param0.isClientSide());
        } else {
            return InteractionResultHolder.pass(param1.getItemInHand(param2));
        }
    }

    @Override
    public void appendHoverText(ItemStack param0, @Nullable Level param1, List<Component> param2, TooltipFlag param3) {
        CompoundTag var0 = param0.getTagElement("Fireworks");
        if (var0 != null) {
            if (var0.contains("Flight", 99)) {
                param2.add(
                    Component.translatable("item.minecraft.firework_rocket.flight")
                        .append(CommonComponents.SPACE)
                        .append(String.valueOf(var0.getByte("Flight")))
                        .withStyle(ChatFormatting.GRAY)
                );
            }

            ListTag var1 = var0.getList("Explosions", 10);
            if (!var1.isEmpty()) {
                for(int var2 = 0; var2 < var1.size(); ++var2) {
                    CompoundTag var3 = var1.getCompound(var2);
                    List<Component> var4 = Lists.newArrayList();
                    FireworkStarItem.appendHoverText(var3, var4);
                    if (!var4.isEmpty()) {
                        for(int var5 = 1; var5 < var4.size(); ++var5) {
                            var4.set(var5, Component.literal("  ").append(var4.get(var5)).withStyle(ChatFormatting.GRAY));
                        }

                        param2.addAll(var4);
                    }
                }
            }

        }
    }

    public static void setDuration(ItemStack param0, byte param1) {
        param0.getOrCreateTagElement("Fireworks").putByte("Flight", param1);
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack var0 = new ItemStack(this);
        setDuration(var0, (byte)1);
        return var0;
    }

    public static enum Shape {
        SMALL_BALL(0, "small_ball"),
        LARGE_BALL(1, "large_ball"),
        STAR(2, "star"),
        CREEPER(3, "creeper"),
        BURST(4, "burst");

        private static final IntFunction<FireworkRocketItem.Shape> BY_ID = ByIdMap.continuous(
            FireworkRocketItem.Shape::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO
        );
        private final int id;
        private final String name;

        private Shape(int param0, String param1) {
            this.id = param0;
            this.name = param1;
        }

        public int getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public static FireworkRocketItem.Shape byId(int param0) {
            return BY_ID.apply(param0);
        }
    }
}
