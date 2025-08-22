package com.saeparam.HeyRoutine.domain.shop.entity;


import com.saeparam.HeyRoutine.domain.shop.enums.PointShopCategory;
import com.saeparam.HeyRoutine.global.common.util.BaseTime;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointShop extends BaseTime {

    //집 나무 사람2개
    @Column(name = "point_shop_id", updatable = false, unique = true, nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String brand;
    @Column(nullable = false)
    private String productName;
    @Column(nullable = false)
    private Long price;
    @Column
    private Long stock;
    @Column
    private PointShopCategory category;
    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    public void minusStock() {
        this.stock = --stock;
    }
}
