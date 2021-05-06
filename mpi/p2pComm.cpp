#include "mpi.h"
#include <iostream>

int main(int argc, char* argv[])
{
    int rank, numtasks, source, dest;
    int send[2];
    int value[2];
    MPI_Status stat;

    MPI_Init(&argc, &argv);

    MPI_Comm_size(MPI_COMM_WORLD, &numtasks);

    MPI_Comm_rank(MPI_COMM_WORLD, &rank);

    source = (rank - 1) % numtasks;
    dest = (rank + 1) % numtasks;

    send[0] = rank;
    send[1] = 16;
    MPI_Allreduce(&send, &value, 2, MPI_INT, MPI_MAX, MPI_COMM_WORLD);

    std::cout << "RANK: " << rank << " VALUE: " << value << std::endl;

    MPI_Finalize();
}
