package net.minecraft.client.gui.components;

import java.util.List;
import java.util.Optional;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface TooltipAccessor {
    Optional<List<FormattedCharSequence>> getTooltip();
}
