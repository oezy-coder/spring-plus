package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface TodoCustomRepository {

    TodoResponse findByIdWithUser(long todoId);

    List<TodoSearchResponse> searchTodos(int page,
                                         int size,
                                         String keyword,
                                         LocalDateTime from,
                                         LocalDateTime to,
                                         String nickname);
}
