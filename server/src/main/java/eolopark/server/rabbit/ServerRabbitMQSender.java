package eolopark.server.rabbit;

import eolopark.server.controller.LogController;
import eolopark.server.model.internal.EoloparkRequestMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ServerRabbitMQSender {

    /* Attributes */
    private final LogController logController;
    private final RabbitTemplate rabbitTemplate;
    @Value ("${controller.verbosity}")
    private int verbosity;

    /* Constructor */
    public ServerRabbitMQSender (LogController logController, RabbitTemplate rabbitTemplate) {
        this.logController = logController;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendCreationProgressMessage (EoloparkRequestMessage message) {
        rabbitTemplate.convertAndSend("eoloparkCreationRequests", message);
        if (verbosity > 0) logController.info("Sent eolopark creation request: " + message);
    }
}
