package org.example.expert.domain.todo.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.comment.entity.QComment;
import org.example.expert.domain.manager.entity.QManager;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.QTodo;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.QUser;

import java.time.LocalDateTime;

import java.util.List;

/**
 * Todo 조회/검색을 위한 QueryDSL 기반 Custom Repository 구현체
 * - 단건 조회 시 User 정보 포함
 * - 검색 시 제목/담당자/생성일 조건 + 총 댓글 수/담당자 수 집계
 */
@RequiredArgsConstructor
public class TodoCustomRepositoryImpl implements TodoCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;
    private final QTodo todo = QTodo.todo;
    private final QUser user = QUser.user;
    private final QComment comment = QComment.comment;
    private final QManager manager = QManager.manager;

    /**
     * Todo 단건 조회
     * - User 정보를 함께 조회하여 TodoResponse로 바로 매핑
     */
    @Override
    public TodoResponse findByIdWithUser(long todoId) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        TodoResponse.class,
                        todo.id,
                        todo.title,
                        todo.contents,
                        todo.weather,
                        Projections.constructor(
                                UserResponse.class,
                                user.id,
                                user.email),
                        todo.createdAt,
                        todo.modifiedAt)
                )
                .from(todo)
                .join(todo.user, user)
                .where(todo.id.eq(todoId))
                .fetchOne();
    }

    /**
     * Todo 검색
     * - 제목 키워드(부분 일치)
     * - 담당자 닉네임(부분 일치)
     * - 생성일 범위(from ~ to)
     * - 댓글 수 / 담당자 수 집계
     * - 생성일 최신순 정렬
     * - 페이징 처리(offset, limit)
     */
    @Override
    public List<TodoSearchResponse> searchTodos(
            int page,
            int size,
            String keyword,
            LocalDateTime from,
            LocalDateTime to,
            String nickname) {

        return jpaQueryFactory
                .select(Projections.constructor(
                        TodoSearchResponse.class,
                        todo.title,
                        comment.id.countDistinct(), // 댓글 수 (중복 방지)
                        manager.id.countDistinct()) // 담당자 수 (중복 방지)
                )
                .from(todo)
                .join(todo.user, user)
                .leftJoin(todo.comments, comment)
                .leftJoin(todo.managers, manager)
                .where(
                        keywordContains(keyword),
                        nicknameContains(nickname),
                        createdAtFrom(from),
                        createdAtTo(to)
                )
                .groupBy(todo.id, todo.title) // 댓글/담당자 수 집계(count)를 위해 groupBy 필요
                .orderBy(todo.createdAt.desc())
                .offset((long) (page - 1) * size) // page는 1부터 시작
                .limit(size)
                .fetch();

    }

    private BooleanExpression keywordContains(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return todo.title.contains(keyword);
    }

    private BooleanExpression nicknameContains(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            return null;
        }
        return user.nickname.contains(nickname);
    }

    private BooleanExpression createdAtFrom(LocalDateTime startDate) {
        if (startDate == null) {
            return null;
        }
        return todo.createdAt.goe(startDate);
    }

    private BooleanExpression createdAtTo(LocalDateTime endDate) {
        if (endDate == null) {
            return null;
        }
        return todo.createdAt.lt(endDate);
    }
}
