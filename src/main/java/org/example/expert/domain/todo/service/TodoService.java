package org.example.expert.domain.todo.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;
    private final WeatherClient weatherClient;

    @Transactional
    public TodoSaveResponse saveTodo(AuthUser authUser, TodoSaveRequest todoSaveRequest) {
        User user = User.fromAuthUser(authUser);

        String weather = weatherClient.getTodayWeather();

        Todo newTodo = new Todo(
                todoSaveRequest.getTitle(),
                todoSaveRequest.getContents(),
                weather,
                user
        );
        Todo savedTodo = todoRepository.save(newTodo);

        return new TodoSaveResponse(
                savedTodo.getId(),
                savedTodo.getTitle(),
                savedTodo.getContents(),
                weather,
                new UserResponse(user.getId(), user.getEmail())
        );
    }

    @Transactional(readOnly = true)
    public Page<TodoResponse> getTodos(int page, int size, String weather, LocalDate startDate, LocalDate endDate) {
        Pageable pageable = PageRequest.of(page - 1, size);

        // 기본은 조건 없이 전체 목록 조회 (최신 수정일 기준)
        Page<Todo> todos = todoRepository.findAllByOrderByModifiedAtDesc(pageable);

        boolean hasWeather = weather != null && !weather.isBlank();
        boolean hasDateRange = startDate != null && endDate != null && !startDate.isAfter(endDate);

        if (hasDateRange) {
            // 날짜만 받은 경우, 하루 전체 범위로 조회하기 위해 시간 범위로 변환
            LocalDateTime searchStartDate = startDate.atTime(LocalTime.MIN);
            LocalDateTime searchEndDate = endDate.atTime(LocalTime.MAX);

            // 날짜 조건이 있을 때, weather 조건이 있으면 함께 적용
            if (hasWeather) {
                todos = todoRepository.findByWeatherAndDateRange(weather,searchStartDate, searchEndDate, pageable);
            } else {
                todos = todoRepository.findByDateRange(searchStartDate, searchEndDate, pageable);
            }
        } else if (hasWeather) {
            // weather 조건만 있을 때
                todos = todoRepository.findByWeather(weather, pageable);
        }

        return todos.map(todo -> new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                todo.getWeather(),
                new UserResponse(todo.getUser().getId(), todo.getUser().getEmail()),
                todo.getCreatedAt(),
                todo.getModifiedAt()
        ));
    }

    @Transactional(readOnly = true)
    public TodoResponse getTodo(long todoId) {
        TodoResponse todoResponse = todoRepository.findByIdWithUser(todoId);

        return todoResponse;
    }

    @Transactional(readOnly = true)
    public List<TodoSearchResponse> searchTodos(
            int page,
            int size,
            String keyword,
            LocalDate from,
            LocalDate to,
            String nickname) {

        // 시작일이 종료일보다 늦은 경우 방지
        if (from != null && to != null && from.isAfter(to)) {
            throw new InvalidRequestException("시작일은 종료일보다 클 수 없습니다.");
        }

        // 날짜 검색을 위해 LocalDate를 LocalDateTime 범위로 변환
        LocalDateTime startDate = from != null ? from.atStartOfDay() : null;
        LocalDateTime endDate = to != null ? to.plusDays(1).atStartOfDay() : null;

        List<TodoSearchResponse> todoSearchResponseList = todoRepository.searchTodos(
                page,
                size,
                keyword,
                startDate,
                endDate,
                nickname
        );

        return todoSearchResponseList;
    }
}
