package org.zwobble.mammoth.tests.docx;

import com.google.common.collect.ImmutableList;
import com.natpryce.makeiteasy.Maker;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.zwobble.mammoth.documents.*;
import org.zwobble.mammoth.docx.BodyXmlReader;
import org.zwobble.mammoth.docx.Styles;
import org.zwobble.mammoth.tests.DeepReflectionMatcher;
import org.zwobble.mammoth.xml.XmlElement;
import org.zwobble.mammoth.xml.XmlNode;
import org.zwobble.mammoth.xml.XmlNodes;

import java.util.List;
import java.util.Optional;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.zwobble.mammoth.tests.DeepReflectionMatcher.deepEquals;
import static org.zwobble.mammoth.tests.documents.DocumentElementMakers.CHILDREN;
import static org.zwobble.mammoth.tests.documents.DocumentElementMakers.PARAGRAPH;
import static org.zwobble.mammoth.tests.docx.BodyXmlReaderMakers.STYLES;
import static org.zwobble.mammoth.tests.docx.BodyXmlReaderMakers.bodyReader;
import static org.zwobble.mammoth.util.MammothMaps.map;
import static org.zwobble.mammoth.xml.XmlNodes.element;

public class BodyXmlTests {

    @Test
    public void textFromTextElementIsRead() {
        XmlElement element = textXml("Hello!");
        assertThat(read(a(bodyReader), element), isTextElement("Hello!"));
    }

    @Test
    public void canReadTextWithinRun() {
        XmlElement element = runXml(ImmutableList.of(textXml("Hello!")));
        assertThat(
            read(a(bodyReader), element),
            isRun(run(text("Hello!"))));
    }

    @Test
    public void canReadTextWithinParagraph() {
        XmlElement element = paragraphXml(ImmutableList.of(runXml(ImmutableList.of(textXml("Hello!")))));
        assertThat(
            read(a(bodyReader), element),
            isParagraph(make(a(PARAGRAPH, with(CHILDREN, asList(run(text("Hello!"))))))));
    }

    @Test
    public void paragraphHasNoStyleIfItHasNoProperties() {
        XmlElement element = paragraphXml();
        assertThat(
            read(a(bodyReader), element),
            hasStyle(Optional.empty()));
    }

    @Test
    public void paragraphHasStyleIdReadFromParagraphPropertiesIfPresent() {
        XmlElement element = paragraphXml(ImmutableList.of(
            element("w:pPr", ImmutableList.of(
                element("w:pStyle", map("w:val", "Heading1"))))));
        assertThat(
            read(a(bodyReader), element),
            hasStyle(Optional.of(new Style("Heading1", Optional.empty()))));
    }

    @Test
    public void whenParagraphHasStyleIdInStylesThenStyleNameIsReadFromStyles() {
        XmlElement element = paragraphXml(ImmutableList.of(
            element("w:pPr", ImmutableList.of(
                element("w:pStyle", map("w:val", "Heading1"))))));

        Style style = new Style("Heading1", Optional.of("Heading 1"));
        Styles styles = new Styles(
            map("Heading1", style),
            map());
        assertThat(
            read(a(bodyReader, with(STYLES, styles)), element),
            hasStyle(Optional.of(style)));
    }

    private static DocumentElement read(Maker<BodyXmlReader> reader, XmlElement element) {
        return reader.make().readElement(element).get(0);
    }

    private XmlElement paragraphXml() {
        return paragraphXml(ImmutableList.of());
    }

    private XmlElement paragraphXml(List<XmlNode> children) {
        return element("w:p", children);
    }

    private XmlElement runXml(List<XmlNode> children) {
        return element("w:r", children);
    }

    private XmlElement textXml(String value) {
        return element("w:t", ImmutableList.of(XmlNodes.text(value)));
    }

    private Matcher<DocumentElement> isParagraph(Paragraph expected) {
        return new DeepReflectionMatcher<>(expected);
    }

    private Matcher<DocumentElement> isRun(Run expected) {
        return new DeepReflectionMatcher<>(expected);
    }

    private Matcher<DocumentElement> isTextElement(String value) {
        return new DeepReflectionMatcher<>(new Text(value));
    }

    private Matcher<? super DocumentElement> hasStyle(Optional<Style> expected) {
        return hasProperty("style", deepEquals(expected));
    }

    private Run run(DocumentElement... children) {
        return new Run(asList(children));
    }

    private Text text(String value) {
        return new Text(value);
    }
}
