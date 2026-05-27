package com.hdl.soar.framework.web.core.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;

import java.io.IOException;

/**
 * Custom Jackson deserializer that trims whitespace from all String fields.
 * <p>
 * Behavior:
 * <ul>
 *   <li>{@code "  hello  "} → {@code "hello"}</li>
 *   <li>{@code "   "} → {@code null}</li>
 *   <li>{@code ""} → {@code null}</li>
 *   <li>{@code null} → {@code null}</li>
 * </ul>
 * <p>
 * This runs BEFORE Bean Validation, so {@code @NotBlank} and {@code @NotNull}
 * will correctly reject whitespace-only input.
 * <p>
 * To exclude a specific field from trimming (e.g. password), use:
 * <pre>{@code
 * @JsonDeserialize(using = StringDeserializer.class)
 * private String password;
 * }</pre>
 */
public class StringTrimDeserializer extends StringDeserializer {

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = super.deserialize(p, ctxt);
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}