package com.tproject.mappers;

import com.tproject.dto.TaskDto;
import com.tproject.entity.Task;
import org.mapstruct.Mapper;

import java.util.Collection;

@Mapper
public interface TaskMapper {
    TaskDto taskToDto(Task task);

    Task dtoToTask(TaskDto taskDto);

    Collection<TaskDto> taskToDtoCollection (Collection<Task> taskCollection);

}
