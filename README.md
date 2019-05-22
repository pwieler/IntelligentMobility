# IntelligentMobility
AAMAS_Project 2019

## Running the system trough: 

### Executable
To execute the program is simple, just double-click the 021.jar.
From the different User and Agent strategies we mentioned in the report, we choosen as the default ones in the project the ones with the best result, which are: Minimum Unpaid Time(Agent) and Time Stressed(User).

### Source Code
To run the system trough the source code, we use the class Main.java as start-up.
If you want to experiment other strategies for curiosity, you can change the Agent and User default strategy on the class EvaluationSetup.java on the lines 29 and 30 under the comment " //Default Strategies " with your strategy of choice:
- static AgentStrategy agentStrategy = AgentStrategy.____;
- static UserStrategy userStrategy = UserStrategy.____;
