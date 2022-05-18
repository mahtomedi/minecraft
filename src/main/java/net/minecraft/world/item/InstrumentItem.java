package net.minecraft.world.item;

import java.util.Iterator;
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
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

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
        Optional<ResourceKey<Instrument>> var0 = this.getInstrument(param0).flatMap(Holder::unwrapKey);
        if (var0.isPresent()) {
            MutableComponent var1 = Component.translatable(Util.makeDescriptionId("instrument", var0.get().location()));
            param2.add(var1.withStyle(ChatFormatting.GRAY));
        }

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
        Optional<Holder<Instrument>> var1 = this.getInstrument(var0);
        if (var1.isPresent()) {
            Instrument var2 = var1.get().value();
            param1.startUsingItem(param2);
            play(param0, param1, var2);
            param1.getCooldowns().addCooldown(this, var2.useDuration());
            return InteractionResultHolder.consume(var0);
        } else {
            return InteractionResultHolder.fail(var0);
        }
    }

    @Override
    public int getUseDuration(ItemStack param0) {
        Optional<Holder<Instrument>> var0 = this.getInstrument(param0);
        return var0.isPresent() ? var0.get().value().useDuration() : 0;
    }

    private Optional<Holder<Instrument>> getInstrument(ItemStack param0) {
        CompoundTag var0 = param0.getTag();
        if (var0 != null) {
            ResourceLocation var1 = ResourceLocation.tryParse(var0.getString("instrument"));
            if (var1 != null) {
                return Registry.INSTRUMENT.getHolder(ResourceKey.create(Registry.INSTRUMENT_REGISTRY, var1));
            }
        }

        Iterator<Holder<Instrument>> var2 = Registry.INSTRUMENT.getTagOrEmpty(this.instruments).iterator();
        return var2.hasNext() ? Optional.of(var2.next()) : Optional.empty();
    }

    @Override
    public UseAnim getUseAnimation(ItemStack param0) {
        return UseAnim.TOOT_HORN;
    }

    private static void play(Level param0, Player param1, Instrument param2) {
        SoundEvent var0 = param2.soundEvent();
        float var1 = param2.range() / 16.0F;
        param0.playSound(param1, param1, var0, SoundSource.RECORDS, var1, 1.0F);
        param0.gameEvent(GameEvent.INSTRUMENT_PLAY, param1.position(), GameEvent.Context.of(param1));
    }
}
