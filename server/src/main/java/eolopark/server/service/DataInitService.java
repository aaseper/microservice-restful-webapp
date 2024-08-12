package eolopark.server.service;

import eolopark.server.controller.LogController;
import eolopark.server.model.internal.*;
import eolopark.server.repository.AerogeneratorRepository;
import eolopark.server.repository.EoloparkRepository;
import eolopark.server.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class DataInitService {

    /* Attributes */
    private static final double KM_TO_DEGREES = 0.5 / 111;
    private static final double POWER_MARGIN = 1.20;
    private final Map<String, int[]> aerogeneratorSizes = Map.of("LOW", new int[]{10, 25, 1500}, "MEDIUM",
            new int[]{20, 50, 2500}, "HIGH", new int[]{40, 100, 3500});
    private final String CITIES_PATH = "/cities.csv";
    private final String PARKS_PATH = "/parks.csv";
    private final ArrayList<String> cityNames = new ArrayList<>();
    private final ArrayList<City> cityList = new ArrayList<>();
    private final CopyOnWriteArrayList<String> parkNames = new CopyOnWriteArrayList<>();
    private final LogController logController;
    private final UserRepository userRepository;
    private final EoloparkRepository eoloparkRepository;
    private final AerogeneratorRepository aerogeneratorRepository;
    private final PasswordEncoder passwordEncoder;
    private final long seed;
    private final Random rand;
    @Value ("${security.user}")
    private String username;
    @Value ("${security.encodedPassword}")
    private String encodedPassword;

    /* Constructors */
    public DataInitService (LogController logController, UserRepository userRepository,
                            EoloparkRepository eoloparkRepository, AerogeneratorRepository aerogeneratorRepository,
                            PasswordEncoder passwordEncoder) {
        this.logController = logController;
        this.userRepository = userRepository;
        this.eoloparkRepository = eoloparkRepository;
        this.aerogeneratorRepository = aerogeneratorRepository;
        this.passwordEncoder = passwordEncoder;
        this.seed = System.currentTimeMillis();
        this.rand = new Random(seed);
        logController.warn("Random seed: " + seed);
    }

    /* Methods */
    // This method is called after the object is created and all dependencies are injected
    // It initializes the city names and park names, and adds the cities to the city list
    // It also adds some random data to the database
    @PostConstruct
    public void init () throws IOException {
        readCities();
        readParks();

        // If there is an admin in the server then that the database is already initialized,
        // thus the method will not add or overwrite any data. Otherwise, it will add the data
        if (!userRepository.findUserByName("admin").isPresent()) {
            User admin = new User(username, encodedPassword, Integer.MAX_VALUE, "admin");
            userRepository.save(admin);
            User user = new User("user", passwordEncoder.encode("user"), 5, "user");
            userRepository.save(user);
            User premium = new User("premium", passwordEncoder.encode("premium"), Integer.MAX_VALUE, "user", "premium");
            userRepository.save(premium);

            userRepository.save(new User("alex", passwordEncoder.encode("alex"), 5, "user"));
            userRepository.save(new User("david", passwordEncoder.encode("david"), 5, "user"));
            userRepository.save(new User("jose", passwordEncoder.encode("jose"), 5, "user"));

            for (int i = 0; i < 10; i++) {
                addEolopark(admin, createRandomEoloparkAndAerogenerators());
            }

            for (int i = 0; i < 5; i++) {
                addEolopark(user, createRandomEoloparkAndAerogenerators());
            }

            for (int i = 0; i < 6; i++) {
                addEolopark(premium, createRandomEoloparkAndAerogenerators());
            }
        }
    }

    public void addEolopark (User user, EoloparkAndAerogenerators eoloparkPair) {
        eoloparkPair.eolopark().setUser(user);
        eoloparkRepository.save(eoloparkPair.eolopark());
        for (Aerogenerator aerogenerator : eoloparkPair.aerogenerators()) {
            aerogenerator.setEolopark(eoloparkPair.eolopark());
            aerogeneratorRepository.save(aerogenerator);
        }
    }

    /* Dataset read */
    // This method reads the csv file with the cities and adds them to the city list and city name list
    private void readCities () throws IOException {
        Resource resource = new ClassPathResource(CITIES_PATH);
        Reader in = new InputStreamReader(resource.getInputStream());

        CSVFormat csvFormat =
                CSVFormat.DEFAULT.builder().setHeader(CITY_HEADERS.class).setDelimiter(';').setSkipHeaderRecord(true).build();

        Iterable<CSVRecord> records = csvFormat.parse(in);

        for (CSVRecord record : records) {
            cityNames.add(record.get(CITY_HEADERS.capital));
            cityList.add(new City(record.get(CITY_HEADERS.capital),
                    Double.parseDouble(record.get(CITY_HEADERS.latETRS89)),
                    Double.parseDouble(record.get(CITY_HEADERS.lonETRS89)), record.get(CITY_HEADERS.altitude),
                    record.get(CITY_HEADERS.meanWind)));
        }

        in.close();
    }

    // This method reads the csv file with the park names and adds them to the park name list
    private void readParks () throws IOException {
        Resource resource = new ClassPathResource(PARKS_PATH);
        Reader in = new InputStreamReader(resource.getInputStream());

        CSVFormat csvFormat =
                CSVFormat.DEFAULT.builder().setHeader("name").setDelimiter(';').setSkipHeaderRecord(true).build();

        Iterable<CSVRecord> records = csvFormat.parse(in);

        for (CSVRecord record : records)
            parkNames.add(record.get("name"));

        in.close();
    }

    public String getCityName (int idx) {
        return cityNames.get(idx);
    }

    public City getCityByCapital (String name) {
        for (City c : cityList)
            if (c.getCapital().equals(name)) return c;
        return null;
    }

    public String getParkName () {
        String name = parkNames.getFirst();
        parkNames.remove(name);
        parkNames.add("New " + name);
        return name;
    }

    public int getCitySize () {return cityNames.size();}

    /* Algorithm to generate random data */
    private String getSubstationModel (double power) {
        if (power <= 10000) return "A-10000";
        else if (power <= 50000) return "B-" + ((int) ((power / 10000) + 1) * 10000);
        else return "C-" + ((int) ((power / 100000) + 1) * 100000);
    }

    // This method creates a new eolopark with the given city and area
    public EoloparkAndAerogenerators createAutomatedEoloparkAndAerogenerators (String city, int area) {
        // City search, if not exists return empty object
        City c = getCityByCapital(city);
        if (c != null) {
            // Park attributes
            String name = getParkName();
            double latitude = c.getLatETRS89();
            double longitude = c.getLonETRS89();
            String terrainType = c.getAltitude();

            // Aerogenerator creation
            List<Aerogenerator> aerogenerators = new ArrayList<>();
            int[] sizes = aerogeneratorSizes.get(c.getMeanWind());
            for (int i = 0; i < area; i++) {
                aerogenerators.add(new Aerogenerator(name.replaceAll("\\s+", "").concat(String.valueOf(i)),
                        latitude - KM_TO_DEGREES, longitude + KM_TO_DEGREES + (i * 2 * KM_TO_DEGREES), sizes[0],
                        sizes[1], sizes[2]));
            }

            // Substation creation
            double totalPower = sizes[2] * area * POWER_MARGIN;
            Substation substation = new Substation(getSubstationModel(totalPower), totalPower, 220);

            // Park creation and return
            return new EoloparkAndAerogenerators(new Eolopark(name, city, latitude, longitude, area, terrainType,
                    substation), aerogenerators);
        }
        else throw new IllegalArgumentException("City not found");
    }

    // This method creates a new random eolopark with via a random number
    public EoloparkAndAerogenerators createRandomEoloparkAndAerogenerators () {
        String city = getCityName((int) (nextRandom() * getCitySize()));
        int area = (int) (nextRandom() * 10) + 1;
        return createAutomatedEoloparkAndAerogenerators(city, area);
    }

    public double nextRandom () {return rand.nextDouble();}

    /* Attributes */
    enum CITY_HEADERS {capital, latETRS89, lonETRS89, altitude, meanWind}
}