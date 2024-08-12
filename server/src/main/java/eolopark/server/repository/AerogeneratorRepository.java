package eolopark.server.repository;

import eolopark.server.model.internal.Aerogenerator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
public interface AerogeneratorRepository extends JpaRepository<Aerogenerator, Long> {

    @Modifying
    @Query (value = "DELETE FROM aerogenerator WHERE eolopark_id = ?1", nativeQuery = true)
    void deleteAerogeneratorByEoloparkId (long id);

    @Modifying
    @Query (value = "UPDATE aerogenerator SET aerogenerator_id = ?1, aerogenerator_latitude = ?2, " +
            "aerogenerator_longitude = ?3, " + "blade_length = ?4, height = ?5, aerogenerator_power = ?6 WHERE id = " + "?7", nativeQuery = true)
    void updateAerogeneratorById (String aerogeneratorId, double aerogeneratorLatitude, double aerogeneratorLongitude
            , int bladeLength, int height, int aerogeneratorPower, Long id);

    @Query (value = "SELECT aerogenerator_id FROM aerogenerator WHERE eolopark_id = ?1", nativeQuery = true)
    List<String> findAerogeneratorNamesByEoloparkId (Long eoloparkId);

    @Query ("SELECT a FROM aerogenerator a WHERE a.aerogeneratorId = ?1")
    Optional<Aerogenerator> findAerogeneratorByName (String name);
}
