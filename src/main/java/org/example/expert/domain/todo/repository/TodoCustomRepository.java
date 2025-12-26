package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.dto.response.TodoResponse;

public interface TodoCustomRepository {

    TodoResponse findByIdWithUser(long todoId);
}
