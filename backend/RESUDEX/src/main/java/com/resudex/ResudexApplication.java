package com.resudex;

import com.resudex.service.DatabaseService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class ResudexApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResudexApplication.class, args);
	}

	@Bean
	public CommandLineRunner dataLoader(DatabaseService db) {
		return args -> {
			// Seed technical jobs ONLY if the table is empty to prevent duplication on restart
			if (db.list_all_jobs(true).isEmpty()) {
				db.post_job("Senior Java Backend Engineer", "Build scalable microservices with Java 21, Spring Boot 3.2, and PostgreSQL. Experience with Kafka and Docker required.", "OPEN");
				db.post_job("Full Stack Developer (React/Node)", "Join our agile team building modern web apps. Tech: React, Node.js, TypeScript, and MongoDB. Design systems knowledge is a plus.", "OPEN");
				db.post_job("DevOps Architect", "Expert in cloud infrastructure (AWS/GCP), Kubernetes orchestration, and Terraform automation. Help us transition to a GitOps model.", "OPEN");
				db.post_job("Data Scientist / ML Engineer", "Develop predictive models using Python, Scikit-learn, and PyTorch. Expertise in NLP and Large Language Models (LLMs) highly preferred.", "OPEN");
				db.post_job("Cybersecurity Analyst", "Protect our infrastructure with penetration testing, threat modeling, and OWASP security audits. Experience with SOC and incident response required.", "OPEN");
				db.post_job("UI/UX Product Designer", "Create stunning, high-fidelity interfaces in Figma and translate them into CSS/XAML/Swing components. Focus on interactive micro-animations.", "OPEN");
				db.post_job("Mobile Lead (Flutter/Dart)", "Develop high-performance cross-platform mobile applications for iOS and Android. Tech: Dart, Flutter, Firebase, and Bloc.", "OPEN");
				db.post_job("Cloud Solutions Architect (Azure)", "Design enterprise-grade cloud solutions on Microsoft Azure. Focus on scalability, disaster recovery, and cost optimization.", "OPEN");
				db.post_job("Embedded Firmware Developer", "Write low-level C/C++ for IoT devices and real-time operating systems. Experience with ESP32, STM32, and I2C/SPI protocols needed.", "OPEN");
				db.post_job("Principal Frontend Engineer", "Optimize frontend performance for high-traffic sites. Expertise in Next.js, TailWind, and Three.js/WebGL for immersive 3D UIs.", "OPEN");
			}
		};
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**")
						.allowedOrigins("http://localhost:3000")
						.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
						.allowedHeaders("*")
						.allowCredentials(true);
			}
		};
	}
}
