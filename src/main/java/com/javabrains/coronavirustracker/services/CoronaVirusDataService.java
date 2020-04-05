package com.javabrains.coronavirustracker.services;

import com.javabrains.coronavirustracker.models.LocationStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class CoronaVirusDataService<globalnumber> {
    private static String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/who_covid_19_situation_reports/who_covid_19_sit_rep_time_series/who_covid_19_sit_rep_time_series.csv";
    private List<LocationStats> allStats = new ArrayList<>();

    public List<LocationStats> getAllStats() {
        return allStats;
    }
    private int globalnumber;
    private int prevglobalnumber;

    public int getGlobalnumber() {
        return globalnumber;
    }

    public int getPrevglobalnumber() {
        return prevglobalnumber;
    }

    @PostConstruct
    @Scheduled(cron = "* * 1 * * *")
    public void fetchVirusData() throws IOException, InterruptedException {
        System.out.println("Application in the method");
        List<LocationStats> newStats = new ArrayList<>();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                                             .uri(URI.create(VIRUS_DATA_URL))
                                             .build();
         HttpResponse<String> response  =    client.send(request, HttpResponse.BodyHandlers.ofString());
        StringReader csvBodyReader = new StringReader(response.body());
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);
        for (CSVRecord record : records) {
            LocationStats locationStats = new LocationStats();
            locationStats.setState(record.get("Province/States"));
            locationStats.setCountry(record.get("Country/Region"));
//            System.out.println(Integer.parseInt(record.get(record.size()-1)));
            int latestCases=0;
            int prevDayCases=0;
            if(!record.get(record.size()-1).equals(""))
            { latestCases = Integer.parseInt(record.get(record.size()-1));}
            if(!record.get(record.size()-2).equals(""))
            {prevDayCases = Integer.parseInt(record.get(record.size()-2));}
//            int prevDayCases = Integer.parseInt(record.get(record.size()-2));
            locationStats.setLatestTotalCases(latestCases);
            if(latestCases!=0)
            {locationStats.setDiffFromPrevDay(latestCases-prevDayCases);}
            if(locationStats.getCountry().equals("Globally")&&(locationStats.getState().equals("Confirmed")))
            {
                globalnumber=locationStats.getLatestTotalCases();
                prevglobalnumber = locationStats.getDiffFromPrevDay();
            }
            boolean onlyCountries = locationStats.getState().equals("Confirmed")||locationStats.getState().equals("Deaths");
            if(locationStats.getLatestTotalCases()!=0&&!onlyCountries)
                newStats.add(locationStats);
        }
        this.allStats = newStats;
    }
}
