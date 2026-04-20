package businesslogic;

import model.Server;
import model.Task;

import java.util.List;

public class ConcreteStrategyQueue implements Strategy{

    @Override
    public void addTask(List<Server> servers, Task t){

        if(servers == null || servers.isEmpty()){
            return;
        }

        Server shortestQueueServer = servers.get(0);
        int minTasks = shortestQueueServer.getTasks().length;

        for(Server server : servers){
            int currentQueueLength = server.getTasks().length;
            if(currentQueueLength < minTasks){
                minTasks = currentQueueLength;
                shortestQueueServer = server;
            }
        }

        shortestQueueServer.addTask(t);
    }
}
