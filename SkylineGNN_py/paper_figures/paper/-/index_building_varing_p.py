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


#Construction time 
# 2	3430.288
# 5	3473.258
# 8	3399.292
# 10     3547.599
# 15	3449.508
# 20	    3553.444

#index size


folder_name = os.path.dirname(__file__)

with open('{}/data_p.txt'.format(folder_name)) as infile:
    trend = numpy.loadtxt(infile)

# line1 = ax.plot(trend[:, 0], trend[:, 1], c='r', marker='o', ls="-", label='construction time', ms=18, lw=4)
line1 = ax.plot(trend[:, 0], trend[:, 1], c='r', marker='o', ls="-", label='index size', ms=18, lw=4)
# ax.set_ylabel('Construction time (Sec.)', **axis_font)
ax.set_ylabel('index size (MB)', **axis_font)
ax.set_xlabel('percentage', **axis_font)
ax.set_xticks(trend[:, 0])
# ax.tick_params(axis="x", which='major', labelsize=25)
# ax.ticklabel_format(style='sci', axis='y', scilimits=(0, 0))
ax.set_ylim(2200, 2800)

# plt.show()
plt.savefig('index_construction_varing_p_22K_index_size.pdf', bbox_inches='tight')
