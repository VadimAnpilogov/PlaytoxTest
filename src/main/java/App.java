import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

class MyThread implements Runnable{

    static List<Account> listObj;//Коллекция, в которой хранятся все счета
    static int countTransaction = 0;//Счетчик транзакций
    private static final Logger logger = Logger.getLogger(MyThread.class);

    public MyThread(){
    }

    static synchronized void Transaction() {
        int numberAccount = (int) (Math.random() * listObj.size()-1);//Выбор счета, с которого будет осуществляться перевод
        int sumMoney = (int) (Math.random() * listObj.get(numberAccount).Money);//Выбор суммы перевода
        while (sumMoney==0){//Проверка, чтобы сумма перевода была > 0
            if(listObj.get(numberAccount).Money==0){
                numberAccount++;
            }else sumMoney++;
        }


        int numberAccountR;//Счет на который будет осуществляться перевод
        if(numberAccount==listObj.size()){
            numberAccountR = 0;
        }
        else numberAccountR = numberAccount + 1;



        listObj.get(numberAccount).Money = listObj.get(numberAccount).Money - sumMoney;
        listObj.get(numberAccountR).Money = listObj.get(numberAccountR).Money + sumMoney;
        countTransaction +=1;

        logger.info("Transaction number: "+ countTransaction +", sumTransaction = " + sumMoney +
                ", Отправитель-ID: " + listObj.get(numberAccount).ID + ", Money= "+listObj.get(numberAccount).Money +
                ", Получатель-ID: " + listObj.get(numberAccountR).ID + ", Money= "+listObj.get(numberAccountR).Money);

        TestMoney();//Проверка значений Money
    }

    public static void TestMoney(){
        int sumMoney=0;
        for(int i=0;i<listObj.size();i++){
            sumMoney += listObj.get(i).Money;
            if(listObj.get(i).Money<0){//Проверка значения Money, чтобы оно не было отрицательным
                logger.error("Money less than zero, ID Account:" + listObj.get(i).ID);
            }
        }
        if(sumMoney!=10000*listObj.size()){//Проверка значения Money, чтобы сумма всех значений не изменилась
            logger.error("Sum Money has changed, sum Money = " + sumMoney);
        }
    }

    @Override
    public void run() {

        while (true){
            App app = new App();
            if(countTransaction>=app.getCountTransaction()){//Проверка количества транзакций
                break;
            }
            Transaction();//Выполнение транзакции
            try {
                Thread.sleep(1000 + (int) (Math.random() * 2000));
            } catch (InterruptedException e) {
                e.printStackTrace();
                logger.error("Error thread " + e);
            }
        }
    }
}

class Account{

    public String ID;
    public int Money;

    public Account(String ID, int money) {
        this.ID = ID;
        Money = money;
    }
}

public class App {

    static int countThreads = 2;//Количество создаваемых потоков при запуске приложения
    static int countAccount = 4;//Количество создаваемых счетов при запуске приложения
    static int countTransaction = 30;//Количество транзакций

    public int getCountTransaction() {
        return countTransaction;
    }

    public static void main(String[] args) {

        MyThread thread = new MyThread();
        thread.listObj = new ArrayList<>();

        newAccount(countAccount, thread.listObj);//Создание счетов

        List<Thread> threads = new ArrayList<>();//Коллекция, в которой хранятся все потоки
        if(countThreads>countTransaction){
            countThreads = countTransaction;
        }
        newThread(countThreads,threads);//Создание потоков
        for(int i=0;i<threads.size();i++){
            threads.get(i).start();
        }
    }

    public static void newAccount(int count, List<Account> list){

        for(int i=0;i < count;i++){
            String id = "" +(int) (Math.random() * (10000+count));
            Account a1 = new Account(id, 10000);
            list.add(a1);
            //проверка для того, чтобы не было счетов с одинаковым ID
            if (list.size()>1){
                for(int j = 0; j< list.size()-1; j++){
                    if(list.get(j).ID.equals(list.get(i).ID)){
                        list.remove(i);
                        i--;
                        break;
                    }
                }
            }
        }
    }
    public static void newThread(int count, List<Thread> list){
        for(int i=0;i<count;i++){
            Thread thread = new Thread(new MyThread());
            list.add(thread);
        }
    }

}
