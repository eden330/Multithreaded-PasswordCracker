# Multithreaded-PasswordCracker

This is a multi-threaded Java application designed to hack hashed passwords using various techniques,
including dictionary-based attacks and prefix/suffix combinations. 
The application reads hashed passwords from a file, generates possible password candidates based
on a provided dictionary, and attempts to crack the hashes using multi-threaded processing.

## Overview 
The Password Hacker application implements a multi-threaded approach to password cracking, leveraging the processing power of multiple threads to efficiently search for password matches. It utilizes different producer threads to generate password candidates and a consumer thread to check for matches against a list of hashed passwords.

The application supports the following features:

- Dictionary-Based Attacks: Utilizes a dictionary of common words and phrases to generate password candidates for hashing.
- Prefix/Suffix Combinations: Adds numeric prefixes and suffixes to dictionary words to create additional password candidates.
- Multi-Threaded Processing: Distributes the password cracking workload across multiple threads to improve performance.
- MD5 Hashing: Utilizes the MD5 hashing algorithm for generating and comparing password hashes.
