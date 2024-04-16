import os
import re
import sys
from collections import Counter
import numpy as np


def read_file(file_path):
    with open(file_path, 'r') as file:
        return file.readlines()

def extract_arrays(line):
    return re.findall(r'\((.*?)\)', line)

def calculate_matching_ratio(approx_arrays, bbs_arrays, base="bbs"):
    common_arrays_count = sum((Counter(approx_arrays) & Counter(bbs_arrays)).values())
    approx_total_lines = len(approx_arrays)
    bbs_total_lines = len(bbs_arrays)
    matching_ratio_approx_base = common_arrays_count / approx_total_lines if approx_total_lines > 0 else 0
    matching_ratio_bbs_base = common_arrays_count / bbs_total_lines if bbs_total_lines > 0 else 0
    return matching_ratio_approx_base if base == "approx" else matching_ratio_bbs_base


def main():
    gnn_backbone_folder = "/Users/hyingchen/PycharmProjects/graph/ICDE23/GNNquery/SkylineGNN_py/data/L_CAL_NONE/epoch100_query100_128_256_16_1_EmbedTrue_Transformer_ConLossTrue/20231121_152111"
    bbs_folder = "/Users/hyingchen/PycharmProjects/graph/ICDE23/GNNquery/SkylineGNN_py/data/L_CAL_NONE_l17_test_1000"

    base = "bbs" #"approx" #"bbs"
    gnn_backbone_files = os.listdir(gnn_backbone_folder)
    gnn_matching_ratio = []
    backbone_matching_ratio = []

    for file in sorted(gnn_backbone_files):
        if file.startswith("gnn_") and file.endswith(".log"):
            gnn_path = os.path.join(gnn_backbone_folder, file)
            bbs_file = file.replace("gnn", "bbs")
            bbs_path = os.path.join(bbs_folder, bbs_file)

            if os.path.exists(bbs_path):
                gnn_lines = read_file(gnn_path)
                bbs_lines = read_file(bbs_path)

                gnn_arrays = [extract_arrays(line) for line in gnn_lines]
                bbs_arrays = [extract_arrays(line) for line in bbs_lines]
                # print(file)
                # print(gnn_arrays[0])
                # print(bbs_arrays[0])
                # sys.exit(0)

                matched_count = sum(calculate_matching_ratio(gnn, bbs, base) for gnn, bbs in zip(gnn_arrays, bbs_arrays))

                total_lines = len(gnn_lines)
                matching_ratio = matched_count / total_lines if total_lines > 0 else 0
                gnn_matching_ratio.append(matching_ratio)
                print(f"Matching ratio for {file}: {matching_ratio:.2%}")

        elif file.startswith("backbone_") and file.endswith(".log"):
            backbone_path = os.path.join(gnn_backbone_folder, file)
            bbs_file = file.replace("backbone", "bbs")
            bbs_path = os.path.join(bbs_folder, bbs_file)

            if os.path.exists(bbs_path):
                backbone_lines = read_file(backbone_path)
                bbs_lines = read_file(bbs_path)

                backbone_arrays = [extract_arrays(line) for line in backbone_lines]
                bbs_arrays = [extract_arrays(line) for line in bbs_lines]
                # print(file)
                # print(backbone_arrays[0])
                # print(bbs_arrays[0])
                # sys.exit(0)

                matched_count = sum(calculate_matching_ratio(backbone, bbs, base) for backbone, bbs in zip(backbone_arrays, bbs_arrays))

                total_lines = len(backbone_lines)
                matching_ratio = matched_count / total_lines if total_lines > 0 else 0
                backbone_matching_ratio.append(matching_ratio)
                print(f"Matching ratio for {file}: {matching_ratio:.2%}")

    print("================ Ratio base: " + base + " ================")
    print(f"Avg matching ratio for gnn: {np.mean(gnn_matching_ratio):.2%}")
    print(f"Avg matching ratio for backbone: {np.mean(backbone_matching_ratio):.2%}")


if __name__ == "__main__":
    main()
