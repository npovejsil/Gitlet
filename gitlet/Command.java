package gitlet;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Map;
import java.util.List;
import java.util.Set;

/**
 * All the commands for Gitlet.
 * @author Nora Povejsil
 */
public class Command {
    /**
     * Main metadata folder.
     */
    static final File CWD = new File(".");

    /**
     * Main metadata folder.
     */
    static final File GITLET_FOLDER = Utils.join(CWD, ".gitlet");

    /**
     * Commits folder within GITLET_FOLDER.
     */
    static final File COMMIT_DIR = Utils.join(GITLET_FOLDER, "COMMIT_DIR");

    /**
     * Removal subfolder in STAGE_DIR.
     */
    static final File STAGE_REMOVAL = Utils.join(GITLET_FOLDER,
            "STAGE_REMOVAL");

    /**
     * Add subfolder in STAGE_DIR.
     */
    static final File STAGE_ADD = Utils.join(GITLET_FOLDER, "STAGE_ADD");

    /**
     * HEAD pointer to most recent commit on current branch in COMMIT_DIR.
     */
    private static Commit head = null;

    /**
     * Master branch in BRANCHES dir.
     */
    static final String MASTER = "";

    /**
     * Blobs subfolder in COMMIT_DIR.
     */
    static final File BLOBS = Utils.join(GITLET_FOLDER, "BLOBS");

    /**
     * Branches subfolder in GITLET_FOLDER.
     */
    static final File BRANCHES = Utils.join(GITLET_FOLDER, "BRANCHES");

    /**
     * Active Branch file in GITLET_FOLDER.
     */
    static final File ACTIVE_BRANCH = Utils.join(GITLET_FOLDER,
            "ACTIVE_BRANCH");

    /**
     * Creates a new Gitlet version-control system in the current directory.
     */
    @SuppressWarnings("unchecked")
    public static void init() {
        File git = new File(".gitlet");
        if (git.exists()) {
            System.out.println("A gitlet version-control system "
                    + "already exists in the current directory.");
            return;
        } else {
            GITLET_FOLDER.mkdir();
            COMMIT_DIR.mkdirs();
            STAGE_ADD.mkdirs();
            STAGE_REMOVAL.mkdirs();
            BLOBS.mkdirs();
            BRANCHES.mkdirs();
            try {
                ACTIVE_BRANCH.createNewFile();
            } catch (IOException ioException) {
                throw new GitletException("wrong");
            }
            Utils.writeContents(ACTIVE_BRANCH, "master");
            Commit initialCommit = new Commit("initial commit",
                    new TreeMap<>(), null, null, true);
            byte[] initialCommitSerial = Utils.serialize(initialCommit);
            String initialCommitSHA1 = Utils.sha1(initialCommitSerial);
            File pointer = Utils.join(BRANCHES, "master");
            try {
                pointer.createNewFile();
            } catch (IOException ioException) {
                throw new GitletException((""));
            }

            Utils.writeContents(pointer, initialCommitSHA1);
            File initialCommitFile = Utils.join(COMMIT_DIR, initialCommitSHA1);
            Utils.writeObject(initialCommitFile, initialCommit);
            head = initialCommit;
            File initialBranch = Utils.join(BRANCHES,
                    Utils.readContentsAsString(ACTIVE_BRANCH));
            Utils.writeContents(initialBranch, initialCommitSHA1);
        }
    }

    /**
     * Saves a snapshot of tracked files in the current commit and staging area
     * so they can be restored at a later time, creating a new commit.
     * @param msg
     */
    @SuppressWarnings(("unchecked"))
    public static void commit(String msg) {
        File[] allFiles = STAGE_ADD.listFiles();
        File[] removeFiles = STAGE_REMOVAL.listFiles();
        if (allFiles.length == 0 && removeFiles.length == 0) {
            System.out.println("No changes added to the commit.");
            return;
        } else if (msg.equals("")) {
            System.out.println("Please enter a commit message.");
            return;
        } else {
            TreeMap<String, String> commits = new TreeMap<>();
            for (int i = 0; i < allFiles.length; i += 1) {
                String blob = Utils.readContentsAsString(allFiles[i]);
                String blobSHA1 = Utils.sha1(blob);
                commits.put(allFiles[i].getName(), blobSHA1);
                File blobFile = Utils.join(BLOBS, blobSHA1);
                Utils.writeContents(blobFile, blob);
            }
            Commit c = new Commit(msg, commits, getRecentCommit(),
                    null, false);
            byte[] serializedC = Utils.serialize(c);
            String cSHA1 = Utils.sha1(serializedC);
            File cStored = Utils.join(COMMIT_DIR, cSHA1);
            Utils.writeObject(cStored, c);
            deleteFolder(STAGE_ADD);
            STAGE_ADD.mkdirs();
            head = c;
            File branchPointer = Utils.join(BRANCHES,
                    Utils.readContentsAsString(ACTIVE_BRANCH));
            Utils.writeContents(branchPointer, cSHA1);
            if (removeFiles.length > 0) {
                for (int i = 0; i < STAGE_REMOVAL.listFiles().length; i += 1) {
                    stageRemoveRemove(STAGE_REMOVAL.listFiles()[i].getName());
                }
            }
        }
    }

