package eolopark.server.service;

import eolopark.server.controller.exception.HttpClientErrorException;
import eolopark.server.model.internal.Aerogenerator;
import eolopark.server.model.internal.Eolopark;
import eolopark.server.model.internal.Substation;
import eolopark.server.model.internal.User;
import eolopark.server.repository.AerogeneratorRepository;
import eolopark.server.repository.EoloparkRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ParkService {

    /* Attributes */
    private final UserService userService;
    private final EoloparkRepository eoloparkRepository;
    private final AerogeneratorRepository aerogeneratorRepository;

    public ParkService (UserService userService, EoloparkRepository eoloparkRepository,
                        AerogeneratorRepository aerogeneratorRepository) {
        this.userService = userService;
        this.eoloparkRepository = eoloparkRepository;
        this.aerogeneratorRepository = aerogeneratorRepository;
    }

    /* Constructors */
    public void reviewMaxParks (String username) {
        User user = userService.getUserByName(username);
        if (user.getEoloparks().size() >= user.getParkMax())
            throw new HttpClientErrorException("You have reached the maximum allowed number of resources (5). Please "
                    + "try again later.");
    }

    /* Methods */
    public Eolopark getParkById (long id) {
        return eoloparkRepository.findById(id).orElseThrow(NoSuchElementException::new);
    }

    public void savePark (Eolopark eolopark, List<Aerogenerator> aerogenerators, String username) {
        reviewMaxParks(username);
        User user = userService.getUserByName(username);
        eolopark.setUser(user);
        eoloparkRepository.save(eolopark);
        for (Aerogenerator aerogenerator : aerogenerators) {
            aerogenerator.setEolopark(eolopark);
            aerogeneratorRepository.save(aerogenerator);
        }

    }

    public void replaceParkById (long id, Eolopark eolopark, Substation substation, boolean keep) {
        eoloparkRepository.updateEoloparkById(eolopark.getName(), eolopark.getCity(), eolopark.getLatitude(),
                eolopark.getLongitude(), eolopark.getArea(), eolopark.getTerrainType(), id);
        eoloparkRepository.updateSubstationById(substation.getModel(), substation.getPower(), substation.getVoltage()
                , id);
        if (!keep) aerogeneratorRepository.deleteAerogeneratorByEoloparkId(id);
    }

    public void deleteParkById (long id) {
        eoloparkRepository.deleteById(id);
    }

    public Page<Eolopark> getParkByKeyword (String keyword, Pageable page) {
        return eoloparkRepository.findEoloparkByKeyword(keyword, page);
    }

    public Page<Eolopark> getParkByUserIdAndKeyword (String keyword, String name, Pageable page) {
        User user = userService.getUserByName(name);
        return eoloparkRepository.findEoloparkByUserIdAndKeyword(keyword, user.getId(), page);
    }

    public Page<Eolopark> getParkByUserId (Long id, Pageable page) {
        return eoloparkRepository.findEoloparkByUserId(id, page);
    }

    public Eolopark getParkByName (String name) {
        return eoloparkRepository.findEoloparkByName(name).orElseThrow(NoSuchElementException::new);
    }

    public Long getMaxEoloparkId () {
        return eoloparkRepository.findMaxEoloparkId().orElseThrow(NoSuchElementException::new);
    }
}