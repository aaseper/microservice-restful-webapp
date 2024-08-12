package eolopark.server.repository;

import eolopark.server.model.internal.Eolopark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
public interface EoloparkRepository extends JpaRepository<Eolopark, Long> {

    @Query (value = "SELECT * FROM eolopark WHERE " + "name LIKE %?1% " + "OR city LIKE %?1%", nativeQuery = true)
    Page<Eolopark> findEoloparkByKeyword (String keyword, Pageable page);

    @Query (value = "SELECT * FROM eolopark WHERE " + "user_id = ?2 " + "AND (name LIKE %?1% " + "OR city LIKE %?1%)"
            , nativeQuery = true)
    Page<Eolopark> findEoloparkByUserIdAndKeyword (String keyword, Long id, Pageable page);

    @Query (value = "SELECT * FROM eolopark WHERE " + "user_id = ?1", nativeQuery = true)
    Page<Eolopark> findEoloparkByUserId (Long id, Pageable page);

    @Modifying
    @Query ("UPDATE eolopark SET name = ?1, city = ?2, latitude = ?3, longitude = ?4, area = ?5, terrainType = ?6 " + "WHERE id = ?7")
    void updateEoloparkById (String name, String city, double latitude, double longitude, int area,
                             String terrainType, long id);

    @Modifying
    @Query ("UPDATE substation SET model = ?1, power = ?2, voltage = ?3 WHERE id = ?4")
    void updateSubstationById (String model, double power, double voltage, long id);

    @Query ("SELECT e FROM eolopark e WHERE e.name = ?1")
    Optional<Eolopark> findEoloparkByName (String name);

    @Query ("SELECT max(e.id) FROM eolopark e")
    Optional<Long> findMaxEoloparkId ();
}