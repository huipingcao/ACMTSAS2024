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

with open('{}/data_m.txt'.format(folder_name)) as infile:
    trend = numpy.loadtxt(infile)

line1 = ax.plot(trend[:, 0], trend[:, 1], c='r', marker='o',
                ls="-", label='construction time', ms=18, lw=4)
ax.set_ylabel('construction time (Sec.)', **axis_font)
ax.set_xlabel('cluster size', **axis_font)
ax.ticklabel_format(style='sci', axis='y', scilimits=(0, 0))

ax2 = ax.twinx()
color = 'tab:blue'
# we already handled the x-label with ax1
ax2.set_ylabel('Index Size (MB)', color=color)
line2 = ax2.plot(trend[:, 0], trend[:, 2], c='g', marker='s', fillstyle='none',
                 markeredgewidth="4", ls="--", label='index size', ms=18, lw=4)
ax2.tick_params(axis='y', labelcolor=color)
ax2.ticklabel_format(style='sci', axis='y', scilimits=(0, 0))

ax.legend(line1 + line2, ['Construction time', 'index size'], loc=0)

# plt.show()
plt.savefig('index_construction_varing_m_22K.pdf', bbox_inches='tight')
