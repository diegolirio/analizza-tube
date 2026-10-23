package br.com.mytube;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MyTubeApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyTubeApiApplication.class, args);
	}

}
