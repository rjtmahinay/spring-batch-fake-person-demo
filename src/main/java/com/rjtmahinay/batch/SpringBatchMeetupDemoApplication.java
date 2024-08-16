package com.rjtmahinay.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Main entry point of a Spring Batch Application.
 *
 * <ul>
 *     <li>{@link SpringBootApplication} abstracts the configuration to manage
 *     Java objects and its dependencies. This annotation manage the objects in the Spring Container.</li>
 *     <li>{@link Slf4j} abstracts the logging configuration. This application will default in using Logback
 *     logging.</li>
 *     <li>{@link RequiredArgsConstructor} generates and inject automatically the fields with final specifier inside
 *     a constructor.</li>
 * </ul>
 */
@SpringBootApplication
@Slf4j
@RequiredArgsConstructor
public class SpringBatchMeetupDemoApplication {

    private final JobLauncher jobLauncher;
    private final Job personDataJob;

    public static void main(String[] args) {
      SpringApplication.exit(SpringApplication.run(SpringBatchMeetupDemoApplication.class, args));
    }

    /**
     * This is an abstraction of a Command Line Interface.
     *
     * This method will be executed after the Spring Container is initialized.
     * It will launch the job with the specified parameters.
     *
     * @return An instance of {@link CommandLineRunner} including the job parameters and execution needed to execute
     * a {@link Job}.
     */
    @Bean
    CommandLineRunner commandLineRunner() {
        return args -> {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution jobExecution = jobLauncher.run(personDataJob, jobParameters);

            log.info("Job Exit Code: {}", jobExecution.getExitStatus().getExitCode());
        };
    }

}
