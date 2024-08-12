package eolopark.server.rabbit;

import eolopark.server.controller.LogController;
import eolopark.server.model.dto.EoloparkResponse;
import eolopark.server.model.internal.*;
import eolopark.server.service.NotificationService;
import eolopark.server.service.ParkService;
import eolopark.server.service.ReportService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class ServerRabbitMQConsumer {

    /* Attributes */
    private final LogController logController;
    private final ReportService reportService;
    private final NotificationService notificationService;
    private final ParkService parkService;
    @Value ("${controller.verbosity}")
    private int verbosity;

    /* Constructor */
    public ServerRabbitMQConsumer (LogController logController, ReportService reportService,
                                   NotificationService notificationService, ParkService parkService) {
        this.logController = logController;
        this.reportService = reportService;
        this.notificationService = notificationService;
        this.parkService = parkService;
    }

    @RabbitListener (queues = "eoloparkCreationProgressNotifications", ackMode = "AUTO")
    public void consumeEoloparkCreationRequest (final CreationProgressMessage message) throws RuntimeException {
        if (verbosity > 0) logController.info("Received eolopark creation progress message: " + message);
        Report report = reportService.replaceReportByQueueMessage(message.id(), message.progress(),
                message.completed());
        notificationService.notifyReportUpdate(report);
        if (message.completed()) {
            EoloparkResponse e = message.eoloparkResponse();
            EoloparkAndAerogenerators eoloparkPair = new EoloparkAndAerogenerators(new Eolopark(e.name(), e.city(),
                    e.latitude(), e.longitude(), e.area(), e.terrainType(), new Substation(e.substation().model(),
                    e.substation().power(), e.substation().voltage())),
                    new ArrayList<>(e.aerogenerators().stream().map(a -> new Aerogenerator(a.aerogeneratorId(),
                            a.aerogeneratorLatitude(), a.aerogeneratorLongitude(), a.bladeLength(), a.height(),
                            a.aerogeneratorPower())).toList()));
            try {
                parkService.savePark(eoloparkPair.eolopark(), eoloparkPair.aerogenerators(), report.getUsername());
            } catch (Exception ex) {
                logController.error("Error saving eolopark and aerogenerators to database: " + ex.getMessage());
            }
        }
    }
}
