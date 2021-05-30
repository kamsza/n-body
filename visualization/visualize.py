import matplotlib.pyplot as plt
from matplotlib import animation
from matplotlib.animation import FuncAnimation
import csv

SIZE = 2.7e11
N = 5

with open('result.csv', 'r') as file:
    reader = csv.reader(file)
    result = list(reader)[:100000]

steps = len(result) // N - 1

fig = plt.figure(figsize=(7, 7))
ax = plt.axes(xlim=(-SIZE, SIZE), ylim=(-SIZE, SIZE))
scatter_path = ax.scatter([0] * N, [0] * N, s=2, color=[.7, .7, 1])
scatter = ax.scatter([0] * N, [0] * N, s=10, color='blue')

prev_pos = []
curr_pos = []

def update(frame):
    global prev_pos, curr_pos
    for x in range(9*N):
        result.pop(0)

    for x in range(N):
        curr_obj = result.pop(0)
        curr_pos += [[curr_obj[2], curr_obj[3]]]
    curr_pos = curr_pos[N * (-1):]

    scatter.set_offsets(curr_pos)
    scatter_path.set_offsets(prev_pos)

    prev_pos += curr_pos
    # prev_pos = prev_pos[N * (-300):]
    return scatter,


anim = FuncAnimation(fig, update, frames=steps//10 - 100, interval=1, repeat=False)
plt.show()
# writergif = animation.PillowWriter(fps=30)
# anim.save('animation.gif', writer=writergif)
