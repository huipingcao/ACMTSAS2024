import matplotlib
import matplotlib.pyplot as plt
import numpy as np

font = {'family': 'Arial', 'weight': 'normal', 'size': 30}
matplotlib.rc('font', **font)
axis_font = {'fontname': 'Arial', 'size': '35'}

# graph = "C9_NY_5K"

fig, ax = plt.subplots()
fig.set_figheight(8)
fig.set_figwidth(10)

labels = ['CORR', 'ANTI', 'INDE']

# # query time NY
# BBS_query_time = [3749.193, 24352.928, 8876.18]
# Backbone_query_time = [421.3, 434.636, 429.36]


# query time BAY
# BBS_query_time = [1636.507, 27437.443, 8384.04]
# Backbone_query_time = [408.773, 466.729, 585.6]

# # #Goodness (Cosine and ED) NY
# BBS_query_time = [0.835, 0.872, 0.876]
# Backbone_query_time = [1.49, 1.199, 1.239]

# #Goodness (Cosine and ED) BAY
BBS_query_time = [0.838, 0.894, 0.877]
# Backbone_query_time = [1.196, 0.802, 1.126]


x = [i*0.5 for i in np.arange(len(labels))]  # the label locations
width = 0.3  # the width of the bars

# rectsBBS = ax.bar(x - width, [round(i, 3) for i in BBS_query_time],
#                   width, label='BBS', align='edge', alpha=.99)
# rectsBackbone = ax.bar(x, [round(i, 3) for i in Backbone_query_time], width,
#                        label='Backbone', align='edge', hatch="\\", alpha=.99)

rectsBBS = ax.bar(x, [round(i, 3) for i in BBS_query_time],
                  width, label='Cosine Sim.', align='center', alpha=.99)
# rectsBackbone = ax.bar(x, [round(i, 3) for i in Backbone_query_time], width,
#                        label='ED', align='edge', hatch="\\", alpha=.99)

# Add some text for labels, title and custom x-axis tick labels, etc.
# ax.set_xlabel('cluster size')
# ax.set_ylabel('Query time (ms)')
ax.set_ylabel('Goodness (Cosine Sim.)')
# ax.set_xlabel('# of skyline paths')
# ax.set_title(
#     'Comparison on {}'.format(graph))
ax.set_xticks(x)
ax.set_xticklabels(labels)
ax.legend(fontsize="medium", loc=0, handletextpad=0.1, labelspacing=.1)
ax.set_ylim(0.7,1)
plt.xlim(-width, x[-1]+width)
# plt.ticklabel_format(axis="y", style="sci", scilimits=(0, 0))


# def autolabel(rects):
#     """Attach a text label above each bar in *rects*, displaying its height."""
#     for rect in rects:
#         height = rect.get_height()
#         ax.annotate('{}'.format(height),
#                     xy=(rect.get_x() + rect.get_width() / 2, height),
#                     xytext=(0, 3),  # 3 points vertical offset
#                     textcoords="offset points",
#                     ha='center', va='bottom', size=20, weight='bold')


# autolabel(rectsBBS)
# autolabel(rectsBackbone)
# autolabel(rectsV2)
# autolabel(rectsV3)

fig.tight_layout()

# plt.xlim(0 - 2.5 * width, len(x) - width)

# plt.show()
# plt.savefig("""Query_time_anti_corr_comparison_BAY_20K.pdf""")
plt.savefig("""Goodness_anti_corr_comparison_BAY_20K.pdf""")