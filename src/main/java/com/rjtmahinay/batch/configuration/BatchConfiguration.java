/**
 * MIT License
 * Copyright (c) 2024 Tristan Mahinay
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * <p>
 * spring-batch-meetup-demo
 *
 * @author rjtmahinay
 * 2024
 */
package com.rjtmahinay.batch.configuration;

import com.rjtmahinay.batch.listener.ChunkListener;
import com.rjtmahinay.batch.listener.JobListener;
import com.rjtmahinay.batch.listener.StepListener;
import com.rjtmahinay.batch.model.Person;
import com.rjtmahinay.batch.properties.PersonBatchProperties;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.concurrent.Executors;

/**
 * Configures the Batch Job and its components. Methods with {@link Bean} are injected in the Spring Container.
 */
@Configuration
public class BatchConfiguration {

    /**
     * Concrete implementation of {@link org.springframework.batch.item.ItemReader}.
     *
     * This reads a flat file and maps the data to a {@link Person} object.
     * @param personBatchProperties Contains properties from application.yml file
     * @return configured instance of {@link FlatFileItemReader}
     */
    @Bean
    FlatFileItemReader<Person> personFlatFileItemReader(PersonBatchProperties personBatchProperties) {
        final String[] dataNames = {"firstName", "lastName", "city", "buildingNumber", "streetName"};

        return new FlatFileItemReaderBuilder<Person>()
                .name("personFlatFileItemReader")
                .targetType(Person.class)
                .resource(new FileSystemResource(personBatchProperties.getDataFilePath()))
                .delimited()
                .delimiter(",")
                .names(dataNames)
                .build();

    }

    /**
     * Concrete implementation of {@link org.springframework.batch.item.ItemWriter}.
     *
     * Writes data to the database.
     *
     * @param dataSource Connection to the database
     * @return configured instance of {@link JdbcBatchItemWriter}
     */
    @Bean
    JdbcBatchItemWriter<Person> personJdbcBatchItemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Person>()
                .dataSource(dataSource)
                .beanMapped()
                .sql("INSERT INTO PERSON (FIRST_NAME, LAST_NAME, CITY, BLDG_NUMBER, STREET_NAME) VALUES (:firstName, :lastName, :city, :buildingNumber, :streetName)")
                .build();
    }

    /**
     * Spawns a virtual thread for each task.
     * @return configured instance of {@link AsyncTaskExecutor}
     */
    @Bean
    AsyncTaskExecutor asyncTaskExecutor() {
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }

    /**
     * A sequential step to perform batch operations using the Person Data. This is responsible in invoking
     * the reading and writing from flat file to the database.
     *
     * This step is a chunk-based operation, meaning it reads and processes data in chunks.
     *
     * @param jobRepository Stores the Job metadata
     * @param platformTransactionManager Handles the transaction management of job repository and the database
     * @param personFlatFileItemReader Reads data from the flat file
     * @param personJdbcBatchItemWriter Writes data to the database
     * @param asyncTaskExecutor Spawns a virtual thread for each task
     * @param stepListener Listens to the step events and logs them
     * @param chunkListener Listens to the chunk events and logs them
     * @param personBatchProperties Contains properties from application.yml file
     *
     * @return configured instance of {@link Step} which will be passed to {@link Job} instance
     */
    @Bean
    Step personDataStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager,
                        FlatFileItemReader<Person> personFlatFileItemReader,
                        JdbcBatchItemWriter<Person> personJdbcBatchItemWriter,
                        AsyncTaskExecutor asyncTaskExecutor,
                        StepListener stepListener,
                        ChunkListener chunkListener,
                        PersonBatchProperties personBatchProperties) {
        return new StepBuilder("personDataStep", jobRepository)
                .<Person, Person>chunk(personBatchProperties.getChunkSize(), platformTransactionManager)
                .taskExecutor(asyncTaskExecutor)
                .reader(personFlatFileItemReader)
                .writer(personJdbcBatchItemWriter)
                .listener(chunkListener)
                .listener(stepListener)
                .build();
    }

    /**
     * Abstracts a single job. This is responsible in invoking a single or multiple steps.
     *
     * @param jobRepository Stores the Job metadata
     * @param jobListener Listens to the job events and logs them
     *
     * @return configured instance of {@link Job} which will be passed to the Spring Container. This will be invoked
     * in {@link com.rjtmahinay.batch.SpringBatchMeetupDemoApplication}
     */
    @Bean
    Job personDataJob(JobRepository jobRepository, Step personDataStep, JobListener jobListener) {
        return new JobBuilder("personDataJob", jobRepository)
                .start(personDataStep)
                .listener(jobListener)
                .incrementer(new RunIdIncrementer())
                .build();
    }
}
