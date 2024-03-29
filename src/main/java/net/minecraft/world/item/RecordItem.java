package net.minecraft.world.item;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class RecordItem extends Item {
    private static final Map<SoundEvent, RecordItem> BY_NAME = Maps.newHashMap();
    private final int analogOutput;
    private final SoundEvent sound;
    private final int lengthInTicks;

    protected RecordItem(int param0, SoundEvent param1, Item.Properties param2, int param3) {
        super(param2);
        this.analogOutput = param0;
        this.sound = param1;
        this.lengthInTicks = param3 * 20;
        BY_NAME.put(this.sound, this);
    }

    @Override
    public InteractionResult useOn(UseOnContext param0) {
        Level var0 = param0.getLevel();
        BlockPos var1 = param0.getClickedPos();
        BlockState var2 = var0.getBlockState(var1);
        if (var2.is(Blocks.JUKEBOX) && !var2.getValue(JukeboxBlock.HAS_RECORD)) {
            ItemStack var3 = param0.getItemInHand();
            if (!var0.isClientSide) {
                Player var4 = param0.getPlayer();
                BlockEntity var8 = var0.getBlockEntity(var1);
                if (var8 instanceof JukeboxBlockEntity var5) {
                    var5.setTheItem(var3.copy());
                    var0.gameEvent(GameEvent.BLOCK_CHANGE, var1, GameEvent.Context.of(var4, var2));
                }

                var3.shrink(1);
                if (var4 != null) {
                    var4.awardStat(Stats.PLAY_RECORD);
                }
            }

            return InteractionResult.sidedSuccess(var0.isClientSide);
        } else {
            return InteractionResult.PASS;
        }
    }

    public int getAnalogOutput() {
        return this.analogOutput;
    }

    @Override
    public void appendHoverText(ItemStack param0, @Nullable Level param1, List<Component> param2, TooltipFlag param3) {
        param2.add(this.getDisplayName().withStyle(ChatFormatting.GRAY));
    }

    public MutableComponent getDisplayName() {
        return Component.translatable(this.getDescriptionId() + ".desc");
    }

    @Nullable
    public static RecordItem getBySound(SoundEvent param0) {
        return BY_NAME.get(param0);
    }

    public SoundEvent getSound() {
        return this.sound;
    }

    public int getLengthInTicks() {
        return this.lengthInTicks;
    }
}
