package eolopark.geoservice.service;

import eolopark.geoservice.model.GeoData;
import jakarta.annotation.PostConstruct;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.NoSuchElementException;

@Service
public class GeoDataService {

    private final String GEO_DATA_PATH = "/dataset.csv";
    private ArrayList<GeoData> geoDataList = new ArrayList<>();

    /* Constructors */
    public GeoDataService() {
    }

    /* Methods */
    // This method is called after the object is created and all dependencies are injected
    // It initializes the city geolocation data
    @PostConstruct
    public void init() throws IOException {
        readCities();
    }

    private void readCities() throws IOException {
        Resource resource = new ClassPathResource(GEO_DATA_PATH);
        Reader in = new InputStreamReader(resource.getInputStream());

        CSVFormat csvFormat =
                CSVFormat.DEFAULT.builder().setHeader(GEO_DATA_HEADERS.class).setDelimiter(';').setSkipHeaderRecord(true).build();

        Iterable<CSVRecord> records = csvFormat.parse(in);

        for (CSVRecord record : records) {
            geoDataList.add(new GeoData(record.get(GEO_DATA_HEADERS.capital),
                    Double.parseDouble(record.get(GEO_DATA_HEADERS.latETRS89)),
                    Double.parseDouble(record.get(GEO_DATA_HEADERS.lonETRS89)),
                    Integer.parseInt(record.get(GEO_DATA_HEADERS.altitude))));
        }

        in.close();
    }

    public GeoData getGeoDataByCityName(String cityName) throws NoSuchElementException {
        for (GeoData geoData : geoDataList) {
            if (geoData.capital().equals(cityName)) {
                return geoData;
            }
        }
        throw new NoSuchElementException("City not found");
    }

    /* Attributes */
    enum GEO_DATA_HEADERS {capital, latETRS89, lonETRS89, altitude}
}