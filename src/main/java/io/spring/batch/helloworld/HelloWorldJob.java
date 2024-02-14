package io.spring.batch.helloworld;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@EnableBatchProcessing
@SpringBootApplication
public class HelloWorldJob {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job job() {
        return this.jobBuilderFactory.get("basicJob")
                .start(step1())
                .build();
    }

    @Bean
    public Step step1() {
        return this.stepBuilderFactory.get("step1")
                .tasklet(helloWorldTasklet2(null))
                .build();
    }

    // ChunkContext 의 getStepContext > getJobParameters 이용하여 파라미터를 가져와 처리
    @Bean
    public Tasklet helloWorldTasklet() {
        return (stepContribution, chunkContext) -> {
            // stepContribution: 커밋되지 않은 현재 트랜잭션에 대한 정보(쓰기 수, 읽기 수 등)를 가지고 있음
            // chunkContext: 실행 시점의 잡 상태를 제공, 태스크릿 내에서는 처리 중인 청크와 관련된 정보를 가짐
            //   - 해당 청크 정보는 스텝 및 잡과 관련된 정보를 가지고 잇으며 JobParameters 포함된 StepContext 의 참조가 있음
            String name = (String) chunkContext
                    .getStepContext()
                    .getJobParameters() // Map 정보를 통해 파라미터 정보를 가져올 수 있음
                    .get("name");
            System.out.printf("Hello %s!%n", name);
            return RepeatStatus.FINISHED;
        };
    }

    // 스프링의 늦은 바인딩으로 JobParameters 코드를 참조하지 않고도 잡 파라미터를 컴포넌트에 주입하는 방식
    @Bean
    public Tasklet helloWorldTasklet2(
            @Value("#{jobParameters['name']}") String name // 스프링 Expression Language(EL) 을 사용해 값을 전달
            // 이와 같은 늦은 바인딩으로 구성될 빈은 스텝이나 잡 스코프를 가져야 함
    ) {
        //
        return (stepContribution, chunkContext) -> {
            System.out.printf("Hello %s!%n", name);
            return RepeatStatus.FINISHED;
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(HelloWorldJob.class, args);
    }

}
