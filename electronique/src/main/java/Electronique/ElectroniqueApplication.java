package Electronique;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("com.commerce.repository")
@EntityScan("com.commerce.model")
@ComponentScan(basePackages = {"Electronique", "com.commerce"}) // AJOUTEZ CETTE LIGNE
public class ElectroniqueApplication {
    public static void main(String[] args) {
        SpringApplication.run(ElectroniqueApplication.class, args);
    }
}
