package com.saeparam.HeyRoutine;

import com.saeparam.HeyRoutine.domain.shop.entity.PointShop;
import com.saeparam.HeyRoutine.domain.shop.repository.PointShopRepository;
import com.saeparam.HeyRoutine.domain.shop.service.PointShopService;
import com.saeparam.HeyRoutine.domain.user.entity.User;
import com.saeparam.HeyRoutine.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PointShopServiceConcurrencyTest {

    @Autowired
    private PointShopService pointShopService;

    @Autowired
    private PointShopRepository pointShopRepository;

    @Autowired
    private UserRepository userRepository;

    private PointShop testProduct;
    private List<User> testUsers = new ArrayList<>();

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        userRepository.deleteAllInBatch();
        pointShopRepository.deleteAllInBatch();

        // 1. 테스트용 상품 생성 (재고 100개, 가격 10포인트)
        testProduct = PointShop.builder()
                .productName("테스트 상품")
                .price(10L)
                .stock(100L)
                .brand("테스트 브랜드")
                .imageUrl("test.jpg")
                .build();
        pointShopRepository.saveAndFlush(testProduct);

        // 2. 테스트용 사용자 200명 생성 (각자 10000 포인트 소유)
        for (int i = 0; i < 200; i++) {
            User user = User.builder()
                    .email("testuser" + i + "@test.com")
                    .nickname("testuser" + i)
                    .point(10000L)
                    .password("password")
                    .build();
            testUsers.add(user);
        }
        userRepository.saveAllAndFlush(testUsers);
    }

    @AfterEach
    void tearDown() {
        // 테스트 데이터 정리
        userRepository.deleteAllInBatch();
        pointShopRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("동시에 200명이 재고가 100개인 상품을 구매하면, 정확히 100명만 성공하고 재고는 0이 되어야 한다.")
    void buyProduct_concurrency_test() throws InterruptedException {
        // given
        int threadCount = 200;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // --- 결과 카운팅을 위한 AtomicInteger 추가 ---
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            final int userIndex = i;
            executorService.submit(() -> {
                try {
                    String lockName = "product_lock:" + testProduct.getId();
                    String userEmail = testUsers.get(userIndex).getEmail();

                    pointShopService.buyProduct(lockName, UUID.fromString(userEmail), testProduct.getId());
                    successCount.incrementAndGet(); // 성공 카운트 증가
                    System.out.println("구매 성공");
                } catch (Exception e) {
                    failCount.incrementAndGet(); // 실패 카운트 증가
                    System.out.println("구매 실패 (예상된 예외): " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        PointShop finalProduct = pointShopRepository.findById(testProduct.getId()).orElseThrow();

        // --- 최종 결과 출력 ---
        System.out.println("==========================================");
        System.out.println("           테스트 최종 결과");
        System.out.println("==========================================");
        System.out.println("총 시도 횟수: " + threadCount);
        System.out.println("구매 성공: " + successCount.get() + "건");
        System.out.println("구매 실패: " + failCount.get() + "건");
        System.out.println("최종 상품 재고: " + finalProduct.getStock() + "개");
        System.out.println("==========================================");

        // 최종 재고가 0인지 확인
        assertThat(finalProduct.getStock()).isEqualTo(0L);
        // 성공한 요청이 100건인지 확인
        assertThat(successCount.get()).isEqualTo(100);
    }
}
