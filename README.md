# Assignment 2 – SPL Course (Microservices Framework & Simulation)

This repository contains my **second university assignment** for the  
**Systems Programming Lab (SPL)** course.  

The project implements a **Microservices-based simulation framework** in Java,  
focusing on concurrent event-driven programming.  
It models multiple sensors (Camera, LiDAR, GPS/IMU) and services (PoseService, FusionSlamService)  
communicating via a custom **MessageBus**.

---

## Project Overview
- **Language**: Java
- **Build System**: Maven (`pom.xml`)
- **Architecture**: Microservices + Publish/Subscribe messaging
- **Domain**: Sensor fusion simulation (camera, LiDAR, pose tracking, SLAM)

---

## Key Concepts & Principles Learned
1. **Microservices architecture**  
   - Independent services communicate asynchronously.  
   - Separation of concerns between components.  

2. **Event-driven design**  
   - Events (`Event`, `Broadcast`) are passed via a central `MessageBus`.  
   - Services subscribe to specific messages and react dynamically.  

3. **Concurrency & synchronization**  
   - Safe communication between threads using `Future<T>` for async results.  
   - Handling parallel microservices without race conditions.  

4. **Sensor fusion simulation**  
   - Camera, LiDAR, and GPS/IMU data combined into a `FusionSlamService`.  
   - Objects detected, tracked, and merged into a consistent pose.  

5. **Software engineering practices**  
   - Modular project structure (`src/main/java` vs `src/test/java`).  
   - Unit testing with JUnit (`MessageBusTest`, `FusionSlamTest`, `CameraTest`).  
   - Maven build and dependency management.  

---

##  Build & Run
To build the project with Maven:
```bash
mvn clean install
