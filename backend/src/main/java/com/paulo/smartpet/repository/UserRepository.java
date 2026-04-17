package com.paulo.smartpet.repository;

import com.paulo.smartpet.entity.User;
import com.paulo.smartpet.entity.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

    @Query("""
            select u
            from User u
            where (:active is null or u.active = :active)
              and (:role is null or u.role = :role)
              and (
                    :search is null
                    or lower(u.name) like lower(concat('%', :search, '%'))
                    or lower(u.username) like lower(concat('%', :search, '%'))
                  )
            """)
    Page<User> findPageByFilters(
            Boolean active,
            UserRole role,
            String search,
            Pageable pageable
    );
}