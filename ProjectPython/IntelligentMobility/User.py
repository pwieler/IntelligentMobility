class User:

    def __init__(self, position=(0, 0)):
        self._position = position

    def set_position(self, position):
        self._position = position

    def get_position(self):
        return self._position

