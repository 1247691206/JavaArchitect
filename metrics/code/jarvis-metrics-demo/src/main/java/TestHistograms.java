import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;
import io.prometheus.client.exporter.PushGateway;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;

public class TestHistograms {
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
     * 实例化一个Histograms
     */
    private static final Histogram randomNums = metrics.histogram(name(TestHistograms.class, "random"));

    public static void handleRequest(double random) {
        randomNums.update((int) (random*100));
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

        Random rand = new Random();
        while(true){
            handleRequest(rand.nextDouble());
            Thread.sleep(100);
        }
    }
}
