package com.moveinsync.mdm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MdmBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(MdmBackendApplication.class, args);
	}

}
