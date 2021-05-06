import sys

def read_file(filename):
    graph = {}
    node = 0
    with open(filename, 'r') as input_file:
        for line in input_file:
            adj_nodes = [int(neighbor) for neighbor in line.split()]
            graph[node] = adj_nodes
            node += 1
    return graph

def write_graph(filename, graph):
    edges = set([])
    with open(filename, 'w') as output_file:
        for u in graph:
            for v in graph[u]:
                edge = tuple(sorted([u,v]))
                if edge not in edges:
                    output_file.write(str(u) + " " + str(v) +"\n")
                    edges.add(edge)

filename = sys.argv[1]
graph = read_file(filename)
write_graph("A2_" + filename, graph)