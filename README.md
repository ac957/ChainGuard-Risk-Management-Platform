# ChainGuard – Risk Management System

A web-based risk management system designed to help organisations identify, assess, monitor and mitigate business risks, with a particular focus on supply chain and logistics environments.
ChainGuard provides a centralised risk register, risk assessment tools, organisational management, notifications, mitigation tracking, analytics and AI-powered educational and mitigation guidance using Google's Gemini API.

## Features

### 🔐 Authentication & Security
- User registration and login
- Secure password handling using BCrypt
- Role-based access for:
  Admins, Managers and Users
- Session-based authentication using Spring Security
- Password reset functionality
- Profile setup and management
- CSRF protection

### ⚠️ Risk Management
- Create and submit organisational risks
- Categorise risks by type
- Record likelihood and impact scores
- Automatically calculate risk severity
- View and update existing risks
- Track the risk lifecycle
- Assign mitigation actions
- AI powered mitigation assignemt guidance 
- Monitor risk status through to resolution

### 📊 Analytics Dashboard
- Risk analytics and statistics
- Visual representation of organisational risks
- AI Powered Risk visualizations analysis
- Dashboard views tailored to the organisation

### 👥 Organisation Management
- Create and manage organisations
- Manage organisation members
- Support different organisational roles
- Manager approval of membership requests
- Organisation-specific risk management

### 🔔 Notifications
- In-system notifications
- Notifications for important risk-management events
- Email notifications using Gmail SMTP
- Notification support for risk submissions and mitigation assignments

### 🤖 AI-Powered Risk Education

ChainGuard integrates Google's Gemini API to provide educational guidance when users submit risks.

The AI can:
Explain what a submitted risk means
Explain the calculated severity
Identify common causes
Explain what effective mitigation can look like
Explain the next steps in the ChainGuard risk lifecycle
Provide feedback on the quality of a risk submission
Generate a risk-literacy score from 1–10
Provide suggestions for improving future risk submissions

The AI service also includes fallback responses so that the application can continue providing useful information if the Gemini API is temporarily unavailable or rate-limited.

### 📄 Risk Reports
- Generate risk reports
- Export risk information as PDF
- Provide structured information about organisational risks

## Technologies Used 
- Java 17 -	Application development
- Spring Boot -	Backend framework
- Spring MVC - Web application architecture
- Spring Security -	Authentication and authorisation
- Spring Data JPA	- Database access
- Hibernate	- ORM
- MySQL -	Relational database
- JSP	Server-side - web interface
- HTML/CSS - Front-end presentation
- Google Gemini API -	AI-powered features
- JavaMail / Gmail SMTP	Email notifications
- iText -	PDF report generation
- Gradle - Build and dependency management
- JUnit 5 -	Unit testing
- Mockito	- Mock-based testing

## AI Integration

ChainGuard uses Google's Gemini API through the Google GenAI Java SDK.

The application sends structured prompts containing information about the submitted risk, including:

- Risk title
- Risk category
- Risk description
- Likelihood
- Impact
- Severity score

Gemini is instructed to return structured JSON containing educational guidance and a risk-literacy score.

The response is then converted into Java DTOs and displayed to the user through the application.

## AI Configuration

The Gemini API key must not be committed to GitHub.

Configure the application using an environment variable:

gemini.api.key=${GEMINI_API_KEY}
gemini.model=gemini-3-flash-preview

Then provide your own API key through the GEMINI_API_KEY environment variable.

For example, on Windows PowerShell:

$env:GEMINI_API_KEY="your-api-key"

On macOS/Linux:

export GEMINI_API_KEY="your-api-key"

The AI functionality is designed to handle temporary API failures and rate limiting using fallback responses.

## Email Configuration

ChainGuard uses Gmail SMTP to send email notifications.

Do not commit your email password or App Password to GitHub.

Configure the application using environment variables:

spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

Example:

$env:MAIL_USERNAME="your-email@gmail.com"
$env:MAIL_PASSWORD="your-app-password"

A Gmail App Password should be used rather than your normal Gmail account password when required by the account configuration.

## Database Setup

ChainGuard uses MySQL.

### 1. Install MySQL

Install MySQL Server and ensure that the MySQL service is running.

### 2. Create the database

Create a database named:

CREATE DATABASE risk_management_system;
### 3. Configure database credentials

The application expects the database configuration to be supplied through environment variables.

spring.datasource.url=jdbc:mysql://localhost:3306/risk_management_system
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

For example, on Windows PowerShell:

$env:DB_USERNAME="root"
$env:DB_PASSWORD="your-database-password"
### 4. Database schema

Hibernate is configured to update the database schema automatically when the application starts.

The application also initialises the default organisational roles and risk categories when it starts.

Default roles include:

ADMIN
MANAGER
USER

Risk categories include:

Operational
Financial
Geopolitical
Environmental
Cybersecurity
Compliance
Reputational
Other

## Installation
### Prerequisites

Before running the application, install:

Java 17 or later
MySQL
Git

Optional:

A Gemini API key for AI functionality
An SMTP-enabled email account for email notifications

### Clone the Repository
git clone https://github.com/YOUR-USERNAME/ChainGuard.git
cd ChainGuard
### Configure Environment Variables

Configure the database credentials:

DB_USERNAME
DB_PASSWORD

For the optional AI functionality:

GEMINI_API_KEY

For email notifications:

MAIL_USERNAME
MAIL_PASSWORD

See the configuration section above for more information.

### Run the Application

On Windows:

gradlew.bat bootRun

On macOS/Linux:

./gradlew bootRun

Alternatively, the project can be imported into IntelliJ IDEA or another Java IDE as a Gradle project.

Once the application has started, access it through the local server address shown in the Spring Boot console.

## Testing

The project includes automated unit tests using JUnit 5 and Mockito.

Tests cover areas of the application's business logic including:

- Authentication services
- Risk management
- Risk severity calculations
- Mitigation assignment
- Notification functionality

Run the test suite using:

### Windows
gradlew.bat test
macOS/Linux
./gradlew test

## Security Considerations

Sensitive configuration must never be committed to the repository.

The following should remain private:

GEMINI_API_KEY
MAIL_PASSWORD
DB_PASSWORD

The repository should contain configuration placeholders rather than real credentials.

For example:

gemini.api.key=${GEMINI_API_KEY}

rather than:

gemini.api.key=YOUR-REAL-API-KEY

## Key Learning Outcomes

This project demonstrates practical experience with:

- Object-oriented programming in Java
- Spring Boot application development
- MVC architecture
- REST/web application development
- Database design and persistence
- JPA and Hibernate
- Authentication and authorisation
- Secure password handling
- Role-based access control
- External API integration
- Prompt engineering and structured AI responses
- Email service integration
- PDF generation
- Automated unit testing
- Mocking with Mockito
- Exception handling and fallback behaviour
- Gradle dependency management
- Git-based version control


Project maintainer and creator : Anetta Chibangula



