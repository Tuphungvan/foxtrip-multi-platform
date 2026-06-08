package haui.foxtrip.order.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${foxtrip.rabbitmq.exchange}")
    private String exchange;

    @Value("${foxtrip.rabbitmq.queues.payment-success}")
    private String paymentSuccessQueue;

    @Value("${foxtrip.rabbitmq.queues.tour-terminated}")
    private String tourTerminatedQueue;

    @Value("${foxtrip.rabbitmq.routing-keys.payment-success}")
    private String paymentSuccessRoutingKey;

    @Value("${foxtrip.rabbitmq.routing-keys.tour-terminated}")
    private String tourTerminatedRoutingKey;

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(exchange);
    }

    @Bean
    public Queue paymentSuccessQueue() {
        return QueueBuilder.durable(paymentSuccessQueue)
                .withArgument("x-dead-letter-exchange", exchange)
                .withArgument("x-dead-letter-routing-key", paymentSuccessRoutingKey + ".dlq")
                .build();
    }

    @Bean
    public Queue tourTerminatedQueue() {
        return QueueBuilder.durable(tourTerminatedQueue)
                .withArgument("x-dead-letter-exchange", exchange)
                .withArgument("x-dead-letter-routing-key", tourTerminatedRoutingKey + ".dlq")
                .build();
    }

    @Bean
    public Binding paymentSuccessBinding() {
        return BindingBuilder.bind(paymentSuccessQueue()).to(exchange()).with(paymentSuccessRoutingKey);
    }

    @Bean
    public Binding tourTerminatedBinding() {
        return BindingBuilder.bind(tourTerminatedQueue()).to(exchange()).with(tourTerminatedRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
