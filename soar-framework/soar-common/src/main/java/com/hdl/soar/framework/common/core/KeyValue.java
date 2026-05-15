package com.hdl.soar.framework.common.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Key-value pair.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeyValue<K, V> implements Serializable {
    private K key;
    private V value;
}
