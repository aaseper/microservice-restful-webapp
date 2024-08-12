package eolopark.server.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

@Controller
public class LogController {

    /* Attributes */
    private final Logger logger = LoggerFactory.getLogger(LogController.class);

    /* Constructor */
    public LogController () {
    }

    /* Methods */
    public void info (String message) {
        logger.info(message);
    }

    public void error (String message) {
        logger.error(message);
    }

    public void warn (String message) {
        logger.warn(message);
    }

    public void debug (String message) {
        logger.debug(message);
    }

    public void trace (String message) {
        logger.trace(message);
    }
}