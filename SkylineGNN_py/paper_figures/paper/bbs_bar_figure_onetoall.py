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

labels = ['5K', '10K', '15K', '200K']

# euclidean_distance 15k
v1_method_value = [1.0255, 0.2725, 1.0962]
v2_method_value = [1.4177, 1.5172, 1.2829]
v3_method_value = [1.0465, 1.3023, 1.3657]

x = np.arange(len(labels))  # the label locations
width = 0.3  # the width of the bars

rectsV1 = ax.bar(x - width, v1_method_value, width,
                 label='none', align='center', hatch="\\", alpha=.99)
rectsV2 = ax.bar(x, v2_method_value, width,
                 label='each', align='center', hatch="/", alpha=.99)
rectsV3 = ax.bar(x + width, [round(i, 1) for i in v3_method_value], width,
                 label='normal', align='center', hatch="-", alpha=.99)

# Add some text for labels, title and custom x-axis tick labels, etc.
ax.set_xlabel('cluster size')
ax.set_ylabel('Goodness (Euclid sim.)')
# ax.set_ylabel('Goodness (cosine sim.)')
ax.set_xticks(x)
ax.set_xticklabels(labels)
ax.legend(fontsize="x-small", loc=0, handletextpad=0.1, labelspacing=.1)

fig.tight_layout()

plt.xlim(0 - 2 * width, len(x) - width)
# plt.ylim(0.80, 0.98)
plt.ylim(0.2, 1.7)

# plt.show()
# plt.savefig("""cosine_C9_NY_15K.pdf""")
plt.savefig("""euclidean_distance_C9_NY_15K.pdf""")
