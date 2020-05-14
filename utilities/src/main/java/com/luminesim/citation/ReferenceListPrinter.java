package com.luminesim.citation;

import de.undercouch.citeproc.csl.CSLDate;
import lombok.AllArgsConstructor;

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
        String header = "Label|Authority|Title|URL|Note|Value";
        String tableMarker = "---|---|---|---|---|---|---|---";
        String format = "%s|%s|%s|%s|%s|%s";
        System.out.println("# Initial Parameters");

        LocalDate date = LocalDate.now();
        String text = date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM));
        System.out.println();
        System.out.println("*Last updated: " + text + "*");
        System.out.println();
        System.out.println(header);
        System.out.println(tableMarker);
        list.forEach(ref -> ref.getValue().forEachRevision((i, value) -> {
                    if (i == 0) {
                        System.out.println(String.format(
                                format,
                                sanitize(ref.getLabel()),
                                sanitize(ref.getData().getAuthority()),
                                sanitize(ref.getData().getTitle()),
                                sanitize(ref.getData().getURL()),
                                sanitize(ref.getData().getNote()),
                                sanitize(value)//,
                                //sanitize((ref.getData().getOriginalDate() != null ? date(ref.getData().getOriginalDate()) : null)),
                                //sanitize((ref.getData().getAccessed() != null ? date(ref.getData().getAccessed()) : null))
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
