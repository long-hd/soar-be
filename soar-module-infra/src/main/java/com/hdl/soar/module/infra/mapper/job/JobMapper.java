package com.hdl.soar.module.infra.mapper.job;

import com.hdl.soar.module.infra.controller.admin.job.dto.job.JobRespDTO;
import com.hdl.soar.module.infra.controller.admin.job.dto.job.JobSaveReqDTO;
import com.hdl.soar.module.infra.dal.entity.job.JobPO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface JobMapper {

    JobMapper INSTANCE = Mappers.getMapper(JobMapper.class);

    @Mapping(target = "status", ignore = true) // set by the service, not by the caller
    JobPO toPO(JobSaveReqDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)     // status changes go through updateJobStatus
    @Mapping(target = "handlerName", ignore = true) // identity: renaming would orphan the Quartz job
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatePO(JobSaveReqDTO dto, @MappingTarget JobPO po);

    JobRespDTO toDTO(JobPO po);
    List<JobRespDTO> toDTOList(List<JobPO> list);

}
