#!/bin/bash

gcc -c -w stdlib.c
gcc -w stdlib.o testlib.c -o testlib

./testlib