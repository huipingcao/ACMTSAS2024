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

labels = ['200', '400', '600', '800']

#### Construction Time
bfs_value = [35.164, 43.842, 89.917, 313.561]
node_value = [34.501, 38.733, 34.264, 89.184]

#### Index size
# bfs_value = [82, 116, 174, 349]
# node_value = [84, 98, 96, 88]

x = np.arange(len(labels))  # the label locations
width = 0.35  # the width of the bars

rectsBBS = ax.bar(x - width, [round(i, 1) for i in bfs_value], width, label='BFS', align='edge', alpha=.99)
rectsNode = ax.bar(x, [round(i, 1) for i in node_value], width, label='NODE', align='edge', hatch="\\",
                   alpha=.99)

ax.set_xlabel('cluster size')
ax.set_ylabel('Construction time (Sec.)')
# ax.set_ylabel('Index size (MB)')
ax.set_xticks(x)
ax.set_xticklabels(labels)
ax.legend( loc=0, handletextpad=0.1, labelspacing=.1)

# def autolabel(rects):
#     """Attach a text label above each bar in *rects*, displaying its height."""
#     for rect in rects:
#         height = rect.get_height()
#         ax.annotate('{}'.format(height),
#                     xy=(rect.get_x() + rect.get_width() / 2, height),
#                     xytext=(0, 3),  # 3 points vertical offset
#                     textcoords="offset points",
#                     ha='center', va='bottom', size=10, weight='bold')
#
#
# autolabel(rectsBBS)
# autolabel(rectsV1)
# autolabel(rectsV2)
# autolabel(rectsV3)

fig.tight_layout()

# plt.xlim(0 - 2.5 * width, len(x) - width)

# plt.show()
plt.savefig("""BFS_NODE_Construction_time_C9_NY_15K.pdf""")
