package com.sikcourse.backend.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * BaseTimeEntity의 @CreatedDate / @LastModifiedDate를 동작시킨다.
 * 메인 클래스에 직접 붙이지 않고 별도 설정 클래스로 분리한 이유는,
 * @WebMvcTest 같은 슬라이스 테스트에서 JPA Auditing이 불필요하게
 * 로딩되는 것을 막기 위함이다.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
