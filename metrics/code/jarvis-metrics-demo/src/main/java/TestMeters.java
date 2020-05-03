import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;
import io.prometheus.client.exporter.PushGateway;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;

public class TestMeters {
    /**
     * 实例化一个registry，最核心的一个模块，相当于一个应用程序的metrics系统的容器，维护一个Map
     */
    private static final MetricRegistry metrics = new MetricRegistry();

    private final static String prometheusHost = "127.0.0.1:9091";


    /**
     * 在控制台上打印输出
     */
    private static ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics).build();

    /**
     * 实例化一个Meter
     */
    private static final Meter requests = metrics.meter(name(TestMeters.class, "request"));

    public static void handleRequest() {
        requests.mark();
    }

    public static void main(String[] args) throws InterruptedException {
        reporter.start(3, TimeUnit.SECONDS);

        final DropwizardExports dropwizardExports = new DropwizardExports(metrics);
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

        while(true){
            handleRequest();
            Thread.sleep(100);
        }
    }

}
