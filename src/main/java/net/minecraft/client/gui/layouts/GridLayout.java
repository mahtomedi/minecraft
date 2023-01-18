package net.minecraft.client.gui.layouts;

import com.mojang.math.Divisor;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GridLayout extends AbstractLayout {
    private final List<LayoutElement> children = new ArrayList<>();
    private final List<GridLayout.CellInhabitant> cellInhabitants = new ArrayList<>();
    private final LayoutSettings defaultCellSettings = LayoutSettings.defaults();

    public GridLayout() {
        this(0, 0);
    }

    public GridLayout(int param0, int param1) {
        super(param0, param1, 0, 0);
    }

    @Override
    public void arrangeElements() {
        super.arrangeElements();
        int var0 = 0;
        int var1 = 0;

        for(GridLayout.CellInhabitant var2 : this.cellInhabitants) {
            var0 = Math.max(var2.getLastOccupiedRow(), var0);
            var1 = Math.max(var2.getLastOccupiedColumn(), var1);
        }

        int[] var3 = new int[var1 + 1];
        int[] var4 = new int[var0 + 1];

        for(GridLayout.CellInhabitant var5 : this.cellInhabitants) {
            Divisor var6 = new Divisor(var5.getHeight(), var5.occupiedRows);

            for(int var7 = var5.row; var7 <= var5.getLastOccupiedRow(); ++var7) {
                var4[var7] = Math.max(var4[var7], var6.nextInt());
            }

            Divisor var8 = new Divisor(var5.getWidth(), var5.occupiedColumns);

            for(int var9 = var5.column; var9 <= var5.getLastOccupiedColumn(); ++var9) {
                var3[var9] = Math.max(var3[var9], var8.nextInt());
            }
        }

        int[] var10 = new int[var1 + 1];
        int[] var11 = new int[var0 + 1];
        var10[0] = 0;

        for(int var12 = 1; var12 <= var1; ++var12) {
            var10[var12] = var10[var12 - 1] + var3[var12 - 1];
        }

        var11[0] = 0;

        for(int var13 = 1; var13 <= var0; ++var13) {
            var11[var13] = var11[var13 - 1] + var4[var13 - 1];
        }

        for(GridLayout.CellInhabitant var14 : this.cellInhabitants) {
            int var15 = 0;

            for(int var16 = var14.column; var16 <= var14.getLastOccupiedColumn(); ++var16) {
                var15 += var3[var16];
            }

            var14.setX(this.getX() + var10[var14.column], var15);
            int var17 = 0;

            for(int var18 = var14.row; var18 <= var14.getLastOccupiedRow(); ++var18) {
                var17 += var4[var18];
            }

            var14.setY(this.getY() + var11[var14.row], var17);
        }

        this.width = var10[var1] + var3[var1];
        this.height = var11[var0] + var4[var0];
    }

    public <T extends LayoutElement> T addChild(T param0, int param1, int param2) {
        return this.addChild(param0, param1, param2, this.newCellSettings());
    }

    public <T extends LayoutElement> T addChild(T param0, int param1, int param2, LayoutSettings param3) {
        return this.addChild(param0, param1, param2, 1, 1, param3);
    }

    public <T extends LayoutElement> T addChild(T param0, int param1, int param2, int param3, int param4) {
        return this.addChild(param0, param1, param2, param3, param4, this.newCellSettings());
    }

    public <T extends LayoutElement> T addChild(T param0, int param1, int param2, int param3, int param4, LayoutSettings param5) {
        if (param3 < 1) {
            throw new IllegalArgumentException("Occupied rows must be at least 1");
        } else if (param4 < 1) {
            throw new IllegalArgumentException("Occupied columns must be at least 1");
        } else {
            this.cellInhabitants.add(new GridLayout.CellInhabitant(param0, param1, param2, param3, param4, param5));
            this.children.add(param0);
            return param0;
        }
    }

    @Override
    protected void visitChildren(Consumer<LayoutElement> param0) {
        this.children.forEach(param0);
    }

    public LayoutSettings newCellSettings() {
        return this.defaultCellSettings.copy();
    }

    public LayoutSettings defaultCellSetting() {
        return this.defaultCellSettings;
    }

    public GridLayout.RowHelper createRowHelper(int param0) {
        return new GridLayout.RowHelper(param0);
    }

    @OnlyIn(Dist.CLIENT)
    static class CellInhabitant extends AbstractLayout.AbstractChildWrapper {
        final int row;
        final int column;
        final int occupiedRows;
        final int occupiedColumns;

        CellInhabitant(LayoutElement param0, int param1, int param2, int param3, int param4, LayoutSettings param5) {
            super(param0, param5.getExposed());
            this.row = param1;
            this.column = param2;
            this.occupiedRows = param3;
            this.occupiedColumns = param4;
        }

        public int getLastOccupiedRow() {
            return this.row + this.occupiedRows - 1;
        }

        public int getLastOccupiedColumn() {
            return this.column + this.occupiedColumns - 1;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public final class RowHelper {
        private final int columns;
        private int index;

        RowHelper(int param1) {
            this.columns = param1;
        }

        public <T extends LayoutElement> T addChild(T param0) {
            return this.addChild(param0, 1);
        }

        public <T extends LayoutElement> T addChild(T param0, int param1) {
            return this.addChild(param0, param1, this.defaultCellSetting());
        }

        public <T extends LayoutElement> T addChild(T param0, LayoutSettings param1) {
            return this.addChild(param0, 1, param1);
        }

        public <T extends LayoutElement> T addChild(T param0, int param1, LayoutSettings param2) {
            int var0 = this.index / this.columns;
            int var1 = this.index % this.columns;
            if (var1 + param1 > this.columns) {
                ++var0;
                var1 = 0;
                this.index = Mth.roundToward(this.index, this.columns);
            }

            this.index += param1;
            return GridLayout.this.addChild(param0, var0, var1, 1, param1, param2);
        }

        public LayoutSettings newCellSettings() {
            return GridLayout.this.newCellSettings();
        }

        public LayoutSettings defaultCellSetting() {
            return GridLayout.this.defaultCellSetting();
        }
    }
}
