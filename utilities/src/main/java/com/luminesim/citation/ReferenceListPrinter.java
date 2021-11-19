package com.luminesim.citation;

import de.undercouch.citeproc.csl.CSLDate;
import lombok.AllArgsConstructor;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Prints a {@link ReferenceList}
 */
@AllArgsConstructor
public class ReferenceListPrinter {

    private final ReferenceList list;

    /**
     * A simple printout to console.
     */
    public void print() {
        print(System.out);
    }

    public void print(PrintWriter out) {
        String header = "Label|Value|Note|Authority|Title|URL";
        String tableMarker = "---|---|---|---|---|---";
        String format = "%s|%s|%s|%s|%s|%s";
        out.println("# Initial Parameters");

        LocalDate date = LocalDate.now();
        String text = date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM));
        out.println();
        out.println("*Last updated: " + text + "*");
        out.println();
        out.println(header);
        out.println(tableMarker);

        list.forEach(ref -> ref.getValue().forEachRevision((i, value) -> {
            if (i == 0) {
                // Author & authority a bit hard to deal with, so do it here.
                List<String> authority = new LinkedList<>();
                if (ref.getData().getAuthority() != null) {
                    authority.add(ref.getData().getAuthority());
                }
                if (ref.getData().getAuthor() != null) {
                    Arrays.stream(ref.getData().getAuthor())
                            .forEach(author -> authority.add(author.getGiven() + " " + author.getFamily()));
                }

                out.println(String.format(
                        format,
                        sanitize(ref.getLabel()),
                        sanitize(value),
                        sanitize(ref.getData().getNote()),
                        sanitize(authority.stream().collect(Collectors.joining(", "))),
                        sanitize(ref.getData().getTitle()),
                        sanitize(ref.getData().getURL())
                ));
            }
        }));
    }

    /**
     * A simple printout to console.
     */
    public void print(PrintStream out) {
        this.print(new PrintWriter(out));
    }

    /**
     * Sanitizes the string for a markdown table.
     *
     * @param raw
     * @return
     */
    private static String sanitize(Object raw) {
        if (raw == null) {
            return "";
        } else {
            String out = raw.toString();
            if (raw.getClass().isArray()) {
                out = Arrays.asList(raw).toString();
            }
            return out
                    .replace("|", "\\|")
                    .replace("\r\n", "; ")
                    .replace("\n", "; ");
        }
    }

}
