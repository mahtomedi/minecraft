package net.minecraft.client.gui.spectator.categories;

import com.google.common.base.MoreObjects;
import java.util.List;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.gui.spectator.SpectatorMenuCategory;
import net.minecraft.client.gui.spectator.SpectatorMenuItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpectatorPage {
    private final SpectatorMenuCategory category;
    private final List<SpectatorMenuItem> items;
    private final int selection;

    public SpectatorPage(SpectatorMenuCategory param0, List<SpectatorMenuItem> param1, int param2) {
        this.category = param0;
        this.items = param1;
        this.selection = param2;
    }

    public SpectatorMenuItem getItem(int param0) {
        return param0 >= 0 && param0 < this.items.size()
            ? MoreObjects.firstNonNull(this.items.get(param0), SpectatorMenu.EMPTY_SLOT)
            : SpectatorMenu.EMPTY_SLOT;
    }

    public int getSelectedSlot() {
        return this.selection;
    }
}
