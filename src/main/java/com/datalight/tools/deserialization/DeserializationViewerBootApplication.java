package com.datalight.tools.deserialization;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;


@SpringBootApplication
@ServletComponentScan
@EnableCaching
public class DeserializationViewerBootApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(DeserializationViewerBootApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		System.out.println("项目已启动...");
	}

}
