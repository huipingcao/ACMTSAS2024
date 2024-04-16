# %%
# http://www.dis.uniroma1.it/challenge9/download.shtml

import numpy as np
import os

area_name = "BAY"

base_path = os.path.expanduser('~') + "/Data/datasets/C9_"+area_name+"/"
node_path = base_path + "USA-road-d."+area_name+".co"
edge_path = base_path + "USA-road-d."+area_name+".gr"
new_node_file = base_path + "output/"+area_name+"_NodeInfo.txt"
new_edge_file = base_path + "output/"+area_name+"_SegInfo.txt"

output_folder = base_path + "output"

if not os.path.exists(output_folder):
    os.mkdir(output_folder)
    print("Created the output folder :  {}".format(output_folder))

if os.path.exists(new_node_file):
    os.remove(new_node_file)
    print("{} was removed".format(new_node_file))
else:
    print("{} The file does not exist".format(new_node_file))

if os.path.exists(new_edge_file):
    os.remove(new_edge_file)
    print("{} was removed".format(new_edge_file))
else:
    print("{} The file does not exist".format(new_edge_file))

content = []

# read the node information
with open(node_path) as f:
    content = f.readlines()

content = content[7:]
content = [x.strip() for x in content]


# %%
class node:
    def __init__(self, nodeid, lat, lng):
        self.id = nodeid
        self.lat = lat
        self.lng = lng


# %%
print("Sample of the format of the node : {} ".format(content[0]))

nodeList = {}
for line in content:
    node_id = (int)(line.split(" ")[1]) - 1
    node_lat = (float)(line.split(" ")[3]) / 1000000
    node_lng = (float)(line.split(" ")[2]) / 1000000
    # print("{} {} {}".format(node_id,node_lat,node_lng))
    nodeList[node_id] = node(node_id, node_lat, node_lng)

print("There are {} # of nodes ".format(len(nodeList)))

# %%
file = open(new_node_file, "w")
for node_id, node in nodeList.items():
    line = "{} {} {}\n".format(node.id, node.lat, node.lng)
    file.write(line)
file.close()

# %%
content = []
with open(edge_path) as f:
    content = f.readlines()

content = content[7:]
content = [x.strip() for x in content]

print("Sample of the format of the edges: {} ".format(content[0]))

# %%
mapping = set()
undirect_edges = 0
edges_number = 0
line_number = 0
file = open(new_edge_file, "w")
for line in content:
    src_id = (int)(line.split(" ")[1]) - 1
    dest_id = (int)(line.split(" ")[2]) - 1

    if line_number % 50000 == 0:
        print("{} ............".format(line_number))

    if (src_id, dest_id) not in mapping and (dest_id, src_id) not in mapping:
        mapping.add((src_id, dest_id))
        undirect_edges += 1
        travel_time = (int)(line.split(" ")[3])
        cost2 = np.random.randint(0, 100 + 1)
        cost3 = np.random.randint(0, 100 + 1)
        line = "{} {} {} {} {} \n".format(src_id, dest_id, travel_time, cost2, cost3)
        file.write(line)

    line_number += 1

file.close()
print("# of un-directional edges : {}     # of directed edges : {}  ".format(undirect_edges, line_number))