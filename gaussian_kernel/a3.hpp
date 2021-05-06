/*  Kevin
 *  Rathbun
 *  kevinrat
 */

#ifndef A3_HPP
#define A3_HPP

#include <cmath>

void f_h(const int n, const float h, const float *x, float *y);

void gpuCall(int n, float h, const float *x, float *y);

void gaussian_kde(int n, float h, const std::vector<float>& x, std::vector<float>& y) {
    gpuCall(n, h, x.data(), y.data());
} // gaussian_kde

#endif // A3_HPP
