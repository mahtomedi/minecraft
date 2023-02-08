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
    private int rowSpacing = 0;
    private int columnSpacing = 0;

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
            int var6 = var5.getHeight() - (var5.occupiedRows - 1) * this.rowSpacing;
            Divisor var7 = new Divisor(var6, var5.occupiedRows);

            for(int var8 = var5.row; var8 <= var5.getLastOccupiedRow(); ++var8) {
                var4[var8] = Math.max(var4[var8], var7.nextInt());
            }

            int var9 = var5.getWidth() - (var5.occupiedColumns - 1) * this.columnSpacing;
            Divisor var10 = new Divisor(var9, var5.occupiedColumns);

            for(int var11 = var5.column; var11 <= var5.getLastOccupiedColumn(); ++var11) {
                var3[var11] = Math.max(var3[var11], var10.nextInt());
            }
        }

        int[] var12 = new int[var1 + 1];
        int[] var13 = new int[var0 + 1];
        var12[0] = 0;

        for(int var14 = 1; var14 <= var1; ++var14) {
            var12[var14] = var12[var14 - 1] + var3[var14 - 1] + this.columnSpacing;
        }

        var13[0] = 0;

        for(int var15 = 1; var15 <= var0; ++var15) {
            var13[var15] = var13[var15 - 1] + var4[var15 - 1] + this.rowSpacing;
        }

        for(GridLayout.CellInhabitant var16 : this.cellInhabitants) {
            int var17 = 0;

            for(int var18 = var16.column; var18 <= var16.getLastOccupiedColumn(); ++var18) {
                var17 += var3[var18];
            }

            var17 += this.columnSpacing * (var16.occupiedColumns - 1);
            var16.setX(this.getX() + var12[var16.column], var17);
            int var19 = 0;

            for(int var20 = var16.row; var20 <= var16.getLastOccupiedRow(); ++var20) {
                var19 += var4[var20];
            }

            var19 += this.rowSpacing * (var16.occupiedRows - 1);
            var16.setY(this.getY() + var13[var16.row], var19);
        }

        this.width = var12[var1] + var3[var1];
        this.height = var13[var0] + var4[var0];
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

    public GridLayout columnSpacing(int param0) {
        this.columnSpacing = param0;
        return this;
    }

    public GridLayout rowSpacing(int param0) {
        this.rowSpacing = param0;
        return this;
    }

    public GridLayout spacing(int param0) {
        return this.columnSpacing(param0).rowSpacing(param0);
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> param0) {
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

        public GridLayout getGrid() {
            return GridLayout.this;
        }

        public LayoutSettings newCellSettings() {
            return GridLayout.this.newCellSettings();
        }

        public LayoutSettings defaultCellSetting() {
            return GridLayout.this.defaultCellSetting();
        }
    }
}
