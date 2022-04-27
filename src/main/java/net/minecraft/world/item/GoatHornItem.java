package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class GoatHornItem extends Item {
    private static final int SOUND_RANGE_BLOCKS = 256;
    public static final String TAG_SOUND_VARIANT = "SoundVariant";
    private static final int SCREAMING_SOUND_VARIANTS = 4;
    private static final int NON_SCREAMING_SOUND_VARIANTS = 4;
    private static final int USE_DURATION = 140;

    public GoatHornItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public void appendHoverText(ItemStack param0, @Nullable Level param1, List<Component> param2, TooltipFlag param3) {
        super.appendHoverText(param0, param1, param2, param3);
        CompoundTag var0 = param0.getOrCreateTag();
        MutableComponent var1 = Component.translatable("item.minecraft.goat_horn.sound." + var0.getInt("SoundVariant"));
        param2.add(var1.withStyle(ChatFormatting.GRAY));
    }

    private static ItemStack create(int param0) {
        ItemStack var0 = new ItemStack(Items.GOAT_HORN);
        setSoundVariantId(var0, param0);
        return var0;
    }

    public static ItemStack createFromGoat(Goat param0) {
        RandomSource var0 = RandomSource.create((long)param0.getUUID().hashCode());
        return create(decideRandomVariant(var0, param0.isScreamingGoat()));
    }

    public static void setRandomNonScreamingSound(ItemStack param0, RandomSource param1) {
        setSoundVariantId(param0, param1.nextInt(4));
    }

    private static void setSoundVariantId(ItemStack param0, int param1) {
        CompoundTag var0 = param0.getOrCreateTag();
        var0.put("SoundVariant", IntTag.valueOf(param1));
    }

    @Override
    public void fillItemCategory(CreativeModeTab param0, NonNullList<ItemStack> param1) {
        for(int var0 = 0; var0 < 8; ++var0) {
            param1.add(create(var0));
        }

    }

    protected static int decideRandomVariant(RandomSource param0, boolean param1) {
        int var0;
        int var1;
        if (param1) {
            var0 = 4;
            var1 = 7;
        } else {
            var0 = 0;
            var1 = 3;
        }

        return Mth.randomBetweenInclusive(param0, var0, var1);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level param0, Player param1, InteractionHand param2) {
        ItemStack var0 = param1.getItemInHand(param2);
        param1.startUsingItem(param2);
        SoundEvent var1 = SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(this.getSoundVariantId(var0));
        playSound(param0, param1, var1);
        param1.getCooldowns().addCooldown(Items.GOAT_HORN, 140);
        return InteractionResultHolder.consume(var0);
    }

    @Override
    public int getUseDuration(ItemStack param0) {
        return 140;
    }

    private int getSoundVariantId(ItemStack param0) {
        CompoundTag var0 = param0.getTag();
        return var0 == null ? 0 : var0.getInt("SoundVariant");
    }

    @Override
    public UseAnim getUseAnimation(ItemStack param0) {
        return UseAnim.TOOT_HORN;
    }

    private static void playSound(Level param0, Player param1, SoundEvent param2) {
        int var0 = 16;
        param0.playSound(param1, param1, param2, SoundSource.RECORDS, 16.0F, 1.0F);
    }
}
