package com.campushub.common.entity;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Import;

import com.campushub.config.JpaConfig;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
@EntityScan(basePackageClasses = BaseEntityTest.class)
class BaseEntityTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldPopulateAuditTimestampsOnCreate() {
        TestDummyEntity entity = new TestDummyEntity("Item 1");

        TestDummyEntity savedEntity = entityManager.persistAndFlush(entity);

        assertThat(savedEntity.getId()).isNotNull();
        assertThat(savedEntity.getCreatedAt()).isNotNull();
        assertThat(savedEntity.getUpdatedAt()).isNotNull();
        assertThat(savedEntity.getCreatedAt()).isEqualTo(savedEntity.getUpdatedAt());
    }

    @Test
    void shouldUpdateOnlyUpdatedAtOnModification() throws InterruptedException {
        TestDummyEntity entity = entityManager.persistAndFlush(new TestDummyEntity("Item 1"));
        LocalDateTime initialCreatedAt = entity.getCreatedAt();
        LocalDateTime initialUpdatedAt = entity.getUpdatedAt();

        Thread.sleep(50);

        entity.setName("Updated Item 1");
        TestDummyEntity updatedEntity = entityManager.persistAndFlush(entity);

        assertThat(updatedEntity.getCreatedAt()).isEqualTo(initialCreatedAt);
        assertThat(updatedEntity.getUpdatedAt()).isAfter(initialUpdatedAt);
    }

    @Entity
    @Table(name = "test_dummy_entity")
    static class TestDummyEntity extends BaseEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String name;

        public TestDummyEntity() {
        }

        public TestDummyEntity(String name) {
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}

