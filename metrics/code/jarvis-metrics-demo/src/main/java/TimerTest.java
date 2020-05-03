import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;
import io.prometheus.client.exporter.PushGateway;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TimerTest {

    public static Random random = new Random();

    private final static String prometheusHost = "127.0.0.1:9091";


    public static void main(String[] args) throws InterruptedException {
        final MetricRegistry registry = new MetricRegistry();
        ConsoleReporter reporter = ConsoleReporter.forRegistry(registry).build();
        reporter.start(1, TimeUnit.SECONDS);

        //dropwizard promethus client 接收信息



        Timer timer = registry.timer(MetricRegistry.name(TimerTest.class, "get-latency"));

        final DropwizardExports dropwizardExports = new DropwizardExports(registry);
        final CollectorRegistry collectorRegistry = new CollectorRegistry();
        final PushGateway pushGateway = new PushGateway(prometheusHost);
        dropwizardExports.register(collectorRegistry);
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
            public void run() {
                dropwizardExports.collect();
                try {
                    pushGateway.push(collectorRegistry, "test-prometheus@pengfeining-mac");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
        Timer.Context ctx;

        while (true) {
            ctx = timer.time();
            Thread.sleep(random.nextInt(1000));
            ctx.stop();
        }
    }
}