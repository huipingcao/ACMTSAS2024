import matplotlib
import matplotlib.pyplot as plt
import numpy as np
from matplotlib.ticker import FormatStrFormatter

font = {'family': 'Arial', 'weight': 'normal', 'size': 30}
matplotlib.rc('font', **font)
axis_font = {'fontname': 'Arial', 'size': '35'}

graph = "C9_NY_15K"

fig, ax = plt.subplots()
fig.set_figheight(8)
fig.set_figwidth(10)

labels = ['200', '400', '600']

# cosine 5k
# v1_method_value = [0.8637531, 0.9354196, 0.9307417]
# v2_method_value = [0.8456589, 0.8610190, 0.8431048]
# v3_method_value = [0.8598604, 0.8502648, 0.8759080]

# cosine 5k non normalization
# v1_method_value = [0.8637531, 0.9388, 0.9341]
# v2_method_value = [0.8456589, 0.8677, 0.8531]
# v3_method_value = [0.8598604, 0.8603, 0.8826]

# # euclidean_distance 5k
# v1_method_value = [0.4581361, 0.2618618, 0.2798224]
# v2_method_value = [0.5086907, 0.4717099, 0.4952286]
# v3_method_value = [0.4598221, 0.4896134, 0.4238644]

# euclidean_distance 5k non normalization
# v1_method_value = [1.0565, 0.4436, 0.4913]
# v2_method_value = [1.1784, 1.0296, 1.0504]
# v3_method_value = [1.095, 1.1735, 0.8642]


# cosine 10k
# v1_method_value = [0.8914, 0.8909, 0.9359]
# v2_method_value = [0.8867, 0.8882, 0.9063]
# v3_method_value = [0.8893, 0.8891, 0.892]

# euclidean_distance 10k
# v1_method_value = [1.0877, 1.1813, 0.6681]
# v2_method_value = [1.3321, 1.3273, 1.1]
# v3_method_value = [1.0976, 1.2442, 1.2608]

# cosine 15k
# v1_method_value = [0.8578257, 0.9776524, 0.8474685]
# v2_method_value = [0.8463509, 0.8428754, 0.8507412]
# v3_method_value = [0.8538326, 0.8373709, 0.8301423]

# euclidean_distance 15k
# v1_method_value = [0.4777699, 0.1373182, 0.5055530]
# v2_method_value = [0.5146427, 0.5147050, 0.4862905]
# v3_method_value = [0.4810689, 0.5149145, 0.5184796]


# cosine 15k non normalization
v1_method_value = [0.8645, 0.9777, 0.8541]
v2_method_value = [0.853, 0.8562, 0.8607]
v3_method_value = [0.8638, 0.8507, 0.8518]

# euclidean_distance 15k non normalization
# v1_method_value = [1.0498, 0.2326, 1.2191]
# v2_method_value = [1.3774, 1.4118, 1.2086]
# v3_method_value = [1.0728, 1.4012, 1.3451]



x = np.arange(len(labels))  # the label locations
width = 0.3  # the width of the bars

rectsV1 = ax.bar(x - width, v1_method_value, width,
                 label='none', align='center', hatch="\\", alpha=.99)
rectsV2 = ax.bar(x, v2_method_value, width,
                 label='each', align='center', hatch="/", alpha=.99)
rectsV3 = ax.bar(x + width, v3_method_value, width,
                 label='normal', align='center', hatch="-", alpha=.99)

# Add some text for labels, title and custom x-axis tick labels, etc.
ax.set_xlabel('cluster size')
# ax.set_ylabel('Goodness (Euclid sim.)')
ax.set_ylabel('Goodness (Cosine sim.)')
ax.set_xticks(x)
ax.set_xticklabels(labels)
ax.yaxis.set_major_formatter(FormatStrFormatter('%.2f'))
ax.legend(fontsize="large", loc=0, handletextpad=0.1, labelspacing=.1)

fig.tight_layout()

plt.xlim(0 - 2 * width, len(x) - width)
plt.ylim(0.8, 1)
# plt.ylim(0.2, 1.6)
# plt.ylim(0.2, 0.55)

# plt.show()
plt.savefig("""cosine_C9_NY_15K_new_1.pdf""")
# plt.savefig("""euclidean_distance_C9_NY_15K_new_1.pdf""")
