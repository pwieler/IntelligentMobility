from enum import Enum


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

    def __init__(self, speed, max_users, price, vehicle_type=VehicleType.A):
        self._speed = speed
        self._price = price
        self._max_users = max_users
        self._current_total_users = 0
        self._current_users = []
        self._direction = 0
        self._position = (0, 0)
        self._type = vehicle_type
        self._desires = []
        self._intentions = []
        self._beliefs = []

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



