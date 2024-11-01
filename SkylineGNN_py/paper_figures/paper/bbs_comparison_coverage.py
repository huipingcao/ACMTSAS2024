import numpy
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

with open('{}/data_coverage.txt'.format(folder_name)) as infile:
    trend = numpy.loadtxt(infile)

plt.scatter(trend[:, 1], trend[:, 0], c='r', marker='o', label='# of Covered nodes', s=400)
ax.set_ylabel('BBS query time (ms)', **axis_font)
ax.set_xlabel('# of Covered nodes', **axis_font)
# ax.set_xticks(trend[:, 0])
# ax.tick_params(axis="x", which='major', labelsize=25)
# ax.ticklabel_format(style='sci', axis='y', scilimits=(0, 0))
ax.ticklabel_format(style='sci', axis='both', scilimits=(0, 0))
ax.set_ylim(1000, 130000)

# plt.show()
plt.savefig('BBS_Comparison_Coverage_BAY.pdf', bbox_inches='tight')
