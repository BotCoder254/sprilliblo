# Technology Stack

## Programming Languages and Versions

### Frontend Technologies
- **JavaScript**: ES6+ with modern React patterns
- **React**: 19.2.0 (latest stable version)
- **CSS**: Tailwind CSS 3.4.18 with custom configuration
- **HTML**: HTML5 with PWA capabilities

### Backend Technologies
- **Java**: Version 17 (LTS)
- **Spring Boot**: 3.5.7
- **Maven**: Build automation and dependency management

## Core Dependencies and Frameworks

### Frontend Dependencies
```json
{
  "react": "^19.2.0",
  "react-dom": "^19.2.0",
  "react-scripts": "5.0.1",
  "web-vitals": "^2.1.4"
}
```

### Frontend Testing Dependencies
```json
{
  "@testing-library/dom": "^10.4.1",
  "@testing-library/jest-dom": "^6.9.1",
  "@testing-library/react": "^16.3.0",
  "@testing-library/user-event": "^13.5.0"
}
```

### Backend Dependencies
- **Spring Boot Starters**:
  - `spring-boot-starter-web` - Web MVC framework
  - `spring-boot-starter-data-mongodb` - MongoDB integration
  - `spring-boot-starter-validation` - Request validation
  - `spring-boot-starter-websocket` - WebSocket support
  - `spring-boot-starter-oauth2-resource-server` - OAuth2 security
  - `spring-boot-starter-test` - Testing framework

- **Additional Libraries**:
  - `spring-session-data-mongodb` - MongoDB session storage
  - `lombok` - Code generation and boilerplate reduction
  - `spring-boot-devtools` - Development utilities
  - `spring-boot-configuration-processor` - Configuration metadata

## Build Systems and Tools

### Frontend Build System
- **Create React App**: Zero-configuration React build setup
- **npm**: Package management and script execution
- **Webpack**: Module bundling (via Create React App)
- **Babel**: JavaScript transpilation (via Create React App)
- **ESLint**: Code linting with React-specific rules

### Backend Build System
- **Maven**: Project management and build automation
- **Maven Wrapper**: Ensures consistent Maven version across environments
- **Spring Boot Maven Plugin**: Application packaging and execution
- **Lombok Annotation Processing**: Compile-time code generation

## Development Commands

### Frontend Development
```bash
# Start development server
npm start

# Run tests in watch mode
npm test

# Build for production
npm run build

# Eject from Create React App (irreversible)
npm run eject
```

### Backend Development
```bash
# Run application in development mode
./mvnw spring-boot:run

# Run tests
./mvnw test

# Package application
./mvnw package

# Clean and compile
./mvnw clean compile
```

## Database and Storage

### Primary Database
- **MongoDB**: NoSQL document database
- **Spring Data MongoDB**: Object-document mapping
- **MongoDB Session Store**: Distributed session management

### Configuration
- **application.properties**: Environment-specific configuration
- **Spring Profiles**: Environment-based configuration management

## Security and Authentication

### Security Framework
- **Spring Security**: Comprehensive security framework
- **OAuth2 Resource Server**: Token-based authentication
- **JWT Support**: JSON Web Token processing (via OAuth2)

### Session Management
- **Spring Session**: Distributed session management
- **MongoDB Session Repository**: Scalable session storage

## Development and Production Tools

### Development Tools
- **Spring Boot DevTools**: Hot reload and development utilities
- **React Hot Reload**: Live code updates during development
- **Maven Wrapper**: Consistent build environment
- **Git**: Version control with appropriate ignore patterns

### Testing Frameworks
- **Frontend**: Jest, React Testing Library, DOM Testing Library
- **Backend**: Spring Boot Test, JUnit, Mockito (via Spring Boot Test)

### Code Quality
- **ESLint**: JavaScript/React code linting
- **Lombok**: Reduces Java boilerplate code
- **Spring Boot Configuration Processor**: Configuration metadata generation

## Browser and Runtime Support

### Frontend Browser Support
- **Production**: >0.2%, not dead, not op_mini all
- **Development**: Latest Chrome, Firefox, and Safari versions

### Backend Runtime
- **Java 17**: Long-term support version
- **Spring Boot 3.x**: Latest stable framework version
- **MongoDB**: Compatible with Spring Data MongoDB version