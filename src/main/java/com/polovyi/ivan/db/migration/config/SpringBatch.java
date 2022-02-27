package com.polovyi.ivan.db.migration.config;

import com.polovyi.ivan.db.migration.entity.mysql.StudentMySql;
import com.polovyi.ivan.db.migration.entity.postgres.StudentPostgres;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class SpringBatch {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private DataSource mysqlDataSource;

    @Autowired
    private DataSource postgresDataSource;

    @Qualifier("postgresEntityManagerFactory")
    @Autowired
    private EntityManagerFactory postgresEntityManagerFactory;

    @Qualifier("mysqlEntityManagerFactory")
    @Autowired
    private EntityManagerFactory mysqlEntityManagerFactory;

    @Autowired
    private JpaTransactionManager jpaTransactionManager;

    @Autowired
    private FirstItemProcessor firstItemProcessor;

    @StepScope
    @Bean
    public JpaCursorItemReader<StudentPostgres> jpaCursorItemReader(
            @Value("#{jobParameters['currentItemCount']}") Integer currentItemCount,
            @Value("#{jobParameters['maxItemCount']}") Integer maxItemCount) {
        JpaCursorItemReader<StudentPostgres> jpaCursorItemReader = new JpaCursorItemReader<>();
        jpaCursorItemReader.setEntityManagerFactory(postgresEntityManagerFactory);
        jpaCursorItemReader.setQueryString("From StudentPostgres");
        jpaCursorItemReader.setCurrentItemCount(currentItemCount);
        jpaCursorItemReader.setMaxItemCount(maxItemCount);
        return jpaCursorItemReader;
    }

    public JpaItemWriter<StudentMySql> jpaItemWriter() {
        JpaItemWriter<StudentMySql> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(mysqlEntityManagerFactory);
        return jpaItemWriter;
    }

    @Bean
    public Job chunkJob() {
        return jobBuilderFactory.get("Chunk Job")
                .incrementer(new RunIdIncrementer())
                .start(firstChunkStep())
                .build();
    }

    private Step firstChunkStep() {
        return stepBuilderFactory.get("First Chunk Step")
                .<StudentPostgres, StudentMySql>chunk(3)
                .reader(jpaCursorItemReader(null, null))
                .processor(firstItemProcessor)
                .writer(jpaItemWriter())
                .faultTolerant()
                .skip(Throwable.class)
                .skipLimit(100)
                .retryLimit(3)
                .retry(Throwable.class)
                .transactionManager(jpaTransactionManager)
                .build();
    }

}
