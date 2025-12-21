<div align="center">
<h1 align="center">WEB-BASED MUSIC STORE</h1>

<p align="center">
<strong>All in one marketplace for songs, instruments, lessons, and live
events.</strong>
</p>


<p align="center">
  <img src="https://img.shields.io/github/last-commit/IT24102844/Web-Based-Music-Store?style=flat-square" />
  <img src="https://img.shields.io/github/languages/count/IT24102844/Web-Based-Music-Store?style=flat-square" />
</p>

<p align="center">
  <img src="https://img.shields.io/github/languages/top/IT24102844/Web-Based-Music-Store?style=flat-square" />
  <img src="https://img.shields.io/badge/java-26.3%25-orange?style=flat-square" />
  <img src="https://img.shields.io/badge/css-1.3%25-purple?style=flat-square" />
</p>


</div>
<p align="center">
  <strong>Built with the tools and technologies</strong>
</p>
<p align="center">
  <img src="https://img.shields.io/badge/Java-24-orange?style=flat-square" />
  <img src="https://img.shields.io/badge/Spring_Boot-3.5.5-brightgreen?style=flat-square" />
  <img src="https://img.shields.io/badge/Thymeleaf-Template_Engine-darkgreen?style=flat-square" />
  <img src="https://img.shields.io/badge/Database-MySQL-lightblue?style=flat-square" />
  <img src="https://img.shields.io/badge/Build-Maven-red?style=flat-square" />
</p>
<hr/>



## Overview

**Web-Based Music Store** is a modular platform that unifies e-commerce, digital
content delivery, learning management, and event ticketing into a single musical
ecosystem.

The system follows a layered Spring Boot architecture with Thymeleaf views,
robust form validation, and MySQL persistence. Uploaded media (songs, cover art,
instrument images) is streamed to a local `uploads/` directory and served
alongside static assets.

**Highlights**

- **Users & Roles:** Customer, Artist, Product Seller, Course Seller, Event Organizer, Admin
- **Domains:** Song marketplace, instrument sales/rentals, courses, events, support desk
- **Security:** Spring Security with role-based access and Thymeleaf security dialect
- **Patterns:** MVC, Strategy (pricing/shipping), Factory utilities, DTO-backed forms

---

## Feature Matrix

<table>
<thead>
<tr>
  <th>Role</th>
  <th>Key Capabilities</th>
</tr>
</thead>
<tbody>

<tr>
  <td><strong>Customer / Listener</strong></td>
  <td>Browse songs & products, manage carts, place orders, review purchases, submit support tickets.</td>
</tr>

<tr>
  <td><strong>Artist</strong></td>
  <td>Upload tracks, edit metadata, manage events, monitor ticket sales, review fan feedback.</td>
</tr>

<tr>
  <td><strong>Instrument Seller</strong></td>
  <td>CRUD products, manage inventory & pricing, track orders, respond to reviews.</td>
</tr>

<tr>
  <td><strong>Course Seller</strong></td>
  <td>Create lessons, publish content, enroll students, track progress.</td>
</tr>

<tr>
  <td><strong>Event Organizer</strong></td>
  <td>Publish events, configure seating, confirm attendees, monitor revenue.</td>
</tr>

<tr>
  <td><strong>Admin</strong></td>
  <td>Moderate content, verify users, view analytics, handle escalations.</td>
</tr>

</tbody>
</table>

---

## Screens & Modules

Role-specific dashboards and forms live under  
`src/main/resources/templates/`.

**Notable views**

- Customer storefront, cart, and checkout
- Artist dashboards for uploads and events
- Seller inventory, orders, and reviews
- Course publishing and enrollment screens
- Event ticketing lifecycle
- Admin moderation and approvals

Static assets are served from  
`src/main/resources/static/`, with runtime uploads mirrored under `uploads/`.

---

## Architecture

<table>
<thead>
<tr>
  <th>Layer</th>
  <th>Packages</th>
  <th>Description</th>
</tr>
</thead>
<tbody>

<tr>
  <td><strong>Config</strong></td>
  <td><code>config</code>, <code>security</code></td>
  <td>Spring Security, datasource, multipart and static-resource config.</td>
</tr>

<tr>
  <td><strong>Controller</strong></td>
  <td><code>controller</code></td>
  <td>HTTP endpoints, validation, DTO binding.</td>
</tr>

<tr>
  <td><strong>Service</strong></td>
  <td><code>service</code>, <code>strategy</code>, <code>factory</code></td>
  <td>Business rules, pricing logic, orchestration.</td>
</tr>

<tr>
  <td><strong>Repository</strong></td>
  <td><code>repository</code></td>
  <td>Spring Data JPA repositories for MySQL.</td>
</tr>

<tr>
  <td><strong>Model</strong></td>
  <td><code>model</code></td>
  <td>Entities such as Artist, Song, Product, Order, Ticket.</td>
</tr>

<tr>
  <td><strong>View</strong></td>
  <td><code>templates</code>, <code>static</code></td>
  <td>Thymeleaf views with role-aware rendering.</td>
</tr>

</tbody>
</table>

---

## Directory Snapshot

```text
src/
├─ main/
│ ├─ java/com/app/musicstore/
│ │ ├─ config/
│ │ ├─ controller/
│ │ ├─ model/
│ │ ├─ repository/
│ │ ├─ security/
│ │ ├─ service/
│ │ ├─ strategy/, factory/, util/
│ │ └─ MusicStoreApplication.java
│ └─ resources/
│   ├─ application.properties
│   ├─ static/
│   └─ templates/
└─ test/java/com/app/musicstore/

```
## Configuration

-   **Database:** MySQL schema `musicStore`
    
-   **JPA:** `ddl-auto=update` for development
    
-   **Uploads:** Multipart enabled (5 MB default limit)
    
-   **Logging:** DEBUG enabled for application, security, and SQL
    

----------

## Quick Start

### Prerequisites

-   Java 24
    
-   Maven Wrapper
    
-   MySQL 8.x with `musicStore` database
    

### Clone & Run

```powershell
git clone https://github.com/IT24102844/Web-Based-Music-Store.git cd Web-Based-Music-Store
```
Configure DB credentials in `application.properties` or via env vars.

```powershell
.\mvnw.cmd spring-boot:run
``` 

Visit: `http://localhost:8080`

### Package
```powershell
.\mvnw.cmd clean package
java -jar target/music-store-webapp-0.0.1-SNAPSHOT.jar
```
----------

## Developer Workflow

-   JPA auto-update enabled (Flyway/Liquibase recommended for teams)
    
-   Tests:
    
```powershell
.\mvnw.cmd test
```
-   No rebuild needed for CSS/JS changes
    
-   Ensure `uploads/` exists and is writable
    

----------

## Troubleshooting

-   **Port in use:** Change `server.port`
    
-   **SQL access denied:** Verify MySQL credentials
    
-   **Large uploads blocked:** Increase multipart limits
    
-   **Static files stale:** Hard refresh browser
    

----------

<div align="center"> <sub>Built with Spring Boot, Thymeleaf, and a shared love for music.</sub> </div> 
