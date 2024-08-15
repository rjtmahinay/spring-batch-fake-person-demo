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

@Configuration
public class BatchConfiguration {

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
    @Bean
    JdbcBatchItemWriter<Person> personJdbcBatchItemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Person>()
                .dataSource(dataSource)
                .beanMapped()
                .sql("INSERT INTO PERSON (FIRST_NAME, LAST_NAME, CITY, BLDG_NUMBER, STREET_NAME) VALUES (:firstName, :lastName, :city, :buildingNumber, :streetName)")
                .build();
    }

    @Bean
    AsyncTaskExecutor asyncTaskExecutor() {
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }

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

    @Bean
    Job personDataJob(JobRepository jobRepository, Step personDataStep, JobListener jobListener) {
        return new JobBuilder("personDataJob", jobRepository)
                .start(personDataStep)
                .listener(jobListener)
                .incrementer(new RunIdIncrementer())
                .build();
    }
}
