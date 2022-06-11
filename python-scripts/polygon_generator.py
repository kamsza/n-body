import copy
import math
import numpy as np
from functools import reduce, partial

"""
Generator, used to generate a STABLE array of n objects, each located at the vertex of an regular n-angle figure.
The objects move around the centre of the array.

Arguments to change:
 N          - number of objects
 DIS        - setting number =/= 1 we can have objects move over elipses, moving towards and away from the centre (e.g. DIS = 0.85 / DIS = 1.1)

 X_CENTER   - coordinate of the centre of the system
 Y_CENTER   - coordinate of the centre of the system 

 V_X_GROUP  - velocity of the centre of the system 
 V_Y_GROUP  - velocity of the centre of the system 

 FILE_NAME  - output file
 
 R          - radius of figure
 m          - mass of one body
 
 G          - gravitational constant (do not change)
 EPS        - accuracy (do not change)
"""

N = 10
DIS = 1.0

X_CENTER = 0.0
Y_CENTER = 0.0

V_X_GROUP = 0
V_Y_GROUP = 0

FILE_NAME = "circle.csv"

R = 1.0E10
m = 1.0E24
G = 6.6743E-11
EPS = 1.0E-5

'''
The function returns the coordinates of the i-th vertex of a regular polygon inscribed in a circle of radius r,
assuming that the central radius is given as the degree parameter.
'''
def get_polygon_verticle_pos(i, r, degree):
    x = r * math.cos(i * degree)
    y = r * math.sin(i * degree)
    return x, y


def draw_polygon(verticles):
    coord = copy.deepcopy(verticles)
    coord.append(coord[0])
    xs, ys = zip(*coord)

    plt.figure()
    plt.plot(xs, ys)
    plt.show()


def get_R_sum(sum, R_l, R_k):
    v = (R_l - R_k) / (np.sqrt((R_k - R_l).dot(R_k - R_l))) ** 3
    return sum + v


'''
The function returns the gravitational force acting on a vertex with index k. 
'''
def get_F(verticles, k, m):
    R_k = verticles[k]
    R_l_list = verticles[:k] + verticles[k + 1:]
    return G * m ** 2 * reduce(partial(get_R_sum, R_k=R_k), R_l_list, np.array([0, 0]))


'''
The function checks that the vectors are parallel (with some approximation).
'''
def are_near_parallel(v1, v2):
    return abs(np.dot(v1, v2) / (np.linalg.norm(v1) * np.linalg.norm(v2))) > 1 - EPS


def get_v_vec(position, v):
    x, y = position
    sin_L = y / math.sqrt(x ** 2 + y ** 2)
    cos_L = x / math.sqrt(x ** 2 + y ** 2)
    v_x = -1 * v * sin_L * DIS + V_X_GROUP
    v_y = v * cos_L * DIS + V_Y_GROUP
    return v_x, v_y


if __name__ == '__main__':
    alpha = 2.0 * math.pi / N
    bodies = []
    positions = []
    for i in range(0, N):
        x, y = get_polygon_verticle_pos(i, R, alpha)
        positions.append(np.array([x, y]))

    forces = []
    for i in range(0, N):
        F_k = get_F(positions, i, m)
        forces.append(F_k)
        if not are_near_parallel(F_k, positions[i]):
            print(f"NOT PARALLEL FOR BODY {i}")

    velocities = []
    for i in range(0, N):
        v = math.sqrt(np.linalg.norm(forces[i]) * R / m)
        v_x, v_y = get_v_vec(positions[i], v)
        velocities.append(np.array([v_x, v_y]))

    bodies = []
    for i in range(0, N):
        bodies.append(
            [m, positions[i][0] + X_CENTER, positions[i][1] + Y_CENTER, velocities[i][0], velocities[i][1]])

    bodies_str = '\n'.join([';'.join(map(str, body)) for body in bodies])
    header = 'mass;position_x;position_y;velocity_x;velocity_y\n'
    with open(FILE_NAME, "w") as output_file:
        output_file.write(header)
        output_file.write(bodies_str)
