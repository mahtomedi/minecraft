package net.minecraft.util;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringEscapeUtils;

public class CsvOutput {
    private final Writer output;
    private final int columnCount;

    private CsvOutput(Writer param0, List<String> param1) throws IOException {
        this.output = param0;
        this.columnCount = param1.size();
        this.writeLine(param1.stream());
    }

    public static CsvOutput.Builder builder() {
        return new CsvOutput.Builder();
    }

    public void writeRow(Object... param0) throws IOException {
        if (param0.length != this.columnCount) {
            throw new IllegalArgumentException("Invalid number of columns, expected " + this.columnCount + ", but got " + param0.length);
        } else {
            this.writeLine(Stream.of(param0));
        }
    }

    private void writeLine(Stream<?> param0) throws IOException {
        this.output.write((String)param0.map(CsvOutput::getStringValue).collect(Collectors.joining(",")) + "\r\n");
    }

    private static String getStringValue(@Nullable Object param0x) {
        return StringEscapeUtils.escapeCsv(param0x != null ? param0x.toString() : "[null]");
    }

    public static class Builder {
        private final List<String> headers = Lists.newArrayList();

        public CsvOutput.Builder addColumn(String param0) {
            this.headers.add(param0);
            return this;
        }

        public CsvOutput build(Writer param0) throws IOException {
            return new CsvOutput(param0, this.headers);
        }
    }
}
