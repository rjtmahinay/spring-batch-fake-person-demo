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

@SpringBootApplication
@Slf4j
@RequiredArgsConstructor
public class SpringBatchMeetupDemoApplication {

    private final JobLauncher jobLauncher;
    private final Job personDataJob;

    public static void main(String[] args) {
      SpringApplication.exit(SpringApplication.run(SpringBatchMeetupDemoApplication.class, args));
    }

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
