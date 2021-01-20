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

public class DyeItem extends Item {
    private static final Map<DyeColor, DyeItem> ITEM_BY_COLOR = Maps.newEnumMap(DyeColor.class);
    private final DyeColor dyeColor;

    public DyeItem(DyeColor param0, Item.Properties param1) {
        super(param1);
        this.dyeColor = param0;
        ITEM_BY_COLOR.put(param0, this);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack param0, Player param1, LivingEntity param2, InteractionHand param3) {
        if (param2 instanceof Sheep) {
            Sheep var0 = (Sheep)param2;
            if (var0.isAlive() && !var0.isSheared() && var0.getColor() != this.dyeColor) {
                var0.level.playSound(param1, var0, SoundEvents.DYE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
                if (!param1.level.isClientSide) {
                    var0.setColor(this.dyeColor);
                    param0.shrink(1);
                }

                return InteractionResult.sidedSuccess(param1.level.isClientSide);
            }
        }

        return InteractionResult.PASS;
    }

    public DyeColor getDyeColor() {
        return this.dyeColor;
    }

    public static DyeItem byColor(DyeColor param0) {
        return ITEM_BY_COLOR.get(param0);
    }
}
