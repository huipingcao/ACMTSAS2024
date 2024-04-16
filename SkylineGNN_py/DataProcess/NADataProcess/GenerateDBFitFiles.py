import os
import numpy as np

area_name = "CAL"

base_path = os.path.expanduser('~') + "/mydata/projectData/BackBone/data/dataset/LFF_" + area_name + "/"
output_folder = base_path + "output/"
node_path = base_path + area_name + ".cnode"
edge_path = base_path + area_name + ".cedge"
new_node_file = output_folder + area_name + "_NodeInfo.txt"
new_edge_file = output_folder + area_name + "_SegInfo.txt"

if not os.path.exists(output_folder):
    os.mkdir(output_folder)
    print("Created the output folder :  {}".format(output_folder))

if os.path.exists(new_node_file):
    os.remove(new_node_file)
    print("{} was removed".format(new_node_file))
else:
    print("{} The file does not exist and create it ".format(new_node_file))

if os.path.exists(new_edge_file):
    os.remove(new_edge_file)
    print("{} was removed".format(new_edge_file))
else:
    print("{} The file does not exist and create it ".format(new_edge_file))

content = []

# read the node information
with open(node_path) as f:
    content = f.readlines()

print("There are {} number of nodes ".format(len(content)))


class node:
    def __init__(self, nodeid, lat, lng):
        self.id = nodeid
        self.lat = lat
        self.lng = lng


nodeList = []
for line in content:
    node_id = (int)(line.split(" ")[0])
    node_lat = (float)(line.split(" ")[1]) / 100
    node_lng = (float)(line.split(" ")[2]) / 100
    # print("{} {} {}".format(node_id, node_lat, node_lng))
    nodeList.append(node(node_id, node_lat, node_lng))

file = open(new_node_file, "w")
for node in nodeList:
    line = "{} {} {}\n".format(node.id, node.lat, node.lng)
    file.write(line)
file.close()

content = []
with open(edge_path) as f:
    content = f.readlines()

content = [x.strip() for x in content]
print(content[0])
# %%
mapping = set()
undirect_edges = 0
edges_number = 0
line_number = 0
file = open(new_edge_file, "w")
for line in content:
    src_id = int(line.split(" ")[1])
    dest_id = int(line.split(" ")[2])

    if line_number % 50000 == 0:
        print("{} ............".format(line_number))

    if (src_id, dest_id) not in mapping and (dest_id, src_id) not in mapping:
        mapping.add((src_id, dest_id))
        undirect_edges += 1
        travel_time = float(line.split(" ")[3])
        cost2 = np.random.randint(0, 100 + 1)
        cost3 = np.random.randint(0, 100 + 1)
        line = "{} {} {} {} {} \n".format(src_id, dest_id, travel_time, cost2, cost3)
        file.write(line)

    line_number += 1

file.close()
print("# of un-directional edges : {}     # of directed edges : {}  ".format(undirect_edges, line_number))
