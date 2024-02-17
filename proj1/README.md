# Part 1 - Performance Evaluation of a single core

## Exercise 1

>Download the example file from moodle that contains the basic algorithm in C/C++ that multiplies two matrices, i.e. multiplies one line of the first matrix by each column of the second matrix (matrixproduct.cpp). Implement the same algorithm in another programming language (just one), such as JAVA, C#, Fortran, etc, of your choice.

>Register the processing time for the two languages of implementation, for input matrices from 600x600 to 3000x3000 elements with increments in both dimensions of 400. Use –O2 as optimization flag in C++.

### Measurements

| Size | C++ (seconds) | C# (seconds)     |
| ---- | ------------- | ---------------- |
| 600  |               |                  |
| 1000 |               |                  |
| 1400 |               |                  |
| 1800 |               |                  |
| 2200 |               |                  |
| 2600 |               |                  |
| 3000 |               |                  |

## Exercise 2

>Implement a version that multiplies an element from the first matrix by the correspondent line of the second matrix, using the 2 programming languages selected in 1.

>Register the processing time of the algorithm, in the 2 versions, for input matrices from 600x600 to 3000x3000 elements with increments in both dimensions of 400.

>Register the processing time from 4096x4096 to 10240x10240 with intervals of 2048 in C/C++ version.

### Measurements

| Size | C++ (seconds) | C# (seconds)     |
| ---- | ------------- | ---------------- |
| 600  |               |                  |
| 1000 |               |                  |
| 1400 |               |                  |
| 1800 |               |                  |
| 2200 |               |                  |
| 2600 |               |                  |
| 3000 |               |                  |
| 4096 |               |        --        |
| 6144 |               |        --        |
| 8192 |               |        --        |
| 10240|               |        --        |

## Exercise 3

>Implement a block oriented algorithm that divides the matrices in blocks and uses the same sequence of computation as in 2, using C/C++.

>Register the processing time from 4096x4096 to 10240x10240 with intervals of 2048, for different block sizes (e.g. 128, 256, 512).

### Measurements

| Size   | Block Size | C++ (seconds) |
| ------ | ---------- | ------------- |
| 4096   | 128        |               |
|        | 256        |               |
|        | 512        |               |
| 6144   | 128        |               |
|        | 256        |               |
|        | 512        |               |
| 8192   | 128        |               |
|        | 256        |               |
|        | 512        |               |
| 10240  | 128        |               |
|        | 256        |               |
|        | 512        |               |
