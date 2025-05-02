package com.minionsai.claim.config;

import com.minionsai.claim.service.ClaimService;
import com.minionsai.claim.tool.AdjusterAssignerTool;
import com.minionsai.claim.tool.AvailableAgentsTool;
import com.minionsai.claim.tool.ClaimSubmissionTool;
import com.minionsai.claim.tool.CoverageCheckerTool;
import com.minionsai.claim.tool.CustomerCommunicationTool;
import com.minionsai.claim.tool.DocumentGeneratorTool;
import com.minionsai.claim.tool.FraudCheckerTool;
import com.minionsai.claim.tool.GeoLocationTool;
import com.minionsai.claim.tool.HistoricalClaimsTool;
import com.minionsai.claim.tool.LoggingTool;
import com.minionsai.claim.tool.MasterOrchestrationTool;
import com.minionsai.claim.tool.PolicyDatabaseTool;
import com.minionsai.claim.tool.PremiumValidatorTool;
import com.minionsai.claim.tool.TowDispatchTool;
import com.minionsai.claim.service.ClaimAgentManager;
import java.util.function.Function;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

@Configuration
public class ToolConfiguration {

  @Bean
  @Description("Provides a list of available agents in the claims processing workflow, along with their responsibilities. This tool helps orchestrate claim processing by informing other components about which agents can handle specific tasks. It is essential for dynamic task assignment and workflow automation. IMPORTANT: ONLY USE THE AGENT NAME THAT YOU GET FROM availableAgentsTool. Any other toolName would not work and would result in an error.")
  public Function<AvailableAgentsTool.Request, AvailableAgentsTool.Response> availableAgentsTool() {
    return new AvailableAgentsTool();
  }

  @Bean
  @Description("Handles communication with the customer, including sending messages and receiving responses through SMS, email, or other channels. Provides transcripts of interactions for audit and tracking. IMPORTANT: All messages for the customer must be sent through this tool. Do not rely on any other tool for customer communication.")
  public Function<CustomerCommunicationTool.Request, CustomerCommunicationTool.Response> customerCommunicationTool(
      ClaimService claimService) {
    return new CustomerCommunicationTool(claimService);
  }

  @Bean
  @Description("Logs all agent decisions, interactions, and key actions. Used for debugging, compliance, audit trails, and AI decision tracking.")
  public Function<LoggingTool.Request, LoggingTool.Response> loggingTool() {
    return new LoggingTool();
  }

  @Bean
  @Description("Retrieves policy details from the insurer’s system to verify whether the policy exists, its status, and the minionType of coverage it provides.")
  public Function<PolicyDatabaseTool.Request, PolicyDatabaseTool.Response> policyDatabaseTool() {
    return new PolicyDatabaseTool();
  }

  @Bean
  @Description("Determines if the reported claim is covered under the policy by analyzing policy terms, coverage limits, and incident details.")
  public Function<CoverageCheckerTool.Request, CoverageCheckerTool.Response> coverageCheckerTool() {
    return new CoverageCheckerTool();
  }

  @Bean
  @Description("Validates whether the policyholder is up-to-date on premium payments to ensure claim eligibility. Claims associated with unpaid policies may be flagged for further review.")
  public Function<PremiumValidatorTool.Request, PremiumValidatorTool.Response> premiumValidatorTool() {
    return new PremiumValidatorTool();
  }

  @Bean
  @Description("Analyzes the claim for potential fraud using AI models, behavioral analytics, and fraud detection heuristics resulting in a fraud score between 0.0 and 1.0. Anything below 0.5 is accepted. Values between 0.5 and 0.7 need to be reviewed by a human agent and anything above 0.7 is fraudulent and must be declined.")
  public Function<FraudCheckerTool.Request, FraudCheckerTool.Response> fraudCheckerTool() {
    return new FraudCheckerTool();
  }

  @Bean
  @Description("Retrieves and analyzes the claimant's past claims history to identify patterns of excessive claims or potential fraudulent behavior.")
  public Function<HistoricalClaimsTool.Request, HistoricalClaimsTool.Response> historicalClaimsTool() {
    return new HistoricalClaimsTool();
  }


  @Bean
  @Description("Finds the nearest available tow truck service for the claimant based on real-time location data and service availability. Ensures timely roadside assistance dispatch.")
  public Function<TowDispatchTool.Request, TowDispatchTool.Response> towDispatchTool() {
    return new TowDispatchTool();
  }

  @Bean
  @Description("Submits the finalized claim to the insurer’s backend system, ensuring it is properly structured and processed for review and approval. All fields are mandatory, and the claim must pass validation checks.")
  public Function<ClaimSubmissionTool.Request, ClaimSubmissionTool.Response> claimSubmissionTool() {
    return new ClaimSubmissionTool();
  }

  @Bean
  @Description("Generates a structured claim summary document in PDF format after the claim submission. This document serves as an official summary for reference and communication.")
  public Function<DocumentGeneratorTool.Request, DocumentGeneratorTool.Response> documentGeneratorTool() {
    return new DocumentGeneratorTool();
  }

  @Bean
  @Description("Assigns a claim adjuster based on claim severity, geographic location, and workload balancing. Ensures timely assessment and resolution of claims.")
  public Function<AdjusterAssignerTool.Request, AdjusterAssignerTool.Response> adjusterAssignerTool() {
    return new AdjusterAssignerTool();
  }

  @Bean
  @Description("Manages the entire claim workflow by dynamically determining the next agent to execute. The nextAgentName cannot be null and must match the exact agent toolName provided to you in the messages without any space.")
  public Function<MasterOrchestrationTool.Request, MasterOrchestrationTool.Response> masterOrchestrationTool(
      ClaimAgentManager claimAgentManager) {
    return new MasterOrchestrationTool(claimAgentManager);
  }

  @Bean
  @Description("Generates latitude and longitude from a reported location. The response includes the generated coordinates, which can be used for validation or geolocation tracking.")
  public Function<GeoLocationTool.Request, GeoLocationTool.Response> geoLocationTool() {
    return new GeoLocationTool();
  }
}
