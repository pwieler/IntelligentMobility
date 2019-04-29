from enum import Enum
import random
from User import User


class Agent:
    class Desire(Enum):
        PICKUP = 1
        DROP = 2

    class Action(Enum):
        MOVING = 1
        ROTATE = 2
        STOP = 3
        PICKUP = 4
        DROP = 5

    class VehicleType(Enum):
        A = 1
        B = 2

    def __init__(self, speed, max_users, price, position=(0, 0), vehicle_type=VehicleType.A):
        #self._id = ...  Do we need id?
        self._speed = speed
        self._price = price
        self._max_users = max_users
        self._current_total_users = 0
        self._current_users = []
        self._direction = 0
        self._position = position
        self._type = vehicle_type
        self._desires = []
        self._intentions = []
        self._beliefs = []
        self._state['Action'] = 3

    def cost(self, travel_distance):
        return self.time(travel_distance)*self._price

    def time(self, travel_distance):
        return travel_distance/self._speed

    def waiting_time(self, travel_distance):
        return self.time(travel_distance)*0.1
        # Agent wait 10% of the travel time

    def pick_up_user(self, user):
        self._current_users.append(user)

    def drop_user(self, user):
        self._current_users.remove(user)

    def free_user_space(self):
        return self._current_users.__len__() < self._max_users

    def rotate_right(self):
        return (self._direction+90) % 360

    def rotate_left(self):
        return (self._direction-90) % 360

    def rotate_random(self):
        if random.choice([True, False]):
            self.rotate_right()
        else:
            self.rotate_left()
