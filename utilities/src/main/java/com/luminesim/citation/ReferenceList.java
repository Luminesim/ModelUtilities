package com.luminesim.citation;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.CSLItemDataBuilder;
import de.undercouch.citeproc.csl.CSLName;
import de.undercouch.citeproc.csl.CSLNameBuilder;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Date;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Tracks all citations. Provides convenience methods to make citation tracking simple.
 * This is built atop {@link de.undercouch.citeproc.CSL} and all related
 * classes, so please see the original project!
 */
@Slf4j
public class ReferenceList {

    /**
     * Helps uniquely identify assumptions.
     */
    private int numberOfAssumptions = 0;

    /**
     * The values tied to citations in the library.
     * (Fully-qualified class name, Label, Value)
     */
    protected Table<String, String, Value<?>> values = HashBasedTable.create();

    /**
     * The citations in the library.
     * (Fully-qualified class name, Label, Value)
     *
     * @implNote Here to help make citations fast. The first iteration of this library
     * required static calls everywhere which was simple but a mess.
     */
    protected Table<String, String, Citation> citations = HashBasedTable.create();

    /**
     * Creates a citation or uses the established value for this class.
     * Note that citations can share label if the function is called from
     * different classes. E.g. MyHospital and MyClinic can have a different
     * "P(Send to XRay)". If two instances of the same class have different
     * cited values, you must include this in the label. E.g.
     * Unity and Wilkie instances of MyClinic might have
     * "P(Send to XRay | Unity)" and "P(Send to XRay | Wilkie)".
     *
     * @param value   The value to use
     * @param builder The citation itself. See {@link #citation(String)}
     * @pre builder must have been built using {@link #citation(String)}
     * @pre arguments not null
     */
    public <T> T use(T value, @NonNull Citation builder) {
        // Use the calling class as the class.
       String clazz = builder.clazz;
        if (!contains(clazz, builder)) {
            values.put(clazz, builder.getCitationLabel(), new Value<>(() -> value));
        }
        return (T)value(clazz, builder).get();
    }

    /**
     * @param value   The value to use, provided via a supplier to reduce recalculations.
     * @param builder The citation itself. See {@link #citation(String)}
     * @pre builder must have been built using {@link #citation(String)}
     * @pre arguments not null
     * @see #use(Supplier, Citation)
     */
    public <T> T use(Supplier<T> value, @NonNull Citation builder) {
        // Use the calling class as the class.
        String clazz = builder.clazz;
        if (!contains(clazz, builder)) {
            values.put(clazz, builder.getCitationLabel(), new Value<>(value));
        }
        return (T)value(clazz, builder).get();
    }

    /**
     * @param value   The value to use, provided via a supplier to reduce recalculations.
     * @param builder The citation itself. See {@link #citation(String)}
     * @pre builder must have been built using {@link #citation(String)}
     * @pre arguments not null
     * @see #use(Supplier, Citation)
     */
    public <T> Value<T> useRevisable(Supplier<T> value, @NonNull Citation builder) {
        // Use the calling class as the class.
        String clazz = builder.clazz;
        if (!contains(clazz, builder)) {
            values.put(clazz, builder.getCitationLabel(), new Value<>(value));
        }
        return value(clazz, builder);
    }

    /**
     * @param value   The value to use, provided via a supplier to reduce recalculations.
     * @param builder The citation itself. See {@link #citation(String)}
     * @pre builder must have been built using {@link #citation(String)}
     * @pre arguments not null
     * @see #use(Supplier, Citation)
     */
    public <T> Value<T> useRevisable(T value, @NonNull Citation builder) {
        // Use the calling class as the class.
        String clazz = builder.clazz;
        if (!contains(clazz, builder)) {
            values.put(clazz, builder.getCitationLabel(), new Value<>(() -> value));
        }
        return value(clazz, builder);
    }

