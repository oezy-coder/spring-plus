package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface TodoRepository extends JpaRepository<Todo, Long>, TodoCustomRepository {

    @Query("SELECT t FROM Todo t LEFT JOIN FETCH t.user u ORDER BY t.modifiedAt DESC")
    Page<Todo> findAllByOrderByModifiedAtDesc(Pageable pageable);

    @Query("SELECT t FROM Todo t WHERE t.weather = :weather AND t.modifiedAt between :startDate and :endDate")
    Page<Todo> findByWeatherAndDateRange(
            @Param("weather") String weather,
            @Param("startDate") LocalDateTime searchStartDate,
            @Param("endDate") LocalDateTime searchEndDate,
            Pageable pageable);

    @Query("SELECT t FROM Todo t WHERE t.weather = :weather")
    Page<Todo> findByWeather(@Param("weather") String weather, Pageable pageable);

    @Query("SELECT t FROM Todo t WHERE t.modifiedAt between :startDate and :endDate")
    Page<Todo> findByDateRange(
            @Param("startDate") LocalDateTime searchStartDate,
            @Param("endDate") LocalDateTime searchEndDate,
            Pageable pageable);
}
