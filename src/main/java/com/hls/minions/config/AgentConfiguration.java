package com.hls.minions.config;

import com.hls.minions.agent.CustomerCommunicationAgent;
import com.hls.minions.agent.PolicyVerificationAgent;
import com.hls.minions.agent.FollowUpQuestionAgent;
import com.hls.minions.agent.FraudDetectionAgent;
import com.sun.net.httpserver.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

@Configuration
public class AgentConfiguration {

    @Bean
    @Description("Handles communication with the customer and returns a transcript of the communication")
    public Function<Request, CustomerCommunicationAgent.Response> communicationAgent() {
        return new CustomerCommunicationAgent();
    }

    @Bean
    @Description("Follow-Up Question Agent")
    public Function<FollowUpQuestionAgent.Request, FollowUpQuestionAgent.Response> followUpQuestionAgent() {
        return new FollowUpQuestionAgent();
    }


    @Bean
    @Description("Policy Lookup Agent")
    public Function<PolicyVerificationAgent.Request, PolicyVerificationAgent.Response> policyVerificationTool() {
        return new PolicyVerificationAgent();
    }

    @Bean
    @Description("Coverage Verification Agent")
    public Function<PolicyVerificationAgent.Request, PolicyVerificationAgent.Response> coverageVerificationAgent() {
        return new PolicyVerificationAgent();
    }

    @Bean
    @Description("Fraud Detection Agent")
    public Function<FraudDetectionAgent.Request, FraudDetectionAgent.Response> fraudDetectionAgent() {
        return new FraudDetectionAgent();
    }

    @Bean
    @Description("Damage Assessment Agent")
    public Function<PolicyVerificationAgent.Request, PolicyVerificationAgent.Response> damageAssessmentAgent() {
        return new PolicyVerificationAgent();
    }

}
