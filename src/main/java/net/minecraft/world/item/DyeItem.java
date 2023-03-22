package net.minecraft.world.item;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SignBlockEntity;

public class DyeItem extends Item implements SignApplicator {
    private static final Map<DyeColor, DyeItem> ITEM_BY_COLOR = Maps.newEnumMap(DyeColor.class);
    private final DyeColor dyeColor;

    public DyeItem(DyeColor param0, Item.Properties param1) {
        super(param1);
        this.dyeColor = param0;
        ITEM_BY_COLOR.put(param0, this);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack param0, Player param1, LivingEntity param2, InteractionHand param3) {
        if (param2 instanceof Sheep var0 && var0.isAlive() && !var0.isSheared() && var0.getColor() != this.dyeColor) {
            var0.level.playSound(param1, var0, SoundEvents.DYE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
            if (!param1.level.isClientSide) {
                var0.setColor(this.dyeColor);
                param0.shrink(1);
            }

            return InteractionResult.sidedSuccess(param1.level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    public DyeColor getDyeColor() {
        return this.dyeColor;
    }

    public static DyeItem byColor(DyeColor param0) {
        return ITEM_BY_COLOR.get(param0);
    }

    @Override
    public boolean tryApplyToSign(Level param0, SignBlockEntity param1, boolean param2, Player param3) {
        if (param1.updateText(param0x -> param0x.setColor(this.getDyeColor()), param2)) {
            param0.playSound(null, param1.getBlockPos(), SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            return true;
        } else {
            return false;
        }
    }
}
