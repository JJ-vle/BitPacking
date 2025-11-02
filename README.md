# BitPacking - Software Engineering Project 2025

This project implements several integer compression algorithms for optimizing transmission speed while preserving direct access to individual elements after compression.  
Developed as part of the **Software Engineering Project 2025**.

A detailed project report is available (in french) as *SE2025_BitPacking_JeanJacquesVIALE.pdf*.

---

## Overview

The goal is to reduce the number of transmitted integers by encoding them in a more compact form using bit-level packing.

Implemented compressors:  
- **BitPackingNoOverlap** → keeps integers within individual 32-bit slots  
- **BitPackingOverlap** → allows integers to cross 32-bit boundaries
- **BitPackingOverflow** → handles rare large values using an overflow area  

Each compressor supports:
- `compress(int[] data)`
- `decompress(int[] data)`
- `get(int index)`

---

## Requirements

Before running the project, make sure you have:

- **Java 21+** (Java 25 recommended)
Check with:
```bash
  java -version
```

- **Maven 3.9+**
Check with:
```bash
  mvn -version
```
If Maven is not installed:
[Download Maven](https://maven.apache.org/download.cgi) and add it to your PATH

---

## Project Structure

```
BitPacking/
├── pom.xml                     # Maven build file
├── README.md                   # Instructions et explications
├── src/
│   ├── main/java/com/jjvle/bitpacking/
│   │   ├── Main.java
│   │   ├── BitPackingFactory.java
│   │   ├── BitPackingNoOverlap.java
│   │   ├── BitPackingOverlap.java
│   │   ├── BitPackingOverflow.java
│   │   ├── ICompressor.java
│   │   └── benchmarks/
│   │       ├── Benchmark.java
│   │       └── BenchmarkGenerator.java
│   └── test/java/com/jjvle/bitpacking/
│       ├── BitPackingNoOverlapTest.java
│       ├── BitPackingOverlapTest.java
│       └── BitPackingOverflowTest.java
├── data/
│   ├── in/                     # Fichiers CSV d’entrée
│   └── out/                    # Résultats des benchmarks
└── target/                      # Dossier généré par Maven (classes compilées, JAR)

```

---

## Build & Run

### Clone the repository

```bash
git clone https://github.com/JJ-vle/BitPacking.git
cd BitPacking
```

### Compile and package
```bash
mvn clean package
```

This creates an executable JAR at:
```
target/BitPacking-1.0.jar
```

### Run the program

#### Run all benchmarks in `data/in`
```bash
java -jar target/BitPacking-1.0.jar
```

#### Run a specific file
```bash
java -jar target/BitPacking-1.0.jar "file/path"
```

You should see output similar to:

```
== BitPacking Benchmarks ==
File: boltzmann_1000.csv
  [no_overlap] OK=true | compress=1.23ms | decompress=0.85ms
  [overlap]    OK=true | compress=1.10ms | decompress=0.78ms
  [overflow]   OK=true | compress=1.65ms | decompress=0.91ms
```

---

## Run Tests

To verify all compressors:
```bash
mvn test
```

## Author

* **Jean-Jacques VIALE**
  M1 INFORMATIQUE DS4H - Software Engineering 2025
  [GitHub Profile](https://github.com/JJ-vle)



