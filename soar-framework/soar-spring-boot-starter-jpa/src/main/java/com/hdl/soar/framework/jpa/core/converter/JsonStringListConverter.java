package com.hdl.soar.framework.jpa.core.converter;

import jakarta.persistence.Converter;

import java.util.List;

/**
 * Converts {@code List<String>} to/from JSON string.
 * DB: {@code ["read","write"]} — Java: {@code List<String>}
 */
@Converter
public class JsonStringListConverter extends AbstractJsonConverter<List<String>> {}
