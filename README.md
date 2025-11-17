# SPRILLIBLO - Multi-Tenant Blog Platform

A modern, responsive multi-tenant blog platform built with React and Spring Boot.

## Features

- **Multi-tenant Architecture**: Each user can create and manage multiple blogs
- **Modern UI**: Built with React 19, Tailwind CSS, and Framer Motion
- **Dark/Light Mode**: Automatic theme switching with user preference
- **Responsive Design**: Mobile-first design that works on all devices
- **Authentication**: Email/password authentication with JWT tokens
- **Real-time Updates**: WebSocket support for live content updates
- **Secure Backend**: Spring Boot with Spring Security and MongoDB

## Tech Stack

### Frontend
- React 19.2.0
- TanStack Query for data fetching
- Tailwind CSS for styling
- Framer Motion for animations
- Lucide React for icons
- React Router for navigation

### Backend
- Spring Boot 3.5.7
- Spring Security with JWT
- Spring Data MongoDB
- Java 17

## Getting Started

### Prerequisites
- Node.js 18+ and npm
- Java 17+
- MongoDB Atlas account (or local MongoDB 4.4+)

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd sprilliblo
   ```

2. **Install Frontend Dependencies**
   ```bash
   cd blog-frontend
   npm install
   ```

3. **Install Backend Dependencies**
   ```bash
   cd ../blog-backend
   ./mvnw clean install
   ```

4. **Configure MongoDB Atlas**
   - Create a `.env` file in the `blog-backend` directory
   - Add your MongoDB password: `MONGODB_PASSWORD=your_actual_password`
   - Or set the environment variable `MONGODB_PASSWORD`

5. **Start the Backend**
   ```bash
   cd blog-backend
   ./mvnw spring-boot:run
   ```

6. **Start the Frontend**
   ```bash
   cd blog-frontend
   npm start
   ```

7. **Access the Application**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080/api

## Project Structure

```
sprilliblo/
├── blog-frontend/          # React frontend
│   ├── src/
│   │   ├── components/     # Reusable UI components
│   │   ├── pages/          # Page components
│   │   ├── contexts/       # React contexts (Auth, Theme)
│   │   ├── services/       # API service layer
│   │   ├── hooks/          # Custom React hooks
│   │   └── utils/          # Utility functions
│   └── public/             # Static assets
├── blog-backend/           # Spring Boot backend
│   └── src/main/java/com/blog/blog_backend/
│       ├── controller/     # REST controllers
│       ├── service/        # Business logic
│       ├── repository/     # Data access layer
│       ├── model/          # Entity models
│       ├── dto/            # Data transfer objects
│       ├── config/         # Configuration classes
│       └── security/       # Security components
└── README.md
```

## API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `POST /api/auth/forgot` - Forgot password
- `POST /api/auth/reset` - Reset password
- `GET /api/auth/me` - Get current user
- `POST /api/auth/logout` - User logout

## Data Models

### User
- id, email, password, firstName, lastName
- emailVerified, createdAt, updatedAt
- resetToken (for password reset)
- tenantIds, currentTenantId (multi-tenant support)

### Tenant (Blog)
- id, slug, name, description, ownerId
- settings (theme, allowComments, isPublic, customDomain)
- members (userId, role, joinedAt)

## Authentication Flow

1. User registers with email/password and creates a blog
2. JWT token is issued upon successful authentication
3. Token includes user ID, email, and current tenant ID
4. Frontend stores token and uses it for API requests
5. Backend validates token on protected routes

## Multi-Tenant Features

- Users can belong to multiple blogs/tenants
- Each tenant has its own settings and members
- Role-based access control (OWNER, ADMIN, EDITOR, VIEWER)
- Tenant switching capability

## Development

### Frontend Development
```bash
cd blog-frontend
npm start          # Start development server
npm test           # Run tests
npm run build      # Build for production
```

### Backend Development
```bash
cd blog-backend
./mvnw spring-boot:run    # Start development server
./mvnw test               # Run tests
./mvnw package            # Build for production
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License.