    /** Delete folder.
     * @param folder
     */
    private static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }
    /** Get recent commit.
     * @return commitSha1
     */

    public static String getRecentCommit() {
        String branchName = Utils.readContentsAsString(ACTIVE_BRANCH);
        File f = Utils.join(BRANCHES, branchName);
        String recentCommitSha1 = Utils.readContentsAsString(f);
        return recentCommitSha1;
    }


    /**
     * Adds a copy of the file as it currently exists to the staging area
     * (see the description of the commit command).
     * @param file
     */
    public static void add(String file) {
        File convert = gitlet.Utils.join(CWD, file);
        if (!convert.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        String blob = Utils.readContentsAsString(convert);
        String blobSHA1 = Utils.sha1(blob);
        String recentCommitSha1 = getRecentCommit();
        Commit recentCommit = readCommit(recentCommitSha1);
        TreeMap<String, String> blobMap = recentCommit.getBlobs();
        if (blobMap.containsKey(file)) {
            String commitBlobSHA1 = blobMap.get(file);
            if (!commitBlobSHA1.equals(blobSHA1)) {
                stageAdd(file, blob);
                stageRemoveRemove(file);
            } else {
                stageAddRemove(file);
                stageRemoveRemove(file);
                return;
            }
        } else {
            stageAdd(file, blob);
        }
    }

    /**
     * Adds a copy of the file as it currently exists to the staging area
     * (see the description of the commit command).
     * @param file
     * @param blob
     */
    public static void stageAdd(String file, String blob) {
        File addFile = Utils.join(STAGE_ADD, file);
        if (addFile.exists()) {
            Utils.writeContents(addFile, blob);
            return;
        }
        try {
            addFile.createNewFile();
        } catch (IOException ioException) {
            throw new GitletException("wrong");
        }
        Utils.writeContents(addFile, blob);
    }
    /**
     * Adds a copy of the file as it currently exists to the staging area
     * (see the description of the commit command).
     * @param file
     */

    public static void stageRemoveRemove(String file) {
        File removeFile = Utils.join(STAGE_REMOVAL, file);
        if (removeFile.exists()) {
            removeFile.delete();
            return;
        }
    }
    /**
     * Adds a copy of the file as it currently exists to the staging area
     * (see the description of the commit command).
     * @param file
     */

    public static void stageAddRemove(String file) {
        File addFile = Utils.join(STAGE_ADD, file);
        if (addFile.exists()) {
            addFile.delete();
            return;
        }
    }
    /**
     * Adds a copy of the file as it currently exists to the staging area
     * (see the description of the commit command).
     * @param commitSHA1
     * @return commit
     */

    public static Commit readCommit(String commitSHA1) {
        File commitFile = Utils.join(COMMIT_DIR, commitSHA1);
        if (!commitFile.exists()) {
            throw new GitletException("no file");
        }
        Commit commit = Utils.readObject(commitFile, Commit.class);
        return commit;
    }

    /**
     * Starting at the current head commit, display information
     * about each commit backwards along the
     * commit tree until the initial commit, following the first parent
     * commit links, ignoring any
     * second parents found in merge commits.
     */

    public static void log() {
        String recentCommitSHA1 = getRecentCommit();
        File commitHolder = Utils.join(COMMIT_DIR, recentCommitSHA1);
        head = Utils.readObject(commitHolder, Commit.class);
        while (head.getParent() != null) {
            Commit commit = head;
            byte[] serializedCommit = Utils.serialize(commit);
            System.out.println("===");
            System.out.println("commit " + Utils.sha1(serializedCommit));
            String parent2ID = commit.getsecondParent();
            System.out.println("Date: " + commit.getTimeStamp());
            System.out.println(commit.getMessage());
            System.out.println();
            if (parent2ID != null) {
                String parent1ID = commit.getParent();
                System.out.println("Merge: " + parent1ID.substring(0, 6)
                        + " " + parent2ID.substring(0, 6));
            }
            String parentSHA1 = head.getParent();
            File parentHolder = Utils.join(COMMIT_DIR, parentSHA1);
            head = Utils.readObject(parentHolder, Commit.class);
        }
        Commit initialCommit = head;
        byte[] serializedInitalCommit = Utils.serialize(initialCommit);
        System.out.println("===");
        System.out.println("commit " + Utils.sha1(serializedInitalCommit));
        System.out.println("Date: " + initialCommit.getTimeStamp());
        System.out.println(initialCommit.getMessage());
        System.out.println();
    }

    /**
     * Checkout is a general command that can do a few different things
     * depending
     * on what its arguments are.
     * Write three versions with the same name but that have different input
     * format
     * java gitlet.Main checkout -- [file name]
     * java gitlet.Main checkout [commit id] -- [file name]
     * java gitlet.Main checkout [branch name]
     * @param args
     */
    public static void checkout(String... args) {
        if (args.length > 4) {
            System.out.println("Incorrect operands.");
        }
        if (args.length == 3) {
            if (!args[1].equals("--")) {
                System.out.println("Incorrect operands.");
            }
            Command.checkoutFile(args[2]);
        } else if (args.length == 4) {
            if (!args[2].equals("--")) {
                System.out.println("Incorrect operands.");
            }
            Command.checkoutCommit(args[1], args[3]);
        } else if (args.length == 2) {
            Command.checkoutBranch(args[1]);
        }
    }
    /**
     * Adds a copy of the file as it currently exists to the staging area
     * (see the description of the commit command).
     * @param file
     */

    public static void checkoutFile(String file) {
        File headCommit = Utils.join(COMMIT_DIR, getRecentCommit());
        head = Utils.readObject(headCommit, Commit.class);
        if (!head.getBlobs().containsKey(file)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        String cwdBlob = head.getBlobs().get(file);
        File blobHolder = Utils.join(BLOBS, cwdBlob);
        String blob = Utils.readContentsAsString(blobHolder);
        File holder = Utils.join(CWD, file);
        Utils.writeContents(holder, blob);
    }
    /**
     * Adds a copy of the file as it currently exists to the staging area
     * (see the description of the commit command).
     * @param commitID
     * @param file
     */

    public static void checkoutCommit(String commitID, String file) {
        File commitFile = Utils.join(COMMIT_DIR, commitID);
        if (commitID.length() < Utils.UID_LENGTH) {
            for (File fileName : COMMIT_DIR.listFiles()) {
                String name = fileName.getName();
                if (name.contains(commitID)) {
                    commitID = name;
                    commitFile = Utils.join(COMMIT_DIR, fileName.getName());
                }
            }
        }

        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit commit = Utils.readObject(commitFile, Commit.class);
        if (!commit.getBlobs().containsKey(file)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        File holder = Utils.join(CWD, file);
        TreeMap<String, String> blobs = commit.getBlobs();
        String blobID = blobs.get(file);
        File blobHolderFile = Utils.join(BLOBS, blobID);
        String blob = Utils.readContentsAsString(blobHolderFile);
        Utils.writeContents(holder, blob);

    }

    /**
     * Takes all files in the commit at the head of the given branch,
     * and puts them in the working directory,
     * overwriting the versions of the files that are already there if
     * they exist.Also, at the end of this command,
     * the given branch will now be considered the current branch (HEAD).
     * Any files that
     * are tracked in the current
     * branch but are not present in the checked-out branch are deleted. The
     * staging
     * area is cleared, unless the
     * checked-out branch is the current branch (see Failure cases below).
     * If that branch is the current branch, print No need to checkout the
     * current branch.
     * If a working file is
     * untracked in the current branch and would be overwritten by the
     * checkout,
     * print There is an untracked file in the way; delete it, or add and
     * commit
     * it first.
     * and exit; perform this check
     * before doing anything else.
     * @param branch
     */
    public static void checkoutBranch(String branch) {
        File branchFile = Utils.join(BRANCHES, branch);
        if (!branchFile.exists()) {
            System.out.println("No such branch exists.");
            return;
        } else if (branch.equals(Utils.readContentsAsString(ACTIVE_BRANCH))) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        Commit branchCommit = getBranchCommit(branchFile);
        TreeMap<String, String> branchBlobMap = branchCommit.getBlobs();
        Commit recentCommit = getRCommit();
        TreeMap<String, String> blobMap = recentCommit.getBlobs();
        File[] cwdFiles = CWD.listFiles();
        for (int i = 0; i < cwdFiles.length; i += 1) {
            String fileName = cwdFiles[i].getName();
            File file = Utils.join(CWD, fileName);
            if (!fileName.equals(".gitlet")) {
                String fileBlob = Utils.readContentsAsString(file);
                if (branchBlobMap.containsKey(fileName)) {
                    if (!blobMap.containsKey(fileName)
                            && !branchBlobMap.get(fileName).equals(
                                    Utils.sha1(fileBlob))) {
                        System.out.println("There is an untracked file in the w"
                                + "ay; delete it, or add and commit it first.");
                        return;
                    }
                }
            }
        }
        File[] addFiles = STAGE_ADD.listFiles();
        for (int i = 0; i < addFiles.length; i += 1) {
            if (!blobMap.containsKey(addFiles[i].getName())) {
                if (branchBlobMap.containsKey(addFiles[i].getName())) {
                    System.out.println("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                    return;
                }
            }
        }
        Set<Map.Entry<String, String>> entries = blobMap.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            if (!branchBlobMap.containsKey(entry.getKey())) {
                File file = Utils.join(CWD, entry.getKey());
                if (file.exists()) {
                    file.delete();
                }
            }
        }
        Set<Map.Entry<String, String>> branchEntries = branchBlobMap.entrySet();
        for (Map.Entry<String, String> entry : branchEntries) {
            File newCwd = Utils.join(CWD, entry.getKey());
            File blobFile = Utils.join(BLOBS, entry.getValue());
            String blob = Utils.readContentsAsString(blobFile);
            Utils.writeContents(newCwd, blob);
        }
        Utils.writeContents(ACTIVE_BRANCH, branch);
        deleteFolder(STAGE_ADD);
        STAGE_ADD.mkdirs();
    }
    /**
     * Get file.
     * @return File
     */
    public static Commit getRCommit() {
        File recentCommitFile = Utils.join(COMMIT_DIR, getRecentCommit());
        Commit recentCommit = Utils.readObject(recentCommitFile, Commit.class);
        return recentCommit;
    }

    /**
     * Get branch commit.
     * @param branchFile
     * @return commit
     */
    public static Commit getBranchCommit(File branchFile) {
        String branchCommitID = Utils.readContentsAsString(branchFile);
        File branchCommitFile = Utils.join(COMMIT_DIR, branchCommitID);
        Commit branchCommit = Utils.readObject(branchCommitFile, Commit.class);
        return branchCommit;
    }
    /**
     * Unstage the file if it is currently staged for addition.
     * If the file is tracked in the current commit,
     * stage it for removal and remove the file from the working directory
     * if the user has not already done so
     * (do not remove it unless it is tracked in the current commit).
     * Failure cases: If the file is neither staged nor tracked by the head
     * commit, print the error message No reason to
     * remove the file.
     * @param file
     */
    public static void rm(String file) {
        File headCommit = Utils.join(COMMIT_DIR, getRecentCommit());
        head = Utils.readObject(headCommit, Commit.class);
        File fileStage = Utils.join(STAGE_ADD, file);
        if (!head.getBlobs().containsKey(file) && !fileStage.exists()) {
            System.out.println("No reason to remove the file.");
            return;
        }
        if (head.getBlobs().containsKey(file)) {
            String blobSHA1 = head.getBlobs().get(file);
            File fileBlobfile = Utils.join(BLOBS, blobSHA1);
            String fileBlob = Utils.readContentsAsString(fileBlobfile);
            File fileRemoval = Utils.join(STAGE_REMOVAL, file);
            Utils.writeContents(fileRemoval, fileBlob);
            stageAddRemove(file);
            File fileCWD = Utils.join(CWD, file);
            if (fileCWD.exists()) {
                Utils.restrictedDelete(fileCWD);
            }
        } else if (fileStage.exists()) {
            stageAddRemove(file);
        }
    }

    /**
     * Like log, except displays information about all commits ever made.
     * The order of the commits does not matter.
     */
    public static void globalLog() {

        File[] allFiles = COMMIT_DIR.listFiles();
        for (int i = 0; i < allFiles.length; i += 1) {
            Commit commit = Utils.readObject(allFiles[i], Commit.class);
            byte[] serializedCommit = Utils.serialize(commit);
            System.out.println("===");
            System.out.println("commit " + Utils.sha1(serializedCommit));
            System.out.println("Date: " + commit.getTimeStamp());
            System.out.println(commit.getMessage());
            System.out.println();
        }
    }


    /**
     * Prints out the ids of all commits that have the given commit message,
     * one per line. If there are
     * multiple such commits, it prints the ids out on separate lines.
     * @param msg
     */
    public static void find(String msg) {
        File[] allFiles = COMMIT_DIR.listFiles();
        int count = 0;
        for (int i = 0; i < allFiles.length; i += 1) {
            Commit commit = Utils.readObject(allFiles[i], Commit.class);
            if (commit.getMessage().equals(msg)) {
                byte[] serializedCommmit = Utils.serialize(commit);
                count += 1;
                System.out.println(Utils.sha1(serializedCommmit));
            }
        }
        if (count == 0) {
            System.out.println("Found no commit with that message.");
        }
    }

    /**
     * Displays what branches currently exist, and marks the current branch
     * with a *. Also displays what
     * files have been staged for addition or removal. An example of the exact
     * format it should follow is as follows.
     */
    public static void status() {
        String activeBranch = Utils.readContentsAsString(ACTIVE_BRANCH);
        System.out.println("=== Branches ===");
        File[] branchFiles = BRANCHES.listFiles();
        for (int i = branchFiles.length - 1; i > -1; i -= 1) {
            String branchName = branchFiles[i].getName();
            if (branchName.equals(activeBranch)) {
                System.out.println('*' + activeBranch);
            } else {
                System.out.println(branchFiles[i].getName());
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        File[] stageFiles = STAGE_ADD.listFiles();
        for (int i = 0; i < stageFiles.length; i += 1) {
            System.out.println(stageFiles[i].getName());
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        File[] removeFiles = STAGE_REMOVAL.listFiles();
        for (int i = 0; i < removeFiles.length; i += 1) {
            System.out.println(removeFiles[i].getName());
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
    }

    /**
     * Creates a new branch with the given name, and points it at the
     * current
     * head node.
     * A branch is nothing more than a name for a reference (a SHA-1
     * identifier)
     * to a commit node.
     * This command does NOT immediately switch to the newly created
     * branch
     * (just as in real Git).
     * Before you ever call branch, your code should be running with
     * a default branch
     * called "master".
     * @param branch
     */
    public static void branch(String branch) {
        File[] branchFiles = BRANCHES.listFiles();
        for (int i = 0; i < branchFiles.length; i += 1) {
            if (branchFiles[i].getName().equals(branch)) {
                System.out.println("A branch with that name already "
                        + "exists.");
                return;
            }
        }
        File newBranch = Utils.join(BRANCHES, branch);
        File headHolder = Utils.join(COMMIT_DIR, getRecentCommit());
        head = Utils.readObject(headHolder, Commit.class);
        byte[] serializedHead = Utils.serialize(head);
        String headSHA1 = Utils.sha1(serializedHead);
        Utils.writeContents(newBranch, headSHA1);

    }

    /**
     * Deletes the branch with the given name. This only means to delete
     * the
     * pointer associated with the branch;
     * it does not mean to delete all commits that were created under the
     * branch,
     * or anything like that.
     * @param branch
     */
    public static void rmBranch(String branch) {
        File branchName = Utils.join(BRANCHES, branch);
        if (!branchName.exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (Utils.readContentsAsString(ACTIVE_BRANCH).equals(branch)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        branchName.delete();
    }

    /**
     * Checks out all the files tracked by the given commit. Removes tracked
     * files that are not
     * present in that commit. Also moves the current branch's head to that
     * commit node.
     * See the intro for an
     * example of what happens to the head pointer after using reset.
     * @param commitID
     */
    public static void reset(String commitID) {
        File[] cwdFiles = CWD.listFiles();
        File recentCommitFile = Utils.join(COMMIT_DIR, getRecentCommit());
        Commit recentCommit = Utils.readObject(recentCommitFile, Commit.class);
        TreeMap<String, String> recentCommitBlobs = recentCommit.getBlobs();

        File commitFile = Utils.join(COMMIT_DIR, commitID);
        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit commit = Utils.readObject(commitFile, Commit.class);
        TreeMap<String, String> blobMap = commit.getBlobs();

        for (int i = 0; i < cwdFiles.length; i += 1) {
            File file = Utils.join(CWD, cwdFiles[i].getName());
            if (file.exists()) {
                if (!file.getName().equals(".gitlet")) {
                    String blob = Utils.readContentsAsString(file);
                    String blobSHA1 = Utils.sha1(blob);
                    if (blobMap.containsKey(file.getName())
                            && !recentCommitBlobs.containsKey(file.getName())) {
                        if (!blobSHA1.equals(blobMap.get(file.getName()))) {
                            System.out.println("There is an untracked "
                                    + "file in the way; delete it, or add and "
                                    + "commit it first.");
                            return;
                        }
                    }
                }
            }
        }

        Set<Map.Entry<String, String>> entries = blobMap.entrySet();

        for (Map.Entry<String, String> entry : entries) {
            checkoutCommit(commitID, entry.getKey());
        }
        Set<Map.Entry<String, String>> recentEntries =
                recentCommitBlobs.entrySet();

        for (Map.Entry<String, String> entry : recentEntries) {
            if (!blobMap.containsKey(entry.getKey())) {
                rm(entry.getKey());
            }
        }

        File commitFolder = Utils.join(BRANCHES,
                Utils.readContentsAsString(ACTIVE_BRANCH));
        Utils.writeContents(commitFolder, commitID);
        deleteFolder(STAGE_REMOVAL);
        STAGE_REMOVAL.mkdirs();
        deleteFolder(STAGE_ADD);
        STAGE_ADD.mkdirs();

    }

    /**
     * Merges files from the given branch into the current branch.
     * This method is a bit
     * complicated,
     * so here's a more detailed description:
     * <p>
     * Failure cases: If there are staged additions or removals present,
     * print the error
     * message
     * You have uncommitted
     * changes. and exit.
     * If a branch with the given name does not exist, print the error
     * message
     * A branch with that
     * name does not exist.
     * If attempting to merge a branch with itself, print the error message
     * Cannot merge a branch with itself.
     * If merge would generate an error because the commit that it does has no
     * changes in it, just let the normal
     * commit error message for this go through.
     * If an untracked file in the current commit would be overwritten or
     * deleted by the merge, print There is an
     * untracked file in the way; delete it, or add and commit it first. and
     * exit; perform this check before doing anything else.
     * @param branch
     */
    public static void merge(String branch) {
        Commit childCommit = null;
        TreeMap<String, String> childBlobs = new TreeMap<>();
        File[] cwdFiles = CWD.listFiles();
        String recentCommitSHA1 = getRecentCommit();
        File commitHolder = Utils.join(COMMIT_DIR, recentCommitSHA1);
        head = Utils.readObject(commitHolder, Commit.class);
        TreeMap<String, String> blobMap = head.getBlobs();
        List<String> branches = Utils.plainFilenamesIn(BRANCHES);
        if (!branches.contains(branch)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        File branchHolder = Utils.join(BRANCHES, branch);
        String branchCommitID = Utils.readContentsAsString(branchHolder);
        File branchCommitHolder = Utils.join(COMMIT_DIR, branchCommitID);
        Commit branchCommit = Utils.readObject(branchCommitHolder,
                Commit.class);
        TreeMap<String, String> branchBlobs = branchCommit.getBlobs();

        boolean encounteredMergeConflict = false;
        for (int i = 0; i < cwdFiles.length; i += 1) {
            File file = Utils.join(CWD, cwdFiles[i].getName());
            if (!blobMap.containsKey(file.getName())
                    && branchBlobs.containsKey(file.getName())) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                return;
            }
        }
        File[] addFiles = STAGE_ADD.listFiles();
        File[] removeFiles = STAGE_REMOVAL.listFiles();
        String currentBranch = Utils.readContentsAsString(ACTIVE_BRANCH);
        if (addFiles.length > 0) {
            System.out.println("You have uncommitted changes.");
            return;
        } else if (removeFiles.length > 0) {
            System.out.println("You have uncommitted changes.");
            return;
        } else if (currentBranch.equals(branch)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        Commit splitPoint = getSplitPoint(branchCommit);
        if (splitPoint == head) {
            checkoutBranch(currentBranch);
            System.out.println("Current branch fast-forwarded.");
            return;
        }
        if (mergeHelper(branch, encounteredMergeConflict)) {
            encounteredMergeConflict = true;
        }
        String msg = "Merged " + branch + " into " + currentBranch + ".";
        childCommit = new Commit(msg, childBlobs, recentCommitSHA1,
                branchCommitID, false);
        finalBit(childCommit, removeFiles);
        if (encounteredMergeConflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }
    /** Commit ending part of merge method.
     * @param branch
     * @param encounteredMergeConflict1
     * @return boolean
     */
    public static boolean mergeHelper(String branch, boolean
            encounteredMergeConflict1) {
        Commit childCommit = null;
        TreeMap<String, String> childBlobs = new TreeMap<>();
        File[] cwdFiles = CWD.listFiles();
        String recentCommitSHA1 = getRecentCommit();
        File commitHolder = Utils.join(COMMIT_DIR, recentCommitSHA1);
        head = Utils.readObject(commitHolder, Commit.class);
        TreeMap<String, String> blobMap = head.getBlobs();
        File branchHolder = Utils.join(BRANCHES, branch);
        String branchCommitID = Utils.readContentsAsString(branchHolder);
        File branchCommitHolder = Utils.join(COMMIT_DIR,
                branchCommitID);
        Commit branchCommit = Utils.readObject(branchCommitHolder,
                Commit.class);
        TreeMap<String, String> branchBlobs = branchCommit.getBlobs();
        String currentBranch = Utils.readContentsAsString(ACTIVE_BRANCH);
        File[] removeFiles = STAGE_REMOVAL.listFiles();
        Commit splitPoint = getSplitPoint(branchCommit);
        boolean encounteredMergeConflict = encounteredMergeConflict1;
        TreeMap<String, String> splitPointBlobs = splitPoint.getBlobs();
        for (int i = 0; i < cwdFiles.length; i += 1) {
            File file = Utils.join(CWD, cwdFiles[i].getName());
            if (!branchBlobs.containsKey(file.getName())
                    && !blobMap.containsKey(file.getName())
                    && splitPointBlobs.containsKey(file.getName())) {
                file.delete();
            }
        }
        ArrayList<String> allFiles = new ArrayList<>();
        Set<Map.Entry<String, String>> splitEntries =
                splitPointBlobs.entrySet();
        for (Map.Entry<String, String> entry : splitEntries) {
            String filename = entry.getKey();
            allFiles.add(filename);
        }
        Set<Map.Entry<String, String>> branchEntries = branchBlobs.entrySet();
        for (Map.Entry<String, String> entry : branchEntries) {
            String filename = entry.getKey();
            if (!allFiles.contains(filename)) {
                allFiles.add(filename);
            }
        }
        Set<Map.Entry<String, String>> commitEntries = blobMap.entrySet();
        for (Map.Entry<String, String> entry : commitEntries) {
            String filename = entry.getKey();
            if (!allFiles.contains(filename)) {
                allFiles.add(filename);
            }
        }
        encounteredMergeConflict = doForLoop(allFiles, branchBlobs,
                splitPointBlobs, blobMap,
                branchCommitID, childBlobs, encounteredMergeConflict);
        return encounteredMergeConflict;
    }
    /**
     * For loop bit.
     * @param splitPointBlobs
     * @param blobMap
     * @param allFiles
     * @param branchBlobs
     * @param branchCommitID
     * @param childBlobs
     * @param encounteredMergeConflict
     * @return boolean
     */
    public static boolean doForLoop(ArrayList<String> allFiles,
                                 TreeMap<String, String> branchBlobs,
                                 TreeMap<String, String> splitPointBlobs,
                                 TreeMap<String, String> blobMap,
                                 String branchCommitID,
                                 TreeMap<String, String> childBlobs,
                                 boolean encounteredMergeConflict) {
        for (int i = 0; i < allFiles.size(); i += 1) {
            String fileName = allFiles.get(i);

            if (isChange(fileName, branchBlobs, splitPointBlobs)
                    && !isChange(fileName, blobMap, splitPointBlobs)) {
                if (branchBlobs.containsKey(fileName)) {
                    checkoutCommit(branchCommitID, fileName);
                    childBlobs.put(fileName, branchBlobs.get(fileName));
                }
            } else if (isChange(fileName, branchBlobs, splitPointBlobs)
                    && isChange(fileName, blobMap, splitPointBlobs)
                    && isChange(fileName, blobMap, branchBlobs)) {
                if (blobMap.containsKey(fileName)
                        && branchBlobs.containsKey(fileName)
                        && splitPointBlobs.containsKey(fileName)) {
                    String blobIDGiven = branchBlobs.get(fileName);
                    String blobIDCurrent = blobMap.get(fileName);
                    File blobFileGiven = Utils.join(BLOBS, blobIDGiven);
                    File blobFileCurrent = Utils.join(BLOBS, blobIDCurrent);
                    String blobCurrent =
                            Utils.readContentsAsString(blobFileCurrent);
                    String blobGiven =
                            Utils.readContentsAsString(blobFileGiven);
                    File cwdFile = Utils.join(CWD, fileName);
                    Utils.writeContents(cwdFile, "<<<<<<< HEAD\n"
                            + blobCurrent  + "=======\n" + blobGiven
                            + ">>>>>>>\n");
                    add(fileName);
                    encounteredMergeConflict = true;
                }
            } else {
                if (blobMap.containsKey(fileName)
                        && branchBlobs.containsKey(fileName)) {
                    checkoutCommit(getRecentCommit(), fileName);
                    childBlobs.put(fileName, blobMap.get(fileName));
                }
                if (blobMap.containsKey(fileName)) {
                    checkoutCommit(getRecentCommit(), fileName);
                    childBlobs.put(fileName, blobMap.get(fileName));
                }
                if (!blobMap.containsKey(fileName)) {
                    File cwdFile = Utils.join(CWD, fileName);
                    if (cwdFile.exists()) {
                        cwdFile.delete();
                    }
                }
            }
        }
        return encounteredMergeConflict;
    }
    /** Save the commit, etc.
     * @param childCommit
     * @param removeFiles
     */
    public static void finalBit(Commit childCommit, File[] removeFiles) {
        byte[] serializedChildCommit = Utils.serialize(childCommit);
        String childCommitSHA1 = Utils.sha1(serializedChildCommit);
        File childCommitFile = Utils.join(COMMIT_DIR, childCommitSHA1);
        Utils.writeObject(childCommitFile, childCommit);
        deleteFolder(STAGE_ADD);
        STAGE_ADD.mkdirs();
        head = childCommit;
        File branchPointer = Utils.join(BRANCHES,
                Utils.readContentsAsString(ACTIVE_BRANCH));
        Utils.writeContents(branchPointer, childCommitSHA1);
        if (removeFiles.length > 0) {
            for (int i = 0; i < STAGE_REMOVAL.listFiles().length; i += 1) {
                stageRemoveRemove(STAGE_REMOVAL.listFiles()[i].getName());
            }
        }
    }
    /** Get split point.
     * @param branchCommit
     * @return splitPoint
     */
    public static Commit getSplitPoint(Commit branchCommit) {
        Commit splitPoint = null;
        ArrayList<Commit> work = new ArrayList<Commit>();
        HashSet<String> commitSet = new HashSet<>();
        work.add(branchCommit);
        while (!work.isEmpty()) {
            Commit node = work.remove(0);
            byte[] serializedNode = Utils.serialize(node);
            String nodeSHA1 = Utils.sha1(serializedNode);
            commitSet.add(nodeSHA1);
            if (node.getParent() != null) {
                File parent1File = Utils.join(COMMIT_DIR, node.getParent());
                Commit parent1 = Utils.readObject(parent1File, Commit.class);
                work.add(parent1);
            }
            if (node.getsecondParent() != null) {
                File parent2File = Utils.join(COMMIT_DIR,
                        node.getsecondParent());
                Commit parent2 = Utils.readObject(parent2File, Commit.class);
                work.add(parent2);
            }
        }

        String recentCommitSHA1 = getRecentCommit();
        File commitHolder = Utils.join(COMMIT_DIR, recentCommitSHA1);
        head = Utils.readObject(commitHolder, Commit.class);
        ArrayList<Commit> currWork = new ArrayList<Commit>();
        currWork.add(head);
        while (!currWork.isEmpty()) {
            Commit currNode = currWork.remove(0);
            byte[] serializedCurrNode = Utils.serialize(currNode);
            String currNodeSHA1 = Utils.sha1(serializedCurrNode);
            if (commitSet.contains(currNodeSHA1)) {
                splitPoint = currNode;
                break;
            }
            if (currNode.getParent() != null) {
                File currParent1File = Utils.join(COMMIT_DIR,
                        currNode.getParent());
                Commit currParent1 = Utils.readObject(currParent1File,
                        Commit.class);
                currWork.add(currParent1);
            }
            if (currNode.getsecondParent() != null) {
                File currParent2File = Utils.join(COMMIT_DIR,
                        currNode.getsecondParent());
                Commit currParent2 = Utils.readObject(currParent2File,
                        Commit.class);
                currWork.add(currParent2);
            }
        }
        return splitPoint;
    }
    /** Get if is change.
     * @param filename
     * @param blobMap1
     * @param blobMap2
     * @return boolean
     */
    public static boolean isChange(String filename,
                                    TreeMap<String, String> blobMap1,
                                    TreeMap<String, String> blobMap2) {

        if (blobMap1.containsKey(filename)
                && blobMap2.containsKey(filename)) {
            if (blobMap1.get(filename).equals(blobMap2.get(filename))) {
                return false;
            }
        }
        if (!blobMap1.containsKey(filename)
                && !blobMap2.containsKey(filename)) {
            return false;
        }
        return true;
    }
    /**
     * Get head commit.
     * @return head
     */
    public Commit getHead() {
        return head;
    }

}