    /**
     * @return The class that called
     */
    private static String getCallingClass() {

        // To test speed:
        return "NA";

        //StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
        //return walker.walk(s -> s.limit(5).reduce((A, B) -> B).get().getClassName());
        //return Thread.currentThread().getStackTrace()[4].getClassName();
    }

    /**
     * Processes the items in the reference list, sorted by class then label.
     * Note that this method may not be available in future releases.
     */
    public void forEach(Consumer<ReferenceListEntry> action) {
        values.rowKeySet()
                .stream()
                .sorted(String::compareTo)
                .forEach(clazz ->
                        values.row(clazz)
                                .keySet()
                                .stream()
                                .sorted(String::compareTo)
                                .forEach(label -> {
                                    action.accept(new ReferenceListEntry(
                                            clazz,
                                            label,
                                            citations.get(clazz, label).builder.build(),
                                            values.get(clazz, label)
                                    ));
                                })
                );
    }

    @Data
    @AllArgsConstructor
    public static class ReferenceListEntry {
        private String clazz;
        private String label;
        private CSLItemData data;
        private Value<?> value;
    }

    /**
     * Convenience method to quickly generate a name.
     * @param given
     * @param family
     * @return
     */
    public static CSLName name(String given, String family) {
        CSLNameBuilder name = new CSLNameBuilder();
        return name.given(given).family(family).build();
    }

    /**
     * @return The value associated with the class and builder's label.
     * @pre arguments not null
     * @pre {@link #contains(String, Citation)}
     */
    private <T> Value<T> value(@NonNull String clazz, @NonNull ReferenceList.Citation builder) {
        if (!contains(clazz, builder)) {
            throw new IllegalArgumentException(String.format(
                    "No value for label %s cited from %s.",
                    builder.getCitationLabel(),
                    clazz
            ));
        }
        return (Value<T>)values.get(clazz, builder.getCitationLabel());
    }

    /**
     * @return True, if a value has been cited for the given class and builder's label.
     */
    private boolean contains(@NonNull String clazz, @NonNull ReferenceList.Citation builder) {
        return contains(clazz, builder.getCitationLabel());
    }

    /**
     * @return True, if a value has been cited for the given class and builder's label.
     */
    private boolean contains(@NonNull String clazz, @NonNull String label) {
        return values.contains(clazz, label);
    }

    /**
     * @param label The citation's label.
     * @return A new citation, if nothing matches the label and calling class, or the existing citation.
     */
    public Citation citation(String label) {

        // Rapidly look up
        String clazz = getCallingClass();
        if (contains(clazz, label)) {
            return citations.get(clazz, label);
        } else {
            Citation citation = new Citation(clazz, label, new CSLItemDataBuilder());
            citations.put(clazz, label, citation);
            return citation;
        }
    }


