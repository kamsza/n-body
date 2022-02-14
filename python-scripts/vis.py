import sys
import matplotlib.pyplot as plt
import pandas as pd
from os import listdir
from os.path import isfile, join, abspath
from operator import iconcat
from functools import reduce
from matplotlib.animation import FuncAnimation

RESULTS_DIR = "../results/"
CSV_DELIMITER = ' '

class ClusterData:
    def __init__(self, file_name):
        self.df = pd.read_csv(file_name, sep=CSV_DELIMITER, header=0, names=['id', 'mass', 'pos_x', 'pos_y', 'v_x', 'v_y', 'timestep'])
        self.data_count = self.df['id'].nunique()
        self.x_lim = self._get_axes_limits('pos_x')
        self.y_lim = self._get_axes_limits('pos_y')
        print(self.df)


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


def get_axes_limits(cluster_data, prop_name):
    lim = [getattr(cluster, prop_name) for cluster in cluster_data]
    return reduce(lambda lim_1, lim_2: (min(lim_1[0], lim_2[0]), max(lim_1[1], lim_2[1])), lim)

def get_points_count(cluster_data):
    return sum([getattr(cluster, 'data_count') for cluster in cluster_data])

def get_steps_count(cluster_data):
    steps_count = cluster_data[0].get_steps_count()
    for cluster in cluster_data:
        if cluster.get_steps_count() != steps_count:
            raise Exception(f"Different number of steps in clusters expected ${steps_count} got: ${cluster.get_steps_count()}")
    return steps_count

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Specify the dir path as an argument to the program")

    # format data
    if isfile(join(RESULTS_DIR, sys.argv[1])):
        files = [abspath(join(RESULTS_DIR, sys.argv[1]))]
    else:
        dir_path = join(RESULTS_DIR, sys.argv[1])
        files = [abspath(join(dir_path, f)) for f in listdir(dir_path) if isfile(join(dir_path, f))]
    cluster_data = [ClusterData(file) for file in files]
    points_count = get_points_count(cluster_data)

    # create figure
    fig = plt.figure(figsize=(7, 7))
    x_lim = get_axes_limits(cluster_data, 'x_lim')
    y_lim = get_axes_limits(cluster_data, 'y_lim')
    ax = plt.axes(xlim=x_lim, ylim=y_lim)
    scatter = ax.scatter([0] * points_count, [0] * points_count, s=10, color='blue')
    scatter_path = ax.scatter([0] * points_count, [0] * points_count, s=2, color=[.7, .7, 1])

    # make visualization
    steps = get_steps_count(cluster_data)
    prev_pos = []

    def update(frame):
        global prev_pos, steps, cluster_data

        curr_pos = reduce(iconcat, [cluster.get_positions(frame) for cluster in cluster_data], [])
        scatter.set_offsets(curr_pos)

        prev_pos += curr_pos
        scatter_path.set_offsets(prev_pos)

        return scatter_path, scatter

    anim = FuncAnimation(fig, update, frames=steps, interval=200, repeat=False)
    plt.show()