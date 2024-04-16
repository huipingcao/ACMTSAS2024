import numpy as np
import matplotlib.pyplot as plt
import matplotlib
import os

font = {'family': 'Arial',
        'weight': 'normal',
        'size': 30}
matplotlib.rc('font', **font)
axis_font = {'fontname': 'Arial', 'size': '35'}

fig, ax = plt.subplots()
fig.set_figheight(8)
fig.set_figwidth(10)

folder_name = os.path.dirname(__file__)

with open('{}/sub_distribution.data'.format(folder_name)) as infile:
    trend = np.loadtxt(infile)

labels = ["5K", "10K", "15K"]
x = np.arange(len(labels)) - 0.5  # the label locations
print(x)
width = 0.25  # the width of the bars

g1_x = [x[0] + i * width - 3 * width + 0.1 for i in range(6)]
print(g1_x)
g2_x = [g1_x[-1] + i * width + 0.5 for i in range(6)]
print(g2_x)
g3_x = [g2_x[-1] + i * width + 0.5 for i in range(5)]
print(g3_x)

rects_g1_v1 = ax.bar(g1_x, trend[:, 1], width, label='5K',
                     align='edge', alpha=.99, linewidth=1, edgecolor="black")

rects_g1_v2 = ax.bar(g2_x, trend[:, 2], width,
                     label='10K', align='edge', alpha=.99, linewidth=1, edgecolor="black", hatch="\\")
rects_g1_v3 = ax.bar(g3_x, trend[0:5, 3], width, label='15k',
                     align='edge', alpha=.99, linewidth=1, edgecolor="black", hatch="/")

ax.set_xlabel('degree')
ax.set_ylabel('Distribution Of Degree (%)')
ax.xaxis.set_label_coords(x[1], -0.06)
# ax.set_title('Degree distribution', pad=15)
ax.legend(fontsize="x-small", loc=[0.6, 0.8], handletextpad=0.1, labelspacing=.1)
plt.xlim(0 - 5 * width, g3_x[-1]+width)


def autoticks(rects):
    """Attach a text label above each bar in *rects*, displaying its height."""
    for i in range(len(rects)):
        rect = rects[i]
        ax.annotate('{}'.format(i + 1),
                    xy=(rect.get_x() + rect.get_width() / 2, 0),
                    xytext=(0, -25),  # 3 points vertical offset
                    textcoords="offset points",
                    ha='center', va='bottom', size=20, weight='bold')


autoticks(rects_g1_v1)
autoticks(rects_g1_v2)
autoticks(rects_g1_v3)

ax.tick_params(
    axis='x',  # changes apply to the x-axis
    which='both',  # both major and minor ticks are affected
    bottom=False,  # ticks along the bottom edge are off
    top=False,  # ticks along the top edge are off
    labelbottom=False,  # labels along the bottom edge are off
)
# plt.xticks(fontsize=16)
# plt.show()
plt.savefig("""sub_graph_degree_distribution.pdf""")
