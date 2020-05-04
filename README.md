# SUTD 50.005 Labs and Programming-Assignments
Labs and Programming Assignments completed for the ISTD Course 50.005 Computer System Engineering.

## Lab 1: Process Management
A lab that teaches the management of inter-process communication (for UNIX processes) using shared memory. Using fork() to create multiple processes, the project uses semaphores to protected and share resources between processes.

## Lab 2: Banker's Algorithm
An implementation of the Banker's Algorithm which prevents the system from entering a deadlock state in the future or immediately. Given a series of requests by processes for resources, the algorithm determines whether to reject or grant each request.

## Lab 3: TOCTOU Race Condition Attack
An investigative lab that teaches the concept of 'privileged programs' and what a Time Of Check-Time Of Use (TOCTOU) bug is.

## Lab 4: Internet Routes and Measurement of Round Trip Times
An introductory lab to networks which explores the ping and traceroute utilities.

## Lab 5: Symmetric Key Encryption and Message Digest
A program that makes use of DES for data encryption, MD5 for creating message digests and RSA for digital signing.

## Lab 6: Internet Domain Name System
A lab that uses specialised network tools to perform and analyse DNS queries. This involves using dig to perform DNS queries and Wireshark to trace and read DNS packets sent to and from a machine.

## Programming Assignment 1
A programming activity that involves building a shell and creating a daemon process, both of which are common applications of fork().

## Programming Assignment 2
A secure file upload application from a client to an Internet file server. The security features implemented are as such:
1. Before the file is uploaded, the client should authenticate the identity of the file server so that the data would not be leaked.
2. The confidentiality of the data should be protected against eavesdropping during the upload.
