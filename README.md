# Computação Paralela e Distribuída

Este repositório contém exercícios e apontamentos da cadeira de Computação Paralela e Distribuída

## Compilação e Execução

Para compilar e correr o programa, temos que correr o seguinte comando:

```sh
g++ -O2 matprod.cpp -o mm -lpapi
./mm
```

Caso dê erro do PAPI, é necessário correr o seguinte script para permitir que o PAPI consiga ter acesso aos contadores de performance de Linux: 

```sh
sudo sh -c 'echo -1 >/proc/sys/kernel/perf_event_paranoid'
```
