package com.hdl.soar.framework.jpa.core.util;

import com.hdl.soar.framework.common.pojo.PageParam;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.framework.common.pojo.SortablePageParam;
import com.hdl.soar.framework.common.pojo.SortingField;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

/**
 * Utility for converting between Soar pagination types and Spring Data pagination types.
 *
 * <p>Usage:
 * <pre>{@code
 * // In service layer:
 * Pageable pageable = PageUtils.toPageable(pageParam);
 * Page<UserDO> page = userRepository.findAll(spec, pageable);
 * return PageUtils.toPageResult(page);
 * }</pre>
 */
public class PageUtils {
    private PageUtils() {}

    /**
     * Convert {@link PageParam} to Spring Data {@link Pageable}.
     *
     * <p>{@link PageParam#getPageNo()} is 1-based (user-facing),
     * while {@link Pageable} is 0-based, so we subtract 1.
     */
    public static Pageable toPageable(PageParam pageParam) {
        return PageRequest.of(pageParam.getPageNo() - 1, pageParam.getPageSize());
    }

    /**
     * Convert {@link SortablePageParam} to Spring Data {@link Pageable} with sorting.
     */
    public static Pageable toPageable(SortablePageParam pageParam) {
        Sort sort = toSort(pageParam.getSortingFields());
        return PageRequest.of(pageParam.getPageNo() - 1, pageParam.getPageSize(), sort);
    }

    /**
     * Convert {@link PageParam} to Spring Data {@link Pageable} with explicit sort.
     */
    public static Pageable toPageable(PageParam pageParam, Sort sort) {
        if(pageParam.getPageSize().equals(PageParam.PAGE_SIZE_NONE)) {
            return Pageable.unpaged();
        }
        return PageRequest.of(pageParam.getPageNo() - 1, pageParam.getPageSize(), sort);
    }

    /**
     * Convert Spring Data {@link Page} to Soar {@link PageResult}.
     */
    public static <T> PageResult<T> toPageResult(Page<T> page) {
        return new PageResult<>(page.getContent(), page.getTotalElements());
    }

    /**
     * Convert {@link SortingField} list to Spring Data {@link Sort}.
     * Returns {@link Sort#unsorted()} if the list is null or empty.
     */
    private static Sort toSort(List<SortingField> sortingFields) {
        if (sortingFields == null || sortingFields.isEmpty()) {
            return Sort.unsorted();
        }
        List<Sort.Order> orders = sortingFields.stream()
                .map(f -> new Sort.Order(
                        SortingField.ORDER_ASC.equals(f.getOrder()) ? Sort.Direction.ASC : Sort.Direction.DESC,
                        f.getField()))
                .toList();
        return Sort.by(orders);
    }

}
