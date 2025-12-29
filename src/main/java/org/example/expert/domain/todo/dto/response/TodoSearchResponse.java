package org.example.expert.domain.todo.dto.response;

import lombok.Getter;

@Getter
public class TodoSearchResponse {

    private final String title;
    private final Long commentCount;
    private final Long managerCount;

    public TodoSearchResponse(String title, Long commentCount, Long managerCount) {
        this.title = title;
        this.commentCount = commentCount;
        this.managerCount = managerCount;
    }
}
