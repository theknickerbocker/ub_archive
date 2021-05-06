#include <cuda.h>
#include <stdio.h>
#include <vector>

void printArray(const float* x, int n)
{
    printf("(");
    for (int i = 0; i < n; i++)
    {
        printf("%f, ", x[i]);
    }
    printf(")\n");
}


__global__
void f_h(const int n, const float h, const float *x, float *y, bool *run)
{
    *run = true;
    // int idx = blockIdx.x * blockDim.x + threadIdx.x;
    float coef = 1 / (n * h) * .3989422804;
    for (int j = 0; j < n; j++)
    {
        float sum = 0;
        float x_val = x[j];
        for (int i = 0; i < n; i++)
        {
            float val = (x_val-x[i]) / h;
            float k = exp(-(val * val) / 2);
            sum = sum + k;
        }
        y[j] = coef * sum;
    }
}

__host__
void gpuCall(int n, float h, const float *x_v, float *y_v)
{
    printf("START GPU CALL\n");
    
    float *x, *y;
    bool *run;
    cudaMallocManaged(&x, n*sizeof(float));
    cudaMallocManaged(&y, n*sizeof(float));
    cudaMallocManaged(&run, sizeof(bool));

    *run = false;

    for (int i = 0; i < n; i++)
    {
        x[i] = x_v[i];
    }

    //==============================================================
    printf("X before\n");
    printArray(x, n);
    printf("\n");

    printf("Y before\n");
    printArray(y, n);
    //==============================================================

    f_h<<<1, 1>>>(n, h, x, y, run);
    cudaDeviceSynchronize();

    printf("Did it run? %d\n", *run);

    //==============================================================
    printf("\n");
    printf("Y\n");
    printArray(y, n);
    //==============================================================

    cudaFree((float*)x);
    cudaFree(y);
    cudaFree(run);
}
