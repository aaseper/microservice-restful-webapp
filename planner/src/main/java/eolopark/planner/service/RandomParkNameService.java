package eolopark.planner.service;

import jakarta.annotation.PostConstruct;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class RandomParkNameService {
    private final CopyOnWriteArrayList<String> parkNames = new CopyOnWriteArrayList<>();

    /* Methods */
    /*
     * This method is called after the object is created and all dependencies are injected
     * and initializes the park names list
     */
    @PostConstruct
    public void init() throws IOException {
        readParkNames();
    }

    /*
     * Reads the csv file with predetermined random park names and adds them to a park name list
     */
    private void readParkNames() throws IOException {
        String PARKS_PATH = "/dataset.csv";
        Resource resource = new ClassPathResource(PARKS_PATH);
        Reader in = new InputStreamReader(resource.getInputStream());

        CSVFormat csvFormat =
                CSVFormat.DEFAULT.builder().setHeader("name").setDelimiter(';').setSkipHeaderRecord(true).build();

        Iterable<CSVRecord> records = csvFormat.parse(in);

        for (CSVRecord record : records)
            parkNames.add(record.get("name"));

        in.close();
    }

    /*
     * Returns a park name from the list and adds a new one to the end of the list
     */
    public String getRandomParkName() {
        String name = parkNames.getFirst();
        parkNames.remove(name);
        // Add a new random park name to the list to prevent duplicates
        parkNames.add(name + new Random().nextInt(1000));
        return name;
    }
}