package com.madr.madr_app_backend;

import org.springframework.boot.SpringApplication;

public class TestMadrAppBackendApplication {

	public static void main(String[] args) {
//		SpringApplication.from(MadrAppBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
		SpringApplication.from(MadrAppBackendApplication::main).run(args);
	}

}
