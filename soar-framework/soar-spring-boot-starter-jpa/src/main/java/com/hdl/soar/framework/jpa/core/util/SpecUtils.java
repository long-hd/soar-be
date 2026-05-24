package com.hdl.soar.framework.jpa.core.util;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.SingularAttribute;

import java.util.List;

/**
 * Jpa Specification utilities class
 */
public class SpecUtils {

    public static <T, V> void eqIfPresent(List<Predicate> predicates,
                                          CriteriaBuilder cb, Root<T> root,
                                          SingularAttribute<? super T, V> field, V value) {
        if (value != null) {
            predicates.add(cb.equal(root.get(field), value));
        }
    }

    public static <T> void likeIfPresent(List<Predicate> predicates,
                                            CriteriaBuilder cb, Root<T> root,
                                            SingularAttribute<? super T, String> field, String value) {
        if(StrUtil.isNotBlank(value)) {
            predicates.add(cb.like(root.get(field), "%" + value + "%"));
        }
    }

    public static <T, V extends Comparable<? super V>> void gt(List<Predicate> predicates,
                                                               CriteriaBuilder cb, Root<T> root,
                                                               SingularAttribute<? super T, V> field, V value) {
        predicates.add(cb.greaterThan(root.get(field), value));
    }

    public static <T, V extends Comparable<? super V>> void betweenIfPresent(List<Predicate> predicates,
                                               CriteriaBuilder cb, Root<T> root,
                                               SingularAttribute<? super T, V> field, V[] value) {
        if(ArrayUtil.isNotEmpty(value) && value.length > 1) {
            predicates.add(cb.between(root.get(field), value[0], value[1]));
        }
    }

}
