# Web-Based-Music-Store
## Project Overview

This project is a **web-based music store platform** developed using **Java 22, Spring Boot, and MySQL**. The system allows different types of users—**Listeners, Artists, and Admins**—to interact with music-related services in one integrated platform. The application supports user management, song uploads, product and instrument management, music lessons, event ticketing, and an admin dashboard with notification capabilities.

The project follows the **MVC architecture** and uses **Spring Boot** for backend logic, **MySQL** for persistent data storage.

---

## Tech Stack

* **Backend:** Java 22, Spring Boot
* **Database:** MySQL
* **Frontend:** JSP/Thymeleaf
* **Build Tool:** Maven

---

## Core Features

### 1. **User Management & Help & Support**

* Register/Login for **Listeners, Artists, Admins**
* Manage user profiles
* Role-based access control (Spring Security)
* Help & Support module (FAQs, support tickets, basic live chat option)

### 2. **Song Upload & Management**

* Artists can upload songs with metadata (title, genre, price)
* Listeners can browse and purchase/download songs
* Song library management (search, filter, view details)

### 3. **Product & Instrument Management (Selling & Renting)**

* CRUD operations for instruments (add, edit, delete, view)
* Option to **sell or rent instruments**
* Product catalog with categories and pricing

### 4. **Music Lessons & Course Management**

* Instructors can create and manage music courses
* Users can enroll in lessons/courses
* Course details, schedules, and progress tracking

### 5. **Event & Ticketing System**

* Organizers can create music events
* Users can view events and book tickets
* Ticket availability and booking history management

### 6. **Admin Dashboard & Notification System**

* Admin can manage users, products, songs, courses, and events
* System-wide analytics and reports
* Notifications (in-app alerts or email) for events, purchases, or course updates

---

## System Architecture

* **MVC Pattern**
* **Layered Structure:**

  * `Controller Layer` → Handles HTTP requests
  * `Service Layer` → Business logic
  * `Repository Layer` → Data persistence
  * `View Layer` → JSP/Thymeleaf

