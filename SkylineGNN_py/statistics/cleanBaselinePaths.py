from os import listdir
from os.path import join, isfile

training_folder = "/home/gqxwolf/shared_git/BackboneIndex/Data/results_bbs/C9_NY_5K/training_GNN"
log_folder = "/home/gqxwolf/shared_git/BackboneIndex/Data/logs"

skyline_paths = [f for f in listdir(training_folder) if isfile(join(training_folder, f))]

key_list = []
for p in skyline_paths:
    k = (p.split("_")[1], p.split("_")[2])
    # print(k)
    key_list.append(k)

# training for 10k
# list_performance_logs = ["compareBBSBackbone_C9_NY_10K_10000_training-[2021-03-18~23-54-10].log",
#                          "compareBBSBackbone_C9_NY_10K_10000_training-[2021-03-19~00-49-29].log",
#                          "compareBBSBackbone_C9_NY_10K_10000_training-[2021-03-19~08-27-51].log",
#                          "compareBBSBackbone_C9_NY_10K_10000_training-[2021-03-19~09-55-48].log",
#                          "compareBBSBackbone_C9_NY_10K_10000_training-[2021-03-19~10-38-05].log",
#                          "compareBBSBackbone_C9_NY_10K_10000_training-[2021-03-19~12-03-37].log",
#                          "compareBBSBackbone_C9_NY_10K_10000_training-[2021-03-19~13-16-12].log",
#                          "compareBBSBackbone_C9_NY_10K_10000_training-[2021-03-19~14-48-31].log"]

# c9_ny_1k
# list_performance_logs = ["compareBBSBackbone_C9_NY_1K_100000_training-[2021-01-29~16-36-20].log"]
# c9_ny_2k
# list_performance_logs = ["compareBBSBackbone_C9_NY_2K_10000_training-[2021-03-15~22-27-43].log"]
# c9_ny_5k
list_performance_logs = ["compareBBSBackbone_C9_NY_5K_10000_training-[2021-03-22~16-33-19].log"]


total_query_time = 0.0
total_path = 0.0
total_query_coverage = 0.0

finished_query = 0

for index, k in enumerate(key_list):
    query_time = 9999999
    query_path = 0
    query_coverage = 1.0
    log_key = ()
    for log in list_performance_logs:
        with open(join(log_folder, log)) as fp:
            lines = fp.readlines()
            for l in lines:
                line_log = l.rstrip("\n")
                if "Executing the query from" in line_log:
                    infos = line_log.split(" ")
                    log_key = (infos[7], infos[9])

                if "Query time" in line_log:
                    infos = line_log.split(" ")
                    if k == log_key:
                        query_time = float(infos[6])
                if "finnal result" in line_log:
                    infos = line_log.split(" ")
                    if k == log_key:
                        query_path = float(infos[10])
                if "coverage" in line_log:
                    infos = line_log.split(" ")
                    if k == log_key:
                        query_coverage = float(infos[8])
    if query_time != 9999999:
        print(index, k[0], k[1], query_time, query_path, query_coverage)
        finished_query += 1

        total_query_time += query_time
        total_path += query_path
        total_query_coverage += query_coverage
    else:
        print(index, k[0], k[1], query_time, query_path, query_coverage)

print("========================================================")
print("{} {:.2f} {:.2f} {:.4f}".format(finished_query, total_query_time / finished_query, total_path / finished_query,  total_query_coverage / finished_query))
