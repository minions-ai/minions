# Job Search Minion Example

This example demonstrates how to create a job search assistant minion using the Minions framework.

## Features

- Job search automation
- Resume optimization
- Interview preparation
- Application tracking
- Follow-up management

## Prompts

The example includes five specialized prompts:

1. **Job Search Assistant** (`job_search_assistant`)
   - Main coordinator for job search process
   - Manages preferences and search strategy
   - Analyzes results and provides insights

2. **Job Analysis** (`job_analysis`)
   - Analyzes job descriptions
   - Matches skills and requirements
   - Evaluates culture fit
   - Assesses growth potential

3. **Resume Matching** (`resume_matching`)
   - Optimizes resume for ATS
   - Enhances keyword usage
   - Reviews format and content
   - Improves presentation

4. **Interview Preparation** (`interview_preparation`)
   - Helps with interview prep
   - Practices common questions
   - Researches companies
   - Improves presentation skills

5. **Job Application** (`job_application`)
   - Tracks applications
   - Manages follow-ups
   - Prepares documents
   - Monitors status

## Usage

1. Run the prompt setup test to create all required prompts:
   ```bash
   mvn test -Dtest=JobSearchPromptsSetupTest
   ```

2. Create a job search minion with your preferences:
   ```java
   CreateMinionRequest request = new CreateMinionRequest()
       .setUserId("your-user-id")
       .setMinionType(MinionType.AUTOMATION_ENGINEER)
       .setPromptName("job_search_assistant")
       .setMetadata(createJobSearchMetadata());
   ```

3. Use the minion to:
   - Search for jobs
   - Optimize your resume
   - Prepare for interviews
   - Track applications

## Configuration

The example uses the following metadata structure:

```json
{
  "jobPreferences": {
    "role": "Software Engineer",
    "experience": "5+ years",
    "location": "Remote",
    "skills": ["Java", "Spring", "Microservices", "AWS"]
  },
  "searchPreferences": {
    "salaryRange": "120k-180k",
    "companySize": "1000+",
    "industry": "Technology",
    "jobType": "Full-time"
  }
}
```

## Contributing

Feel free to contribute to this example by:
1. Adding more specialized prompts
2. Enhancing existing prompts
3. Adding more test cases
4. Improving documentation 