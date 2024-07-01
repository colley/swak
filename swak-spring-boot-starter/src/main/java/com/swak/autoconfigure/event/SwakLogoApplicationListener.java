
package com.swak.autoconfigure.event;


import com.swak.core.SwakConstants;
import com.swak.core.SwakVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.SpringVersion;
import org.springframework.core.annotation.Order;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.swak.core.SwakConstants.LINE_SEPARATOR;


/**
 * Dubbo Welcome Logo {@link ApplicationListener}
 *
 * @see ApplicationListener
 * @since 2.7.0
 */
@Order(Ordered.HIGHEST_PRECEDENCE +30)  // After LoggingApplicationListener#DEFAULT_ORDER
public class SwakLogoApplicationListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static AtomicBoolean processed = new AtomicBoolean(false);

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        // Skip if processed before, prevent duplicated execution in Hierarchical ApplicationContext
        if (processed.get()) {
            return;
        }
        String pid = event.getEnvironment().getProperty("pid");
        /**
         * Gets Logger After LoggingSystem configuration ready
         * @see LoggingApplicationListener
         */
        final Logger logger = LoggerFactory.getLogger(getClass());
        String bannerText = buildBannerText(pid);
        if (logger.isInfoEnabled()) {
            logger.info(bannerText);
        } else {
            System.out.print(bannerText);
        }
        // mark processed to be true
        processed.compareAndSet(false, true);
    }

    String buildBannerText(String pid) {
        StringBuilder bannerBuilder = new StringBuilder();
        String bootVersion = SpringBootVersion.getVersion();
        String springVersion = SpringVersion.getVersion();
        String jdkVersion = System.getProperty("java.version");
        bannerBuilder.append(LINE_SEPARATOR);
        bannerBuilder.append("-=-=-=-=-=-=-=-=-=-=  -=-=-=-=-=-=-=-=-=-=  -=-=-=-=-=-=-=-=-=-=  -=-=-=-=-=-=-=-=-=-=").append(LINE_SEPARATOR);
        bannerBuilder.append(" ______     __     __     ______     __  __ ").append(LINE_SEPARATOR);
        bannerBuilder.append("/\\  ___\\   /\\ \\  _ \\ \\   /\\  __ \\   /\\ \\/ /    " ).append(LINE_SEPARATOR);
        bannerBuilder.append("\\ \\___  \\  \\ \\ \\/ \".\\ \\  \\ \\  __ \\  \\ \\  _\"-.  ").append(LINE_SEPARATOR);
        bannerBuilder.append(" \\/\\_____\\  \\ \\__/\".~\\_\\  \\ \\_\\ \\_\\  \\ \\_\\ \\_\\ " ).append(LINE_SEPARATOR);
        bannerBuilder.append("  \\/_____/   \\/_/   \\/_/   \\/_/\\/_/   \\/_/\\/_/  by colley" ).append(LINE_SEPARATOR);
        bannerBuilder.append("================-.`___`-.__\\ \\___  /__.-'_.'_.-'================" ).append(LINE_SEPARATOR);
        bannerBuilder.append(LINE_SEPARATOR);
        bannerBuilder.append(" :: Java version ::").append("(V").append(jdkVersion).append(")" ).append(LINE_SEPARATOR);
        bannerBuilder.append(" :: Spring Boot ::").append("(V").append(bootVersion).append(")" ).append(LINE_SEPARATOR);
        bannerBuilder.append(" :: Spring ::").append("(V").append(springVersion).append(")" ).append(LINE_SEPARATOR);
        bannerBuilder.append(" :: Swak Framework (V").append(SwakVersion.getVersion(getClass(), SwakVersion.getVersion())).append(") : ")
                .append(SwakConstants.SWAK_BOOT_URL).append(LINE_SEPARATOR);
        bannerBuilder.append(" :: Doc ::").append(SwakConstants.SWAK_DOCS_URL).append(LINE_SEPARATOR);
        bannerBuilder.append(" :: Discuss group :: ").append(SwakConstants.DISCUSS_GROUP).append(LINE_SEPARATOR);
        bannerBuilder.append(" :: Pid :: ").append(pid).append(LINE_SEPARATOR);

                bannerBuilder.append( "-=-=-=-=-=-=-=-=-=-=  -=-=-=-=-=-=-=-=-=-=  -=-=-=-=-=-=-=-=-=-=  -=-=-=-=-=-=-=-=-=-=").append(LINE_SEPARATOR);;
        return bannerBuilder.toString();
    }

    public static void main(String[] args) {
        String bootVersion = SpringBootVersion.getVersion();
        String springVersion = SpringVersion.getVersion();
        String jdkVersion = System.getProperty("java.version");
        System.out.println(bootVersion+" " + springVersion + " "+ jdkVersion);
    }

}
