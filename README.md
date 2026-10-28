# LeaveFlow – Employee Leave Management System

## Project Overview
LeaveFlow is a web-based Employee Leave Management System that allows employees to submit and manage leave requests while administrators can review and approve or reject them.

## Technologies Used
- **Frontend:** React, Vite, JavaScript
- **Backend:** Java, Spring Boot
- **Database:** MySQL 8
- **Authentication:** JWT
- **Password Security:** BCrypt

## Features
- Employee registration and login
- Secure password storage
- JWT-based authentication
- Employee leave request submission
- Edit and cancel pending requests
- View personal leave request history
- Filter requests by status and leave type
- Admin dashboard to view, approve, and reject requests
- Role-based access control
- MySQL database persistence

## Project Structure
```text
ST assignment 01/
├── backend/
│   └── employee-leave-management/
├── frontend/
├── database/
│   └── assessment_queries.sql
└── README.md
```

## Database Setup
1. Install and start MySQL Server.
2. Create a database named `employee_leave_management`.
3. Configure the database connection in the backend's `application.properties`.
4. Set the `DB_PASSWORD` and `JWT_SECRET` environment variables before starting the backend.
5. The application uses Spring Data JPA to manage database tables.

**Important:** Do not commit passwords, JWT secrets, or other credentials to GitHub.

## Run the Backend
Open a terminal in the backend project directory:

```bash
cd backend/employee-leave-management
```

Set the required environment variables, then run:

```bash
mvn spring-boot:run
```

If the project includes Maven Wrapper, you can use `mvnw` instead.

Backend URL: `http://localhost:8080`

## Run the Frontend
Open another terminal:

```bash
cd frontend
npm install
npm run dev
```

Open the local URL displayed in the terminal, usually `http://localhost:5173`.

## SQL Assessment
The `database/assessment_queries.sql` file contains queries for:

1. Displaying all users
2. Retrieving leave requests for a specific user
3. Counting requests by status
4. Finding pending requests
5. Retrieving approved requests ordered by start date
6. Joining users with their leave requests
7. Updating a leave request's status
8. Deleting a pending leave request

The update and delete examples are commented out to prevent accidental changes to the database.

## Testing
The application was tested for:
- User registration and login
- Employee leave submission
- Editing and cancelling pending requests
- Admin approval and rejection
- Dashboard updates after status changes
- Status and leave-type filtering
- Employee-specific request visibility
- Role-based access restrictions
- Empty form submission and date validation

## Conclusion
LeaveFlow provides a centralized system for managing employee leave requests, with role-based access, secure authentication, and persistent MySQL storage.
