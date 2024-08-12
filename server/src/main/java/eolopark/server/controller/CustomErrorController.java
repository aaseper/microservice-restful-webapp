package eolopark.server.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    /* Methods */
    @RequestMapping ("/error")
    public String handleError () {
        return "error";
    }

    @RequestMapping ("/400")
    public String handle400 () {
        return "400";
    }

    @RequestMapping ("/403")
    public String handle403 () {
        return "403";
    }

    @RequestMapping ("/404")
    public String handle404 () {
        return "404";
    }

    @RequestMapping ("/429")
    public String handle429 () {
        return "429";
    }
}
