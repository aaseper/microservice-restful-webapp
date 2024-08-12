package eolopark.server.service;

import eolopark.server.model.internal.Aerogenerator;
import eolopark.server.model.internal.Eolopark;
import eolopark.server.repository.AerogeneratorRepository;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class AerogeneratorService {

    /* Attributes */
    private final AerogeneratorRepository aerogeneratorRepository;

    /* Constructors */
    public AerogeneratorService (AerogeneratorRepository aerogeneratorRepository) {
        this.aerogeneratorRepository = aerogeneratorRepository;
    }

    /* Methods */
    public Aerogenerator getAerogeneratorById (long id) {
        return aerogeneratorRepository.findById(id).orElseThrow();
    }

    public void saveAerogenerator (Aerogenerator aerogenerator, Eolopark eolopark) {
        if (aerogeneratorRepository.findAerogeneratorNamesByEoloparkId(eolopark.getId()).contains(aerogenerator.getAerogeneratorId()))
            throw new IllegalArgumentException("Aerogenerator with id " + aerogenerator.getAerogeneratorId() + " " +
                    "already exists in the park");

        aerogenerator.setEolopark(eolopark);
        aerogeneratorRepository.save(aerogenerator);
    }

    public void deleteAerogeneratorById (long id) {
        aerogeneratorRepository.deleteById(id);
    }

    public void replaceAerogeneratorById (Aerogenerator aerogenerator, Long id, Long eoloparkId) {
        if (aerogeneratorRepository.findAerogeneratorNamesByEoloparkId(eoloparkId).contains(aerogenerator.getAerogeneratorId()))
            throw new IllegalArgumentException("Aerogenerator with id " + aerogenerator.getAerogeneratorId() + " " +
                    "already exists in the park");

        aerogeneratorRepository.updateAerogeneratorById(aerogenerator.getAerogeneratorId(),
                aerogenerator.getAerogeneratorLatitude(), aerogenerator.getAerogeneratorLongitude(),
                aerogenerator.getBladeLength(), aerogenerator.getHeight(), aerogenerator.getAerogeneratorPower(), id);
    }

    public Aerogenerator getAerogeneratorByName (String aerogeneratorId) {
        return aerogeneratorRepository.findAerogeneratorByName(aerogeneratorId).orElseThrow(NoSuchElementException::new);
    }
}
