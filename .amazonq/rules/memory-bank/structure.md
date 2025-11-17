# Project Structure

## Directory Organization

### Root Level Structure
```
sprilliblo/
├── blog-frontend/          # React frontend application
├── blog-backend/           # Spring Boot backend application
└── .amazonq/              # Amazon Q configuration and rules
```

## Frontend Structure (blog-frontend/)

### Core Directories
- **`public/`** - Static assets and HTML template
  - `index.html` - Main HTML template
  - `favicon.ico`, `logo192.png`, `logo512.png` - App icons
  - `manifest.json` - PWA configuration
  - `robots.txt` - Search engine directives

- **`src/`** - React application source code
  - `App.js` - Main application component
  - `index.js` - Application entry point
  - `index.css` - Global styles
  - `App.test.js` - Application tests
  - `setupTests.js` - Test configuration
  - `reportWebVitals.js` - Performance monitoring

### Configuration Files
- `package.json` - Dependencies and scripts
- `tailwind.config.js` - Tailwind CSS configuration with custom themes
- `README.md` - Frontend documentation
- `.gitignore` - Git exclusion rules

## Backend Structure (blog-backend/)

### Source Code Organization
```
src/
├── main/
│   ├── java/com/blog/blog_backend/
│   │   └── BlogBackendApplication.java    # Spring Boot main class
│   └── resources/
│       ├── application.properties         # Configuration
│       ├── static/                       # Static resources
│       └── templates/                    # Template files
└── test/
    └── java/com/blog/blog_backend/
        └── BlogBackendApplicationTests.java  # Test suite
```

### Build and Configuration
- **`pom.xml`** - Maven project configuration and dependencies
- **`target/`** - Compiled classes and build artifacts
- **`.mvn/wrapper/`** - Maven wrapper for consistent builds
- **`mvnw`, `mvnw.cmd`** - Maven wrapper scripts
- **`HELP.md`** - Backend documentation

## Core Components and Relationships

### Frontend Architecture
- **React Components**: Modular UI components with modern React patterns
- **Tailwind Integration**: Utility-first CSS with custom design system
- **Testing Framework**: Jest and React Testing Library setup
- **Build System**: Create React App with custom configurations

### Backend Architecture
- **Spring Boot Application**: Main application class with auto-configuration
- **MongoDB Integration**: Document-based data persistence
- **Security Layer**: OAuth2 resource server for authentication
- **WebSocket Support**: Real-time communication infrastructure
- **Session Management**: MongoDB-based session storage

### Inter-Service Communication
- **API Layer**: RESTful endpoints for frontend-backend communication
- **Real-time Updates**: WebSocket connections for live data
- **Authentication Flow**: OAuth2 token-based security
- **Data Persistence**: MongoDB for flexible document storage

## Architectural Patterns

### Frontend Patterns
- **Component-Based Architecture**: Reusable React components
- **Utility-First CSS**: Tailwind CSS for rapid styling
- **Progressive Web App**: Service worker and manifest configuration
- **Modern JavaScript**: ES6+ features and React hooks

### Backend Patterns
- **Layered Architecture**: Clear separation of concerns
- **Dependency Injection**: Spring's IoC container
- **Configuration Management**: External property files
- **Test-Driven Development**: Comprehensive test suite

### Development Patterns
- **Monorepo Structure**: Frontend and backend in single repository
- **Hot Reload**: Development-time code reloading
- **Build Automation**: Maven and npm build processes
- **Version Control**: Git with appropriate ignore patterns