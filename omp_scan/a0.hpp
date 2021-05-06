/*  Kevin
 *  Rathbun
 *  kevinrat
 */

#ifndef A0_HPP
#define A0_HPP
#include <omp.h>

#define MIN_SIZE 100

void print_vector(std::vector<int> path) {
    for (auto i = path.begin(); i != path.end(); ++i)
    std::cout << *i << ' ';
    std::cout << std::endl;
}

void print_array(const int* arr, int n) {
    for (int i = 0; i < n; i++)
    {
        std::cout << arr[i] << ' ';
    }
    std::cout << std::endl;
}

//Sequential  prefix
template <typename T, typename Op>
void loc_scan(int start, int end, const T *in, T *out, Op op)
{
    T temp = in[start];
    for (int i = start; i < end - 1; i++)
    {
        out[i] = temp;
        temp = op(in[i+1], temp);
    }
    out[end - 1] = temp;
}

template <typename T, typename Op>
void loc_bcast(int val, int start, int end, const T *in, T *out, Op op)
{
    for (int i = start; i < end; i++) {
        out[i] = op(in[i],val);
    }
}

std::vector<int> partition_data(int nthreads, int vals_per_pe, int vals_extra)
{
    std::vector<int> partition(nthreads, vals_per_pe);
    int* partition_data = partition.data();
    for (int i = 0; i < vals_extra; i++) {
        partition_data[i] = partition_data[i] + 1;
    }
    loc_scan(0, nthreads, partition_data, partition_data, std::plus<int>());
    return partition;
}

int get_thread_count() {
    int nthreads;
    
    #pragma omp parallel
    {
        int tid = omp_get_thread_num();
        if (tid == 0)
        {
            nthreads = omp_get_num_threads();
        }
    }

    return nthreads;
}

//The good one
template <typename T, typename Op>
void p_prefix_2(int n, const T *in, T *out, Op op)
{    
    int nthreads = omp_get_max_threads();

    int vals_per_pe = n/nthreads;
    int vals_extra = n % nthreads;

    //If size of n is small then do sequential prefix
    if (n <= MIN_SIZE) {
        loc_scan(0, n, in, out, op);
        return;
    }

    //Find even partitioning of data between threads
    std::vector<int> partitions = partition_data(nthreads, vals_per_pe, vals_extra);

    std::vector<int> offsets(nthreads);
    int* offsets_data = offsets.data();
    int tid;
    int end;
    #pragma omp parallel shared(in, out, offsets_data) private(tid, end)
    {
        //Sequential prefix of partitioned values in parallel
        tid = omp_get_thread_num();
        end = partitions[tid];
        if (tid == 0) {
            loc_scan(0, end, in, out, op);
        }
        else
        {
            int start = partitions[tid-1];
            loc_scan(start, end, in, out, op);
        }
        offsets[tid] = out[end - 1];

        #pragma omp barrier
        
        //Sequential prefix of local last element
        if (tid == 0)
        {
            loc_scan(0, nthreads,offsets_data, offsets_data, op);
        }

        #pragma omp barrier

        //Broadcast of prefix of previous values
        if (tid != 0) {
            int start = partitions[tid-1];
            end = partitions[tid];
            int offset = offsets_data[tid-1];
            loc_bcast(offset, start, end, out, out, op);
        }
    }    
}

template <typename T, typename Op>
void omp_scan(int n, const T *in, T *out, Op op)
{
    // std::cout << "INPUT" << std::endl;
    // print_array(in, n);
    // std::cout << std::endl;

    // loc_scan(0, n, in, out, op);
    // p_prefix_1(n, in, out, op);
    p_prefix_2(n, in, out, op);

    // std::cout << "OUTPUT:" << std::endl;
    // print_array(out, n);
    // std::cout << std::endl;
} // omp_scan


//Baaaad
// template <typename T, typename Op>
// void p_prefix_1(int n, const T *x, T *s, Op op)
// {
//     s[0] = x[0];
//     if (n <= 1)
//     {
//         return;
//     }
//     std::vector<T> y(n);
//     std::vector<T> z(n);
//     int i;
//     #pragma omp parallel shared(x, y) private(i)
//     {
//         #pragma omp for schedule(static) nowait
//         for (i = 0; i < n / 2; i++)
//         {
//             y[i] = op(x[2 * i], x[2 * i + 1]);
//         }
//     }
//     p_prefix_1(n / 2 , y.data(), z.data(), op);

//     #pragma omp parallel shared(s, x, z) private(i)
//     {
//         #pragma omp for schedule(static) nowait
//         for (i = 1; i < n; i++)
//         {
//             if (i % 2 == 0)
//             {
//                 s[i] = op(z[i / 2 - 1], x[i]);
//             }
//             else
//             {
//                 s[i] = z[(i - 1) / 2];
//             }
//         }
//     }
// }
#endif // A0_HPP
