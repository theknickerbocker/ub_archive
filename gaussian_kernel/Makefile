all: a3.cpp
	nvcc -c a3_gpu.cu
	g++ -O3 -std=c++11 -c a3.cpp
#	g++ -o a3 a3.o a3_gpu.o -L/usr/local/cuda/lib -lcudart
	g++ -o a3 a3.o a3_gpu.o -L/util/common/cuda/cuda-8.0/lib64 -lcudart
	rm -rf *.o

clean:
	rm -rf a3 core.*
