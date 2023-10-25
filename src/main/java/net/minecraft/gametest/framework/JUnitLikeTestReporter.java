package net.minecraft.gametest.framework;

import com.google.common.base.Stopwatch;
import java.io.File;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class JUnitLikeTestReporter implements TestReporter {
    private final Document document;
    private final Element testSuite;
    private final Stopwatch stopwatch;
    private final File destination;

    public JUnitLikeTestReporter(File param0) throws ParserConfigurationException {
        this.destination = param0;
        this.document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        this.testSuite = this.document.createElement("testsuite");
        Element var0 = this.document.createElement("testsuite");
        var0.appendChild(this.testSuite);
        this.document.appendChild(var0);
        this.testSuite.setAttribute("timestamp", DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
        this.stopwatch = Stopwatch.createStarted();
    }

    private Element createTestCase(GameTestInfo param0, String param1) {
        Element var0 = this.document.createElement("testcase");
        var0.setAttribute("name", param1);
        var0.setAttribute("classname", param0.getStructureName());
        var0.setAttribute("time", String.valueOf((double)param0.getRunTime() / 1000.0));
        this.testSuite.appendChild(var0);
        return var0;
    }

    @Override
    public void onTestFailed(GameTestInfo param0) {
        String var0 = param0.getTestName();
        String var1 = param0.getError().getMessage();
        Element var2 = this.document.createElement(param0.isRequired() ? "failure" : "skipped");
        var2.setAttribute("message", "(" + param0.getStructureBlockPos().toShortString() + ") " + var1);
        Element var3 = this.createTestCase(param0, var0);
        var3.appendChild(var2);
    }

    @Override
    public void onTestSuccess(GameTestInfo param0) {
        String var0 = param0.getTestName();
        this.createTestCase(param0, var0);
    }

    @Override
    public void finish() {
        this.stopwatch.stop();
        this.testSuite.setAttribute("time", String.valueOf((double)this.stopwatch.elapsed(TimeUnit.MILLISECONDS) / 1000.0));

        try {
            this.save(this.destination);
        } catch (TransformerException var2) {
            throw new Error("Couldn't save test report", var2);
        }
    }

    public void save(File param0) throws TransformerException {
        TransformerFactory var0 = TransformerFactory.newInstance();
        Transformer var1 = var0.newTransformer();
        DOMSource var2 = new DOMSource(this.document);
        StreamResult var3 = new StreamResult(param0);
        var1.transform(var2, var3);
    }
}
