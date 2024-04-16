import numpy
import scipy
import sklearn.preprocessing
import matplotlib.pyplot as plt
import matplotlib
from pylab import *
import io

font = {'family': 'Arial',
        'weight': 'normal',
        'size': 30}
matplotlib.rc('font', **font)
axis_font = {'fontname': 'Arial', 'size': '35'}

fig, ax = plt.subplots()
fig.set_figheight(8)
fig.set_figwidth(10)


data_str = """
0.001 2.83 0.2 0.16
0.05 182.85926 6.76506 3.29888
0.1 435.1223 15.833975 7.226675
0.2 1098.6218 33.7412 15.439
0.5 2899.94 112.41 52.35
1 6860.19 256.45 113.4"""

trend = numpy.loadtxt(io.StringIO(data_str), delimiter=" ")

# speedup wit improved exact
plt.plot(trend[:, 0], trend[:, 1], c='r', marker='o',
         ls="-", label='ExactAlg-improved', ms=18, lw=4)
plt.plot(trend[:, 0], trend[:, 2], c='g', marker='s', fillstyle='none',
         markeredgewidth="4", ls="--", label='Approx-range-indexed', ms=18, lw=4)
plt.plot(trend[:, 0], trend[:, 3],  c='k', marker="d", fillstyle='none',
         markeredgewidth="4", ls="--", label='Approx-mix-indexed', ms=18, lw=4)
plt.ylabel('Running Time (Sec.)', **axis_font)
plt.xlabel('# of graph nodes (in millions)', **axis_font)
plt.ticklabel_format(style='sci', axis='y', scilimits=(0, 0))
plt.legend(loc=2)
# plt.savefig('speedup.pdf', bbox_inches='tight')
plt.show()
