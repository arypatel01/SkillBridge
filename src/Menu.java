import Util.InputUtil;

public class Menu {

    static int currentStudentId = -1;
    static Student loggedInStudent = null;
    Login obj = new Login();
    Validations vObj = new Validations();
    private final HistoryService historyService;

    public Menu(HistoryService historyService) {
        this.historyService = historyService;
    }

    void studentMenu() throws Exception {
        int choice = 0;
        boolean flag = true;
        while (flag) {
            System.out.println("\n\n========================== WELCOME TO SKILLBRIDGE ==========================");
            System.out.println("         SkillBridge - A Skill Exchange Platform for Students");
            System.out.println("===========================================================================");
            System.out.println("  1. Student Signup");
            System.out.println("  2. Student Login");
            System.out.println("  3. Forgot Password");
            System.out.println("  4. Exit");
            System.out.println("---------------------------------------------------------------------------");

            choice = InputUtil.readInt("Enter Choice : ");

            switch (choice){
                case 1 ->{
                    String name = obj.registerStudent();
                    if (name != null) {
                        historyService.logEvent("New student registered: " + name);
                    }
                }
                case 2 ->{
                    if(obj.handleStudentLogin()){
                        historyService.logEvent("Student '" + loggedInStudent.getName() + "' logged in.");
                        loggedInStudent.studentPortal(loggedInStudent,historyService);
                    }
                }
                case 3 ->{
                    String email;
                    while(true){
                        String temp = InputUtil.readString("Enter Email: ");
                        if(vObj.isValidEmail(temp)){
                            email = temp;
                            break;
                        }
                        else{
                            System.out.println("Enter Valid Email!!!");
                        }
                    }
                    obj.handleForgotPassword(email);
                }
                case 4 -> {
                    flag = false;
                }
                default -> {
                    System.out.println("Enter Valid Choice !!!");
                }
            }
        }
    }

    void start() throws Exception {
        Admin a = new Admin(historyService);
        int choice = 0;
        boolean flag = true;
        while (flag){
            System.out.println("\n\n========================== WELCOME TO SKILLBRIDGE ==========================");
            System.out.println("         SkillBridge - A Skill Exchange Platform for Students");
            System.out.println("===========================================================================");
            System.out.println("  1. Admin");
            System.out.println("  2. Student");
            System.out.println("  3. exit");
            System.out.println("---------------------------------------------------------------------------");

            choice = InputUtil.readInt("Enter Choice : ");

            switch (choice){
                case 1 -> {
                    if( obj.handleAdminLogin()){
                        historyService.logEvent("Admin logged in.");
                        a.start();
                        historyService.logEvent("Admin logged out.");
                    }
                    else System.out.println("Invalid");
                }

                case 2 -> {
                    studentMenu();
                }

                case 3 -> {
                    flag = false;
                    System.out.println("Thanks For Visiting our Platform!!!");
                }
                default -> {
                    System.out.println("Enter Valid Choice !!!");
                }
            }
        }
    }
}
