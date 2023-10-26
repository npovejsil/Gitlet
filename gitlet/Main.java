package gitlet;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Nora Povejsil
 */
public class Main {
    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        switch (args[0]) {
        case "init":
            Command.init();
            break;
        case "add":
            Command.add(args[1]);
            break;
        case "commit":
            Command.commit(args[1]);
            break;
        case "checkout":
            Command.checkout(args);
            break;
        case "log":
            Command.log();
            break;
        case "global-log":
            Command.globalLog();
            break;
        case "find":
            Command.find(args[1]);
            break;
        case "status":
            java.io.File file = Utils.join(Command.GITLET_FOLDER);
            if (!file.exists()) {
                System.out.println("Not in an initialized Gitlet directory.");
                return;
            }
            Command.status();
            break;
        case "rm":
            Command.rm(args[1]);
            break;
        case "branch":
            Command.branch(args[1]);
            break;
        case "rm-branch":
            Command.rmBranch(args[1]);
            break;
        case "reset":
            Command.reset(args[1]);
            break;
        case "merge":
            Command.merge(args[1]);
            break;
        case "":
            System.out.println("Please enter a command.");
            break;
        default:
            System.out.println("No command with that name exists.");
            break;
        }
        return;
    }
}
