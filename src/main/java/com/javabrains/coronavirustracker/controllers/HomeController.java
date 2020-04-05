package com.javabrains.coronavirustracker.controllers;

import com.javabrains.coronavirustracker.models.LocationStats;
import com.javabrains.coronavirustracker.services.CoronaVirusDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Stream;

@Controller
public class HomeController {

    @Autowired
    CoronaVirusDataService service;
    @GetMapping("/")
    public String home(Model model)
    {
        List<LocationStats> allStats = service.getAllStats();
//        int totalCases = allStats.stream().mapToInt(stat -> stat.getLatestTotalCases()).sum();
        int totalNewCases = allStats.stream().mapToInt(stat -> stat.getDiffFromPrevDay()).sum();
        int total = allStats.stream().findFirst().orElse(null).getLatestTotalCases();
//        System.out.println(total);
        LocationStats global = new LocationStats();
        global.setCountry("China");
        model.addAttribute("locationStats", allStats);
        model.addAttribute("totalReportedCases", service.getGlobalnumber());
        model.addAttribute("totalNewCases", service.getPrevglobalnumber());

        return "home";
    }
}
