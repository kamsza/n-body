import math, copy
import numpy as np
from functools import reduce, partial


G = 6.6743E-11
EPS = 1.0E-5


'''
Funkcja zwraca współrzęcne i-tego wierzchołka wielokąta foremnego wpisanego w okrąg o promieniu r,
przyjmując, że promieńśrodkowy jest podany jako parametr degree.  
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
    v = (R_l - R_k) / (np.sqrt((R_k - R_l).dot(R_k - R_l)))**3
    return sum + v


'''
Funkcja zwraca siłę grawitacji działającą na wierzchołek i indeksie k. 
'''
def get_F(verticles, k, m):
    R_k = verticles[k]
    R_l_list = verticles[:k] + verticles[k+1:]
    return G * m**2 * reduce(partial(get_R_sum, R_k=R_k), R_l_list, np.array([0, 0]))


'''
Funkcja sprawdza, czy wektory są równoległe (z pewnym przyblizeniem).
'''
def are_near_parallel(v1, v2):
    return abs(np.dot(v1,v2)/(np.linalg.norm(v1)*np.linalg.norm(v2))) > 1 - EPS


def get_v_vec(position, v):
    x,y = position
    sin_L = y / math.sqrt(x**2 + y**2)
    cos_L = x / math.sqrt(x**2 + y**2)
    v_x = -1 * v * sin_L
    v_y = v * cos_L
    return v_x, v_y


if __name__ == '__main__':
    N = 10
    m = 1.0E24
    R = 1.0E11
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
        bodies.append([i, m, positions[i][0], positions[i][1], forces[i][0], forces[i][1], velocities[i][0], velocities[i][1]])

    bodies_str = '\n'.join([' '.join(map(str, body)) for body in bodies])
    header = 'id mass position_x position_y F_x F_y velocity_x velocity_y\n'
    with open(f"circle_{N}.txt", "w") as output_file:
        output_file.write(header)
        output_file.write(bodies_str)