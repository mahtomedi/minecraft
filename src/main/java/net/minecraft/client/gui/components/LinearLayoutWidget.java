package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import com.mojang.math.Divisor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LinearLayoutWidget extends AbstractContainerWidget {
    private final LinearLayoutWidget.Orientation orientation;
    private final List<LinearLayoutWidget.ChildContainer> children = new ArrayList<>();
    private final List<AbstractWidget> containedChildrenView = Collections.unmodifiableList(Lists.transform(this.children, param0x -> param0x.child));
    private final LayoutSettings defaultChildLayoutSettings = LayoutSettings.defaults();

    public LinearLayoutWidget(int param0, int param1, LinearLayoutWidget.Orientation param2) {
        this(0, 0, param0, param1, param2);
    }

    public LinearLayoutWidget(int param0, int param1, int param2, int param3, LinearLayoutWidget.Orientation param4) {
        super(param0, param1, param2, param3, Component.empty());
        this.orientation = param4;
    }

    public void pack() {
        if (!this.children.isEmpty()) {
            int var0 = 0;
            int var1 = this.orientation.getSecondaryLength(this);

            for(LinearLayoutWidget.ChildContainer var2 : this.children) {
                var0 += this.orientation.getPrimaryLength(var2);
                var1 = Math.max(var1, this.orientation.getSecondaryLength(var2));
            }

            int var3 = this.orientation.getPrimaryLength(this) - var0;
            int var4 = this.orientation.getPrimaryPosition(this);
            Iterator<LinearLayoutWidget.ChildContainer> var5 = this.children.iterator();
            LinearLayoutWidget.ChildContainer var6 = var5.next();
            this.orientation.setPrimaryPosition(var6, var4);
            var4 += this.orientation.getPrimaryLength(var6);
            LinearLayoutWidget.ChildContainer var8;
            if (this.children.size() >= 2) {
                for(Divisor var7 = new Divisor(var3, this.children.size() - 1); var7.hasNext(); var4 += this.orientation.getPrimaryLength(var8)) {
                    var4 += var7.nextInt();
                    var8 = var5.next();
                    this.orientation.setPrimaryPosition(var8, var4);
                }
            }

            int var9 = this.orientation.getSecondaryPosition(this);

            for(LinearLayoutWidget.ChildContainer var10 : this.children) {
                this.orientation.setSecondaryPosition(var10, var9, var1);
            }

            this.orientation.setSecondaryLength(this, var1);
        }
    }

    @Override
    protected List<? extends AbstractWidget> getContainedChildren() {
        return this.containedChildrenView;
    }

    public LayoutSettings newChildLayoutSettings() {
        return this.defaultChildLayoutSettings.copy();
    }

    public LayoutSettings defaultChildLayoutSetting() {
        return this.defaultChildLayoutSettings;
    }

    public <T extends AbstractWidget> T addChild(T param0) {
        return this.addChild(param0, this.newChildLayoutSettings());
    }

    public <T extends AbstractWidget> T addChild(T param0, LayoutSettings param1) {
        this.children.add(new LinearLayoutWidget.ChildContainer(param0, param1));
        return param0;
    }

    @OnlyIn(Dist.CLIENT)
    static class ChildContainer extends AbstractContainerWidget.AbstractChildWrapper {
        protected ChildContainer(AbstractWidget param0, LayoutSettings param1) {
            super(param0, param1);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Orientation {
        HORIZONTAL,
        VERTICAL;

        int getPrimaryLength(AbstractWidget param0) {
            return switch(this) {
                case HORIZONTAL -> param0.getWidth();
                case VERTICAL -> param0.getHeight();
            };
        }

        int getPrimaryLength(LinearLayoutWidget.ChildContainer param0) {
            return switch(this) {
                case HORIZONTAL -> param0.getWidth();
                case VERTICAL -> param0.getHeight();
            };
        }

        int getSecondaryLength(AbstractWidget param0) {
            return switch(this) {
                case HORIZONTAL -> param0.getHeight();
                case VERTICAL -> param0.getWidth();
            };
        }

        int getSecondaryLength(LinearLayoutWidget.ChildContainer param0) {
            return switch(this) {
                case HORIZONTAL -> param0.getHeight();
                case VERTICAL -> param0.getWidth();
            };
        }

        void setPrimaryPosition(LinearLayoutWidget.ChildContainer param0, int param1) {
            switch(this) {
                case HORIZONTAL:
                    param0.setX(param1, param0.getWidth());
                    break;
                case VERTICAL:
                    param0.setY(param1, param0.getHeight());
            }

        }

        void setSecondaryPosition(LinearLayoutWidget.ChildContainer param0, int param1, int param2) {
            switch(this) {
                case HORIZONTAL:
                    param0.setY(param1, param2);
                    break;
                case VERTICAL:
                    param0.setX(param1, param2);
            }

        }

        int getPrimaryPosition(AbstractWidget param0) {
            return switch(this) {
                case HORIZONTAL -> param0.getX();
                case VERTICAL -> param0.getY();
            };
        }

        int getSecondaryPosition(AbstractWidget param0) {
            return switch(this) {
                case HORIZONTAL -> param0.getY();
                case VERTICAL -> param0.getX();
            };
        }

        void setSecondaryLength(AbstractWidget param0, int param1) {
            switch(this) {
                case HORIZONTAL:
                    param0.height = param1;
                    break;
                case VERTICAL:
                    param0.width = param1;
            }

        }
    }
}
