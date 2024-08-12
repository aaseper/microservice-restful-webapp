package eolopark.server.controller;

import eolopark.server.model.internal.Aerogenerator;
import eolopark.server.model.internal.Eolopark;
import eolopark.server.model.internal.EoloparkRequestMessage;
import eolopark.server.model.internal.Substation;
import eolopark.server.rabbit.ServerRabbitMQSender;
import eolopark.server.service.DataInitService;
import eolopark.server.service.ParkService;
import eolopark.server.service.ReportService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Controller
public class ParkWebController {

    /* Attributes */
    private final LogController logController;
    private final ParkService parkService;
    private final ServerRabbitMQSender serverRabbitMQSender;
    private final ReportService reportService;
    private final DataInitService dataInitService;
    @Value ("${controller.verbosity}")
    private int verbosity;

    /* Constructor */
    public ParkWebController (LogController logController, ParkService parkService,
                              ServerRabbitMQSender serverRabbitMQSender, ReportService reportService,
                              DataInitService dataInitService
    ) {
        this.logController = logController;
        this.parkService = parkService;
        this.serverRabbitMQSender = serverRabbitMQSender;
        this.reportService = reportService;
        this.dataInitService = dataInitService;
    }

    /* Methods */
    @ModelAttribute
    public void addAttributes (Model model, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            model.addAttribute("logged", true);
            model.addAttribute("username", principal.getName());
            model.addAttribute("admin", request.isUserInRole("admin"));
        }
        else model.addAttribute("logged", false);
    }

    @GetMapping ("/")
    public String getAllPark (Model model, @RequestParam (required = false) Optional<String> keyword,
                              @RequestParam (required = false) Optional<String> searchType, @PageableDefault (size =
            5) Pageable page) {
        String keywordString = keyword.orElseGet(() -> "");
        String searchTypeString = searchType.orElseGet(() -> "ALL");
        Page<Eolopark> eoloparks = searchTypeString.equals("ALL") ?
                parkService.getParkByKeyword(keywordString, page) :
                parkService.getParkByUserIdAndKeyword(keywordString, Objects.requireNonNull(model.getAttribute(
                        "username")).toString(), page);
        model.addAttribute("parks", eoloparks);
        model.addAttribute("hasPrev", eoloparks.hasPrevious());
        model.addAttribute("hasNext", eoloparks.hasNext());
        model.addAttribute("nextPage", eoloparks.getNumber() + 1);
        model.addAttribute("prevPage", eoloparks.getNumber() - 1);
        model.addAttribute("lastPage", eoloparks.getTotalPages());
        model.addAttribute("keyword", keywordString);
        model.addAttribute("searchType", searchTypeString);
        model.addAttribute("searchByKeyword", !keywordString.equals(""));
        if (verbosity > 0) logController.info("Index page requested");
        return "index";
    }

    @PostMapping ("/")
    public String searchPark (Model model, String keyword, String searchType,
                              @PageableDefault (size = 5) Pageable page) {
        if (verbosity > 0) logController.info("Eolopark search by name requested");
        Page<Eolopark> eoloparks = searchType.equals("ALL") ? parkService.getParkByKeyword(keyword, page) :
                parkService.getParkByUserIdAndKeyword(keyword,
                        Objects.requireNonNull(model.getAttribute("username")).toString(), page);
        model.addAttribute("parks", eoloparks);
        model.addAttribute("hasPrev", eoloparks.hasPrevious());
        model.addAttribute("hasNext", eoloparks.hasNext());
        model.addAttribute("nextPage", eoloparks.getNumber() + 1);
        model.addAttribute("prevPage", eoloparks.getNumber() - 1);
        model.addAttribute("lastPage", eoloparks.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("searchType", searchType);
        model.addAttribute("searchByKeyword", true);
        return "index";
    }

    @GetMapping ("/parks/{id}")
    public String getPark (Model model, @PathVariable long id) {
        Eolopark park = parkService.getParkById(id);
        if (park.getUser().getName().equals(Objects.requireNonNull(model.getAttribute("username"))) || Objects.requireNonNull(model.getAttribute("admin")).equals(true)) {
            model.addAttribute("park", park);
            if (verbosity > 0) logController.info("Park with id " + id + " requested.");
            return "show_park";
        }
        else
            throw new InsufficientAuthenticationException("Unauthorized access to park with id " + id + " requested " + "for edition");
    }

    @GetMapping ("/parks/new")
    public String newPark (Model model) {
        if (verbosity > 0) logController.info("New park creation form requested");
        return "new_park";
    }

    @PostMapping ("/parks/new")
    public String newPark (Model model, Eolopark park, Substation substation, Aerogenerator aerogenerator) {
        if (aerogenerator.getAerogeneratorId().equals("0"))
            parkService.savePark(new Eolopark(park, substation), new ArrayList<>(),
                    Objects.requireNonNull(Objects.requireNonNull(model.getAttribute("username")).toString()));
        else
            parkService.savePark(new Eolopark(park, substation), List.of(aerogenerator),
                    Objects.requireNonNull(Objects.requireNonNull(model.getAttribute("username")).toString()));
        if (verbosity > 0) logController.info("New park successfully saved");
        return "saved_park";
    }

    // The following methods create a new automated park
    @GetMapping ("/parks/automate")
    public String newAutomatePark (Model model) {
        if (verbosity > 0) logController.info("New automated park creation form requested");
        return "new_automate_park";
    }

    // This method creates a new automate park
    @PostMapping ("/parks/automate")
    public String newAutomatePark (Model model, String city, int area) {
        if (verbosity > 0) logController.info("New automate park creation requested");
        parkService.reviewMaxParks(Objects.requireNonNull(model.getAttribute("username")).toString());
        if (dataInitService.getCityByCapital(city) == null) throw new IllegalArgumentException("City not found");
        Long id = reportService.saveReport(Objects.requireNonNull(Objects.requireNonNull(model.getAttribute("username"
        )).toString()));
        serverRabbitMQSender.sendCreationProgressMessage(new EoloparkRequestMessage(id, city, area));
        return "redirect:/report?reportId=" + id;
    }

    // This method creates a new random park (debugging)
    @PostMapping ("/parks/automate/random")
    public String newRandomPark (Model model) {
        if (verbosity > 0) logController.info("New random park creation requested");
        parkService.reviewMaxParks(Objects.requireNonNull(model.getAttribute("username")).toString());
        Long id = reportService.saveReport(Objects.requireNonNull(Objects.requireNonNull(model.getAttribute("username"
        )).toString()));
        serverRabbitMQSender.sendCreationProgressMessage(new EoloparkRequestMessage(id,
                dataInitService.getCityName((int) (dataInitService.nextRandom() * dataInitService.getCitySize())),
                (int) (dataInitService.nextRandom() * 10) + 1));
        return "redirect:/report?reportId=" + id;
    }

    @GetMapping ("/report")
    public String getReport (Model model, @RequestParam String reportId) {
        model.addAttribute("reportId", reportId);
        return "report";
    }

    @GetMapping ("/parks/{id}/edit")
    public String putPark (Model model, @PathVariable long id) {
        Eolopark park = parkService.getParkById(id);
        if (park.getUser().getName().equals(Objects.requireNonNull(model.getAttribute("username"))) || Objects.requireNonNull(model.getAttribute("admin")).equals(true)) {
            model.addAttribute("park", park);
            model.addAttribute("substation", park.getSubstation());
            if (verbosity > 0) logController.info("Park with id " + id + " requested for edition");
            return "edit_park";
        }
        else
            throw new InsufficientAuthenticationException("Unauthorized access to park with id " + id + " requested " + "for edition");
    }

    @PostMapping ("/parks/{id}/edit")
    public String putPark (Model model, Eolopark park, Substation substation, String keep, @PathVariable long id) {
        if (parkService.getParkById(id).getUser().getName().equals(Objects.requireNonNull(model.getAttribute(
                "username"))) || Objects.requireNonNull(model.getAttribute("admin")).equals(true)) {
            model.addAttribute("park", park);
            parkService.replaceParkById(id, park, substation, keep.equals("yes"));
            if (verbosity > 0) logController.info("Park with id " + id + " edited");
            return "edited_park";
        }
        else
            throw new InsufficientAuthenticationException("Unauthorized access to park with id " + id + " requested " + "for edition");
    }

    @GetMapping ("/parks/{id}/delete")
    public String deletePark (Model model, @PathVariable long id) {
        if (parkService.getParkById(id).getUser().getName().equals(Objects.requireNonNull(model.getAttribute(
                "username"))) || Objects.requireNonNull(model.getAttribute("admin")).equals(true)) {
            parkService.deleteParkById(id);
            if (verbosity > 0) logController.info("Park with id " + id + " deleted");
            return "deleted_park";
        }
        else
            throw new InsufficientAuthenticationException("Unauthorized access to park with id " + id + " requested " + "for edition");
    }
}