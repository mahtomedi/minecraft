package net.minecraft.server.gui;

import com.google.common.collect.Lists;
import com.mojang.util.QueueLogAppender;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.server.dedicated.DedicatedServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MinecraftServerGui extends JComponent {
    private static final Font MONOSPACED = new Font("Monospaced", 0, 12);
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String TITLE = "Minecraft server";
    private static final String SHUTDOWN_TITLE = "Minecraft server - shutting down!";
    private final DedicatedServer server;
    private Thread logAppenderThread;
    private final Collection<Runnable> finalizers = Lists.newArrayList();
    private final AtomicBoolean isClosing = new AtomicBoolean();

    public static MinecraftServerGui showFrameFor(final DedicatedServer param0) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception var3) {
        }

        final JFrame var0 = new JFrame("Minecraft server");
        final MinecraftServerGui var1 = new MinecraftServerGui(param0);
        var0.setDefaultCloseOperation(2);
        var0.add(var1);
        var0.pack();
        var0.setLocationRelativeTo(null);
        var0.setVisible(true);
        var0.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent param0x) {
                if (!var1.isClosing.getAndSet(true)) {
                    var0.setTitle("Minecraft server - shutting down!");
                    param0.halt(true);
                    var1.runFinalizers();
                }

            }
        });
        var1.addFinalizer(var0::dispose);
        var1.start();
        return var1;
    }

    private MinecraftServerGui(DedicatedServer param0) {
        this.server = param0;
        this.setPreferredSize(new Dimension(854, 480));
        this.setLayout(new BorderLayout());

        try {
            this.add(this.buildChatPanel(), "Center");
            this.add(this.buildInfoPanel(), "West");
        } catch (Exception var3) {
            LOGGER.error("Couldn't build server GUI", (Throwable)var3);
        }

    }

    public void addFinalizer(Runnable param0) {
        this.finalizers.add(param0);
    }

    private JComponent buildInfoPanel() {
        JPanel var0 = new JPanel(new BorderLayout());
        StatsComponent var1 = new StatsComponent(this.server);
        this.finalizers.add(var1::close);
        var0.add(var1, "North");
        var0.add(this.buildPlayerPanel(), "Center");
        var0.setBorder(new TitledBorder(new EtchedBorder(), "Stats"));
        return var0;
    }

    private JComponent buildPlayerPanel() {
        JList<?> var0 = new PlayerListComponent(this.server);
        JScrollPane var1 = new JScrollPane(var0, 22, 30);
        var1.setBorder(new TitledBorder(new EtchedBorder(), "Players"));
        return var1;
    }

    private JComponent buildChatPanel() {
        JPanel var0 = new JPanel(new BorderLayout());
        JTextArea var1 = new JTextArea();
        JScrollPane var2 = new JScrollPane(var1, 22, 30);
        var1.setEditable(false);
        var1.setFont(MONOSPACED);
        JTextField var3 = new JTextField();
        var3.addActionListener(param1 -> {
            String var0x = var3.getText().trim();
            if (!var0x.isEmpty()) {
                this.server.handleConsoleInput(var0x, this.server.createCommandSourceStack());
            }

            var3.setText("");
        });
        var1.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent param0) {
            }
        });
        var0.add(var2, "Center");
        var0.add(var3, "South");
        var0.setBorder(new TitledBorder(new EtchedBorder(), "Log and chat"));
        this.logAppenderThread = new Thread(() -> {
            String var0x;
            while((var0x = QueueLogAppender.getNextLogEvent("ServerGuiConsole")) != null) {
                this.print(var1, var2, var0x);
            }

        });
        this.logAppenderThread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        this.logAppenderThread.setDaemon(true);
        return var0;
    }

    public void start() {
        this.logAppenderThread.start();
    }

    public void close() {
        if (!this.isClosing.getAndSet(true)) {
            this.runFinalizers();
        }

    }

    private void runFinalizers() {
        this.finalizers.forEach(Runnable::run);
    }

    public void print(JTextArea param0, JScrollPane param1, String param2) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> this.print(param0, param1, param2));
        } else {
            Document var0 = param0.getDocument();
            JScrollBar var1 = param1.getVerticalScrollBar();
            boolean var2 = false;
            if (param1.getViewport().getView() == param0) {
                var2 = (double)var1.getValue() + var1.getSize().getHeight() + (double)(MONOSPACED.getSize() * 4) > (double)var1.getMaximum();
            }

            try {
                var0.insertString(var0.getLength(), param2, null);
            } catch (BadLocationException var8) {
            }

            if (var2) {
                var1.setValue(Integer.MAX_VALUE);
            }

        }
    }
}
