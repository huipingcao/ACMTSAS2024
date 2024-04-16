import matplotlib
import matplotlib.pyplot as plt
import numpy as np

font = {'family': 'Arial', 'weight': 'normal', 'size': 30}
matplotlib.rc('font', **font)
axis_font = {'fontname': 'Arial', 'size': '35'}

graph = "C9_NY_15K"

fig, (ax, ax2) = plt.subplots(2, 1, sharex=True)
fig.set_figheight(8)
fig.set_figwidth(10)

# labels = ['200', '400', '600']
# bbs_value = [4781.91, 4781.91, 4781.91]
# v1_method_value = [421.6, 73791.513, 49224.223]
# v2_method_value = [409.23, 412.063, 407.357]
# v3_method_value = [402.99, 406.697, 419.657]

labels = ['200', '400', '600']
bbs_value = [4.78191, 4.78191, 4.78191]
v1_method_value = [0.4216, 73791.513, 49224.223]
v2_method_value = [0.40923, 0.412063, 0.407357]
v3_method_value = [0.40299, 0.406697, 0.419657]

x = np.arange(len(labels))  # the label locations
width = 0.2  # the width of the bars

scaler = 1
decimal = 2
ax_rectsBBS = ax.bar(x-2*width, [round(i/scaler, decimal) for i in bbs_value],
                     width, label='BBS', align='edge', alpha=.99)
ax_rectsV1 = ax.bar(x-width, [round(i/scaler, decimal) for i in v1_method_value], width,
                    label='none', align='edge', hatch="\\", alpha=.99)
ax_rectsV2 = ax.bar(x, [round(i/scaler, decimal) for i in v2_method_value], width,
                    label='each', align='edge', hatch="/", alpha=.99)
ax_rectsV3 = ax.bar(x+width, [round(i/scaler, decimal) for i in v3_method_value], width,
                    label='normal', align='edge', hatch="-", alpha=.99)
scaler = 1
ax2_rectsBBS = ax2.bar(x-2*width, [round(i/scaler, decimal) for i in bbs_value],
                       width, label='BBS', align='edge', alpha=.99)
ax2_rectsV1 = ax2.bar(x-width, [round(i/scaler, decimal) for i in v1_method_value], width,
                      label='none', align='edge', hatch="\\", alpha=.99)
ax2_rectsV2 = ax2.bar(x, [round(i/scaler, decimal) for i in v2_method_value], width,
                      label='each', align='edge', hatch="/", alpha=.99)
ax2_rectsV3 = ax2.bar(x+width, [round(i/scaler, decimal) for i in v3_method_value], width,
                      label='normal', align='edge', hatch="-", alpha=.99)


# hide the spines between ax and ax2
ax.spines['bottom'].set_visible(False)
ax2.spines['top'].set_visible(False)
ax2.set_ylim(0, 6000/1000)
ax.set_ylim(40000/scaler, 80000/scaler)
ax2.xaxis.tick_bottom()


d = .015  # how big to make the diagonal lines in axes coordinates
# arguments to pass to plot, just so we don't keep repeating them
kwargs = dict(transform=ax.transAxes, color='k', clip_on=False)
ax.plot((-d, +d), (-d, +d), **kwargs)        # top-left diagonal
ax.plot((1 - d, 1 + d), (-d, +d), **kwargs)  # top-right diagonal

kwargs.update(transform=ax2.transAxes)  # switch to the bottom axes
ax2.plot((-d, +d), (1 - d, 1 + d), **kwargs)  # bottom-left diagonal
ax2.plot((1 - d, 1 + d), (1 - d, 1 + d), **kwargs)  # bottom-right diagonal

# # Add some text for labels, title and custom x-axis tick labels, etc.
ax2.set_xlabel('cluster size')

ax2.set_xticks(x)
ax2.set_xticklabels(labels)
ax.legend(fontsize="medium", loc=0, handletextpad=0.1, labelspacing=.1)
# ax.set_ylabel('Query time (ms)')
# ax.set_title('Comparison on {}'.format(graph))
fig.text(0.08, 0.5, 'Query time (ms)', va='center', rotation='vertical')


# def autolabel(rects):
#     """Attach a text label above each bar in *rects*, displaying its height."""
#     for rect in rects:
#         height = rect.get_height()
#         ax.annotate('{}'.format(height),
#                     xy=(rect.get_x() + rect.get_width() / 2, height),
#                     xytext=(0, 3),  # 3 points vertical offset
#                     textcoords="offset points",
#                     ha='center', va='bottom', size=10, weight='bold')
#         ax2.annotate('{}'.format(height),
#                      xy=(rect.get_x() + rect.get_width() / 2, height),
#                      xytext=(0, 3),  # 3 points vertical offset
#                      textcoords="offset points",
#                      ha='center', va='bottom', size=10, weight='bold')


# autolabel(ax_rectsBBS)
# autolabel(ax_rectsV1)
# autolabel(ax_rectsV2)
# autolabel(ax_rectsV3)

# autolabel(ax2_rectsBBS)
# autolabel(ax2_rectsV1)
# autolabel(ax2_rectsV2)
# autolabel(ax2_rectsV3)

fig.tight_layout()

plt.xlim(0-2.5*width, len(x)-width)
ax.ticklabel_format(axis="y", style="sci", scilimits=(3,3))
# ax2.ticklabel_format(axis="y", style="sci", scilimits=(0,0))

# plt.show()
plt.savefig("""query_time_{}_non_annotation.pdf""".format(graph))
