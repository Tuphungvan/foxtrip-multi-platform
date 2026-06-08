package haui.foxtrip.user.repository;

import haui.foxtrip.common.dto.PageData;
import haui.foxtrip.user.domain.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import haui.foxtrip.user.domain.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepository extends JpaRepository<User, UUID> {

       Optional<User> findByEmailAndDeletedAtIsNull(String email);

       Optional<User> findByGoogleId(String googleId);

       boolean existsByEmail(String email);

       boolean existsByPhoneNumber(String phoneNumber);

       Optional<User> findByEmail(String email);

       List<User> findByRoleAndDeletedAtIsNull(Role role);

       @Query("SELECT u FROM User u WHERE u.role IN :roles " +
                     "AND (:keyword IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
       Page<User> searchUsersByRolesAndEmail(@Param("roles") List<Role> roles, @Param("keyword") String keyword, Pageable pageable);
}
