// File: repository/TaskRepository.java
package com.example.multiagent.repository;

import com.example.multiagent.task.Task;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends MongoRepository<Task, String> {
  // Custom query methods can be added as needed.
}
