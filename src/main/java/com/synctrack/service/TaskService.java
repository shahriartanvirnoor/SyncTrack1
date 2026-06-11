package com.synctrack.service;

import com.synctrack.model.Task;
import com.synctrack.repository.TaskRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TaskService {
    private TaskRepository taskRepository;
    
    public TaskService() {
        this.taskRepository = new TaskRepository();
    }
    
    public Task createTask(Task task) {
        return taskRepository.create(task);
    }
    
    public Task updateTask(Task task) {
        taskRepository.update(task);
        return task;
    }
    
    public void deleteTask(int taskId) {
        taskRepository.delete(taskId);
    }
    
    public List<Task> getTasksByUser(int userId) {
        return taskRepository.findByUserId(userId);
    }
    
    public Task completeTask(int taskId) {
        taskRepository.completeTask(taskId, 0);
        return taskRepository.findByUserId(taskId).stream().findFirst().orElse(null);
    }
    
    public Map<String, Integer> getTaskCountByCategory(int userId) {
        return taskRepository.findByUserId(userId).stream()
            .collect(Collectors.groupingBy(Task::getCategory, Collectors.summingInt(t -> 1)));
    }
}
