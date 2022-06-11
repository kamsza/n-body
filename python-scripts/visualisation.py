import math
import matplotlib.pyplot as plt
import pandas as pd
import sys
import numpy as np
from functools import reduce
from matplotlib.animation import FuncAnimation
from operator import iconcat
from os import listdir
from os.path import isfile, join, abspath

RESULTS_DIR = "../results/"
CSV_DELIMITER = ';'

"""
Visualises data on a graph.

Arguments:
 args(1) - data path
 args(2) - interval
"""

class ClusterData:
    def __init__(self, file_name):
        self.df = pd.read_csv(file_name, sep=CSV_DELIMITER, header=0,
                              names=['id', 'mass', 'pos_x', 'pos_y', 'v_x', 'v_y', 'timestamp'],
                              dtype={'pos_x': np.float64, 'pos_y': np.float64})
        self.data_count = self.df['id'].nunique()
        self.x_lim = self._get_axes_limits('pos_x')
        self.y_lim = self._get_axes_limits('pos_y')
        # print(self.df)

    def _get_axes_limits(self, prop_name):
        min_val = self.df[prop_name].min()
        max_val = self.df[prop_name].max()
        margin = (max_val - min_val) * 0.1
        return min_val - margin, max_val + margin

    def get_positions(self, step_id: int) -> list:
        return list(self.df.iloc[step_id * self.data_count: (step_id + 1) * self.data_count] \
                    .apply(lambda row: [row['pos_x'], row['pos_y']], axis=1))

    def get_steps_count(self):
        return len(self.df) // self.data_count


def get_summary_axes_limits(cluster_data, prop_name):
    lim = [getattr(cluster, prop_name) for cluster in cluster_data]
    return reduce(lambda lim_1, lim_2: get_axes_limits(lim_1, lim_2), lim)


def get_axes_limits(lim_1, lim_2):
    return min(lim_1[0], lim_2[0]), max(lim_1[1], lim_2[1])


def get_points_count(cluster_data):
    return sum([getattr(cluster, 'data_count') for cluster in cluster_data])


def get_steps_count(cluster_data):
    steps_count = cluster_data[0].get_steps_count()
    for cluster in cluster_data:
        if cluster.get_steps_count() != steps_count:
            raise Exception(
                f"Different number of steps in clusters expected ${steps_count} got: ${cluster.get_steps_count()}")
    return steps_count


def update_limit(curr_pos, ax):
    x = [pos[0] for pos in curr_pos]
    y = [pos[1] for pos in curr_pos]
    lim = max([round_up_abs(min(x)), round_up_abs(max(x)), round_up_abs(min(y)), round_up_abs(max(y))])
    old_lim = max([abs(ax.get_ylim()[0]), abs(ax.get_ylim()[1]), abs(ax.get_xlim()[0]), abs(ax.get_xlim()[0])])
    if old_lim < lim:
        ax.set_xlim(-1 * lim, lim)
        ax.set_ylim(-1 * lim, lim)


def round_up_abs(num):
    digits = int(math.log10(abs(num)))
    first_digit = abs(int(1.2 * num / pow(10, digits)))
    first_digit += 2 if first_digit > 0 else -2
    return first_digit * pow(10, digits)


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Specify the dir path as an argument to the program")

    # format data
    if isfile(join(RESULTS_DIR, sys.argv[1])):
        files = [abspath(join(RESULTS_DIR, sys.argv[1]))]
    else:
        dir_path = join(RESULTS_DIR, sys.argv[1])
        files = [abspath(join(dir_path, f)) for f in listdir(dir_path) if isfile(join(dir_path, f))]

    interval = 200 if len(sys.argv) < 3 else int(sys.argv[2])

    cluster_data = [ClusterData(file) for file in files]
    points_count = get_points_count(cluster_data)

    # create figure
    fig = plt.figure(figsize=(7, 7))
    x_lim = get_summary_axes_limits(cluster_data, 'x_lim')
    y_lim = get_summary_axes_limits(cluster_data, 'y_lim')
    lim = get_axes_limits(x_lim, y_lim)
    ax = plt.axes()
    scatter_path = ax.scatter([0] * points_count, [0] * points_count, s=2, color=[.7, .7, 1])
    scatter = ax.scatter([0] * points_count, [0] * points_count, s=10, color='blue')
    center = 5e10
    r = 5e11
    ax.set_xlim(center-r, center+r)
    ax.set_ylim(center-r, center+r)

    # make visualization
    steps = get_steps_count(cluster_data)
    prev_pos = []


    def update(frame):
        global prev_pos, steps, cluster_data, ax

        curr_pos = reduce(iconcat, [cluster.get_positions(frame) for cluster in cluster_data], [])
        scatter.set_offsets(curr_pos)

        prev_pos += curr_pos
        scatter_path.set_offsets(prev_pos)

        # update_limit(curr_pos, ax)
        print(f"{frame} / {steps}")

        return scatter, scatter_path


    anim = FuncAnimation(fig, update, frames=steps, interval=interval, repeat=False)
    plt.show()
