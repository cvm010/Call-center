import  java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Call{
    private final int id ;
    public Call(int id){
        this.id = id;
    }
    public int getId(){
        return id;
    }
    public void process(String handler) {
        System.out.println("call id -> " + id + " is handled by " + handler);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Call id -" + id + " has been completed by " + handler);
    }
}
abstract class  Employee{
    private final String  role;
    private boolean busy;

    public  Employee(String role){
        this.role = role;
        this.busy = false;
    }

    public boolean isBusy(){
        return busy;
    }
    public  void setBusy(boolean busy){
        this.busy = busy;
    }
    public void handleCall(Call call){
        setBusy(true);
        call.process(role);
        setBusy(false);
    }
}

class Operator extends Employee{
    public Operator(){
        super("Operator");
        }
        }

class Supervisor extends Employee{
    public Supervisor(){
        super("Supervisor");
    }
}

class Director extends Employee{
    public Director(){
        super("Director");
    }
}

class CallCenter {
    private final Queue<Operator> operators;
    private final Queue<Supervisor> supervisors;
    private final Queue<Director> directors;
    private final ExecutorService executorService;

    public CallCenter(int numOperators, int numSupervisors, int numDirectors) {
        operators = new LinkedList<>();
        supervisors = new LinkedList<>();
        directors = new LinkedList<>();
        executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < numOperators; i++) {
            operators.add(new Operator());
        }
        for (int i = 0; i < numSupervisors; i++) {
            supervisors.add(new Supervisor());
        }
        for (int i = 0; i < numDirectors; i++) {
            directors.add(new Director());
        }
    }

    public void receiveCall(Call call) {
        Employee handler = getAvailableEmployee();
        if (handler != null) {
            executorService.submit(() -> {
                handler.handleCall(call);
                releaseEmployee(handler);
            });
        } else {
            System.out.println("All employees are busy , Queuing your call" + call.getId());
        }
    }


    private Employee getAvailableEmployee() {
        synchronized (this) {
            for (Operator operator : operators) {
                if (!operator.isBusy()) {
                    operator.setBusy(true);
                    return operator;
                }
            }
            for (Supervisor supervisor : supervisors) {
                if (!supervisor.isBusy()) {
                    supervisor.setBusy(true);
                    return supervisor;
                }
            }
            for (Director director : directors) {
                if (!director.isBusy()) {
                    director.setBusy(true);
                    return director;
                }
            }
        }
        return null;
    }

    private  void releaseEmployee(Employee employee){
        synchronized (this){
            if(employee instanceof  Operator){
                operators.add((Operator) employee);
            }else if(employee instanceof Supervisor){
                supervisors.add((Supervisor) employee);
            }else if(employee instanceof Director){
                directors.add((Director) employee);
            }
        }
    }

    public void shutdown(){
        executorService.shutdown();
    }
}


public class Main {
    public static void main(String[] args) {
        CallCenter callCenter = new CallCenter(3,2,1);
        for(int i=1;i<=10;i++){
            Call call = new Call(i);
            callCenter.receiveCall(call);
        }
        callCenter.shutdown();
    }
}