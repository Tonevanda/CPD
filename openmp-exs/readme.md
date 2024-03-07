# OpenMP Exercises Week 4-6

## 1. Computing PI


### 1.1
- With n = 10⁵: <br>
    - PI = 3.14159265359816198
    - Error = -8.36886115962443e-12
- With n = 10⁶: <br>
    - PI = 3.14159265358976425
    - Error = 2.88657986402540701e-14
- With n = 10⁷: <br>
    - PI = PI = 3.14159265358973094
    - Error = 6.21724893790087663e-14
- With n = 10⁸: <br>
    - PI = 3.14159265359042639
    - Error = -6.33271213246189291e-13
- With n = 10⁹: <br>
    - PI = PI = 3.1415926535899712
    - Error = -1.78079773149875109e-13

### 1.2

### 1.3

## 2. Loop Scheduling
- #pragma omp for schedule (static, 4)

Number of threads: 4 <br>
Thread: 1   i= 4 <br>
Thread: 0   i= 0 <br>
Thread: 2   i= 8 <br>
Thread: 0   i= 1 <br>
Thread: 0   i= 2 <br>
Thread: 0   i= 3 <br>
Thread: 1   i= 5 <br>
Thread: 2   i= 9 <br>
Thread: 1   i= 7

Here we can see that it attributed to each thread 4 values sequencially. Thread 0 got 0,1,2 and 3, Threat 1 got 4,5,6 and 7 and Thread 2 got 8,9 and 10. Thread 2 only got 3 values for i because they were all given 4 values in sequence, so thread 2 got less and thread 3 got 0.

- #pragma omp for schedule (dynamic, 4)

Number of threads: 4 <br>
Thread: 1   i= 0 <br>
Thread: 3   i= 8 <br>
Thread: 2   i= 4 <br>
Thread: 1   i= 1 <br>
Thread: 2   i= 5 <br>
Thread: 1   i= 2 <br>
Thread: 3   i= 9 <br>
Thread: 1   i= 3 <br>
Thread: 2   i= 6 <br>
Thread: 2   i= 7



