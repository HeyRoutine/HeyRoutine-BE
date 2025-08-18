//package com.saeparam.HeyRoutine.domain.routine.entity;
//
//
//import com.saeparam.HeyRoutine.domain.user.entity.User;
//import com.saeparam.HeyRoutine.global.common.util.BaseTime;
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//
//@Entity
//@Getter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class PersonalRoutineList extends BaseTime {
//
//    //집 나무 사람2개
//    @Column(name = "personal_routine_list_id", updatable = false, unique = true, nullable = false)
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private long id;
//
//    @JoinColumn(name="user_id")
//    @ManyToOne(fetch = FetchType.LAZY)
//    private User user;
//
//    @Column
//    private String title;
//
//    @Column
//    private day;
//
//
//
//}
