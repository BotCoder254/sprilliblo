# Development Guidelines

## Code Quality Standards

### JavaScript/React Formatting Patterns
- **JSDoc Comments**: Use TypeScript-style JSDoc comments for configuration objects
  ```javascript
  /** @type {import('tailwindcss').Config} */
  ```
- **Module Exports**: Use CommonJS `module.exports` for configuration files
- **Array Formatting**: Multi-line arrays with trailing commas for better diffs
- **Object Structure**: Logical grouping with inline comments for sections

### Java Formatting Standards
- **Package Structure**: Follow reverse domain naming convention (`com.blog.blog_backend`)
- **Class Naming**: PascalCase with descriptive suffixes (`BlogBackendApplication`)
- **Method Naming**: camelCase with clear intent (`contextLoads`)
- **Indentation**: Tab-based indentation consistently applied
- **Import Organization**: Group imports logically (framework imports first)

### General Code Structure
- **File Organization**: Clear separation between configuration, source, and test files
- **Naming Conventions**: Descriptive names that indicate purpose and scope
- **Comment Style**: Inline comments for complex sections, JSDoc for type definitions
- **Consistent Formatting**: Maintain consistent spacing and indentation patterns

## Semantic Patterns Overview

### Configuration Management Patterns
- **Tailwind Configuration**: Extended theme configuration with custom color palettes
  ```javascript
  theme: {
    extend: {
      colors: {
        // Organized by theme (dark/light) and purpose (text/accent)
      }
    }
  }
  ```
- **Color System**: Semantic color naming with theme-specific variants
- **Animation Definitions**: Custom keyframes with descriptive names and smooth transitions

### Spring Boot Application Patterns
- **Main Class Structure**: Standard Spring Boot application entry point
  ```java
  @SpringBootApplication
  public class BlogBackendApplication {
      public static void main(String[] args) {
          SpringApplication.run(BlogBackendApplication.class, args);
      }
  }
  ```
- **Annotation Usage**: `@SpringBootApplication` for auto-configuration
- **Package Organization**: Domain-based package structure

### Testing Patterns
- **Test Class Naming**: Suffix test classes with `Tests` (`BlogBackendApplicationTests`)
- **Test Method Naming**: Descriptive method names indicating test purpose (`contextLoads`)
- **Spring Boot Testing**: Use `@SpringBootTest` for integration tests
- **JUnit 5**: Modern testing framework with `@Test` annotations

### Performance Monitoring Patterns
- **Web Vitals Integration**: Conditional performance monitoring
  ```javascript
  const reportWebVitals = onPerfEntry => {
    if (onPerfEntry && onPerfEntry instanceof Function) {
      // Dynamic import for performance metrics
    }
  };
  ```
- **Dynamic Imports**: Lazy loading of performance monitoring libraries
- **Function Validation**: Type checking before execution

## Architectural Design Patterns

### Frontend Architecture
- **Utility-First CSS**: Tailwind CSS with extensive customization
- **Theme System**: Comprehensive dark/light mode support with semantic color naming
- **Animation Framework**: Custom animations with consistent timing and easing
- **Performance Optimization**: Web vitals monitoring and dynamic imports

### Backend Architecture
- **Spring Boot Convention**: Standard application structure with auto-configuration
- **Package Organization**: Domain-driven package structure
- **Testing Strategy**: Integration testing with Spring Boot Test framework
- **Minimal Configuration**: Leverage Spring Boot defaults with targeted customization

### Development Workflow Patterns
- **Configuration-First**: Extensive configuration files for customization
- **Convention Over Configuration**: Follow framework conventions where possible
- **Separation of Concerns**: Clear boundaries between frontend styling, backend logic, and testing
- **Performance Monitoring**: Built-in performance tracking and optimization

## Code Quality Practices

### Documentation Standards
- **Inline Comments**: Use comments to explain complex configuration sections
- **Type Annotations**: JSDoc type annotations for better IDE support
- **Descriptive Naming**: Self-documenting code through clear naming conventions

### Error Handling and Validation
- **Function Parameter Validation**: Check parameter types before execution
- **Conditional Execution**: Guard clauses for optional functionality
- **Graceful Degradation**: Handle missing dependencies gracefully

### Performance Considerations
- **Lazy Loading**: Dynamic imports for non-critical functionality
- **Efficient Animations**: Hardware-accelerated CSS animations with appropriate timing
- **Minimal Dependencies**: Only include necessary libraries and frameworks

### Testing Approach
- **Integration Testing**: Focus on application context and framework integration
- **Minimal Test Setup**: Leverage framework testing utilities
- **Clear Test Intent**: Test method names that clearly indicate purpose

## Framework-Specific Guidelines

### Tailwind CSS Best Practices
- **Custom Color Palettes**: Define semantic color systems for consistent theming
- **Animation Definitions**: Create reusable animations with descriptive names
- **Content Configuration**: Specify file patterns for optimal build performance
- **Theme Extension**: Extend default theme rather than overriding completely

### Spring Boot Best Practices
- **Auto-Configuration**: Rely on Spring Boot's auto-configuration capabilities
- **Standard Structure**: Follow Spring Boot project conventions
- **Annotation-Driven**: Use appropriate Spring annotations for configuration
- **Testing Integration**: Utilize Spring Boot testing framework for comprehensive tests

### React Development Patterns
- **Performance Monitoring**: Integrate web vitals for production performance tracking
- **Conditional Loading**: Use dynamic imports for optional functionality
- **Export Patterns**: Consistent default exports for components and utilities