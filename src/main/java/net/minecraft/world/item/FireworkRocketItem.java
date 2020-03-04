package net.minecraft.world.item;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FireworkRocketItem extends Item {
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

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level param0, Player param1, InteractionHand param2) {
        if (param1.isFallFlying()) {
            ItemStack var0 = param1.getItemInHand(param2);
            if (!param0.isClientSide) {
                param0.addFreshEntity(new FireworkRocketEntity(param0, var0, param1));
                if (!param1.abilities.instabuild) {
                    var0.shrink(1);
                }
            }

            return InteractionResultHolder.success(param1.getItemInHand(param2));
        } else {
            return InteractionResultHolder.pass(param1.getItemInHand(param2));
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack param0, @Nullable Level param1, List<Component> param2, TooltipFlag param3) {
        CompoundTag var0 = param0.getTagElement("Fireworks");
        if (var0 != null) {
            if (var0.contains("Flight", 99)) {
                param2.add(
                    new TranslatableComponent("item.minecraft.firework_rocket.flight")
                        .append(" ")
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
                            var4.set(var5, new TextComponent("  ").append(var4.get(var5)).withStyle(ChatFormatting.GRAY));
                        }

                        param2.addAll(var4);
                    }
                }
            }

        }
    }

    public static enum Shape {
        SMALL_BALL(0, "small_ball"),
        LARGE_BALL(1, "large_ball"),
        STAR(2, "star"),
        CREEPER(3, "creeper"),
        BURST(4, "burst");

        private static final FireworkRocketItem.Shape[] BY_ID = Arrays.stream(values())
            .sorted(Comparator.comparingInt(param0 -> param0.id))
            .toArray(param0 -> new FireworkRocketItem.Shape[param0]);
        private final int id;
        private final String name;

        private Shape(int param0, String param1) {
            this.id = param0;
            this.name = param1;
        }

        public int getId() {
            return this.id;
        }

        @OnlyIn(Dist.CLIENT)
        public String getName() {
            return this.name;
        }

        @OnlyIn(Dist.CLIENT)
        public static FireworkRocketItem.Shape byId(int param0) {
            return param0 >= 0 && param0 < BY_ID.length ? BY_ID[param0] : SMALL_BALL;
        }
    }
}
