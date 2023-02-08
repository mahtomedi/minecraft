package net.minecraft.world.item;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.GlowItemFrame;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class HangingEntityItem extends Item {
    private static final Component TOOLTIP_RANDOM_VARIANT = Component.translatable("painting.random").withStyle(ChatFormatting.GRAY);
    private final EntityType<? extends HangingEntity> type;

    public HangingEntityItem(EntityType<? extends HangingEntity> param0, Item.Properties param1) {
        super(param1);
        this.type = param0;
    }

    @Override
    public InteractionResult useOn(UseOnContext param0) {
        BlockPos var0 = param0.getClickedPos();
        Direction var1 = param0.getClickedFace();
        BlockPos var2 = var0.relative(var1);
        Player var3 = param0.getPlayer();
        ItemStack var4 = param0.getItemInHand();
        if (var3 != null && !this.mayPlace(var3, var1, var4, var2)) {
            return InteractionResult.FAIL;
        } else {
            Level var5 = param0.getLevel();
            HangingEntity var7;
            if (this.type == EntityType.PAINTING) {
                Optional<Painting> var6 = Painting.create(var5, var2, var1);
                if (var6.isEmpty()) {
                    return InteractionResult.CONSUME;
                }

                var7 = var6.get();
            } else if (this.type == EntityType.ITEM_FRAME) {
                var7 = new ItemFrame(var5, var2, var1);
            } else {
                if (this.type != EntityType.GLOW_ITEM_FRAME) {
                    return InteractionResult.sidedSuccess(var5.isClientSide);
                }

                var7 = new GlowItemFrame(var5, var2, var1);
            }

            CompoundTag var11 = var4.getTag();
            if (var11 != null) {
                EntityType.updateCustomEntityTag(var5, var3, var7, var11);
            }

            if (var7.survives()) {
                if (!var5.isClientSide) {
                    var7.playPlacementSound();
                    var5.gameEvent(var3, GameEvent.ENTITY_PLACE, var7.position());
                    var5.addFreshEntity(var7);
                }

                var4.shrink(1);
                return InteractionResult.sidedSuccess(var5.isClientSide);
            } else {
                return InteractionResult.CONSUME;
            }
        }
    }

    protected boolean mayPlace(Player param0, Direction param1, ItemStack param2, BlockPos param3) {
        return !param1.getAxis().isVertical() && param0.mayUseItemAt(param3, param1, param2);
    }

    @Override
    public void appendHoverText(ItemStack param0, @Nullable Level param1, List<Component> param2, TooltipFlag param3) {
        super.appendHoverText(param0, param1, param2, param3);
        if (this.type == EntityType.PAINTING) {
            CompoundTag var0 = param0.getTag();
            if (var0 != null && var0.contains("EntityTag", 10)) {
                CompoundTag var1 = var0.getCompound("EntityTag");
                Painting.loadVariant(var1)
                    .ifPresentOrElse(
                        param1x -> {
                            param1x.unwrapKey().ifPresent(param1xx -> {
                                param2.add(Component.translatable(param1xx.location().toLanguageKey("painting", "title")).withStyle(ChatFormatting.YELLOW));
                                param2.add(Component.translatable(param1xx.location().toLanguageKey("painting", "author")).withStyle(ChatFormatting.GRAY));
                            });
                            param2.add(
                                Component.translatable(
                                    "painting.dimensions",
                                    Mth.positiveCeilDiv(((PaintingVariant)param1x.value()).getWidth(), 16),
                                    Mth.positiveCeilDiv(((PaintingVariant)param1x.value()).getHeight(), 16)
                                )
                            );
                        },
                        () -> param2.add(TOOLTIP_RANDOM_VARIANT)
                    );
            } else if (param3.isCreative()) {
                param2.add(TOOLTIP_RANDOM_VARIANT);
            }
        }

    }
}
