import matplotlib
import matplotlib.pyplot as plt
import numpy as np

font = {'family': 'Arial', 'weight': 'normal', 'size': 30}
matplotlib.rc('font', **font)
axis_font = {'fontname': 'Arial', 'size': '35'}

graph = "C9_NY_5K"

fig, ax = plt.subplots()
fig.set_figheight(8)
fig.set_figwidth(10)

labels = ['200', '400', '600']

# skyline paths 5k
# bbs_value = [204.32, 204.32, 204.32]
# v1_method_value = [5.55, 61.4833, 39.63]
# v2_method_value = [3.4433, 9.7793, 11.91]
# v3_method_value = [5.6433, 7.5351, 18.7633]

# # skyline paths 10k
# bbs_value = [1012.203, 1012.203, 1012.203]
# v1_method_value = [13.193, 12.39, 102.24]
# v2_method_value = [9.873, 13.28, 85.36]
# v3_method_value = [13.077, 12.84, 25.753]
#
# # skyline paths 15k
# bbs_value = [277.8467, 277.8467, 277.8467]
# v1_method_value = [4.8833, 172.3133, 5.54]
# v2_method_value = [3.68, 3.66, 7.02]
# v3_method_value = [4.8233, 3.9267, 4.7652]
#
# query time 5k
bbs_value = [928, 928, 928]
v1_method_value = [441, 1333, 1051]
v2_method_value = [466, 413, 424]
v3_method_value = [425, 429, 432]
#
# query time 10k
# bbs_value = [47.771, 47.771, 47.771]
# v1_method_value = [0.57, 0.467, 52.072]
# v2_method_value = [0.439, 0.435, 0.999]
# v3_method_value = [0.464, 0.432, 0.955]

x = np.arange(len(labels))  # the label locations
width = 0.2  # the width of the bars

rectsBBS = ax.bar(x - 2 * width, [round(i, 1) for i in bbs_value],
                  width, label='BBS', align='edge', alpha=.99)
rectsV1 = ax.bar(x - width, [round(i, 1) for i in v1_method_value], width,
                 label='none', align='edge', hatch="\\", alpha=.99)
rectsV2 = ax.bar(x, [round(i, 1) for i in v2_method_value], width,
                 label='each', align='edge', hatch="/", alpha=.99)
rectsV3 = ax.bar(x + width, [round(i, 1) for i in v3_method_value], width,
                 label='normal', align='edge', hatch="-", alpha=.99)

# Add some text for labels, title and custom x-axis tick labels, etc.
ax.set_xlabel('cluster size')
ax.set_ylabel('Query time (ms)')
# ax.set_ylabel('# of skyline paths')
# ax.set_title(
#     'Comparison on {}'.format(graph))
ax.set_xticks(x)
ax.set_xticklabels(labels)
ax.legend(fontsize="medium", loc=0, handletextpad=0.1, labelspacing=.1)


# def autolabel(rects):
#     """Attach a text label above each bar in *rects*, displaying its height."""
#     for rect in rects:
#         height = rect.get_height()
#         ax.annotate('{}'.format(height),
#                     xy=(rect.get_x() + rect.get_width() / 2, height),
#                     xytext=(0, 3),  # 3 points vertical offset
#                     textcoords="offset points",
#                     ha='center', va='bottom', size=10, weight='bold')


# autolabel(rectsBBS)
# autolabel(rectsV1)
# autolabel(rectsV2)
# autolabel(rectsV3)

fig.tight_layout()

plt.xlim(0 - 2.5 * width, len(x) + width)
ax.ticklabel_format(axis="y", style="sci", scilimits=(0,0))


# plt.show()
plt.savefig("""query_time_C9_NY_5K_no_annotation.pdf""")
# plt.savefig("""skyline_paths_C9_NY_15K_new.pdf""")
