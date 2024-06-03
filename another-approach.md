# Core Service Orchestrator and Redis Integration

## Overview

This document outlines the key efforts and accomplishments of the architecture and engineering teams in developing a core service orchestrator. The orchestrator integrates with various system layers, leveraging Redis for efficient lookups and optimizing client request handling.

## Core Service Orchestrator

The core service orchestrator is designed to streamline communication between different backend layers. It serves as a central hub, coordinating interactions and ensuring seamless data flow across the system.

### Key Features

- **API-Oriented Backend Layers**: The orchestrator connects to multiple backend layers via APIs, facilitating efficient data exchange and process management.
- **Redis for Lookups**: We have transitioned from traditional database lookups and JDBC join calls to Redis-based lookups. This shift has significantly improved the performance and scalability of our system.
- **Core Service API**: We have started developing a framework to enable post-screening filters, allowing the declaration of various filters to enhance the flexibility and functionality of the core service.

## Redis Integration

Redis plays a crucial role in our new architecture by handling a variety of lookup data that was previously managed by database join operations.

### Key Changes

- **Lookup Ready Hashes**: Important data, such as config information, is now stored in Redis as hash structures, allowing for fast and direct lookups.
- **Elimination of JDBC Joins**: By moving lookup data to Redis, we have eliminated the need for expensive JDBC join operations, resulting in quicker response times and reduced database load.

## Client Request Handling

We have overhauled our client request handling mechanism to improve efficiency and reliability.

### Previous Approach

- **Database Storage**: Client requests were previously stored in a database, capturing the request lines for processing.

### New Approach

- **HTTP Interactions**: We have replaced database storage with direct HTTP interactions between customer-facing APIs (e.g., JMS, REST APIs) and large batch interface APIs. This change enhances the responsiveness and scalability of our system.
- **Java 21 Integration**: As part of this effort, we have integrated Java 21, leveraging virtual threads for many of our blocking calls against backend lower-level APIs, which are predominantly blocking endpoints. This integration supports concurrent calls against multiple screening requests, reducing blocking time on platform threads and enhancing the performance and concurrency of our system.

## Data Preloading Module

To support our Redis-based architecture, we developed a module dedicated to preloading data from databases into Redis.

### Key Features

- **Adhoc Process**: The data preloading module operates as a separate adhoc process, ensuring that Redis is always populated with the necessary data for efficient lookups.
- **Database Integration**: The module seamlessly integrates with existing databases, extracting and loading data into Redis without disrupting ongoing operations.

## Large Batch Processing

We have implemented Spring Batch processing to handle large batch files efficiently.

### Key Features

- **Spring Batch**: This framework processes input files, splits them, processes them against the core service, and writes the output back.
- **Object Storage**: All file handling (input, split, and output files) is performed on object storage, ensuring scalability and reliability.
- **Kubernetes Support**: The entire batch processing workflow runs in containers, making it compatible with any Kubernetes-supported environment.

## Conclusion

The collaborative efforts of our architecture and engineering teams have resulted in a robust and efficient system that leverages Redis for lookups, optimizes client request handling, and ensures seamless data flow between backend layers. The integration of Java 21 and virtual threads further enhances the performance, scalability, and reliability of our services by supporting concurrent calls against multiple screening requests and minimizing blocking time on platform threads. Additionally, the development of a post-screening filter framework and the implementation of Spring Batch processing within a Kubernetes environment highlight our commitment to innovation and efficiency.
