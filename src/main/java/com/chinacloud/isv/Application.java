package com.chinacloud.isv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication 
public class Application {

	
	

	
	public static void main(String[] args) {
		System.out.println("--------spring boot begin---------");
		SpringApplication.run(Application.class, args);
		System.out.println("--------spring boot end---------");
	}

}
