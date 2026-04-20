package businesslogic;
import java.util.List;
import model.Server;
import model.Task;

public class ConcreteStrategyTime implements Strategy {

    @Override
    public void addTask(List<Server> servers, Task t){
        if(servers == null || servers.isEmpty()){
            return;
        }

        Server shortestTimeServer = servers.get(0);
        int minTime = shortestTimeServer.getWaitingPeriod().get();

        for(Server server : servers){

            int currentServerTime = server.getWaitingPeriod().get();
            if(currentServerTime < minTime){
                shortestTimeServer = server;
                minTime = currentServerTime;
            }
        }

        shortestTimeServer.addTask(t);
    }
}
