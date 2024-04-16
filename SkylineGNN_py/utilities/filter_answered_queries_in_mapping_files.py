import sys
import os
from os import listdir
from os.path import isfile, join

from readData import readLogs
from tqdm import tqdm




# mapping_files_path = "/home/hchen/IntelliJProjects/java_SkylineGNN/Data/new_mapped/C9_NY_NONE_5K/query500/"
# mapping_files_path = "/home/hchen/IntelliJProjects/java_SkylineGNN/Data/new_mapped/C9_NY_NONE_5K_TSP/query500/"
# mapping_files_path = "/home/hchen/IntelliJProjects/java_SkylineGNN/Data/new_mapped/C9_NY_NONE_15K/"
# mapping_files_path = "/home/hchen/IntelliJProjects/java_SkylineGNN/Data/new_mapped/C9_NY_NONE_15K_TSP/"
mapping_files_path = "/home/hchen/IntelliJProjects/java_SkylineGNN/Data/new_mapped/L_CAL_NONE/"
# mapping_files_path = "/home/hchen/IntelliJProjects/java_SkylineGNN/Data/new_mapped/L_CAL_NONE_TSP/"

dir_list = os.listdir(mapping_files_path)

# saved_path = "/home/hchen/IntelliJProjects/java_SkylineGNN/Data/new_mapped/C9_NY_NONE_5K/answered100/"
# saved_path = "/home/hchen/IntelliJProjects/java_SkylineGNN/Data/new_mapped/C9_NY_NONE_5K_TSP/answered100/"
# saved_path = "/home/hchen/IntelliJProjects/java_SkylineGNN/Data/new_mapped/C9_NY_NONE_15K/"
# saved_path = "/home/hchen/IntelliJProjects/java_SkylineGNN/Data/new_mapped/C9_NY_NONE_15K_TSP/"
saved_path = "/home/hchen/IntelliJProjects/java_SkylineGNN/Data/new_mapped/L_CAL_NONE/"
# saved_path = "/home/hchen/IntelliJProjects/java_SkylineGNN/Data/new_mapped/L_CAL_NONE_TSP/"


print(f'Saved in folder: {saved_path}')
print('============================================================================\n')

for f in dir_list:
    f_full = mapping_files_path + f
    saved_f = "answered100_" + f
    saved_f_full = saved_path + saved_f

    # print(f)
    # print(saved_f_full)
    # sys.exit(0)

    print(f'Filtering original file: {f}.................')
    print(f'Saved in file: {saved_f}\n')

    cnt = 0
    with open(f_full, 'r') as fr:
        lines = fr.readlines()

        for l in lines:
            if cnt == 100: break
            if '[]' in l:
                continue
            # print(l)
            cnt += 1

            with open(saved_f_full, "a+") as sf:
                sf.write(l)

    sf.close()
    fr.close()
    # sys.exit(0)
