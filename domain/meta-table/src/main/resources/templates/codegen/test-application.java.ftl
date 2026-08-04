package ${packageBase};

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(
        basePackages = "${packageBase}",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = ${packageBase}.rest.${entityName}Controller.class))
public class GeneratedTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(GeneratedTestApplication.class, args);
    }
}
