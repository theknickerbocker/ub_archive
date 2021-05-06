#include <iostream>

void printArray(const float* x, int n)
{
    std::cout << "(";
    for (int i = 0; i < n; i++)
    {
        std::cout << x[i] << ", ";
    }
    std::cout << ")" << std::endl;
}

void f_h(const int n, const float h, const float *x, float *y, bool *run, int idx)
{
    *run = true;
    float coef = 1 / (n * h) * .3989422804;
    float sum = 0;
    float x_val = x[idx];
    for (int i = 0; i < n; i++)
    {
        float val = (x_val-x[i]) / h;
        float k = exp(-(val * val) / 2);
        sum = sum + k;
    }
    y[idx] = coef * sum;
}

void gaussian_kde(int n, float h, const float *x_v, float *y_v)
{
    std::cout << "START GPU CALL" << std::endl;
    bool r = false;
    bool *run = &r;

    float *x, *y;

    x = new float[n];
    y = new float[n];

    for (int i = 0; i < n; i++)
    {
        x[i] = x_v[i];
    }
    //==============================================================
    std::cout << "X before" << std::endl;
    printArray(x, n);
    std::cout << std::endl;

    std::cout << "Y" << std::endl;
    printArray(y, n);
    //==============================================================

    for (int i = 0; i < n; i++) {
        f_h(n, h, x, y, run, i);
    }

    std::cout << "Did it run? " << r << std::endl;

    //==============================================================
    std::cout << "X" << std::endl;
    printArray(x, n);
    std::cout << std::endl;

    std::cout << "Y" << std::endl;
    printArray(y, n);
    //==============================================================

    delete x;
    delete y;
}
