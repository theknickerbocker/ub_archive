#include <cuda.h>
#include <iostream>
#include <vector>

void printArray(const float* x, int n)
{
    std::cout << "(";
    for (int i = 0; i < n; i++)
    {
        std::cout << x[i] << ", ";
    }
    std::cout << ")" << std::endl;
}

// My attempt at using shared mem among blocks. Runs slightly slower than my naïve
// algorithm did but I like this more as it is at least an attempt at optimization
// even though it runs much slower than it should had it.
__global__
void f_h(const int n, const float h, const float *x, float *y, int memSize)
{
    extern __shared__ float x_reg[];
   
    const int idx = blockIdx.x * blockDim.x + threadIdx.x;
    const float coef = 1 / (n * h) * .3989422804;
    float sum = 0;
    float x_val = x[idx];

    for (int i = 0; i < n; i += memSize)
    {
        for (int j = 0; j < memSize; j += blockDim.x)
        {
            if(i + j + threadIdx.x < n)
            {
                x_reg[j + threadIdx.x] = x[j + i + threadIdx.x];
            }
        }
        __syncthreads();

        if (idx >= n)
        {
            return;
        }
        
        for (int k = 0; k < memSize && k+i < n; k++)
        {
            float val = (x_val-x_reg[k]) / h;
            float k_x = exp(-(val * val) / 2);
            sum = sum + k_x;
        }
    }
    y[idx] = coef * sum;
}

void gpuCall(int n, float h, const float *x_v, float *y_v)
{
    int arrSize = n*sizeof(float);

    float *x, *y;
    cudaMalloc(&x, arrSize);
    cudaMalloc(&y, arrSize);

    cudaMemcpy(x, x_v, arrSize, cudaMemcpyHostToDevice);
    cudaMemcpy(y, y_v, arrSize, cudaMemcpyHostToDevice);

    int blockSize = 256;
    int numBlocks = (n + blockSize - 1) / blockSize;
    int memSize = blockSize * 4;

    f_h<<<numBlocks, blockSize, memSize * sizeof(float)>>>(n, h, x, y, memSize);
    cudaDeviceSynchronize();

    cudaMemcpy(y_v, y, arrSize, cudaMemcpyDeviceToHost);

    cudaFree(x);
    cudaFree(y);
}
