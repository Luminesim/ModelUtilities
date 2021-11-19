package com.luminesim.citation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests {@link ReferenceListPrinter}
 */
public class ReferenceListPrinterTest {

    private ReferenceList refs;
    private ReferenceListPrinter printer;
    private PrintWriter out;
    private StringWriter result;

    @BeforeEach
    public void setup() {
        refs = new ReferenceList();
        printer = new ReferenceListPrinter(refs);
        result = new StringWriter();
        out = new PrintWriter(result);
    }

    private boolean resultMatches(String regex) {
        return result.toString().split("\n")[result.toString().split("\n").length-1].matches(regex);
    }

    /**
     * Ensures that the reference list printer provides a blank column for authority
     * when no author or authority is present.
     */
    @Test
    public void print_labelOnly_shouldHaveBareMinimumInfo() {
        refs.use(10, refs.citation("Test"));
        printer.print(out);
        assertTrue(resultMatches("Test\\|10\\|+?"), () -> "Got " + result.toString());
    }

    /**
     * Ensures that the reference list printer provides a column with the authority name
     * if only an authority is provided.
     */
    @Test
    public void print_onlyAuthority_shouldShowIt() {
        refs.use(10, refs.citation("Test").authority("Auth"));
        printer.print(out);
        assertTrue(resultMatches(".+?\\|Auth\\|.+?"), () -> "Got " + result.toString());
    }

    /**
     * Ensures that the reference list printer provides a column with a single author name
     * if only an author is provided.
     */
    @Test
    public void print_onlyOneAuthor_shouldShowWithoutComma() {
        refs.use(10, refs.citation("Test").author("Sam", "Handwich"));
        printer.print(out);
        assertTrue(resultMatches(".+?|Sam Handwich|.+?"), () -> "Got " + result.toString());
    }

    /**
     * Ensures that the reference list printer provides a column with comma-separated author names
     * if several authors are provided.
     */
    @Test
    public void print_manyAuthors_shouldShowWithCommas() {
        refs.use(10, refs.citation("Test").authors("Sam", "Handwich", "Bob", "Bobson"));
        printer.print(out);
        assertTrue(resultMatches(".+?\\|Sam Handwich, Bob Bobson\\|.+?"), () -> "Got " + result.toString());
    }

    /**
     * Ensures that the reference list printer provides a column with the authority and author names if both are
     * provided.
     */
    @Test
    public void print_bothAuthorsOrAuthority_shouldHaveAllWithAuthorityFirst() {
        refs.use(10, refs.citation("Test").authority("Company Corp").authors("Sam", "Handwich", "Bob", "Bobson"));
        printer.print(out);
        assertTrue(resultMatches(".+?\\|Company Corp, Sam Handwich, Bob Bobson\\|.+?"), () -> "Got " + result.toString());
    }
}
