# Queue Management Simulator

A multi-threaded simulation tool that models queue-based service systems (such as checkout counters or server nodes) to analyze and optimize wait times.

## Key Features
* **Concurrent Execution:** Utilizes multi-threading and synchronization to process multiple clients across independent queues simultaneously.
* **Dynamic Routing:** Implements the Strategy Design Pattern to route tasks based on different algorithms (e.g., Shortest Queue vs. Shortest Wait Time).
* **Real-time Monitoring:** Features a live GUI that updates as clients enter, wait, and exit the system.
* **Performance Logging:** Outputs detailed simulation metrics and step-by-step events to a tracking file (`simulation_log.txt`).

## Technologies Built With
* Java
* Multi-threading & Concurrency API
* Java Swing
* Maven
