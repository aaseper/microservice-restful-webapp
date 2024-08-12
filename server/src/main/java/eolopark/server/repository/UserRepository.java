package eolopark.server.repository;

import eolopark.server.model.internal.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findUserByName (String name);

    @Modifying
    @Query ("UPDATE user SET parkMax = ?1 WHERE id = ?2")
    void updateUserParkMaxById (int parkMax, Long id);

    @Modifying
    @Query (value = "INSERT INTO user_roles VALUES (?2, ?1)", nativeQuery = true)
    void updateUserRolesById (String role, Long id);

    @Query (value = "SELECT * FROM user", nativeQuery = true)
    Page<User> findAllUser (Pageable page);
}