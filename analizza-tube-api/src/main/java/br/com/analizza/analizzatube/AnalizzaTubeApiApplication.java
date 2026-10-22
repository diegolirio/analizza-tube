package br.com.analizza.analizzatube;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AnalizzaTubeApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnalizzaTubeApiApplication.class, args);
	}

}
