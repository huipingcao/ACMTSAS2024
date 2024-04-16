from os import listdir
from os.path import isfile, join

from ..utilities.readData import readLogs
from tqdm import tqdm

train_paths_folder = "/home/gqxwolf/shared_git/BackboneIndex/Data/results_bbs/C9_NY_1K/training_GNN"

onlyfiles = [join(train_paths_folder, f) for f in listdir(train_paths_folder) if isfile(join(train_paths_folder, f))]

nodes = set()

for f in onlyfiles:
    # print(f)
    p = readLogs(f)
    for n in p.nodes:
        nodes.add(n)
    print(p)

print(len(nodes))