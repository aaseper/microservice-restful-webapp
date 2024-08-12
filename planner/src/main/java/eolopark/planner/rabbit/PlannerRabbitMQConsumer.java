package eolopark.planner.rabbit;

import eolopark.planner.controller.LogController;
import eolopark.planner.model.EoloparkRequestMessage;
import eolopark.planner.service.AutomaticParkDataService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PlannerRabbitMQConsumer {

    /* Attributes */
    private final LogController logController;
    private final AutomaticParkDataService automaticParkDataService;
    @Value("${controller.verbosity}")
    private int verbosity;

    /* Constructor */
    public PlannerRabbitMQConsumer(LogController logController, AutomaticParkDataService automaticParkDataService) {
        this.logController = logController;
        this.automaticParkDataService = automaticParkDataService;
    }

    /* Methods */
    @RabbitListener(queues = "eoloparkCreationRequests", ackMode = "AUTO")
    public void consumeEoloparkCreationRequest(final EoloparkRequestMessage message) throws Exception {
        if (verbosity > 0) logController.info("Received eolopark creation request: " + message);

        // Simulate the creation of the eolopark
        automaticParkDataService.automateEoloparkResponse(message.id(), message.city(), message.size());
    }
}
