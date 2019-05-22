package intelligentMobility;

/**
 * Created by Pascal on 22.05.2019.
 */
public class EvaluationSetup {

    public enum EvaluationMode {
        StrategyVariation,
        UserVariation,
        AgentVariation,
        Default
    }

    // Variables
    static int setup_counter = 0;

    static int user_increase = 5;
    static int max_user = 100;

    static int agent_increase = 2;
    static int max_agent = 30;


    // Params
    static EvaluationMode evaluationMode = EvaluationMode.StrategyVariation;

    //Default Strategies
    static AgentStrategy agentStrategy = AgentStrategy.MinUnpaidTime;
    static UserStrategy userStrategy = UserStrategy.TimeStressed;
    

    static int agent_count = 5; 
//    static int agent_count = 1;
//    static int user_count = 50;
    static int user_count = 50;

    static void nextSetup(){
    	evaluationMode=EvaluationMode.StrategyVariation;
        switch(evaluationMode){
            case StrategyVariation:
                nextSetupStrategyVariation();
                break;
            case UserVariation:
                nextSetupUserVariation();
                break;
            case AgentVariation:
                nextSetupAgentVariation();
                break;
        }

        printSetup();

        setup_counter ++;
    }

    static void printSetup(){
        System.out.println("Mode: "+evaluationMode+" Agents: "+agent_count+" User: "+user_count+" UserStrategy: "+userStrategy+" AgentStrategy: "+agentStrategy);
    }

    static String getTitle(){
        return ""+evaluationMode;
    }


    static void nextSetupUserVariation(){

        agentStrategy = AgentStrategy.ClusterBased;
        userStrategy = UserStrategy.MostPassengers;

        user_count+=user_increase;

        if(user_count>max_user){
            setup_counter = 0;
//            evaluationMode = EvaluationMode.AgentVariation;
            evaluationMode = EvaluationMode.Default;

            agent_count = 1;
            user_count = 50;
        }
    }

    static void nextSetupAgentVariation(){

        agentStrategy = AgentStrategy.ClusterBased;
        userStrategy = UserStrategy.MostPassengers;

        agent_count+=agent_increase;

        if(agent_count>max_agent){
            setup_counter = 0;
            evaluationMode = EvaluationMode.Default;
        }
    }

    static void nextSetupStrategyVariation(){

        agent_count = 5;
        user_count = 50;


        switch(setup_counter){
            case 0:
                agentStrategy = AgentStrategy.ClusterBased;
                userStrategy = UserStrategy.MostPassengers;
                break;
            case 1:
                agentStrategy = AgentStrategy.ClusterBased;
                userStrategy = UserStrategy.Loner;
                break;
            case 2:
                agentStrategy = AgentStrategy.ClusterBased;
                userStrategy = UserStrategy.ShortestPickup;
                break;
            case 3:
                agentStrategy = AgentStrategy.ClusterBased;
                userStrategy = UserStrategy.TimeStressed;
                break;


            case 4:
                agentStrategy = AgentStrategy.MaxGuaranteedPaidTime;
                userStrategy = UserStrategy.MostPassengers;
                break;
            case 5:
                agentStrategy = AgentStrategy.MaxGuaranteedPaidTime;
                userStrategy = UserStrategy.Loner;
                break;
            case 6:
                agentStrategy = AgentStrategy.MaxGuaranteedPaidTime;
                userStrategy = UserStrategy.ShortestPickup;
                break;
            case 7:
                agentStrategy = AgentStrategy.MaxGuaranteedPaidTime;
                userStrategy = UserStrategy.TimeStressed;
                break;



            case 8:
                agentStrategy = AgentStrategy.MinUnpaidTime;
                userStrategy = UserStrategy.MostPassengers;
                break;
            case 9:
                agentStrategy = AgentStrategy.MinUnpaidTime;
                userStrategy = UserStrategy.Loner;
                break;
            case 10:
                agentStrategy = AgentStrategy.MinUnpaidTime;
                userStrategy = UserStrategy.ShortestPickup;
                break;
            case 11:
                agentStrategy = AgentStrategy.MinUnpaidTime;
                userStrategy = UserStrategy.TimeStressed;
                break;

            default:
                setup_counter = 0;
//                evaluationMode = EvaluationMode.UserVariation;
                evaluationMode = EvaluationMode.Default;
                user_count = 5;
        }
    }

}
