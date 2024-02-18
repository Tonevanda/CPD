# Part 1 - Performance Evaluation of a single core

## Exercise 1

>Download the example file from moodle that contains the basic algorithm in C/C++ that multiplies two matrices, i.e. multiplies one line of the first matrix by each column of the second matrix (matrixproduct.cpp). Implement the same algorithm in another programming language (just one), such as JAVA, C#, Fortran, etc, of your choice.

>Register the processing time for the two languages of implementation, for input matrices from 600x600 to 3000x3000 elements with increments in both dimensions of 400. Use –O2 as optimization flag in C++.

### Measurements

| Size | C++ (seconds) | C# (seconds)     |
| ---- | ------------- | ---------------- |
| 600  |    0.242      |                  |
| 1000 |    1.523      |                  |
| 1400 |    4.372      |                  |
| 1800 |    20.773     |                  |
| 2200 |    44.503     |                  |
| 2600 |    81.254     |                  |
| 3000 |    135.789    |                  |

## Exercise 2

>Implement a version that multiplies an element from the first matrix by the correspondent line of the second matrix, using the 2 programming languages selected in 1.

>Register the processing time of the algorithm, in the 2 versions, for input matrices from 600x600 to 3000x3000 elements with increments in both dimensions of 400.

>Register the processing time from 4096x4096 to 10240x10240 with intervals of 2048 in C/C++ version.

### Measurements

| Size | C++ (seconds) | C# (seconds)     |
| ---- | ------------- | ---------------- |
| 600  |     0.133     |                  |
| 1000 |     0.675     |                  |
| 1400 |     1.966     |                  |
| 1800 |     4.119     |                  |
| 2200 |     7.499     |                  |
| 2600 |     12.521    |                  |
| 3000 |     19.107    |                  |
| 4096 |     49.877    |        --        |
| 6144 |     165.976   |        --        |
| 8192 |     401.148   |        --        |
| 10240|     750.871   |        --        |

## Exercise 3

>Implement a block oriented algorithm that divides the matrices in blocks and uses the same sequence of computation as in 2, using C/C++.

>Register the processing time from 4096x4096 to 10240x10240 with intervals of 2048, for different block sizes (e.g. 128, 256, 512).

### Measurements

| Size   | Block Size | C++ (seconds) |
| ------ | ---------- | ------------- |
| 4096   | 128        |     36.624    |
|        | 256        |     32.812    |
|        | 512        |     36.634    |
| 6144   | 128        |     126.026   |
|        | 256        |     114.939   |
|        | 512        |     133.084   |
| 8192   | 128        |     498.126   |
|        | 256        |     518.864   |
|        | 512        |     398.672   |
| 10240  | 128        |     585.762   |
|        | 256        |     516.067   |
|        | 512        |     600.399   |
