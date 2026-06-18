package com.hdl.soar.module.system.dal.entity.dict;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.framework.jpa.core.entity.BasePO;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "system_dict_data")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted = false")
public class DictDataPO extends BasePO {

    /**
     * Dictionary data ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Dictionary sort order
     */
    @Column(name = "sort")
    @Builder.Default
    private Integer sort = 0;

    /**
     * Dictionary label
     */
    @Column(name = "label")
    private String label;

    /**
     * Dictionary value
     */
    @Column(name = "value")
    private String value;

    /**
     * Dictionary type
     *
     * <p>Redundant field {@link DictDataPO#getDictType()}
     */
    @Column(name = "dict_type")
    private String dictType;

    /**
     * Status
     *
     * <p> Enum {@link CommonStatusEnum}
     */
    @Column(name = "status")
    @Builder.Default
    private CommonStatusEnum status = CommonStatusEnum.ENABLE;

    /**
     * Color type
     *
     * <p>Corresponds to element-ui types: default, primary, success, info, warning, danger
     */
    @Column(name = "color_type")
    private String colorType;

    /**
     * CSS class
     */
    @Column(name = "css_class")
    private String cssClass;

    /**
     * Remark
     */
    @Column(name = "remark")
    private String remark;

}
