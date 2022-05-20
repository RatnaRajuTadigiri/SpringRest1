package com.nt.config;

import javax.persistence.EntityManagerFactory;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.data.MongoItemWriter;
import org.springframework.batch.item.data.builder.MongoItemWriterBuilder;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.nt.model.OExamResult;
import com.nt.listener.JobMonitoringListener;
import com.nt.model.IExamResult;
import com.nt.processor.ExamResultItemProcessor;

@Configuration
@EnableBatchProcessing
public class BatchConfig {
	@Autowired
	private  JobBuilderFactory jbFactory;
	@Autowired
	private  StepBuilderFactory sbFactory;
	@Autowired
	private  JobMonitoringListener listener;
	
	@Autowired
    private  EntityManagerFactory emFactory;	
	@Autowired
	private ExamResultItemProcessor processor;
	
	
	  // reader obj
	@Bean
	public FlatFileItemReader<IExamResult> createReader(){
		return  new FlatFileItemReaderBuilder<IExamResult>()
				                  .name("csv-reader")
				                  .resource(new ClassPathResource("TopBrains.csv"))
				                  .delimited().delimiter(",")
				                  .names("id","dob","semester","percentage")
				                  .targetType(IExamResult.class)
				                  .build();
	}
	
	/*	//writer  object (Version1)
	 @Bean
		public  MongoItemWriter<ExamResult> createWriter(){
			 //create MongoItemWriter obj
			MongoItemWriter<ExamResult> writer=new MongoItemWriter<ExamResult>();
			// specify the collection name
			writer.setCollection("SuperBrains");
		    //specify the Template
			writer.setTemplate(template);
			//return Writer obj
			return writer;
		}
	*/	
	
	//writer  (version1)
	/*	@Bean
		public  JpaItemWriter<OExamResult> createWriter(){
			//create Object
			 JpaItemWriter<OExamResult> writer=new JpaItemWriter<OExamResult>();
			 //set EntityManagerFactory
			 writer.setEntityManagerFactory(emFactory);
			 return writer;
		}
	*/	
	
	@Bean
	public  JpaItemWriter<OExamResult> createWriter(){
	    return  new JpaItemWriterBuilder<OExamResult>()
	    		         .entityManagerFactory(emFactory)
	    		         .build();
	}

	@Bean(name="step1")
	public  Step createStep1() {
	    return  sbFactory.get("step1")
	    		      .<IExamResult,OExamResult>chunk(5)
	    		      .reader(createReader())
	    		      .writer(createWriter())
	    		      .processor(processor)
	    		      .build();
	}
	
	
	@Bean(name="job1")
	public  Job createJob1() {
		return  jbFactory.get("job1")
				     .incrementer(new RunIdIncrementer())
				     .listener(listener)
				     .start(createStep1())
				     .build();
	}

	
	

}
