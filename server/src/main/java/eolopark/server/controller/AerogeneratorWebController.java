package eolopark.server.controller;

import eolopark.server.model.internal.Aerogenerator;
import eolopark.server.model.internal.Eolopark;
import eolopark.server.service.AerogeneratorService;
import eolopark.server.service.ParkService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Objects;

@Controller
@RequestMapping ("/parks")
public class AerogeneratorWebController {

    /* Attributes */
    private final LogController logController;
    private final ParkService parkService;
    private final AerogeneratorService aerogeneratorService;
    @Value ("${controller.verbosity}")
    private int verbosity;

    /* Constructor */
    public AerogeneratorWebController (LogController logController, ParkService parkService,
                                       AerogeneratorService aerogeneratorService) {
        this.logController = logController;
        this.parkService = parkService;
        this.aerogeneratorService = aerogeneratorService;
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

    @GetMapping ("/{eoloparkId}/aerogenerators/new")
    public String newAerogenerator (Model model, @PathVariable Long eoloparkId) {
        Eolopark park = parkService.getParkById(eoloparkId);
        if (park.getUser().getName().equals(Objects.requireNonNull(model.getAttribute("username"))) || Objects.requireNonNull(model.getAttribute("admin")).equals(true)) {
            model.addAttribute("park", park);
            if (verbosity > 0)
                logController.info("Aerogenerator creation form for park with id " + eoloparkId + " requested.");
            return "new_aerogenerator";
        }
        else
            throw new InsufficientAuthenticationException("Unauthorized access to park with id " + eoloparkId + " " +
                    "requested for edition");
    }

    @PostMapping ("/{eoloparkId}/aerogenerators/new")
    public String newAerogenerator (Model model, Aerogenerator aerogenerator, @PathVariable Long eoloparkId) {
        Eolopark park = parkService.getParkById(eoloparkId);
        if (park.getUser().getName().equals(Objects.requireNonNull(model.getAttribute("username"))) || Objects.requireNonNull(model.getAttribute("admin")).equals(true)) {
            aerogeneratorService.saveAerogenerator(aerogenerator, park);
            model.addAttribute("park", park);
            if (verbosity > 0)
                logController.info("Aerogenerator with id " + aerogenerator.getAerogeneratorId() + " for park with " +
                        "id" + " " + eoloparkId + " created.");
            return "saved_aerogenerator";
        }
        else
            throw new InsufficientAuthenticationException("Unauthorized access to park with id " + eoloparkId + " " +
                    "requested for edition");
    }

    @GetMapping ("/{eoloparkId}/aerogenerators/{aerogeneratorId}/edit")
    public String putAerogenerator (Model model, @PathVariable Long eoloparkId, @PathVariable Long aerogeneratorId) {
        Eolopark park = parkService.getParkById(eoloparkId);
        if (park.getUser().getName().equals(Objects.requireNonNull(model.getAttribute("username"))) || Objects.requireNonNull(model.getAttribute("admin")).equals(true)) {
            Aerogenerator aerogenerator = aerogeneratorService.getAerogeneratorById(aerogeneratorId);
            model.addAttribute("aerogenerator", aerogenerator);
            model.addAttribute("park", park);
            if (verbosity > 0)
                logController.info("Edition form for aerogenerator with id " + aerogeneratorId + " requested.");
            return "edit_aerogenerator";
        }
        else
            throw new InsufficientAuthenticationException("Unauthorized access to park with id " + eoloparkId + " " +
                    "requested for edition");
    }

    @PostMapping ("/{eoloparkId}/aerogenerators/{aerogeneratorId}/edit")
    public String putAerogenerator (Model model, Aerogenerator aerogenerator, @PathVariable Long eoloparkId,
                                    @PathVariable Long aerogeneratorId) {
        Eolopark park = parkService.getParkById(eoloparkId);
        if (park.getUser().getName().equals(Objects.requireNonNull(model.getAttribute("username"))) || Objects.requireNonNull(model.getAttribute("admin")).equals(true)) {
            aerogeneratorService.replaceAerogeneratorById(aerogenerator, aerogeneratorId, eoloparkId);
            if (verbosity > 0) logController.info("Aerogenerator with id " + aerogeneratorId + " edited.");
            model.addAttribute("park", park);
            return "saved_aerogenerator";
        }
        else
            throw new InsufficientAuthenticationException("Unauthorized access to park with id " + eoloparkId + " " +
                    "requested for edition");
    }

    @GetMapping ("/{eoloparkId}/aerogenerators/{aerogeneratorId}/delete")
    public String deleteAerogenerator (Model model, @PathVariable Long eoloparkId, @PathVariable Long aerogeneratorId) {
        Eolopark park = parkService.getParkById(eoloparkId);
        if (park.getUser().getName().equals(Objects.requireNonNull(model.getAttribute("username"))) || Objects.requireNonNull(model.getAttribute("admin")).equals(true)) {
            model.addAttribute("park", park);
            aerogeneratorService.deleteAerogeneratorById(aerogeneratorId);
            if (verbosity > 0) logController.info("Aerogenerator with id " + aerogeneratorId + " deleted.");
            return "deleted_aerogenerator";
        }
        else
            throw new InsufficientAuthenticationException("Unauthorized access to park with id " + eoloparkId + " " +
                    "requested for edition");
    }
}
