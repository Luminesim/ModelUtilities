package com.luminesim.citation;

import de.undercouch.citeproc.csl.CSLDate;
import lombok.AllArgsConstructor;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

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

    /**
     * A simple printout to console.
     */
    public void print(PrintStream out) {
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
                        out.println(String.format(
                                format,
                                sanitize(ref.getLabel()),
                                sanitize(value),
                                sanitize(ref.getData().getNote()),
                                sanitize(ref.getData().getAuthority()),
                                sanitize(ref.getData().getTitle()),
                                sanitize(ref.getData().getURL())
                                )
                        );
                    }
                }
        ));
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
            return raw.toString()
                    .replace("|", "\\|")
                    .replace("\r\n", "; ")
                    .replace("\n", "; ");
        }
    }

}
