#include "mpi.h"
#include <stdio.h>

int main(int argc, char* argv[])
{
    int processes, rank, len;
    char hostname[MPI_MAX_PROCESSOR_NAME];

    MPI_Init(&argc, &argv);

    MPI_Comm_size(MPI_COMM_WORLD, &processes);

    MPI_Comm_rank(MPI_COMM_WORLD, &rank);

    MPI_Get_processor_name(hostname, &len);

    printf ("Hello from task %d on %s!\n", rank, hostname);
    if (rank == 0)
       printf("MASTER: Number of MPI tasks is: %d\n",processes);

    MPI_Finalize();
}