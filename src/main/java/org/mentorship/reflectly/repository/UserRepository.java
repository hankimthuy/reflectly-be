package org.mentorship.reflectly.repository;

import org.mentorship.reflectly.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
}