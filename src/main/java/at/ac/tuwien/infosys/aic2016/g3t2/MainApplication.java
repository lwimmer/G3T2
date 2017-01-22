package at.ac.tuwien.infosys.aic2016.g3t2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@SpringBootApplication
@PropertySources({
        @PropertySource(ignoreResourceNotFound = true, value = "classpath:/credentials.properties"),
        @PropertySource(ignoreResourceNotFound = true, value = "file:credentials.properties")
    })

public class MainApplication {

	public static void main(String[] args) {
		SpringApplication.run(MainApplication.class, args);
	}
}
