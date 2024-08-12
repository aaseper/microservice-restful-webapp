package eolopark.planner.rabbit;

import eolopark.planner.controller.LogController;
import eolopark.planner.model.CreationProgressMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PlannerRabbitMQSender {

    /* Attributes */
    private final LogController logController;
    private final RabbitTemplate rabbitTemplate;
    @Value("${controller.verbosity}")
    private int verbosity;

    /* Constructor */
    public PlannerRabbitMQSender(LogController logController, RabbitTemplate rabbitTemplate) {
        this.logController = logController;
        this.rabbitTemplate = rabbitTemplate;
    }

    /* Methods */
    public void sendCreationProgressMessage(CreationProgressMessage message) {
        rabbitTemplate.convertAndSend("eoloparkCreationProgressNotifications", message);
        if (verbosity > 0) logController.info("Sent eolopark creation progress message: " + message);
    }
}
