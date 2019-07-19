package net.minecraft.client.renderer.chunk;

import java.util.BitSet;
import java.util.Set;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VisibilitySet {
    private static final int FACINGS = Direction.values().length;
    private final BitSet data = new BitSet(FACINGS * FACINGS);

    public void add(Set<Direction> param0) {
        for(Direction var0 : param0) {
            for(Direction var1 : param0) {
                this.set(var0, var1, true);
            }
        }

    }

    public void set(Direction param0, Direction param1, boolean param2) {
        this.data.set(param0.ordinal() + param1.ordinal() * FACINGS, param2);
        this.data.set(param1.ordinal() + param0.ordinal() * FACINGS, param2);
    }

    public void setAll(boolean param0) {
        this.data.set(0, this.data.size(), param0);
    }

    public boolean visibilityBetween(Direction param0, Direction param1) {
        return this.data.get(param0.ordinal() + param1.ordinal() * FACINGS);
    }

    @Override
    public String toString() {
        StringBuilder var0 = new StringBuilder();
        var0.append(' ');

        for(Direction var1 : Direction.values()) {
            var0.append(' ').append(var1.toString().toUpperCase().charAt(0));
        }

        var0.append('\n');

        for(Direction var2 : Direction.values()) {
            var0.append(var2.toString().toUpperCase().charAt(0));

            for(Direction var3 : Direction.values()) {
                if (var2 == var3) {
                    var0.append("  ");
                } else {
                    boolean var4 = this.visibilityBetween(var2, var3);
                    var0.append(' ').append((char)(var4 ? 'Y' : 'n'));
                }
            }

            var0.append('\n');
        }

        return var0.toString();
    }
}
