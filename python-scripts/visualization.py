import matplotlib.pyplot as plt
from matplotlib import animation
from matplotlib.animation import FuncAnimation
import sys
import pandas as pd
# matplotlib.use('Qt5Agg')

RESULTS_DIR = "../results/"
PATH_LENGTH = 10
CSV_DELIMITER = ' '

if len(sys.argv) != 2:
    print("Specify the file path as an argument to the program")


def get_axes_limits(df, prop_name):
    min_val = df[prop_name].min()
    max_val = df[prop_name].max()
    margin = (max_val - min_val) * 0.1
    return min_val - margin, max_val + margin


# file = RESULTS_DIR + sys.argv[1]
file = RESULTS_DIR + 'circle_XX.csv'
df = pd.read_csv(file, sep=CSV_DELIMITER, header=0, names=['id', 'mass', 'pos_x', 'pos_y', 'v_x', 'v_y', 'msg id'])
points_count = df['id'].nunique()
fig = plt.figure(figsize=(7, 7))
x_lim = get_axes_limits(df, 'pos_x')
y_lim = get_axes_limits(df, 'pos_y')
lim = (min(x_lim[0], y_lim[0]), max(x_lim[1], y_lim[1]))
ax = plt.axes(xlim=lim, ylim=lim)
scatter = ax.scatter([0] * points_count, [0] * points_count, s=10, color='blue')
scatter_path = ax.scatter([0] * points_count, [0] * points_count, s=2, color=[.7, .7, 1])

prev_pos = []

curr_frame = 0
steps = (len(df) - 1) // points_count


def update(frame):
    global prev_pos, curr_frame, steps, df

    curr_pos = df.iloc[curr_frame * points_count: (curr_frame + 1) * points_count] \
        .apply(lambda row: [row['pos_x'], row['pos_y']], axis=1)
    scatter.set_offsets(list(curr_pos))

    prev_pos += list(curr_pos)
#     prev_pos = prev_pos[points_count * (-PATH_LENGTH):]
    scatter_path.set_offsets(prev_pos)

    curr_frame += 1

    return scatter_path, scatter


if __name__ == '__main__':
    anim = FuncAnimation(fig, update, frames=steps, interval=30, repeat=False)
    # plt.show()
    writergif = animation.PillowWriter(fps=20)
    anim.save('animation.gif', writer=writergif)
