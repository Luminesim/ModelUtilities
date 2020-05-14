package com.luminesim.regions;

import lombok.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * A location. May have a parent location.
 */
@Data
@RequiredArgsConstructor
@ToString(of = {"name","id"})
@EqualsAndHashCode(of ={"id","name"})
public class Location implements HasAttributes {

    /**
     * The location's ID.
     */
    private final String id;

    /**
     * The name of the location.
     */
    private final String name;

    /**
     * Attributes of the location.
     */
    private Map<String, String> attributes = new HashMap<>();

    /**
     * Gets the attribute, using a default if not present.
     * @param name
     * @param <T>
     * @return
     */
    public <T> T getAttribute(@NonNull String name, Function<String, T> transform) {
        return transform.apply(attributes.get(name));
    }

    @Override
    public boolean hasAttribute(@NonNull String name) {
        return attributes.containsKey(name);
    }

    /**
     * Sets the attribute.
     * @param name
     * @param attribute
     */
    public void setAttribute(@NonNull String name, @NonNull String attribute) {
        attributes.put(name, attribute);
    }
}
