package com.hdl.soar.module.infra.mapper.job;

import com.hdl.soar.module.infra.controller.admin.job.dto.log.JobLogRespDTO;
import com.hdl.soar.module.infra.dal.entity.job.JobLogPO;
import com.hdl.soar.module.infra.mapper.InfraEnumMapper;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(uses = {InfraEnumMapper.class})
public interface JobLogMapper {

    JobLogMapper INSTANCE = Mappers.getMapper(JobLogMapper.class);

    JobLogRespDTO toDTO(JobLogPO po);

    List<JobLogRespDTO> toDTOList(List<JobLogPO> list);

}
