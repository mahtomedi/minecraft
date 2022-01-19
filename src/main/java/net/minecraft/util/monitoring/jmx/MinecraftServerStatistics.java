package net.minecraft.util.monitoring.jmx;

import com.mojang.logging.LogUtils;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.DynamicMBean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

public final class MinecraftServerStatistics implements DynamicMBean {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final MinecraftServer server;
    private final MBeanInfo mBeanInfo;
    private final Map<String, MinecraftServerStatistics.AttributeDescription> attributeDescriptionByName = Stream.of(
            new MinecraftServerStatistics.AttributeDescription("tickTimes", this::getTickTimes, "Historical tick times (ms)", long[].class),
            new MinecraftServerStatistics.AttributeDescription("averageTickTime", this::getAverageTickTime, "Current average tick time (ms)", Long.TYPE)
        )
        .collect(Collectors.toMap(param0x -> param0x.name, Function.identity()));

    private MinecraftServerStatistics(MinecraftServer param0) {
        this.server = param0;
        MBeanAttributeInfo[] var0 = this.attributeDescriptionByName
            .values()
            .stream()
            .map(MinecraftServerStatistics.AttributeDescription::asMBeanAttributeInfo)
            .toArray(param0x -> new MBeanAttributeInfo[param0x]);
        this.mBeanInfo = new MBeanInfo(
            MinecraftServerStatistics.class.getSimpleName(), "metrics for dedicated server", var0, null, null, new MBeanNotificationInfo[0]
        );
    }

    public static void registerJmxMonitoring(MinecraftServer param0) {
        try {
            ManagementFactory.getPlatformMBeanServer().registerMBean(new MinecraftServerStatistics(param0), new ObjectName("net.minecraft.server:type=Server"));
        } catch (InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException | MalformedObjectNameException var2) {
            LOGGER.warn("Failed to initialise server as JMX bean", (Throwable)var2);
        }

    }

    private float getAverageTickTime() {
        return this.server.getAverageTickTime();
    }

    private long[] getTickTimes() {
        return this.server.tickTimes;
    }

    @Nullable
    @Override
    public Object getAttribute(String param0) {
        MinecraftServerStatistics.AttributeDescription var0 = this.attributeDescriptionByName.get(param0);
        return var0 == null ? null : var0.getter.get();
    }

    @Override
    public void setAttribute(Attribute param0) {
    }

    @Override
    public AttributeList getAttributes(String[] param0) {
        List<Attribute> var0 = Arrays.stream(param0)
            .map(this.attributeDescriptionByName::get)
            .filter(Objects::nonNull)
            .map(param0x -> new Attribute(param0x.name, param0x.getter.get()))
            .collect(Collectors.toList());
        return new AttributeList(var0);
    }

    @Override
    public AttributeList setAttributes(AttributeList param0) {
        return new AttributeList();
    }

    @Nullable
    @Override
    public Object invoke(String param0, Object[] param1, String[] param2) {
        return null;
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        return this.mBeanInfo;
    }

    static final class AttributeDescription {
        final String name;
        final Supplier<Object> getter;
        private final String description;
        private final Class<?> type;

        AttributeDescription(String param0, Supplier<Object> param1, String param2, Class<?> param3) {
            this.name = param0;
            this.getter = param1;
            this.description = param2;
            this.type = param3;
        }

        private MBeanAttributeInfo asMBeanAttributeInfo() {
            return new MBeanAttributeInfo(this.name, this.type.getSimpleName(), this.description, true, false, false);
        }
    }
}
