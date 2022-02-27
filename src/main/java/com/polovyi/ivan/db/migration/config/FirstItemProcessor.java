package com.polovyi.ivan.db.migration.config;

import com.polovyi.ivan.db.migration.entity.mysql.StudentMySql;
import com.polovyi.ivan.db.migration.entity.postgres.StudentPostgres;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FirstItemProcessor implements ItemProcessor<StudentPostgres, StudentMySql> {

    @Override
    public StudentMySql process(StudentPostgres item) throws Exception {

       log.info("student id = ", item.getId());

        return StudentMySql.builder()
                .id(item.getId())
                .firstName(item.getFirstName())
                .lastName(item.getLastName())
                .email(item.getEmail())
                .deptId(item.getDeptId())
                .isActive(item.getIsActive() != null ? Boolean.valueOf(item.getIsActive()) : false).build();

    }

}
