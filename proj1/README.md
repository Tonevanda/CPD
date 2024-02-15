# Part 1 - Performance Evaluation of a single core

## Exercise 1

>Download the example file from moodle that contains the basic algorithm in C/C++ that multiplies two matrices, i.e. multiplies one line of the first matrix by each column of the second matrix (matrixproduct.cpp). Implement the same algorithm in another programming language (just one), such as JAVA, C#, Fortran, etc, of your choice.

>Register the processing time for the two languages of implementation, for input matrices from 600x600 to 3000x3000 elements with increments in both dimensions of 400. Use –O2 as optimization flag in C++.

### Measurements

1. Size **600**
    - C++: 0.187 seconds
    - Python: 0.231 seconds
2. Size **1000**
    - C++:
    - Python
3. Size **1400**
    - C++:
    - Python:
4. Size **1800**
    - C++:
    - Python:
5. Size **2200**
    - C++:
    - Python:
6. Size **2600**
    - C++:
    - Python:
7. Size **3000**
    - C++: 117.782 seconds
    - Python: 39.563 seconds

## Exercise 2

>Implement a version that multiplies an element from the first matrix by the correspondent line of the second matrix, using the 2 programming languages selected in 1.

>Register the processing time of the algorithm, in the 2 versions, for input matrices from 600x600 to 3000x3000 elements with increments in both dimensions of 400.

>Register the processing time from 4096x4096 to 10240x10240 with intervals of 2048 in C/C++ version.

### Measurements

1. Size **600**
    - C++:
    - Python:
2. Size **1000**
    - C++:
    - Python
3. Size **1400**
    - C++:
    - Python:
4. Size **1800**
    - C++:
    - Python:
5. Size **2200**
    - C++:
    - Python:
6. Size **2600**
    - C++:
    - Python:
7. Size **3000**
    - C++:
    - Python:
8. Size **4096**
    - C++:
9. Size **6144**
    - C++:
10. Size **8192**
    - C++:
11. Size **10240**
    - C++:

## Exercise 3

>Implement a block oriented algorithm that divides the matrices in blocks and uses the same sequence of computation as in 2, using C/C++.

>Register the processing time from 4096x4096 to 10240x10240 with intervals of 2048, for different block sizes (e.g. 128, 256, 512).

### Measurements

1. Size **4096**
    - Block size **128**:
    - Block size **256**:
    - Block size **512**:
2. Size **6144**
    - Block size **128**:
    - Block size **256**:
    - Block size **512**:
3. Size **8192**
    - Block size **128**:
    - Block size **256**:
    - Block size **512**:
4. Size **10240**
    - Block size **128**:
    - Block size **256**:
    - Block size **512**: