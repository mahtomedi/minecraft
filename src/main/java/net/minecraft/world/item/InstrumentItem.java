package net.minecraft.world.item;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class InstrumentItem extends Item {
    private static final String TAG_INSTRUMENT = "instrument";
    private TagKey<Instrument> instruments;

    public InstrumentItem(Item.Properties param0, TagKey<Instrument> param1) {
        super(param0);
        this.instruments = param1;
    }

    @Override
    public void appendHoverText(ItemStack param0, @Nullable Level param1, List<Component> param2, TooltipFlag param3) {
        super.appendHoverText(param0, param1, param2, param3);
        MutableComponent var0 = Component.translatable(Util.makeDescriptionId("instrument", getInstrumentLocation(param0)));
        param2.add(var0.withStyle(ChatFormatting.GRAY));
    }

    public static ItemStack create(Item param0, Holder<Instrument> param1) {
        ItemStack var0 = new ItemStack(param0);
        setSoundVariantId(var0, param1);
        return var0;
    }

    public static void setRandom(ItemStack param0, TagKey<Instrument> param1, RandomSource param2) {
        Optional<Holder<Instrument>> var0 = Registry.INSTRUMENT.getTag(param1).flatMap(param1x -> param1x.getRandomElement(param2));
        if (var0.isPresent()) {
            setSoundVariantId(param0, var0.get());
        }

    }

    private static void setSoundVariantId(ItemStack param0, Holder<Instrument> param1) {
        CompoundTag var0 = param0.getOrCreateTag();
        var0.putString("instrument", param1.unwrapKey().orElseThrow(() -> new IllegalStateException("Invalid instrument")).location().toString());
    }

    @Override
    public void fillItemCategory(CreativeModeTab param0, NonNullList<ItemStack> param1) {
        if (this.allowedIn(param0)) {
            for(Holder<Instrument> var0 : Registry.INSTRUMENT.getTagOrEmpty(this.instruments)) {
                param1.add(create(Items.GOAT_HORN, var0));
            }
        }

    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level param0, Player param1, InteractionHand param2) {
        ItemStack var0 = param1.getItemInHand(param2);
        Instrument var1 = getInstrument(var0);
        if (var1 != null) {
            param1.startUsingItem(param2);
            play(param0, param1, var1);
            param1.getCooldowns().addCooldown(this, var1.useDuration());
            return InteractionResultHolder.consume(var0);
        } else {
            return InteractionResultHolder.fail(var0);
        }
    }

    @Override
    public int getUseDuration(ItemStack param0) {
        Instrument var0 = getInstrument(param0);
        return var0 != null ? var0.useDuration() : 0;
    }

    @Nullable
    private static Instrument getInstrument(ItemStack param0) {
        return Registry.INSTRUMENT.get(getInstrumentLocation(param0));
    }

    @Nullable
    private static ResourceLocation getInstrumentLocation(ItemStack param0) {
        CompoundTag var0 = param0.getTag();
        return var0 != null ? ResourceLocation.tryParse(var0.getString("instrument")) : null;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack param0) {
        return UseAnim.TOOT_HORN;
    }

    private static void play(Level param0, Player param1, Instrument param2) {
        SoundEvent var0 = param2.soundEvent();
        float var1 = param2.range() / 16.0F;
        param0.playSound(param1, param1, var0, SoundSource.RECORDS, var1, 1.0F);
    }
}
