/*
 * Copyright (c) 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package grails.plugins.quartz;

import static org.quartz.JobBuilder.newJob;

import org.quartz.JobDetail;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;

/**
 * Simplified version of Spring's <a href='http://static.springframework.org/spring/docs/2.5.x/api/org/springframework/scheduling/quartz/MethodInvokingJobDetailFactoryBean.html'>MethodInvokingJobDetailFactoryBean</a>
 * that avoids issues with non-serializable classes (for JDBC storage).
 *
 * @author <a href='mailto:beckwithb@studentsonly.com'>Burt Beckwith</a>
 * @author Sergey Nebolsin (nebolsin@gmail.com)
 * @since 0.3.2
 */
public class JobDetailFactoryBean implements FactoryBean<JobDetail>, InitializingBean {
    public static final transient String JOB_NAME_PARAMETER = "org.grails.plugins.quartz.grailsJobName";

    private GrailsJobClass jobClass;
    private JobDetail jobDetail;

    @Required
    public void setJobClass(GrailsJobClass jobClass) {
        this.jobClass = jobClass;
    }

    public void afterPropertiesSet() {
        String name = jobClass.getFullName();
        Assert.state(name != null, "name is required");

        String group = jobClass.getGroup();
        Assert.state(group != null, "group is required");

        // Consider the concurrent flag to choose between stateful and stateless job.
        Class<? extends GrailsJobFactory.GrailsJob> clazz =
                jobClass.isConcurrent() ? GrailsJobFactory.GrailsJob.class : GrailsJobFactory.StatefulGrailsJob.class;

        // Build JobDetail instance.
        jobDetail =
                newJob(clazz)
                .withIdentity(name, group)
                .storeDurably(jobClass.isDurability())
                .requestRecovery(jobClass.isRequestsRecovery())
                .usingJobData(JOB_NAME_PARAMETER, name)
                .withDescription(jobClass.getDescription())
                .build();
    }

    public JobDetail getObject() {
        return jobDetail;
    }

    public Class<JobDetail> getObjectType() {
        return JobDetail.class;
    }

    public boolean isSingleton() {
        return true;
    }
}
