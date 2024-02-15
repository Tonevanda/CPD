# Part 1 - Performance Evaluation of a single core

## Exercise 1

>Download the example file from moodle that contains the basic algorithm in C/C++ that multiplies two matrices, i.e. multiplies one line of the first matrix by each column of the second matrix (matrixproduct.cpp). Implement the same algorithm in another programming language (just one), such as JAVA, C#, Fortran, etc, of your choice.

>Register the processing time for the two languages of implementation, for input matrices from 600x600 to 3000x3000 elements with increments in both dimensions of 400. Use –O2 as optimization flag in C++.

### 600x600

For matrices of size 600x600:

- C++: 0.187 seconds
- Python: 0.231 seconds

### 3000x3000

For matrices of size 3000x3000:

- C++: 117.782 seconds
- Python: 39.563 seconds