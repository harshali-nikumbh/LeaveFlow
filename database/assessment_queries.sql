
-- SETTribe LLP Assessment
-- Employee Leave Management System
-- Database: employee_leave_management

USE employee_leave_management;

-- 1. Display all users
SELECT id, name, email, role, created_at
FROM users;

-- 2. Display leave requests for one user
-- Change 1 to the required employee ID.
SELECT *
FROM leave_requests
WHERE user_id = 1
ORDER BY created_at DESC;

-- 3. Count leave requests by status
SELECT status, COUNT(*) AS total_requests
FROM leave_requests
GROUP BY status;

-- 4. Find all pending requests
SELECT *
FROM leave_requests
WHERE status = 'PENDING'
ORDER BY created_at DESC;

-- 5. Find approved requests ordered by start date
SELECT *
FROM leave_requests
WHERE status = 'APPROVED'
ORDER BY from_date ASC;

-- 6. Join users with their leave requests
SELECT
    u.id AS user_id,
    u.name,
    u.email,
    lr.id AS leave_request_id,
    lr.leave_type,
    lr.from_date,
    lr.to_date,
    lr.reason,
    lr.status
FROM users u
INNER JOIN leave_requests lr
    ON u.id = lr.user_id
ORDER BY lr.created_at DESC;

-- 7. Update a request's status
-- SAFETY: Replace 1 with an existing request ID
-- only when you intend to change that request.
-- UPDATE leave_requests
-- SET status = 'APPROVED'
-- WHERE id = 1 AND status = 'PENDING';

-- 8. Delete a pending request
-- SAFETY: This permanently deletes the matching row.
-- Uncomment only after confirming the request ID.
-- DELETE FROM leave_requests
-- WHERE id = 1 AND status = 'PENDING';
