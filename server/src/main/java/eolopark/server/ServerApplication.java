package eolopark.server;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication (exclude = {ErrorMvcAutoConfiguration.class})
@EnableScheduling
@EnableAsync
public class ServerApplication {

    public static void main (String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    /* Queues for RabbitMQ */
    @Bean
    public Queue eoloparkRequestsQueue () {
        return new Queue("eoloparkCreationRequests", false);
    }

    @Bean
    public Queue progressNotificationsQueue () {
        return new Queue("eoloparkCreationProgressNotifications", false);
    }


    /* Message converter for RabbitMQ messages to be JSON */
    @Bean
    public Jackson2JsonMessageConverter messageConverter () {
        // Define a custom ObjectMapper to fail on unknown properties
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return new Jackson2JsonMessageConverter(mapper);
    }

    /* Custom RabbitTemplate to use the JSON message converter */
    @Bean
    public RabbitTemplate rabbitTemplate (final ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}