import math, sys, copy, matplotlib
import matplotlib.pyplot as plt
import numpy as np
from functools import reduce, partial
matplotlib.use('Qt5Agg')


G = 6.6743E-11
EPS = 1.0E-5

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
    v = (R_l - R_k) / (np.sqrt((R_k - R_l).dot(R_k - R_l)))**3
    return sum + v


def get_F(verticles, k, M, N):
    R_k = verticles[k]
    R_l_list = verticles[:k] + verticles[k+1:]
    return G * (M/N)**2 * reduce(partial(get_R_sum, R_k=R_k), R_l_list, np.array([0, 0]))


def are_near_parallel(v1, v2):
    return abs(np.dot(v1,v2)/(np.linalg.norm(v1)*np.linalg.norm(v2))) > 1 - EPS


if __name__ == '__main__':
    M = 1000
    N = 10
    R = 100

    alpha = 2.0 * math.pi / N
    verticles = []
    for verticle_id in range(0, N):
        x, y = get_polygon_verticle_pos(verticle_id, R, alpha)
        verticles.append(np.array([x, y]))

    for i in range(N):
        print(are_near_parallel(verticles[i], get_F(verticles, i, M, N)))
