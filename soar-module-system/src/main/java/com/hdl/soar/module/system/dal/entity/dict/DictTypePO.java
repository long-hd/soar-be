package com.hdl.soar.module.system.dal.entity.dict;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.framework.jpa.core.entity.BasePO;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Entity
@Table(name = "system_dict_type")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted = false")
public class DictTypePO extends BasePO {

    /**
     * Dictionary primary key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Dictionary name
     */
    @Column(name = "name")
    private String name;

    /**
     * Dictionary type
     */
    @Column(name = "type")
    private String type;

    /**
     * Status
     *
     * <p>Enum {@link CommonStatusEnum}
     */
    @Column(name = "status")
    @Builder.Default
    private CommonStatusEnum status = CommonStatusEnum.ENABLE;

    /**
     * Remark
     */
    @Column(name = "remark")
    private String remark;

}
