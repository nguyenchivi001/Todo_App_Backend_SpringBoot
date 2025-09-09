package com.todoapp.task.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TaskStatisticsResponse {

    private Long totalTasks;

    private Long completedTasks;

    private Long pendingTasks;

    private Long overdueTasks;

    private Long highPriorityTasks;

    private Long mediumPriorityTasks;

    private Long lowPriorityTasks;

    private Double completionRate;

    private Long tasksThisWeek;

    private Long tasksToday;
}