    /**
     * Builds a citation.
     *
     * @implNote Based on an existing citation library, but extended to allow for rapid retrieval.
     * @see CSLItemDataBuilder
     */
    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public class Citation {

        /**
         * The label attached to the citation.
         */
        private String citationLabel;

        /**
         * The class attached to the citation.
         */
        private String clazz;

        /**
         * The citation builder.
         */
        private CSLItemDataBuilder builder;

        /**
         * If true, this citation is flagged as a placeholder.
         */
        private boolean isPlaceholder = false;

        /**
         * Creates a citation.
         *
         * @param clazz
         * @param citationLabel
         * @param builder
         */
        private Citation(@NonNull String clazz, @NonNull String citationLabel, @NonNull CSLItemDataBuilder builder) {
            this.citationLabel = citationLabel;
            this.clazz = clazz;
            this.builder = builder;
        }

        public Citation accessed(int year, int month, int day) {
            builder.accessed(year, month, day);
            return this;
        }

        public Citation author(@NonNull String given, @NonNull String family) {
            builder.author(given, family);
            return this;
        }

        public Citation author(@NonNull CSLName... people) {
            builder.author(people);
            return this;
        }

        public Citation authority(@NonNull String authority) {
            builder.authority(authority);
            return this;
        }

        public Citation DOI(@NonNull String doi) {
            builder.DOI(doi);
            return this;
        }

        public Citation ISBN(@NonNull String isbn) {
            builder.ISBN(isbn);
            return this;
        }

        public Citation ISSN(@NonNull String issn) {
            builder.ISSN(issn);
            return this;
        }

        /**
         * Marks the citation as a placeholder value.
         */
        public Citation isPlaceholder(boolean isPlaceholder) {
            this.isPlaceholder = true;
            return this;
        }

        public Citation issue(int issue) {
            builder.issue(issue);
            return this;
        }

        public Citation journalAbbreviation(@NonNull String abbreviation) {
            builder.journalAbbreviation(abbreviation);
            return this;
        }

        public Citation originalDate(int year, int month, int date) {
            builder.originalDate(year, month, date);
            return this;
        }

        public Citation originalDate(int year, int month) {
            builder.originalDate(year, month);
            return this;
        }

        public Citation originalDate(int year) {
            builder.originalDate(year);
            return this;
        }

        public Citation note(@NonNull String note) {
            builder.note(note);
            return this;
        }

        public Citation page(int page) {
            builder.page(page);
            return this;
        }

        public Citation publisher(@NonNull String publisher) {
            builder.publisher(publisher);
            return this;
        }

        public Citation section(@NonNull String section) {
            builder.section(section);
            return this;
        }

        public Citation title(@NonNull String title) {
            builder.title(title);
            return this;
        }

        public Citation volume(int volume) {
            builder.volume(volume);
            return this;
        }

        public Citation URL(@NonNull String url) {
            builder.URL(url);
            return this;
        }
    }

    /**
     * @return A citation, flagged as a placeholder.
     */
    public Citation placeholder(String label) {
        return citation(label)
                .authority("Placeholder")
                .isPlaceholder(true);
    }

    /**
     * @return A citation for an operational value, i.e. one important to the
     * everyday operation of a system, but not necessarily important
     * for outside users to teak.
     * E.g. the average time in the morning that people leave for work.
     *
     */
    public Citation operationalValue(String label) {
        return citation(label)
                .authority("Operational Value");
    }

    /**
     * Notes a critical assumption (similar to using any use() method),
     * allowing the caller to modify the resulting citation.
     */
    public Citation noteToReader(String note) {
        numberOfAssumptions += 1;
        Citation citation = citation("Assumption #" + numberOfAssumptions);
        citation.note(note);
        use("N/A", citation);
        return citation;
    }

    /**
     * A value attached to a citation.
     * @param <T>
     */
    public class Value<T> {
        private ArrayList<Supplier<T>> values = new ArrayList<>(1);
        protected ArrayList<Date> revisionTimes = new ArrayList<>(1);

        public Value(@NonNull Supplier<T> value) {
            values.add(value);
        }

        public void revise(@NonNull T value, @NonNull Date now) {
            this.values.add(() -> value);
            this.revisionTimes.add(now);
        }

        /**
         * Rolls back the value to its original value.
         * @param now
         */
        public void rollback(Date now) {
            if (revisionTimes.isEmpty()) {
                throw new IllegalStateException("There is nothing to roll back to.");
            }
            values.add(values.get(0));
            revisionTimes.add(now);
        }

        public T get() {
            return values.get(values.size()-1).get();
        }

        public Date getLastRevisionTime() {
            return revisionTimes.get(revisionTimes.size() - 1);
        }

        public void forEachRevision(BiConsumer<Integer, T> action) {
            for (int i = 0; i < values.size(); i += 1) {
                action.accept(i, values.get(i).get());
            }
        }

        public int getVersion() {
            return values.size();
        }

        @Override
        public String toString() {
            T value = get();
            if (value == null) {
                return "";
            }
            else {
                return "" + value;
            }
        }
    }
}
