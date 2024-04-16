from os import listdir
from os.path import join, isfile

# training_folder = "/home/gqxwolf/shared_git/BackboneIndex/Data/results_bbs/C9_NY_1K/training_GNN"
# training_folder = "/home/gqxwolf/shared_git/BackboneIndex/Data/results_bbs/C9_NY_2K/training_GNN"
# training_folder = "/home/gqxwolf/shared_git/BackboneIndex/Data/results_bbs/C9_NY_5K/training_GNN"
training_folder = "/home/gqxwolf/shared_git/BackboneIndex/Data/results_bbs/C9_NY_5K_1/training_GNN"

log_folder = "/home/gqxwolf/shared_git/BackboneIndex/Data/logs"

# c9_ny_1k
# list_performance_logs = ["compareBBSBackbone_C9_NY_1K_100000_training-[2021-01-29~16-36-20].log"]
# c9_ny_2k
# list_performance_logs = ["compareBBSBackbone_C9_NY_2K_10000_training-[2021-03-15~22-27-43].log"]
# c9_ny_5k
# list_performance_logs = ["compareBBSBackbone_C9_NY_5K_10000_training-[2021-03-22~16-33-19].log"]
list_performance_logs = ["compareBBSBackbone_C9_NY_5K_1_10000_training-[2021-07-28~16-56-11].log"]



# c9_ny_10k

skyline_paths = [f for f in listdir(training_folder) if isfile(join(training_folder, f))]

coverage_map = {}
for p in skyline_paths:
    k = (p.split("_")[1], p.split("_")[2]) #source node ID, destination node ID
    coverage_map[k] = 0

for log in list_performance_logs:
    with open(join(log_folder, log)) as fp:
        lines = fp.readlines()
        for l in lines:
            line_log = l.rstrip("\n")
            if "coverage" in line_log:
                infos = line_log.split(" ")
                src = infos[3].split("|")[1].split(">")[0]
                dest = infos[3].split("|")[1].split(">")[1]
                key = (src, dest)
                if key in coverage_map.keys():
                    if coverage_map[key] == 0:
                        coverage_map[key] = float(infos[-1])

c = 0
sum_coverage = 0
for index, key in enumerate(coverage_map):
    c += 1
    coverage = coverage_map[key]
    sum_coverage += coverage
    print(index, key[0], key[1], coverage_map[key])

print(c, "{:2.5f}".format(sum_coverage/c))