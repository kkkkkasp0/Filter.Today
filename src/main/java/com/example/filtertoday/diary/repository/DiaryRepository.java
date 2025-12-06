package com.example.filtertoday.diary.repository;

import com.example.filtertoday.diary.entity.Diary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
    //특정인의 특정기간 일기
    List<Diary> findByMemberIdAndRecordDateBetween(Long memberId, LocalDate startDate, LocalDate endDate);
}
