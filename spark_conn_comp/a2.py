# Kevin
# Rathbun
# kevinrat

from pyspark import SparkContext, SparkConf
from pyspark.sql import SparkSession
import sys
import pprint

if len(sys.argv) != 2:
    print("Please enter the filename as an argument")
    sys.exit()

def to_list(a):
    return [a]

def append(a, b):
    a.append(b)
    return a

def extend(a, b):
    a.extend(b)
    return a

def large_map_func(edge):
    return [(edge[0], edge[1]), (edge[1], edge[0])]

def large_reduce_func(node):
    u = node[0]
    neighborhood = node[1]
    m = min(min(neighborhood), u)
    out = []
    for v in neighborhood:
        # print(str(u) + " -> (" + str(v) + ", " + str(m) + ")")
        if (u < v):
            out.append((v,m))
    return out

def small_map_func(edge):
    u = edge[0]
    v = edge[1]
    if (v <= u):
        return [(v,u)]
    else:
        return [(u,v)]

def small_reduce_func(node):
    u = node[0]
    neighborhood = node[1]
    # print(node)
    m = min(min(neighborhood), u)
    out = []
    for v in neighborhood:
        if(v < u):
            if(v != m):
                # print(str(u) + " -> (" + str(v) + ", " + str(m) + ")")
                out.append((v,m))
            else:
                out.append((u,v))
    return out

def get_neighbors(u, adj_list):
    return adj_list.filter(lambda x: x[0] == u).values().flatMap(lambda x: x)

def get_ext_neighbors(u, adj_list):
    return get_neighbors(u, adj_list).append(u)

def read_edges(filename, session):
    return session.sparkContext.textFile(filename).map(lambda x: tuple([int(y) for y in x.split(" ")]))

def get_adj_list(edges):
    return edges.combineByKey(to_list, append, extend)

def small_map(edges):
    return edges.flatMap(small_map_func)
    
def large_map(edges):
    return edges.flatMap(large_map_func)

def large_reduce(adj_list):
    return adj_list.flatMap(large_reduce_func).distinct()

def small_reduce(adj_list):
    return adj_list.flatMap(small_reduce_func).distinct()

def map_adj_node(node):
    u = node[0]
    neighborhood = set(node[1])
    out = [(str(u) + " " + str(u))]
    for v in neighborhood:
        out.append((str(v) + " " + str(u)))
    return out

def write_connected(adj_list):
    connected = adj_list.flatMap(map_adj_node)
    connected.saveAsTextFile("a2out")

def reduce_components(filename):
    spark = SparkSession\
        .builder\
        .appName("A2")\
        .getOrCreate()
    edges = read_edges(filename, spark)
    # print("START")
    pp = pprint.PrettyPrinter(indent=2)
    # print(edges.collect())

    graph = large_map(edges)
    adj_list = get_adj_list(graph)
    graph = large_reduce(adj_list)

    for i in range(20):
        # print("\nSTEP " + str(i))
        if i % 2:
            graph = large_map(graph)
            # print("L GRAPH")
            # pp.pprint(graph.collect())
            adj_list = get_adj_list(graph)
            # print("ADJ LIST")
            # pp.pprint(adj_list.collect())
            graph = large_reduce(adj_list)

        else:
            graph = large_map(graph)
            # print("S GRAPH")
            # pp.pprint(graph.collect())
            adj_list = get_adj_list(graph)
            # print("ADJ LIST")
            # pp.pprint(adj_list.collect())
            graph = small_reduce(adj_list)
            # print("S GRAPH")
            # pp.pprint(graph.collect())
        

    # print("\n\nOUT")
    graph = small_map(graph)
    # print("S GRAPH")
    # pp.pprint(graph.collect())
    adj_list = get_adj_list(graph)
    # pp.pprint(adj_list.collect())

    write_connected(adj_list)
    spark.stop()

reduce_components(sys.argv[1])

