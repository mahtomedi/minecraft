package net.minecraft.server.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import javax.swing.JComponent;
import javax.swing.Timer;
import net.minecraft.Util;
import net.minecraft.server.MinecraftServer;

public class StatsComponent extends JComponent {
    private static final DecimalFormat DECIMAL_FORMAT = Util.make(
        new DecimalFormat("########0.000"), param0 -> param0.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT))
    );
    private final int[] values = new int[256];
    private int vp;
    private final String[] msgs = new String[11];
    private final MinecraftServer server;
    private final Timer timer;

    public StatsComponent(MinecraftServer param0) {
        this.server = param0;
        this.setPreferredSize(new Dimension(456, 246));
        this.setMinimumSize(new Dimension(456, 246));
        this.setMaximumSize(new Dimension(456, 246));
        this.timer = new Timer(500, param0x -> this.tick());
        this.timer.start();
        this.setBackground(Color.BLACK);
    }

    private void tick() {
        long var0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        this.msgs[0] = "Memory use: "
            + var0 / 1024L / 1024L
            + " mb ("
            + Runtime.getRuntime().freeMemory() * 100L / Runtime.getRuntime().maxMemory()
            + "% free)";
        this.msgs[1] = "Avg tick: " + DECIMAL_FORMAT.format(this.getAverage(this.server.tickTimes) * 1.0E-6) + " ms";
        this.values[this.vp++ & 0xFF] = (int)(var0 * 100L / Runtime.getRuntime().maxMemory());
        this.repaint();
    }

    private double getAverage(long[] param0) {
        long var0 = 0L;

        for(long var1 : param0) {
            var0 += var1;
        }

        return (double)var0 / (double)param0.length;
    }

    @Override
    public void paint(Graphics param0) {
        param0.setColor(new Color(16777215));
        param0.fillRect(0, 0, 456, 246);

        for(int var0 = 0; var0 < 256; ++var0) {
            int var1 = this.values[var0 + this.vp & 0xFF];
            param0.setColor(new Color(var1 + 28 << 16));
            param0.fillRect(var0, 100 - var1, 1, var1);
        }

        param0.setColor(Color.BLACK);

        for(int var2 = 0; var2 < this.msgs.length; ++var2) {
            String var3 = this.msgs[var2];
            if (var3 != null) {
                param0.drawString(var3, 32, 116 + var2 * 16);
            }
        }

    }

    public void close() {
        this.timer.stop();
    }
}
